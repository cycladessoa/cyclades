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
package org.cyclades.engine.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.cyclades.engine.CycladesEngine;
import org.cyclades.engine.EngineContext;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.ResponseCodeEnum;
import org.cyclades.engine.exception.CycladesException;
import org.cyclades.engine.util.MapHelper;
import org.cyclades.xml.comparitor.XMLComparitor;
import org.cyclades.xml.parser.XMLParserException;
import org.cyclades.xml.parser.api.XMLGeneratedObject;
import org.w3c.dom.Node;

public abstract class Nyxlet {

    protected static Logger logger = Logger.getLogger(Nyxlet.class);

    /**
     * All subclasses should call this constructor!!!
     *
     * @throws Exception
     */
    public Nyxlet () throws Exception { }

    public static Nyxlet valueOf(final Node xmlNode, final ClassLoader classLoader, final Properties buildProperties) throws XMLParserException {
        XMLGeneratedObject xmlo = null;
        Nyxlet nyxlet = null;
        try {
            String name = XMLComparitor.getAttributeOrError(xmlNode, "name");
            final String nyxletClassName = XMLComparitor.getAttributeOrError(xmlNode, "class");
            nyxlet = (Nyxlet)classLoader.loadClass(nyxletClassName).newInstance();
            String rrdString = XMLComparitor.getAttributeOrNull(xmlNode, "rrd");
            nyxlet.setName(name);
            nyxlet.setBuildProperties(buildProperties);
            // Load the attributes
            final Vector attributeList = XMLComparitor.getMatchingChildNodes(xmlNode, "attribute");
            for (int j = 0; j < attributeList.size(); j++) {
                String attributeName = XMLComparitor.getAttribute((Node)attributeList.elementAt(j), "name");
                final Vector valueList = XMLComparitor.getMatchingChildNodes((Node)attributeList.elementAt(j), "value");
                String value = null;
                for (int k = 0; k < valueList.size(); k++) {
                    Node node = (Node)valueList.elementAt(k);
                    if (node.getFirstChild() == null) {
                        continue;
                    }
                    value = node.getFirstChild().getNodeValue();
                }
                if (value != null && !value.equals("")) {
                    nyxlet.setAttribute(attributeName, value);
                }
            }
            // IMPORTANT: Initialize this command AFTER attributes are loaded...
            nyxlet.init();
            if (rrdString != null) nyxlet.setRRDString(rrdString);
            return nyxlet;
        } catch (Exception ex) {
            // Try and destroy any resources created here, fail silently
            try { if (nyxlet != null) nyxlet.destroy(); } catch (Exception e) {}
            throw new XMLParserException(ex.getMessage(), ex);
        } finally {
            if( xmlo != null ) { xmlo.cleanUp(); }
        }
    } // end of valueOf(...)

    /**
     * Main entry point into the Nyxlet
     *
     * @param sessionDelegate   The object representing the session instance for the request. Since the base implementation of the CycladesEngine
     *  is a Servlet, we are using HTTPServletRequest/HTTPServletResponse as a base strategy for accessing the services directly ... and adapters of these
     *  classes are provided for access via other mechanisms (JMS etc..). This means an engineer writes a service without having to worry about the
     *  access mechanism...and can concentrate on developing the intended functionality.
     * @return              Usually null, but can be the byte array representation of the response for this Nyxlet. Nyxlets typically write straight to the outputStream
     *  parameter, as this is usually more efficient than buffering the response. Writing to the outputStream parameter and returning null from
     *  the implementation this command is highly recommended
     * @throws CycladesException
     */
    public abstract byte[] process (NyxletSession sessionDelegate) throws CycladesException;

    /**
     * Invoke this service locally
     *
     * @param requestParameters Request parameters
     * @param inputStream   Used to read from incoming stream if needed
     * @param outputStream  Used to write to outgoing stream if needed
     * @return  Usually null, can be the service response if it is not written to the dataOutputStream directly.
     * @throws CycladesException
     */
    public byte[] process (final Map<String, List<String>> requestParameters, final InputStream inputStream, final OutputStream outputStream) throws CycladesException {
        try {
            return this.process(new NyxletSession(requestParameters, inputStream, outputStream));
        } catch (Exception ex) {
            logger.error(ex, ex);
            try {
                return this.handleError(ResponseCodeEnum.GENERAL_ERROR.getCode(), ex);
            } catch (CycladesException exx) {
                throw exx;

            } catch (Exception exx) {
                throw new CycladesException(exx);
            }
        }
    } // end of process(...)

    /**
     * Initialize this service
     *
     * @throws CycladesException
     */
    public abstract void init () throws CycladesException;

    /**
     * Destroy this service
     *
     * @throws CycladesException
     */
    public abstract void destroy () throws CycladesException;

    /**
     * Set an attribute value
     */
    public void setAttribute (String name, String value) {
        this.attributes.put(name, value);
    }

    /**
     * Get an attribute
     */
    public String getAttribute (String name) {
        return (String)this.attributes.get(name);
    }

    public byte[] handleError (short defaultErrorCode, Exception incomingException) throws CycladesException {
        try {
            StringBuilder sb = new StringBuilder();
            String cachedMessage = null;
            if (incomingException instanceof CycladesException) {
                try {
                    cachedMessage = this.getAttribute("error_code" + ((CycladesException)incomingException).getCode());
                } catch (Exception ex) {
                    // Nothing
                }
                if (cachedMessage != null) {
                    sb.append(cachedMessage);
                    sb.append(((CycladesException)incomingException).getDataString());
                } else if (((CycladesException)incomingException).getFriendlyMessage() != null) {
                    sb.append(((CycladesException)incomingException).getFriendlyMessage());
                    sb.append(((CycladesException)incomingException).getDataString());
                } else {
                    sb.append((incomingException));
                }
            } else {
                sb.append(incomingException);
            }
            return sb.toString().getBytes();
        } catch (Exception ex) {
            throw new CycladesException(ex);
        }
    } // end of handleError(...)

    public Logger getLogger () {
        return logger;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getName () {
        return this.name;
    }

    /**
     * This method returns true if the NyxletSession passed in yields true for the given matching algorithm.
     * This method is meant to be overridden, otherwise it will always return false (default below).
     *
     * "RRD" = "RESTful Request Dispatch"
     *
     * XXX - Don't forget to enable the attributes of the element "nyxlet" in the nyxlet_manifest.xml file if you override this.
     * This will simply be an attribute named "rrd" with the value being the collection name (which will be the value of the RRD
     * header), the URI to parameter mapping, and the priority separated by the "|" delimiter ([targetservicecollection]|[URI mapping]|[priority]).
     * Here is an example:
     *
     * rrd="mytargetrestfulservices|/key1/key2/key3/key4|100"
     *
     * Every field must be specified, the URI mapping can be empty.
     *
     * RRD mode is activated when one of the following occurs:
     * - the HTTP Header "rrd" is present
     * - the HTTP Query Parameter "rrd" is present
     *
     * The HTTP Header takes precedence. Here is an example RRD request, using the second option above to specify the "RRD" feature:
     * (NOTE: the value of the "rrd" field should be the target nyxlet RRD group, or the first field in the "rrd" atribute in nyxlet_manifest.xml)
     *
     * http://localhost:8080/cyclades/key1_value/key2_value/key3_value/key4_value?rrd=mytargetrestfulservices
     *
     * @param sessionDelegate   The session delegate wrapping the request data structures
     * @return true if match, false otherwise
     * @throws CycladesException
     */
    public boolean isRRDMatch (NyxletSession sessionDelegate) throws CycladesException {
        return false;
    }

    public Map<String, List<String>> getRRDURIParameterMap (NyxletSession nyxletSession) throws Exception {
        final String eLabel = "Nyxlet.getRRDURIParameterMap";
        try {
            Map<String, List<String>> returnMap = new HashMap<String, List<String>>();
            if (RRDURIToParameterKeyMapping == null) return returnMap;
            String webServiceRequest = nyxletSession.getRequestPathInfo();
            if (webServiceRequest != null && webServiceRequest.length() > 1) returnMap = MapHelper.parameterMapFromURI(webServiceRequest, 1, RRDURIToParameterKeyMapping);
            return returnMap;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    /**
     * Subclasses should override this method to return a valid health check status.
     *
     * @return  true means this is a healthy service, false if otherwise.
     *
     * @throws CycladesException    An exception is definitely not a healthy response!
     */
    public boolean isHealthy () throws CycladesException {
        return false;
    }

    /**
     * Get the cached health status of the Nyxlet
     *
     * @return true if healthy, false otherwise
     */
    public final boolean getHealth () {
        return healthy;
    }

    /**
     * Set the cached health status of the Nyxlet. This will be set by the engine
     * every time a health check request is serviced.
     *
     * @param healthy
     */
    public final void setHealth (boolean healthy) {
        this.healthy = healthy;
    }

    /**
     * Get the RRD (RESTful Request Dispatch) string associated with this object. This can be null,
     * meaning this service cannot be invoked by RRD. If it is not null...it should be in the format
     * of [RRD Key]|[priority] ...so an example would look like:
     *
     * "mygroupkey_a|3 or" simply "mygroupkey_a"
     *
     * If no priority is specified, a default value will be assigned
     *
     * @return the rrdString in its entirety
     */
    public String getRRDString () {
        return this.rrdString;
    }

    public void setRRDString (String rrdString) throws Exception {
        this.rrdString = rrdString;
        String[] fields = rrdString.split("[|]");
        if (fields.length != 3) throw new Exception("Invalid RRD format, should be \"[group]|[uri_part_mapping]|[priority]\"");
        if (!fields[1].isEmpty()) {
            String[] uriParts = fields[1].split("[/]");
            if (uriParts.length > 1) RRDURIToParameterKeyMapping = Arrays.copyOfRange(uriParts, 1, uriParts.length);
        }
    }

    public static EngineContext getEngineContext () {
        return CycladesEngine.getEngineContext();
    }

    /**
     * Subclasses of this class will be responsible for implementing the behavior based on the
     * value returned from this  method.
     *
     * @return true if active, false otherwise
     */
    public final boolean isActive () {
        return this.active;
    }

    /**
     * Subclasses of this class will be responsible for setting this value.
     *
     * @param active
     */
    public final void setActive (boolean active) {
        this.active = active;
    }

    public final void setBuildProperties (Properties buildProperties) {
        this.buildProperties = buildProperties;
    }

    public final Properties getBuildProperties () {
        return buildProperties;
    }

    public String getServiceAgentAttribute () {
        return (attributes.containsKey(SERVICE_AGENT)) ? attributes.get(SERVICE_AGENT).toString() : "";
    }

    private ConcurrentHashMap<Object, Object> attributes = new ConcurrentHashMap<Object, Object>();
    private String name = null;
    private String rrdString = null;
    private String[] RRDURIToParameterKeyMapping = null;
    private volatile boolean active = true;
    private volatile boolean healthy = true;
    private Properties buildProperties;
    public static final String SERVICE_AGENT = "service-agent";

}
