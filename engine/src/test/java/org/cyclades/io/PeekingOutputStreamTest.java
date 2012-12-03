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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class PeekingOutputStreamTest {
  
    @BeforeClass
    public static void setUpBefore() throws Exception {
    }

    @AfterClass
    public static void tearDownAfter() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void basic_functional_test () throws Exception {
        String originalStreamValue = "<helloworld>";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PeekingOutputStream peekingOutputStream = new PeekingOutputStream (baos, 'd') {
            public void peek (byte[] data) throws PeekException {
                String dataString = new String(data);
                if (!dataString.equals("<helloworld")) 
                    errorCollector.addError(new AssertionError("Bad peek data: " + dataString));
                System.out.println(dataString);
            }
        };
        peekingOutputStream.write(originalStreamValue.getBytes());
        String actuallyWritten = baos.toString();
        if (!actuallyWritten.equals(originalStreamValue)) 
            errorCollector.addError(new AssertionError("Bad Stream Value: " + actuallyWritten));
        System.out.println(actuallyWritten);
    }
    
    @Test
    public void parse_status_xml_test () throws Exception {
        String toWrite = "<response service=\"servicebroker\" error-code=\"2\"><blah/><blah/><blah></response>";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PeekingOutputStream peekingOutputStream = new PeekingOutputStream (baos, '>') {
            public void peek (byte[] data) throws PeekException {
                String dataString = new String(data);
                short status = Short.parseShort(dataString.substring(dataString.length() - 3, dataString.length() - 2));
                if (status != 2) errorCollector.addError(new AssertionError("Bad Status Value: " + status));
                System.out.println(String.valueOf(status));
            }
        };
        peekingOutputStream.write(toWrite.getBytes());
        String actuallyWritten = baos.toString();
        if (!actuallyWritten.equals(toWrite)) 
            errorCollector.addError(new AssertionError("Bad Stream Value: " + actuallyWritten));
        System.out.println(actuallyWritten);
    }
    
    @Test
    public void parse_status_json_test () throws Exception {
        String toWrite = "{\"error-code\":\"2\",\"service\":\"servicebroker\",\"error-code\":\"0\",\"data\":{}}";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PeekingOutputStream peekingOutputStream = new PeekingOutputStream (baos, ',') {
            public void peek (byte[] data) throws PeekException {
                String dataString = new String(data);
                short status = Short.parseShort(dataString.substring(dataString.length() - 3, dataString.length() - 2));
                if (status != 2) errorCollector.addError(new AssertionError("Bad Status Value: " + status));
                System.out.println(String.valueOf(status));
            }
        };
        peekingOutputStream.write(toWrite.getBytes());
        String actuallyWritten = baos.toString();
        if (!actuallyWritten.equals(toWrite)) 
            errorCollector.addError(new AssertionError("Bad Stream Value: " + actuallyWritten));
        System.out.println(actuallyWritten);
    }
    
    @Test
    public void json_peek_exception_test () throws Exception {
        String toWriteSuccess = "{\"error-code\":\"0\",\"service\":\"servicebroker\",\"error-code\":\"0\",\"data\":{}}";
        String toWriteError = "{\"error-code\":\"2\",\"service\":\"servicebroker\",\"error-code\":\"0\",\"data\":{}}";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            getPeekingOutputStream(baos).write(toWriteSuccess.getBytes());
        } catch (PeekException e) {
            errorCollector.addError(new AssertionError("Success condition should not throw error"));
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            getPeekingOutputStream(baos).write(toWriteError.getBytes());
            errorCollector.addError(new AssertionError("Error condition should throw error"));
        } catch (PeekException e) {
            // Good stuff!
            short code = e.getCode();
            System.out.println(String.valueOf(code));
            if (code != 2) errorCollector.addError(new AssertionError("Exception code should be 2"));
        }
        String actuallyWritten = baos.toString();
        if (actuallyWritten.length() != 0) 
            errorCollector.addError(new AssertionError("Error, there should have been nothing written to the stream on exception"));
    }
    
    private PeekingOutputStream getPeekingOutputStream (OutputStream os) {
        return new PeekingOutputStream (os, ',') {
            public void peek (byte[] data) throws PeekException {
                String dataString = new String(data);
                short status = Short.parseShort(dataString.substring(dataString.length() - 3, dataString.length() - 2));
                if (status != 0) throw new PeekException("Non-zero X-STROMA error-code returned", status);
            }
        };
    }
    
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();
}