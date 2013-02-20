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
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQPrefetchPolicy;

public class ConnectionPoolableObjectFactory extends BasePoolableObjectFactory <ConnectionObject> {

    public ConnectionPoolableObjectFactory (ActiveMQConnectionFactory factory, int prefetchCount) throws Exception {
       this(factory, null, prefetchCount);
    }

    public ConnectionPoolableObjectFactory (ActiveMQConnectionFactory factory, String queueName, int prefetchCount) throws Exception {
        this.factory = factory;
        this.queueName = queueName;
        this.prefetchCount = prefetchCount;
    }

    @Override
    public ConnectionObject makeObject() throws Exception {
        final String eLabel = "ConnectionPoolObjectFactory.makeObject: ";
        Connection connection = factory.createConnection();
        ActiveMQPrefetchPolicy policy = new ActiveMQPrefetchPolicy();
        policy.setAll(prefetchCount);
        ((ActiveMQConnection)connection).setPrefetchPolicy(policy);
        connection.start();
        return new ConnectionObject(connection);

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
    private final String queueName;
    private int prefetchCount;

}

class ConnectionObject {

    public ConnectionObject (Connection connection) {
        this.connection = connection;
    }

    public void destroy () throws Exception {
        try { connection.close(); } catch (Exception e) { e.printStackTrace(); };
    }

    public Connection getConnection() {
        return connection;
    }

    private final Connection connection;

}