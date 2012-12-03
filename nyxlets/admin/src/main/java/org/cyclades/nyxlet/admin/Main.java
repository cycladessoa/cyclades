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
package org.cyclades.nyxlet.admin;

import org.cyclades.annotations.Nyxlet;
import org.cyclades.engine.exception.CycladesException;
import org.cyclades.engine.nyxlet.templates.stroma.STROMANyxlet;

@Nyxlet
public class Main extends STROMANyxlet {

    public Main () throws Exception {
        super();
    }

    /**
     * This method will be called in order to evaluate the health of the Nyxlet.
     *
     * An example implementation could be as the following:
     *  - ping a database to see if it is up, and/or under too much load
     *  - if the load is deemed to heavy for the system, or if it is down, one can return "false" to flag an unhealthy Nyxlet.
     *      Folks could monitor this state via the Cyclades "health" action and take external action on the issue.
     *  - if a developer wishes to "inactivate" this nyxlet, simply call "setActive(false)" prior to returning. Please
     *      remember to include a way to "activate" this Nyxlet again. For example, when the Cyclades "health" action
     *      is called again, and the Nyxlet is deemed healthy, call "setActive(true)" prior to returning. (The Nyxlet will
     *      return the error-code "3" indicating Nyxlet inactivity when it is in the inactive state).
     *
     * This mechanism is in place to help aid in fault detection, and try to avoid failure. It is up to the Nyxlet
     * designers to provide the right algorithm for the intended behavior. The implementation below is simply an
     * example of what can be done.
     *
     * @return true if healthy, false if otherwise
     * @throws CycladesException
     */
    @Override
    public boolean isHealthy () throws CycladesException {
        /***************************************************************************************/
        /** Each action handler can override the "isHealthy()" method. If desired this health **/
        /** check can be extended to include checking each ActionHandler individually         **/
        /** (calling super.isHealthy() as done below does this for you). If this is done      **/
        /** please be sure to override the "isHealthy()" method for each of your action       **/
        /** handlers to return something meaningful                                           **/
        /***************************************************************************************/
        if (super.isHealthy()) {
            // Recovery condition
            //setActive(true);
            return true;
        } else {
            //logError("Deactiviating the service");
            //setActive(false);
            return false;
        }
    }
}
