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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.cyclades.engine.nyxlet.templates.xstroma.ServiceBrokerNyxletImpl;
import org.cyclades.engine.nyxlet.templates.xstroma.message.api.MessageConsumer;
import org.cyclades.engine.nyxlet.templates.xstroma.message.api.MessageProcessor;
import org.cyclades.engine.nyxlet.templates.xstroma.message.impl.ResponseProcessor;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * ActiveMQ MessageConsumer implementation. A MessageConsumer is generally tasked with reading
 * X-STROMA messages from a message repository implementation, processing each X-STROMA message, and placing the
 * results in a specified message repository implementation for message provider consumption. A typical use case scenario
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
public class Consumer implements MessageConsumer {

    @Override
    public void init (Map<String, String> initializationMap, MessageProcessor messageProcessor, ResponseProcessor responseProcessor, ServiceBrokerNyxletImpl callBackServiceInstance) throws Exception {
        final String eLabel = "activemq.Consumer.init: ";
        try {
            if (!initializationMap.containsKey(TARGET_QUEUE_CONFIG_PARAMETER)) throw new Exception("Initialization parameter missing: " + TARGET_QUEUE_CONFIG_PARAMETER);
            if (!initializationMap.containsKey(CONNECTION_STRING_CONFIG_PARAMETER)) throw new Exception("Initialization parameter missing: " + CONNECTION_STRING_CONFIG_PARAMETER);
            String targetQueue = initializationMap.get(TARGET_QUEUE_CONFIG_PARAMETER);
            String connectionString = initializationMap.get(CONNECTION_STRING_CONFIG_PARAMETER);
            // Default settings...highly concurrent by default...using a connection per channel per DefaultConsumer. This may be
            // wasteful for some implementations. Please see the RabbitMQ documentation for more details on how to configure this.
            // There will most likely be an optimal setting for your company setup.
            int numConsumers = 1;
            String consumerTag = "defaultConsumerTag:";
            boolean cancelRecovery = false;
            if (initializationMap.containsKey(NUM_CONSUMERS_CONFIG_PARAMETER)) numConsumers = Integer.parseInt(initializationMap.get(NUM_CONSUMERS_CONFIG_PARAMETER));
            if (initializationMap.containsKey(CONSUMER_TAG_CONFIG_PARAMETER)) consumerTag = initializationMap.get(CONSUMER_TAG_CONFIG_PARAMETER);
            if (initializationMap.containsKey(CANCEL_RECOVERY_CONFIG_PARAMETER)) cancelRecovery = initializationMap.get(CANCEL_RECOVERY_CONFIG_PARAMETER).equalsIgnoreCase("true");
            int prefetchCount = -1;
            if (initializationMap.containsKey(PREFETCH_COUNT_CONFIG_PARAMETER)) prefetchCount = Integer.parseInt(initializationMap.get(PREFETCH_COUNT_CONFIG_PARAMETER));
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(connectionString);
            //if (initializationMap.containsKey(CONNECTION_HEARTBEAT_SECONDS_PARAMETER)) factory.setRequestedHeartbeat(Integer.parseInt(initializationMap.get(CONNECTION_HEARTBEAT_SECONDS_PARAMETER)));
            for (int i = 0; i < numConsumers; i++) {
                connectionResources.add(new ConnectionResource(factory, targetQueue, consumerTag + i, cancelRecovery, prefetchCount, callBackServiceInstance).init(initializationMap, messageProcessor, responseProcessor).connect());
            }
        } catch (Exception e) {
            e.printStackTrace();
            try { destroy(); } catch (Exception ex) { e.printStackTrace(); }
            throw new Exception(eLabel + e);
        }
    }

    public synchronized boolean isHealthy () throws Exception {
        try {
            for (ConnectionResource connectionResource : connectionResources) connectionResource.getConnection().getMetaData();
        } catch (Exception e) {
            for (ConnectionResource connectionResource : connectionResources) try { connectionResource.close(); } catch (Exception ex) { e.printStackTrace(); }
            for (ConnectionResource connectionResource : connectionResources) connectionResource.reconnect();
        }
        return true;
    }

    @Override
    public synchronized void destroy() throws Exception {
        for (ConnectionResource connectionResource : connectionResources) connectionResource.destroy();
    }

    @Override
    public void onMessage(Object... parameters) throws Exception {
        final String eLabel = "activemq.Consumer.onMessage: ";
        // NO-OP...not needed...using a local class to do the dirty work in here.
        throw new UnsupportedOperationException("Not used or needed in this implementation");
    }

    private List<ConnectionResource> connectionResources = new ArrayList<ConnectionResource>();
    public static final String TARGET_QUEUE_CONFIG_PARAMETER            = "target_queue";
    public static final String CONNECTION_STRING_CONFIG_PARAMETER       = "connection_string";
    public static final String NUM_CONSUMERS_CONFIG_PARAMETER           = "num_consumers";
    public static final String CONSUMER_TAG_CONFIG_PARAMETER            = "consumer_tag";
    public static final String CANCEL_RECOVERY_CONFIG_PARAMETER         = "cancel_recovery";
    public static final String PREFETCH_COUNT_CONFIG_PARAMETER          = "prefetch_count";
    public static final String CONNECTION_HEARTBEAT_SECONDS_PARAMETER   = "connection_heartbeat_seconds";

}
