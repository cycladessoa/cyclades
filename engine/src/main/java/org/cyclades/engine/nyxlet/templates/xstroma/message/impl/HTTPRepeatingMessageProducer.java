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

import java.io.OutputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import org.cyclades.engine.MetaTypeEnum;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.nyxlet.templates.xstroma.ServiceBrokerNyxletImpl;
import org.cyclades.engine.nyxlet.templates.xstroma.XSTROMANyxlet;
import org.cyclades.engine.nyxlet.templates.xstroma.message.api.StreamingMessageProducer;
import org.cyclades.engine.stroma.STROMAResponseWriter;
import org.cyclades.engine.stroma.xstroma.XSTROMAResponseWriter;
import org.cyclades.io.ResourceRequestUtils;
import org.cyclades.io.StreamUtils;

public class HTTPRepeatingMessageProducer implements StreamingMessageProducer {

    public void init(Map<String, String> initializationMap) throws Exception {
        queryParameters = new StringBuilder("?").append(ServiceBrokerNyxletImpl.XSTROMA_MESSAGE).toString();
    }

    public void sendMessage (String message, Map<String, List<String>> attributeMap, OutputStream os) throws Exception {
        final String eLabel = "HTTPRepeatingMessageProducer.sendMessage: ";
        NyxletSession ns = new NyxletSession(attributeMap, null, os);
        XSTROMAResponseWriter writer = new XSTROMAResponseWriter("servicebroker", ns);
        try {
            if (!attributeMap.containsKey(URI_PARAMETER)) throw new Exception("Requires parameter parameter: " + URI_PARAMETER);
            int connectionTimeout = (attributeMap.containsKey(CONNECTION_TIMEOUT)) ? Integer.parseInt(attributeMap.get(CONNECTION_TIMEOUT).get(0)) : 0;
            int readTimeout = (attributeMap.containsKey(READ_TIMEOUT)) ? Integer.parseInt(attributeMap.get(READ_TIMEOUT).get(0)) : 0;
            String responseString;
            OutputStream writeTo = writer.getOutputStream();
            int i = 0;
            for (String uri : attributeMap.get(URI_PARAMETER)) {
                if (i++ > 0 && ns.getResponseMetaTypeEnum().equals(MetaTypeEnum.JSON)) writeTo.write(",".getBytes());
                writer.addResponseParameter(URI_PARAMETER, uri);
                try {
                    responseString = new String(ResourceRequestUtils.getData(uri + queryParameters, message.getBytes(), headerProperties, StreamUtils.DEFAULT_BUFFER_SIZE,
                        connectionTimeout, readTimeout));
                    writeTo.write(responseString.getBytes());
                    if (responseString.contains(XSTROMAResponseWriter.ORCHESTRATION_FAULT_ENCOUNTERED_ATTRIBUTE)) {
                        writer.addResponseParameter("error", uri);
                        break;
                    }
                } catch (Exception e) {
                    // We caught an exception...let's print it out and bail
                    new STROMAResponseWriter("servicebroker", ns).writeErrorResponse(1, e.toString());
                    writer.addResponseParameter("error", new StringBuilder(uri).append(" " ).append(e.toString()).toString());
                    if (attributeMap.containsKey(XSTROMANyxlet.RESPECT_ORCHESTRATION_FAULT_ATTRIBUTE)) break;
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(eLabel + e);
        } finally {
            writer.done();
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

    private String queryParameters;
    public static final String CONNECTION_TIMEOUT   = "connection-timeout";
    public static final String READ_TIMEOUT         = "read-timeout";
    public static final String URI_PARAMETER        = "uri";
    private static final Map<String, String> headerProperties;
    static {
        headerProperties = new HashMap<String, String>();
        headerProperties.put("STROMA", "true");
    }
}
