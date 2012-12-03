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
 * This class validates that a parameter has a specified number of values. API consumers
 * can specify a minimum and/or a maximum value to verify against.
 */
public class ParameterHasNumValues extends ParameterListValidator {

    public ParameterHasNumValues(String key) {
        super(key);
    }

    public ParameterHasNumValues(String key, boolean terminal) {
        super(key, terminal);
    }

    public ParameterHasNumValues setMin (int min) {
        this.min = min;
        return this;
    }

    public ParameterHasNumValues setMax (int max) {
        this.max = max;
        return this;
    }

    @Override
    public ValidationFaultElement validate(List<String> parameters) throws Exception {
        int size = parameters.size();
        if ((min != null && size < min) || (max != null && size > max)) {
            StringBuilder sb = new StringBuilder("Invalid number of values for parameter \"");
            sb.append(getKey()).append("\". Expected number of values between (inclusive) ");
            sb.append((min != null) ? min : "(Unbounded Min)").append(" and ").append((max != null) ? max : "(Unbounded Max)").append(", but got ").append(size);
            return new ValidationFaultElement(sb.toString());
        }
        return null;
    }

    private Integer min = null;
    private Integer max = null;

}
