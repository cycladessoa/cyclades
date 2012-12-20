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
package org.cyclades.engine.stroma;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.cyclades.engine.MetaTypeEnum;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.util.MapHelper;
import org.cyclades.xml.comparitor.XMLComparitor;
import org.json.JSONObject;
import org.w3c.dom.Node;

public class STROMAResponse {

    public STROMAResponse (Object object) throws Exception {
        if (object instanceof JSONObject) {
            init((JSONObject)object);
        } else {
            init((Node)object);
        }
    }

    private void init (Node node) throws Exception {
        final String eLabel = "STROMAResponse.STROMAResponse(Node): ";
        try {
            this.serviceName = XMLComparitor.getAttribute(node, "service");
            this.transactionData = XMLComparitor.getAttribute(node, NyxletSession.TRANSACTION_DATA_PARAMETER);
            this.serviceAgent = XMLComparitor.getAttribute(node, NyxletSession.SERVICE_AGENT_PARAMETER);
            this.action = XMLComparitor.getAttribute(node, "action");
            this.errorCode = Integer.parseInt(XMLComparitor.getAttribute(node, ERROR_CODE));
            this.errorMessage = XMLComparitor.getAttribute(node, ERROR_MESSAGE);
            Vector<Node> parameterNodes = XMLComparitor.getMatchingChildNodes(node, PARAMETERS);
            if (parameterNodes.size() > 0) this.parameters = MapHelper.parameterMapFromMetaObject(parameterNodes.firstElement().getChildNodes());
            this.data = node;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private void init (JSONObject jsonObject) throws Exception {
        final String eLabel = "STROMAResponse.STROMAResponse(JSONObject): ";
        try {
            this.serviceName = jsonObject.getString("service");
            this.transactionData = (jsonObject.has(NyxletSession.TRANSACTION_DATA_PARAMETER)) ? jsonObject.getString(NyxletSession.TRANSACTION_DATA_PARAMETER) : null;
            this.serviceAgent = (jsonObject.has(NyxletSession.SERVICE_AGENT_PARAMETER)) ? jsonObject.getString(NyxletSession.SERVICE_AGENT_PARAMETER) : null;
            this.action = (jsonObject.has("action")) ? jsonObject.getString("action") : null;
            this.errorCode = Integer.parseInt(jsonObject.getString(ERROR_CODE));
            this.errorMessage = jsonObject.has(ERROR_MESSAGE) ? jsonObject.getString(ERROR_MESSAGE) : null;
            this.parameters = jsonObject.has(PARAMETERS) ? MapHelper.parameterMapFromMetaObject(jsonObject.getJSONArray(PARAMETERS)) : null;
            this.data = jsonObject.has("data") ? jsonObject.getJSONObject("data") : new JSONObject();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public static STROMAResponse fromBytes (MetaTypeEnum metaTypeEnum, byte[] data) throws Exception {
        if (data == null || data.length == 0) return null;
        return fromString(metaTypeEnum, new String(data));
    }

    public static STROMAResponse fromString (MetaTypeEnum metaTypeEnum, String data) throws Exception {
        final String eLabel = "STROMAResponse.fromString: ";
        try {
            if (data == null || data.isEmpty()) return null;
            return new STROMAResponse(metaTypeEnum.createObjectFromMeta(data));
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public String getAction() {
        return action;
    }

    public Object getData() {
        return data;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getServiceName() {
        return serviceName;
    }
    
    public String getTransactionData () {
        return transactionData;
    }
    
    public String getServiceAgent () {
        return serviceAgent;
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

    private String serviceName;
    private String action;
    private int errorCode;
    private String errorMessage;
    private Map<String, List<String>> parameters = null;
    private Object data;
    private String transactionData;
    private String serviceAgent;
    public final static String ERROR_CODE       = "error-code";
    public final static String ERROR_MESSAGE    = "error-message";
    public final static String PARAMETERS       = "parameters";
    
}
