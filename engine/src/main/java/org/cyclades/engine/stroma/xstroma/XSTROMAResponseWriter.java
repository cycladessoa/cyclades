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

import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringEscapeUtils;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.api.Nyxlet;
import org.cyclades.engine.util.MapHelper;
import org.json.JSONObject;

public class XSTROMAResponseWriter {

    public XSTROMAResponseWriter (String serviceName, NyxletSession nyxletSession)  throws Exception {
        this(serviceName, nyxletSession, null);
    }
    
    public XSTROMAResponseWriter (String serviceName, NyxletSession nyxletSession, Nyxlet nyxlet)  throws Exception {
        final String eLabel = "XSTROMAResponseWriter.XSTROMAResponseWriter: ";
        try {
            if (nyxlet != null) {
                if (nyxletSession.serviceAgentRequested()) serviceAgent = nyxlet.getServiceAgentAttribute();   
            }
            this.outputStream = nyxletSession.getOutputStream();
            this.serviceName = serviceName;
            this.transactionData = nyxletSession.getTransactionDataString();
            this.durationRequested = nyxletSession.durationRequested();
            this.durationStart = nyxletSession.getDurationStart();
            switch (nyxletSession.getResponseMetaTypeEnum()) {
            case JSON:
                isXML = false;
                break;
            case XML:
                isXML = true;
                break;
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public void setResponseParameters (Map<String, List<String>> responseParameterMap) throws Exception {
        if (done) throw new Exception("Response already done");
        this.responseParameterMap = responseParameterMap;
    }

    public XSTROMAResponseWriter addResponseParameter(String key, String value) throws Exception {
        if( this.responseParameterMap == null ) {
            this.responseParameterMap = new LinkedHashMap<String, List<String>>();
        }
        List<String> values = this.responseParameterMap.get(key);
        if( values == null ) {
            values = new LinkedList<String>();
            this.responseParameterMap.put(key, values);
        }
        values.add(value);
        return this;
    }

    public OutputStream getOutputStream () throws Exception {
        final String eLabel = "STROMAResponseWriter.getOutputStream: ";
        try {
            checkAndSetResponseInFlight();
            if (isXML) {
                writeXMLResponsePreMeta(null, 0);
            } else {
                writeJSONResponsePreMeta(null, 0);
            }
            return outputStream;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public void done () throws Exception {
        final String eLabel = "STROMAResponseWriter.done: ";
        try {
            if (!responseInFlight) throw new Exception("getOutputStream was never called");
            if (done) throw new Exception("Response already done");
            if (isXML) {
                writeXMLResponsePostMeta();
            } else {
                writeJSONResponsePostMeta();
            }
            done = true;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public void writeResponse (String content) throws Exception {
        writeAsString(content, 0);
        done = true;
    }

    public void writeErrorResponse (int errorCode, String errorMessage) throws Exception {
        writeAsString(errorMessage, errorCode);
        done = true;
    }

    private void writeAsString (String content, int error) throws Exception {
        final String eLabel = "STROMAResponse.writeAsString: ";
        try {
            checkAndSetResponseInFlight();
            if (isXML) {
                writeXMLResponsePreMeta(content, error);
                if (error < 1) write(content);
                writeXMLResponsePostMeta();
            } else {
                writeJSONResponsePreMeta(content, error);
                if (error < 1) write(content);
                writeJSONResponsePostMeta();
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private void checkAndSetResponseInFlight () throws Exception {
        checkResponseInFlight ();
        responseInFlight = true;
    }

    private void checkResponseInFlight () throws Exception {
        if (done) throw new Exception("Response already done");
        if (responseInFlight) throw new Exception("Response already in flight, no turning back now..." +
                "you should be building your meta data with the OutputStream");
    }

    private void writeJSONResponsePreMeta (String content, int error) throws Exception {
        final String eLabel = "XSTROMAResponseWriter.writeJSONResponsePreMeta: ";
        if (omitPrefix) return;
        try {
            write("{\"");
            write(ERROR_CODE_ATTRIBUTE);
            write("\":\"0\",\"");
            write(SERVICE_ATTRIBUTE);
            write("\":\"");
            write(serviceName);
            write("\",\"");
            if (serviceAgent != null) {
                write(NyxletSession.SERVICE_AGENT_PARAMETER);
                write("\":");
                write(JSONObject.quote(serviceAgent));
                write(",\"");
            }
            if (transactionData != null) {
                write(TRANSACTION_DATA_ATTRIBUTE);
                write("\":");
                write(JSONObject.quote(transactionData));
                write(",\"");
            }
            write(DATA_ATTRIBUTE);
            write("\":{\"");
            write(RESPONSES_ATTRIBUTE);
            write("\":[");
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private void writeJSONResponsePostMeta () throws Exception {
        final String eLabel = "STROMAResponseWriter.writeJSONResponsePostMeta: ";
        if (omitSuffix) return;
        try {
            write("]}");
            if (writeOrchestrationFault) {
                write(",\"");
                write(ORCHESTRATION_FAULT_ENCOUNTERED_ATTRIBUTE);
                write("\":\"true\"");
            }
            if (responseParameterMap != null) {
                write(",\"");
                write(PARAMETERS_ATTRIBUTE);
                write("\":");
                write(MapHelper.parameterMapToJSON(responseParameterMap));
            }
            if (durationRequested) {
                write(",\"");
                write(DURATION_ATTRIBUTE);
                write("\":\"");
                write(String.valueOf((System.currentTimeMillis() - durationStart)));
                write("\"");
            }
            write("}");
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private void writeXMLResponsePreMeta (String content, int error) throws Exception {
        final String eLabel = "XSTROMAResponseWriter.writeXMLResponsePreMeta: ";
        if (omitPrefix) return;
        try {
            write("<");
            write(RESPONSE_ATTRIBUTE);
            write(" ");
            writeXMLAttribute(SERVICE_ATTRIBUTE, serviceName, true);
            if (serviceAgent != null) {
                writeXMLAttribute(NyxletSession.SERVICE_AGENT_PARAMETER, StringEscapeUtils.escapeXml(serviceAgent), true);
            }
            if (transactionData != null) {
                writeXMLAttribute(TRANSACTION_DATA_ATTRIBUTE, StringEscapeUtils.escapeXml(transactionData), true);
            }
            writeXMLAttribute(ERROR_CODE_ATTRIBUTE, "0", false);
            write(">");
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private void writeXMLResponsePostMeta () throws Exception {
        final String eLabel = "STROMAResponseWriter.writeXMLResponsePostMeta: ";
        if (omitSuffix) return;
        try {
            if (writeOrchestrationFault) {
                write("<");
                write(ORCHESTRATION_FAULT_ENCOUNTERED_ATTRIBUTE);
                write("/>");
            }
            if (responseParameterMap != null) {
                write("<"); write(PARAMETERS_ATTRIBUTE); write(">");
                write(MapHelper.parameterMapToXML(responseParameterMap, "parameter"));
                write("</"); write(PARAMETERS_ATTRIBUTE); write(">");
            }
            if (durationRequested) {
                write("<"); write(DURATION_ATTRIBUTE); write(" ");
                writeXMLAttribute("val", String.valueOf((System.currentTimeMillis() - durationStart)), false);
                write("/>");
            }
            write("</");
            write(RESPONSE_ATTRIBUTE);
            write(">");
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private void writeXMLAttribute (String name, String value, boolean withSpaceAfter) throws Exception {
        final String eLabel = "STROMAResponseWriter.writeXMLAttribute: ";
        try {
            write(name);
            write("=\"");
            write(StringEscapeUtils.escapeXml(value));
            write("\"");
            if (withSpaceAfter) write(" ");
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private void write (String toWrite) throws Exception {
        final String eLabel = "STROMAResponseWriter.write: ";
        try {
            outputStream.write(toWrite.getBytes());
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public void writeOrchestrationFault (boolean writeOrchestrationFault) {
        this.writeOrchestrationFault = writeOrchestrationFault;
    }

    public void setOmitPrefix (boolean omitPrefix) {
        this.omitPrefix = omitPrefix;
    }

    public void setOmitSuffix (boolean omitSuffix) {
        this.omitSuffix = omitSuffix;
    }

    private boolean omitPrefix = false;
    private boolean omitSuffix = false;
    private String serviceName;
    private String transactionData;
    private String serviceAgent;
    private OutputStream outputStream;
    private boolean isXML = false;
    private boolean responseInFlight = false;
    private boolean done = false;
    private boolean writeOrchestrationFault = false;
    private Map<String, List<String>> responseParameterMap = null;
    private boolean durationRequested = false;
    private long durationStart;
    private final static String ERROR_CODE_ATTRIBUTE                        = "error-code";
    private final static String SERVICE_ATTRIBUTE                           = "service";
    private final static String DATA_ATTRIBUTE                              = "data";
    private final static String RESPONSES_ATTRIBUTE                         = "responses";
    public final static String RESPONSE_ATTRIBUTE                           = "response";
    public final static String ORCHESTRATION_FAULT_ENCOUNTERED_ATTRIBUTE    = "orchestration-fault";
    private final static String PARAMETERS_ATTRIBUTE                        = "parameters";
    private final static String DURATION_ATTRIBUTE                          = "duration";
    private final static String TRANSACTION_DATA_ATTRIBUTE                  = "transaction-data";
    
}
