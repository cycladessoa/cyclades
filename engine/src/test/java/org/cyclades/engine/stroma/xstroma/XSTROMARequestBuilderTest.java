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
package org.cyclades.engine.stroma.xstroma;

import org.json.JSONObject;
import org.cyclades.engine.util.GenericXMLObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

public class XSTROMARequestBuilderTest {

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
    public void generateJSONString_test () throws Exception {
        XSTROMARequestBuilder xstromaBuilder = XSTROMARequestBuilder.newBuilder(null);
        xstromaBuilder.add(STROMARequestBuilder.newBuilder("helloworld").parameter("action", "sayhello").parameter("name", "tom").parameter("name", "nick").data("\"raw_json\":{\"l\":\"k\"}").build());
        xstromaBuilder.add(STROMARequestBuilder.newBuilder("helloworld").parameter("action", "sayhello").parameter("name", "tom2").parameter("name", "nick2").build());
        xstromaBuilder.add(STROMARequestBuilder.newBuilder("helloworld").parameter("action", "sayhello").parameter("name", "tom3").parameter("name", "nick3").build());
        xstromaBuilder.parameter("target", "localhost");
        xstromaBuilder.parameter("connection-timeout", "2000");
        String xstromaOutput = xstromaBuilder.build().toXSTROMAMessage();
        System.out.println(xstromaOutput);
        new JSONObject(xstromaOutput);
        String dataOutput = xstromaBuilder.build().generateData();
        System.out.println(dataOutput);
        new JSONObject(dataOutput);
        if (xstromaOutput.indexOf(dataOutput) < 0) errorCollector.addError(new AssertionError("Data output is not a substring of XSTROMA message output"));
    }
    
    @Test
    public void generateXMLString_test () throws Exception {
        XSTROMARequestBuilder xstromaBuilder = XSTROMARequestBuilder.newBuilder(null).xml();
        xstromaBuilder.add(STROMARequestBuilder.newBuilder("helloworld").parameter("action", "sayhello").parameter("name", "tom").parameter("name", "nick").data("<raw_xml>hi</raw_xml>").build());
        xstromaBuilder.add(STROMARequestBuilder.newBuilder("helloworld").parameter("action", "sayhello").parameter("name", "tom2").parameter("name", "nick2").build());
        xstromaBuilder.add(STROMARequestBuilder.newBuilder("helloworld").parameter("action", "sayhello").parameter("name", "tom3").parameter("name", "nick3").build());
        xstromaBuilder.parameter("target", "localhost");
        xstromaBuilder.parameter("connection-timeout", "2000");
        String xstromaOutput = xstromaBuilder.build().toXSTROMAMessage();
        System.out.println(xstromaOutput);
        new GenericXMLObject(xstromaOutput);
        String dataOutput = xstromaBuilder.build().generateData();
        System.out.println(dataOutput);
        new GenericXMLObject(dataOutput);
        if (xstromaOutput.indexOf(dataOutput) < 0) errorCollector.addError(new AssertionError("Data output is not a substring of XSTROMA message output"));
    }

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();
    
}