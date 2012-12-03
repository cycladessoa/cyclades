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
package org.cyclades.engine.nyxlet.templates.stroma.actionhandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

public class ActionHandlerTest {

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
    public void parameterAsBoolean_test () throws Exception {
        final String key = "am_i_true";
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        // Basic true case
        parameters.put(key, Arrays.asList(new String[]{""}));
        if (!ActionHandler.parameterAsBoolean(key, parameters, false)) errorCollector.addError(new AssertionError("Failed basic true case (empty value)"));
        // False due to forcing value, would be true otherwise
        parameters.put(key, Arrays.asList(new String[]{""}));
        if (ActionHandler.parameterAsBoolean(key, parameters, true)) errorCollector.addError(new AssertionError("Failed basic false case (empty value and requireValue true)"));
        // False due to forcing value, and using non-true value
        parameters.put(key, Arrays.asList(new String[]{"false"}));
        if (ActionHandler.parameterAsBoolean(key, parameters, false)) errorCollector.addError(new AssertionError("Failed basic false case (non empty value and requireValue false)"));
        // False due to forcing value, and using non-true value
        parameters.put(key, Arrays.asList(new String[]{"xxxfalsexxx"}));
        if (ActionHandler.parameterAsBoolean(key, parameters, false)) errorCollector.addError(new AssertionError("Failed basic false case (non empty value and requireValue false)"));
        // False due to forcing value, and using non-true value
        parameters.put(key, Arrays.asList(new String[]{"xxxfalsexxx"}));
        if (ActionHandler.parameterAsBoolean(key, parameters, true)) errorCollector.addError(new AssertionError("Failed basic false case (non empty value and requireValue true)"));
        // True due to forcing value, and using true value
        parameters.put(key, Arrays.asList(new String[]{"true"}));
        if (!ActionHandler.parameterAsBoolean(key, parameters, true)) errorCollector.addError(new AssertionError("Failed basic true case (non empty value and requireValue true)"));
        // True due true value
        parameters.put(key, Arrays.asList(new String[]{"true"}));
        if (!ActionHandler.parameterAsBoolean(key, parameters, false)) errorCollector.addError(new AssertionError("Failed basic true case (non empty value and requireValue false)"));
        // True due true value
        parameters.put(key, Arrays.asList(new String[]{"TrUe"}));
        if (!ActionHandler.parameterAsBoolean(key, parameters, false)) errorCollector.addError(new AssertionError("Failed basic true case (non empty value and requireValue false, TrUe)"));
        // True due true value
        parameters.put(key, Arrays.asList(new String[]{"TrUe"}));
        if (!ActionHandler.parameterAsBoolean(key, parameters, true)) errorCollector.addError(new AssertionError("Failed basic true case (non empty value and requireValue true, TrUe)"));
    }

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();
}