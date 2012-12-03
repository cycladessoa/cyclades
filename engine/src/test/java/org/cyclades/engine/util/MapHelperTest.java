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
package org.cyclades.engine.util;

import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

public class MapHelperTest {

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
    public void indexedParameterMapFromMetaObject_JSON_test () throws Exception {
        String jsonString = "[{\"keys\":[\"pnas\",\"pnas2\"],\"parameters\":[{\"name\":\"resource-type\",\"value\":\"abstract\"},{\"name\":\"child-target\",\"value\":\"semantic_input\"},{\"name\":\"child-replyto\",\"value\":\"semantic_output\"},{\"name\":\"base-uri\",\"value\":\"http://sass.highwire.org\"},{\"name\":\"user\",\"value\":\"temis\"},{\"name\":\"password\",\"value\":\"!semio!fast!\"},{\"name\":\"input-type\",\"value\":\"Tmx\"},{\"name\":\"plan\",\"value\":\"PNASDemoBER-AP\"},{\"name\":\"plan\",\"value\":\"PNASDemoRTF-AP\"},{\"name\":\"consumer\",\"value\":\"SimpleJSON\"},{\"name\":\"feed-only-plan\",\"value\":\"PNASPilotSim-AP\"},{\"name\":\"base-output-uri\",\"value\":\"/semantics/dev/skos\"},{\"name\":\"collection\",\"value\":\"pnas_collection\"}]},      {\"keys\":[\"aap1\",\"aap2\"],\"parameters\":[{\"name\":\"resource-type\",\"value\":\"abstract\"},{\"name\":\"child-target\",\"value\":\"semantic_input\"},{\"name\":\"child-replyto\",\"value\":\"semantic_output\"},{\"name\":\"base-uri\",\"value\":\"http://sass.highwire.org\"},{\"name\":\"user\",\"value\":\"temis\"},{\"name\":\"password\",\"value\":\"!semio!fast!\"},{\"name\":\"input-type\",\"value\":\"Tmx\"},{\"name\":\"plan\",\"value\":\"MER-AP\"},{\"name\":\"plan\",\"value\":\"RTF-AP\"},{\"name\":\"consumer\",\"value\":\"SimpleJSON\"},{\"name\":\"feed-only-plan\",\"value\":\"AAPSim-AP\"},{\"name\":\"base-output-uri\",\"value\":\"/semantics/dev/skos\"},{\"name\":\"collection\",\"value\":\"aap_collection\"}]}]";
        Map<String, Map<String, List<String>>> indexedMap = MapHelper.indexedParameterMapFromMetaObject (new JSONArray(jsonString));
        printIndexedParameterMap(indexedMap);
        //errorCollector.addError(new AssertionError(\"Duplicate Value: \" + currentID));
    }

    @Test
    public void indexedParameterMapFromMetaObject_XML_test () throws Exception {
        String xmlString = "<root><entry><keys><key value=\"pnas\"/><key value=\"pnas2\"/></keys><parameters><parameter name=\"plan\" value=\"PNASDemoBER-AP\"/><parameter name=\"plan\" value=\"PNASDemoRTF-AP\"/><parameter name=\"collection\" value=\"pnas_collection\"/></parameters></entry><entry><keys><key value=\"aap\"/><key value=\"aap2\"/><key value=\"aap3\"/></keys><parameters><parameter name=\"plan\" value=\"MER-AP\"/><parameter name=\"plan\" value=\"RTF-AP\"/><parameter name=\"collection\" value=\"aap_collection\"/></parameters></entry></root>";
        Map<String, Map<String, List<String>>> indexedMap = MapHelper.indexedParameterMapFromMetaObject (new GenericXMLObject(xmlString).getRootElement().getChildNodes());
        printIndexedParameterMap(indexedMap);
        //errorCollector.addError(new AssertionError(\"Duplicate Value: \" + currentID));
    }

    private void printIndexedParameterMap (Map<String, Map<String, List<String>>> indexedMap) {
        for (Map.Entry<String, Map<String, List<String>>> indexedMapEntry : indexedMap.entrySet()) {
            System.out.println(indexedMapEntry.getKey());
            for (Map.Entry<String, List<String>> mapEntry : indexedMapEntry.getValue().entrySet()) {
                System.out.println("\t" + mapEntry.getKey());
                for (String value : mapEntry.getValue()) System.out.println("\t\t" + value);
            }
        }
    }

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();
}