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

import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;
import org.cyclades.xml.generator.XMLWriter;

public class PropertyUtils {

    public static String propertiesToJSON (Properties properties) throws Exception {
        final String eLabel = "MetaResponseHelper.propertiesToJSON: ";
        try {
            StringBuilder sb = new StringBuilder("{");
            Set keySet = properties.keySet();
            int i = keySet.size();
            for (Object key : properties.keySet()) {
                sb.append("\"");
                sb.append(key);
                sb.append("\":\"");
                sb.append(properties.get(key.toString()).toString().replace("\"", "\\\""));
                sb.append("\"");
                if (--i > 0) {
                    sb.append(",");
                }
            }
            sb.append("}");
            return sb.toString();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public static String propertiesToXML (Properties properties) throws Exception {
        final String eLabel = "MetaResponseHelper.propertiesToXML: ";
        try {
            Hashtable<String, String> atts = new Hashtable<String, String>();
            XMLWriter writer = new XMLWriter(null, PROPERTIES_ATTRIBUTE, null);
            Set keySet = properties.keySet();
            int i = keySet.size();
            for (Object key : properties.keySet()) {
                atts.put(NAME_ATTRIBUTE, key.toString());
                writer.addLeafNode(PROPERTY_ATTRIBUTE, atts, properties.get(key.toString()).toString(), true);
            }
            writer.done();
            return writer.toString();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private final static String PROPERTIES_ATTRIBUTE        = "properties";
    private final static String PROPERTY_ATTRIBUTE          = "property";
    private final static String NAME_ATTRIBUTE              = "name";
}
