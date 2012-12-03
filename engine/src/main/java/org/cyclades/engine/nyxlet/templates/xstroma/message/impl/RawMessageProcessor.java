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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cyclades.engine.MetaTypeEnum;
import org.cyclades.engine.stroma.STROMARequestHelper;
import org.cyclades.engine.util.MapHelper;
import org.json.JSONObject;

public class RawMessageProcessor {

    public RawMessageProcessor (JSONObject jsonObject) throws Exception {
        serviceName = jsonObject.getString(SERVICE_NAME);
        responseInputParameter = jsonObject.getString(RESPONSE_INPUT_PARAMETER);
        metaTypeEnum = (jsonObject.has(META_TYPE)) ? MetaTypeEnum.valueOf(jsonObject.getString(META_TYPE)) : MetaTypeEnum.JSON;
        if (jsonObject.has(PARMAMETERS)) fixedParameters = MapHelper.parameterMapFromMetaObject(jsonObject.getJSONArray(PARMAMETERS));
    }

    public void process (String message) throws Exception {
        final String eLabel = "RawMessageProcessor.process: ";
        try {
            STROMARequestHelper.request(requestParameters(message), metaTypeEnum, serviceName, null);
        } catch (Exception e) {
            throw new Exception(eLabel + e + " [" + message + "]");
        }
    }

    public String processAndGetResponse (String message) throws Exception {
        final String eLabel = "RawMessageProcessor.processAndGetResponse: ";
        try {
            return STROMARequestHelper.requestStreamedResponse(requestParameters(message), metaTypeEnum, serviceName, null);
        } catch (Exception e) {
            throw new Exception(eLabel + e + " [" + message + "]");
        }
    }

    private Map<String, List<String>> requestParameters (String message) {
        Map<String, List<String>> requestParameters = new HashMap<String, List<String>>();
        requestParameters.putAll(fixedParameters);
        requestParameters.put(responseInputParameter, new ArrayList<String>(Arrays.asList(message)));
        return requestParameters;
    }

    private final String serviceName;
    private final String responseInputParameter;
    private final MetaTypeEnum metaTypeEnum;
    private Map<String, List<String>> fixedParameters = new HashMap<String, List<String>>();
    private final static String SERVICE_NAME                = "service_name";
    private final static String RESPONSE_INPUT_PARAMETER    = "response_input_parameter";
    private final static String META_TYPE                   = "meta_type";
    private final static String PARMAMETERS                 = "parameters";

}
