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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.cyclades.engine.MetaTypeEnum;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.api.Nyxlet;
import org.cyclades.engine.nyxlet.NyxletRepository;

import java.util.List;

/**
 * This class to be used when a local service request is to be made from within another service. For remote request
 * capabilities, please see the class VirtualizedSTROMARequest.
 */
public class STROMAServiceRequest {

    public STROMAServiceRequest (String serviceName, MetaTypeEnum metaTypeEnum, Map<String, List<String>> params, String data) throws Exception {
        final String eLabel = "STROMAServiceRequest.STROMAServiceRequest: ";
        try {
            this.serviceName = serviceName;
            if (params != null) this.parameters = params;
            this.metaTypeEnum = metaTypeEnum;
            this.data = data;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public STROMAResponse execute () throws Exception {
        final String eLabel = "STROMAServiceRequest.execute: ";
        try {
            if (parameters.containsKey("raw-response")) throw new Exception("Parameter \"raw-response\" permitted only in \"execute(OutputStream)\"");
            Nyxlet mod = NyxletRepository.getStaticInstance().getNyxlet(serviceName);
            if (mod == null) throw new Exception("Service not found: " + serviceName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            parameters.put("data-type", new ArrayList<String>(Arrays.asList(metaTypeEnum.name().toLowerCase())));
            if (data != null) parameters.put("data", new ArrayList<String>(Arrays.asList(data)));
            mod.process(parameters, null, baos);
            return STROMAResponse.fromBytes(metaTypeEnum, baos.toByteArray());
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    /**
     * Execute this request and stream the response to the OutputStream parameter
     *
     * @param os
     * @throws Exception
     */
    public void execute (OutputStream os) throws Exception {
        final String eLabel = "STROMAServiceRequest.execute(OutputStream): ";
        try {
            Nyxlet mod = NyxletRepository.getStaticInstance().getNyxlet(serviceName);
            if (mod == null) throw new Exception("Service not found: " + serviceName);
            parameters.put("data-type", new ArrayList<String>(Arrays.asList(metaTypeEnum.name().toLowerCase())));
            if (data != null) parameters.put("data", new ArrayList<String>(Arrays.asList(data)));
            mod.process(parameters, null, os);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    /**
     * Static method to give the caller a bit more control of the request by allowing a NyxletSession object
     * to be passed in. This can give the user the advantage of MapChannel control, etc. Please see the class
     * "Nyxlet" and the method "process" as an example of how to initially create a NyxletSession.
     *
     * TIP - An example of where this would be handy is when you simulate a "chained" request where the target
     * Nyxlet is set to "chain forward", placing its output in the MapChannel of the NyxletRequest passed in.
     * This can be accessed directly rather than parsing meta data (XML or JSON) redundantly. Of course the
     * Nyxlet you are calling needs to support that functionality.
     *
     * @param serviceName
     * @param nyxletSession
     * @throws Exception
     */
    public static void execute (String serviceName, NyxletSession nyxletSession) throws Exception {
        final String eLabel = "STROMAServiceRequest.execute(NyxletSession): ";
        try {
            Nyxlet mod = NyxletRepository.getStaticInstance().getNyxlet(serviceName);
            if (mod == null) throw new Exception("Service not found: " + serviceName);
            mod.process(nyxletSession);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public final static String ERROR_CODE       = "error-code";
    public final static String ERROR_MESSAGE    = "error-message";
    private Map<String, List<String>> parameters = new HashMap<String, List<String>>();
    private final MetaTypeEnum metaTypeEnum;
    private final String data;
    private final String serviceName;
}
