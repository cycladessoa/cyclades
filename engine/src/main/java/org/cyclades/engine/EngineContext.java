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

import java.net.InetAddress;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletConfig;
import org.cyclades.engine.auth.api.AuthDelegate;
import org.cyclades.engine.logging.LoggingEnum;
import org.cyclades.engine.util.SendMail;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.log4j.Logger;

public class EngineContext {

    public EngineContext(AuthDelegate authDelegate, ServletConfig servletConfig,
            String applicationBaseDirectory, boolean minimizeMemoryFootprint,
            int debugMode) throws Exception {
        final String eLabel = "EngineContext.EngineContext: ";
        try {
            this.authDelegate = authDelegate;
            this.servletConfig = servletConfig;
            this.applicationBaseDirectory = applicationBaseDirectory;
            this.minimizeMemoryFootprint = minimizeMemoryFootprint;
            this.debugMode = debugMode;
            extractNotificationEmailData(servletConfig.getInitParameter(NOTIFICATION_EMAIL_DATA));
            String notificationLoggerString = servletConfig.getInitParameter(NOTIFICATION_LOGGER);
            this.notificationLogger = Logger.getLogger((notificationLoggerString != null) ? notificationLoggerString : DEFAULT_NOTIFICATION_LOGGER);
            this.hostName = getHostName();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private void extractNotificationEmailData (String data) throws Exception {
        final String eLabel = "EngineContext.extractNotificationEmailData: ";
        try {
            if (data == null) return;
            String[] dataParts = data.split("[|]");
            if (dataParts.length != 4) throw new Exception("Notification email data has not been properly defined");
            notificationList        = dataParts[0].trim();
            notificationFromEmail   = dataParts[1].trim();
            notificationSMTPHost    = dataParts[2].trim();
            notificationLevel       = LoggingEnum.valueOf(dataParts[3].trim().toUpperCase());
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }
    
    private String getHostName() {
        String hostName = null;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
        }
        return String.valueOf(hostName);
    }

    public void setNyxletTargets (String nyxletRegistry) throws Exception {
        final String eLabel = "EngineContext.populateNyxletTargetMap: ";
        try {
            this.nyxletToTargetMap = new ConcurrentHashMap<String, String[]>();
            if (nyxletRegistry == null) return;
            String[] targets;
            JSONObject nyxletRegistryJSONObject = new JSONObject(nyxletRegistry);
            @SuppressWarnings("unchecked")
            Iterator<String> iterator = nyxletRegistryJSONObject.keys();
            JSONArray jsonArray;
            String key;
            while (iterator.hasNext()) {
                key = iterator.next();
                jsonArray = nyxletRegistryJSONObject.getJSONArray(key);
                targets = new String[jsonArray.length()];
                for (int i = 0; i < targets.length; i++) {
                    targets[i] = jsonArray.getString(i);
                }
                this.nyxletToTargetMap.put(key, targets);
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public AuthDelegate getAuthDelegate() {
        return this.authDelegate;
    }

    public ServletConfig getServletConfig() {
        return this.servletConfig;
    }

    public String getApplicatonBaseDirectory() {
        return this.applicationBaseDirectory;
    }

    public boolean isMinimizeMemoryFootprint() {
        return this.minimizeMemoryFootprint;
    }

    public int getDebugMode() {
        return this.debugMode;
    }

    public String[] getNyxletDirectories () {
        return this.nyxletDirectories;
    }

    public void setNyxletDirectories (String[] nyxletDirectories) {
        this.nyxletDirectories = nyxletDirectories;
    }

    public String[] getNyxletTargets (String nyxlet) {
        return this.nyxletToTargetMap.get(nyxlet);
    }

    public void sendNotification (LoggingEnum level, String message) throws Exception {
        if (notificationLogger != null) notificationLogger.log(level.getLog4jLevel(), message);
        StringBuilder sb = new StringBuilder("NOTIFICATION:[");
        sb.append(hostName).append("] LEVEL:[").append(level.name()).append("]");
        if (notificationList != null && notificationLevel.shouldLog(level)) SendMail.sendMessage(notificationList, notificationFromEmail, notificationSMTPHost, sb.toString(), message);
    }

    /**
     * Return a complete path to the engine directory WEB-INF. This is typically the base directory from where
     * resources are placed for access by Cyclades and Nyxlets.
     *
     * If the path starts with a "/", or contains a ":" character, assume this is a complete path,
     * otherwise assume this path is relative to [applicationBaseDirectory]/WEB-INF.
     *
     * @param path
     * @return real path
     */
    public String getCanonicalEngineDirectoryPath (String path) {
        if (path.startsWith("/") || path.contains(":")) return path;
        return getCanonicalEngineApplicationBaseDirectoryPath(new StringBuilder("WEB-INF/").append(path).toString());
    }

    /**
     * Return a complete path to the engine application base directory (i.e. contains WEB-INF). This method
     * gives the caller access to items that may be beyond application functionality scope, and is
     * mainly intended for administrative purposed.
     *
     * If the path starts with a "/", or contains a ":" character, assume this is a complete path,
     * otherwise assume this path is relative to [applicationBaseDirectory].
     *
     * @param path
     * @return real path
     */
    public String getCanonicalEngineApplicationBaseDirectoryPath (String path) {
        if (path.startsWith("/") || path.contains(":")) return path;
        return new StringBuilder(getApplicatonBaseDirectory()).append("/").append(path).toString();
    }

    private final AuthDelegate authDelegate;
    private final ServletConfig servletConfig;
    private final String applicationBaseDirectory;
    private final boolean minimizeMemoryFootprint;
    private final int debugMode;
    private String[] nyxletDirectories;
    private ConcurrentHashMap<String, String[]> nyxletToTargetMap = new ConcurrentHashMap<String, String[]>();
    private String notificationList;
    private String notificationFromEmail;
    private String notificationSMTPHost;
    private LoggingEnum notificationLevel;
    private final String hostName;
    private final Logger notificationLogger;
    public static final String NOTIFICATION_EMAIL_DATA      = "notificationEmailData";
    public static final String NOTIFICATION_LOGGER          = "notificationLogger";
    public static final String DEFAULT_NOTIFICATION_LOGGER  = "notify";
    
}
