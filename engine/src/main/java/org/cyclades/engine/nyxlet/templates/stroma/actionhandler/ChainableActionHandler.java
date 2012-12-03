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

import org.cyclades.engine.nyxlet.templates.stroma.STROMANyxlet;
import org.cyclades.engine.stroma.STROMAResponse;
import org.cyclades.engine.stroma.STROMAResponseWriter;
import org.cyclades.engine.NyxletSession;
import java.util.List;
import java.util.Map;

public abstract class ChainableActionHandler extends ActionHandler {

    public ChainableActionHandler(STROMANyxlet parentCommand) {
        super(parentCommand);
    }

    /**
     * This method will return a list of keys that will determine which one of the two handlers of this ChainableActionHandler
     * will be called. The following behavior can be expected:
     *
     * - returning null (default, no override) will NOT call handleMapChannel, handleSTROMAResponse
     *  will be called instead
     *
     * - returning a populated list (one or more items) will call handleMapChannel only if all of the items
     *  are found to exist as keys in the Map Channel, otherwise, handleSTROMAResponse will be called.
     *
     * - returning a non null BUT EMPTY list will always call handleMapChannel
     *
     * @param   nyxletSession NyxletSession of this request, in case a more detailed algorithm needs be utilized to determine the
     *          return value
     *
     * @return  An Object array bases on the rules above
     */
    public Object[] getMapChannelKeyTargets (NyxletSession nyxletSession) throws Exception {
        return null;
    }

    /**
     * Method called when there is Map Channel data to process by the handler (see event behavior in getMapChannelKeys comments).
     * Use sessionDelegate.getMapChannelObject([key]) to get map channel Objects. (See NyxletSession for all MapChannel related
     * methods)
     *
     * @param sessionDelegate
     * @param baseParameters
     * @param stromaResponseWriter
     * @throws Exception
     */
    public abstract void handleMapChannel (NyxletSession sessionDelegate, Map<String, List<String>> baseParameters, STROMAResponseWriter stromaResponseWriter) throws Exception;

    /**
     * Method called when the STROMAResponse from the port data is to be used as a chaining data channel mechanism. This is generally
     * considered the default entry point under normal circumstances. The STROMAResponse Object may be null, indicating there no data
     * present, or that it is not compatible.
     *
     * @param sessionDelegate
     * @param baseParameters
     * @param stromaResponseWriter
     * @param stromaResponse
     * @throws Exception
     */
    public abstract void handleSTROMAResponse (NyxletSession sessionDelegate, Map<String, List<String>> baseParameters, STROMAResponseWriter stromaResponseWriter, STROMAResponse stromaResponse) throws Exception;

    /**
     * Method to detect if a STROMAResponse is compatible. If not compatible, then "null" will be passed to handleSTROMAResponse.
     *
     * @param stromaResponse
     * @return true if STROMA compatible, false otherwise
     * @throws Exception
     */
    public abstract boolean isSTROMAResponseCompatible (STROMAResponse stromaResponse) throws Exception;

    public void handle (NyxletSession nyxletSession, Map<String, List<String>> baseParameters, STROMAResponseWriter stromaResponseWriter) throws Exception {
        final String eLabel = "ChainableActionHandler.handle: ";
        try {
            if (containsMapChannelKeys(nyxletSession, getMapChannelKeyTargets(nyxletSession))) {
                handleMapChannel(nyxletSession, baseParameters, stromaResponseWriter);
            } else {
                STROMAResponse stromaResponse = STROMAResponse.fromBytes(nyxletSession.getMetaTypeEnum(), nyxletSession.getPortData());
                stromaResponse = (stromaResponse == null) ? null : ((isSTROMAResponseCompatible(stromaResponse)) ? stromaResponse : null);
                handleSTROMAResponse(nyxletSession, baseParameters, stromaResponseWriter, stromaResponse);
            }
        } catch (Exception e) {
            handleException(nyxletSession, stromaResponseWriter, eLabel, e);
        }
    }

    private boolean containsMapChannelKeys (NyxletSession nyxletSession, Object[] keys) throws Exception {
        final String eLabel = "ChainableActionHandler.containsMapChannelKeys: ";
        try {
            if (keys == null) return false;
            if (keys.length == 0) return true;
            for (Object key : keys) {
                if (!nyxletSession.containsMapChannelKey(key)) return false;
            }
            return true;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }
}
