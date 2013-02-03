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
package org.cyclades.engine.stroma;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cyclades.engine.MetaTypeEnum;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.exception.CycladesException;
import org.cyclades.engine.stroma.xstroma.VirtualizedSTROMARequest;

/**
 * Simple class to help make STROMA service calls
 */
public class STROMARequestHelper {

    /******************************************************************************/
    /***************** Non-Optimized Serializing Implementations ******************/
    /******************************************************************************/

    /**
     * Make a request to a service, local or remote. Make sure an entry for the target service exists in the
     * "registry" file for remote capabilities, otherwise the default will be local
     *
     * @param xstromaParameters Additional parameters for the servicebroker (see STROMA/X-STROMA spec)
     * @param stromaParameters  Parameters for the service request
     * @param metaType          The meta type for the request
     * @param serviceName       The name of the target service
     * @param data              The data section, or null if not applicable (see STROMA/X-STROMA spec)
     * @return the data portion of the response
     * @throws Exception
     */
    public static Object virtualRequest (Map<String, List<String>> xstromaParameters, Map<String, List<String>> stromaParameters, MetaTypeEnum metaType, String serviceName, String data) throws Exception {
        STROMAResponse stromaResponse = new VirtualizedSTROMARequest(xstromaParameters, metaType, serviceName, stromaParameters, data).execute();
        if (stromaResponse.getErrorCode() != 0) {
            throw new Exception(stromaResponse.getServiceName() + " " + stromaResponse.getAction() + " " + stromaResponse.getErrorCode() + " " + stromaResponse.getErrorMessage());
        }
        return stromaResponse.getData();
    }

    /**
     * Make a request to a local service.
     *
     * @param stromaParameters  Parameters for the service request
     * @param metaType          The meta type for the request
     * @param serviceName       The name of the target service
     * @param data              The data section, or null if not applicable (see STROMA/X-STROMA spec)
     * @return the data portion of the response
     * @throws Exception
     */
    public static Object request (Map<String, List<String>> stromaParameters, MetaTypeEnum metaType, String serviceName, String data) throws Exception {
        STROMAResponse stromaResponse = new STROMAServiceRequest(serviceName, metaType, stromaParameters, data).execute();
        if (stromaResponse.getErrorCode() != 0) {
            throw new Exception(stromaResponse.getServiceName() + " " + stromaResponse.getAction() + " " + stromaResponse.getErrorCode() + " " + stromaResponse.getErrorMessage());
        }
        return stromaResponse.getData();
    }

    /******************************************************************************/
    /******* Optimized Implementations Utilizing MapChannel Output ****************/
    /******************************************************************************/

    /**
     * Make a request to a local service, avoiding any serialization by returning the mapChannel Object of the
     * service requested directly.
     *
     * @param stromaParameters  Parameters for the service request
     * @param serviceName       The name of the target service
     * @param mapChannelKey     The key for the mapChannel return Object
     * @return the MapChannel Object
     * @throws Exception
     */
    public static Object requestGetMapChannel (Map<String, List<String>> stromaParameters, String serviceName, String mapChannelKey) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        NyxletSession ns = new NyxletSession(stromaParameters, null, os);
        ns.setChainsForward(true);
        STROMAServiceRequest.execute(serviceName, ns);
        STROMAResponse sr = STROMAResponse.fromBytes(ns.getMetaTypeEnum(), os.toByteArray());
        if (sr.getErrorCode() != 0) throw new CycladesException(sr.getErrorMessage(), (short)sr.getErrorCode());
        return ns.getMapChannelObject(mapChannelKey);
    }

    /******************************************************************************/
    /**** Optimized Implementations Utilizing MapChannel Input and Output *********/
    /******************************************************************************/

    /**
     * Make a request to a local service, avoiding any serialization by setting and returning the mapChannel Object of the
     * service requested directly.
     *
     * @param stromaParameters      Parameters for the service request
     * @param serviceName           The name of the target service
     * @param mapChannelKey         The key for the mapChannel input Object
     * @param mapChannelObject      The value for the mapChannel input Object
     * @param returMapChannelKey    The key for the mapChannel return Object
     * @return the MapChannel Object
     * @throws Exception
     */
    public static Object requestSetAndGetMapChannel (Map<String, List<String>> stromaParameters, String serviceName, String mapChannelKey, Object mapChannelObject, String returMapChannelKey) throws Exception {
        Map<Object, Object> mapChannel = new HashMap<Object, Object>();
        mapChannel.put(mapChannelKey, mapChannelObject);
        return requestSetAndGetMapChannel(stromaParameters, serviceName, mapChannel).get(returMapChannelKey);
    }
    
    /**
     * Make a request to a local service, avoiding any serialization by setting and returning the mapChannel Object of the
     * service requested directly.
     *
     * @param stromaParameters      Parameters for the service request
     * @param serviceName           The name of the target service
     * @param mapChannel            The Map to use as the Map Channel itself
     * @return the MapChannel as modified, if modified by the request
     * @throws Exception
     */
    public static Map<Object, Object> requestSetAndGetMapChannel (Map<String, List<String>> stromaParameters, String serviceName, Map<Object, Object> mapChannel) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        NyxletSession ns = new NyxletSession(stromaParameters, null, os);
        ns.setMapChannel(mapChannel);
        ns.setChainsForward(true);
        STROMAServiceRequest.execute(serviceName, ns);
        STROMAResponse sr = STROMAResponse.fromBytes(ns.getMetaTypeEnum(), os.toByteArray());
        if (sr.getErrorCode() != 0) throw new CycladesException(sr.getErrorMessage(), (short)sr.getErrorCode());
        return ns.getMapChannel();
    }

    /**
     * Make a request to a local service, avoiding any input serialization by setting the mapChannel Object of the
     * service requested directly.
     *
     * Nothing written to the OutputStream means error
     *
     * @param stromaParameters  Parameters for the service request
     * @param serviceName       The name of the target service
     * @param mapChannelKey     The key for the mapChannel input Object
     * @param mapChannelObject  The value for the mapChannel input Object
     * @param os                The OutputStream to write the service response
     * @throws Exception
     */
    public static void requestSetMapChannel (Map<String, List<String>> stromaParameters, String serviceName, String mapChannelKey, Object mapChannelObject, OutputStream os) throws Exception {
        NyxletSession ns = new NyxletSession(stromaParameters, null, os);
        ns.putMapChannelObject(mapChannelKey, mapChannelObject);
        STROMAServiceRequest.execute(serviceName, ns);
    }
    
    /**
     * Make a request to a local service, avoiding any input serialization by setting the mapChannel Object of the
     * service requested directly.
     *
     * Nothing written to the OutputStream means error
     *
     * @param stromaParameters  Parameters for the service request
     * @param serviceName       The name of the target service
     * @param mapChannel        The Map Channel itself
     * @param os                The OutputStream to write the service response
     * @throws Exception
     */
    public static void requestSetMapChannel (Map<String, List<String>> stromaParameters, String serviceName, Map<Object, Object> mapChannel, OutputStream os) throws Exception {
        NyxletSession ns = new NyxletSession(stromaParameters, null, os);
        ns.setMapChannel(mapChannel);
        STROMAServiceRequest.execute(serviceName, ns);
    }

    /******************************************************************************/
    /********************** Streaming Implementations *****************************/
    /******************************************************************************/

    /**
     * Make a request to a local service, returning a String representation of the service response
     *
     * @param stromaParameters  Parameters for the service request
     * @param metaType          The meta type for the request
     * @param serviceName       The name of the target service
     * @param data              The data section, or null if not applicable (see STROMA/X-STROMA spec)
     * @return the streamed response as a String
     * @throws Exception
     */
    public static String requestStreamedResponse (Map<String, List<String>> stromaParameters, MetaTypeEnum metaType, String serviceName, String data) throws Exception {
        final String eLabel = "STROMARequestHelper.requestStreamedResponse: ";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            requestStreamedResponse(stromaParameters, metaType, serviceName, data, baos);
            if (baos.size() < 1) throw new Exception("No data returned in service response stream");
            return String.valueOf(baos);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    /**
     * Make a request to a local service
     *
     * Nothing written to the OutputStream means error
     *
     * @param stromaParameters  Parameters for the service request
     * @param metaType          The meta type for the request
     * @param serviceName       The name of the target service
     * @param data              The data section, or null if not applicable (see STROMA/X-STROMA spec)
     * @param os                The OutputStream to write the service response
     * @throws Exception
     */
    public static void requestStreamedResponse (Map<String, List<String>> stromaParameters, MetaTypeEnum metaType, String serviceName, String data, OutputStream os) throws Exception {
        new STROMAServiceRequest(serviceName, metaType, stromaParameters, data).execute(os);
    }

}
