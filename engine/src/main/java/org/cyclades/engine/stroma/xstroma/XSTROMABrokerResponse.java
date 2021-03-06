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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.cyclades.engine.MetaTypeEnum;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.stroma.STROMAResponse;
import org.cyclades.engine.util.GenericXMLObject;
import org.cyclades.engine.util.MapHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.cyclades.xml.comparitor.XMLComparitor;

public class XSTROMABrokerResponse {
    
    public XSTROMABrokerResponse (String responseString) throws Exception {
        this((responseString.charAt(0) == '<') ? MetaTypeEnum.XML : MetaTypeEnum.JSON, responseString);
    }

    public XSTROMABrokerResponse (MetaTypeEnum metaTypeEnum, String responseString) throws Exception {
        final String eLabel = "XSTROMABrokerResponse.STROMABrokerResponse: ";
        try {
            if (metaTypeEnum.equals(MetaTypeEnum.JSON)) {
                populate(new JSONObject(responseString));
            } else {
                populate((new GenericXMLObject(responseString)).getRootElement());
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }
    
    public static XSTROMABrokerResponse parse (String responseString) throws Exception {
        return new XSTROMABrokerResponse(responseString);
    }

    private void populate (Node node) throws Exception {
        final String eLabel = "XSTROMABrokerResponse.populate(Node): ";
        try {
            serviceName = XMLComparitor.getAttribute(node, "service");
            errorCode = Integer.parseInt(XMLComparitor.getAttribute(node, ERROR_CODE));
            errorMessage = XMLComparitor.getAttribute(node, ERROR_MESSAGE);
            transactionData = XMLComparitor.getAttribute(node, NyxletSession.TRANSACTION_DATA_PARAMETER);
            serviceAgent = XMLComparitor.getAttribute(node, NyxletSession.SERVICE_AGENT_PARAMETER);
            orchestrationFault = (XMLComparitor.getMatchingChildNodes(node, ORCHESTRATION_FAULT).size() > 0);
            Vector<Node> nodesVector = XMLComparitor.getMatchingChildNodes(node, "response");
            for (int i = 0; i < nodesVector.size(); i++) {
                responses.add(new STROMAResponse(nodesVector.get(i)));
            }
            nodesVector = XMLComparitor.getMatchingChildNodes(node, PARAMETERS);
            for (int i = 0; i < nodesVector.size(); i++) {
                this.parameters = MapHelper.parameterMapFromMetaObject(nodesVector.get(i).getChildNodes());
            }
            nodesVector = XMLComparitor.getMatchingChildNodes(node, "duration");
            if (nodesVector.size() > 0) this.duration = Long.parseLong(XMLComparitor.getAttribute(nodesVector.firstElement(), "val"));
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private void populate (JSONObject jsonObject) throws Exception {
        final String eLabel = "XSTROMABrokerResponse.populate(JSONObject): ";
        try {
            serviceName = jsonObject.getString("service");
            errorCode = Integer.parseInt(jsonObject.getString(ERROR_CODE));
            errorMessage = (jsonObject.has(ERROR_MESSAGE)) ? jsonObject.getString(ERROR_MESSAGE) : null;
            transactionData = (jsonObject.has(NyxletSession.TRANSACTION_DATA_PARAMETER)) ? jsonObject.getString(NyxletSession.TRANSACTION_DATA_PARAMETER) : null;
            serviceAgent = (jsonObject.has(NyxletSession.SERVICE_AGENT_PARAMETER)) ? jsonObject.getString(NyxletSession.SERVICE_AGENT_PARAMETER) : null;
            if (jsonObject.has(ORCHESTRATION_FAULT)) orchestrationFault = jsonObject.getString(ORCHESTRATION_FAULT).equalsIgnoreCase("true");
            this.parameters = jsonObject.has(PARAMETERS) ? MapHelper.parameterMapFromMetaObject(jsonObject.getJSONArray(PARAMETERS)) : null;
            JSONObject data = (jsonObject.has("data")) ? jsonObject.getJSONObject("data") : null;
            if (data == null || !data.has("responses")) return;
            JSONArray serviceResponses = data.getJSONArray("responses");
            JSONObject serviceResponse;
            for (int i = 0; i < serviceResponses.length(); i++) {
                serviceResponse = serviceResponses.getJSONObject(i);
                responses.add(new STROMAResponse(serviceResponse));
            }
            if (jsonObject.has("duration")) duration = Long.parseLong(jsonObject.getString("duration"));
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean getOrchestrationFault() {
        return orchestrationFault;
    }

    public String getTransactionData () {
        return transactionData;
    }
    
    public String getServiceAgent () {
        return serviceAgent;
    }
    
    public long getDuration () {
        return duration;
    }

    public List<STROMAResponse> getResponses() {
        return responses;
    }
    
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Get the parameters of this STROMAResponse. This could be null if there were no parameters
     * specified.
     *
     * @return response parameters
     */
    public Map<String, List<String>> getParameters () {
        return parameters;
    }

    public final static String ERROR_CODE       = "error-code";
    public final static String ERROR_MESSAGE    = "error-message";
    private List<STROMAResponse> responses = new ArrayList<STROMAResponse>();
    private String serviceName;
    private int errorCode;
    private String errorMessage;
    private boolean orchestrationFault = false;
    private String transactionData = null;
    private String serviceAgent = null;
    private Map<String, List<String>> parameters = null;
    private long duration = -1;
    public final static String PARAMETERS           = "parameters";
    public final static String ORCHESTRATION_FAULT  = "orchestration-fault";
    
}
