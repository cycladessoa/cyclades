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
package org.cyclades.engine.nyxlet.templates.stroma;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import org.cyclades.engine.ResponseCodeEnum;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.api.Nyxlet;
import org.cyclades.engine.exception.CycladesException;
import org.cyclades.engine.logging.LogWriter;
import org.cyclades.engine.logging.LogWriterInterface;
import org.cyclades.engine.logging.LoggingEnum;
import org.cyclades.engine.logging.LoggingDelegate;
import org.cyclades.engine.nyxlet.templates.stroma.actionhandler.ActionHandler;
import org.cyclades.engine.nyxlet.templates.stroma.actionhandler.ListActionsHandler;
import org.cyclades.engine.stroma.STROMARequestParameterAggregate;
import org.cyclades.engine.stroma.STROMAResponseWriter;
import org.cyclades.engine.util.MapHelper;
import org.cyclades.engine.util.XProperties;
import org.cyclades.io.ResourceRequestUtils;
import org.cyclades.engine.validator.ValidationFaultElement;

public abstract class STROMANyxlet extends Nyxlet {

    public STROMANyxlet () throws Exception {
        super();
    }

    @Override
    public byte[] process (NyxletSession nyxletSession) throws CycladesException {
        try {
            nyxletSession.setResponseContentType(nyxletSession.getDataContentType());
            this.processInternal(nyxletSession);
            return null;
        } catch (Exception ex) {
            // Something went terribly wrong...this is the final last resort...
            logError(ex.getMessage() + " " + ex.toString());
            return this.handleError(ResponseCodeEnum.GENERAL_ERROR.getCode(), ex);
        }
    }

    protected void processInternal (NyxletSession nyxletSession) throws CycladesException {
        final String eLabel = "STROMANyxlet.processInternal: ";
        String action = null;
        try {
            if (!this.isActive()) throw new CycladesException("This service is inactive", ResponseCodeEnum.SERVICE_INACTIVE.getCode());
            Map<String, List<String>> RRDParameterMap = null;
            if (nyxletSession.isRRDRequest()) {
                RRDParameterMap = getRRDURIParameterMap(nyxletSession);
            }
            // First...detect if this is a request that ignores STROMA input, with an action handler specified as a query parameter or a RRD URI part.
            // The query parameter has precedence.
            ActionHandler handler = getActionHandler(nyxletSession.getActionString());
            if (handler == null && RRDParameterMap != null) {
                if (RRDParameterMap.containsKey(NyxletSession.ACTION_PARAMETER)) handler = getActionHandler(RRDParameterMap.get(NyxletSession.ACTION_PARAMETER).get(0));
            }
            // Second...load the parameters based on this detection
            Map<String, List<String>> mergedBaseParameters;
            if (handler != null && handler.ignoreSTROMAParameters()) {
                mergedBaseParameters = baseParametersNonSTROMA(nyxletSession, RRDParameterMap);
            } else {
                mergedBaseParameters = baseParameters(nyxletSession, RRDParameterMap);
            }
            // Third...move forward and process now that all configuration scenarios have been covered
            action = nyxletSession.getActionString();
            if (action == null) {
                throw new Exception("No action has been specified.");
            }
            handler = getActionHandler(action);
            if (handler == null) throw new Exception("Unknown action specified: " + action);
            if (handler.getFieldValidators().size() > 0) processFieldValidators(handler.getFieldValidators().validate(nyxletSession, mergedBaseParameters));
            handler.handle(nyxletSession, mergedBaseParameters, new STROMAResponseWriter(getName(), nyxletSession));
        } catch (Exception e) {
            logError(eLabel + e);
            try {
                // Try and handle the error gracefully
                nyxletSession.raiseOrchestrationFault(eLabel + e);
                new STROMAResponseWriter(this.getName(), nyxletSession).writeErrorResponse((e instanceof CycladesException) ? ((CycladesException)e).getCode() : ResponseCodeEnum.GENERAL_ERROR.getCode(),
                        eLabel + e);
            } catch (Exception ex) {
                // Last resort....
                nyxletSession.setResponseContentType("text/html");
                StringBuffer sb = new StringBuffer("<HTML><TITLE>STROMANyxlet</TITLE><BODY>");
                sb.append("FAILURE: " + e);
                sb.append("</BODY></HTML>");
                try {
                    nyxletSession.getOutputStream().write(sb.toString().getBytes());
                } catch (Exception exx) {
                    logError(eLabel + exx);
                }
            }
        }
    } // end of process(...)

    private ActionHandler getActionHandler (String actionHandlerString) {
        if (actionHandlerString == null) return null;
        Object handler = this.actionHandlers.get(actionHandlerString);
        return ((handler == null) ? null : (ActionHandler) handler);
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<String>> baseParameters (NyxletSession nyxletSession, Map<String, List<String>> additionalMap) throws Exception {
        final String eLabel = "STROMANyxlet.baseParameters: ";
        try {
            Map<String, List<String>> mergedParameters;
            if (additionalMap != null) {
                mergedParameters = STROMARequestParameterAggregate.getParameters(nyxletSession,
                        MapHelper.mergeMaps(nyxletSession.getParameterMap(), additionalMap, null),
                        null);
            } else {
                mergedParameters = STROMARequestParameterAggregate.getParameters(nyxletSession, null);
            }
            setReservedBaseParameters(nyxletSession, mergedParameters);
            return mergedParameters;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<String>> baseParametersNonSTROMA (NyxletSession nyxletSession, Map<String, List<String>> additionalMap) throws Exception {
        final String eLabel = "STROMANyxlet.baseParametersNonSTROMA: ";
        try {
            Map<String, List<String>> mergedParameters;
            mergedParameters = MapHelper.immutableParameterMap(MapHelper.mergeMaps(nyxletSession.getParameterMap(), additionalMap, null));
            setReservedBaseParameters(nyxletSession, mergedParameters);
            return mergedParameters;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private void setReservedBaseParameters (NyxletSession nyxletSession, Map<String, List<String>> mergedParameters) throws Exception {
        final String eLabel = "STROMANyxlet.setReservedBaseParameters: ";
        try {
            if (mergedParameters.containsKey(NyxletSession.ACTION_PARAMETER)) nyxletSession.setActionObject(mergedParameters.get(NyxletSession.ACTION_PARAMETER).get(0));
            if (mergedParameters.containsKey(NyxletSession.TRANSACTION_DATA_PARAMETER)) nyxletSession.setTransactionDataObject(mergedParameters.get(NyxletSession.TRANSACTION_DATA_PARAMETER).get(0));
            if (mergedParameters.containsKey(NyxletSession.LOG_LEVEL_PARAMETER)) nyxletSession.setUserLoggingLevel(Enum.valueOf(LoggingEnum.class, mergedParameters.get(NyxletSession.LOG_LEVEL_PARAMETER).get(0).toUpperCase()));
            if (mergedParameters.containsKey(NyxletSession.DURATION_PARAMETER)) {
                nyxletSession.setDurationRequested((mergedParameters.get(NyxletSession.DURATION_PARAMETER).get(0).isEmpty()) ? true : mergedParameters.get(NyxletSession.DURATION_PARAMETER).get(0).equalsIgnoreCase("true"));
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    /**
     * This method is responsible for initializing the action handlers. It is called from within init() which
     * will pass the appropriate actionHandlerEntrySet. If you wish to implement your own custom action handler
     * registration mechanism then you can override this method.
     *
     * @param actionHandlerEntrySet
     * @throws Exception
     */
    public void loadActionHandlers (Set<Entry<Object, Object>> actionHandlerEntrySet) throws Exception {
        final Class[] parameterArray = {STROMANyxlet.class};
        final Object[] objectArray = {this};
        Map<String, ActionHandler> classToObjectMap = new HashMap<String, ActionHandler>();
        for (Entry actionHandler : actionHandlerEntrySet) {
            ActionHandler handler = classToObjectMap.get(actionHandler.getValue().toString());
            if (handler == null) {
                handler = (ActionHandler)this.getClass().getClassLoader().loadClass(actionHandler.getValue().toString()).
                            getConstructor(parameterArray).newInstance(objectArray);
                handler.init();
                classToObjectMap.put(actionHandler.getValue().toString(), handler);
            }
            actionHandlers.put(actionHandler.getKey().toString(), handler);
        }
    }

    protected void loadActionHandlers (String propertyString) throws Exception {
        ByteArrayInputStream bis = null;
        try {
            bis = new ByteArrayInputStream(propertyString.getBytes());
            Properties props = new Properties();
            props.load(bis);
            actionHandlers.put("listactions", new ListActionsHandler(this));
            this.loadActionHandlers(props.entrySet());
        } finally {
            try { bis.close(); } catch (Exception ignore) {}
        }
    }

    private void loadLogWriters (String propertyString) throws Exception {
        final String eLabel = "STROMANyxlet.loadLogWriters: ";
        ByteArrayInputStream bis = null;
        try {
            bis = new ByteArrayInputStream(propertyString.getBytes());
            Properties props = new Properties();
            props.load(bis);
            Enumeration keySet = props.keys();
            String writerName;
            String[] writerFields;
            LogWriter writer;
            while (keySet.hasMoreElements()) {
                writerName = (String)keySet.nextElement();
                writerFields = ((String)props.get(writerName)).split("\\|");
                if (writerFields.length != 3) {
                    throw new Exception("Invalid LogWriter entry, parse error: " + writerName);
                }
                writer = new LogWriter(getEngineContext().getCanonicalEngineDirectoryPath(writerFields[0]), writerFields[1], writerFields[2]);
                this.logWriterMap.put(writerName, writer);
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        } finally {
            try {
                bis.close();
            } catch (Exception e) {
            }
        }
    }

    private void loadLoggingDelegates (String propertyString) throws Exception {
        final String eLabel = "STROMANyxlet.loadLoggingDelegates: ";
        ByteArrayInputStream bis = null;
        try {
            bis = new ByteArrayInputStream(propertyString.getBytes());
            Properties props = new Properties();
            props.load(bis);
            Enumeration keySet = props.keys();
            String loggingDelegateName;
            String[] fields;
            LoggingDelegate loggingDelegate;
            while (keySet.hasMoreElements()) {
                loggingDelegateName = (String)keySet.nextElement();
                fields = ((String)props.get(loggingDelegateName)).split("\\|");
                if (fields.length != 2) {
                    throw new Exception("Invalid LoggingDelegate entry, parse error: " + loggingDelegateName);
                }
                loggingDelegate = new LoggingDelegate(this.getLogWriter(fields[0]),
                        LoggingEnum.valueOf(this.externalProperties.getProperty("LoggingDelegate." + loggingDelegateName + ".logLevel", fields[1]).toUpperCase()));
                this.loggingDelegateMap.put(loggingDelegateName, loggingDelegate);
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        } finally {
            try {
                bis.close();
            } catch (Exception e) {
            }
        }
    }

    public LogWriterInterface getLogWriter (String writerName) throws Exception {
        final String eLabel = "STROMANyxlet.getLogWriter: ";
        try {
            LogWriterInterface writer = this.logWriterMap.get(writerName);
            if (writer == null) {
                throw new Exception("Writer does not exist: " + writerName);
            }
            return writer;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public LoggingDelegate getLoggingDelegate (String loggingDelegateName) throws Exception {
        final String eLabel = "STROMANyxlet.getLoggingDelegate: ";
        try {
            LoggingDelegate loggingDelegate = this.loggingDelegateMap.get(loggingDelegateName);
            if (loggingDelegate == null) {
                throw new Exception("LoggingDelegate does not exist: " + loggingDelegateName);
            }
            return loggingDelegate;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private void closeLogWriters () throws Exception {
        final String eLabel = "STROMANyxlet.closeLogWriters: ";
        try {
            for (String key : this.logWriterMap.keySet()) {
                this.logWriterMap.get(key).close();
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    protected void destroyActionHandlers () throws Exception {
        for (String key : this.actionHandlers.keySet()) {
            try {
                ((ActionHandler)this.actionHandlers.get(key)).destroy();
            } catch (Exception ex) {
                logError(ex.getMessage() + " " + " Continuing to destroy remaining ActionHandlers");
            }
        }
    } // end of destroyActionHandlers(...)

    @Override
    public void init () throws CycladesException {
        final String eLabel = "STROMANyxlet.init: ";
        try {
            // External Properties
            String aValue = this.getAttribute(EXTERNAL_PROPERTIES);
            if (aValue != null && !aValue.isEmpty()) {
                this.loadExternalProperties(this.getAttribute(EXTERNAL_PROPERTIES));
            } else {
                loadExternalPropertiesDefault();
            }

            // LogWriters
            aValue = this.getAttribute(LOG_WRITERS);
            if (aValue == null) {
                logInfo(eLabel + "No LogWriters defined.");
            } else {
                this.loadLogWriters(aValue);
            }

            // LoggingDelegates
            aValue = this.getAttribute(LOGGING_DELEGATES);
            if (aValue == null) {
                logInfo(eLabel + "No LoggingDelegates defined.");
            } else {
                this.loadLoggingDelegates(aValue);
            }

            // action handlers
            aValue = this.getAttribute(ACTION_HANDLERS);
            if (aValue == null) {
                logInfo(eLabel + "No ActionHandlers defined.");
            } else {
                this.loadActionHandlers(aValue);
            }
        } catch (Exception ex) {
            logError(eLabel + ex);
            throw new CycladesException(ex.getMessage(), ex);
        }
    } // end of init()

    @Override
    public void destroy () throws CycladesException {
        try {
            this.destroyActionHandlers();
        } catch (Exception ex) {
            logError(ex.toString());
        }
        try {
            this.closeLogWriters();
        } catch (Exception ex) {
            logError(ex.toString());
        }
    } // end of destroy()

    public XProperties getExternalProperties () {
        return this.externalProperties;
    }

    protected void loadExternalProperties (String path) throws Exception {
        loadExternalProperties(path.split("[,]"));
    }

    protected void loadExternalPropertiesDefault () throws Exception {
        String[] nyxletDirectories = getEngineContext().getNyxletDirectories();
        String[] propertyPaths = new String[nyxletDirectories.length];
        for (int i = 0; i < nyxletDirectories.length; i++) {
            propertyPaths[i] = nyxletDirectories[i] + "/" + getName() + ".properties";
        }
        loadExternalProperties(propertyPaths);
    }

    private void loadExternalProperties (String[] propPaths) throws Exception {
        InputStream is = null;
        for (String propPath : propPaths) {
            try {
                is = ResourceRequestUtils.getInputStream(propPath.trim(), null);
                this.externalProperties.load(is);
                // XXX - This is flexible for linking properties files...however someone can introduce an infinite loop if they do something foolish.
                if (externalProperties.containsKey(SUPPLEMENTAL_PROPERTIES)) loadExternalProperties(getEngineContext().getCanonicalEngineDirectoryPath((String)externalProperties.remove(SUPPLEMENTAL_PROPERTIES)));
                logInfo("This external properties file was successfully loaded: " + propPath);
                is.close();
            } catch (Exception ex) {
                logError(ex.toString());
            } finally {
                try { is.close();  } catch (Exception ignore ) { }
            }
        }
    }

    public boolean auth (NyxletSession sessionDelegate, Map authAttributes, Object authMetaObject) throws Exception {
        return (sessionDelegate.auth(authAttributes, authMetaObject, false) != null);
    }

    public void logDebug (String logMessage, Throwable... t) {
        log(LoggingEnum.DEBUG, logMessage, t);
    }

    public void logInfo (String logMessage, Throwable... t) {
        log(LoggingEnum.INFO, logMessage, t);
    }

    public void logWarn (String logMessage, Throwable... t) {
        log(LoggingEnum.WARN, logMessage, t);
    }

    public void logError (String logMessage, Throwable... t) {
        log(LoggingEnum.ERROR, logMessage, t);
    }

    public void logNotify (LoggingEnum level, String logMessage, Throwable... t) {
        // Log to normal channel
        try { log(level, logMessage, t); } catch (Exception e) {}
        // Send notification
        try {
            getEngineContext().sendNotification(level, (t.length > 0) ? logMessage + " " +  t[0] : logMessage);
        } catch (Exception e) {
            logger.log(LoggingEnum.ERROR.getLog4jLevel(), "Failed to send notification, we need to look into this: " + e);
            // Do we want to print out a stack trace?
        }
    }

    public void logStackTrace (Throwable t) { logStackTrace(t, true); }

    public void logStackTrace (Throwable t, boolean newLine) {
        StringBuilder sb = new StringBuilder(t.toString());
        sb.append(" > STACK_TRACE_ELEMENTS > [");
        StackTraceElement[] stes = t.getStackTrace();
        for (int i = 0; i < stes.length; i++) {
            if (newLine) sb.append("\n");
            sb.append("\"").append(stes[i].toString()).append("\"");
             sb.append((i < stes.length - 1) ? "," : (newLine) ? "\n" : "");
        }
        sb.append("]");
        log(LoggingEnum.ERROR, sb.toString());
    }

    private void log (LoggingEnum loggingEnum, String message, Throwable... t) {
        try {
            getLoggingDelegate(DEFAULT_LOGGING_DELEGATE).log(loggingEnum, "", (t.length > 0) ? message + " " +  t[0] : message);
        } catch (Exception e) {
            if (t.length > 0) {
                logger.log(loggingEnum.getLog4jLevel(), message, t[0]);
            } else {
                logger.log(loggingEnum.getLog4jLevel(), message);
            }
        }
    }

    public boolean shouldLog (LoggingEnum logLevel) throws Exception { return shouldLog(logLevel, null); }

    public boolean shouldLog (LoggingEnum logLevel, LoggingEnum userLogLevel) throws Exception {
        final String eLabel = "STROMANyxlet.shouldLog: ";
        try {
            LoggingDelegate loggingDelegate = getLoggingDelegate(DEFAULT_LOGGING_DELEGATE);
            if (loggingDelegate != null) return loggingDelegate.shouldLog(logLevel, userLogLevel);
            return LoggingEnum.shouldLogLog4jLevel(logger.getLevel(), logLevel.getLog4jLevel(), (userLogLevel != null) ? userLogLevel.getLog4jLevel() : null);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private void processFieldValidators(List<ValidationFaultElement> validationFaultElements) throws CycladesException {
        if (validationFaultElements.size() < 1) return;
        throw new CycladesException(ValidationFaultElement.toString("VALIDATION_FAULT_ELEMENTS", validationFaultElements),
                ResponseCodeEnum.REQUEST_VALIDATION_FAULT.getCode());
    }

    /**
     * Loop through all of the action handlers and test each one to see if it is healthy. Return
     * false if any are encountered that are not healthy. Subclasses can override this method and
     * do something meaningful with the return value. See the helloword example Nyxlet for a further
     * example.
     */
    @Override
    public boolean isHealthy () throws CycladesException {
        try {
            for (String key : actionHandlers.keySet()) {
                if (!((ActionHandler)actionHandlers.get(key)).isHealthy()) throw new Exception("Unhealthy action handler: " + key);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public String[] listActionHandlers () {
        return  actionHandlers.keySet().toArray(new String[0]);
    }

    protected Map<String, Object> actionHandlers = new HashMap<String, Object>();
    private Map<String, LogWriterInterface> logWriterMap = new HashMap<String, LogWriterInterface>();
    private Map<String, LoggingDelegate> loggingDelegateMap = new HashMap<String, LoggingDelegate>();
    private XProperties externalProperties = new XProperties();
    public static final String ACTION_HANDLERS          = "actionHandlers";
    public static final String LOG_WRITERS              = "logWriters";
    public static final String LOGGING_DELEGATES        = "loggingDelegates";
    public static final String EXTERNAL_PROPERTIES      = "externalProperties";
    public static final String DEFAULT_LOGGING_DELEGATE = "general";
    public static final String SUPPLEMENTAL_PROPERTIES  = "supplementalProperties";
}
