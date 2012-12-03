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

import java.util.List;
import java.util.Map;
import org.cyclades.engine.nyxlet.templates.xstroma.message.api.RawMessageProducer;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.GetResponse;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.cyclades.pool.GenericObjectPoolConfigBuilder;

/**
 * RabbitMQ MessageProducer implementation. This MessageProducer is actually a mechanism
 * for fetching, or extraction of binary messages for a message initiator (client).
 */
public class RawExtractor implements RawMessageProducer {

    @Override
    public void init(Map<String, String> initializationMap) throws Exception {
        if (!initializationMap.containsKey(CONNECTION_STRING_CONFIG_PARAMETER)) throw new Exception("Initialization parameter missing: " + CONNECTION_STRING_CONFIG_PARAMETER);
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
            if (!attributeMap.containsKey(QUEUE_PARAMETER)) throw new Exception("Missing parameter: " + QUEUE_PARAMETER);
            String queue = attributeMap.get(QUEUE_PARAMETER).get(0);
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
                channel.queueDeclarePassive(queue);
            } catch (Exception e) {
                if (pooled && connObj != null) {
                    connectionPool.invalidateObject(connObj);
                    connObj = null; // Do not return again in finally statement
                }
                e.printStackTrace();
                throw e;
            }
            boolean autoAck = false;
            GetResponse response = channel.basicGet(queue, autoAck);
            byte[] responseBytes = null;
            if (response != null) {
                AMQP.BasicProperties props = response.getProps();
                long deliveryTag = response.getEnvelope().getDeliveryTag();
                responseBytes = response.getBody();
                channel.basicAck(deliveryTag, false);
            }
            // Make sure to return null if there is no message to return.
            return responseBytes;
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

    private ConnectionFactory factory;
    private ObjectPool<ConnectionObject> connectionPool = null;
    private static final String QUEUE_PARAMETER                         = "queue";
    public static final String CONNECTION_STRING_CONFIG_PARAMETER       = "connection_string";
    public static final String POOL_CONFIG_PARAMETER                    = "pool";
    public static final String CONNECTION_HEARTBEAT_SECONDS_PARAMETER   = "connection_heartbeat_seconds";

}
