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

import java.util.Date;
import org.apache.log4j.Level;

public enum LoggingEnum {
    ERROR   (Level.ERROR),
    WARN    (Level.WARN),
    INFO    (Level.INFO),
    DEBUG   (Level.DEBUG);

    LoggingEnum (Level log4jLevel) {
        this.log4jLevel = log4jLevel;
    }

    public void log (LoggingEnum statementUsageLoggingLevel,
                     LogWriterInterface writer, String token, String entry) throws Exception {
        final String eLabel = "LoggingEnum.log: ";
        try {
            log(this, statementUsageLoggingLevel, null, writer, token, entry);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public void log (LoggingEnum statementUsageLoggingLevel,
                     LoggingEnum userUsageLoggingLevel, LogWriterInterface writer, String token, String entry) throws Exception {
        final String eLabel = "LoggingEnum.log: ";
        try {
            log(this, statementUsageLoggingLevel, userUsageLoggingLevel, writer, token, entry);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public void log (LoggingEnum baseUsageLoggingLevel, LoggingEnum statementUsageLoggingLevel,
                     LoggingEnum userUsageLoggingLevel, LogWriterInterface writer, String token, String entry) throws Exception {
        final String eLabel = "LoggingEnum.log: ";
        try {
            if (shouldLog(baseUsageLoggingLevel, statementUsageLoggingLevel, userUsageLoggingLevel)) {
                StringBuilder sb = new StringBuilder("[");
                sb.append((new Date()).toString());
                sb.append("] [");
                sb.append(token);
                sb.append("] ");
                sb.append(statementUsageLoggingLevel.toString());
                sb.append(" ");
                sb.append(entry);
                sb.append("\n");
                writer.write(sb.toString());
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public boolean shouldLog (LoggingEnum statementUsageLoggingLevel) throws Exception {
        final String eLabel = "LoggingEnum.shouldLog: ";
        try {
            return shouldLog(this, statementUsageLoggingLevel, null);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public boolean shouldLog (LoggingEnum statementUsageLoggingLevel, LoggingEnum userUsageLoggingLevel) throws Exception {
        final String eLabel = "LoggingEnum.shouldLog: ";
        try {
            return shouldLog(this, statementUsageLoggingLevel, userUsageLoggingLevel);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public boolean shouldLog (LoggingEnum baseUsageLoggingLevel, LoggingEnum statementUsageLoggingLevel, LoggingEnum userUsageLoggingLevel) throws Exception {
        final String eLabel = "LoggingEnum.shouldLog: ";
        try {
            if ((userUsageLoggingLevel != null && (userUsageLoggingLevel.ordinal() >= statementUsageLoggingLevel.ordinal())) ||
                (statementUsageLoggingLevel.ordinal() <= baseUsageLoggingLevel.ordinal())) {
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public static boolean shouldLogLog4jLevel (Level baseUsageLoggingLevel, Level statementUsageLoggingLevel, Level userUsageLoggingLevel) throws Exception {
        final String eLabel = "LoggingEnum.shouldLogLog4jLevel: ";
        try {
            if ((userUsageLoggingLevel != null && (statementUsageLoggingLevel.isGreaterOrEqual(userUsageLoggingLevel))) ||
                    (statementUsageLoggingLevel.isGreaterOrEqual(baseUsageLoggingLevel))) {
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public Level getLog4jLevel () {
        return log4jLevel;
    }

    private final Level log4jLevel;
}
