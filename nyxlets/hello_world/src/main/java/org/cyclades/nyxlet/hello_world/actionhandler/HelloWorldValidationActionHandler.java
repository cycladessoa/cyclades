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

package org.cyclades.nyxlet.hello_world.actionhandler;

import org.cyclades.engine.nyxlet.templates.stroma.STROMANyxlet;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.nyxlet.templates.stroma.actionhandler.ActionHandler;
import java.util.List;
import java.util.Map;
import org.cyclades.annotations.AHandler;
import org.cyclades.engine.stroma.STROMAResponseWriter;
import javax.xml.stream.XMLStreamWriter;
import org.cyclades.engine.validator.ParameterHasValue;
import org.cyclades.engine.validator.ParametersValidator;
import org.cyclades.engine.validator.ValidationFaultElement;

/**
 * A simple handler to showcase field validation.
 *
 * XXX - Field Validators will be created in this file for this example...ultimately it would
 * be best for organization purposes to keep them in their own class files, even a different package
 * specific to validators. Please be sure to also check the documentation.
 *
 */
@AHandler("sayhellovalidation")
public class HelloWorldValidationActionHandler extends ActionHandler {

    public HelloWorldValidationActionHandler (STROMANyxlet parentNyxlet) {
        super(parentNyxlet);
    }

    @Override
    public void init () throws Exception {
        // Initialize FieldValidators.
        // The examples below are meant as sample uses cases. Note that these will be executed in the
        // order that they are added.
        // Add a validator (defined below) that ensures there exists at least three parameters
        getFieldValidators().add(new AtLeast3ParametersExist(false));
        // Add a built in validator that ensures there is a name parameter and it is not empty
        // Note the "true" for the second parameter, this implies "do not validate any further if this validation fails"
        getFieldValidators().add(new ParameterHasValue("name", true));
        // Add a validator (defined below) that ensures there is a name parameter, it is not empty and it has 5 characters
        getFieldValidators().add(new ParameterHas5Characters("name", false));
    }

    @Override
    public void handle (NyxletSession nyxletSession, Map<String, List<String>> baseParameters, STROMAResponseWriter stromaResponseWriter) throws Exception {
        final String eLabel = "HelloWorldValidationActionHandler.handle: ";
        try {
            StringBuilder message = new StringBuilder("Hello World: ");
            // Get any parameter values entered under the key "name"
            if (baseParameters.containsKey("name")) {
                for (String name : baseParameters.get("name")) message.append("[").append(name).append("] ");
            }
            // Using XMLStreamWriter. The benefit here is that it gets converted for you
            // automatically to JSON or XML behind the scenes. This is most likely the
            // best all around method to write data.
            XMLStreamWriter streamWriter = stromaResponseWriter.getXMLStreamWriter();
            streamWriter.writeStartElement("message");
            streamWriter.writeCharacters(message.toString());
            streamWriter.writeEndElement();
        } catch (Exception e) {
            getParentNyxlet().logStackTrace(e);
            handleException(nyxletSession, stromaResponseWriter, eLabel, e);
        } finally {
            stromaResponseWriter.done();
        }
    }

}

/**
 * Example FieldValidator that extends another FieldValidator that already does what we want
 * NOTE: You can also extend ParameterListValidator and start from scratch if desired.
 */
class ParameterHas5Characters extends ParameterHasValue {

    public ParameterHas5Characters(String key) {
        super(key);
    }

    public ParameterHas5Characters(String key, boolean terminal) {
        super(key, terminal);
    }

    @Override
    public ValidationFaultElement validate(List<String> values) throws Exception {
        // Let's reuse the already existing validation we extended
        ValidationFaultElement vfe = super.validate(values);
        if (vfe != null) return vfe;
        // We now are sure there is a value, let's verify there it has 5 characters...
        if (values.get(0).length() != DESIRED_LENGTH) {
            return new ValidationFaultElement("\"" + getKey() + "\" does not have desired character length \"" + DESIRED_LENGTH + "\"");
        }
        return null;
    }

    private static final int DESIRED_LENGTH = 5;
}

/**
 * Example ParametersValidator...this type of validator takes in all of the parameters at once...giving
 * developers the opportunity to verify all the parameters atomically.
 */
class AtLeast3ParametersExist extends ParametersValidator {

    public AtLeast3ParametersExist () {
        super();
    }

    public AtLeast3ParametersExist (boolean terminal) {
        super(terminal);
    }

    public ValidationFaultElement validate (Map<String, List<String>> parameters) throws Exception {
        // Simple verify there is at least one parameter
        if (parameters == null || parameters.size() < 3) return new ValidationFaultElement("There are less than three parameters!");
        return null;
    }

}
