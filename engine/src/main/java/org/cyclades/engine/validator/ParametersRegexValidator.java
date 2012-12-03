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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * ParametersRegexValidator checks that parameter values
 * exist and that they conform to a specified regular
 * expression pattern.
 */
public class ParametersRegexValidator extends ParameterHasValue {
        private Pattern pattern = null;

        /**
         * Basic constructor
         *
         * @param key   The key to validate
         */
        public ParametersRegexValidator(String key) {
            super(key);
        }

        /**
         * Basic constructor
         *
         * @param key       The key to validate
         * @param terminal  if true, validation will terminate on this validator
         */
        public ParametersRegexValidator(String key, boolean terminal) {
            super(key, terminal);
        }

        /**
         * setPattern sets the pattern to validate against
         * @param regex a regular expression string compatible with java.util.regex.Pattern.
         * @return this
         * @throws PatternSyntaxException if the pattern not compatible with java.util.regex.Pattern.
         */
        public ParametersRegexValidator setPattern(final String regex) throws PatternSyntaxException {
                this.pattern = Pattern.compile(regex);
                return this;
        }

        /**
         * setPattern sets the pattern to validate against (not making a clone)
         * @param pattern pattern to validate against (the value is not copied or cloned)
         * @return this
         */
        public ParametersRegexValidator setPattern(final Pattern pattern) {
                this.pattern = pattern;
                return this;
        }

        @Override
        public ValidationFaultElement validate (List<String> values) throws Exception {
                final ValidationFaultElement vfe = super.validate(values);
                if (vfe != null) {
                        return vfe;
                }
                for (String v : values) {
                        if (!this.pattern.matcher(v).matches()) {
                                final StringBuilder sb = new StringBuilder("Parameter does not match regex: ").append(this.pattern.toString()).append(": ").append(v);
                                return new ValidationFaultElement(sb.toString());
                        }
                }
                return null;
            }
}
