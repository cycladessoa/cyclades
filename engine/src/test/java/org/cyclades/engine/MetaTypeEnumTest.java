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
package org.cyclades.engine;

import javax.xml.transform.dom.DOMSource;
import org.cyclades.xml.parser.api.XMLGeneratedObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

public class MetaTypeEnumTest {

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
    public void basic_xml_test () throws Exception {
        String xmlStringBefore = "<root><value>some text value</value></root>";
        XMLGeneratedObject xmlGeneratedObject = new XMLGeneratedObject(xmlStringBefore) { public void populate() {} };
        String xmlStringAfter = xmlGeneratedObject.toXMLString(true);
        System.out.println(xmlStringAfter);
        if (!xmlStringBefore.equals(xmlStringAfter)) errorCollector.addError(new AssertionError("[" + xmlStringBefore + "] does not equal [" + xmlStringAfter + "]"));
        String xmlStringAfterNode = XMLGeneratedObject.toXMLString(new DOMSource(xmlGeneratedObject.getRootElement()), true);
        if (!xmlStringBefore.equals(xmlStringAfterNode)) errorCollector.addError(new AssertionError("[" + xmlStringBefore + "] does not equal [" + xmlStringAfterNode + "]"));
        String xmlStringAfterDocument = XMLGeneratedObject.toXMLString(new DOMSource(xmlGeneratedObject.getDocument()), true);
        if (!xmlStringAfterNode.equals(xmlStringAfterDocument)) errorCollector.addError(new AssertionError("[" + xmlStringAfterNode + "] does not equal [" + xmlStringAfterDocument + "]"));
        String metaEnumXMLString = MetaTypeEnum.XML.createMetaFromObject(xmlGeneratedObject.getRootElement());
        if (!xmlStringBefore.equals(metaEnumXMLString)) errorCollector.addError(new AssertionError("[" + xmlStringBefore + "] does not equal [" + metaEnumXMLString + "]"));
    }

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();
}