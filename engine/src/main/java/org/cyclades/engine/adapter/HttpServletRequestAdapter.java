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
package org.cyclades.engine.adapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;

@SuppressWarnings("deprecation")
public class HttpServletRequestAdapter implements HttpServletRequest {

    static Logger logger = Logger.getLogger(HttpServletRequestAdapter.class);

    public HttpServletRequestAdapter (Map<String, String> headerMap, Map<String, String[]> parameterMap, Map<String, Object> attributeMap, InputStream is) {
        this.headerMap = headerMap;
        this.parameterMap = parameterMap;
        this.attributeMap = attributeMap;
        this.is = (is == null) ? null : new ServletInputStreamAdapter(is);
    }

    public String getHeader(String arg0) {
        return this.headerMap.get(arg0);
    }

    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(this.headerMap.keySet());
    }

    public Enumeration<String> getHeaders(String arg0) {
        ArrayList<String> list = new ArrayList<String>();
        list.add(this.headerMap.get(arg0));
        return Collections.enumeration(list);
    }

    public int getIntHeader(String arg0) {
        return Integer.parseInt(this.headerMap.get(arg0));
    }

    public Object getAttribute(String arg0) {
        return this.attributeMap.get(arg0);
    }

    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(this.attributeMap.keySet());
    }

    public ServletInputStream getInputStream() throws IOException {
        return this.is;
    }

    public String getParameter(String arg0) {
        String[] parameterValues = this.parameterMap.get(arg0);
        return (parameterValues != null && parameterValues.length > 0) ? parameterValues[0] : null;
    }

    public Map<String, String[]> getParameterMap() {
        return this.parameterMap;
    }

    public void setParameterMap(Map<String, String[]> parameterMap) {
        this.parameterMap = parameterMap;
    }

    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(this.parameterMap.keySet());
    }

    public String[] getParameterValues(String arg0) {
        return this.parameterMap.get(arg0);
    }

    public void removeAttribute(String arg0) {
        this.attributeMap.remove(arg0);
    }

    public void setAttribute(String arg0, Object arg1) {
        this.attributeMap.put(arg0, arg1);
    }

    public Locale getLocale() {return null;}
    public Enumeration getLocales() {return null;}
    public String getAuthType() {return null;}
    public int getLocalPort() {return 0;}
    public String getLocalAddr() {return null;}
    public String getLocalName() {return null;}
    public String getContextPath() {return null;}
    public Cookie[] getCookies() {return null;}
    public long getDateHeader(String arg0) {return 0;}
    public String getMethod() {return null;}
    public String getPathInfo() {return null;}
    public String getPathTranslated() {return null;}
    public String getQueryString() {return null;}
    public String getRemoteUser() {return null;}
    public String getRequestedSessionId() {return null;}
    public String getRequestURI() {return null;}
    public StringBuffer getRequestURL() {return null;}
    public String getServletPath() {return null;}
    public HttpSession getSession() {return null;}
    public HttpSession getSession(boolean arg0) {return null;}
    public Principal getUserPrincipal() {return null;}
    public boolean isRequestedSessionIdFromCookie() {return false;}
    public int getRemotePort() {return 0;}
    public boolean isRequestedSessionIdFromUrl() {return false;}

    public boolean isRequestedSessionIdFromURL() {return false;}
    public boolean isRequestedSessionIdValid() {return false;}
    public boolean isUserInRole(String arg0) {return false;}
    public String getCharacterEncoding() {return null;}
    public int getContentLength() {return 0;}
    public String getContentType() {return null;}
    public String getProtocol() {return null;}
    public BufferedReader getReader() throws IOException {return null;}
    public String getRealPath(String arg0) {return null;}
    public String getRemoteAddr() {return null;}
    public String getRemoteHost() {return null;}
    public RequestDispatcher getRequestDispatcher(String arg0) {return null;}
    public String getScheme() {return null;}
    public String getServerName() {return null;}
    public int getServerPort() {return 0;}
    public boolean isSecure() {return false;}
    public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {}

    private ServletInputStreamAdapter is;
    private Map<String, String[]> parameterMap;
    private Map<String, Object> attributeMap;
    private Map<String, String> headerMap;
}
