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

import java.io.ByteArrayOutputStream;
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
import org.cyclades.engine.stroma.STROMARequestHelper;
import org.cyclades.engine.stroma.STROMAResponse;
import org.cyclades.engine.stroma.STROMAResponseWriter;
import org.cyclades.engine.stroma.STROMAServiceRequest;
import org.cyclades.engine.stroma.xstroma.VirtualizedSTROMARequest;

@AHandler("orchestratesayhello")
public class HelloWorldOrchestrateSTROMAActionHandler extends ActionHandler {

    public HelloWorldOrchestrateSTROMAActionHandler (STROMANyxlet parentNyxlet) {
        super(parentNyxlet);
    }

    public void handle (NyxletSession nyxletSession, Map<String, List<String>> baseParameters, STROMAResponseWriter stromaResponseWriter) throws Exception {
        final String eLabel = "HelloWorldOrchestrateSTROMAActionHandler.handle: ";
        try {
            int requestCount = 1;
            /************************************************************************************************************/
            /** Example of a Nyxlet request local to this engine. This strategy returns a "STROMAResponse" object with **/
            /** parsed meta data. This is a convenient strategy for a typical local request.                           **/
            /** NOTE: You can NOT use the "raw-response" parameter with this strategy                                  **/
            /************************************************************************************************************/
            {
                Map<String, List<String>> serviceRequestParameters = new HashMap<String, List<String>>();
                serviceRequestParameters.put("action", new ArrayList<String>(Arrays.asList("sayhello")));
                serviceRequestParameters.put("data-type", new ArrayList<String>(Arrays.asList("json")));
                serviceRequestParameters.put("name", new ArrayList<String>(Arrays.asList("local")));
                STROMAResponse stromaResponse = new STROMAServiceRequest("helloworld", MetaTypeEnum.JSON, serviceRequestParameters, null).execute();
                stromaResponseWriter.addResponseParameter("request:" + requestCount++, stromaResponse.getParameters().get("message-as-parameter").get(0));
            }
            /************************************************************************************************************/
            /** Example of a Nyxlet local or remote brokered request. This strategy returns a "STROMAResponse" object  **/
            /** with parsed meta data. This is a convenient strategy for a typical small to mid size request.          **/
            /** Use the VirtualSTROMARequest to make a Nyxlet request to a Nyxlet residing anywhere in a Cyclades      **/
            /** federation, abstracting away all the location specific details of the transaction.                     **/
            /** NOTE: "raw-response" is not supported on brokered requests. Also, make sure your "registry" file   is  **/
            /** set up properly if remote access is desired.                                                           **/
            /************************************************************************************************************/
            {
                Map<String, List<String>> xstromaParameters = new HashMap<String, List<String>>();
                xstromaParameters.put("connection-timeout", new ArrayList<String>(Arrays.asList("0")));
                xstromaParameters.put("read-timeout", new ArrayList<String>(Arrays.asList("0")));
                Map<String, List<String>> serviceRequestParameters = new HashMap<String, List<String>>();
                serviceRequestParameters.put("action", new ArrayList<String>(Arrays.asList("sayhello")));
                serviceRequestParameters.put("name", new ArrayList<String>(Arrays.asList("local or remote")));
                STROMAResponse stromaResponse = new VirtualizedSTROMARequest(xstromaParameters,  MetaTypeEnum.JSON, "helloworld", serviceRequestParameters, null).execute();
                stromaResponseWriter.addResponseParameter("request:" + requestCount++, stromaResponse.getParameters().get("message-as-parameter").get(0));
            }
            /************************************************************************************************************/
            /** Example of a Nyxlet request local to this engine. This strategy is optimized for a service local to    **/
            /** this engine. Note the MapChannel being used to avoid data serialization. This will most likely be the  **/
            /** best way to call another service if it a.) resides on the same engine and b.) supports MapChannel      **/
            /** optimized access when chaining forward, or otherwise and c.) performance similar to a method request   **/
            /** is desired.                                                                                            **/
            /************************************************************************************************************/
            {
                // NOTE: See STROMARequestHelper for examples on how to implement orchestration directly, without the helper wrappers
                Map<String, List<String>> serviceRequestParameters = new HashMap<String, List<String>>();
                serviceRequestParameters.put("action", new ArrayList<String>(Arrays.asList("sayhellochainable")));
                serviceRequestParameters.put("data-type", new ArrayList<String>(Arrays.asList("json")));
                Object ret = STROMARequestHelper.requestSetAndGetMapChannel(serviceRequestParameters, "helloworld", "string", "local via MapChannel", "string");
                stromaResponseWriter.addResponseParameter("request:" + requestCount++, ret.toString());
            }
            /************************************************************************************************************/
            /** Example of a Nyxlet request local to this engine. This strategy writes directly to the OutputStream    **/
            /** passed in. This is a good strategy to use when the request returns large data and streaming is         **/
            /** preferred.                                                                                             **/
            /** NOTE: You CAN use the "raw-response" parameter with this strategy. This will return the raw response   **/
            /** of the Nyxlet without the STROMA wrapper.                                                              **/
            /************************************************************************************************************/
            {
                // NOTE: See STROMARequestHelper for examples on how to implement orchestration directly, without the helper wrappers
                Map<String, List<String>> serviceRequestParameters = new HashMap<String, List<String>>();
                serviceRequestParameters.put("action", new ArrayList<String>(Arrays.asList("sayhello")));
                serviceRequestParameters.put("name", new ArrayList<String>(Arrays.asList("STREAMING LOCAL STROMA!")));
                //serviceRequestParameters.put("raw-response", new ArrayList<String>(Arrays.asList("")));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                STROMARequestHelper.requestStreamedResponse(serviceRequestParameters, MetaTypeEnum.JSON, "helloworld", null, baos);
                stromaResponseWriter.addResponseParameter("request:" + requestCount++, new String(baos.toByteArray(), "UTF-8"));
            }
            /************************************************************************************************************/
            /** Example of a Nyxlet local or remote brokered request. This strategy writes directly to the             **/
            /** OutputStream passed in. Use the VirtualSTROMARequest to make a Nyxlet request to a Nyxlet residing     **/
            /** anywhere in a Cyclades federation, abstracting away all the location specific details of the           **/
            /** transaction.                                                                                           **/
            /** This is a good strategy to use when the request returns large data and streaming is preferred.         **/
            /** NOTE: The entire X-STROMA response will be written to the OutputStream. The X-STROMA response          **/
            /** will contain only one nested STROMA response, this will be the response of the Nyxlet requested.       **/
            /** "raw-response" is not supported on brokered requests. Also, make sure your "registry" file   is        **/
            /** set up properly if remote access is desired.                                                           **/
            /************************************************************************************************************/
            {
                Map<String, List<String>> xstromaParameters = new HashMap<String, List<String>>();
                xstromaParameters.put("connection-timeout", new ArrayList<String>(Arrays.asList("0")));
                xstromaParameters.put("read-timeout", new ArrayList<String>(Arrays.asList("0")));
                Map<String, List<String>> serviceRequestParameters = new HashMap<String, List<String>>();
                serviceRequestParameters.put("action", new ArrayList<String>(Arrays.asList("sayhello")));
                serviceRequestParameters.put("name", new ArrayList<String>(Arrays.asList("STREAMING LOCAL OR REMOTE X-STROMA!")));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                new VirtualizedSTROMARequest(xstromaParameters, MetaTypeEnum.JSON, "helloworld", serviceRequestParameters, null).execute(baos);
                stromaResponseWriter.addResponseParameter("request:" + requestCount++, new String(baos.toByteArray(), "UTF-8"));
            }
        } catch (Exception e) {
            getParentNyxlet().logStackTrace(e);
            handleException(nyxletSession, stromaResponseWriter, eLabel, e);
        } finally {
            stromaResponseWriter.done();
        }
    }
}
