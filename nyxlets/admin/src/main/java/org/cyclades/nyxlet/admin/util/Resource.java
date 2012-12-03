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
package org.cyclades.nyxlet.admin.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.util.TransactionIdentifier;
import org.cyclades.io.FileUtils;
import org.cyclades.io.ResourceRequestUtils;
import org.cyclades.xml.parser.api.XMLGeneratedObject;
import com.google.common.io.ByteStreams;
import org.w3c.dom.Node;

public class Resource {

    /**
     * Extract the resource path from a Nyxlet request. The algorithm is as follows:
     *
     * 1.) If the parameter uri exists, return its value
     * 2.) If valid URI input exists, return its value as a URI string
     * 3.) Return null if there is no valid path source detected
     *
     * @param nyxletSession     The request NyxletSession
     * @param baseParameters    The request base parameters
     * @param offset            The offset from which to begin creating the path. If from a typical service invocation, this
     *                          would be 1, if from a RRD request, this may be 0.
     * @return resource path
     * @throws Exception
     */
    public static String getRequestResourcePath (NyxletSession nyxletSession, Map<String, List<String>> baseParameters, int offset) throws Exception {
        if (baseParameters.containsKey(URI_FIELD)) return baseParameters.get(URI_FIELD).get(0);
        String webServiceRequest = nyxletSession.getRequestPathInfo();
        if (webServiceRequest != null && webServiceRequest.length() > 1) {
            String[] URIParts = webServiceRequest.split("/");
            if (URIParts.length > 1 && offset < URIParts.length - 1) {
                StringBuilder sb = new StringBuilder();
                int loopCount = 0;
                for (int i = offset + 1; i < URIParts.length; i++) {
                    if (loopCount++ > 0) sb.append("/");
                    sb.append(URIParts[i]);
                }
                return sb.toString();
            }
        }
        return null;
    }

    public static void writeResource (String path, byte[] data) throws Exception {
        final String eLabel = "ResourceUtils.writeResource: ";
        try {
            String tempPath = getDocumentTempPath(path);
            FileUtils.verifyFileOutputDirectory(tempPath);
            FileUtils.writeToFile(data, tempPath);
            renameResource(tempPath, path);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public static void writeResourceEnhancedXML (String path, Node node) throws Exception {
        final String eLabel = "ResourceUtils.writeResourceEnhancedXML: ";
        OutputStream os = null;
        try {
            String tempPath = getDocumentTempPath(path);
            FileUtils.verifyFileOutputDirectory(tempPath);
            os = new FileOutputStream(new File(tempPath));
            XMLGeneratedObject.writeToStreamResult(new DOMSource(node), new StreamResult(os), true);
            os.flush();
            os.close(); // Make sure it is closed before renaming
            renameResource(tempPath, path);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        } finally {
            try { os.close(); } catch (Exception e) {}
        }
    }

    public static void writeResource (String path, InputStream is) throws Exception {
        final String eLabel = "ResourceUtils.writeResource: ";
        OutputStream os = null;
        try {
            String tempPath = getDocumentTempPath(path);
            FileUtils.verifyFileOutputDirectory(tempPath);
            os = new FileOutputStream(new File(tempPath));
            ByteStreams.copy(is, os);
            os.close(); // Make sure it is closed before renaming
            renameResource(tempPath, path);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        } finally {
            try { os.close(); } catch (Exception e) {}
        }
    }

    public static void writeResource (String path, String inputURI) throws Exception {
        final String eLabel = "ResourceUtils.writeResource: ";
        InputStream is = null;
        try {
            is = ResourceRequestUtils.getInputStream(inputURI, null);
            writeResource(path, is);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        } finally {
            try { is.close(); } catch (Exception e) {}
        }
    }

    public static void readResource (String path, OutputStream os) throws Exception {
        final String eLabel = "ResourceUtils.readResource: ";
        InputStream is = null;
        try {
            is = ResourceRequestUtils.getInputStream(path, null);
            ByteStreams.copy(is, os);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        } finally {
            try { is.close(); } catch (Exception e) {}
        }
    }

    public static String getDocumentTempPath (String path) {
        return new StringBuilder(path).append(".").append(transactionIdentifier.getTransactionID()).toString();
    }

    public static void renameResource (String from, String to) throws Exception {
        if (!new File(from).renameTo(new File(to))) throw new Exception("Failed to rename file [" + from + "] to [" + to + "]");
    }

    public static final String URI_FIELD            = "uri";
    public static final String MAP_CHANNEL_OBJECT   = "binary";
    public final static String PAYLOAD_PARAMETER    = "payload";
    public final static String SOURCE_PARAMETER     = "source";
    private static TransactionIdentifier transactionIdentifier = null;
    static {
        try { transactionIdentifier = new TransactionIdentifier("admin-"); } catch (Exception e) {}
    }
}
