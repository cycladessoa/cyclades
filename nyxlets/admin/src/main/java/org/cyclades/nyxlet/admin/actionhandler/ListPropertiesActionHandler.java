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
package org.cyclades.nyxlet.admin.actionhandler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.stream.XMLStreamWriter;
import org.cyclades.annotations.AHandler;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.nyxlet.templates.stroma.STROMANyxlet;
import org.cyclades.engine.nyxlet.templates.stroma.actionhandler.ActionHandler;
import org.cyclades.engine.stroma.STROMAResponseWriter;
import org.cyclades.engine.validator.ParameterHasValue;
import org.cyclades.io.ResourceRequestUtils;
import org.cyclades.nyxlet.admin.util.Auth;

@AHandler({"listproperties"})
public class ListPropertiesActionHandler extends ActionHandler {

    public ListPropertiesActionHandler (STROMANyxlet parentNyxlet) {
        super(parentNyxlet);
    }

    @Override
    public void handle (NyxletSession nyxletSession, Map<String, List<String>> baseParameters, 
            STROMAResponseWriter stromaResponseWriter) throws Exception {
        final String eLabel = "ListPropertiesActionHandler.handle: ";
        try {
            /********************************************************************/
            /*******                  START CODE BLOCK                    *******/
            /*******                                                      *******/
            /******* YOUR CODE GOES HERE...WITHIN THESE COMMENT BLOCKS.   *******/
            /******* MODIFYING ANYTHING OUTSIDE OF THESE BLOCKS WITHIN    *******/
            /******* THIS METHOD MAY EFFECT THE STROMA COMPATIBILITY      *******/
            /******* OF THIS ACTION HANDLER.                              *******/
            /********************************************************************/
            List<PropertyBundle> propertyBundles = new ArrayList<PropertyBundle>();
            boolean fromXML = parameterAsBoolean(PROPERTY_RESOURCE_XML_PARAMETER, baseParameters, false);
            for (String propertiesURI : baseParameters.get(PROPERTY_RESOURCE_PARAMETER)) {
                InputStream is = null;
                try {
                    is = ResourceRequestUtils.getInputStream(propertiesURI, null);
                    Properties properties = new Properties();
                    if (fromXML) {
                        properties.loadFromXML(is);
                    } else {
                        properties.load(is);
                    }
                    propertyBundles.add(new PropertyBundle(propertiesURI, properties));
                } finally {
                    try { is.close(); } catch (Exception e) {}
                }
            }
            XMLStreamWriter streamWriter = stromaResponseWriter.getXMLStreamWriter();
            for (PropertyBundle propertyBundle : propertyBundles) {
                streamWriter.writeStartElement("properties");
                streamWriter.writeAttribute("source", propertyBundle.uri);
                for (String key : propertyBundle.properties.stringPropertyNames()) {
                    streamWriter.writeStartElement("property");
                    streamWriter.writeAttribute("key", key);
                    streamWriter.writeAttribute("value", propertyBundle.properties.getProperty(key));
                    streamWriter.writeEndElement();
                }
                streamWriter.writeEndElement();
            }
            /********************************************************************/
            /*******                  END CODE BLOCK                      *******/
            /********************************************************************/
        } catch (Exception e) {
            getParentNyxlet().logStackTrace(e);
            handleException(nyxletSession, stromaResponseWriter, eLabel, e);
        } finally {
            stromaResponseWriter.done();
        }
    }
    
    private static class PropertyBundle {
        PropertyBundle (String uri, Properties properties) {
            this.uri = uri;
            this.properties = properties;
        }
        public final String uri;
        public final Properties properties;
    }

    /**
     * Return a valid health check status. This one simply returns true, which
     * will always flag a healthy ActionHandler...more meaningful algorithms
     * can be used.
     *
     * @return true means this is a healthy ActionHandler
     * @throws Exception
     */
    @Override
    public boolean isHealthy () throws Exception {
        return true;
    }

    @Override
    public void init () throws Exception {
        getFieldValidators().add(new ParameterHasValue (PROPERTY_RESOURCE_PARAMETER));
        Auth.addPasswordValidation(this);
    }

    @Override
    public void destroy () throws Exception {
        // your destruction code here, if any
    }
    
    private static final String PROPERTY_RESOURCE_PARAMETER     = "properties";
    private static final String PROPERTY_RESOURCE_XML_PARAMETER = "from-xml";

}
