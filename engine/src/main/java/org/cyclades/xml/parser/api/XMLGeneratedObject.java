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
package org.cyclades.xml.parser.api;

import java.io.File;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import org.xml.sax.InputSource;
import java.io.StringReader;
import org.cyclades.xml.parser.XMLParserException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Extend this class to make an Object that can be populated
 * by an XML String.
 *
 */
public abstract class XMLGeneratedObject {
    /**
     * This constructor is for loading the DOM parser. Must call
     * to initialize DOM. Pass in null to bypass this super class's
     * functionality and use child class as a normal class.
     *
     * @param xmlString
     * @throws XMLParserException
     */
    public XMLGeneratedObject (String xmlString) throws XMLParserException {
        final String eLabel = "XMLGeneratedObject.XMLGeneratedObject: ";
        if (xmlString == null) {
            return;
        }
        try {
            parseFromString(xmlString);
        } catch (Exception e) {
            throw new XMLParserException (eLabel + e);
        }

    }

    /**
     * Load this DOM from the String passed in...
     *
     * @param xmlString XML String
     * @throws XMLParserException
     */
    public void parseFromString (String xmlString) throws XMLParserException {
        final String eLabel = "XMLGeneratedObject.parseFromString: ";
        try {
            // Get the factory
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            // Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // Parse using builder to get DOM representation of the XML file
            this.document = db.parse(new InputSource(new StringReader(xmlString)));
            // Get the root element
            this.rootElement = document.getDocumentElement();
        } catch (Exception e) {
            throw new XMLParserException (eLabel + e);
        }

    }

    /**
     * Load this DOM from the file passed in...
     *
     * @param file
     * @throws XMLParserException
     */
    public void parseFromFile (File file) throws XMLParserException {
        final String eLabel = "XMLGeneratedObject.parseFromFile: ";
        try {
            // Get the factory
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            // Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // Parse using builder to get DOM representation of the XML file
            document = db.parse(file);
            // Get the root element
            rootElement = document.getDocumentElement();
        } catch (Exception e) {
            throw new XMLParserException (eLabel + e);
        }

    }

    /**
     * Abstract method that should be implemented in child class that
     * will populate local variables from XML DOM.
     *
     * @throws XMLParserException
     */
    public abstract void populate () throws XMLParserException;

    /**
     * Get the root element of the DOM
     *
     * @return root Element
     */
    public Element getRootElement () {
        return rootElement;
    }

    /**
     * Get the document of the DOM
     *
     * @return root Document
     */
    public Document getDocument () {
        return document;
    }

    /**
     * Release the DOM from memory so it can be Garbage Collected.
     * For each object created with this abstract class there will
     * also be a DOM created. This DOM will hang around until we
     * make the pointer to it null.
     *
     */
    public void cleanUp () {
        rootElement = null;
        document = null;
    }

    /*****************************************************************/
    /* Here are a couple helper classes. You can use these or access */
    /* the APIs for the DOM directly in your populate() method       */
    /*****************************************************************/

    /**
     * Get the text value of a nodes data section
     * @param ele
     * @param tagName
     * @return String
     * @throws XMLParserException
     */
    public static String getTextValue(Element ele, String tagName) throws XMLParserException {
        final String eLabel = "XMLGeneratedObject.getTextValue: ";
        try {
            String textVal = null;
            NodeList nl = ele.getElementsByTagName(tagName);
            if(nl != null && nl.getLength() > 0) {
                Element el = (Element)nl.item(0);
                textVal = el.getFirstChild().getNodeValue();
            }
            return textVal;
        } catch (Exception e) {
            throw new XMLParserException(eLabel + e);
        }
    }

    /**
     * Get the integer value of a nodes data section
     *
     * @param ele
     * @param tagName
     * @return int
     * @throws XMLParserException
     */
    public static int getIntValue(Element ele, String tagName) throws XMLParserException {
        final String eLabel = "XMLGeneratedObject.getIntValue: ";
        try {
            return Integer.parseInt(getTextValue(ele,tagName));
        } catch (Exception e) {
            throw new XMLParserException(eLabel + e);
        }
    }

    /**
     * Get the attribute text value of a node
     *
     * @param ele
     * @param tagName
     * @param attName
     * @return String
     * @throws XMLParserException
     */
    public static String getAttributeTextValue(Element ele, String tagName, String attName) throws XMLParserException {
        final String eLabel = "XMLGeneratedObject.getAttributeTextValue: ";
        try {
            String textVal = null;
            NodeList nl = ele.getElementsByTagName(tagName);
            if(nl != null && nl.getLength() > 0) {
                Element el = (Element)nl.item(0);
                textVal = el.getAttribute(attName);
            }
            return textVal;
        } catch (Exception e) {
            throw new XMLParserException(eLabel + e);
        }
    }

    /**
     * Get the integer attribute value of a node
     *
     * @param ele
     * @param tagName
     * @param attName
     * @return int
     * @throws XMLParserException
     */
    public static int getAttributeIntValue(Element ele, String tagName, String attName) throws XMLParserException {
        final String eLabel = "XMLGeneratedObject.getAttributeIntValue: ";
        try {
            return Integer.parseInt(getAttributeTextValue(ele,tagName, attName));
        } catch (Exception e) {
            throw new XMLParserException(eLabel + e);
        }
    }

    /**
     * Convert this object to a String representation of XML
     *
     * @return String
     * @throws Exception
     */
    public String toXMLString () throws Exception {
        return toXMLString(false);
    }

    /**
     * Convert this object to a String representation of XML
     *
     * @param omitXMLDeclaration
     * @return String
     * @throws Exception
     */
    public String toXMLString (boolean omitXMLDeclaration) throws Exception {
        // If we cleaned up the DOM...then it's the implementing class'
        // responsibility to hold the data...it is gone at this level.
        if (document == null) return null;
        return toXMLString(new DOMSource(document), omitXMLDeclaration);
    }

    /**
     * Convert the DOMSource passed in to a String representation of the meta data
     * (Typical use is a DOMsource from a Document or a Node)
     *
     * @param domSource
     * @param omitXMLDeclaration
     * @return String
     * @throws Exception
     */
    public static String toXMLString (DOMSource domSource, boolean omitXMLDeclaration) throws Exception {
        final String eLabel = "XMLGeneratedObject.toXMLString(DOMSource): ";
        try {
            StringWriter stringWriter = new StringWriter();
            writeToStreamResult(domSource, new StreamResult(stringWriter), omitXMLDeclaration);
            stringWriter.flush();
            return stringWriter.toString();
        } catch (Exception e) {
            throw new Exception (eLabel + e);
        }
    }

    /**
     * Write the DOMSource to the StreamResult
     *
     * @param domSource
     * @param streamResult
     * @param omitXMLDeclaration
     * @throws Exception
     */
    public static void writeToStreamResult (DOMSource domSource, StreamResult streamResult, boolean omitXMLDeclaration) throws Exception {
        final String eLabel = "XMLGeneratedObject.writeToStreamResult: ";
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            if (omitXMLDeclaration) transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(domSource, streamResult);
        } catch (Exception e) {
            throw new Exception (eLabel + e);
        }
    }

    private Element rootElement;
    private Document document;

}
