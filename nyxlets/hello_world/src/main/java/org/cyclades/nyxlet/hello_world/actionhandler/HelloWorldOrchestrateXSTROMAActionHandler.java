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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.cyclades.engine.nyxlet.templates.stroma.STROMANyxlet;
import org.cyclades.engine.MetaTypeEnum;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.nyxlet.templates.stroma.actionhandler.ActionHandler;
import java.util.Map;
import org.cyclades.annotations.AHandler;
import org.cyclades.engine.stroma.xstroma.XSTROMABrokerRequest;
import org.cyclades.engine.stroma.xstroma.XSTROMABrokerResponse;
import org.cyclades.engine.stroma.STROMAResponse;
import org.cyclades.engine.stroma.STROMAResponseWriter;

@AHandler("orchestratesayhellobroker")
public class HelloWorldOrchestrateXSTROMAActionHandler extends ActionHandler {

    public HelloWorldOrchestrateXSTROMAActionHandler (STROMANyxlet parentNyxlet) {
        super(parentNyxlet);
    }

    public void handle (NyxletSession nyxletSession, Map<String, List<String>> baseParameters, STROMAResponseWriter stromaResponseWriter) throws Exception {
        final String eLabel = "HelloWorldOrchestrateXSTROMAActionHandler.handle: ";
        try {
            /************************************************************************************/
            /*** Sample Service Composition Via Broker, Can Be Local Or Remote, Chained etc. ****/
            /************************************************************************************/
            Map<String, List<String>> xstromaParameters = new HashMap<String, List<String>>();
            xstromaParameters.put("connection-timeout", new ArrayList<String>(Arrays.asList("0")));
            xstromaParameters.put("read-timeout", new ArrayList<String>(Arrays.asList("0")));
            // You can specify a target parameter (by setting a "broker-target" request parameter), which enables remote service access...
            if (baseParameters.containsKey("broker-target")) xstromaParameters.put("target", new ArrayList<String>(Arrays.asList(baseParameters.get("broker-target").get(0))));
            XSTROMABrokerRequest request =  new XSTROMABrokerRequest("servicebroker", MetaTypeEnum.JSON, xstromaParameters);
            Map<String, List<String>> serviceRequestParameters;
            for (int i = 1; i <= 10; i++) {
                serviceRequestParameters = new HashMap<String, List<String>>();
                serviceRequestParameters.put("action", new ArrayList<String>(Arrays.asList("sayhello")));
                serviceRequestParameters.put("name", new ArrayList<String>(Arrays.asList("name-" + i)));
                request.addSTROMARequest("helloworld", serviceRequestParameters, null);
            }
            XSTROMABrokerResponse response = request.execute();
            if (response.getErrorCode() != 0) {
                throw new Exception("Service Broker error: " + response.getErrorCode() + " " + response.getErrorMessage());
            }
            int i = 1;
            for (STROMAResponse serviceResponse : response.getResponses()) {
                if (serviceResponse.getErrorCode() != 0) {
                    throw new Exception(serviceResponse.getServiceName() + " " + serviceResponse.getAction() + " " + serviceResponse.getErrorCode() + " " + serviceResponse.getErrorMessage());
                }
                stromaResponseWriter.addResponseParameter("response-" + i++, serviceResponse.getParameters().get("message-as-parameter").get(0));
            }
            /************************************************************************************/
            /************************************************************************************/
            /************************************************************************************/
        } catch (Exception e) {
            getParentNyxlet().logStackTrace(e);
            handleException(nyxletSession, stromaResponseWriter, eLabel, e);
        } finally {
            stromaResponseWriter.done();
        }
    }

}
