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

import org.apache.log4j.Logger;

class SimpleHTMLPage {

    static Logger logger = Logger.getLogger(SimpleHTMLPage.class);

    public SimpleHTMLPage (String title, String bgColor, String textColor, String bg) {
        this.sb.append("<HTML><TITLE>");
        this.sb.append(title);
        this.sb.append("</TITLE><BODY BGCOLOR=");
        this.sb.append(bgColor);
        this.sb.append(" TEXT=");
        this.sb.append(textColor);
        this.sb.append(" background=\""+bg+"\"");
        //this.sb.append(" background=\"/cyclades?action=background");
        this.sb.append(">");
    }

    public SimpleHTMLPage append (String string) {
        this.sb.append(string);
        return this;
    }

    public SimpleHTMLPage append (int intVal) throws Exception {
        final String eLabel = "HTMLPage.append: ";
        if (this.done) {
            throw new Exception (eLabel + "HTML committed.");
        }
        this.sb.append(intVal);
        return this;
    }

    public SimpleHTMLPage append (float floatVal) throws Exception {
        final String eLabel = "HTMLPage.append: ";
        if (this.done) {
            throw new Exception (eLabel + "HTML committed.");
        }
        this.sb.append(floatVal);
        return this;
    }

    public SimpleHTMLPage append (byte[] bytes) throws Exception {
        final String eLabel = "HTMLPage.append: ";
        if (this.done) {
            throw new Exception (eLabel + "HTML committed.");
        }
        this.sb.append(bytes);
        return this;
    }

    public String getHTML () {
        if (!this.done) {
            this.done = true;
            this.sb.append("</BODY></HTML>");
        }
        return this.sb.toString();
    }

    StringBuilder sb = new StringBuilder();
    boolean done = false;
}
