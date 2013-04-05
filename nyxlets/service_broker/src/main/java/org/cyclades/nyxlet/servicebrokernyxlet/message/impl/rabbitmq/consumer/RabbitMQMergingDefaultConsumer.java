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
package org.cyclades.nyxlet.servicebrokernyxlet.message.impl.rabbitmq.consumer;

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
import org.cyclades.nyxlet.servicebrokernyxlet.message.impl.rabbitmq.ConnectionResource;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;

public class RabbitMQMergingDefaultConsumer extends TimerTask implements RabbitMQConsumer {

    public RabbitMQMergingDefaultConsumer (ConnectionResource connectionResource) throws IOException {
        this.connectionResource = connectionResource;
    }

    public RabbitMQMergingDefaultConsumer init (Map<String, String> parameters) throws Exception {
        final String eLabel = "RabbitMQMergingDefaultConsumer.init: ";
        try {
            if (parameters.containsKey("timer_delay_mills")) timerDelayMills = Long.parseLong(parameters.get("timer_delay_mills"));
            if (parameters.containsKey("timer_period_mills")) timerPeriodMills = Long.parseLong(parameters.get("timer_period_mills"));
            if (parameters.containsKey("accumulation_wait_mills")) accumulationWaitMills = Long.parseLong(parameters.get("accumulation_wait_mills"));
            if (parameters.containsKey("min_messages")) minMessages = Integer.parseInt(parameters.get("min_messages"));
            if (parameters.containsKey(REPLYTO_MESSAGE_DELIVERY_MODE)) replyToMessageDeliveryMode = Integer.parseInt(parameters.get(REPLYTO_MESSAGE_DELIVERY_MODE));
            if (parameters.containsKey(MERGE_ON_REPLYTO) && parameters.get(MERGE_ON_REPLYTO) != null) mergeOnReplyTo = parameters.get(MERGE_ON_REPLYTO).equalsIgnoreCase("true");
            timer = new Timer();
            timer.schedule(this, timerDelayMills, timerPeriodMills);
            return this;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public void destroy () throws Exception {
        final String eLabel = "RabbitMQMergingDefaultConsumer.destroy: ";
        try {
            timer.cancel();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    @Override
    public void run() {
        final String eLabel = "RabbitMQMergingDefaultConsumer.run: ";
        try {
            accumulateMessages();
        } catch (Exception e) {
            connectionResource.getCallBackServiceInstance().logError(eLabel + e);
        }
    }

    private synchronized void accumulateMessages () throws Exception {
        final String eLabel = "RabbitMQMergingDefaultConsumer.accumulateMessage: ";
        try {
            GetResponse message = connectionResource.getChannel().basicGet(connectionResource.getQueueName(), false);
            while (message != null) {
                messages.add(new Message(message.getEnvelope(), message.getProps(), message.getBody()));
                if (accumulationExpired()) break;
                message = connectionResource.getChannel().basicGet(connectionResource.getQueueName(), false);
            }
            releaseAlternateFormatMessages();
            if (accumulationExpired()) {
                processMessages();
                ackMessages();
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public synchronized void releaseAlternateFormatMessages () throws Exception {
        final String eLabel = "RabbitMQMergingDefaultConsumer.releaseAlternateFormatMessages: ";
        try {
            for (Message message : messages.getAlternateFormatMessages()) {
                connectionResource.getChannel().basicReject(message.envelope.getDeliveryTag(), true);
            }
            messages.getAlternateFormatMessages().clear();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public synchronized void processMessages () throws Exception {
        final String eLabel = "RabbitMQMergingDefaultConsumer.processMessage: ";
        try {
            
            if (mergeOnReplyTo) {
                for (Map.Entry<String, List<Message>> entry : messages.getMessagesByReplyTo().entrySet()) {
                    processMessageBatch(entry.getValue());
                }
            } else {
                processMessageBatch(messages.getMessages());
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }
    
    private synchronized void processMessageBatch (List<Message> messageObjectList) throws Exception {
        String replyTo;
        Map<String, Integer> replyToQueueMap = new LinkedHashMap<String, Integer>();
        List<String> messageList = new ArrayList<String>();
        for (Message message : messageObjectList) {
            messageList.add(new String(message.body));
            replyTo = message.properties.getReplyTo();
            if (replyTo != null && !replyTo.isEmpty()) replyToQueueMap.put(replyTo, 1);
        }
        //connectionResource.getCallBackServiceInstance().logError(connectionResource.getConsumerTag() + " Merging messages: " + messageList.size());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        connectionResource.getCallBackServiceInstance().processXSTROMAMessagePayloads(baos, messageList,
                (messages.messageStartsWith == '{') ? MetaTypeEnum.JSON : MetaTypeEnum.XML);
        byte[] message = baos.toByteArray();
        if (replyToQueueMap.size() > 0) {
            AMQP.BasicProperties.Builder propsBuilder = new AMQP.BasicProperties.Builder();
            if (replyToMessageDeliveryMode > -1) propsBuilder.deliveryMode(replyToMessageDeliveryMode);
            AMQP.BasicProperties messageProps = propsBuilder.build();
            for (Map.Entry<String, Integer> queueEntry : replyToQueueMap.entrySet()) {
                connectionResource.getChannel().basicPublish("", queueEntry.getKey(), messageProps, message);
            }
        }
        // XXX - Passing in "new byte[] {}" here as the value of the original request. Since the original request
        // is actually an aggregation of multiple X-STROMA requests, we'll need to build in some sort of
        // List structure to accommodate this later if needed
        if (connectionResource.hasResponseProcessor()) connectionResource.fireResponseProcessor(message, new byte[] {});
    }

    public synchronized void ackMessages () throws Exception {
        final String eLabel = "RabbitMQMergingDefaultConsumer.processMessage: ";
        try {
            for (Message message : messages.getMessages()) {
                connectionResource.getChannel().basicAck(message.envelope.getDeliveryTag(), false);
            }
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
    ConnectionResource connectionResource;
    MessageListAggregate messages = new MessageListAggregate();
    private int replyToMessageDeliveryMode = -1;
    private boolean mergeOnReplyTo = true;
    private final static String MERGE_ON_REPLYTO = "merge_on_replyto";

}

class MessageListAggregate {

    public void add (Message message) throws Exception {
        final String eLabel = "RabbitMQMergingDefaultConsumer.MessageList.add: ";
        try {
            if (message.body.length < 1) throw new Exception("Message body is empty");
            if (messageStartsWith == null) messageStartsWith = (char)message.body[0];
            if (messageStartsWith == (char)message.body[0]) {
                addValidMessages(message);
            } else {
                alternateFormatMessages.add(message);
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }
    
    private void addValidMessages (Message message) {
        // Add to total aggregate List
        messages.add(message);
        // Also index by reply to value
        List<Message> messageList = messagesByReplyTo.get(message.properties.getReplyTo());
        if (messageList == null) {
            messageList = Collections.synchronizedList(new ArrayList<Message>());
            messagesByReplyTo.put(message.properties.getReplyTo(), messageList);
        }
        messageList.add(message);
    }

    public void reset () {
        messages.clear();
        messageStartsWith = null;
        messagesByReplyTo.clear();
    }

    public List<Message> getMessages () {
        return messages;
    }
    
    public Map<String, List<Message>> getMessagesByReplyTo () {
        return messagesByReplyTo;
    }

    public List<Message> getAlternateFormatMessages () {
        return alternateFormatMessages;
    }

    Map<String, List<Message>> messagesByReplyTo = new LinkedHashMap<String, List<Message>>();
    List<Message> messages = Collections.synchronizedList(new ArrayList<Message>());
    List<Message> alternateFormatMessages = Collections.synchronizedList(new ArrayList<Message>());
    Character messageStartsWith = null;

}

class Message {

    public Message (Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        this.envelope = envelope;
        this.properties = properties;
        this.body = body;
        this.timeStamp = System.currentTimeMillis();
    }

    public final Envelope envelope;
    public final AMQP.BasicProperties properties;
    public final byte[] body;
    public final long timeStamp;

}
