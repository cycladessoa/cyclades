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

import java.util.Map;
import java.util.List;
import org.cyclades.engine.ResponseCodeEnum;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.exception.CycladesException;
import org.cyclades.engine.nyxlet.templates.stroma.STROMANyxlet;
import org.cyclades.engine.stroma.STROMAResponseWriter;
import org.cyclades.engine.validator.FieldValidators;

public abstract class ActionHandler {

    public ActionHandler (STROMANyxlet parentNyxlet) {
        this.parentNyxlet = parentNyxlet;
    }

    public abstract void handle (NyxletSession nyxletSession, Map<String, List<String>> parameters, STROMAResponseWriter writer) throws Exception;

    /**
     * Override this to provide logic that will be called when
     * this ActionHandler is initialized by the framework.
     */
    public void init () throws Exception {
    }

    /**
     * Override this to provide logic that will be called when
     * this ActionHandler is destroyed by the framework.
     */
    public void destroy () throws Exception {
    }

    /**
     * Evaluates a parameter as a boolean value, true is indicated by the following algorithm:
     *
     * 1.) The parameter exists and has no value (empty string)
     * 2.) The parameter exists and has a value of "true", case insensitive
     *
     * @param key The target key
     * @param parameters The parameters to use
     * @param requireValue If true, an empty value for this parameter will evaluate to "false"
     * @return true if evaluated as such, false otherwise
     */
    public static boolean parameterAsBoolean (String key, Map<String, List<String>> parameters, boolean requireValue) {
        return parameters.containsKey(key) && ((parameters.get(key).get(0).isEmpty() && !requireValue) || parameters.get(key).get(0).equalsIgnoreCase("true"));
    }

    public String[] getRequiredParameterKeys () throws Exception {
        return null;
    }

    public void handleException (NyxletSession nyxletSession, STROMAResponseWriter stromaResponseWriter,
            Exception exception) throws Exception {
        handleException (nyxletSession, stromaResponseWriter, null, exception, true);
    }

    public void handleException (NyxletSession nyxletSession, STROMAResponseWriter stromaResponseWriter,
            String prefix, Exception exception) throws Exception {
        handleException (nyxletSession, stromaResponseWriter, prefix, exception, true);
    }

    public void handleException (NyxletSession nyxletSession, STROMAResponseWriter stromaResponseWriter,
            String prefix, Exception exception, boolean raiseOrchestrationFault) throws Exception {
        prefix = (prefix == null) ? "" : prefix;
        parentNyxlet.logError(prefix + parentNyxlet.getName() + " " + exception);
        if (raiseOrchestrationFault) nyxletSession.raiseOrchestrationFault(prefix + exception);
        stromaResponseWriter.writeErrorResponse((exception instanceof CycladesException) ?
                ((CycladesException)exception).getCode() : ResponseCodeEnum.GENERAL_ERROR.getCode(), prefix + exception);
    }

    /**
     * Subclasses should override this method to return a valid health check status.
     *
     * @return true means this is a healthy ActionHandler
     * @throws Exception
     */
    public boolean isHealthy () throws Exception {
        return true;
    }

    /**
     * Subclasses should override this method to return true if this ActionHandler
     * is designed to read non STROMA conforming data from the input stream. This mechanism
     * allows folks to "bend the rules" and bypass STROMA behavior. An example of this would
     * be if this ActionHandler would read and process binary data from the InputStream. Overriding
     * this method to return true will not parse the incoming data to look for parameters.
     *
     * XXX - WARNING: Please make sure you know what you are doing when overriding this
     *  method...it may render this entry point as non STROMA compliant.
     *
     * @return true ignore parsing the incoming stream for STROMA parameters
     * @throws Exception
     */
    public boolean ignoreSTROMAParameters () {
        return false;
    }

    public STROMANyxlet getParentNyxlet () {
        return parentNyxlet;
    }

    public FieldValidators getFieldValidators () {
        return fieldValidators;
    }

    protected final STROMANyxlet parentNyxlet;
    protected final FieldValidators fieldValidators = new FieldValidators();

}
