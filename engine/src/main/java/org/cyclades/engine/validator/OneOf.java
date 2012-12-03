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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.cyclades.engine.NyxletSession;

/**
 * This class will be comprised of a list of child FieldValidators.
 * This FieldValidator will return success (null) if any one of the child FieldValidators succeeds. No further
 * processing will take place after one of the child FieldValidators succeeds.
 * This FieldValidator will return failure (ValidationFaultElement) if none of the child FieldValidators succeed.
 */
public class OneOf extends FieldValidator {

    public OneOf () {
        super();
    }

    public OneOf (boolean terminal) {
        super(terminal);
    }

    @Override
    public ValidationEnum getValidationType() {
        return ValidationEnum.ONE_OF;
    }

    /**
     * Add a child FieldValidator
     *
     * @param fieldValidator
     * @return this
     */
    public OneOf add (FieldValidator fieldValidator) {
        fieldValidators.add(fieldValidator);
        return this;
    }

    public ValidationFaultElement validate (NyxletSession nyxletSession, Map<String, List<String>> parameters) throws Exception {
        ValidationFaultElement vfe = null;
        List<ValidationFaultElement> vfeList = new ArrayList<ValidationFaultElement>();
        for (FieldValidator fv : fieldValidators) {
            try {
                vfe = fv.validate(nyxletSession, parameters);
            } catch (Exception e) {
                vfe = new ValidationFaultElement(e);
            }
            // If we come across a validator that succeeds, return null (success)
            if (vfe == null) return null;
            vfeList.add(vfe);
        }
        return new ValidationFaultElement(ValidationFaultElement.toString(FAULT_PREFIX, vfeList));
    }

    private List<FieldValidator> fieldValidators = new ArrayList<FieldValidator>();
    private static final String FAULT_PREFIX = "ONE_OF_VALIDATION_FAULT_ELEMENTS";
}
