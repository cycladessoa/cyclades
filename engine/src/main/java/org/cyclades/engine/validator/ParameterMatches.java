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
package org.cyclades.engine.validator;

import java.util.List;

/**
 * This class is a concrete implementation of ParameterListValidator that verifies
 * the following:
 *  * There is a value
 *  * The value is not null or an empty String
 */
public class ParameterMatches extends ParameterListValidator {

    public ParameterMatches(String key, String value) {
        super(key);
        this.value = value;
    }

    public ParameterMatches(String key, String value, boolean terminal) {
        super(key, terminal);
        this.value = value;
    }

    public ParameterMatches showValues (boolean showValuesBool) {
        this.showValuesBool = showValuesBool;
        return this;
    }

    public ValidationFaultElement validate (List<String> values) throws Exception {
        if (values == null || values.size() < 1) {
            StringBuilder sb = new StringBuilder("Parameter does not exist: ").append(getKey());
            return new ValidationFaultElement(sb.toString());
        } else if (!values.get(0).equals(value)) {
            StringBuilder sb = new StringBuilder("Parameter input value does not match designated value: name:[").append(getKey()).append("]");
            if (showValuesBool) sb.append(" designated:[").append(value).append("] ").append("actual:[").append(values.get(0)).append("]");
            return new ValidationFaultElement(sb.toString());
        }
        return null;
    }

    String value;
    boolean showValuesBool = true;
}
