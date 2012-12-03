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

import java.io.CharConversionException;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

import org.apache.log4j.Logger;

public class ServletOutputStreamAdapter extends ServletOutputStream {

    static Logger logger = Logger.getLogger(ServletOutputStreamAdapter.class);

    public ServletOutputStreamAdapter (OutputStream os) {
        this.os = os;
    }

    @Override
    public void print(String s) throws IOException {
        if (s == null) s = "null";
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if ((c & 0xff00) != 0) { // high order byte must be zero
                throw new CharConversionException("err.not_iso8859_1");
            }
            this.os.write(c);
        }
    }

    @Override
    public void print(boolean arg0) throws IOException {
        this.print(arg0 ? "true" : "false");
    }

    @Override
    public void print(char arg0) throws IOException {
        this.print(String.valueOf(arg0));
    }

    @Override
    public void print(double arg0) throws IOException {
        this.print(String.valueOf(arg0));
    }

    @Override
    public void print(float arg0) throws IOException {
        this.print(String.valueOf(arg0));
    }

    @Override
    public void print(int arg0) throws IOException {
        this.print(String.valueOf(arg0));
    }

    @Override
    public void print(long arg0) throws IOException {
        this.print(String.valueOf(arg0));
    }

    @Override
    public void println() throws IOException {
        this.print("\r\n");
    }

    @Override
    public void println(boolean arg0) throws IOException {
        this.print(String.valueOf(arg0));
        this.println();

    }

    @Override
    public void println(char arg0) throws IOException {
        this.print(String.valueOf(arg0));
        this.println();
    }

    @Override
    public void println(double arg0) throws IOException {
        this.print(String.valueOf(arg0));
        this.println();
    }

    @Override
    public void println(float arg0) throws IOException {
        this.print(String.valueOf(arg0));
        this.println();
    }

    @Override
    public void println(int arg0) throws IOException {
        this.print(String.valueOf(arg0));
        this.println();
    }

    @Override
    public void println(long arg0) throws IOException {
        this.print(String.valueOf(arg0));
        this.println();
    }

    @Override
    public void println(String arg0) throws IOException {
        this.print(String.valueOf(arg0));
        this.println();
    }

    @Override
    public void close() throws IOException {
        this.os.close();
    }

    @Override
    public void flush() throws IOException {
        this.os.flush();
    }

    @Override
    public void write(byte[] arg0) throws IOException {
        this.os.write(arg0);
    }

    @Override
    public void write(byte[] arg0, int arg1, int arg2) throws IOException {
        this.os.write(arg0, arg1, arg2);
    }

    @Override
    public void write(int arg0) throws IOException {
        this.os.write(arg0);
    }

    private OutputStream os;
}
