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

import java.util.Properties;

@SuppressWarnings("serial")
public class XProperties extends Properties {

    public XProperties() {
        super();
    }

    public XProperties(Properties props) {
        super(props);
    }

    public String getPropertyOrError(String key, String... err) throws Exception {
        final String value = this.getProperty(key);
        if( value == null || value.trim().length() == 0 ) {
            if( err != null && err.length > 0 ) {
                throw new Exception(err[0]);
            } else {
                throw new Exception("Property ["+key+"] does not exist");
            }
        }
        return value.trim();
    } // end of getPropertyOrError(...)

    public String getProperyFromListOrError(String key, boolean mustExist, String... ofList) throws Exception {
        String value = null;
        if( mustExist ) {
            value = this.getPropertyOrError(key);
        } else {
            value = this.getProperty(key);
        }

        if( value != null && ofList != null && ofList.length > 0 ) {
            for( final String possibleValue : ofList ) {
                if( value.equals(possibleValue) ) {
                    return value;
                }
            }
            throw new Exception("Value ["+value+"] for key ["+key+"] is not acceptable");
        }
        return value;
    } // end of getProperyFromListOrError(...)

    public Integer getIntegerOrError(String key, String... err) throws Exception {
        final String value = this.getPropertyOrError(key, err);
        try { // ensure that it is valid integer
             return Integer.valueOf(value);
        } catch(Exception ex) {
            throw new Exception("Value of property ["+key+"] is not an integer");
        }
    } // end of getIntegerOrError(...)

} // end of XProperties class