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

package org.cyclades.nyxlet.hello_world.actionhandler;

import java.util.List;
import java.util.Map;
import org.cyclades.annotations.AHandler;
import org.cyclades.engine.nyxlet.templates.stroma.STROMANyxlet;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.nyxlet.templates.stroma.actionhandler.ActionHandler;
import org.cyclades.engine.stroma.STROMAResponseWriter;
import org.cyclades.engine.logging.LogWriterInterface;
import org.cyclades.engine.logging.LoggingEnum;
import org.cyclades.engine.logging.LoggingDelegate;

/**
 * This ActionHandler simply writes to a log file and returns an empty response. It is intended
 * to provide examples of how to utilize some simple logging APIs provided by the core framework.
 * Please be sure to also check the documentation.
 */
@AHandler("sayhellologging")
public class HelloWorldLoggingActionHandler extends ActionHandler {

    public HelloWorldLoggingActionHandler (STROMANyxlet parentNyxlet) {
        super(parentNyxlet);
    }

    @Override
    public void handle (NyxletSession nyxletSession, Map<String, List<String>> baseParameters, STROMAResponseWriter stromaResponseWriter) throws Exception {
        final String eLabel = "HelloWorldLoggingActionHandler.handle: ";
        try {
            // Retrieve a LogWriter defined in nyxlet_manifest.xml with the name "general".
            // NOTE: You can also create your own, however if you do you need to make sure to
            // destroy them properly.
            LogWriterInterface logWriter = getParentNyxlet().getLogWriter("general");
            /******************************************************/
            /**   Examples of writing directly to a LogWriter    **/
            /******************************************************/
            logWriter.write("Example of writing directly to the writer with no new lines..not recommended..but possible\n");
            logWriter.writeLine("Example of writing a line directly to the writer");
            logWriter.writeDatedLine("Example of writing a line directly to the writer prepended with a time stamp", " - ");

            /*****************************************************************************************************************/
            /** Examples of using a LoggingDelegate to write to a logWriter...this can be used for logging with levels, and **/
            /**                      automatically adjusting logging levels based on the user                               **/
            /*****************************************************************************************************************/
            // Alternative: Create a LoggingDelegate with the LogWriter retrieved above ("general") and with a log level setting of "WARN":
            // LoggingDelegate loggingDelegate = new LoggingDelegate(logWriter, LoggingEnum.WARN);
            // A cleaner and more flexible way to do this is to create this association in the nyxlet_manifest.xml:
            LoggingDelegate loggingDelegate = getParentNyxlet().getLoggingDelegate("general");
            loggingDelegate.log (LoggingEnum.DEBUG, "", "This SHOULD NOT write to the log");
            loggingDelegate.log (LoggingEnum.WARN, "", "This SHOULD write to the log");
            loggingDelegate.log (LoggingEnum.INFO, nyxletSession.getUserLoggingLevel(), "",
                    "This should only log when the parameter \"log-level\" is INFO or above (DEBUG)");

            /***************************************************************************************/
            /**     Examples of avoiding unnecessary code blocks based on logging levels          **/
            /***************************************************************************************/
            if (loggingDelegate.shouldLog(LoggingEnum.DEBUG)) {
                logWriter.writeLine("This SHOULD NOT write to the log");
            }
            if (loggingDelegate.shouldLog(LoggingEnum.INFO, nyxletSession.getUserLoggingLevel())) {
                logWriter.writeLine("This should only write to the log when the parameter \"log-level\" is INFO or above (i.e. DEBUG)");
            }

            /***************************************************************************************/
            /**     Examples of built in logging methods that output to Nyxlet's general log file **/
            /***************************************************************************************/
            getParentNyxlet().logDebug("Print me when in DEBUG mode");
            getParentNyxlet().logInfo("Print me when in INFO mode");
            getParentNyxlet().logWarn("Print me when in WARN mode");
            getParentNyxlet().logError("Print me when in ERROR mode");
            getParentNyxlet().logStackTrace(new Exception("Print me"));
            getParentNyxlet().logNotify(LoggingEnum.ERROR, "Print me when you would like to notify folks");
        } catch (Exception e) {
            getParentNyxlet().logStackTrace(e);
            handleException(nyxletSession, stromaResponseWriter, eLabel, e);
        } finally {
            stromaResponseWriter.done();
        }
    }

}
