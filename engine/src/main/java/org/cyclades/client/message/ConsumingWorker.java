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
package org.cyclades.client.message;

import java.util.concurrent.Callable;
import org.cyclades.engine.nyxlet.templates.xstroma.target.ConsumerTarget;
import org.json.JSONObject;

public class ConsumingWorker implements Callable<Boolean> {
    
    public ConsumingWorker (String consumerClass, JSONObject consumerJSON, CompletableMessageProcessor mp, long maxWait) {
        this.consumerClass = consumerClass;
        this.consumerJSON = consumerJSON;
        this.cmp = mp;
        this.maxWait = maxWait;
    }
    
    /**
     * Returns true if this job exits due to completion, false if otherwise
     */
    public Boolean call() throws Exception {
        ConsumerTarget ct = null;
        try {
            ct = new ConsumerTarget(consumerClass, consumerJSON, cmp);
            long exitTime = System.currentTimeMillis() + maxWait;
            synchronized (cmp) {
                // Do we need this here as well: !Thread.currentThread().isInterrupted(), may be redundant
                while ((System.currentTimeMillis() < exitTime || maxWait < 0) && !cmp.done()) {
                    if (maxWait < 0) { 
                        cmp.wait(); 
                    } else { 
                        cmp.wait(exitTime - System.currentTimeMillis()); 
                    }
                }
            }  
            return Boolean.valueOf(cmp.done());
        } finally {
            try { ct.destroy(); } catch (Exception e) {}
        }
    }
    
    private final String consumerClass;
    private final JSONObject consumerJSON;
    private final CompletableMessageProcessor cmp;
    private final long maxWait; // Anything less than 0 will wait forever for cmp.done() to be true
    
}
