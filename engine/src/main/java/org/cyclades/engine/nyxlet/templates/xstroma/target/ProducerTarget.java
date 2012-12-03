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
package org.cyclades.engine.nyxlet.templates.xstroma.target;

import org.cyclades.engine.exception.AuthException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.cyclades.engine.nyxlet.templates.xstroma.ServiceBrokerNyxletImpl;
import org.cyclades.engine.nyxlet.templates.xstroma.message.api.MessageProducer;
import org.cyclades.engine.util.MapHelper;
import org.cyclades.engine.NyxletSession;

public class ProducerTarget {

    /**
     * Constructor
     *
     * @param authenticationData    Data to use for the auth strategy
     * @param authDataForwarding    Automatically include auth data in requests if true
     * @param theClass              The class to instantiate as a service client (wrapped by this class)
     * @param targetInitData        Initialization data in the form of a JSONObject, straight from the config file
     * @param isLocal               Mark this target as local if true
     * @throws Exception
     */
    public ProducerTarget (String authenticationData, boolean authDataForwarding, String theClass, JSONObject targetInitData, boolean isLocal, ServiceBrokerNyxletImpl service)  throws Exception {
        final String eLabel = "ProducerTarget.ProducerTarget: ";
        try {
            this.authenticationData = authenticationData;
            this.authDataForwarding = authDataForwarding;
            this.theClass = theClass;
            this.isLocal = isLocal;
            if (!isLocal) {
                messageProducer = (MessageProducer)service.getClass().getClassLoader().loadClass(theClass).newInstance();
                messageProducer.init(MapHelper.mapFromMetaObject(targetInitData));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(eLabel + e);
        }
    }

    public void destroy () throws Exception {
        final String eLabel = "ProducerTarget.destroy: ";
        try {
            if (messageProducer != null) messageProducer.destroy();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public String getAuthenticationData () {
        return authenticationData;
    }

    public boolean forwardAuthData () {
        return authDataForwarding;
    }

    @SuppressWarnings("unchecked")
    public boolean auth (NyxletSession sessionDelegate) throws Exception {
        final String eLabel = "ProducerTarget.auth: ";
        try {
            if (authenticationData == null) return true;
            // Pass in all the query parameters from the request for auth...remember this is the broker, not a STROMA compliant service..so meta parameters
            // do not apply
            return (sessionDelegate.auth(sessionDelegate.getParameterMap(), authenticationData, false) != null);
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    /**
     * Get a map representation of any auth data we want to send over the wire for any reason. Right now, we will only
     * return one item in the map, the authDelegateObject as a String. This will be under the key "authDelegateObject".
     *
     * @param sessionDelegate
     * @return a map of <String>Lists
     * @throws Exception
     */
    public Map<String, List<String>> getAuthDataMap (NyxletSession sessionDelegate) throws Exception {
        final String eLabel = "ProducerTarget.getAuthDataMap: ";
        try {
            HashMap<String, List<String>> map = new HashMap<String, List<String>>();
            map.put(AUTH_DELEGATE_OBJECT, new ArrayList<String>(Arrays.asList(sessionDelegate.getAuthDelegateObject().toString())));
            return map;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public MessageProducer getMessageProducer () {
        return messageProducer;
    }

    /**
     * Load the client targets from a JSON string of the format:
     *
     * serviceClientTargets=[{"target":"localhost","local":"true"},{"target":"sample_http","authentication_data":"authenticate","auth_data_forwarding":"false","class":"org.cyclades.nyxlet.servicebrokernyxlet.message.producer.HTTPMessageProducer","target_init_data":{"uri":"http://localhost:8080/cycladesengine/servicebroker"}}]
     * serviceClientTargetAliases=[{"target":"localhost","aliases":["sample_alias_1","sample_alias_2"]}]
     *
     * @param clientTargetsJSONString
     * @return map of ProducerTargets
     * @throws Exception
     */
    public static Map loadTargets (String clientTargetsJSONString, String clientTargetAliasesJSONString, ServiceBrokerNyxletImpl service) throws Exception {
        final String eLabel = "ProducerTarget.loadTargets: ";
        try {
            Map<String, ProducerTarget> targetsMap = new HashMap<String, ProducerTarget>();
            JSONArray targets = new JSONArray(clientTargetsJSONString);
            JSONObject target;
            JSONArray targetAliasesArray = (clientTargetAliasesJSONString == null) ? null : new JSONArray(clientTargetAliasesJSONString);
            String targetauthenticationData;
            boolean isLocal;
            String className;
            JSONObject targetInitJSONObject;
            boolean forwardUserData;
            for (int i = 0; i < targets.length(); i++) {
                target = targets.getJSONObject(i);
                targetauthenticationData = (target.has(AUTHENTICATION_DATA)) ?  target.getString(AUTHENTICATION_DATA) : null;
                if (target.has(LOCAL) && target.getString(LOCAL).equalsIgnoreCase("true")) {
                    isLocal = true;
                    className = null;
                    targetInitJSONObject = null;
                    forwardUserData = false;
                } else {
                    isLocal = false;
                    className = target.getString(CLASS);
                    targetInitJSONObject = target.getJSONObject(TARGET_INITIALIZATION_DATA);
                    forwardUserData = (target.has(AUTH_DATA_FORWARDING)) ?  target.getString(AUTH_DATA_FORWARDING).equalsIgnoreCase("true") : false;
                }
                targetsMap.put(target.getString("target"), new ProducerTarget(targetauthenticationData, forwardUserData, className, targetInitJSONObject, isLocal, service));
            }
            if (targetAliasesArray != null) {
                JSONObject targetAliasObject;
                String targetName;
                JSONArray targetAliasArray;
                for (int i = 0; i < targetAliasesArray.length(); i++) {
                    targetAliasObject = targetAliasesArray.getJSONObject(i);
                    targetName = targetAliasObject.getString("target");
                    targetAliasArray = targetAliasObject.getJSONArray("aliases");
                    if (!targetsMap.containsKey(targetName)) throw new Exception("Target does not exist: " + targetName);
                    for (int j = 0; j < targetAliasArray.length(); j++) {
                        targetsMap.put(targetAliasArray.getString(j), targetsMap.get(targetName));
                    }
                }
            }
            return targetsMap;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public String toString () {
        return theClass;
    }

    public boolean isLocal () {
        return isLocal;
    }

    public boolean isHealthy () throws Exception {
        return ((isLocal) ? true : messageProducer.isHealthy());
    }

    private final String authenticationData;
    private final boolean authDataForwarding;
    private MessageProducer messageProducer;
    private final String theClass;
    boolean isLocal;
    private static final String AUTHENTICATION_DATA         = "authentication_data";
    private static final String AUTH_DATA_FORWARDING        = "auth_data_forwarding";
    private static final String AUTH_DELEGATE_OBJECT        = "authDelegateObject";
    private static final String CLASS                       = "class";
    private static final String TARGET_INITIALIZATION_DATA  = "target_init_data";
    private static final String LOCAL                       = "local";
}
