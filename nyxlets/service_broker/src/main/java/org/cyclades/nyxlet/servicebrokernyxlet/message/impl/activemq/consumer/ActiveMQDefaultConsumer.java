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
import java.util.Map;
import org.cyclades.nyxlet.servicebrokernyxlet.message.impl.activemq.ConnectionResource;
import org.cyclades.nyxlet.servicebrokernyxlet.message.impl.activemq.MessageUtils;
import javax.jms.MessageProducer;
import javax.jms.MessageConsumer;
import javax.jms.Connection;
import javax.jms.Session;
import javax.jms.Message;
import javax.jms.BytesMessage;
import javax.jms.TextMessage;
import javax.jms.Destination;

public class ActiveMQDefaultConsumer implements ActiveMQConsumer, Runnable {

    public ActiveMQDefaultConsumer (ConnectionResource connectionResource) throws IOException {
        this.connectionResource = connectionResource;
    }

    public ActiveMQDefaultConsumer init (Map<String, String> parameters) throws Exception {
        if (parameters.containsKey(REPLYTO_MESSAGE_DELIVERY_MODE)) replyToMessageDeliveryMode = Integer.parseInt(parameters.get(REPLYTO_MESSAGE_DELIVERY_MODE));
        return this;
    }

    @Override
    public void destroy () throws Exception {
        alive = false;
        consumer.close();
    }

    @Override
    public void run () {
        final String eLabel = "ActiveMQDefaultConsumer.run: ";
        Connection connection = connectionResource.getConnection();
        Session session = null;
        try {
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            consumer = session.createConsumer(session.createQueue(connectionResource.getQueueName()));
            Message message = null;
            while (alive) {
                try {
                    byte[] body;
                    message = consumer.receive();
                    if (message == null) continue;
                    if (message instanceof BytesMessage) {
                        body = MessageUtils.readBytes((BytesMessage)message);
                    } else if (message instanceof TextMessage) {
                        body = ((TextMessage)message).getText().getBytes();
                    } else {
                        throw new UnsupportedOperationException("Message type not supported: " + message.getClass().getName());
                    }
                    byte[] response;
                    if (connectionResource.hasMessageProcessor()) {
                        response = connectionResource.fireMessageProcessor(body);
                    } else {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        connectionResource.getCallBackServiceInstance().processXSTROMAMessagePayload(baos, 
                                new String(body, "UTF-8"));
                        response = baos.toByteArray();
                    }
                    Destination replyToQueue = message.getJMSReplyTo();
                    if (replyToQueue != null) {
                        MessageProducer producer = null;
                        try {
                            producer = session.createProducer(replyToQueue);
                            if (replyToMessageDeliveryMode > -1) producer.setDeliveryMode(replyToMessageDeliveryMode);
                            BytesMessage outMessage = session.createBytesMessage();
                            outMessage.writeBytes(response);
                            producer.send(outMessage);
                        } finally {
                            try { producer.close(); } catch (Exception e) {}
                        }
                    } else {
                        // XXX - Verify this is what we want to do if there is no replyto set...DONT'T REPLY!
                        //System.out.println(baos.toString());
                    }
                    if (connectionResource.hasResponseProcessor()) connectionResource.fireResponseProcessor(response, body);
                } catch (Exception e) {
                    connectionResource.getCallBackServiceInstance().logStackTrace(e);
                } finally {
                    try { message.acknowledge(); } catch (Exception e) {}
                }
            }
        } catch (Exception e) {
            connectionResource.getCallBackServiceInstance().logStackTrace(e);
        } finally {
            try { session.close(); } catch (Exception e) {}
            try { consumer.close(); } catch (Exception e) {}
        }
    }

    ConnectionResource connectionResource;
    private int replyToMessageDeliveryMode = -1;
    private MessageConsumer consumer;
    private volatile boolean alive = true;

}
