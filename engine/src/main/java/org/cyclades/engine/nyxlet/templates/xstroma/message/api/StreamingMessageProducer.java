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
package org.cyclades.engine.nyxlet.templates.xstroma.message.api;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Interface to implement in order to create a custom MessageProducer for the
 * ServiceBrokerNyxlet
 *
 * XXX - Empty constructor will be used to instantiate the Object representing this class
 *
 */
public interface StreamingMessageProducer extends MessageProducer {
    /**
     * Initialize this Message Producer using the initialization Map passed
     * in. This Map will be coming from the properties file entry for this
     * target.
     *
     * @param initializationMap The Map to use as an initialization mechanism for this Object
     * @throws Exception
     */
    //public void init (Map<String, String> initializationMap) throws Exception;

    /**
     * Destroy this MessageProducer
     *
     * @throws Exception
     */
    //public void destroy () throws Exception;

    /**
     * Send this message to the receiving/listening ServiceBrokerNyxlet instance.
     *
     * @param message
     * @param attributeMap
     * @param os
     * @throws Exception
     */
    public void sendMessage (String message, Map<String, List<String>> attributeMap, OutputStream os) throws Exception;

    //public boolean isHealthy () throws Exception;
}
