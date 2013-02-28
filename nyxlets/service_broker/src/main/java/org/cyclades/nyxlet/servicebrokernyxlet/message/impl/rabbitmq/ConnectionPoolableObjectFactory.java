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

import org.apache.commons.pool.BasePoolableObjectFactory;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import org.apache.commons.pool.ObjectPool;

public class ConnectionPoolableObjectFactory extends BasePoolableObjectFactory <ConnectionObject> {

    public ConnectionPoolableObjectFactory (ConnectionFactory factory) throws Exception {
       this(factory, null);
    }

    public ConnectionPoolableObjectFactory (ConnectionFactory factory, String queueName) throws Exception {
        this.factory = factory;
        this.queueName = queueName;
    }

    @Override
    public ConnectionObject makeObject() throws Exception {
        final String eLabel = "ConnectionPoolObjectFactory.makeObject: ";
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        if (queueName != null) channel.queueDeclarePassive(queueName);
        if (!channel.isOpen()) throw new Exception(eLabel + "Channel is not open..failure to create Object");
        return new ConnectionObject(connection, channel);

    }

    @Override
    public boolean validateObject(ConnectionObject connObj) {
        try {
            if (queueName != null) connObj.getChannel().queueDeclarePassive(queueName);
            return connObj.getChannel().isOpen();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void destroyObject(ConnectionObject connObj) throws Exception {
        try { connObj.destroy(); } catch (Exception e) { e.printStackTrace(); };
    }

    private final ConnectionFactory factory;
    private final String queueName;

}

class ConnectionObject {

    public ConnectionObject (Connection connection, Channel channel) {
        this.connection = connection;
        this.channel = channel;
    }

    public void destroy () throws Exception {
        try { connection.abort(); } catch (Exception e) { e.printStackTrace(); };
    }

    public Connection getConnection() {
        return connection;
    }
    public Channel getChannel() {
        return channel;
    }
    
    public static ConnectionObject getConnectionObject (ObjectPool<ConnectionObject> connectionPool, ConnectionFactory factory, 
            boolean pooled) throws Exception {
        ConnectionObject connObj = null;
        if (pooled) {
            connObj = connectionPool.borrowObject();
        } else {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            connObj = new ConnectionObject(connection, channel);
        }
        return connObj;
    }
    
    public static void releaseConnectionObject (ObjectPool<ConnectionObject> connectionPool, ConnectionObject connObj, 
            boolean pooled) throws Exception {
        if (pooled) {
            if (connObj != null) connectionPool.returnObject(connObj);
        } else {
            if (connObj != null) connObj.destroy();
        }
    }

    private final Connection connection;
    private final Channel channel;

}