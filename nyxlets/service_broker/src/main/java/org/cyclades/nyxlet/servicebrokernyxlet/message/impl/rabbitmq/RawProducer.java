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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cyclades.engine.nyxlet.templates.xstroma.message.api.RawMessageProducer;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.AMQP;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.cyclades.pool.GenericObjectPoolConfigBuilder;

/**
 * RabbitMQ MessageProducer implementation. This MessageProducer is a mechanism
 * for submission of binary messages by a message initiator (client).
 */
public class RawProducer implements RawMessageProducer {

    @Override
    public void init(Map<String, String> initializationMap) throws Exception {
        final String eLabel = "rabbitmq.Producer.init: ";
        if (!initializationMap.containsKey(TARGET_QUEUE_CONFIG_PARAMETER)) throw new Exception("Initialization parameter missing: " + TARGET_QUEUE_CONFIG_PARAMETER);
        if (!initializationMap.containsKey(CONNECTION_STRING_CONFIG_PARAMETER)) throw new Exception("Initialization parameter missing: " + CONNECTION_STRING_CONFIG_PARAMETER);
        if (initializationMap.containsKey(REPLYTO_INACTIVITY_DELETE_CONFIG_PARAMETER)) replyToInactivityTimeout = Long.parseLong(initializationMap.get(REPLYTO_INACTIVITY_DELETE_CONFIG_PARAMETER));
        targetQueue = initializationMap.get(TARGET_QUEUE_CONFIG_PARAMETER);
        factory = new ConnectionFactory();
        if (initializationMap.containsKey(CONNECTION_HEARTBEAT_SECONDS_PARAMETER)) factory.setRequestedHeartbeat(Integer.parseInt(initializationMap.get(CONNECTION_HEARTBEAT_SECONDS_PARAMETER)));
        factory.setUri(initializationMap.get(CONNECTION_STRING_CONFIG_PARAMETER));
        boolean usePool = false;
        if (initializationMap.containsKey(POOL_CONFIG_PARAMETER)) usePool = Boolean.parseBoolean(initializationMap.get(POOL_CONFIG_PARAMETER));
        if (usePool) {
            connectionPool = new GenericObjectPool<ConnectionObject>(new ConnectionPoolableObjectFactory(factory), new GenericObjectPoolConfigBuilder().build(initializationMap));
        }
    }

    @Override
    public void destroy() throws Exception {
        try { if (connectionPool != null) connectionPool.close(); } catch (Exception e) {}
    }

    @Override
    public String sendMessage(String message, Map<String, List<String>> attributeMap) throws Exception {
        byte[] messageBytes = sendMessage(message.getBytes(), attributeMap);
        // Hard code the UTF-8 for now, we can always pass it in as an attribute as well
        return (messageBytes != null) ? new String(messageBytes,"UTF-8") : null;
    }

    @Override
    public byte[] sendMessage(byte[] message, Map<String, List<String>> attributeMap) throws Exception {
        boolean pooled = false;
        ConnectionObject connObj = null;
        try {
            Channel channel = null;
            String replyToQueue = "";
            if (connectionPool != null) {
                connObj = connectionPool.borrowObject();
                channel = connObj.getChannel();
                pooled = true;
            } else {
                Connection connection = factory.newConnection();
                channel = connection.createChannel();
                connObj = new ConnectionObject(connection, channel);
            }
            try {
                channel.queueDeclarePassive(targetQueue);
            } catch (Exception e) {
                if (pooled && connObj != null) {
                    connectionPool.invalidateObject(connObj);
                    connObj = null; // Do not return again in finally statement
                }
                e.printStackTrace();
                throw e;
            }
            AMQP.BasicProperties.Builder propsBuilder = new AMQP.BasicProperties.Builder();
            if (attributeMap.containsKey(REPLY_TO_PARAMETER)) {
                replyToQueue = attributeMap.get(REPLY_TO_PARAMETER).get(0);
                propsBuilder.replyTo(replyToQueue);
                Map<String, Object> attributes = new HashMap<String, Object>();
                if (replyToInactivityTimeout > -1) attributes.put("x-expires", replyToInactivityTimeout);
                channel.queueDeclare(replyToQueue, false, false, false, attributes);
            } /*else {
                Should we make this synchronous? Probably not, abuse is imminent! Use HTTP adapter instead for now
                unless you plan on making this Message Queue an Enterprise Integration pattern...good luck.
            }*/
            channel.basicPublish("", targetQueue, propsBuilder.build(), message);
            // Make sure to return null, there is no message to return.
            return null;
        } finally {
            if (pooled) {
                try { if (connObj != null) connectionPool.returnObject(connObj); } catch (Exception e) { e.printStackTrace(); }
            } else {
                try { if (connObj != null) connObj.destroy(); } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }

    @Override
    public boolean isHealthy () throws Exception {
        return true;
    }

    ConnectionFactory factory;
    private String targetQueue;
    private long replyToInactivityTimeout = -1;
    private ObjectPool<ConnectionObject> connectionPool = null;
    public static final String REPLY_TO_PARAMETER                           = "replyto";
    public static final String TARGET_QUEUE_CONFIG_PARAMETER                = "target_queue";
    public static final String CONNECTION_STRING_CONFIG_PARAMETER           = "connection_string";
    public static final String REPLYTO_INACTIVITY_DELETE_CONFIG_PARAMETER   = "replyto_inactivity_delete";
    public static final String POOL_CONFIG_PARAMETER                        = "pool";
    public static final String CONNECTION_HEARTBEAT_SECONDS_PARAMETER       = "connection_heartbeat_seconds";

}
