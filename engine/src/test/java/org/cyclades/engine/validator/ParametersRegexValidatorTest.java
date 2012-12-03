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

public class ParametersRegexValidatorTest {

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
        public void test_valid_uri() throws Exception {
                final NyxletSession nyxletSession = new NyxletSession(null, null, null, null);
                final Map<String, List<String>> parameters = new HashMap<String, List<String>>();
                final String parameterName = "test";
                parameters.put(parameterName, Arrays.asList(new String[]{
                                "abc",
                                "def",
                                "(fgh)",
                                "10",
                                "28",
                                "1001"}));
                final ParametersRegexValidator fv = new ParametersRegexValidator(parameterName, false);
                fv.setPattern("^(abc|def|\\(fgh\\)|[1-9][0-9]+)$");
                final ValidationFaultElement vfe = fv.validate(nyxletSession, parameters);
                if (vfe != null) {
                        errorCollector.addError(new AssertionError("Error in verification: " + vfe));
                }
        }

        @Test
        public void test_invalid_uri() throws Exception {
                final NyxletSession nyxletSession = new NyxletSession(null, null, null, null);
                final Map<String, List<String>> parameters = new HashMap<String, List<String>>();
                final String parameterName = "test";
                parameters.put(parameterName, Arrays.asList(new String[]{
                                "abc-def-ghi-124"}));
                final ParametersRegexValidator fv = new ParametersRegexValidator(parameterName, false);
                fv.setPattern("^abc-def-ghi-123$");
                final ValidationFaultElement vfe = fv.validate(nyxletSession, parameters);
                if (vfe == null) {
                        errorCollector.addError(new AssertionError("Error in verification: expected a failure and got a pass"));
                }
        }

        @Rule
        public ErrorCollector errorCollector = new ErrorCollector();
}
