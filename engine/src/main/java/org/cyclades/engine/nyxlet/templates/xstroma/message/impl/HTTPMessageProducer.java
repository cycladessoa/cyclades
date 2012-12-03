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
package org.cyclades.engine.nyxlet.templates.xstroma.message.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import org.cyclades.engine.nyxlet.templates.xstroma.ServiceBrokerNyxletImpl;
import org.cyclades.engine.nyxlet.templates.xstroma.message.api.StreamingMessageProducer;
import org.cyclades.io.ResourceRequestUtils;
import com.google.common.io.ByteStreams;

public class HTTPMessageProducer implements StreamingMessageProducer {

    public void init(Map<String, String> initializationMap) throws Exception {
        if (!initializationMap.containsKey(URI_CONFIG_PARAMETER)) throw new Exception("Initialization parameter missing: " + URI_CONFIG_PARAMETER);
        StringBuilder builder = new StringBuilder(initializationMap.get(URI_CONFIG_PARAMETER));
        builder.append("?").append(ServiceBrokerNyxletImpl.XSTROMA_MESSAGE);
        accessURL = builder.toString();
    }

    public void sendMessage (String message, Map<String, List<String>> attributeMap, OutputStream os) throws Exception {
        final String eLabel = "HTTPMessageProducer.sendMessage: ";
        InputStream is = null;
        try {
            int connectionTimeout = (attributeMap.containsKey(CONNECTION_TIMEOUT)) ? Integer.parseInt(attributeMap.get(CONNECTION_TIMEOUT).get(0)) : 0;
            int readTimeout = (attributeMap.containsKey(READ_TIMEOUT)) ? Integer.parseInt(attributeMap.get(READ_TIMEOUT).get(0)) : 0;
            is = ResourceRequestUtils.getInputStream(accessURL, message.getBytes(), headerProperties, connectionTimeout, readTimeout);
            ByteStreams.copy(is, os);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(eLabel + e);
        } finally {
            try { is.close(); } catch (Exception e) {}
        }
    }

    public String sendMessage (String message, Map<String, List<String>> attributeMap) throws Exception {
        throw new UnsupportedOperationException("Please use the sendMessage implementation from StreamingMessageProducer");
    }

    public void destroy() throws Exception {
    }

    public boolean isHealthy () throws Exception {
        return true;
    }

    private String accessURL;
    public static final String CONNECTION_TIMEOUT   = "connection-timeout";
    public static final String READ_TIMEOUT         = "read-timeout";
    public static final String URI_CONFIG_PARAMETER = "uri";
    private static final Map<String, String> headerProperties;
    static {
        headerProperties = new HashMap<String, String>();
        headerProperties.put("STROMA", "true");
    }
}
