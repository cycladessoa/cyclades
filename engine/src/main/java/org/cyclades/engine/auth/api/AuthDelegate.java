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
package org.cyclades.engine.auth.api;

import java.util.Map;
import javax.servlet.ServletConfig;

import org.cyclades.engine.exception.AuthException;

/**
 * Interface intended to be called as an external auth hook for
 * clients of this framework. Please see the class GenericAuthDelegate for an
 * example of how to create an implementing class. 
 * 
 * Create a custom AuthDelegate to plug in a common auth model into the framework,
 * and across loaded nyxlets.
 * 
 * XXX - Do not declare any constructors for your class...reflection will be used to 
 * instantiate a concrete representation of this interface using an empty constructor.
 *
 */
public interface AuthDelegate {
    public void initialize (ServletConfig config) throws Exception;
    public void destroy () throws Exception;
    /**
     * Auth a session based on parameters passed in.
     * 
     * @param authParameterMap  Values/parameters to use for authenticating/authorizing a user/session
     * @param authMetaObject    An Object representation of any supplemental data regarding the auth itself
     * 
     * XXX - the difference between the above method parameters is the first parameter contains the actual 
     *  parameters (in a Map) that will be used to actually authenticate against...the second parameter is 
     *  an object that may be used as a strategy for the auth logic.
     *  
     * 
     * @return                  An object (specific to desired auth implementation) that will 
     *                                  be returned and cached on valid auth attempt. An authorization error
     *                                  is implied if null is returned..however, an exception should be thrown
     *                                  with the correct code for any error.
     * @throws AuthException    Thrown if auth fails. Don't forget to include correct code 
     *                                                  for your application.
     */
    public Object auth(Map authParameterMap, Object authMetaObject) throws AuthException;
}
