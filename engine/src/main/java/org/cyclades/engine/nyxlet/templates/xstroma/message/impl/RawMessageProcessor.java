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
import org.cyclades.engine.MetaTypeEnum;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.nyxlet.templates.xstroma.message.api.MessageProcessor;
import org.cyclades.engine.stroma.STROMARequestHelper;
import org.cyclades.engine.util.MapHelper;
import org.json.JSONObject;

public class RawMessageProcessor implements MessageProcessor {

    public RawMessageProcessor (JSONObject jsonObject) throws Exception {
        serviceName = jsonObject.getString(SERVICE_NAME);
        // Preserve both, response_input_parameter and input_parameter for backwards compatibility
        inputParameterName = (jsonObject.has(RESPONSE_INPUT_PARAMETER)) ? jsonObject.getString(RESPONSE_INPUT_PARAMETER) : jsonObject.getString(INPUT_PARAMETER);
        metaTypeEnum = (jsonObject.has(DATA_TYPE)) ? MetaTypeEnum.valueOf(jsonObject.getString(DATA_TYPE).toUpperCase()) : MetaTypeEnum.JSON;
        useMapChannel = (jsonObject.has(USE_MAP_CHANNEL)) ? jsonObject.getString(USE_MAP_CHANNEL).equalsIgnoreCase("true") : false;
        stringEncoding = (jsonObject.has(BINARY_TO_STRING_ENCODING)) ? jsonObject.getString(BINARY_TO_STRING_ENCODING) : "UTF-8";
        if (jsonObject.has(PARMAMETERS)) fixedParameters = MapHelper.parameterMapFromMetaObject(jsonObject.getJSONArray(PARMAMETERS));
    }
    
    /**
     * Process a String message
     * 
     * @param message The String message to process
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
    @Override
    public void process (String message) throws Exception {
        if (useMapChannel) {
            processMapChannel(message.getBytes());
        } else {
            processString(message);
        }
    }

    /**
     * Process a String message and get a String response
     * 
     * @param message The String message to process
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
    @Override
    public String processAndGetResponse (String message) throws Exception {
        if (useMapChannel) {
            return new String(processAndGetResponseMapChannel(message.getBytes()), stringEncoding);
        } else {
            return new String(processAndGetResponseString(message), stringEncoding);
        }
    }
    
    /**
     * Process a String message
     * 
     * @param message The byte[] message to process
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
    @Override
    public void process (byte[] message) throws Exception {
        if (useMapChannel) {
            processMapChannel(message);
        } else {
            processString(new String(message, stringEncoding));
        }
    }

    /**
     * Process a String message and get a byte[] response
     * 
     * @param message The String message to process
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
    @Override
    public byte[] processAndGetResponse (byte[] message) throws Exception {
        if (useMapChannel) {
            return processAndGetResponseMapChannel(message);
        } else {
            return processAndGetResponseString(new String(message, stringEncoding));
        }
        
    }
    
    private void processString (String message) throws Exception {
        STROMARequestHelper.request(requestParameters(message), metaTypeEnum, serviceName, null);
    }
    
    private byte[] processAndGetResponseString (String message) throws Exception {            
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        STROMARequestHelper.requestStreamedResponse(requestParameters(message), metaTypeEnum, serviceName, null, baos);
        return baos.toByteArray();
    }
    
    private void processMapChannel (byte[] message) throws Exception {           
        Map<String, List<String>> requestParameters = requestParameters();
        requestParameters.put(NyxletSession.DATA_TYPE_PARAMETER, new ArrayList<String>(Arrays.asList(metaTypeEnum.name())));
        STROMARequestHelper.requestSetAndGetMapChannel(requestParameters, serviceName, inputParameterName, message, "discard");
    }
    
    private byte[] processAndGetResponseMapChannel (byte[] message) throws Exception {            
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Map<String, List<String>> requestParameters = requestParameters();
        requestParameters.put(NyxletSession.DATA_TYPE_PARAMETER, new ArrayList<String>(Arrays.asList(metaTypeEnum.name())));
        STROMARequestHelper.requestSetMapChannel(requestParameters, serviceName, inputParameterName, message, baos);
        return baos.toByteArray();
    }
    
    protected Map<String, List<String>> requestParameters () {
        return requestParameters(null);
    }

    private Map<String, List<String>> requestParameters (String message) {
        Map<String, List<String>> requestParameters = new HashMap<String, List<String>>();
        requestParameters.putAll(fixedParameters);
        if (message != null) requestParameters.put(inputParameterName, new ArrayList<String>(Arrays.asList(message)));
        return requestParameters;
    }

    protected final String serviceName;
    protected final String inputParameterName;
    protected final MetaTypeEnum metaTypeEnum;
    protected final boolean useMapChannel;
    protected final String stringEncoding;
    protected Map<String, List<String>> fixedParameters = new HashMap<String, List<String>>();
    protected final static String SERVICE_NAME                = "service_name";
    protected final static String RESPONSE_INPUT_PARAMETER    = "response_input_parameter";
    protected final static String INPUT_PARAMETER             = "input_parameter";
    protected final static String DATA_TYPE                   = "data_type";
    protected final static String PARMAMETERS                 = "parameters";
    protected final static String USE_MAP_CHANNEL             = "use_map_channel";
    protected final static String BINARY_TO_STRING_ENCODING   = "encoding";
    
}
