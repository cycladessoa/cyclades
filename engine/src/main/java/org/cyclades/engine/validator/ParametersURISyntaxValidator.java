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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * ParametersURISyntaxValidator validates that a parameter has a value
 * and that it conforms to syntax of a URI.
 */
public class ParametersURISyntaxValidator extends ParameterHasValue {

        /**
         * Basic constructor
         *
         * @param key   The key to validate
         */
        public ParametersURISyntaxValidator(String key) {
                super(key);
        }

        /**
         * Basic constructor
         *
         * @param key       The key to validate
         * @param terminal  if true, validation will terminate on this validator
         */
        public ParametersURISyntaxValidator(String key, boolean terminal) {
                super(key, terminal);
        }

        @Override
        public ValidationFaultElement validate (List<String> values) throws Exception {
                final ValidationFaultElement vfe = super.validate(values);
                if (vfe != null) {
                        return vfe;
                }
                for (String v : values) {
                        try {
                                new URI(v);
                        } catch(URISyntaxException e) {
                                final StringBuilder sb = new StringBuilder("Parameter is not a valid URI: ").append(v);
                                return new ValidationFaultElement(sb.toString());
                        }
                }
                return null;
            }
}
