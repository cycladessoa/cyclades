/*******************************************************************************
 * Copyright (c) 2013, THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY
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
package org.cyclades.engine.validator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.cyclades.engine.NyxletSession;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class NoJSONValidatorTest {

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
        public void noJSONSuccessTest() throws Exception {
            final Map<String, List<String>> parameters = new HashMap<String, List<String>>();
            parameters.put(NyxletSession.DATA_TYPE_PARAMETER, Arrays.asList(new String[]{"xml"}));
            parameters.put(NyxletSession.RESPONSE_DATA_TYPE_PARAMETER, Arrays.asList(new String[]{"xml"}));
            final NyxletSession nyxletSession = new NyxletSession(parameters, null, null);
            final ValidationFaultElement vfe = new NoJSON().validate(nyxletSession, parameters);
            if (vfe != null) {
                    errorCollector.addError(new AssertionError("Error in verification: " + vfe));
            }
        }
        
        @Test
        public void noJSONFailureTest1() throws Exception {
            final Map<String, List<String>> parameters = new HashMap<String, List<String>>();
            parameters.put(NyxletSession.DATA_TYPE_PARAMETER, Arrays.asList(new String[]{"json"}));
            parameters.put(NyxletSession.RESPONSE_DATA_TYPE_PARAMETER, Arrays.asList(new String[]{"xml"}));
            final NyxletSession nyxletSession = new NyxletSession(parameters, null, null);
            final ValidationFaultElement vfe = new NoJSON().validate(nyxletSession, parameters);
            if (vfe == null) {
                    errorCollector.addError(new AssertionError(
                            "Error in verification, validator should have triggered error"));
            }
        }
        
        @Test
        public void noJSONFailureTest2() throws Exception {
            final Map<String, List<String>> parameters = new HashMap<String, List<String>>();
            parameters.put(NyxletSession.DATA_TYPE_PARAMETER, Arrays.asList(new String[]{"xml"}));
            parameters.put(NyxletSession.RESPONSE_DATA_TYPE_PARAMETER, Arrays.asList(new String[]{"json"}));
            final NyxletSession nyxletSession = new NyxletSession(parameters, null, null);
            final ValidationFaultElement vfe = new NoJSON().validate(nyxletSession, parameters);
            if (vfe == null) {
                    errorCollector.addError(new AssertionError(
                            "Error in verification, validator should have triggered error"));
            }
        }

        @Rule
        public ErrorCollector errorCollector = new ErrorCollector();
        
}
