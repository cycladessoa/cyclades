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
package org.cyclades.engine;

import java.util.Map;
import java.util.List;
import javax.xml.transform.dom.DOMSource;
import org.cyclades.engine.util.GenericXMLObject;
import org.cyclades.engine.util.MapHelper;
import org.cyclades.xml.parser.api.XMLGeneratedObject;
import org.json.JSONObject;
import org.json.JSONArray;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public enum MetaTypeEnum {

    JSON("application/json") {
        public Object createObjectFromMeta (String meta) throws Exception {
            final String eLabel = "MetaTypeEnum.createObjectFromMeta(JSON): ";
            try {
                return new JSONObject(meta);
            } catch (Exception e) {
                throw new Exception(eLabel + e);
            }
        }

        public String createMetaFromObject (Object object) throws Exception {
            final String eLabel = "MetaTypeEnum.createMetaFromObject(JSON): ";
            try {
                return ((JSONObject)object).toString();
            } catch (Exception e) {
                throw new Exception(eLabel + e);
            }
        }

        @SuppressWarnings("unchecked")
        public Map<String, String> getMapFromMeta (Object metaObject, String[] totalParameters) throws Exception {
            return MapHelper.mapFromMeta((JSONObject)metaObject, totalParameters);
        }

        public Map<String, List<String>> getParameterMapFromMeta (Object metaObject) throws Exception {
            return MapHelper.parameterMapFromMetaObject((JSONArray)metaObject);
        }

        public boolean isMatch (Object object) {
            return (object instanceof JSONObject);
        }

        public  String getMetaStringFromMap (Map<String, String> map) throws Exception {
            return MapHelper.mapToJSON(map, false);
        }

        public  String getMetaStringFromParameterMap (Map<String, List<String>> map) throws Exception {
            return MapHelper.parameterMapToJSON(map);
        }
    },

    XML("application/xml; charset=UTF-8") {
        public Object createObjectFromMeta (String meta) throws Exception {
            final String eLabel = "MetaTypeEnum.createObjectFromMeta(XML): ";
            try {
                return (new GenericXMLObject(meta)).getRootElement();
            } catch (Exception e) {
                throw new Exception(eLabel + e);
            }
        }

        public String createMetaFromObject (Object object) throws Exception {
            final String eLabel = "MetaTypeEnum.createMetaFromObject(JSON): ";
            try {
                return XMLGeneratedObject.toXMLString(new DOMSource((Node)object), true);
            } catch (Exception e) {
                throw new Exception(eLabel + e);
            }
        }

        @SuppressWarnings("unchecked")
        public Map<String, String> getMapFromMeta (Object metaObject, String[] totalParameters) throws Exception {
            return MapHelper.mapFromMeta((Node)metaObject, totalParameters);
        }

        public Map<String, List<String>> getParameterMapFromMeta (Object metaObject) throws Exception {
            return MapHelper.parameterMapFromMetaObject((NodeList)metaObject);
        }

        public boolean isMatch (Object object) {
            return (object instanceof Node);
        }

        public  String getMetaStringFromMap (Map<String, String> map) throws Exception {
            return MapHelper.mapToXML(map, "map", false);
        }

        public  String getMetaStringFromParameterMap (Map<String, List<String>> map) throws Exception {
            return MapHelper.parameterMapToXML(map, "parameter");
        }
    };

    public abstract Object createObjectFromMeta (String meta) throws Exception;

    public abstract String createMetaFromObject (Object object) throws Exception;

    public abstract Map<String, String> getMapFromMeta (Object metaObject, String[] totalParameters) throws Exception;

    public abstract Map<String, List<String>> getParameterMapFromMeta (Object metaObject) throws Exception;

    public abstract String getMetaStringFromMap (Map<String, String> map) throws Exception;

    public abstract String getMetaStringFromParameterMap (Map<String, List<String>> map) throws Exception;

    public abstract boolean isMatch (Object object);

    MetaTypeEnum (String contentType) {
        this.contentType = contentType;
    }

    public String getContentType () {
        return contentType;
    }

    public static MetaTypeEnum detectMetaTypeEnum (Object object) throws Exception {
        final String eLabel = "MetaTypeEnum.detectMetaTypeEnum: ";
        try {
            if (object == null) throw new Exception("null paramter value passed in");
            for (MetaTypeEnum targetEnum : values()) {
                if (targetEnum.isMatch(object)) return targetEnum;
            }
            throw new Exception("No match was found for object:[" + object + "]");
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private String contentType;
}
