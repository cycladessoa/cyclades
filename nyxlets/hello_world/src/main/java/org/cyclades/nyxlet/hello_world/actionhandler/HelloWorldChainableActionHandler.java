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

package org.cyclades.nyxlet.hello_world.actionhandler;

import java.util.HashMap;
import java.util.List;
import org.cyclades.engine.nyxlet.templates.stroma.STROMANyxlet;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.nyxlet.templates.stroma.actionhandler.ChainableActionHandler;
import org.cyclades.xml.comparitor.XMLComparitor;
import org.cyclades.annotations.AHandler;
import org.cyclades.engine.stroma.STROMAResponse;
import org.cyclades.engine.stroma.STROMAResponseWriter;
import java.util.Map;

/**
 * This is an example of a minimalistic ChainableActionHandler. Although there are many ways to develop orchestration data
 * tranfer mechanisms within this framework, we provide a class to help simplify this with respect to Nyxlet/service chaining.
 * Keep in mind that the MapChannel mechanism can be utilized even without chained orchestration. Developers can easily
 * implement MapChannel and STROMAResponse communication by default for all chained service requests. A simple example of this would be
 * for a Nyxlet to always provide both a MapChannel representaion and serialization via the STROMAResponseWriter for every request.
 * This, of course, would be a bit wasteful and may negatively effect performance. Another possibility would be a parameter based
 * decision on what mechanism to utilize, such as a "use-map-channel" parameter. That being said, this is an example of how
 * to efficiently share data across chained service requests, and developers are encouraged to implement any creative solutions
 * they see fit to get the job done accordingly.
 *
 * This ActionHandler, when a member of a chained orchestration with itself, will output the original "Hello World..." message
 * along with an additional directive for each time a request is serviced (+MapChannel for requests serviced via the MapChannel
 * and +STROMAResponse otherwise)
 */
@AHandler({"sayhellochainable"})
public class HelloWorldChainableActionHandler extends ChainableActionHandler {

    public HelloWorldChainableActionHandler (STROMANyxlet parentNyxlet) {
        super(parentNyxlet);
    }

    @Override
    public void handleMapChannel (NyxletSession nyxletSession, Map<String, List<String>> baseParameters, STROMAResponseWriter stromaResponseWriter) throws Exception {
        String incomingStringValue = nyxletSession.getMapChannelObject(MAP_CHANNEL_KEY).toString() + " +MapChannel";
        handleLocal(nyxletSession, baseParameters, stromaResponseWriter, incomingStringValue);
    }

    @Override
    public void handleSTROMAResponse (NyxletSession nyxletSession, Map<String, List<String>> baseParameters, STROMAResponseWriter stromaResponseWriter, STROMAResponse stromaResponse) throws Exception {
        String incomingStringValue = null;
        if (stromaResponse != null) {
            incomingStringValue = getMessageFromSTROMAResponse(stromaResponse) + " +STROMAResponse";
        } else {
            incomingStringValue = "Hello World: ";
            if (baseParameters.containsKey("name")) {
                for (String name : baseParameters.get("name")) incomingStringValue = incomingStringValue + "[" + name + "] ";
            }
        }
        handleLocal(nyxletSession, baseParameters, stromaResponseWriter, incomingStringValue);
    }

    private void handleLocal (NyxletSession nyxletSession, Map<String, List<String>> baseParameters, STROMAResponseWriter stromaResponseWriter, String message) throws Exception {
        final String eLabel = "HelloWorldChainableActionHandler.handle: ";
        try {
            /********************************************************************/
            /*******                  START CODE BLOCK                    *******/
            /*******                                                      *******/
            /******* YOUR CODE GOES HERE...WITHIN THESE COMMENT BLOCKS.   *******/
            /******* MODIFYING ANYTHING OUTSIDE OF THESE BLOCKS WITHIN    *******/
            /******* THIS METHOD MAY EFFECT THE STROMA COMPATIBILITY      *******/
            /******* OF THIS ACTION HANDLER.                              *******/
            /********************************************************************/

            // We would prefer to have the data transported without serialization...so we'll set the map channel data accordingly here...
            // If we are chaining to another service, simply use the MapChannel, otherwise, write out to the stream. We could write to both
            // mechanisms, but it may be wasteful for large responses...so here's how to control that:
            // But first, for experimental purposes, let's allow a user to force serialization rather than using the optimized MapChannel:
            String forceSerializationiString = (baseParameters.containsKey(FORCE_SERIALIZATION_PARAM)) ? baseParameters.get(FORCE_SERIALIZATION_PARAM).get(0) : "false";
            boolean forceSerialization = forceSerializationiString.isEmpty() || forceSerializationiString.equalsIgnoreCase("true");
            if (nyxletSession.chainsForward() && !forceSerialization) {
                nyxletSession.putMapChannelObject(MAP_CHANNEL_KEY, message);
            } else {
                Map<String, String> responseMap = new HashMap<String, String>();
                responseMap.put("message", message);
                stromaResponseWriter.writeResponse(nyxletSession.getResponseMetaTypeEnum().getMetaStringFromMap(responseMap));
            }

            /********************************************************************/
            /*******                  END CODE BLOCK                      *******/
            /********************************************************************/
        } catch (Exception e) {
            getParentNyxlet().logStackTrace(e);
            handleException(nyxletSession, stromaResponseWriter, eLabel, e);
        } finally {
            stromaResponseWriter.done();
        }
    }

    /**
     * Extract the "message" field from the serialized output of the Nyxlet previous to this one in the chained orchestration.
     * The alternative to simply passing Objects via the MapChannel
     *
     * @param response The STROMAResponse of the previous Nyxlet in the chained orchestration.
     * @return The String representation of the "message" field in the STROMAResponse
     */
    private String getMessageFromSTROMAResponse (STROMAResponse response) throws Exception {
        final String eLabel = "HelloWorldChainableActionHandler.getMessageFromSTROMAResponse: ";
        try {
            return ((response.getData() instanceof Node) ?
                    XMLComparitor.getAttribute((Node)XMLComparitor.getMatchingChildNodes((Node)response.getData(), "map").firstElement(), "message") : ((JSONObject)response.getData()).getString("message"));
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    /**
     * Under what circumstances will we accept a serialized response from the previous Nyxlet in a chained orchestration?
     *
     * @param response The STROMResponse from the previous Nyxlet
     * @return true if acceptable, false if otherwise
     */
    @Override
    public boolean isSTROMAResponseCompatible (STROMAResponse response) throws UnsupportedOperationException {
        if (!response.getServiceName().equals("toms_hello_world") || !response.getAction().equals("sayhellochainable")) return false;
        return true;
    }

    /**
     * Under what circumstances will we bypass parsing the serialized output of the previous Nyxlet and utilize data in the MapChannel?
     * In this implementation, if the key "string" is detected in the MapChannel, we assume use of the MapChannel and avoid
     * creation of the STROMAResponse Object, which involves parsing the meta data output of the previous Nyxlet in the chained
     * orchestration. Avoiding serialization/deserializeation can drastically improve orchestration performance!
     *
     * See super for details.
     */
    @Override
    public Object[] getMapChannelKeyTargets (NyxletSession nyxletSession) {
        return new Object[]{MAP_CHANNEL_KEY};
    }

    private static final String MAP_CHANNEL_KEY                 = "string";
    private static final String FORCE_SERIALIZATION_PARAM       = "serialize";

}
