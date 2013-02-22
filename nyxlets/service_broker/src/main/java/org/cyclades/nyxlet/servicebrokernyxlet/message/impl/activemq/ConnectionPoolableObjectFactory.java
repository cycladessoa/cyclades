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

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.Connection;
import javax.jms.Session;
import javax.jms.MessageProducer;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQPrefetchPolicy;

public class ConnectionPoolableObjectFactory extends BasePoolableObjectFactory <ConnectionObject> {

    public ConnectionPoolableObjectFactory (ActiveMQConnectionFactory factory, int prefetchCount, 
            int messageDeliveryMode, boolean createProducer, int sessionAckMode) throws Exception {
        this.factory = factory;
        this.prefetchCount = prefetchCount;
        this.messageDeliveryMode = messageDeliveryMode;
        this.createProducer = createProducer;
        this.sessionAckMode = sessionAckMode;
    }

    @Override
    public ConnectionObject makeObject() throws Exception {
        return makeObject(factory, prefetchCount, messageDeliveryMode, createProducer, sessionAckMode);
    }
    
    public static ConnectionObject makeObject (ActiveMQConnectionFactory factory, int prefetchCount, 
            int messageDeliveryMode, boolean createProducer, int sessionAckMode) throws Exception {
        Connection connection = factory.createConnection();
        ActiveMQPrefetchPolicy policy = new ActiveMQPrefetchPolicy();
        policy.setAll(prefetchCount);
        ((ActiveMQConnection)connection).setPrefetchPolicy(policy);
        connection.start();
        Session session = connection.createSession(false, sessionAckMode);
        MessageProducer producer = null;
        if (createProducer) {
            producer = session.createProducer(null);
            if (messageDeliveryMode > -1) producer.setDeliveryMode(messageDeliveryMode);
        }
        return new ConnectionObject(connection, session, producer);
    }

    @Override
    public boolean validateObject(ConnectionObject connObj) {
        try {
            connObj.getConnection().getMetaData();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void destroyObject(ConnectionObject connObj) throws Exception {
        try { connObj.destroy(); } catch (Exception e) { e.printStackTrace(); };
    }

    private final ActiveMQConnectionFactory factory;
    private final int prefetchCount;
    private final int messageDeliveryMode;
    private final boolean createProducer;
    private final int sessionAckMode;

}

class ConnectionObject {

    public ConnectionObject (Connection connection, Session session, MessageProducer producer) {
        this.connection = connection;
        this.session = session;
        this.producer = producer;
    }

    public void destroy () throws Exception {
        try { session.close(); } catch (Exception e) {}
        try { producer.close(); } catch (Exception e) {}
        try { connection.close(); } catch (Exception e) { e.printStackTrace(); };
    }

    public Connection getConnection () {
        return connection;
    }
    
    public Session getSession () {
        return session;
    }
    
    public MessageProducer getMessageProducer () {
        return producer;
    }
    
    private final Connection connection;
    private final Session session;
    private final MessageProducer producer;

}