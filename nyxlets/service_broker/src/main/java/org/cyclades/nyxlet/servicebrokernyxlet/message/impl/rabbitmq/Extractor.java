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
import org.cyclades.engine.nyxlet.templates.xstroma.message.api.MessageProducer;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.GetResponse;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.cyclades.pool.GenericObjectPoolConfigBuilder;

/**
 * RabbitMQ MessageProducer implementation. This MessageProducer is actually a mechanism
 * for fetching, or extraction of X-STROMA response messages for a message initiator (client). A typical use case scenario
 * would be as follows:
 *
 * 1.) A message initiator (client) submits a message via a Producer (implementation of a MessageProducer interface)
 * 2.) The message is processed by the Consumer (implementation of a MessageConsumer)
 *      a.) The message is retrieved from the message repository implementation
 *      b.) The message is processed by the Cyclades Engine
 *      c.) The response is placed on a specified location on the message repository implementation
 * 3.) A message initiator (client) fetches messages via an Extractor (implementation of a MessageProducer interface).
 *
 * Since this is an asynchronous process, step 1 should return immediately. The message initiator (client) may have to poll
 * for the response by repeating step 3 for a period of time.
 */
public class Extractor implements MessageProducer {

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
    public synchronized void destroy() throws Exception {
        try { if (connectionPool != null) connectionPool.close(); } catch (Exception e) {}
    }

    @Override
    public String sendMessage(String message, Map<String, List<String>> attributeMap) throws Exception {
        byte[] messageBytes = sendMessage(message.getBytes(), attributeMap);
        // Hard code the UTF-8 for now, we can always pass it in as an attribute as well
        return (messageBytes != null) ? new String(messageBytes,"UTF-8") : null;
    }
    
    public byte[] sendMessage(byte[] message, Map<String, List<String>> attributeMap) throws Exception {
        boolean pooled = false;
        ConnectionObject connObj = null;
        try {
            if (!attributeMap.containsKey(QUEUE_PARAMETER)) throw new Exception("Missing parameter: " + QUEUE_PARAMETER);
            String queue = attributeMap.get(QUEUE_PARAMETER).get(0);
            pooled = (connectionPool != null);
            connObj = ConnectionObject.getConnectionObject(connectionPool, factory, pooled);
            try {
                connObj.getChannel().queueDeclarePassive(queue);
            } catch (Exception e) {
                if (pooled && connObj != null) {
                    connectionPool.invalidateObject(connObj);
                    connObj = null; // Do not return again in finally statement
                }
                e.printStackTrace();
                throw e;
            }
            boolean autoAck = false;
            GetResponse response = connObj.getChannel().basicGet(queue, autoAck);
            byte[] responseBytes = null;
            if (response != null) {
                AMQP.BasicProperties props = response.getProps();
                long deliveryTag = response.getEnvelope().getDeliveryTag();
                responseBytes = response.getBody();
                connObj.getChannel().basicAck(deliveryTag, false);
            }
            // Make sure to return null if there is no message to return.
            return responseBytes;
        } finally {
            try { 
                ConnectionObject.releaseConnectionObject(connectionPool, connObj, pooled);
            } catch (Exception e) { 
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized boolean isHealthy () throws Exception {
        boolean pooled = false;
        ConnectionObject connObj = null;
        try {
            pooled = (connectionPool != null);
            connObj = ConnectionObject.getConnectionObject(connectionPool, factory, pooled);
            try {
                if (!connObj.getChannel().isOpen()) throw new Exception("Channel is closed!!!");
            } catch (Exception e) {
                if (pooled && connObj != null) {
                    connectionPool.invalidateObject(connObj);
                    connObj = null; // Do not return again in finally statement
                }
                e.printStackTrace();
                throw e;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { 
                ConnectionObject.releaseConnectionObject(connectionPool, connObj, pooled);
            } catch (Exception e) { 
                e.printStackTrace();
            }
        }
    }

    private ConnectionFactory factory;
    private ObjectPool<ConnectionObject> connectionPool = null;
    private static final String QUEUE_PARAMETER                         = "queue";
    public static final String CONNECTION_STRING_CONFIG_PARAMETER       = "connection_string";
    public static final String POOL_CONFIG_PARAMETER                    = "pool";
    public static final String CONNECTION_HEARTBEAT_SECONDS_PARAMETER   = "connection_heartbeat_seconds";

}
