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
package org.cyclades.xml.generator;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Class to implement XML generation
 * Usually not accessed directly, use "XMLWriter" class
 *
 */
public class XMLGen {
    /**
     * Constructor, any of these can be null
     * 
     * @param encoding  if specified will add the encoding tag
     * @param docRoot   if specified will add a root opening tag
     * @param attributes        if specified will add attributes
     * @throws XMLGeneratorException
     */
    public XMLGen (String encoding, String docRoot, Hashtable attributes) throws XMLGeneratorException {
        final String eLabel = "XMLGen.XMLGen: ";
        try {
            if (encoding == null) {
                mXmlBuffer = new StringBuilder();
            } else {
                mXmlBuffer = new StringBuilder("<?xml version=\"1.0\" encoding=\"");
                mXmlBuffer.append(encoding);
                mXmlBuffer.append("\"?>");
            }
                        
            if (docRoot != null) {
                mDocRoot = docRoot;
                addBeginningTag(docRoot, attributes);
            }
        } catch (Exception e) {
            throw new XMLGeneratorException(eLabel + e);
        }
    }
        
    /**
     * Add an element to the xml document
     * 
     * @param name name of element
     * @param attributes        attributes if any, can be null
     * @param data      data for data section
     * @param isCdata   is data section a CDATA type?
     */
    public void addElement(String name, Hashtable attributes, String data, boolean isCdata) throws XMLGeneratorException {
        final String eLabel = "XMLGen.addElement: ";
        try {
            addBeginningTag(name, attributes);
            addDataSection(data, isCdata);
            addEndingTag(name);
        } catch (Exception e) {
            throw new XMLGeneratorException(eLabel + e);
        }
    }
        
    /**
     * Add a beginning tag to the XML representation
     * 
     * @param name      name of element
     * @param attributes        attributes if any, can be null
     * @throws XMLGeneratorException
     */
    public void addBeginningTag (String name, Hashtable attributes) throws XMLGeneratorException {
        final String eLabel = "XMLGen.addTag: ";
        try {
            mXmlBuffer.append("<");
            mXmlBuffer.append(name);
            // Add the attributes here 
            if (attributes != null) {
                Enumeration keyEnum = attributes.keys();
                while (keyEnum.hasMoreElements()) {
                    Object key = keyEnum.nextElement();
                    Object value = attributes.get(key);
                    mXmlBuffer.append(" ");
                    mXmlBuffer.append(key.toString());
                    mXmlBuffer.append("=\"");
                    mXmlBuffer.append(value.toString());
                    mXmlBuffer.append("\"");
                }
            }
            mXmlBuffer.append(">");
        } catch (Exception e) {
            throw new XMLGeneratorException(eLabel + e);
        }
    }
        
    /**
     * Add a data section to the XML representation
     * 
     * @param data      data to be appended
     * @param isCdata   is this a CDATA section?
     * @throws XMLGeneratorException
     */
    public void addDataSection (String data, boolean isCdata) throws XMLGeneratorException {
        final String eLabel = "XMLGen.addDataSection: ";
        try {
            if (isCdata) {
                mXmlBuffer.append("<![CDATA[");
                mXmlBuffer.append(data);
                mXmlBuffer.append("]]>");
            } else {
                mXmlBuffer.append(data);
            }
        } catch (Exception e) {
            throw new XMLGeneratorException(eLabel + e);
        }
    }
        
    /**
     * Add an ending tag to this XML representation
     * 
     * @param name the name of the element
     * @throws XMLGeneratorException
     */
    public void addEndingTag (String name) throws XMLGeneratorException {
        final String eLabel = "XMLGen.addTag: ";
        try {
            mXmlBuffer.append("</");
            mXmlBuffer.append(name);
            mXmlBuffer.append(">");
        } catch (Exception e) {
            throw new XMLGeneratorException(eLabel + e);
        }
    }
        
    /**
     * Add a raw string to the XML representation
     * 
     * @param rawString String to add
     * @throws XMLGeneratorException
     */
    public void addRaw (String rawString) throws XMLGeneratorException {
        final String eLabel = "XMLGen.addRaw: ";
        try {
            mXmlBuffer.append(rawString);
        } catch (Exception e) {
            throw new XMLGeneratorException(eLabel + e);
        }
    }
        
    /**
     * Method to call when you are done with this XML spec. This will
     * add an end to your docroot if one was passed into the constructor.
     * Otherwise will do nothing.
     * 
     * @throws XMLGeneratorException
     */
    public void done () throws XMLGeneratorException {
        final String eLabel = "XMLGen.done: ";
        try {
            if (mDocRoot != null) { 
                addEndingTag(mDocRoot);
            }
        } catch (Exception e) {
            throw new XMLGeneratorException(eLabel + e);
        }
    }
        
    public String toString () {
        return mXmlBuffer.toString();
    }
        
    // Variables
    private StringBuilder mXmlBuffer;
    private String mDocRoot = null;
    public static final String  ENCODING_SCHEME_UTF8            = "UTF-8";
    public static final String  ENCODING_SCHEME_US_ASCII        = "US-ASCII";
}
