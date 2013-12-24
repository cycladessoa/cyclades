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
import org.cyclades.engine.NyxletSession;

/**
 * This class will return the value of the "then" validator if the "if" validator
 * succeeds. This facilitates an "if then" capability with validators.
 */
public class IfThen extends AbstractValidator {

    public IfThen (FieldValidator ifValidator, FieldValidator thenValidator) {
        super();
        this.ifValidator = ifValidator;
        this.thenValidator = thenValidator;
    }

    public IfThen (FieldValidator ifValidator, FieldValidator thenValidator, boolean terminal) {
        super(terminal);
        this.ifValidator = ifValidator;
        this.thenValidator = thenValidator;
    }

    @Override
    public ValidationEnum getValidationType() {
        return ValidationEnum.ABSTRACT_VALIDATOR;
    }

    public ValidationFaultElement validate (NyxletSession nyxletSession, Map<String, List<String>> parameters) throws Exception {
        ValidationFaultElement vfe;
        try {
            vfe = ifValidator.validate(nyxletSession, parameters);
        } catch (Exception e) {
            vfe = new ValidationFaultElement(e);
        }
        if (vfe != null) return null;
        try {
            vfe = thenValidator.validate(nyxletSession, parameters);
        } catch (Exception e) {
            vfe = new ValidationFaultElement(e);
        }
        return vfe;
    }

    private final FieldValidator ifValidator;
    private final FieldValidator thenValidator;
}
