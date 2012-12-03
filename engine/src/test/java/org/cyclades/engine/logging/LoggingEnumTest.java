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
package org.cyclades.engine.logging;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

public class LoggingEnumTest {

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
    public void shouldLogLog4jLevel_test () throws Exception {
        // base/statement/user
        if (LoggingEnum.ERROR.shouldLog(LoggingEnum.DEBUG, null)) {
            errorCollector.addError(new AssertionError("DEBUG level should not log when base level is set to ERROR"));
        }
        if (LoggingEnum.shouldLogLog4jLevel(Level.ERROR, LoggingEnum.DEBUG.getLog4jLevel(), null)) {
            errorCollector.addError(new AssertionError("DEBUG level should not log when base level is set to ERROR"));
        }
        if (!LoggingEnum.shouldLogLog4jLevel(Level.DEBUG, LoggingEnum.DEBUG.getLog4jLevel(), null)) {
            errorCollector.addError(new AssertionError("DEBUG level should log when base level is set to DEBUG"));
        }
        if (!LoggingEnum.shouldLogLog4jLevel(Level.ERROR, LoggingEnum.ERROR.getLog4jLevel(), LoggingEnum.DEBUG.getLog4jLevel())) {
            errorCollector.addError(new AssertionError("DEBUG level should log when user specified"));
        }
        if (LoggingEnum.shouldLogLog4jLevel(Level.ERROR, LoggingEnum.WARN.getLog4jLevel(), LoggingEnum.ERROR.getLog4jLevel())) {
            errorCollector.addError(new AssertionError("ERROR user level should not log against a WARN statement level"));
        }
        if (LoggingEnum.shouldLogLog4jLevel(Level.ERROR, LoggingEnum.WARN.getLog4jLevel(), null)) {
            errorCollector.addError(new AssertionError("WARN statement level should not log against an ERROR base logging level"));
        }
    }

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();
}