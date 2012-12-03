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
package org.cyclades.xml.comparitor;

import org.cyclades.xml.parser.XMLParserException;
import org.w3c.dom.Element;

public class XMLComparableObject {

    public XMLComparableObject (String XML) throws XMLParserException {
        final String eLabel = "XMLComparableObject.XMLComparableObject: ";
        try {
            if (XML != null) {
                comparitor = new XMLComparitor(XML);
            }
        } catch (Exception e) {
            throw new XMLParserException (eLabel + e);
        }
    }
        
    public void setComparitor (String XML) throws XMLParserException {
        final String eLabel = "XMLComparableObject.setComparitor: ";
        try {
            comparitor = new XMLComparitor(XML);
        } catch (Exception e) {
            throw new XMLParserException (eLabel + e);
        }
    }
        
    public boolean matchTo (String XML) throws XMLParserException {
        final String eLabel = "XMLComparableObject.matchTo: ";
        try {
            return comparitor.isMatch(XML);
        } catch (Exception e) {
            throw new XMLParserException (eLabel + e);
        }
    }
        
    public boolean matchTo (Element root) throws XMLParserException {
        final String eLabel = "XMLComparableObject.matchTo: ";
        try {
            return comparitor.isMatch(root);
        } catch (Exception e) {
            throw new XMLParserException (eLabel + e);
        }
    }
        
    public boolean matchTo (XMLComparableObject xmlComparableObject) throws XMLParserException {
        final String eLabel = "XMLComparableObject.matchTo: ";
        try {
            return comparitor.isMatch(xmlComparableObject.getXMLComparitor().getDomRootElement());
        } catch (Exception e) {
            throw new XMLParserException (eLabel + e);
        }
    }
        
    public boolean matchedBy (String XML) throws XMLParserException {
        final String eLabel = "XMLComparableObject.matchedBy: ";
        try {
            XMLComparitor matchComparitor = new XMLComparitor (XML);
            return matchComparitor.isMatch(comparitor.getDomRootElement());
        } catch (Exception e) {
            throw new XMLParserException (eLabel + e);
        }
    }
        
    public boolean matchedBy (Element root) throws XMLParserException {
        final String eLabel = "XMLComparableObject.matchedBy: ";
        try {
            XMLComparitor matchComparitor = new XMLComparitor (root);
            return matchComparitor.isMatch(comparitor.getDomRootElement());
        } catch (Exception e) {
            throw new XMLParserException (eLabel + e);
        }
    }
        
    public boolean matchedBy (XMLComparableObject xmlComparableObject) throws XMLParserException {
        final String eLabel = "XMLComparableObject.matchedBy: ";
        try {
            return xmlComparableObject.getXMLComparitor().isMatch(comparitor.getDomRootElement());
        } catch (Exception e) {
            throw new XMLParserException (eLabel + e);
        }
    }
        
    public XMLComparitor getXMLComparitor  () {
        return comparitor;
    }
        
    private XMLComparitor comparitor;
}
