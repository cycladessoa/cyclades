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
package org.cyclades.engine.stroma.xstroma;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cyclades.engine.MetaTypeEnum;
import org.cyclades.engine.api.Nyxlet;
import org.cyclades.engine.nyxlet.NyxletRepository;
import org.cyclades.engine.util.MapHelper;

public class XSTROMABrokerRequest {

    public XSTROMABrokerRequest (String brokerName, MetaTypeEnum metaTypeEnum, Map<String, List<String>> params) throws Exception {
        final String eLabel = "XSTROMABrokerRequest.XSTROMABrokerRequest: ";
        try {
            this.brokerName = brokerName;
            if (params != null) this.parameters = params;
            this.metaTypeEnum = metaTypeEnum;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    /**
     * Static method that bypasses "data" parameter accumulation. The InputStream can also be utilized as the "data" section if no "data"
     * parameter exists in the "parameters" argument.
     *
     * @param brokerName
     * @param metaTypeEnum
     * @param parameters
     * @param is
     * @return XSTROMABrokerResponse
     * @throws Exception
     */
    public static XSTROMABrokerResponse execute (String brokerName, MetaTypeEnum metaTypeEnum, Map<String, List<String>> parameters, InputStream is) throws Exception {
        final String eLabel = "XSTROMABrokerRequest.execute: ";
        try {
            Nyxlet brokerNyxlet = NyxletRepository.getStaticInstance().getNyxlet(brokerName);
            if (brokerNyxlet == null) throw new Exception("Service not found: " + brokerName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (metaTypeEnum != null) parameters.put("data-type", new ArrayList<String>(Arrays.asList(metaTypeEnum.name().toLowerCase())));
            brokerNyxlet.process(parameters, is, baos);
            return new XSTROMABrokerResponse(metaTypeEnum, baos.toString());
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public XSTROMABrokerResponse execute () throws Exception {
        final String eLabel = "XSTROMABrokerRequest.execute: ";
        try {
            Nyxlet mod = NyxletRepository.getStaticInstance().getNyxlet(brokerName);
            if (mod == null) throw new Exception("Service not found: " + brokerName);
            return execute(mod);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public XSTROMABrokerResponse execute (Nyxlet brokerNyxlet) throws Exception {
        final String eLabel = "XSTROMABrokerRequest.execute: ";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            parameters.put("data-type", new ArrayList<String>(Arrays.asList(metaTypeEnum.name().toLowerCase())));
            parameters.put("data", new ArrayList<String>(Arrays.asList(generateData())));
            brokerNyxlet.process(parameters, null, baos);
            return new XSTROMABrokerResponse(metaTypeEnum, baos.toString());
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public void execute (OutputStream os) throws Exception {
        final String eLabel = "XSTROMABrokerRequest.execute(OutputStream): ";
        try {
            Nyxlet mod = NyxletRepository.getStaticInstance().getNyxlet(brokerName);
            if (mod == null) throw new Exception("Service not found: " + brokerName);
            execute(mod, os);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public void execute (Nyxlet brokerNyxlet, OutputStream os) throws Exception {
        final String eLabel = "XSTROMABrokerRequest.execute(Nyxlet, OutputStream): ";
        try {
            parameters.put("data-type", new ArrayList<String>(Arrays.asList(metaTypeEnum.name().toLowerCase())));
            parameters.put("data", new ArrayList<String>(Arrays.asList(generateData())));
            brokerNyxlet.process(parameters, null, os);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    /**
     *
     * @param serviceName
     * @param params    key/value pairs for the request..basically the same as would be query parameters in the HTTP request
     * @param data      json: Populate as a JSONObject between the "{" and the "}", exclusive
     *                          xml: Populate as a root element, between <root> and the </root>, exclusive
     * @throws Exception
     */
    public void addSTROMARequest (String serviceName, Map<String, List<String>> params, String data) throws Exception {
        final String eLabel = "XSTROMABrokerRequest.addSTROMARequest: ";
        try {
            addSTROMARequest(new STROMARequest(serviceName, params, data));
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public void addSTROMARequest (STROMARequest stromaRequest) throws Exception {
        final String eLabel = "XSTROMABrokerRequest.addSTROMARequest: ";
        try {
            serviceRequests.add(stromaRequest);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }
    
    public void setSTROMARequests (List<STROMARequest> serviceRequests) {
        this.serviceRequests = serviceRequests;
    }

    public String generateData () throws Exception {
        return (metaTypeEnum.equals(MetaTypeEnum.XML)) ? generateXMLData() : generateJSONData();
    }

    public String generateJSONData () throws Exception {
        final String eLabel = "XSTROMABrokerRequest.generateJSONData: ";
        try {
            // Build data section
            StringBuilder builder = new StringBuilder("{\"requests\":[");
            int i = 0;
            for (STROMARequest request : serviceRequests) {
                if (i++ > 0) builder.append(",");
                builder.append(request.toJSONString());
            }
            builder.append("]}");
            return builder.toString();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public String generateXMLData () throws Exception {
        final String eLabel = "XSTROMABrokerRequest.generateXMLData: ";
        try {
            StringBuilder builder = new StringBuilder("<data><requests>");
            for (STROMARequest request : serviceRequests) {
                builder.append(request.toXMLString());
            }
            builder.append("</requests></data>");
            return builder.toString();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }
    
    public String toXSTROMAMessage () throws Exception {
        return (metaTypeEnum.equals(MetaTypeEnum.XML)) ? toXMLXSTROMAMessage() : toJSONXSTROMAMessage();
    }

    public String toJSONXSTROMAMessage () throws Exception {
        StringBuilder builder = new StringBuilder("{\"");
        builder.append(BASE_PARAMETERS).append("\":");
        builder.append(MapHelper.parameterMapToJSON(parameters));    
        builder.append(",\"data\":{\"requests\":[");
        int i = 0;
        for (STROMARequest request : serviceRequests) {
            if (i++ > 0) builder.append(",");
            builder.append(request.toJSONString());
        }
        builder.append("]}}");
        return builder.toString();
    }

    public String toXMLXSTROMAMessage () throws Exception {
        final String eLabel = "XSTROMABrokerRequest.generateXMLData: ";
        try {
            StringBuilder builder = new StringBuilder("<x-stroma>");
            builder.append("<").append(BASE_PARAMETERS).append(">");
            builder.append(MapHelper.parameterMapToXML(parameters, "parameter"));
            builder.append("</").append(BASE_PARAMETERS).append(">");
            builder.append("<data><requests>");
            for (STROMARequest request : serviceRequests) {
                builder.append(request.toXMLString());
            }
            builder.append("</requests></data></x-stroma>");
            return builder.toString();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }
    
    public Map<String, List<String>> getParameters () {
        return parameters;
    }

    private Map<String, List<String>> parameters = new HashMap<String, List<String>>();
    private MetaTypeEnum metaTypeEnum;
    private String brokerName;
    private List<STROMARequest> serviceRequests = new ArrayList<STROMARequest>();
    private static final String BASE_PARAMETERS = "parameters";
    
}
