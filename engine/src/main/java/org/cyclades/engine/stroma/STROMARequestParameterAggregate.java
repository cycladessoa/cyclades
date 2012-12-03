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

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.util.MapHelper;
import org.w3c.dom.Node;
import org.cyclades.xml.comparitor.XMLComparitor;
import org.json.JSONObject;

public class STROMARequestParameterAggregate {

    public static Map<String, List<String>> getParameters (NyxletSession nyxletSession, String[] requiredParameters) throws Exception {
        return getParameters(nyxletSession, nyxletSession.getParameterMap(), requiredParameters);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, List<String>> getParameters (NyxletSession nyxletSession, Map<String,
            List<String>> secondaryParameters, String[] requiredParameters) throws Exception {
        final String eLabel = "STROMARequestParameterAggregate.getParameters: ";
        try {
            if (nyxletSession.getDataObject() != null) {
                Object baseParameters = getBaseParametersFromMeta(nyxletSession);
                return MapHelper.immutableParameterMap(MapHelper.mergeMaps(
                        baseParameters == null ? new HashMap() : nyxletSession.getMetaTypeEnum().getParameterMapFromMeta(baseParameters),
                                secondaryParameters, requiredParameters));
            } else {
                return MapHelper.immutableParameterMap(MapHelper.mergeMaps(secondaryParameters, null, requiredParameters));
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private static Object getBaseParametersFromMeta (NyxletSession nyxletSession) throws Exception {
        final String eLabel = "STROMARequestParameterAggregate.getBaseParametersFromMeta: ";
        try {
            Object baseParameters = null;
            switch (nyxletSession.getMetaTypeEnum()) {
            case XML:
                @SuppressWarnings("unchecked")
                Vector<Node> results = XMLComparitor.getMatchingChildNodes((Node)nyxletSession.getDataObject(), BASE_PARAMETERS);
                if (results.size() > 0) baseParameters = results.firstElement().getChildNodes();
                break;
            case JSON:
                try {
                    baseParameters = ((JSONObject)nyxletSession.getDataObject()).getJSONArray(BASE_PARAMETERS);
                } catch (Exception e) {
                    baseParameters = null;
                }
                break;
            default:
                throw new Exception("Invalid meta type");
            }
            return baseParameters;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private final static String BASE_PARAMETERS = "parameters";
}
