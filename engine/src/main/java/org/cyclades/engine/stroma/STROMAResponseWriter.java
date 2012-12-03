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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLOutputFactory;
import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jettison.badgerfish.BadgerFishXMLStreamWriter;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.util.MapHelper;
import org.cyclades.xml.XXMLStreamWriter;
import org.json.JSONObject;

public class STROMAResponseWriter {

    public STROMAResponseWriter (String serviceName, NyxletSession nyxletSession)  throws Exception {
        final String eLabel = "STROMAResponseWriter.STROMAResponseWriter: ";
        try {
            this.outputStream = nyxletSession.getOutputStream();
            this.action = nyxletSession.getActionString();
            this.transactionData = nyxletSession.getTransactionDataString();
            this.serviceName = serviceName;
            this.rawResponseRequested = nyxletSession.rawResponseRequested();
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

    // not thread safe
    public STROMAResponseWriter addResponseParameter(String key, String value) throws Exception {
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
    } // end of addResponseParameter(...)

    /**
     * This method is called to initiate a response for the service. It is assumed that if you
     * are calling this method there are no errors to be reported.
     *
     * pre conditions:
     * - No other method has been called on this object
     * post conditions:
     * - No other method will be called after this one (except for done)
     * - The method "commit" must be called when done writing the response
     *
     * Valid response formats (written to the OutputStream returned):
     *
     * XML: Any valid XML
     * JSON: A valid JSON Object (not an array) comprised of any valid JSON sub components
     *
     * For errors, simply call "writeErrorResponse"
     *
     * @return OutputStream
     * @throws Exception
     */
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

    /**
     * This method is called to initiate a response for the service. It is assumed that if you
     * are calling this method there are no errors to be reported.
     *
     * pre conditions:
     * - No other method has been called on this object
     * post conditions:
     * - No other method will be called after this one (except for done)
     * - The method "commit" must be called when done writing the response
     *
     * For errors, simply call "writeErrorResponse"
     *
     * @return XXMLStreamWriter
     * @throws Exception
     */
    public XXMLStreamWriter getXMLStreamWriter () throws Exception {
        final String eLabel = "STROMAResponseWriter.getXMLStreamWriter: ";
        try {
            checkAndSetResponseInFlight();
            if (isXML) {
                final XMLOutputFactory f = XMLOutputFactory.newInstance();
                f.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.TRUE);
                xxmlStreamWriter = new XXMLStreamWriter(f.createXMLStreamWriter(outputStream));
                writeXMLResponsePreMeta(null, 0);
            } else {
                isJSONStreamWriter = true;
                xxmlStreamWriter = new XXMLStreamWriter(new BadgerFishXMLStreamWriter(new OutputStreamWriter(outputStream, "UTF-8")));
                writeJSONResponsePreMeta(null, 0);
                xxmlStreamWriter.writeStartElement("root");
            }
            xxmlStreamWriter.suppressStartDocument(true);
            return xxmlStreamWriter;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    /**
     * Completes the response after getOutputStream is called. This method will also make sure
     * that a correct "empty" response format is generated if nothing is written. It may be a
     * good idea to always call this method when a STROMA response is desired, best done in the
     * finally clause of the action handler.
     *
     * pre conditions:
     * EITHER:
     *  - getOutputStream is called
     *  - All work on the returned OutputStream is done
     *  - The data written to the OutputStream must comprise a valid String representation of the designated meta type
     *      (sessionDelegate.getResponseMetaTypeEnum())
     * OR
     *  - getOutputStream has NOT been called
     *  - An empty response is desired
     *
     * post conditions:
     * - This Object cannot be used again...as the response has been committed.
     *
     * NOTE: If there is no response "in flight", we will assume an empty response is desired. That value will be the
     * following:
     *
     * JSON: "{}"
     * XML: "" (i.e. nothing for XML, it's a bit more forgiving)
     *
     * @throws Exception
     */
    public void done () throws Exception {
        final String eLabel = "STROMAResponseWriter.done: ";
        try {
            if (done) return;
            if (!responseInFlight) {
                // Create a complete well formatted empty response
                if (isXML) {
                    writeResponse("");
                } else {
                    writeResponse("{}");
                }
            } else {
                if (xxmlStreamWriter != null) {
                    if (isJSONStreamWriter) xxmlStreamWriter.writeEndElement();
                    xxmlStreamWriter.writeEndDocument();
                    xxmlStreamWriter.close();
                }
                // Wrap up a written response
                if (isXML) {
                    writeXMLResponsePostMeta();
                } else {
                    writeJSONResponsePostMeta();
                }
                done = true;
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    /**
     * Write the response as a String
     *
     * pre conditions:
     * - No other method has been called on this Object
     * - The "content" parameter must be a valid String representation of the designated meta type
     *  (sessionDelegate.getResponseMetaTypeEnum())
     * post conditions:
     * - This Object cannot be used again...as the response has been committed.
     *
     * @param content
     * @throws Exception
     */
    public void writeResponse (String content) throws Exception {
        writeAsString(content, 0);
        done = true;
    }

    /**
     * Write out the response to an error. The "errorMessage" parameter can simply
     * be a message, i.e. it does not have to be well formed meta data of XML or JSON
     *
     * pre conditions:
     * - No other method has been called on this Object
     * post conditions:
     * - This Object cannot be used again...as the response has been committed.
     *
     * @param errorCode
     * @param errorMessage
     * @throws Exception
     */
    public void writeErrorResponse (int errorCode, String errorMessage) throws Exception {
        writeAsString(errorMessage, errorCode);
        done = true;
    }


    /**
     * Write the complete response from this String passed in.
     *
     * The form of the String should be the following:
     * JSON: A JSON Object "{...}"
     * XML: Any valid XML
     *
     * If "error" is larger than 0, the content will simply be treated as the error message
     *
     * @param content
     * @param error
     * @throws Exception
     */
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
        final String eLabel = "STROMAResponseWriter.writeJSONResponsePreMeta: ";
        try {
            if (rawResponseRequested) return;
            write("{\"");
            // Error needs to be first (optimal and predictable error peeking)
            write(ERROR_CODE_ATTRIBUTE);
            if (error < 1) {
                write("\":\"0\",\"");
            } else {
                write("\":\"");
                write(String.valueOf(error));
                write("\",\"");
                write(ERROR_MESSAGE_ATTRIBUTE);
                write("\":");
                write(JSONObject.quote(content));
                write(",\"");
            }
            write(SERVICE_ATTRIBUTE);
            write("\":\"");
            write(serviceName);
            write("\"");
            // Do not print out this field if null
            if (action != null) {
                write(",\"");
                write(ACTION_ATTRIBUTE);
                write("\":\"");
                write(action);
                write("\"");
            }
            // Do not print out this field if null
            if (transactionData != null) {
                write(",\"");
                write(TRANSACTION_DATA_ATTRIBUTE);
                write("\":");
                write(JSONObject.quote(transactionData));
            }
            if (error < 1) {
                write(",\"");
                write(DATA_ATTRIBUTE);
                write("\":");
                // XXX - content would go here
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private void writeJSONResponsePostMeta () throws Exception {
        final String eLabel = "STROMAResponseWriter.writeJSONResponsePostMeta: ";
        try {
            if (rawResponseRequested) return;
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
        final String eLabel = "STROMAResponseWriter.writeXMLResponsePreMeta: ";
        try {
            if (rawResponseRequested) return;
            write("<");
            write(RESPONSE_ATTRIBUTE);
            write(" ");
            writeXMLAttribute(SERVICE_ATTRIBUTE, serviceName, true);
            if (action != null) {
                writeXMLAttribute(ACTION_ATTRIBUTE, action, true);
            }
            if (transactionData != null) {
                writeXMLAttribute(TRANSACTION_DATA_ATTRIBUTE, StringEscapeUtils.escapeXml(transactionData), true);
            }
            if (error < 1) {
                writeXMLAttribute(ERROR_CODE_ATTRIBUTE, "0", false);
            } else {
                writeXMLAttribute(ERROR_MESSAGE_ATTRIBUTE, StringEscapeUtils.escapeXml(content), true);
                writeXMLAttribute(ERROR_CODE_ATTRIBUTE, String.valueOf(error), false);
            }
            write(">");
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private void writeXMLResponsePostMeta () throws Exception {
        final String eLabel = "STROMAResponseWriter.writeXMLResponsePostMeta: ";
        try {
            if (rawResponseRequested) return;
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

    private String serviceName;
    private String action;
    private String transactionData;
    private OutputStream outputStream;
    private boolean isXML = false;
    private boolean responseInFlight = false;
    private boolean done = false;
    private boolean rawResponseRequested = false;
    private boolean durationRequested = false;
    private long durationStart;
    private XXMLStreamWriter xxmlStreamWriter = null;
    private boolean isJSONStreamWriter = false;
    private Map<String, List<String>> responseParameterMap = null;
    private final static String ERROR_CODE_ATTRIBUTE        = "error-code";
    private final static String ERROR_MESSAGE_ATTRIBUTE     = "error-message";
    private final static String SERVICE_ATTRIBUTE           = "service";
    private final static String ACTION_ATTRIBUTE            = "action";
    private final static String DATA_ATTRIBUTE              = "data";
    private final static String RESPONSE_ATTRIBUTE          = "response";
    private final static String TRANSACTION_DATA_ATTRIBUTE  = "transaction-data";
    private final static String PARAMETERS_ATTRIBUTE        = "parameters";
    private final static String DURATION_ATTRIBUTE          = "duration";
}
