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
package org.cyclades.io;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class will encapsulate some general methods for IO that have been
 * found to be rewritten frequently.
 *
 */
public class StreamUtils {
    
    /**
     * Write from a URI to an OutpusStream
     * Note: It is the callers responsibility to close the OutputStream
     */
    public static void write (String URI, OutputStream out) throws Exception {
        write (URI, out, DEFAULT_BUFFER_SIZE);

    }

    /**
     * Write from a URI to an OutpusStream
     * Note: It is the callers responsibility to close the OutputStream
     *
     * @param URI
     * @param out
     * @param bufferSize - is now ignored
     * @throws Exception
     */
    public static void write (String URI, OutputStream out, int bufferSize) throws Exception {
        InputStream is = null;
        try {
            if (bufferSize < 1) throw new Exception("Invalid buffer size: " + bufferSize);
            is = ResourceRequestUtils.getInputStream(URI, null);
            write(is, out);
        } catch (Exception ex) {
            throw new Exception(ex);

        } finally {
            try {
                is.close();
            } catch (Exception ignore) {}
        }
    }
    
    /**
     * Write from an InputStream to an OutputStream
     * NOTE: It is the callers responsibility to close both streams
     * 
     * @param is
     * @param out
     * @throws Exception
     */
    public static void write (InputStream is, OutputStream out) throws Exception {
        write(is, out, DEFAULT_BUFFER_SIZE);
    }
    
    /**
     * Write from an InputStream to an OutputStream
     * NOTE: It is the callers responsibility to close both streams
     * 
     * @param is
     * @param out
     * @param bufferSize
     * @throws Exception
     */
    public static void write (InputStream is, OutputStream out, int bufferSize) throws Exception {
        final String eLabel = "StreamUtils.write(InputStream, OutputStream): ";
        try {
            if (bufferSize < 1) throw new Exception("Invalid buffer size: " + bufferSize);
            int bytesRead;
            byte[] buffer = new byte[bufferSize];
            while ((bytesRead = is.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }
    
    /**
     * Create and return a byte array from an InputStream
     * NOTE: It is the callers responsibility to close the InputStream
     * 
     * @param is
     * @return The byte array representation of the InputStream
     * @throws Exception
     */
    public static byte[] toByteArray (InputStream is) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        write(is, baos);
        return baos.toByteArray();
    }
    
    public static final int DEFAULT_BUFFER_SIZE = 1024;
    
}
