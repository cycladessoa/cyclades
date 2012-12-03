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

/* commented out for now - cthai
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import com.google.common.io.ByteStreams;
*/
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
/* commented out for now - cthai
import java.io.InputStream;
*/

public class ResourceRequestUtilsTest {

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
    public void getHttpURLConnection_get_test () throws Exception {
        /* commented it out for now - cthai
        HttpURLConnection conn;
        conn = ResourceRequestUtils.getHttpURLConnection("http://services-dev-01.highwire.org:8080/cyclades/admin?action=whoami", "GET", null, null, -1, -1);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = null;
        try {
            is = conn.getInputStream();
            ByteStreams.copy(is, baos);
            int responseCode = conn.getResponseCode();
            System.out.println(responseCode);
            if (responseCode != 200) errorCollector.addError(new AssertionError("Bad response code: " + responseCode));
            System.out.println(new String(baos.toByteArray(), "UTF-8"));
        } finally {
            try { is.close(); } catch (Exception e) {}
        }
        */
    }

    @Test
    public void getHttpURLConnection_post_test () throws Exception {
        /* commented it out for now - cthai
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("content-type", "");
        HttpURLConnection conn;
        ByteArrayInputStream bais = new ByteArrayInputStream("test me!".getBytes());
        conn = ResourceRequestUtils.getHttpURLConnection("http://services-dev-01.highwire.org:8080/cyclades/restfs/junit.txt", "POST", bais, headers, -1, -1);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = null;
        try {
            is = conn.getInputStream();
            ByteStreams.copy(is, baos);
            int responseCode = conn.getResponseCode();
            System.out.println(responseCode);
            if (responseCode != 200) errorCollector.addError(new AssertionError("Bad response code: " + responseCode));
            System.out.println(new String(baos.toByteArray(), "UTF-8"));
        } finally {
            try { is.close(); } catch (Exception e) {}
        }
        */
    }

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();
}
