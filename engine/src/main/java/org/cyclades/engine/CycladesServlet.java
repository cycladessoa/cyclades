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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.cyclades.engine.exception.CycladesException;
import org.cyclades.io.Jar;
import com.google.common.base.Strings;
import com.google.common.io.Files;

@SuppressWarnings("serial")
public class CycladesServlet extends HttpServlet {

    static Logger logger = Logger.getLogger(CycladesServlet.class);

    public static String servletBase = "";

    private File backgroundImageFile = null;

    @Override
    public void init (ServletConfig config) throws ServletException {
        super.init(config);
        // Get the base application installation directory...
        servletBase = config.getServletContext().getRealPath("");
        // Initialize the build properties, if present...
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(servletBase + "/META-INF/MANIFEST.MF");
            buildProperties = Jar.getJarManifestMainAttributes(fis);
            if (buildProperties.isEmpty()) logger.warn("No properties found in MANIFEST.MF");
        } catch (Exception e) {
            logger.warn("No MANIFEST.MF found for this war file deployment");
        } finally {
            try {fis.close();} catch (Exception e) {}
        }
        this.backgroundImageFile = new File(servletBase +"/WEB-INF/bg.jpg");
        if( !this.backgroundImageFile.exists() ||
            !this.backgroundImageFile.isFile() ||
            !this.backgroundImageFile.canRead() )
        {
            this.backgroundImageFile = null;
        }
        try {
            hostName = java.net.InetAddress.getLocalHost().getHostName();
            this.engine = new CycladesEngine(config, servletBase);
            this.startDate = System.currentTimeMillis();
        } catch (Exception ex) {
            throw new ServletException(ex.getMessage(), ex);
        }
    } // end of init(...)

    @Override
    public void destroy () {
        try {
            engine.destroy();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        doPost(request, response);
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        doPost(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        try {
            this.engine.processRequest(request, response);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new ServletException(ex);
        }
    } // end of doPost(...)

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        try {
            final String webServiceRequest = Strings.nullToEmpty(request.getPathInfo()).trim();
            if (webServiceRequest.length() > 1) {
                this.doPost(request, response);
                return;
            }
            response.setContentType("text/html");
            String actionString = Strings.nullToEmpty(request.getParameter("action")).trim();

            @SuppressWarnings("unchecked")
            Map<String, ?> pNames = request.getParameterMap();
            if( pNames.size() == 1 ) {
                final String param = pNames.keySet().iterator().next();
                if( !param.equals("action") ) { actionString = param; }
            }

            if( actionString.length() > 0 ) {
                final Action action = Action.valueOfIgnoreCase(actionString);

                if( action == Action.background ) {
                    response.setContentType("image/jpeg");
                    final ServletOutputStream outputStream = response.getOutputStream();
                    try {
                        Files.copy(this.backgroundImageFile, outputStream);
                    } catch (Exception ignore) {
                        return;
                    } finally {
                        outputStream.close();
                    }
                    return;
                }

                SimpleHTMLPage page = new SimpleHTMLPage("Cyclades Service Engine", BACKGROUND_COLOR, FOREGROUND_COLOR, "?background");
                page.append("<H2>");
                switch( action ) {
                case help:
                    this.help(page);
                    break;

                case reload:
                    if (!this.getInitParameter(Definitions.ENABLE_ENGINE_RELOAD).equalsIgnoreCase("true")) {
                        throw new Exception(Definitions.ENABLE_ENGINE_RELOAD + ": " + this.getInitParameter(Definitions.ENABLE_ENGINE_RELOAD));
                    }
                    page.append(this.engine.loadNyxletRepository(request.getParameter("uris")));
                    break;

                case loaded:
                    page.append(this.engine.listNyxlets());
                    break;

                case health:
                    response.getWriter().write(engine.getSystemHealth());
                    return;

                case healthcheck:
                    page.append(this.engine.systemHealthCheck());
                    break;

                case memory:
                    page.append(this.getMemoryInfoForDisplay(false));
                    break;

                case memorywithgc:
                    page.append(this.getMemoryInfoForDisplay(true));
                    break;

                case buildinfo:
                    page.append(this.getBuildInfo());
                    break;

                default:
                    page.append("Invalid action request: ").append(actionString);
                }

                page.append("</H2>");
                response.getWriter().write(page.getHTML());
                return;
            }

            final SimpleHTMLPage page = new SimpleHTMLPage("Cyclades Service Engine", BACKGROUND_COLOR, FOREGROUND_COLOR, "?background");
            page.append("<H2><CENTER>Engine Settings</CENTER><BR>")
                .append("<CENTER><table border=\"1\">")
                .append("<tr><th>Setting</th><th>Value</th></tr>")
                .append("<tr><td>Host Name</td><td>").append(hostName).append("</td></tr>")
                .append("<tr><td>Engine Start Time</td><td>").append(new Date(this.startDate).toString()).append("</td></tr>")
                .append("<tr><td>Class Loader Strategy</td><td>").append(this.getInitParameter(Definitions.CLASS_LOADER_STRATEGY)).append("</td></tr>")
                .append("<tr><td>Debug Mode</td><td>").append(this.getInitParameter(Definitions.DEBUG_MODE)).append("</td></tr>")
                .append("<tr><td>Initialization Delegates</td><td>").append(this.getInitParameter(Definitions.INITIALIZATION_DELEGATES)).append("</td></tr>")
                .append("<tr><td>Auth Delegate</td><td>").append(this.getInitParameter(Definitions.AUTH_DELEGATE)).append("</td></tr>")
                .append("<tr><td>Engine Reloadable</td><td>").append(this.getInitParameter(Definitions.ENABLE_ENGINE_RELOAD)).append("</td></tr>")
                .append("<tr><td>Last Nyxlet Load</td><td>").append(new Date(this.engine.getNyxletLoadStamp()).toString())
                .append("<tr><td>Nyxlet Source Directories</td><td>").append(this.getInitParameter(Definitions.NYXLET_DIRECTORIES)).append("</td></tr>")
                .append("<tr><td>Nyxlet Registry URI</td><td>").append(this.getInitParameter(Definitions.SERVICE_REGISTRY)).append("</td></tr>")
                .append("</table></CENTER></H2>");
            response.getWriter().write(page.getHTML());

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new ServletException(ex.getMessage(), ex);
        }
    } // end of doGet(...)

    private void help(SimpleHTMLPage page) {
        page.append("<CENTER><table border=\"1\">");
        page.append("<tr><th>Cyclades Engine Action</th><th>Description</th></tr>");
        page.append("<tr><td>help</td><td>Display this page</td></tr>");
        page.append("<tr><td>buildinfo</td><td>Display the system wide build information</td></tr>");
        page.append("<tr><td>reload</td><td>Reload and display all of the Nyxlets</td></tr>");
        page.append("<tr><td>loaded</td><td>Display all of the Nyxlets loaded</td></tr>");
        page.append("<tr><td>health</td><td>Display a general overall health state of the Nyxlets (minimalistic format)</td></tr>");
        page.append("<tr><td>healthcheck</td><td>Run a healthcheck on the Nxylets and display the results</td></tr>");
        page.append("<tr><td>memory</td><td>Display the memory usage of the system</td></tr>");
        page.append("<tr><td>memorywithgc</td><td>Make a GC request and display the memory usage of the system</td></tr>");
    }

    private String getMemoryInfoForDisplay (boolean garbageCollect) throws ServletException {
        final String eLabel = "CycladesServlet.getMemoryInfoForDisplay: ";
        try {
            StringBuilder sb = new StringBuilder("<H2><CENTER>CycladesEngine JVM Memory (bytes)");
            Runtime runTime = Runtime.getRuntime();
            if (garbageCollect) {
                runTime.gc();
                sb.append(" (Garbage Collection Requested Prior To Fetching Memory Stats)");
            }
            sb.append("</CENTER><BR>");
            sb.append("<CENTER><table border=\"1\">");
            sb.append("<tr><td>Free Memory</td><td>").append(runTime.freeMemory()).append("</td></tr>");
            sb.append("<tr><td>Total Memory</td><td>").append(runTime.totalMemory()).append("</td></tr>");
            sb.append("<tr><td>Max Memory</td><td>").append(runTime.maxMemory()).append("</td></tr>");
            sb.append("</table></CENTER></H2>");
            return sb.toString();
        } catch (Exception e) {
            throw new ServletException(eLabel + e);
        }
    } // end of getMemoryInfoForDisplay(...)

    public String getBuildInfo () throws CycladesException {
        final String eLabel = "CycladesServlet.getBuildInfo: ";
        try {
            StringBuilder sb = new StringBuilder("<H2><CENTER>Cyclades Engine Build Information</CENTER><BR>");
            sb.append("<CENTER><table border=\"1\">");
            sb.append("<tr><th>Property Name</th><th>PropertyValue</th></tr>");
            for (Map.Entry<Object, Object> entry : buildProperties.entrySet()) {
                sb.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("</td></tr>");
            }
            sb.append("</table></CENTER></H2><BR>");
            engine.listNyxletsBuildInfo(sb);
            return sb.toString();
        } catch (Exception e) {
            throw new CycladesException(eLabel + e);
        }
    }

    private static enum Action {

        help, reload, loaded, health, healthcheck, memory, memorywithgc, background, buildinfo, unknown;

        public static Action valueOfIgnoreCase(String actionString) {
            if (actionString.trim().length() == 0 ||
                  actionString.equalsIgnoreCase("help")       ) { return help; }
            if (actionString.equalsIgnoreCase("reload")       ) { return reload; }
            if (actionString.equalsIgnoreCase("loaded")       ) { return loaded; }
            if (actionString.equalsIgnoreCase("health")       ) { return health; }
            if (actionString.equalsIgnoreCase("healthcheck")  ) { return healthcheck; }
            if (actionString.equalsIgnoreCase("memory")       ) { return memory; }
            if (actionString.equalsIgnoreCase("memorywithgc") ) { return memorywithgc; }
            if (actionString.equalsIgnoreCase("background")   ) { return background; }
            if (actionString.equalsIgnoreCase("buildinfo")   ) { return buildinfo; }

            return unknown;
        } // end of valueOfIgnoreCase(...)

    } // end of enum Action

    private CycladesEngine engine = null;
    private long startDate = -1;
    private String hostName;
    private Properties buildProperties = new Properties();
    private static final String BACKGROUND_COLOR = "000000";
    private static final String FOREGROUND_COLOR = "00FF00";

} // end of CycladesServlet(...)
