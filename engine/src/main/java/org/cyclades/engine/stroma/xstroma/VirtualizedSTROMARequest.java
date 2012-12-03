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
package org.cyclades.engine.stroma.xstroma;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cyclades.engine.MetaTypeEnum;
import org.cyclades.engine.ResponseCodeEnum;
import org.cyclades.engine.api.Nyxlet;
import org.cyclades.engine.nyxlet.NyxletRepository;
import org.cyclades.engine.stroma.STROMAResponse;
import org.cyclades.io.PeekException;
import org.cyclades.io.PeekingOutputStream;

public class VirtualizedSTROMARequest {

    public VirtualizedSTROMARequest(Map<String, List<String>> xstromaParams,
                                    MetaTypeEnum metaTypeEnum,
                                    String serviceName,
                                    Map<String, List<String>> params,
                                    String data)
        throws Exception
    {
        this("servicebroker", xstromaParams, metaTypeEnum, serviceName, params, data);
    }

    public VirtualizedSTROMARequest (String brokerName, Map<String, List<String>> xstromaParams,
                                     MetaTypeEnum metaTypeEnum, String serviceName,
                                     Map<String, List<String>> params, String data)
        throws Exception
    {
        final String eLabel = "VirtualizedSTROMARequest.VirtualizedSTROMARequest: ";
        try {
            this.brokerName = brokerName;
            this.xstromaParams = xstromaParams;
            this.metaTypeEnum = metaTypeEnum;
            stromaRequest = new STROMARequest(serviceName, params, data);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public STROMAResponse execute () throws Exception {
        final String eLabel = "VirtualizedSTROMARequest.execute: ";
        try {
            Nyxlet nyxlet = NyxletRepository.getStaticInstance().getNyxlet(brokerName);
            if (nyxlet == null) throw new Exception("Service not found: " + brokerName);
            String[] targets = nyxlet.getEngineContext().getNyxletTargets(stromaRequest.getServiceName());
            String target;
            STROMAResponse stromaResponse = null;
            if (targets == null || targets.length == 0) {
                return executeXSTROMARequest(nyxlet, null);
            }

            for (int i = 0; i < targets.length; i++) {
                target = targets[i];
                stromaResponse = executeXSTROMARequest(nyxlet, target);
                if (stromaResponse.getErrorCode() != ResponseCodeEnum.SERVICE_NOT_FOUND.getCode()) break;
            }
            return stromaResponse;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private STROMAResponse executeXSTROMARequest (Nyxlet brokerNyxlet, String target) throws Exception {
        final String eLabel = "VirtualizedSTROMARequest.executeXSTROMARequest: ";
        try {
            Map<String, List<String>> stromaRequestParameters = new HashMap<String, List<String>>();
            stromaRequestParameters.putAll(xstromaParams);
            if (target != null) stromaRequestParameters.put("target", new ArrayList<String>(Arrays.asList(target)));
            XSTROMABrokerRequest xstromaRequest =  new XSTROMABrokerRequest(brokerName, metaTypeEnum, stromaRequestParameters);
            xstromaRequest.addSTROMARequest(stromaRequest);
            XSTROMABrokerResponse response = xstromaRequest.execute(brokerNyxlet);
            if (response.getErrorCode() != 0) {
                throw new Exception("Service Broker error: " + response.getErrorCode() + " " + response.getErrorMessage());
            }
            List<STROMAResponse> stromaResponseList = response.getResponses();
            if (stromaResponseList.size() != 1) throw new Exception("There should allways be exactly one service STROMAResponse");
            return stromaResponseList.get(0);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public void execute (OutputStream out) throws Exception {
        final String eLabel = "VirtualizedSTROMARequest.execute(OutputStream): ";
        try {
            Nyxlet nyxlet = NyxletRepository.getStaticInstance().getNyxlet(brokerName);
            if (nyxlet == null) throw new Exception("Service not found: " + brokerName);
            String[] targets = nyxlet.getEngineContext().getNyxletTargets(stromaRequest.getServiceName());
            String target;
            if (targets == null || targets.length == 0) {
                executeXSTROMARequest(nyxlet, null, out);
            } else {
                for (int i = 0; i < targets.length; i++) {
                    target = targets[i];
                    if (executeXSTROMARequest(nyxlet, target, out)) break;
                }
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private boolean executeXSTROMARequest (Nyxlet brokerNyxlet, String target, OutputStream out) throws Exception {
        final String eLabel = "VirtualizedSTROMARequest.executeXSTROMARequest(Nyxlet, String, OutputStream): ";
        try {
            Map<String, List<String>> stromaRequestParameters = new HashMap<String, List<String>>();
            stromaRequestParameters.putAll(xstromaParams);
            if (target != null) stromaRequestParameters.put("target", new ArrayList<String>(Arrays.asList(target)));
            XSTROMABrokerRequest xstromaRequest =  new XSTROMABrokerRequest(brokerName, metaTypeEnum, stromaRequestParameters);
            xstromaRequest.addSTROMARequest(stromaRequest);
            try {
                xstromaRequest.execute(brokerNyxlet, getSTROMAPeekingOutputStream(out, metaTypeEnum.equals(MetaTypeEnum.JSON) ? ',' : '>'));
            } catch (PeekException pe) {
                if (pe.getCode() == ResponseCodeEnum.SERVICE_NOT_FOUND.getCode()) return false;
            }
            return true;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private PeekingOutputStream getSTROMAPeekingOutputStream (OutputStream os, char delimiter) {
        return new PeekingOutputStream (os, delimiter) {
            public void peek (byte[] data) throws PeekException {
                String dataString = new String(data);
                short status = Short.parseShort(dataString.substring(dataString.length() - 3, dataString.length() - 2));
                if (status != 0) throw new PeekException("Non-zero STROMA error-code returned", status);
            }
        };
    }

    private final String brokerName;
    private final Map<String, List<String>> xstromaParams;
    private final MetaTypeEnum metaTypeEnum;
    private final STROMARequest stromaRequest;
}
