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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.cyclades.engine.NyxletSession;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class FieldValidatorTest {

    @BeforeClass
    public static void setUpBefore() throws Exception {
    }

    @AfterClass
    public static void tearDownAfter() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void parameter_list_fail_test () throws Exception {
        NyxletSession nyxletSession = new NyxletSession(null, null, null, null);
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        parameters.put("mykey", Arrays.asList(new String[]{"mykey_value"}));
        FieldValidator fv = new ParameterListValidator("mykey") {
            public ValidationFaultElement validate (List<String> parameters) throws Exception {
                if (parameters == null || parameters.size() < 1 || !parameters.get(0).equals("mykey_valuexxx")) return new ValidationFaultElement("We blew it!");
                return null;
            }
        };
        ValidationFaultElement response = fv.validate(nyxletSession, parameters);
        if (response == null)
            errorCollector.addError(new AssertionError("Error in verification: this should have been a faulty validation!"));
    }

    @Test
    public void parameter_list_success_test () throws Exception {
        NyxletSession nyxletSession = new NyxletSession(null, null, null, null);
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        parameters.put("mykey", Arrays.asList(new String[]{"mykey_value"}));
        FieldValidator fv = new ParameterListValidator("mykey") {
            public ValidationFaultElement validate (List<String> parameters) throws Exception {
                if (parameters == null || parameters.size() < 1 || !parameters.get(0).equals("mykey_value")) return new ValidationFaultElement("We blew it!");
                return null;
            }
        };
        ValidationFaultElement response = null;
        try {
            response = fv.validate(nyxletSession, parameters);
        } catch (Exception e) {
            response = new ValidationFaultElement(e);
        }
        if (response != null)
            errorCollector.addError(new AssertionError("Error in verification: " + response));
    }

    @Test
    public void field_validators_success_test () throws Exception {
        NyxletSession nyxletSession = new NyxletSession(null, null, null, null);
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        parameters.put("mykey", Arrays.asList(new String[]{" s "}));
        FieldValidators fieldValidators = new FieldValidators();
        fieldValidators.add(new ParameterHasValue("mykey"));
        List<ValidationFaultElement> faultList = fieldValidators.validate(nyxletSession, parameters);
        for (ValidationFaultElement fault : faultList) {
            System.out.println(fault);
            errorCollector.addError(new AssertionError(fault.toString()));
        }
    }

    @Test
    public void field_validators_multiple_success_test () throws Exception {
        NyxletSession nyxletSession = new NyxletSession(null, null, null, null);
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        parameters.put("mykey", Arrays.asList(new String[]{" s "}));
        parameters.put("mykey2", Arrays.asList(new String[]{" s "}));
        parameters.put("mykey3", Arrays.asList(new String[]{" s "}));
        FieldValidators fieldValidators = new FieldValidators();
        fieldValidators.add(new ParameterHasValue("mykey")).add(new ParameterHasValue("mykey2")).add(new ParameterHasValue("mykey3"));
        List<ValidationFaultElement> faultList = fieldValidators.validate(nyxletSession, parameters);
        for (ValidationFaultElement fault : faultList) {
            System.out.println(fault);
            errorCollector.addError(new AssertionError(fault.toString()));
        }
    }

    @Test
    public void parameters_success_test () throws Exception {
        NyxletSession nyxletSession = new NyxletSession(null, null, null, null);
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        parameters.put("mykey", Arrays.asList(new String[]{" s "}));
        parameters.put("mykey2", Arrays.asList(new String[]{" s "}));
        parameters.put("mykey3", Arrays.asList(new String[]{" s "}));
        FieldValidator fv = new ParametersValidator(false) {
            public ValidationFaultElement validate (Map<String, List<String>> parameters) throws Exception {
                if (parameters == null) return new ValidationFaultElement("Parameters structure is null");
                if (!parameters.containsKey("mykey")) return new ValidationFaultElement("Key does not exist: \"mykey\"");
                if (!parameters.containsKey("mykey2")) return new ValidationFaultElement("Key does not exist: \"mykey2\"");
                if (!parameters.containsKey("mykey3")) return new ValidationFaultElement("Key does not exist: \"mykey3\"");
                if (!parameters.get("mykey").get(0).equals(parameters.get("mykey2").get(0)) ||
                    !parameters.get("mykey2").get(0).equals(parameters.get("mykey3").get(0))
                   ) return new ValidationFaultElement("Keys do not match");
                return null;
            }
        };
        ValidationFaultElement response = null;
        try {
            response = fv.validate(nyxletSession, parameters);
        } catch (Exception e) {
            response = new ValidationFaultElement(e);
        }
        if (response != null)
            errorCollector.addError(new AssertionError("Error in verification: " + response));
    }

    @Test
    public void parameters_fail_test () throws Exception {
        NyxletSession nyxletSession = new NyxletSession(null, null, null, null);
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        parameters.put("mykey", Arrays.asList(new String[]{" s "}));
        parameters.put("mykey2", Arrays.asList(new String[]{" xxx "}));
        parameters.put("mykey3", Arrays.asList(new String[]{" s "}));
        FieldValidator fv = new ParametersValidator() {
            public ValidationFaultElement validate (Map<String, List<String>> parameters) throws Exception {
                if (parameters == null) return new ValidationFaultElement("Parameters structure is null");
                if (!parameters.containsKey("mykey")) return new ValidationFaultElement("Key does not exist: \"mykey\"");
                if (!parameters.containsKey("mykey2")) return new ValidationFaultElement("Key does not exist: \"mykey2\"");
                if (!parameters.containsKey("mykey3")) return new ValidationFaultElement("Key does not exist: \"mykey3\"");
                if (!parameters.get("mykey").get(0).equals(parameters.get("mykey2").get(0)) ||
                    !parameters.get("mykey2").get(0).equals(parameters.get("mykey3").get(0))
                   ) return new ValidationFaultElement("Keys do not match");
                return null;
            }
        };
        ValidationFaultElement response = null;
        try {
            response = fv.validate(nyxletSession, parameters);
        } catch (Exception e) {
            response = new ValidationFaultElement(e);
        }
        if (response == null)
            errorCollector.addError(new AssertionError("Verification should have failed and did not!"));
    }

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();
}