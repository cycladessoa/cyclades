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
import java.util.Map;
import org.cyclades.engine.MetaTypeEnum;
import org.cyclades.engine.NyxletSession;
import org.json.JSONObject;
import org.w3c.dom.Node;

public abstract class FieldValidator {

    public abstract ValidationEnum getValidationType ();

    public FieldValidator () {}

    public FieldValidator (boolean terminal) {
        this.terminal = terminal;
    }

    public ValidationFaultElement validate (NyxletSession nyxletSession, Map<String, List<String>> parameters) throws Exception {
        final String eLabel = "FieldValidator.validate: ";
        try {
            return getValidationType().validate(this, nyxletSession, parameters);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    public boolean isTerminal () {
        return terminal;
    }

    static enum ValidationEnum  {
        ABSTRACT_VALIDATOR {
            public ValidationFaultElement validate (FieldValidator validator, NyxletSession nyxletSession, Map<String, List<String>> parameters) throws Exception {
                final String eLabel = "FieldValidator.ValidationEnum.ABSTRACT_VALIDATOR.validate: ";
                try {
                    return ((AbstractValidator)validator).validate(nyxletSession, parameters);
                } catch (Exception e) {
                    throw new Exception(eLabel + e);
                }
            }
        },
        PARAMETERS {
            public ValidationFaultElement validate (FieldValidator validator, NyxletSession nyxletSession, Map<String, List<String>> parameters) throws Exception {
                final String eLabel = "FieldValidator.ValidationEnum.PARAMETER_LIST.validate: ";
                try {
                    return ((ParametersValidator)validator).validate(parameters);
                } catch (Exception e) {
                    throw new Exception(eLabel + e);
                }
            }
        },
        PARAMETER_LIST {
            public ValidationFaultElement validate (FieldValidator validator, NyxletSession nyxletSession, Map<String, List<String>> parameters) throws Exception {
                final String eLabel = "FieldValidator.ValidationEnum.PARAMETER_LIST.validate: ";
                try {
                    ParameterListValidator plv = (ParameterListValidator)validator;
                    return plv.validate(parameters.get(plv.getKey()));
                } catch (Exception e) {
                    throw new Exception(eLabel + e);
                }
            }
        },
        JSON {
            public ValidationFaultElement validate (FieldValidator validator, NyxletSession nyxletSession, Map<String, List<String>> parameters) throws Exception {
                final String eLabel = "FieldValidator.ValidationEnum.JSON.validate: ";
                try {
                    if (!nyxletSession.getMetaTypeEnum().equals(MetaTypeEnum.JSON)) return null;
                    Object dataObject = nyxletSession.getDataObject();
                    return ((JSONValidator)validator).validate((dataObject == null) ? null : ((JSONObject)dataObject));
                } catch (Exception e) {
                    throw new Exception(eLabel + e);
                }
            }
        },
        XML {
            public ValidationFaultElement validate (FieldValidator validator, NyxletSession nyxletSession, Map<String, List<String>> parameters) throws Exception {
                final String eLabel = "FieldValidator.ValidationEnum.XML.validate: ";
                try {
                    if (!nyxletSession.getMetaTypeEnum().equals(MetaTypeEnum.XML)) return null;
                    Object dataObject = nyxletSession.getDataObject();
                    return ((XMLValidator)validator).validate((dataObject == null) ? null : ((Node)dataObject));
                } catch (Exception e) {
                    throw new Exception(eLabel + e);
                }
            }
        };
        public abstract ValidationFaultElement validate (FieldValidator validator, NyxletSession nyxletSession, Map<String, List<String>> parameters) throws Exception;
    };

    private boolean terminal = false;
}
