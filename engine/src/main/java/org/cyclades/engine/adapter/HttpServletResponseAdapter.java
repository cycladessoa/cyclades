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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

@SuppressWarnings("deprecation")
public class HttpServletResponseAdapter implements HttpServletResponse {

    static Logger logger = Logger.getLogger(HttpServletResponseAdapter.class);

    public HttpServletResponseAdapter (OutputStream os) {
        this.os = new ServletOutputStreamAdapter(os);
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return this.os;
    }

    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(this.os);
    }

    public String getContentType() {return null;}
    public void setCharacterEncoding(String charset) {}
    public void addCookie(Cookie arg0) {}
    public void addDateHeader(String arg0, long arg1) {}
    public void addHeader(String arg0, String arg1) {}
    public void addIntHeader(String arg0, int arg1) {}
    public boolean containsHeader(String arg0) {return false;}
    public String encodeRedirectUrl(String arg0) {return null;}
    public String encodeRedirectURL(String arg0) {return null;}
    public String encodeUrl(String arg0) {return null;}
    public String encodeURL(String arg0) {return null;}
    public void sendError(int arg0, String arg1) throws IOException {}
    public void sendError(int arg0) throws IOException {}
    public void sendRedirect(String arg0) throws IOException {}
    public void setDateHeader(String arg0, long arg1) {}
    public void setHeader(String arg0, String arg1) {}
    public void setIntHeader(String arg0, int arg1) {}
    public void setStatus(int arg0, String arg1) {}
    public void setStatus(int arg0) {}
    public void flushBuffer() throws IOException {}
    public int getBufferSize() {return 0;}
    public String getCharacterEncoding() {return null;}
    public Locale getLocale() {return null;}
    public boolean isCommitted() {return false;}
    public void reset() {}
    public void resetBuffer() {}
    public void setBufferSize(int arg0) {}
    public void setContentLength(int arg0) {}
    public void setContentType(String arg0) {}
    public void setLocale(Locale arg0) {}

    private ServletOutputStreamAdapter os;
}
