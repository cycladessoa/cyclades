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

import org.cyclades.annotations.AHandler;
import org.cyclades.engine.nyxlet.templates.stroma.STROMANyxlet;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.nyxlet.templates.stroma.actionhandler.ActionHandler;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.cyclades.engine.stroma.STROMAResponseWriter;
import org.cyclades.engine.validator.ParameterHasIntegerValue;
import javax.xml.stream.XMLStreamWriter;

/**
 * This action handler helps validate the health check mechanism is working. The idea here is
 * to specify a number (from 1 to 5, inclusive) which will be the times the healthcheck of this
 * ActionHandler will fail before automatically reporting healthy again. This is a good way to
 * verify your deployments soundly report errors. Please be sure to also see the health check
 * script provided in the bin directory of the source ditribution.
 */
@AHandler("mockhealth")
public class HelloWorldMockHealthActionHandler extends ActionHandler {

    public HelloWorldMockHealthActionHandler (STROMANyxlet parentNyxlet) {
        super(parentNyxlet);
    }

    @Override
    public void init () throws Exception {
        getFieldValidators().add(new ParameterHasIntegerValue (PARAMETER).setMin(1).setMax(5));
    }

    @Override
    public void handle (NyxletSession nyxletSession, Map<String, List<String>> baseParameters, STROMAResponseWriter stromaResponseWriter) throws Exception {
        final String eLabel = "HelloWorldMockHealthActionHandler.handle: ";
        try {
            countDown.set(Integer.parseInt(baseParameters.get(PARAMETER).get(0)));
            StringBuilder message = new StringBuilder(PARAMETER).append(" has been set to: ");
            message.append(countDown);
            XMLStreamWriter streamWriter = stromaResponseWriter.getXMLStreamWriter();
            streamWriter.writeStartElement("message");
            streamWriter.writeCharacters(message.toString());
            streamWriter.writeEndElement();
        } catch (Exception e) {
            getParentNyxlet().logStackTrace(e);
            handleException(nyxletSession, stromaResponseWriter, eLabel, e);
        } finally {
            stromaResponseWriter.done();
        }
    }

    @Override
    public boolean isHealthy () throws Exception {
        int currentValue = countDown.get();
        if (currentValue < 1) return true;
        countDown.compareAndSet(currentValue, currentValue - 1);
        getParentNyxlet().logError("Countdown to healthy nyxlet (num healthchecks left): " + currentValue);
        return false;
    }

    private AtomicInteger countDown = new AtomicInteger(0);
    private static final String PARAMETER = "unhealthy-count-down";

}
