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

import java.util.List;
import org.cyclades.engine.nyxlet.templates.stroma.STROMANyxlet;
import org.cyclades.engine.MetaTypeEnum;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.nyxlet.templates.stroma.actionhandler.ChainableActionHandler;
import org.cyclades.engine.nyxlet.templates.xstroma.OrchestrationTypeEnum;
import org.cyclades.annotations.AHandler;
import org.cyclades.engine.stroma.STROMAResponse;
import org.cyclades.engine.stroma.STROMAResponseWriter;
import org.cyclades.nyxlet.admin.util.Auth;
import org.cyclades.nyxlet.admin.util.Resource;
import org.cyclades.nyxlet.admin.util.StatusCodeEnum;

import java.util.Map;
import javax.xml.stream.XMLStreamWriter;
import org.w3c.dom.Node;

@AHandler("add")
public class AddHandlerSTROMA extends ChainableActionHandler {

    public AddHandlerSTROMA (STROMANyxlet parentNyxlet) {
        super(parentNyxlet);
    }

    @Override
    public void init () throws Exception {
        Auth.addPasswordValidation(this);
    }

    @Override
    public void handleMapChannel (NyxletSession nyxletSession, Map<String, List<String>> baseParameters, STROMAResponseWriter stromaResponseWriter) throws Exception {
        handleLocal(nyxletSession, baseParameters, stromaResponseWriter, (byte[])nyxletSession.getMapChannelObject(Resource.MAP_CHANNEL_OBJECT));
        nyxletSession.getMapChannel().remove(Resource.MAP_CHANNEL_OBJECT);
    }

    @Override
    public void handleSTROMAResponse (NyxletSession nyxletSession, Map<String, List<String>> baseParameters, STROMAResponseWriter stromaResponseWriter, STROMAResponse stromaResponse) throws Exception {
        byte[] payload = (stromaResponse == null) ? null : nyxletSession.getMetaTypeEnum().createMetaFromObject(stromaResponse.getData()).getBytes();
        handleLocal(nyxletSession, baseParameters, stromaResponseWriter, payload);
    }

    private void handleLocal (NyxletSession nyxletSession, Map<String, List<String>> baseParameters, STROMAResponseWriter stromaResponseWriter, byte[] chainedInputData) throws Exception {
        final String eLabel = "AddHandler.handle: ";
        try {
            StatusCodeEnum statusCodeEnum = StatusCodeEnum.SUCCESS;
            String message;
            String resourceURI;
            if (nyxletSession.getOrchestrationTypeEnum().equals(OrchestrationTypeEnum.NONE)) {
                resourceURI = Resource.getRequestResourcePath(nyxletSession, baseParameters, 1);
            } else {
                resourceURI = (baseParameters.containsKey(Resource.URI_FIELD)) ? baseParameters.get(Resource.URI_FIELD).get(0) : null;
            }
            if (resourceURI == null) {
                statusCodeEnum = StatusCodeEnum.REQUEST_FORMAT_ERROR;
                message = "Cannot detect path source (uri)";
            } else {
                String absoluteResourceURI = parentNyxlet.getEngineContext().getCanonicalEngineApplicationBaseDirectoryPath(resourceURI);
                message = resourceURI;
                if (chainedInputData != null) {
                    Resource.writeResource(absoluteResourceURI, chainedInputData);
                } else if (baseParameters.containsKey(Resource.PAYLOAD_PARAMETER)) {
                    Resource.writeResource(absoluteResourceURI, baseParameters.get(Resource.PAYLOAD_PARAMETER).get(0).getBytes());
                } else if (baseParameters.containsKey(Resource.SOURCE_PARAMETER)) {
                    Resource.writeResource(absoluteResourceURI, parentNyxlet.getEngineContext().getCanonicalEngineApplicationBaseDirectoryPath(baseParameters.get(Resource.SOURCE_PARAMETER).get(0)));
                } else {
                    // Just write the XML or the JSON in the data area....
                    // XXX - You can optimize on the XML side by not getting a String and the bytes
                    if (nyxletSession.getDataObject() != null) {
                        if (nyxletSession.getMetaTypeEnum().equals(MetaTypeEnum.XML)) {
                            Resource.writeResourceEnhancedXML(absoluteResourceURI, (Node)nyxletSession.getDataObject());
                        } else {
                            Resource.writeResource(absoluteResourceURI, nyxletSession.getMetaTypeEnum().createMetaFromObject(nyxletSession.getDataObject()).getBytes());
                        }
                    } else {
                        statusCodeEnum = StatusCodeEnum.REQUEST_FORMAT_ERROR;
                        message = "No data to add!";
                    }
                }
            }
            if (nyxletSession.rawResponseRequested()) nyxletSession.getHttpServletResponse().setStatus(Integer.parseInt(statusCodeEnum.getCode()));
            XMLStreamWriter streamWriter = stromaResponseWriter.getXMLStreamWriter();
            streamWriter.writeStartElement("entity");
            streamWriter.writeAttribute("status-code", statusCodeEnum.getCode());
            streamWriter.writeAttribute("message", message);
            streamWriter.writeEndElement();
        } catch (Exception e) {
            getParentNyxlet().logStackTrace(e);
            handleException(nyxletSession, stromaResponseWriter, eLabel, e);
        } finally {
            stromaResponseWriter.done();
        }
    }

    @Override
    public boolean isSTROMAResponseCompatible (STROMAResponse response) throws UnsupportedOperationException {
        return true;
    }

    @Override
    public Object[] getMapChannelKeyTargets (NyxletSession nyxletSession) {
        return new Object[]{Resource.MAP_CHANNEL_OBJECT};
    }
}
