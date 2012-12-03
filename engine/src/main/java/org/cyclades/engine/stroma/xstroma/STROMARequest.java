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
package org.cyclades.engine.stroma.xstroma;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.cyclades.engine.util.MapHelper;

public class STROMARequest {

    public STROMARequest (String serviceName, Map<String, List<String>> params, String data) throws Exception {
        final String eLabel = "STROMARequest.STROMARequest: ";
        try {
            if (params != null) this.parameters = params;
            this.serviceName = serviceName;
            this.data = data;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public String getData() {
        return data;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String toJSONString () throws Exception {
        final String eLabel = "STROMARequest.toRequestString: ";
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("{\"service\":\"").append(serviceName);
            builder.append("\",\"data\":{\"").append(BASE_PARAMETERS).append("\":");
            builder.append(MapHelper.parameterMapToJSON(parameters));
            if (data != null) {
                builder.append(",");
                builder.append(data);
            }
            builder.append("}}");
            return builder.toString();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public String toXMLString () throws Exception {
        final String eLabel = "STROMARequest.toRequestString: ";
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("<request ");
            builder.append("service=\"").append(serviceName);
            builder.append("\"><").append(BASE_PARAMETERS).append(">");
            builder.append(MapHelper.parameterMapToXML(parameters, "parameter"));
            builder.append("</").append(BASE_PARAMETERS).append(">");
            if (data != null) builder.append(data);
            builder.append("</request>");
            return builder.toString();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private final String data;
    private final String serviceName;
    private Map<String, List<String>> parameters = new HashMap<String, List<String>>();
    private static final String BASE_PARAMETERS = "parameters";
}
