/*******************************************************************************
 * Copyright (c) 2012, THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *    Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *    Neither the name of the STANFORD UNIVERSITY nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cyclades.nyxlet.servicebrokernyxlet.message.impl.activemq.consumer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.cyclades.engine.MetaTypeEnum;
import org.cyclades.nyxlet.servicebrokernyxlet.message.impl.activemq.ConnectionResource;
import org.cyclades.nyxlet.servicebrokernyxlet.message.impl.activemq.MessageUtils;
import javax.jms.MessageProducer;
import javax.jms.MessageConsumer;
import javax.jms.Connection;
import javax.jms.Session;
import javax.jms.BytesMessage;
import javax.jms.TextMessage;
import javax.jms.Destination;

public class ActiveMQMergingDefaultConsumer extends TimerTask implements ActiveMQConsumer {

    public ActiveMQMergingDefaultConsumer (ConnectionResource connectionResource) throws IOException {
        this.connectionResource = connectionResource;
    }

    public ActiveMQMergingDefaultConsumer init (Map<String, String> parameters) throws Exception {
        final String eLabel = "ActiveMQMergingDefaultConsumer.init: ";
        try {
            if (parameters.containsKey("timer_delay_mills")) timerDelayMills = Long.parseLong(parameters.get("timer_delay_mills"));
            if (parameters.containsKey("timer_period_mills")) timerPeriodMills = Long.parseLong(parameters.get("timer_period_mills"));
            if (parameters.containsKey("accumulation_wait_mills")) accumulationWaitMills = Long.parseLong(parameters.get("accumulation_wait_mills"));
            if (parameters.containsKey("min_messages")) minMessages = Integer.parseInt(parameters.get("min_messages"));
            if (parameters.containsKey(REPLYTO_MESSAGE_DELIVERY_MODE)) replyToMessageDeliveryMode = Integer.parseInt(parameters.get(REPLYTO_MESSAGE_DELIVERY_MODE));
            session = connectionResource.getConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
            consumer = session.createConsumer(session.createQueue(connectionResource.getQueueName()));
            producer = session.createProducer(null);
            if (replyToMessageDeliveryMode > -1) producer.setDeliveryMode(replyToMessageDeliveryMode);
            timer = new Timer();
            timer.schedule(this, timerDelayMills, timerPeriodMills);
            return this;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public void destroy () throws Exception {
        final String eLabel = "ActiveMQMergingDefaultConsumer.destroy: ";
        try { session.close(); } catch (Exception e) {}
        try { consumer.close(); } catch (Exception e) {}
        try { producer.close(); } catch (Exception e) {}
        try {
            timer.cancel();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    @Override
    public void run() {
        final String eLabel = "ActiveMQMergingDefaultConsumer.run: ";
        try {
            accumulateMessages();
        } catch (Exception e) {
            connectionResource.getCallBackServiceInstance().logError(eLabel + e);
        }
    }

    private synchronized void accumulateMessages () throws Exception {
        final String eLabel = "ActiveMQMergingDefaultConsumer.accumulateMessage: ";
        Connection connection = connectionResource.getConnection();
        Session session = null;
        try {
            byte[] body;
            javax.jms.Message jmsMessage = consumer.receive(100);          
            while (jmsMessage != null) {
                if (jmsMessage instanceof BytesMessage) {
                    body = MessageUtils.readBytes((BytesMessage)jmsMessage);
                } else if (jmsMessage instanceof TextMessage) {
                    body = ((TextMessage)jmsMessage).getText().getBytes();
                } else {
                    throw new UnsupportedOperationException("Message type not supported: " + jmsMessage.getClass().getName());
                }
                messages.add(new Message(body, jmsMessage));
                if (accumulationExpired()) break;
                jmsMessage = consumer.receive(100);
            }
            // XXX - ActiveMQ and RabbitMQ behave a bit differently wrt committing/rejecting messages..(ActiveMQ does not
            // have the ability to reject one of N messages independently, or we simply have not found it) so we will greedily hang
            // on to alternate formatted messages until we commit the whole batch...and then send the alternate format messages
            // as new messages...unlike the RabbitMQ consumer that rejects each alternate formatted message almost immediately.
            // This should not be an issue as 1.) it works ad 2.) this is a corner case, as the norm should be that all messages
            // are the same format (XML or JSON)
            //releaseAlternateFormatMessages();
            if (accumulationExpired()) {
                // XXX - think about the policy here...this should work for most conditions...how can we make this better?
                // Let's come back and revisit this as we test it.
                byte[] accumulated = processMessages();
                ackMessages();
                releaseAlternateFormatMessages();
                // XXX - Passing in "null" here as the value of the original request. Since the original request
                // is actually an aggregation of multiple X-STROMA requests, we'll need to build in some sort of
                // List structure to accommodate this later if needed
                if (connectionResource.hasResponseProcessor()) connectionResource.fireResponseProcessor(accumulated, null);
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public synchronized void releaseAlternateFormatMessages () throws Exception {
        final String eLabel = "ActiveMQMergingDefaultConsumer.releaseAlternateFormatMessages: ";
        try { 
            for (Message message : messages.getAlternateFormatMessages()) producer.send(session.createQueue(
                    connectionResource.getQueueName()), message.jmsMessage);
            messages.getAlternateFormatMessages().clear();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public synchronized byte[] processMessages () throws Exception {
        final String eLabel = "ActiveMQMergingDefaultConsumer.processMessage: ";
        try {
            Destination replyTo;
            Map<Destination, Integer> replyToQueueMap = new LinkedHashMap<Destination, Integer>();
            List<String> messageList = new ArrayList<String>();
            for (Message message : messages.getMessages()) {
                messageList.add(new String(message.body, "UTF-8"));
                replyTo = message.jmsMessage.getJMSReplyTo();
                if (replyTo != null) replyToQueueMap.put(replyTo, 1);
            }
            //connectionResource.getCallBackServiceInstance().logError(connectionResource.getConsumerTag() + " Merging messages: " + messageList.size());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            connectionResource.getCallBackServiceInstance().processXSTROMAMessagePayloads(baos, messageList,
                    (messages.messageStartsWith == '{') ? MetaTypeEnum.JSON : MetaTypeEnum.XML);
            byte[] message = baos.toByteArray();
            if (replyToQueueMap.size() > 0) {                
                for (Map.Entry<Destination, Integer> queueEntry : replyToQueueMap.entrySet()) {
                    BytesMessage outMessage = session.createBytesMessage();
                    outMessage.writeBytes(message);
                    producer.send(queueEntry.getKey(), outMessage);
                }
            }
            return message;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public synchronized void ackMessages () throws Exception {
        final String eLabel = "ActiveMQMergingDefaultConsumer.processMessage: ";
        try {
            for (Message message : messages.getMessages()) message.jmsMessage.acknowledge();
            messages.reset();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private synchronized boolean accumulationExpired () {
        return ((messages.getMessages().size() > 0 && ((messages.getMessages().get(0).timeStamp + accumulationWaitMills) < System.currentTimeMillis())) ||
                messages.getMessages().size() >= minMessages);
    }

    private Timer timer;
    private long timerDelayMills = 1000;
    private long timerPeriodMills = 10000;
    private long accumulationWaitMills = 10000;
    private int minMessages = 10;
    private ConnectionResource connectionResource;
    private Session session;
    private MessageConsumer consumer;
    private MessageProducer producer;
    private MessageListAggregate messages = new MessageListAggregate();
    private int replyToMessageDeliveryMode = -1;

}

class MessageListAggregate {

    public void add (Message message) throws Exception {
        final String eLabel = "ActiveMQMergingDefaultConsumer.MessageList.add: ";
        try {
            if (message.body.length < 1) throw new Exception("Message body is empty");
            if (messageStartsWith == null) messageStartsWith = (char)message.body[0];
            if (messageStartsWith == (char)message.body[0]) {
                messages.add(message);
            } else {
                alternateFormatMessages.add(message);
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public void reset () {
        messages.clear();
        messageStartsWith = null;
    }

    public List<Message> getMessages () {
        return messages;
    }

    public List<Message> getAlternateFormatMessages () {
        return alternateFormatMessages;
    }

    List<Message> messages = Collections.synchronizedList(new ArrayList<Message>());
    List<Message> alternateFormatMessages = Collections.synchronizedList(new ArrayList<Message>());
    Character messageStartsWith = null;

}

class Message {

    public Message (byte[] body, javax.jms.Message jmxMessage) {
        this.body = body;
        this.jmsMessage = jmxMessage;
        this.timeStamp = System.currentTimeMillis();
    }

    public final javax.jms.Message jmsMessage;
    public final byte[] body;
    public final long timeStamp;

}
