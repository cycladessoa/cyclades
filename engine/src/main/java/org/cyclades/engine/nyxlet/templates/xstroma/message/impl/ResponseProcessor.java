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
package org.cyclades.engine.nyxlet.templates.xstroma.message.impl;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.stroma.STROMARequestHelper;
import org.json.JSONObject;

public class ResponseProcessor extends RawMessageProcessor {

    public ResponseProcessor (JSONObject jsonObject) throws Exception {
        super(jsonObject);
        requestInputParameterName = (jsonObject.has(REQUEST_INPUT_PARAMETER)) ? jsonObject.getString(REQUEST_INPUT_PARAMETER) : null;
    }
    
    /**
     * Process a X-STROMA response
     * 
     * @param responseMessage The response String message to process
     * @param requestMessage The original request message that correlates to the response
     * 
     * Parameter submission algorithm:
     * - If "useMapChannel" is false, this message value will be submitted as 
     *  a String as a conventional request parameter
     * - If "useMapChannel" is true, this message value will be submitted as 
     *  a byte array (byte[]) in the Map Channel
     *  
     * In both scenarios, the key will be equal to the value of the input parameter
     * specified
     * 
     * @throws Exception
     */
    public void process (String responseMessage, String requestMessage) throws Exception {
        if (useMapChannel) {
            processMapChannel(responseMessage.getBytes(), requestMessage.getBytes());
        } else {
            processString(responseMessage, requestMessage);
        }
    }

    /**
     * Process a X-STROMA response
     * 
     * @param responseMessage The String message to process
     * @param requestMessage The original request message that correlates to the response
     * 
     * Parameter submission algorithm:
     * - If "useMapChannel" is false, this message value will be submitted as 
     *  a String as a conventional request parameter
     * - If "useMapChannel" is true, this message value will be submitted as 
     *  a byte array (byte[]) in the Map Channel
     *  
     * In both scenarios, the key will be equal to the value of the input parameter
     * specified
     * 
     * @return The String value of the response of the service
     * @throws Exception
     */
    public String processAndGetResponse (String responseMessage, String requestMessage) throws Exception {
        if (useMapChannel) {
            return new String(processAndGetResponseMapChannel(responseMessage.getBytes(), requestMessage.getBytes()), stringEncoding);
        } else {
            return new String(processAndGetResponseString(responseMessage, requestMessage), stringEncoding);
        }
    }
    
    /**
     * Process a X-STROMA response
     * 
     * @param responseMessage The byte[] response message to process
     * @param requestMessage The original request message that correlates to the response
     * 
     * Parameter submission algorithm:
     * - If "useMapChannel" is false, this message value will be submitted as 
     *  a String as a conventional request parameter
     * - If "useMapChannel" is true, this message value will be submitted as 
     *  a byte array (byte[]) in the Map Channel
     *  
     * In both scenarios, the key will be equal to the value of the input parameter
     * specified
     * 
     * @throws Exception
     */
    public void process (byte[] responseMessage, byte[] requestMessage) throws Exception {
        if (useMapChannel) {
            processMapChannel(responseMessage, requestMessage);
        } else {
            processString(new String(responseMessage, stringEncoding), new String(requestMessage, stringEncoding));
        }
    }

    /**
     * Process a X-STROMA response
     * 
     * @param responseMessage The byte[] response message to process
     * @param requestMessage The original request message that correlates to the response
     * 
     * Parameter submission algorithm:
     * - If "useMapChannel" is false, this message value will be submitted as 
     *  a String as a conventional request parameter
     * - If "useMapChannel" is true, this message value will be submitted as 
     *  a byte array (byte[]) in the Map Channel
     *  
     * In both scenarios, the key will be equal to the value of the input parameter
     * specified
     * 
     * @return The byte[] value of the response of the service
     * @throws Exception
     */
    public byte[] processAndGetResponse (byte[] responseMessage, byte[] requestMessage) throws Exception {
        if (useMapChannel) {
            return processAndGetResponseMapChannel(responseMessage, requestMessage);
        } else {
            return processAndGetResponseString(new String(responseMessage, stringEncoding), new String(requestMessage, stringEncoding));
        }
        
    }
    
    private void processString (String responseMessage, String requestMessage) throws Exception {
        STROMARequestHelper.request(requestParameters(responseMessage, requestMessage), metaTypeEnum, serviceName, null);
    }
    
    private byte[] processAndGetResponseString (String responseMessage, String requestMessage) throws Exception {            
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        STROMARequestHelper.requestStreamedResponse(requestParameters(responseMessage, requestMessage), metaTypeEnum, serviceName, null, baos);
        return baos.toByteArray();
    }
    
    private void processMapChannel (byte[] responseMessage, byte[] requestMessage) throws Exception {           
        Map<String, List<String>> requestParameters = requestParameters();
        requestParameters.put(NyxletSession.DATA_TYPE_PARAMETER, new ArrayList<String>(Arrays.asList(metaTypeEnum.name())));
        Map<Object, Object> mapChannel = new HashMap<Object, Object>();
        mapChannel.put(inputParameterName, responseMessage);
        if (requestInputParameterName != null) mapChannel.put(requestInputParameterName, requestMessage);
        STROMARequestHelper.requestSetAndGetMapChannel(requestParameters, serviceName, mapChannel);
    }
    
    private byte[] processAndGetResponseMapChannel (byte[] responseMessage, byte[] requestMessage) throws Exception {            
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Map<String, List<String>> requestParameters = requestParameters();
        requestParameters.put(NyxletSession.DATA_TYPE_PARAMETER, new ArrayList<String>(Arrays.asList(metaTypeEnum.name())));
        Map<Object, Object> mapChannel = new HashMap<Object, Object>();
        mapChannel.put(inputParameterName, responseMessage);
        if (requestInputParameterName != null) mapChannel.put(requestInputParameterName, requestMessage);
        STROMARequestHelper.requestSetMapChannel(requestParameters, serviceName, mapChannel, baos);
        return baos.toByteArray();
    }

    private Map<String, List<String>> requestParameters (String responseMessage, String requestMessage) {
        Map<String, List<String>> requestParameters = new HashMap<String, List<String>>();
        requestParameters.putAll(fixedParameters);
        if (responseMessage != null) requestParameters.put(inputParameterName, new ArrayList<String>(Arrays.asList(responseMessage)));
        if (requestMessage != null && requestInputParameterName != null) requestParameters.put(requestInputParameterName, 
                new ArrayList<String>(Arrays.asList(requestMessage)));
        return requestParameters;
    }

    private final String requestInputParameterName;
    private final static String REQUEST_INPUT_PARAMETER     = "request_input_parameter";
    
}
