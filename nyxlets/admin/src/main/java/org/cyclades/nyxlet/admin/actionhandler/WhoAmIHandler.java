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
package org.cyclades.nyxlet.admin.actionhandler;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamWriter;
import org.cyclades.annotations.AHandler;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.nyxlet.templates.stroma.STROMANyxlet;
import org.cyclades.engine.nyxlet.templates.stroma.actionhandler.ActionHandler;
import org.cyclades.engine.stroma.STROMAResponseWriter;

@AHandler("whoami")
public class WhoAmIHandler extends ActionHandler {

    public WhoAmIHandler (STROMANyxlet parentNyxlet) {
        super(parentNyxlet);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handle (NyxletSession nyxletSession, Map<String, List<String>> baseParameters, STROMAResponseWriter stromaResponseWriter) throws Exception {
        final String eLabel = "WhoAmIHandler.handle: ";
        try {
            XMLStreamWriter streamWriter = stromaResponseWriter.getXMLStreamWriter();
            // HTTP Method
            streamWriter.writeStartElement("http_method");
            streamWriter.writeCharacters(nyxletSession.getHttpServletRequest().getMethod());
            streamWriter.writeEndElement();
            // HTTP Headers
            Enumeration<String> headerNameEnumeration = nyxletSession.getHttpServletRequest().getHeaderNames();
            Enumeration<String> headerValueEnumeration;
            streamWriter.writeStartElement("http_headers");
            String temp;
            String temp2;
            if (headerNameEnumeration != null) {
                while (headerNameEnumeration.hasMoreElements()) {
                    temp = headerNameEnumeration.nextElement();
                    streamWriter.writeStartElement("header");
                    streamWriter.writeAttribute("name", temp);
                    headerValueEnumeration = nyxletSession.getHttpServletRequest().getHeaders(temp);
                    while (headerValueEnumeration.hasMoreElements()) {
                        temp2 = headerValueEnumeration.nextElement();
                        streamWriter.writeStartElement("value");
                        streamWriter.writeCharacters(temp2);
                        streamWriter.writeEndElement();
                    }
                    streamWriter.writeEndElement();
                }
            }
            streamWriter.writeEndElement();
            // HTTP Query Parameters
            Map<String, String[]> parameterMap = nyxletSession.getHttpServletRequest().getParameterMap();
            streamWriter.writeStartElement("http_parameters");
            if (parameterMap != null) {
                for (Map.Entry<String, String[]> entry: parameterMap.entrySet()) {
                    streamWriter.writeStartElement("parameter");
                    streamWriter.writeAttribute("name", entry.getKey());
                    for (String value : entry.getValue()) {
                        streamWriter.writeStartElement("value");
                        streamWriter.writeCharacters(value);
                        streamWriter.writeEndElement();
                    }
                    streamWriter.writeEndElement();
                }
            }
            // STROMA Parameters
            streamWriter.writeEndElement();
            if (baseParameters.containsKey(STROMA_PARAMETERS)) {
                streamWriter.writeStartElement("stroma_parameters");
                for (Map.Entry<String, List<String>> entry: baseParameters.entrySet()) {
                    streamWriter.writeStartElement("parameter");
                    streamWriter.writeAttribute("name", entry.getKey());
                    for (String value : entry.getValue()) {
                        streamWriter.writeStartElement("value");
                        streamWriter.writeCharacters(value);
                        streamWriter.writeEndElement();
                    }
                    streamWriter.writeEndElement();
                }
                streamWriter.writeEndElement();
            }
        } catch (Exception e) {
            getParentNyxlet().logStackTrace(e);
            handleException(nyxletSession, stromaResponseWriter, eLabel, e);
        } finally {
            stromaResponseWriter.done();
        }
    }

    private final static String STROMA_PARAMETERS = "stroma";
}
