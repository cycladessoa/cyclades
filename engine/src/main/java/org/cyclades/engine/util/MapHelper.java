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
package org.cyclades.engine.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.Vector;
import org.json.JSONObject;
import org.json.JSONArray;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.commons.lang.StringEscapeUtils;
import org.cyclades.xml.comparitor.XMLComparitor;
import org.cyclades.xml.generator.XMLWriter;
import java.util.List;

public class MapHelper {

    public static Map mapFromMeta (Node node, String[] keys) throws Exception {
        final String eLabel = "MapHelper.mapFromMeta(Node, String[]): ";
        try {
            if (keys == null) return mapFromMetaObject(node);
            HashMap<String, String> returnMap = new HashMap<String, String>();
            String temp;
            for (int i = 0; i < keys.length; i++) {
                try {
                    temp = XMLComparitor.getAttribute(node, keys[i]);
                    if (temp == null) continue;
                    returnMap.put(keys[i], temp);
                } catch (Exception e) {
                    // Skip the exception for this
                }
            }
            return returnMap;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public static Map mapFromMeta (Node node) throws Exception {
        return mapFromMeta(node, null);
    }

    public static Map mapFromMetaObject (Node node) throws Exception {
        final String eLabel = "MapHelper.mapFromMetaObject(Node): ";
        try {
            NamedNodeMap nodeMap = node.getAttributes();
            if (nodeMap == null) throw new Exception("Node passed in is not an element!");
            Map<String, String> returnMap = new HashMap<String, String>();
            for (int i = 0; i < nodeMap.getLength(); i++) {
                returnMap.put(nodeMap.item(i).getNodeName(), nodeMap.item(i).getNodeValue());
            }
            return returnMap;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public static Map<String, List<String>> parameterMapFromURI (String URI, int offset, String[] keys) throws Exception {
        final String eLabel = "MapHelper.parameterMapFromURI: ";
        try {
            return parameterMapFromArray(URI.split("[/]"), offset, keys);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public static Map<String, List<String>> parameterMapFromArray (String[] values, int offset, String[] keys) throws Exception {
        final String eLabel = "MapHelper.parameterMapFromArray: ";
        try {
            Map<String, List<String>> listMap = new HashMap<String, List<String>>();
            for (int i = offset; i < values.length; i++) {
                if (i - offset > keys.length - 1) break;
                addParameterToParameterMap(listMap, keys[i - offset], values[i]);
            }
            return listMap;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public static Map<String, List<String>> parameterMapFromMetaObject (NodeList nodeList) throws Exception {
        final String eLabel = "MapHelper.parameterMapFromMetaObject: ";
        try {
            Map<String, List<String>> listMap = new HashMap<String, List<String>>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                addParameterToParameterMap(listMap, XMLComparitor.getAttribute(nodeList.item(i), "name"), XMLComparitor.getAttribute(nodeList.item(i), "value"));
            }
            return listMap;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public static Map<String, List<String>> parameterMapFromMetaObject (JSONArray jsonArray) throws Exception {
        final String eLabel = "MapHelper.parameterMapFromMetaObject: ";
        try {
            Map<String, List<String>> listMap = new HashMap<String, List<String>>();
            JSONObject parameter;
            for (int i = 0; i < jsonArray.length(); i++) {
                parameter = jsonArray.getJSONObject(i);
                addParameterToParameterMap(listMap, parameter.getString("name"), parameter.getString("value"));
            }
            return listMap;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public static Map<String, Map<String, List<String>>> indexedParameterMapFromMetaObject (NodeList nodeList) throws Exception {
        final String eLabel = "MapHelper.indexedParameterMapFromMetaObject: ";
        try {
            Map<String, Map<String, List<String>>> indexedParameterMap = new HashMap<String, Map<String, List<String>>>();
            Node entry;
            NodeList keys;
            Vector<Node> vector;
            for (int i = 0; i < nodeList.getLength(); i++) {
                entry = nodeList.item(i);
                vector = XMLComparitor.getMatchingChildNodes(entry, "parameters");
                if (vector == null || vector.size() < 1) throw new Exception("No parameters declared!");
                Map<String, List<String>> parameterMap = parameterMapFromMetaObject(vector.firstElement().getChildNodes());
                vector = XMLComparitor.getMatchingChildNodes(entry, "keys");
                if (vector == null || vector.size() < 1) throw new Exception("No keys declared!");
                keys = vector.firstElement().getChildNodes();
                for (int j = 0; j < keys.getLength(); j++) indexedParameterMap.put(XMLComparitor.getAttribute(keys.item(j), "value"), parameterMap);
            }
            return indexedParameterMap;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public static Map<String, Map<String, List<String>>> indexedParameterMapFromMetaObject (JSONArray jsonArray) throws Exception {
        final String eLabel = "MapHelper.indexedParameterMapFromMetaObject: ";
        try {
            Map<String, Map<String, List<String>>> indexedParameterMap = new HashMap<String, Map<String, List<String>>>();
            JSONObject entry;
            JSONArray keys;
            for (int i = 0; i < jsonArray.length(); i++) {
                entry = jsonArray.getJSONObject(i);
                Map<String, List<String>> parameterMap = parameterMapFromMetaObject(entry.getJSONArray("parameters"));
                keys = entry.getJSONArray("keys");
                for (int j = 0; j < keys.length(); j++) indexedParameterMap.put(keys.getString(j), parameterMap);
            }
            return indexedParameterMap;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private static void addParameterToParameterMap (Map <String, List<String>> parameterMap, String key, String value) {
        List<String> values = parameterMap.get(key);
        if (values == null) {
            values = new ArrayList<String>();
            parameterMap.put(key, values);
        }
        values.add(value);
    }

    public static Map<String, List<String>> immutableParameterMap (Map<String, List<String>> map) {
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            map.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(map);
    }

    public static Map<String, String[]> arrayParameterMapFromParameterMap (Map<String, List<String>> map) {
        HashMap<String, String[]> parameterMap = new HashMap<String, String[]>();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            parameterMap.put(entry.getKey(), entry.getValue().toArray(new String[]{}));
        }
        return parameterMap;
    }

    public static Map mapFromMeta (JSONObject JSONDeliverable, String[] keys) throws Exception {
        final String eLabel = "MapHelper.mapFromMeta(JSON): ";
        try {
            if (keys == null) return mapFromMetaObject(JSONDeliverable);
            HashMap<String, String> returnMap = new HashMap<String, String>();
            String temp;
            for (int i = 0; i < keys.length; i++) {
                try {
                    temp = JSONDeliverable.getString(keys[i]);
                    if (temp == null) continue;
                    returnMap.put(keys[i], temp);
                } catch (Exception e) {
                    // Skip the exception for this
                }
            }
            return returnMap;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public static Map mapFromMeta (JSONObject JSONDeliverable) throws Exception {
        return mapFromMeta(JSONDeliverable, null);
    }

    @SuppressWarnings("unchecked")
    public static Map mapFromMetaObject (JSONObject jsonObject) throws Exception {
        final String eLabel = "MapHelper.mapFromMetaObject(JSONObject): ";
        try {
            Map<String, String> returnMap = new HashMap<String, String>();
            // Use iterator to avoid JSONObjects loop X 2
            Iterator<String> keys = jsonObject.keys();
            String key;
            while (keys.hasNext()) {
                key = keys.next();
                Object value = jsonObject.get(key);
                if ((value instanceof String)||(value instanceof Boolean)||(value instanceof Integer)) returnMap.put(key, value.toString());
            }
            return returnMap;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map mergeMaps (Map mainMap, Map defaultMap, String[] keys, boolean enforceValues) throws Exception {
        final String eLabel = "MapHelper.MergeMaps: ";
        // Create a new map to populate and return. We may not want to modify the originals.
        HashMap returnMap = new HashMap();
        // Cover the case where a null default map is passed in...that is acceptable
        if (defaultMap == null) {
            defaultMap = new HashMap();
        }
        try {
            for (int i = 0; i < keys.length; i++) {
                if (!mainMap.containsKey(keys[i]) && !defaultMap.containsKey(keys[i])) {
                    if (enforceValues) {
                        throw new Exception("Required value does not exist for key: " + keys[i]);
                    }
                    continue;
                }
                returnMap.put(keys[i],(mainMap.get(keys[i]) != null) ? mainMap.get(keys[i]) : defaultMap.get(keys[i]));
            }
            return returnMap;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public static HashMap mergeMaps (Map toMergePrimary, Map toMergeSecondary, Object[] verifyKeys) throws Exception {
        final String eLabel = "MapHelper.mergeMaps: ";
        try {
            return (HashMap)mergeMaps(toMergePrimary, toMergeSecondary, new HashMap(), verifyKeys);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map mergeMaps (Map toMergePrimary, Map toMergeSecondary, Map mergedMap, Object[] verifyKeys) throws Exception {
        final String eLabel = "MapHelper.mergeMaps: ";
        try {
            if (toMergePrimary == null) throw new Exception("toMergePrimary parameter must not be null!");
            if (mergedMap == null) throw new Exception("mergedMap parameter must not be null!");
            if (toMergeSecondary != null) mergedMap.putAll(toMergeSecondary);
            mergedMap.putAll(toMergePrimary);
            if (verifyKeys != null) {
                StringBuilder sb = new StringBuilder();
                int i = 0;
                for (Object key: verifyKeys) {
                    if (!mergedMap.containsKey(key)) {
                        if (i++ > 0) sb.append(", ");
                        sb.append("Key does not exist: ").append(key);
                    }
                }
                if (sb.length() > 0) throw new Exception(sb.toString());
            }
            return mergedMap;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public static Map<String, String> parameterMapToMap (Map<String, String[]> parameterMap) throws Exception {
        final String eLabel = "MapHelper.parameterMapToMap: ";
        try {
            HashMap<String, String> map = new HashMap<String, String>();
            String key;
            String value;
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                key = entry.getKey();
                value = (entry.getValue() == null || entry.getValue().length < 1) ? null : entry.getValue()[0];
                map.put(key, value);
            }
            return map;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }

    }

    public static Map<String, String[]> mapToParameterMap (Map<String, String> map) throws Exception {
        final String eLabel = "MapHelper.mapToParameterMap: ";
        try {
            HashMap<String, String[]> parameterMap = new HashMap<String, String[]>();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                parameterMap.put(entry.getKey(), new String[]{entry.getValue()});
            }
            return parameterMap;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public static String mapToJSON (Map<String, String> map, boolean omitNullOrEmptyValues) throws Exception {
        final String eLabel = "MapHelper.mapToJSON: ";
        try {
            StringBuilder sb = new StringBuilder("{");
            int count = 0;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (omitNullOrEmptyValues && (entry.getValue() == null || entry.getValue().trim().equals(""))) continue;
                if (count++ > 0) sb.append(",");
                sb.append("\"").append(entry.getKey()).append("\":").append(JSONObject.quote(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public static String mapToXML (Map<String, String> map, String rootName, boolean omitNullOrEmptyValues) throws Exception {
        final String eLabel = "MapHelper.mapToXML: ";
        try {
            Hashtable<String, String> atts = new Hashtable<String, String>();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (omitNullOrEmptyValues && (entry.getValue() == null || entry.getValue().trim().equals(""))) continue;
                atts.put(entry.getKey(), StringEscapeUtils.escapeXml((entry.getValue() == null) ? "" : entry.getValue()));
            }
            XMLWriter writer = new XMLWriter(null, rootName, atts);
            writer.done();
            return writer.toString();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public static String parameterMapToJSON (Map<String, List<String>> map) throws Exception {
        final String eLabel = "MapHelper.parameterMapToJSON: ";
        try {
            StringBuilder sb = new StringBuilder("[");
            int count = 0;
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                if (entry.getValue() == null || entry.getValue().isEmpty()) {
                    if (count++ > 0) sb.append(",");
                    sb.append("{\"name\":\"").append(entry.getKey()).append("\",\"value\":\"\"}");
                    continue;
                }
                for (String value : entry.getValue()) {
                    if (count++ > 0) sb.append(",");
                    sb.append("{\"name\":\"").append(entry.getKey()).append("\",\"value\":");
                    sb.append(JSONObject.quote(value)).append("}");
                }
            }
            sb.append("]");
            return sb.toString();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public static String parameterMapToXML (Map<String, List<String>> map, String elementName) throws Exception {
        final String eLabel = "MapHelper.parameterMapToXML: ";
        try {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                if (entry.getValue() == null || entry.getValue().isEmpty()) {
                    sb.append("<").append(elementName).append(" name=\"").append(entry.getKey()).append("\" value=\"\"/>");
                    continue;
                }
                for (String value : entry.getValue()) {
                    sb.append("<").append(elementName).append(" name=\"").append(entry.getKey()).append("\" value=\"");
                    sb.append(StringEscapeUtils.escapeXml(value)).append("\"/>");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }
}
