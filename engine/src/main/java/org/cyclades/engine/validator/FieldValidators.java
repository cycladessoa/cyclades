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
 * This class should be used to maintain FieldValidator objects, and ultimately execute them when necessary.
 */
public class FieldValidators {

    public FieldValidators add (FieldValidator fieldValidator) {
        fieldValidators.add(fieldValidator);
        return this;
    }

    /**
     * Execute the validators. Terminal FieldValidators will be respected.
     * Returns an empty list if there have been no errors, otherwise a list of each error
     *
     * @param nyxletSession     The NyxletSession of the calling Nyxlet (to get data if needed)
     * @param parameters        The parameters to verify
     *
     * @return List<ValidationFaultElement>
     */
    public List<ValidationFaultElement> validate (NyxletSession nyxletSession, Map<String, List<String>> parameters) {
        return validate(nyxletSession, parameters, false);
    }

    /**
     * Execute the validators.
     * Returns an empty list if there have been no errors, otherwise a list of each error
     *
     * @param nyxletSession     The NyxletSession of the calling Nyxlet (to get data if needed)
     * @param parameters        The parameters to verify
     * @param ignoreTerminals   If true, no terminal settings will be respected...i.e. all FieldValidators will be
     *                          validated regardless of their "terminal" settings. This is most likely not the norm.
     *                          You can simply call the method above to assume this false fir simplicity.
     * @return List<ValidationFaultElement>
     */
    public List<ValidationFaultElement> validate (NyxletSession nyxletSession, Map<String, List<String>> parameters, boolean ignoreTerminals) {
        List<ValidationFaultElement> validationFaultElements = new ArrayList<ValidationFaultElement>();
        ValidationFaultElement vfe = null;
        for (FieldValidator fv : fieldValidators) {
            try {
                vfe = fv.validate(nyxletSession, parameters);
            } catch (Exception e) {
                vfe = new ValidationFaultElement(e);
            }
            if (vfe != null) {
                validationFaultElements.add(vfe);
                if (fv.isTerminal() && !ignoreTerminals) break;
            }
        }
        return validationFaultElements;
    }

    public int size () {
        return fieldValidators.size();
    }

    private List<FieldValidator> fieldValidators = new ArrayList<FieldValidator>();
}
