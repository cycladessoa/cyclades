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
package org.cyclades.nyxlet.servicebrokernyxlet.message.impl.activemq;

import java.util.List;
import java.util.Map;
import org.cyclades.engine.nyxlet.templates.xstroma.message.api.MessageProducer;
import javax.jms.Session;
import javax.jms.BytesMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.cyclades.pool.GenericObjectPoolConfigBuilder;

/**
 * ActiveMQ MessageProducer implementation. This MessageProducer is actually a mechanism
 * for submission of X-STROMA request messages by a message initiator (client). A typical use case scenario
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
public class Producer implements MessageProducer {

    @Override
    public void init(Map<String, String> initializationMap) throws Exception {
        if (!initializationMap.containsKey(TARGET_QUEUE_CONFIG_PARAMETER)) throw new Exception("Initialization parameter missing: " + TARGET_QUEUE_CONFIG_PARAMETER);
        if (!initializationMap.containsKey(CONNECTION_STRING_CONFIG_PARAMETER)) throw new Exception("Initialization parameter missing: " + CONNECTION_STRING_CONFIG_PARAMETER);
        if (initializationMap.containsKey(REPLYTO_INACTIVITY_DELETE_CONFIG_PARAMETER)) replyToInactivityTimeout = Long.parseLong(initializationMap.get(REPLYTO_INACTIVITY_DELETE_CONFIG_PARAMETER));
        if (initializationMap.containsKey(REPLYTO_HA_POLICY)) replyToHAPolicy = initializationMap.get(REPLYTO_HA_POLICY);
        if (initializationMap.containsKey(REPLYTO_DURABLE)) replyToDurableQueue = initializationMap.get(REPLYTO_DURABLE).equalsIgnoreCase("true");
        if (initializationMap.containsKey(MESSAGE_DELIVERY_MODE)) messageDeliveryMode = Integer.parseInt(initializationMap.get(MESSAGE_DELIVERY_MODE));
        if (initializationMap.containsKey(PREFETCH_COUNT_CONFIG_PARAMETER)) prefetchCount = Integer.parseInt(initializationMap.get(PREFETCH_COUNT_CONFIG_PARAMETER));
        targetQueue = initializationMap.get(TARGET_QUEUE_CONFIG_PARAMETER);
        factory = new ActiveMQConnectionFactory(initializationMap.get(CONNECTION_STRING_CONFIG_PARAMETER));
        //if (initializationMap.containsKey(CONNECTION_HEARTBEAT_SECONDS_PARAMETER)) factory.setRequestedHeartbeat(Integer.parseInt(initializationMap.get(CONNECTION_HEARTBEAT_SECONDS_PARAMETER)));
        boolean usePool = false;
        if (initializationMap.containsKey(POOL_CONFIG_PARAMETER)) usePool = Boolean.parseBoolean(initializationMap.get(POOL_CONFIG_PARAMETER));
        if (usePool) {
            connectionPool = new GenericObjectPool<ConnectionObject>(
                    new ConnectionPoolableObjectFactory(factory, (prefetchCount > -1) ? prefetchCount : 1, messageDeliveryMode, 
                            true, Session.AUTO_ACKNOWLEDGE), 
                    new GenericObjectPoolConfigBuilder().build(initializationMap));
        }
    }

    @Override
    public void destroy() throws Exception {
        try { if (connectionPool != null) connectionPool.close(); } catch (Exception e) {}
    }

    @Override
    public String sendMessage(String message, Map<String, List<String>> attributeMap) throws Exception {
       sendMessage(message.getBytes(), attributeMap);
       return null;
    }
    
    public byte[] sendMessage(byte[] message, Map<String, List<String>> attributeMap) throws Exception {
        boolean pooled = false;
        ConnectionObject connObj = null;
        try {
            String replyToQueue = "";
            if (connectionPool != null) {
                connObj = connectionPool.borrowObject();
                pooled = true;
            } else {
                connObj = ConnectionPoolableObjectFactory.makeObject(factory, (prefetchCount > -1) ? prefetchCount : 1, 
                        messageDeliveryMode, true, Session.AUTO_ACKNOWLEDGE);
            }
            try {
                connObj.getConnection().getMetaData();
            } catch (Exception e) {
                if (pooled && connObj != null) {
                    connectionPool.invalidateObject(connObj);
                    connObj = null; // Do not return again in finally statement
                }
                e.printStackTrace();
                throw e;
            }
            BytesMessage outboundMessage = connObj.getSession().createBytesMessage();
            outboundMessage.writeBytes(message);
            if (attributeMap.containsKey(REPLY_TO_PARAMETER)) {
                replyToQueue = attributeMap.get(REPLY_TO_PARAMETER).get(0);
                outboundMessage.setJMSReplyTo(connObj.getSession().createQueue(replyToQueue));
            }
            connObj.getMessageProducer().send(connObj.getSession().createQueue(targetQueue), outboundMessage);
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

    ActiveMQConnectionFactory factory;
    private String targetQueue;
    private long replyToInactivityTimeout = -1;
    private String replyToHAPolicy = null;
    private boolean replyToDurableQueue = false;
    private int messageDeliveryMode = -1;
    private ObjectPool<ConnectionObject> connectionPool = null;
    private int prefetchCount = -1;
    public static final String REPLY_TO_PARAMETER                           = "replyto";
    public static final String TARGET_QUEUE_CONFIG_PARAMETER                = "target_queue";
    public static final String CONNECTION_STRING_CONFIG_PARAMETER           = "connection_string";
    public static final String POOL_CONFIG_PARAMETER                        = "pool";
    public static final String MESSAGE_DELIVERY_MODE                        = "message_delivery_mode";
    public static final String CONNECTION_HEARTBEAT_SECONDS_PARAMETER       = "connection_heartbeat_seconds";
    public static final String PREFETCH_COUNT_CONFIG_PARAMETER              = "prefetch_count";
    // reply to queue settings
    public static final String REPLYTO_INACTIVITY_DELETE_CONFIG_PARAMETER   = "replyto_inactivity_delete";
    public static final String REPLYTO_HA_POLICY                            = "replyto_ha_policy";
    public static final String REPLYTO_DURABLE                              = "replyto_durable";

}
