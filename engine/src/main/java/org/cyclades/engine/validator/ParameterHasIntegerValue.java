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
 * This class validates that a parameter has a value and that it is an integer. API consumers
 * can specify a minimum and/or a maximum value to verify against.
 */
public class ParameterHasIntegerValue extends ParameterHasValue {

    /**
     * Basic constructor
     *
     * @param key   The key to validate
     */
    public ParameterHasIntegerValue(String key) {
        super(key);
    }

    /**
     * Basic constructor
     *
     * @param key       The key to validate
     * @param terminal  if true, validation will terminate on this validator
     */
    public ParameterHasIntegerValue(String key, boolean terminal) {
        super(key, terminal);
    }

    /**
     * Specify a minimum value (inclusive) to verify against.
     *
     * @param min
     * @return this
     */
    public ParameterHasIntegerValue setMin (int min) {
        this.min = min;
        return this;
    }

    /**
     * Specify a maximum value (inclusive) to verify against.
     *
     * @param max
     * @return this
     */
    public ParameterHasIntegerValue setMax (int max) {
        this.max = max;
        return this;
    }

    public ValidationFaultElement validate (List<String> values) throws Exception {
        // Let's reuse the already existing validation we extended
        ValidationFaultElement vfe = super.validate(values);
        if (vfe != null) return vfe;
        // We now are sure there is a value, let's verify it is an integer
        int value;
        try {
            value = Integer.parseInt(values.get(0));
        } catch (Exception e) {
            return new ValidationFaultElement("Invalid integer format: " + values.get(0));
        }
        // Verify the range settings
        if ((min != null && value < min) || (max != null && value > max)) {
            StringBuilder sb = new StringBuilder("Invalid integer value for parameter \"");
            sb.append(getKey()).append("\". Expected value between (inclusive) ");
            sb.append((min != null) ? min : Integer.MIN_VALUE).append(" and ").append((max != null) ? max : Integer.MAX_VALUE).append(", but got ").append(value);
            return new ValidationFaultElement(sb.toString());
        }
        return null;
    }

    private Integer min = null;
    private Integer max = null;
}