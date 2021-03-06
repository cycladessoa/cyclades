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
package org.cyclades.engine.nyxlet.templates.stroma.actionhandler;

import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamWriter;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.api.Nyxlet;
import org.cyclades.engine.nyxlet.templates.stroma.STROMANyxlet;
import org.cyclades.engine.stroma.STROMAResponseWriter;

public class GetServiceMetaHandler extends ActionHandler {

    public GetServiceMetaHandler(STROMANyxlet parentNyxlet) {
        super(parentNyxlet);
    }

    @Override
    public void handle (NyxletSession nyxletSession, Map<String, List<String>> baseParameters, STROMAResponseWriter stromaResponseWriter) throws Exception {
        final String eLabel = "GetServiceMetaHandler.handle: ";
        try {
            stromaResponseWriter.addResponseParameter(Nyxlet.SERVICE_AGENT, getParentNyxlet().getServiceAgentAttribute());
            Map<Object, Object> buildProperties = getParentNyxlet().getBuildProperties();
            if (buildProperties != null && (!baseParameters.containsKey(BUILD_INFO) || parameterAsBoolean(BUILD_INFO, baseParameters, false))) {
                XMLStreamWriter streamWriter = stromaResponseWriter.getXMLStreamWriter();
                streamWriter.writeStartElement(BUILD_INFO);
                for (Map.Entry<Object, Object> mapEntry : buildProperties.entrySet()) {
                    streamWriter.writeStartElement("parameter");
                    streamWriter.writeAttribute("name", mapEntry.getKey().toString());
                    streamWriter.writeAttribute("value", mapEntry.getValue().toString());
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

    private static final String BUILD_INFO = "buildinfo";

}
