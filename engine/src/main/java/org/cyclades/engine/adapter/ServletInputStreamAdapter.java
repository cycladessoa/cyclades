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
import java.io.InputStream;

import javax.servlet.ServletInputStream;

import org.apache.log4j.Logger;

public class ServletInputStreamAdapter extends ServletInputStream {

    static Logger logger = Logger.getLogger(ServletInputStreamAdapter.class);

    public ServletInputStreamAdapter (InputStream is) {
        this.is = is;
    }

    @Override
    public int readLine(byte[] b, int off, int len) throws IOException {
        if (len <= 0) {
            return 0;
        }
        int count = 0, c;
        while ((c = this.is.read()) != -1) {
            b[off++] = (byte)c;
            count++;
            if (c == '\n' || count == len) {
                break;
            }
        }
        return count > 0 ? count : -1;
    }

    @Override
    public int available() throws IOException {
        return this.is.available();
    }

    @Override
    public void close() throws IOException {
        this.is.close();
    }

    @Override
    public synchronized void mark(int arg0) {
        this.is.mark(arg0);
    }

    @Override
    public boolean markSupported() {
        return this.is.markSupported();
    }

    @Override
    public int read(byte[] arg0, int arg1, int arg2) throws IOException {
        return this.is.read(arg0, arg1, arg2);
    }

    @Override
    public int read(byte[] arg0) throws IOException {
        return this.is.read(arg0);
    }

    @Override
    public synchronized void reset() throws IOException {
        this.is.reset();
    }

    @Override
    public long skip(long arg0) throws IOException {
        return this.is.skip(arg0);
    }

    @Override
    public int read() throws IOException {
        return this.is.read();
    }

    private InputStream is;
}
