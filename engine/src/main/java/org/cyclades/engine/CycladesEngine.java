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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.cyclades.engine.api.InitializationDelegate;
import org.cyclades.engine.api.Nyxlet;
import org.cyclades.engine.auth.api.AuthDelegate;
import org.cyclades.engine.exception.CycladesException;
import org.cyclades.engine.nyxlet.NyxletRepository;
import org.cyclades.io.ResourceRequestUtils;

import com.google.common.base.Strings;

public class CycladesEngine {

    static Logger logger = Logger.getLogger(CycladesEngine.class);

    /**
     * It all starts here!
     *
     * @param servletConfig
     * @throws CycladesException
     */
    public CycladesEngine (ServletConfig servletConfig, String applicationBaseDirectory) throws CycladesException {
        AuthDelegate authDelegate;
        boolean minimizeMemoryFootprint = false;
        int debugMode = 0;
        try {
            // Create and initialize the specified InitializationDelegate
            String tempString = servletConfig.getInitParameter(Definitions.INITIALIZATION_DELEGATES);
            if (tempString != null) {
                InitializationDelegate initializationDelegate;
                for (String delegateClassName : tempString.split("[,]")) {
                    initializationDelegate = (InitializationDelegate)Class.forName(delegateClassName.trim()).newInstance();
                    initializationDelegate.initialize(servletConfig);
                    this.initializationDelegates.add(initializationDelegate);
                }
            } else {
                logger.info("No InitializationDelegates specified");
            }
            // Create and initialize the specified AuthDelegate
            tempString = servletConfig.getInitParameter(Definitions.AUTH_DELEGATE);
            if (tempString != null) {
                authDelegate = (AuthDelegate)Class.forName(tempString).newInstance();
                authDelegate.initialize(servletConfig);
            } else {
                authDelegate = null;
                logger.info("No InitializationDelegate specified");
            }
            // Initialize the suggested memory usage of the system
            String minimizeMemoryFootprintFlag = servletConfig.getInitParameter(Definitions.MINIMIZE_MEMORY_FOOTPRINT_FLAG);
            if (minimizeMemoryFootprintFlag != null) {
                minimizeMemoryFootprint = minimizeMemoryFootprintFlag.toLowerCase().equals("true");
            }
            logger.debug(Definitions.MINIMIZE_MEMORY_FOOTPRINT_FLAG + ": " + minimizeMemoryFootprint);
            // Initialize The Debug Mode
            String debugModeString = servletConfig.getInitParameter(Definitions.DEBUG_MODE);
            if (debugModeString == null) {
                throw new Exception("Config parameter " + Definitions.DEBUG_MODE + " came back null.");
            }
            debugMode = Integer.parseInt(debugModeString);
            logger.debug("Debug Mode:[" + debugMode + "]");
            // Initialize the engine context object, which will be made available to everyone
            engineContext = new EngineContext(authDelegate, servletConfig, applicationBaseDirectory, minimizeMemoryFootprint, debugMode);
            // Initialize the NyxletRepository
            this.loadNyxletRepository(null);
        } catch (CycladesException ex) {
            throw ex;

        } catch (Exception ex) {
            throw new CycladesException(ex.getMessage(), ex);
        }
    }

    /**
     * Load the Nyxlets, if any
     *
     * XXX - Extract Class Loader strategy from Servlet Config Parameter
     *
     * @param nyxletDirOverride If not null, use this as the Nyxlet directories value
     *
     * @return human readable message (html)
     * @throws CycladesException
     */
    public String loadNyxletRepository (String nyxletDirOverride) throws CycladesException {
        try {
            String classLoaderStrategyString = engineContext.getServletConfig().getInitParameter(Definitions.CLASS_LOADER_STRATEGY);
            if (classLoaderStrategyString == null) {
                throw new Exception("Config parameter " + Definitions.CLASS_LOADER_STRATEGY + " came back null.");
            }
            if( !classLoaderStrategyString.equalsIgnoreCase(Definitions.ISOLATED_CLASS_LOADER) &&
                !classLoaderStrategyString.equalsIgnoreCase(Definitions.COLLECTIVE_CLASS_LOADER) )
            {
                throw new Exception("Config parameter " + Definitions.CLASS_LOADER_STRATEGY + " invalid: " + classLoaderStrategyString);
            }

            if( logger.isInfoEnabled() ) {
                logger.info("Class Loader strategy:[" + classLoaderStrategyString + "]");
            }
            return this.loadNyxletRepository(classLoaderStrategyString.equalsIgnoreCase(Definitions.ISOLATED_CLASS_LOADER), nyxletDirOverride);

        } catch (Exception ex) {
            throw new CycladesException(ex.getMessage(), ex);
        }
    } // end of loadNyxletRepository()

    /**
     * Load the Nyxlets, returning a nice HTML String for display.
     *
     * @param isolatedClassLoader       Use the isolated class loader strategy if this value is true
     * @param nyxletDirOverride         If not null, use this as the Nyxlet directories value
     * @return human readable message (html)
     * @throws CycladesException
     */
    public String loadNyxletRepository (boolean isolatedClassLoader, String nyxletDirOverride) throws CycladesException {
        StringBuilder sb = new StringBuilder("<CENTER>");
        try {
            String nyxletDirectoriesString = (nyxletDirOverride == null) ? engineContext.getServletConfig().getInitParameter(Definitions.NYXLET_DIRECTORIES) : nyxletDirOverride;
            if (nyxletDirectoriesString == null) {
                sb.append("Config parameter ")
                  .append(Definitions.NYXLET_DIRECTORIES)
                  .append(" came back null. Nyxlets will not be loaded");

            } else {
                String[] nyxletDirectories = nyxletDirectoriesString.split("[,]");
                for (int i = 0; i < nyxletDirectories.length; i++) {
                    nyxletDirectories[i] = engineContext.getCanonicalEngineDirectoryPath(nyxletDirectories[i].trim());
                }
                engineContext.setNyxletDirectories(nyxletDirectories);
                int loaded = NyxletRepository.getStaticInstance().reloadNyxletFiles(nyxletDirectories, true, isolatedClassLoader);
                sb.append("Nyxlets have been loaded from source directories: ")
                  .append(nyxletDirectoriesString)
                  .append("<BR>Number of Nyxlets loaded: ")
                  .append(loaded);
            }

            String temp = engineContext.getServletConfig().getInitParameter(Definitions.SERVICE_REGISTRY);
            temp = (temp == null) ? null : new String(ResourceRequestUtils.getData(engineContext.getCanonicalEngineDirectoryPath(temp), null));
            engineContext.setNyxletTargets(temp);
            nyxletLoadStamp = System.currentTimeMillis();
            sb.append("</CENTER><BR>").append(this.listNyxlets());
            String finalString = sb.toString();
            logger.debug(finalString);

            return finalString;
        } catch (CycladesException ex) {
            throw ex;

        } catch (Exception ex) {
            throw new CycladesException(ex.getMessage(), ex);
        }
    } // end of loadNyxletRepository(...)

    /**
     * Simply list the Nyxlets currently loaded into the engine. Format return
     * String in HTML.
     *
     * @return human readable message (html)
     * @throws CycladesException
     */
    public String listNyxlets () throws CycladesException {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<CENTER><table border=\"1\">");
            sb.append("<tr><th>Nyxlet Name</th><th>RESTful Request Dispatch Setting</th><th>Active</th></tr>");
            Set<String> keySet = NyxletRepository.getStaticInstance().keys();
            Nyxlet nyxlet;
            String active;
            int i = 0;
            for (String key : keySet) {
                i++;
                nyxlet = NyxletRepository.getStaticInstance().getNyxlet(key);
                active = (nyxlet.isActive()) ? "true" : "<p style=\"color:red\">false</p>";
                sb.append("<tr><td>").append(nyxlet.getName()).append("</td><td>").append(nyxlet.getRRDString());
                sb.append("</td><td>").append(active).append("</td></tr>");
            }
            sb.append("</table></CENTER><BR><CENTER>Nyxlets Loaded: ").append(i).append(" @ ").append(new java.util.Date(nyxletLoadStamp)).append("</CENTER>");
            return sb.toString();
        } catch (Exception ex) {
            throw new CycladesException(ex);
        }
    } // end of listNyxlets(...)

    public void listNyxletsBuildInfo (StringBuilder sb) throws CycladesException {
        try {
            sb.append("<BR><CENTER><H2>Nyxlet Build Information</CENTER><BR>");
            Set<String> keySet = NyxletRepository.getStaticInstance().keys();
            sb.append("<CENTER><table border=\"1\">");
            sb.append("<tr><th>Property Name</th><th>PropertyValue</th></tr>");
            for (String key : keySet) {
                listNyxletBuildInfo(NyxletRepository.getStaticInstance().getNyxlet(key), sb);
            }
            sb.append("</table></CENTER></H2>");
        } catch (Exception ex) {
            throw new CycladesException(ex);
        }
    }

    private void listNyxletBuildInfo (Nyxlet nyxlet, StringBuilder sb) throws CycladesException {
        try {
            sb.append("<tr><th COLSPAN=\"2\">").append(nyxlet.getName()).append("</th></tr>");
            for (Map.Entry<Object, Object> entry : nyxlet.getBuildProperties().entrySet()) {
                sb.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("</td></tr>");
            }
        } catch (Exception ex) {
            throw new CycladesException(ex);
        }
    }

    /**
     * List the Nyxlets currently loaded into the engine, running a health check on each. Format return
     * String in HTML.
     *
     * @return human readable message (html)
     * @throws CycladesException
     */
    public String systemHealthCheck () throws CycladesException {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<CENTER><table border=\"1\">");
            sb.append("<tr><th>Nyxlet Name</th><th>RESTful Request Dispatch Setting</th><th>Healthy</th><th>Active</th></tr>");
            Set<?> keySet = NyxletRepository.getStaticInstance().keys();
            Nyxlet nyxlet;
            int i = 0;
            boolean isHealthy;
            String healthString;
            String active;
            for (Object key : keySet) {
                i++;
                nyxlet = NyxletRepository.getStaticInstance().getNyxlet(key);
                try {
                    isHealthy = nyxlet.isHealthy();
                } catch (Exception ex) {
                    logger.error(ex);
                    isHealthy = false; // obviously
                }
                nyxlet.setHealth(isHealthy);
                healthString = (isHealthy) ? "true" : "<p style=\"color:red\">false</p>";
                active = (nyxlet.isActive()) ? "true" : "<p style=\"color:red\">false</p>";
                sb.append("<tr><td>").append(nyxlet.getName()).append("</td><td>").append(nyxlet.getRRDString());
                sb.append("</td><td>").append(healthString).append("</td><td>").append(active).append("</td></tr>");
            }
            sb.append("</table></CENTER><BR><CENTER>Nyxlets Loaded: ").append(i).append(" @ ").append(new java.util.Date(nyxletLoadStamp)).append("</CENTER>");
            return sb.toString();
        } catch (Exception ex) {
            throw new CycladesException(ex);
        }
    } // end of systemHealthCheck()

    public String getSystemHealth () throws CycladesException {
        try {
            Set<?> keySet = NyxletRepository.getStaticInstance().keys();
            Nyxlet nyxlet;
            int unhealthy = 0;
            int inactive = 0;
            for (Object key : keySet) {
                nyxlet = NyxletRepository.getStaticInstance().getNyxlet(key);
                if (!nyxlet.getHealth()) unhealthy++;
                if (!nyxlet.isActive()) inactive++;
            }
            return new StringBuilder("unhealthy:[").append(unhealthy).append("] inactive:[").append(inactive).append("]").toString();
        } catch (Exception ex) {
            throw new CycladesException(ex);
        }
    }

    /**
     * Get the number of loaded Nyxlets
     *
     * @return number of Nyxlets loaded
     * @throws CycladesException
     */
    public int numNyxlets () throws CycladesException {
        try {
            return NyxletRepository.getStaticInstance().size();
        } catch (Exception ex) {
            throw new CycladesException(ex);
        }
    }

    /**
     * Life Cycle Support
     *
     * @throws CycladesException
     */
    public void destroy () throws CycladesException {
        // Destroy the NyxletRepository.
        try {
            NyxletRepository.getStaticInstance().destroy();
        } catch (Exception ex) {
            logger.error(ex);
        }

        // Destroy the InitializationDelegates
        for (InitializationDelegate initializationDelegate : this.initializationDelegates) {
            try {
                initializationDelegate.destroy();
            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        // Destroy the AuthDelegate
        try {
            final AuthDelegate authDelegate = engineContext.getAuthDelegate();
            if (authDelegate != null) {  authDelegate.destroy();  }
        } catch (Exception ex) {
            logger.error(ex);
        }
    } // end of destroy()

    /**
     * Process the incoming request
     *
     * @param request
     * @param response
     * @throws CycladesException
     */
    public void processRequest (HttpServletRequest request, HttpServletResponse response) throws CycladesException {
        DataInputStream  dis = null;
        DataOutputStream dos = null;
        //logger.debug("INCOMING REQUEST FOR ENGINE");
        try {
            Object nyxletDelegateKey = null;
            if( Strings.nullToEmpty(request.getHeader(CONTENT_TYPE)).trim().equalsIgnoreCase(FORM_ENCODED) && !Strings.nullToEmpty(request.getHeader(STROMA_ENABLED)).trim().equalsIgnoreCase("true") ) {
                //logger.debug("Form data  enabled");
            } else {
                dis = new DataInputStream(request.getInputStream());
                //logger.debug("Form data disabled");
            }

            dos = new DataOutputStream(response.getOutputStream());
            Nyxlet nyxlet    = null;
            byte[] commandCollectiveResponse = null;
            NyxletSession sessionDelegate  = new NyxletSession(request, response, dis, dos);
            // If this is a RRD tagged request (targeting a service with the RRD header and some URI analytics)
            String rrd = request.getHeader(RESTFUL_REQUEST_DISPATCH);
            if (rrd == null) { // try again from query parameters
                rrd = request.getParameter(RESTFUL_REQUEST_DISPATCH.toLowerCase());
            }

            // By default, let the action be the HTTP Request Method unless overwritten later
            if (sessionDelegate.getActionString() == null) {
                sessionDelegate.setActionObject(sessionDelegate.getRequestMethod());
            }

            if (rrd != null) {
                nyxletDelegateKey = "RRD: " + rrd; // Just used for the error output below
                nyxlet = NyxletRepository.getStaticInstance().getNyxletRRD(rrd, sessionDelegate);
                sessionDelegate.setRRDRequest(true);
            } else {
                // This is a normal service request (targeting a service with the URI part)
                String webServiceRequest = Strings.nullToEmpty(request.getPathInfo()).trim();
                if( webServiceRequest.length() == 0 ) {
                    throw new Exception ("Invalid engine request URI, no data for service dispatching");
                }
                String[] URIParts = webServiceRequest.split("/");
                // A valid URI part looks something like this: /ServiceNyxlet/something
                // When this is split on the character "/", it yields ("", "ServiceNyxlet", "something")
                // We want the second index, or "ServiceNyxlet" in the example above.
                // XXX - The following check seems unnecessary...just for the sake of belt and braces
                if (URIParts.length < 2) {
                    throw new Exception("Bad web service request URI:[" + webServiceRequest + "]");
                }
                nyxletDelegateKey = URIParts[1];
                if( logger.isDebugEnabled() ) {
                    logger.debug("Servicing URL Services request for:" + nyxletDelegateKey);
                }

                nyxlet = NyxletRepository.getStaticInstance().getNyxlet(nyxletDelegateKey);
            }

            // Return an error if there is no Nyxlet module registered under the requested name. Call "writeError" directly here
            // because the response code is unique and not covered in the exception list of this method
            if (nyxlet == null) {
                this.writeError(dos, ResponseCodeEnum.SERVICE_NOT_FOUND.getCode(), "Unknown service Nyxlet module: " + nyxletDelegateKey, response);
                return;
            }

            final String serviceAgent = nyxlet.getServiceAgentAttribute();
            if (serviceAgent != null && !serviceAgent.isEmpty()) response.setHeader(Nyxlet.SERVICE_AGENT, serviceAgent);

            commandCollectiveResponse = nyxlet.process(sessionDelegate);
            // NOTE: Check to see if the commandCollectiveResponse has been returned as null. This is actually a valid
            // condition as it will flag that the targeted command will not want to proceed with the following
            // code. If the commandCollectiveResponse is returned as null it is assumed that the all the following logic and
            // writing to the output stream is taken care of by the command itself. An example of why a
            // certain implementation would want to do this is the case where a command would want to save main
            // memory usage and stream a resource directly from the file system to the outputstream.
            //
            // In the case where there is nothing to write back to the stream from the command, simply
            // return a byte[] with no elements...so "new byte[0]".
            //
            // See sample command for more details.
            if (commandCollectiveResponse == null) {
                return;
            }
            dos.write(commandCollectiveResponse);

        } catch (CycladesException ex) {
            try {
                this.writeError(dos, ex.getCode(), this.buildErrorMessage(ex), response);
            } catch (Exception ignore) {}
            logger.error(ex);
            throw ex;

        } catch (Exception ex) {
            try {
                this.writeError(dos, ResponseCodeEnum.GENERAL_ERROR.getCode(), ex.getMessage(), response);
            } catch (Exception ignore) {}
            logger.error(ex);
            throw new CycladesException(ex);

        } finally {
            try { dos.flush(); } catch (Exception ignore) { }
            try { dos.close(); } catch (Exception ignore) { }
            try { dis.close(); } catch (Exception ignore) { }
        }
    } // end of processRequest(...)

    private String buildErrorMessage (CycladesException e) throws CycladesException {
        try {
            StringBuilder errorStringBuilder = new StringBuilder();
            String mappedMessage = null;
            /*try {
            // Create a mapping mechanism
            mappedMessage = SomeMessageMappingHash.getMessage(e.getCode());
            } catch (Exception ex) {
            mLogger.error(eLabel + ex);
            }*/

            if (mappedMessage != null) {
                return errorStringBuilder.append(mappedMessage).append(e.getDataString()).toString();
            }

            if (e.getFriendlyMessage() != null) {
                return errorStringBuilder.append(e.getFriendlyMessage()).append(e.getDataString()).toString();
            }

            return errorStringBuilder.append(e.getMessage()).toString();
        } catch (Exception ex) {
            throw new CycladesException("" + e + ex);
        }
    } // end of buildErrorMessage(...)

    /**
     * Write the error out, part binary and part string...and set the status code as an internal error
     * in the response
     *
     * @param dos
     * @param status
     * @param msg
     * @param response
     * @throws CycladesException
     */
    private void writeError (final DataOutputStream dos, final short status,
                             final String msg,           final HttpServletResponse response)
        throws CycladesException
    {
        try {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            dos.writeShort(Definitions.CYCLADES_ENGINE_ERROR_RESPONSE);
            dos.writeShort(status);
            dos.writeUTF(msg);
        } catch (Exception ex) {
            throw new CycladesException(ex);
        }
    } // end of writeError(...)

    public static EngineContext getEngineContext () {
        return engineContext;
    }

    public long getNyxletLoadStamp () {
        return nyxletLoadStamp;
    }

    private List<InitializationDelegate> initializationDelegates = new ArrayList<InitializationDelegate>();
    private static EngineContext engineContext;
    private long nyxletLoadStamp;
    private static final String STROMA_ENABLED              = "STROMA";
    private static final String FORM_ENCODED                = "application/x-www-form-urlencoded";
    private static final String CONTENT_TYPE                = "CONTENT-TYPE";
    private static final String RESTFUL_REQUEST_DISPATCH    = "RRD";

} // end of class CycladesEngine
