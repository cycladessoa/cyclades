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
package org.cyclades.nyxlet.servicebrokernyxlet.message.impl.rabbitmq.consumer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import org.cyclades.nyxlet.servicebrokernyxlet.message.impl.rabbitmq.ConnectionResource;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

public class RabbitMQDefaultConsumer extends DefaultConsumer implements RabbitMQConsumer {

    public RabbitMQDefaultConsumer (ConnectionResource connectionResource) throws IOException {
        super(connectionResource.getChannel());
        this.connectionResource = connectionResource;
    }

    public RabbitMQDefaultConsumer init (Map<String, String> parameters) throws Exception {
        if (parameters.containsKey(REPLYTO_MESSAGE_DELIVERY_MODE)) replyToMessageDeliveryMode = Integer.parseInt(parameters.get(REPLYTO_MESSAGE_DELIVERY_MODE));
        return this;
    }

    public void destroy () throws Exception {
        // No-op
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        final String eLabel = "RabbitMQDefaultConsumer.handleDelivery: ";
        try {
            //System.out.println("CONSUMER: " + consumerTag);
            byte[] response;
            if (connectionResource.hasRawMessageProcessor()) {
                response = connectionResource.fireRawMessageProcessor(body);
            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                connectionResource.getCallBackServiceInstance().processXSTROMAMessagePayload(baos, new String(body, "UTF-8"));
                response = baos.toByteArray();
            }
            String replyToQueue = properties.getReplyTo();
            if (replyToQueue != null && !replyToQueue.isEmpty()) {
                AMQP.BasicProperties.Builder propsBuilder = new AMQP.BasicProperties.Builder();
                if (replyToMessageDeliveryMode > -1) propsBuilder.deliveryMode(replyToMessageDeliveryMode);
                connectionResource.getChannel().basicPublish("", replyToQueue, propsBuilder.build(), response);
            } else {
                // XXX - Verify this is what we want to do if there is no replyto set...DONT'T REPLY!
                //System.out.println(baos.toString());
            }
            if (connectionResource.hasResponseProcessor()) connectionResource.fireResponseProcessor(response);
        } catch (Exception e) {
            connectionResource.getCallBackServiceInstance().logError(eLabel + e);
        } finally {
            try {
                connectionResource.getChannel().basicAck(envelope.getDeliveryTag(), false);
            } catch (Exception e) {
                connectionResource.getCallBackServiceInstance().logError(eLabel + e);
            }
        }
    }

    @Override
    public void handleCancel (String consumerTag) throws IOException {
        final String eLabel = "rabbitmq.Consumer.handleCancel: ";
        try {
            // System.out.println("This queue has been abruptly cancelled (i.e. deleted): " + connectionResource.getQueueName());
            if (connectionResource.getCancelRecovery()) {
                System.out.println(eLabel + "Re-connecting to queue: " + connectionResource.getQueueName() + " consumerTag: " + consumerTag);
                connectionResource.reconnect();
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void handleShutdownSignal(java.lang.String consumerTag, ShutdownSignalException sig) {
        // Can we use this to flag a certain condition? TBD. Let's keep it simple for now.
    }

    ConnectionResource connectionResource;
    private int replyToMessageDeliveryMode = -1;

}
