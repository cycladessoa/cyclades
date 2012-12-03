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
package org.cyclades.engine.util;

/**
 * This class provides a mechanism for retrieving a unique ID. Each instantiation of this class
 * should have a unique prefix as to avoid an ID collisions on the same server.
 */
public class TransactionIdentifier {

    /**
     * Constructor with prefix to add to the IDs returned from this Object
     *
     * @param prefix    The String that will be in the front of this ID. Each TransactionIdentifier
     *                  instantiated should have a unique prefix to preserve uniqueness.
     *
     * @throws Exception
     */
    public TransactionIdentifier (String prefix) throws Exception {
        final String eLabel = "TransactionIdentifier.TransactionIdentifier: ";
        try {
            StringBuilder prefixBuilder = new StringBuilder((prefix == null) ? "" : prefix);
            prefixBuilder.append(getShortHostName(java.net.InetAddress.getLocalHost().getHostName()));
            this.prefix = prefixBuilder.toString();
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private String getShortHostName (String longHostName) {
        return longHostName.split("[.]").clone()[0];
    }

    /**
     * Get the unique transaction ID
     *
     * @return transaction ID
     */
    public String getTransactionID () {
        return new StringBuilder(prefix).append("-").append(getTransactionIDStamp()).toString();
    }

    /**
     * Get the timestamp used as the base of this ID.
     *
     * XXX - It is not recommended to use this directly as there can be ambiguous IDs introduced
     *
     * @return timestamp
     */
    public synchronized long getTransactionIDStamp () {
        long currentStamp = System.currentTimeMillis();
        if (currentStamp <= stamp) currentStamp = stamp + 1;
        stamp = currentStamp;
        return currentStamp;
    }

    private long stamp = -1;
    private final String prefix;
}
