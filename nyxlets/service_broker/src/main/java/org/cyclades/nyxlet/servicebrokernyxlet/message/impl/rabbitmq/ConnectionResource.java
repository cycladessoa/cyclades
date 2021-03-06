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
package org.cyclades.nyxlet.servicebrokernyxlet.message.impl.rabbitmq;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.cyclades.engine.nyxlet.templates.xstroma.ServiceBrokerNyxletImpl;
import org.cyclades.engine.nyxlet.templates.xstroma.message.api.MessageProcessor;
import org.cyclades.engine.nyxlet.templates.xstroma.message.impl.ResponseProcessor;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.cyclades.nyxlet.servicebrokernyxlet.message.impl.rabbitmq.consumer.ConsumerEnum;
import org.cyclades.nyxlet.servicebrokernyxlet.message.impl.rabbitmq.consumer.RabbitMQConsumer;

public class ConnectionResource {

    public ConnectionResource (ConnectionFactory factory, String queueName, String consumerTag, boolean cancelRecovery, int prefetchCount, ServiceBrokerNyxletImpl callBackServiceInstance) throws IOException {
        this.factory = factory;
        this.queueName = queueName;
        this.consumerTag = consumerTag;
        this.cancelRecovery = cancelRecovery;
        this.prefetchCount = prefetchCount;
        this.callBackServiceInstance = callBackServiceInstance;
    }

    public ConnectionResource init (Map<String, String> parameters, MessageProcessor messageProcessor, ResponseProcessor responseProcessor) throws Exception {
        this.parameters = parameters;
        this.messageProcessor = messageProcessor;
        this.responseProcessor = responseProcessor;
        if (parameters.containsKey(CONSUMER_TYPE)) consumerEnum = ConsumerEnum.valueOf(parameters.get(CONSUMER_TYPE).toUpperCase());
        return this;
    }

    public synchronized ConnectionResource connect () throws Exception {
        if (killed) return null;
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            Map<String, Object> attributes = new HashMap<String, Object>();
            if (parameters.containsKey(INACTIVITY_DELETE)) attributes.put("x-expires", Long.parseLong(parameters.get(INACTIVITY_DELETE)));
            if (parameters.containsKey(HA_POLICY)) attributes.put("x-ha-policy", parameters.get(HA_POLICY));
            boolean durable = parameters.containsKey(DURABLE) && parameters.get(DURABLE).equalsIgnoreCase("true");
            channel.queueDeclare(queueName, durable, false, false, attributes);
            if (prefetchCount > 0) channel.basicQos(prefetchCount);
            consumer = consumerEnum.build(this, parameters);
            return this;
        } catch (IOException e) {
            try { channel.basicCancel(consumerTag); } catch (Exception ex) { e.printStackTrace(); }
            try { connection.abort(); } catch (Exception ex) { e.printStackTrace(); }
            throw e;
        }
    }

    public synchronized void reconnect () throws Exception {
        close();
        connect();
    }

    public synchronized void close () {
        try { channel.basicCancel(consumerTag); } catch (Exception e) { e.printStackTrace(); }
        try { connection.abort(); } catch (Exception e) { e.printStackTrace(); }
        try { consumer.destroy(); } catch (Exception e) { e.printStackTrace(); }
    }

    public synchronized void destroy () {
        killed = true;
        close();
    }

    public String getQueueName() {
        return queueName;
    }
    public ConnectionFactory getFactory() {
        return factory;
    }
    public Connection getConnection() {
        return connection;
    }
    public Channel getChannel() {
        return channel;
    }
    public String getConsumerTag() {
        return consumerTag;
    }
    public boolean getCancelRecovery () {
        return cancelRecovery;
    }
    public ServiceBrokerNyxletImpl getCallBackServiceInstance () {
        return callBackServiceInstance;
    }

    public void fireResponseProcessor (byte[] response, byte[] request) throws Exception {
        if (responseProcessor != null) responseProcessor.process(response, request);
    }

    public boolean hasResponseProcessor () {
        return (responseProcessor != null);
    }

    public byte[] fireMessageProcessor (byte[] message) throws Exception {
        return (messageProcessor != null) ? messageProcessor.processAndGetResponse(message) : null;
    }

    public boolean hasMessageProcessor () {
        return (messageProcessor != null);
    }

    private ResponseProcessor responseProcessor = null;
    private MessageProcessor messageProcessor = null;
    RabbitMQConsumer consumer;
    ConsumerEnum consumerEnum = ConsumerEnum.DEFAULT;
    Map<String, String> parameters = new HashMap<String, String>();
    private String queueName;
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    private String consumerTag;
    private boolean cancelRecovery;
    private int prefetchCount = -1;
    private ServiceBrokerNyxletImpl callBackServiceInstance;
    private volatile boolean killed = false;
    public static final String CONSUMER_TYPE        = "consumer_type";
    public static final String INACTIVITY_DELETE    = "inactivity_delete";
    public static final String HA_POLICY            = "ha_policy";
    public static final String DURABLE              = "durable";

}
