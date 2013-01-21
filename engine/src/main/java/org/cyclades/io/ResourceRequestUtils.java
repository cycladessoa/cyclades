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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * This class will encapsulate some general methods for IO that have been
 * found to be rewritten frequently.
 *
 */
public class ResourceRequestUtils {
    /**
     * Get the InputStream of the response. This will either be an HTTP
     * transaction or simply read from a file.
     *
     * @param URLString URL to connect to
     * @param data              The data to send if this is a POST, set as null otherwise
     * @return                  InputStream to read from. Make sure to close this in calling code
     * @throws Exception
     */
    public static InputStream getInputStream (String URLString, byte[] data) throws Exception {
        final String eLabel = "ResourceRequestUtils.getInputStream: ";
        try {
            return getInputStream(URLString, data, null);
        } catch (Exception e) {
            throw new Exception(eLabel + e + " " + URLString);
        }
    }

    /**
     * Get the InputStream of the response. This will either be an HTTP
     * transaction or simply read from a file.
     *
     * @param URLString                         URL to connecto to
     * @param data                              The data to send if this is a POST, set as null otherwise
     * @param headerProperties  Header properties to set if applicable. Null is acceptable.
     * @return                                  InputStream to read from. Make sure to close this in calling code
     * @throws Exception
     */
    public static InputStream getInputStream (String URLString, byte[] data, Map<String, String> headerProperties) throws Exception {
        final String eLabel = "ResourceRequestUtils.getInputStream: ";
        try {
            if (URLString.toLowerCase().startsWith("http")) {
                return getInputStreamHTTP(URLString, data, headerProperties, 0, 0);
            } else {
                return getInputStreamFile(URLString);
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e + " " + URLString);
        }
    }

    public static InputStream getInputStream (String URLString, byte[] data, Map<String, String> headerProperties,
            int connectionTimeoutInMS, int readTimeoutInMS) throws Exception {
        final String eLabel = "ResourceRequestUtils.getInputStream: ";
        try {
            if (URLString.toLowerCase().startsWith("http")) {
                return getInputStreamHTTP(URLString, data, headerProperties, connectionTimeoutInMS, readTimeoutInMS);
            } else {
                return getInputStreamFile(URLString);
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e + " " + URLString);
        }
    }

    /**
     * Get the InputStream from a HTTP response
     *
     * @param URLString                 The URL to connect to
     * @param data                              The data to send if this is a POST, set as null otherwise
     * @return                                  InputStream to read from. Make sure to close this in calling code
     * @throws Exception
     */
    public static InputStream getInputStreamHTTP (String URLString, byte[] data) throws Exception {
        final String eLabel = "ResourceRequestUtils.getInputStreamHTTP(String, byte[]): ";
        try {
            return getInputStreamHTTP(URLString, data, null);
        } catch (Exception e) {
            throw new Exception(eLabel + e + " " + URLString);
        }
    }

    /**
     * Get the InputStream from a HTTP response
     *
     * @param URLString                         The URL to connect to
     * @param data                              The data to send if this is a POST, set as null otherwise
     * @param headerProperties  Header properties to set if applicable. Null is acceptable.
     * @return                                  InputStream to this connection XXX -must close this in the calling code
     * @throws Exception
     */
    public static InputStream getInputStreamHTTP (String URLString, byte[] data, Map<String, String> headerProperties) throws Exception {
        return getInputStreamHTTP (URLString, data, headerProperties, 0, 0);
    }

    public static InputStream getInputStreamHTTP (String URLString, byte[] data, Map<String, String> headerProperties,
            int connectionTimeoutInMS, int readTimeoutInMS) throws Exception {
        final String eLabel = "ResourceRequestUtils.getInputStreamHTTP: ";
        OutputStream os = null;
        try {
            URL url = new URL(URLString);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            if (connectionTimeoutInMS > 0) conn.setConnectTimeout(connectionTimeoutInMS);
            if (readTimeoutInMS > 0) conn.setReadTimeout(readTimeoutInMS);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            if (headerProperties != null) {
                for (String prop : headerProperties.keySet()) {
                    conn.setRequestProperty(prop, headerProperties.get(prop));
                }
            }
            if (data != null) {
                conn.setRequestMethod("POST");
                os = conn.getOutputStream();
                os.write(data);
                os.flush();
            } else {
                conn.setRequestMethod("GET");
            }
            return conn.getInputStream();
        } catch (Exception e) {
            throw new Exception(eLabel + e + " " + URLString);
        } finally {
            try {
                os.close();
            } catch (Exception e) {}
        }
    }

    /**
     * Get the InputStream from an HTTP request.
     *
     * @param URLString                 The URL to connect to
     * @param method                    The HTTP method to use, defaults to POST if this is null
     * @param is                        The inputStream to write from. Nothing will be written if this is null.
     * @param headerProperties          Any header properties to include
     * @param connectionTimeoutInMS     Connection Timeout in milliseconds
     * @param readTimeoutInMS           Read Timeout in milliseconds
     * @return InputStream
     * @throws Exception
     */
    public static InputStream getInputStreamHTTP (String URLString, String method, InputStream is, Map<String, String> headerProperties,
            int connectionTimeoutInMS, int readTimeoutInMS) throws Exception {
        final String eLabel = "ResourceRequestUtils.getInputStreamHTTP: ";
        OutputStream os = null;
        try {
            URL url = new URL(URLString);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            if (connectionTimeoutInMS > 0) conn.setConnectTimeout(connectionTimeoutInMS);
            if (readTimeoutInMS > 0) conn.setReadTimeout(readTimeoutInMS);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            if (headerProperties != null) {
                for (String prop : headerProperties.keySet()) {
                    conn.setRequestProperty(prop, headerProperties.get(prop));
                }
            }
            conn.setRequestMethod((method == null) ? "POST" : method);
            if (is != null) {
                os = conn.getOutputStream();
                StreamUtils.write(is, os);
                os.flush();
            }
            if (conn.getResponseCode() != 200) throw new Exception("Invalid response code returned: " + conn.getResponseCode());
            return conn.getInputStream();
        } catch (Exception e) {
            throw new Exception(eLabel + e + " " + URLString);
        } finally {
            try {
                os.close();
            } catch (Exception e) {}
        }
    }

    /**
     * Make a HTTP request and return the HttpURLConnection to transfer more functionality to the user
     *
     * @param URLString                 The HTTP request string
     * @param method                    The HTTP method, i.e. GET, POST etc...
     * @param is                        The InputStream of the data to post..if any, or null
     * @param headerProperties          Any HTTP headers to include
     * @param connectionTimeoutInMS     The connection timeout in milliseconds
     * @param readTimeoutInMS           The read timeout in milliseconds
     * @return                          The HttpURLConnection of the connection/response
     * @throws Exception
     */
    public static HttpURLConnection getHttpURLConnection (String URLString, String method, InputStream is, Map<String, String> headerProperties,
            int connectionTimeoutInMS, int readTimeoutInMS) throws Exception {
        final String eLabel = "ResourceRequestUtils.getHttpURLConnection: ";
        OutputStream os = null;
        try {
            URL url = new URL(URLString);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            if (connectionTimeoutInMS > 0) conn.setConnectTimeout(connectionTimeoutInMS);
            if (readTimeoutInMS > 0) conn.setReadTimeout(readTimeoutInMS);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            if (headerProperties != null) {
                for (String prop : headerProperties.keySet()) {
                    conn.setRequestProperty(prop, headerProperties.get(prop));
                }
            }
            conn.setRequestMethod((method == null) ? "POST" : method);
            if (is != null) {
                os = conn.getOutputStream();
                StreamUtils.write(is, os);
                os.flush();
            }
            return conn;
        } catch (Exception e) {
            throw new Exception(eLabel + e + " " + URLString);
        } finally {
            try {
                os.close();
            } catch (Exception e) {}
        }
    }

    /**
     * Get the InputStream from a File
     *
     * @param URLString The URI to the file resource
     * @return                  The InputStream to read the file resource. Close this in calling code.
     * @throws Exception
     */
    public static InputStream getInputStreamFile (String URLString) throws Exception {
        final String eLabel = "ResourceRequestUtils.getInputStreamFile: ";
        OutputStream os = null;
        try {
            File file = new File(URLString);
            if ((!file.exists()) || (!file.isFile())) {
                throw new Exception("File resource does not exist: " + URLString);
            }
            return new FileInputStream(file);
        } catch (Exception e) {
            throw new Exception(eLabel + e + " " + URLString);
        } finally {
            try {
                os.close();
            } catch (Exception e) {}
        }
    }

    /**
     * Get the entire response in a byte array
     *
     * @param URLString The resource URL/URI to connect to
     * @param data              The data to send if this is a POST request
     * @return                  Byte array of the complete resource requested
     * @throws Exception
     */
    public static byte[] getData (String URLString, byte[] data) throws Exception {
        final String eLabel = "ResourceRequestUtils.getData: ";
        try {
            return getData(URLString, data, null, StreamUtils.DEFAULT_BUFFER_SIZE);
        } catch (Exception e) {
            throw new Exception(eLabel + e + " " + URLString);
        }
    }

    /**
     * Get the entire response in a byte[]
     *
     * @param URLString                 The resource URL/URI to connect to
     * @param data                              The data to send if this is a POST request
     * @param headerProperties  Any header properties to set if applicable
     * @param bufferSize                The preferred buffer size for this transaction
     * @return response bytes
     * @throws Exception
     */
    public static byte[] getData (String URLString, byte[] data, Map<String, String> headerProperties, int bufferSize) throws Exception {
        return getData (URLString, data, headerProperties, bufferSize, 0, 0);
    } // end of getData(...)

    public static byte[] getData (String URLString, byte[] data, Map<String, String> headerProperties, int bufferSize,
            int connectionTimeoutInMS, int readTimeoutInMS) throws Exception {
        InputStream is = null;
        try {
            is = getInputStream(URLString, data, headerProperties, connectionTimeoutInMS, readTimeoutInMS);
            return StreamUtils.toByteArray(is);
        } catch (Exception e) {
            throw new Exception(e + " " + URLString);

        } finally {
            try {
                is.close();
            } catch (Exception ignore) {}
        }
    } // end of getData(...)

    public static void main (String[] args) {
        if (args.length < 1) {
            System.out.println("cmd [-Dheaders] URI [POST data]");
            return;
        }
        String URLString = args[0];
        byte[] data = null;
        if (args.length > 1) {
            data = args[1].getBytes();
        }
        String headers = System.getProperty("headers");
        HashMap<String, String> headerMap = new HashMap<String, String>();
        if (headers != null) {
            String[] headerPairs = headers.split("[,]");
            String[] headerPair;
            for (String headerPairString : headerPairs) {
                headerPair = headerPairString.split("[:]");
                headerMap.put(headerPair[0], headerPair[1]);
            }
        }
        try {
            byte[] dataReturned = getData(URLString, data, headerMap, StreamUtils.DEFAULT_BUFFER_SIZE);
            System.out.println("SIZE:" + dataReturned.length);
            String outputString = new String(dataReturned);
            System.out.println("**********************************");
            System.out.println(outputString);
            System.out.println("**********************************");
        } catch (Exception e) {
            System.out.println("Main: " + e);
        }
    }
    
}
