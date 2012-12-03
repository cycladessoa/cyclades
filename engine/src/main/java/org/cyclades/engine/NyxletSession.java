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
package org.cyclades.engine;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.log4j.Logger;
import org.cyclades.engine.adapter.HttpServletRequestAdapter;
import org.cyclades.engine.adapter.HttpServletResponseAdapter;
import org.cyclades.engine.auth.api.AuthDelegate;
import org.cyclades.engine.exception.AuthException;
import org.cyclades.engine.logging.LoggingEnum;
import org.cyclades.engine.nyxlet.templates.xstroma.OrchestrationTypeEnum;
import org.cyclades.engine.util.MapHelper;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

public class NyxletSession {

    static Logger logger = Logger.getLogger(NyxletSession.class);

    public NyxletSession (HttpServletRequest request, HttpServletResponse response,
                          InputStream inputStream,    OutputStream outputStream) {
        this.request      = request;
        this.response     = response;
        this.inputStream  = inputStream;
        this.outputStream = outputStream;
    }

    public NyxletSession (Map<String, List<String>> requestParameters, InputStream is, OutputStream os) {
        this.request = new HttpServletRequestAdapter(new HashMap<String, String>(), MapHelper.arrayParameterMapFromParameterMap(requestParameters),
                new HashMap<String, Object>(), is);
        this.response = new HttpServletResponseAdapter(os);
        this.inputStream = is;
        this.outputStream = os;
    }

    private void loadDataObject () throws Exception {
        final String eLabel = "NyxletSession.loadDataObject: ";
        try {
            String meta = this.request.getParameter(DATA_PARAMETER);
            if (meta == null && inputStream != null) meta = new String(ByteStreams.toByteArray(inputStream));
            if (meta != null && !meta.isEmpty()) this.dataObject = this.getMetaTypeEnum().createObjectFromMeta(meta);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    } // end of loadDataObject()

    /*****************************************************/
    /**** IMPORTANT: DO NOT USE the following HTTP    ****/
    /**** specific methods UNLESS the Nyxlet you are  ****/
    /**** calling any of these from is intended as a  ****/
    /**** NON STROMA, servlet functionality type of   ****/
    /**** service.                                    ****/
    /*****************************************************/
    public HttpServletRequest getHttpServletRequest () {
        return this.request;
    }

    public HttpServletResponse getHttpServletResponse () {
        return this.response;
    }
    /*****************************************************/
    /*****************************************************/
    /*****************************************************/

    public String getParameter (String name) {
        return request.getParameter(name);
    }

    public Map<String, List<String>> getParameterMap () {
        Map<String, List<String>> parameterMap = Maps.newHashMap();
        @SuppressWarnings("unchecked")
        Enumeration<String> names = request.getParameterNames();
        String name;
        while (names.hasMoreElements()) {
            name = names.nextElement();
            parameterMap.put(name, Arrays.asList(request.getParameterValues(name)));
        }
        return parameterMap;
    }

    public void setResponseContentType (String type) {
        response.setContentType(type);
    }

    public String getRequestMethod () {
        return request.getMethod();
    }

    public void setResponseStatus (int status) {
        response.setStatus(status);
    }

    public String getRequestPathInfo () {
        return request.getPathInfo();
    }

    public Object getDataObject () throws Exception {
        final String eLabel = "NyxletSession.getDataObject: ";
        try {
            if (this.dataObject == null) {
                this.loadDataObject();
            }
            return this.dataObject;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public void setDataObject (Object dataObject) throws Exception {
        this.dataObject = dataObject;
        this.metaTypeEnum = MetaTypeEnum.detectMetaTypeEnum(dataObject);
    }

    /**
     * Does the HttpServletRequest contain the "data" parameter?
     *
     * XXX - It is safer to simply call "getDataObject" and check if it is null or not. There
     * are instances where the core framework uses this, but for the most part, Nyxlets should
     * avoid it.
     *
     * @return true if data parameter exists, false otherwise
     */
    public boolean hasData () {
        return (this.request.getParameter(DATA_PARAMETER) != null);
    }

    public MetaTypeEnum getMetaTypeEnum () throws Exception {
        final String eLabel = "NyxletSession.getMetaTypeEnum: ";
        try {
            if (this.metaTypeEnum == null) {
                String type = this.request.getParameter(DATA_TYPE_PARAMETER);
                if (type != null) {
                    this.metaTypeEnum = Enum.valueOf(MetaTypeEnum.class, type.toUpperCase());
                } else {
                    this.metaTypeEnum = MetaTypeEnum.JSON;
                }
            }
            return this.metaTypeEnum;
        } catch (Exception e) {
            throw new Exception(eLabel + "Invalid data type: " +  e);
        }
    }

    public MetaTypeEnum getResponseMetaTypeEnum () throws Exception {
        final String eLabel = "NyxletSession.getResponseMetaTypeEnum: ";
        try {
            if (this.responseMetaTypeEnum == null) {
                String type = this.request.getParameter(RESPONSE_DATA_TYPE_PARAMETER);
                if (type != null) {
                    this.responseMetaTypeEnum = Enum.valueOf(MetaTypeEnum.class, type.toUpperCase());
                } else {
                    this.responseMetaTypeEnum = getMetaTypeEnum();
                }
            }
            return this.responseMetaTypeEnum;
        } catch (Exception e) {
            throw new Exception(eLabel + "Invalid data type: " +  e);
        }
    }

    public String getDataContentType () throws Exception {
        final String eLabel = "NyxletSession.getDataContentType: ";
        try {
            return this.getResponseMetaTypeEnum().getContentType();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public Object getActionObject () throws Exception {
        final String eLabel = "NyxletSession.getActionObject: ";
        try {
            if (this.actionObject == null) {
                this.actionObject = this.request.getParameter(ACTION_PARAMETER);
            }
            return this.actionObject;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public String getActionString () throws Exception {
        final String eLabel = "NyxletSession.getActionString: ";
        try {
            Object action = this.getActionObject();
            return ((action == null) ? null : action.toString());
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public void setActionObject (Object actionObject) {
        this.actionObject = actionObject;
    }

    public Object getTransactionDataObject () throws Exception {
        final String eLabel = "NyxletSession.getTransactionDataObject: ";
        try {
            if (this.transactionDataObject == null) {
                this.transactionDataObject = this.request.getParameter(TRANSACTION_DATA_PARAMETER);
            }
            return this.transactionDataObject;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public String getTransactionDataString () throws Exception {
        final String eLabel = "NyxletSession.getTransactionDataString: ";
        try {
            Object transdata = this.getTransactionDataObject();
            return ((transdata == null) ? null : transdata.toString());
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public void setTransactionDataObject (Object transactionDataObject) {
        this.transactionDataObject = transactionDataObject;
    }

    public boolean rawResponseRequested () throws Exception {
        final String eLabel = "NyxletSession.rawResponseRequested: ";
        try {
            if (this.rawResponse == null) {
                String rawResponseString = this.request.getParameter(RAW_RESPONSE_PARAMETER);
                if (rawResponseString != null && (rawResponseString.isEmpty() || rawResponseString.equalsIgnoreCase("true")) ) {
                    this.rawResponse = true;
                } else {
                    this.rawResponse = false;
                }
            }
            return this.rawResponse;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public void setRawResponseRequested (boolean rawResponse) {
        this.rawResponse = rawResponse;
    }

    public LoggingEnum getUserLoggingLevel () throws Exception {
        final String eLabel = "NyxletSession.getUserLoggingLevel: ";
        try {
            if (this.userLoggingLevel == null) {
                String level = this.request.getParameter(LOG_LEVEL_PARAMETER);
                this.userLoggingLevel = (level == null) ? null : Enum.valueOf(LoggingEnum.class, level.toUpperCase());
            }
            return this.userLoggingLevel;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public void setUserLoggingLevel (LoggingEnum userLoggingLevel) {
        this.userLoggingLevel = userLoggingLevel;
    }

    public boolean durationRequested () throws Exception {
        final String eLabel = "NyxletSession.durationRequested: ";
        try {
            if (this.durationRequested == null) {
                String rawResponseString = this.request.getParameter(DURATION_PARAMETER);
                if (rawResponseString != null && (rawResponseString.isEmpty() || rawResponseString.equalsIgnoreCase("true")) ) {
                    this.durationRequested = true;
                } else {
                    this.durationRequested = false;
                }
            }
            return this.durationRequested;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public void setDurationRequested (Boolean durationRequested) {
        this.durationRequested = durationRequested;
    }

    @SuppressWarnings("unchecked")
    public void putMapChannelObject (Object key, Object value) throws Exception {
        final String eLabel = "NyxletSession.putMapChannelObject: ";
        try {
            this.mapChannel.put(key, value);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public Object getMapChannelObject (Object key) throws Exception {
        final String eLabel = "NyxletSession.getMapChannelObject: ";
        try {
            return this.mapChannel.get(key);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public boolean containsMapChannelKey (Object key) throws Exception {
        final String eLabel = "NyxletSession.containsMapChannelKey: ";
        try {
            return this.mapChannel.containsKey(key);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public void setMapChannel (Map<Object, Object> map) {
        this.mapChannel = map;
    }

    public Map<Object, Object> getMapChannel () {
        return this.mapChannel;
    }

    public void raiseOrchestrationFault (String orchestrationFaultMessage) {
        this.orchestrationFault = true;
        this.orchestrationFaultMessage = orchestrationFaultMessage;
    }

    public void clearOrchestrationFault () {
        this.orchestrationFault = false;
        this.orchestrationFaultMessage = "";
    }

    public boolean orchestrationFaultRaised () {
        return this.orchestrationFault;
    }

    public String getOrchestrationFaultMessage () {
        return (this.orchestrationFaultMessage == null) ? "" : this.orchestrationFaultMessage;
    }

    public Object auth (Map authAttributeMap, Object authMetaObject, boolean force) throws Exception {
        final String eLabel = "NyxletSession.auth: ";
        try {
            if (this.authDelegateObject == null || force) {
                final AuthDelegate authDelegate = CycladesEngine.getEngineContext().getAuthDelegate();
                if( authDelegate == null) {
                    throw new AuthException("No AutheDelegate defined!", ResponseCodeEnum.NO_AUTH_DELEGATE.getCode());
                }
                this.authDelegateObject = authDelegate.auth(authAttributeMap, authMetaObject);
            }
            return this.authDelegateObject;

        } catch (AuthException e) {
            this.authDelegateObject = null;
            throw e;

        } catch (Exception e) {
            this.authDelegateObject = null;
            throw new Exception(eLabel + e, e);
        }
    }

    public void setAuthDelegateObject (Object authDelegateObject) {
        this.authDelegateObject = authDelegateObject;
    }

    public Object getAuthDelegateObject () {
        return this.authDelegateObject;
    }

    public void setPortData (byte[] port) {
        this.portData = port;
    }

    public byte[] getPortData () {
        return this.portData;
    }

    public NyxletSession setInputStream (InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }

    public InputStream getInputStream () {
        return inputStream;
    }

    public NyxletSession setOutputStream (OutputStream outputStream) {
        this.outputStream = outputStream;
        return this;
    }

    public OutputStream getOutputStream () {
        return outputStream;
    }

    public boolean chainsForward () {
        return chainsForward;
    }

    public void setChainsForward (boolean chainsForward) {
        this.chainsForward = chainsForward;
    }

    public boolean isLast () {
        return isLastBoolean;
    }

    public void setIsLast (boolean isLastBoolean) {
        this.isLastBoolean = isLastBoolean;
    }

    public boolean isRRDRequest () {
        return isRRDRequest;
    }

    public void setRRDRequest (boolean isRRDRequest) {
        this.isRRDRequest = isRRDRequest;
    }

    public void setOrchestrationTypeEnum (OrchestrationTypeEnum orchestrationTypeEnum) {
        this.orchestrationTypeEnum = orchestrationTypeEnum;
    }

    public OrchestrationTypeEnum getOrchestrationTypeEnum () {
        return orchestrationTypeEnum;
    }

    public long getDuration () {
        return System.currentTimeMillis() - durationStart;
    }

    public long getDurationStart () {
        return durationStart;
    }

    public void resetDuration () {
        durationStart = System.currentTimeMillis();
    }

    private HttpServletRequest request;
    private HttpServletResponse response;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Object dataObject = null;
    private Object actionObject = null;
    private Object transactionDataObject = null;
    private Boolean rawResponse = null;
    private LoggingEnum userLoggingLevel = null;
    private MetaTypeEnum metaTypeEnum = null;
    private MetaTypeEnum responseMetaTypeEnum = null;
    private Map<Object, Object> mapChannel = new Hashtable<Object, Object>();
    private byte[] portData = null;
    private boolean orchestrationFault = false;
    private String orchestrationFaultMessage;
    private Object authDelegateObject;
    private boolean chainsForward = false;
    private boolean isLastBoolean = true;
    private boolean isRRDRequest = false;
    private OrchestrationTypeEnum orchestrationTypeEnum = OrchestrationTypeEnum.NONE;
    private long durationStart = System.currentTimeMillis();
    private Boolean durationRequested = null;

    // Defines
    public static final String DATA_PARAMETER               = "data";
    public static final String DATA_TYPE_PARAMETER          = "data-type";
    public static final String RESPONSE_DATA_TYPE_PARAMETER = "data-out";
    public static final String ACTION_PARAMETER             = "action";
    public static final String TRANSACTION_DATA_PARAMETER   = "transaction-data";
    public static final String LOG_LEVEL_PARAMETER          = "log-level";
    public static final String RAW_RESPONSE_PARAMETER       = "raw-response";
    public static final String DURATION_PARAMETER           = "duration";
}
