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

public class ValidationFaultElement {
    public ValidationFaultElement (Throwable t) {
        message = t.toString();
    }

    public ValidationFaultElement (String message) {
        this.message = message;
    }

    @Override
    public String toString () {
        return message;
    }

    /**
     * Convenience method to create a String from a List<ValidationFaultElement>
     * One can effectively do a "join" on a List of ValidationFaultElements by calling this method and
     * passing the result to a new ValidationFaultElement, i.e.:
     *
     * new ValidationFaultElement(ValidationFaultElement.toString("my_prefix", validationFaultElements));
     *
     * @param prefix    A prefix to add to the output String. No prefix will be added if this is null.
     * @param v         The List of ValidationFaultElements to combine
     * @return String explanation of the fault
     */
    public static String toString (String prefix, List<ValidationFaultElement> v) {
        StringBuilder sb = new StringBuilder();
        if (prefix != null) {
            sb.append(prefix).append(" > ");
        }
        sb.append("[");
        for (int i = 0; i < v.size(); i++) {
            sb.append("\"").append(v.get(i).toString()).append("\"");
            sb.append((i < v.size() - 1) ? "," : "");
        }
        sb.append("]");
        return sb.toString();
    }

    private String message;
}
