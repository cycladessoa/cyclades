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

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;

public class LogWriter implements LogWriterInterface {

    public LogWriter (String logDirectory, String logFileName) throws Exception {
        final String eLabel = "LogWriter.LogWriter: ";
        try {
            initWriter(logDirectory, logFileName, "yyyyMMdd");
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public LogWriter (String logDirectory, String logFileName, String dateFormat) throws Exception {
        final String eLabel = "LogWriter.LogWriter: ";
        try {
            initWriter(logDirectory, logFileName, dateFormat);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public void initWriter (String logDirectory, String logFileName, String dateFormat) throws Exception {
        final String eLabel = "LogWriterr.initWriter: ";
        try {
            this.logDirectory = logDirectory;
            this.logFileName = logFileName;
            simpleDate = new SimpleDateFormat(dateFormat);
            File directory = new File(logDirectory);
            //Make the directory
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    throw new Exception("Could not create directory: " + logDirectory);
                }
            }
            // Append to log files on startup
            makeLogFile(simpleDate.format(new Date()), true);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private synchronized void makeLogFile (String dateString, boolean appendToFile) throws Exception {
        final String eLabel = "LogWriter.makeLogFile: ";
        try {
            logToDateString = dateString;
            writerFile = new File(logDirectory, logFileName + "." + logToDateString);
            if (!writerFile.exists()) {
                if (!writerFile.createNewFile()) {
                    throw new Exception("Failed to create data file: " + writerFile.getName());
                }
            }
            writer = new FileWriter(writerFile, appendToFile);
        } catch (Exception e){
            throw new Exception(eLabel + e);
        }
    }

    public synchronized void write (String message) throws Exception {
        final String eLabel = "LogWriter.write: ";
        try {
            checkAndRollNewFileIfNecessary();
            writer.write(message);
            writer.flush();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public synchronized void writeLine (String message) throws Exception {
        final String eLabel = "LogWriter.writeLine: ";
        try {
            checkAndRollNewFileIfNecessary();
            writer.write(message);
            writer.write("\n");
            writer.flush();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public synchronized void writeDatedLine (String message, String delimeter) throws Exception {
        final String eLabel = "LogWriter.writeDatedLine: ";
        try {
            StringBuilder sb = new StringBuilder();
            sb.append((new Date()).getTime());
            sb.append(delimeter);
            sb.append(message);
            sb.append("\n");
            checkAndRollNewFileIfNecessary();
            writer.write(sb.toString());
            writer.flush();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    protected synchronized void checkAndRollNewFileIfNecessary () throws Exception {
        final String eLabel = "LogWriter.checkAndRollNewFileIfNecessary: ";
        try {
            String currentDateString = simpleDate.format(new Date());
            if (!currentDateString.equals(logToDateString)) {
                try {
                    writer.close();
                } catch (Exception e) {
                    logger.error(eLabel + e + "Error closing: " + writerFile.getName());
                }
                // Start from the beginning when rolling over as to clear an old file if one exists
                makeLogFile(currentDateString, false);
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public synchronized void close () throws Exception {
        final String eLabel = "LogWriter.close: ";
        try {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e) {
                if (logger != null) {
                    logger.error(eLabel + e);
                } else {
                    System.out.println(eLabel + e);
                }
            }
        } catch (Exception e){
            throw new Exception(eLabel + e);
        }
    }

    public void setLogger (Logger logger) {
        this.logger = logger;
    }

    private File writerFile;
    private String logToDateString;
    private String logDirectory;
    private FileWriter writer = null;
    private String logFileName;
    private SimpleDateFormat simpleDate;
    private Logger logger = null;
}
