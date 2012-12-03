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
package org.cyclades.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class XXMLStreamWriter implements XMLStreamWriter {

    private XMLStreamWriter delegate;

    private boolean suppressStartDocument = false;
    private boolean suppressEndDocument   = false;

    public XXMLStreamWriter(XMLStreamWriter delegate) {
        this.delegate = delegate;
    }

    /** TODO: Explain me */
    public void suppressStartDocument(boolean suppress) {
        this.suppressStartDocument = suppress;
    }

    /** TODO: Explain me */
    public void suppressEndDocument(boolean suppress) {
        this.suppressEndDocument = suppress;
    }

    /** TODO: Explain me */
    public void writeAttributeIfNotNull(String namespaceURI, String localName, Object value)
        throws XMLStreamException
    {
        if( value == null ) { return; }
        this.delegate.writeAttribute(namespaceURI, localName, value.toString());
    } // end of writeAttributeIfNotNull

    /** TODO: Explain me */
    public void writeAttributeIfNotNull(String localName, Object value) throws XMLStreamException
    {
        if( value == null ) { return; }
        this.delegate.writeAttribute(localName, value.toString());
    } // end of writeAttributeIfNotNull

    /** TODO: Explain me */
    public void writeElementWithText(String namespaceURI, String localName, String text)
        throws XMLStreamException
    {
        this.delegate.writeStartElement(namespaceURI, localName);
        this.delegate.writeCharacters(text);
        this.delegate.writeEndElement();
    }

    /** TODO: Explain me */
    public void writeElementWithTextIfNotNull(String namespaceURI, String localName, Object text)
        throws XMLStreamException
    {
        if( text == null ) { return; }
        this.writeElementWithText(namespaceURI, localName, text.toString());
    }

    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        this.delegate.writeStartElement(localName);
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        this.delegate.writeStartElement(namespaceURI, localName);
    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        this.delegate.writeStartElement(prefix, localName, namespaceURI);
    }

    @Override
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        this.delegate.writeEmptyElement(namespaceURI, localName);
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        this.delegate.writeEmptyElement(prefix, localName, namespaceURI);
    }

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
        this.delegate.writeEmptyElement(localName);
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        this.delegate.writeEndElement();
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        if( !this.suppressEndDocument ) {
            this.delegate.writeEndDocument();
        }
    }

    @Override
    public void close() throws XMLStreamException {
        this.delegate.close();
    }

    @Override
    public void flush() throws XMLStreamException {
        this.delegate.flush();
    }

    @Override
    public void writeAttribute(String localName, String value) throws XMLStreamException {
        this.delegate.writeAttribute(localName, value);
    }

    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
        throws XMLStreamException
    {
        this.delegate.writeAttribute(prefix, namespaceURI, localName, value);
    }

    @Override
    public void writeAttribute(String namespaceURI, String localName, String value)
        throws XMLStreamException
    {
        this.delegate.writeAttribute(namespaceURI, localName, value);
    }

    @Override
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        this.delegate.writeNamespace(prefix, namespaceURI);
    }

    @Override
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        this.delegate.writeDefaultNamespace(namespaceURI);
    }

    @Override
    public void writeComment(String data) throws XMLStreamException {
        this.delegate.writeComment(data);
    }

    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException {
        this.delegate.writeProcessingInstruction(target);
    }

    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        this.delegate.writeProcessingInstruction(target, data);
    }

    @Override
    public void writeCData(String data) throws XMLStreamException {
        this.delegate.writeCData(data);
    }

    @Override
    public void writeDTD(String dtd) throws XMLStreamException {
        this.delegate.writeDTD(dtd);
    }

    @Override
    public void writeEntityRef(String name) throws XMLStreamException {
        this.delegate.writeEntityRef(name);
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        if( !this.suppressStartDocument ) {
            this.delegate.writeStartDocument();
        }
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        if( !this.suppressStartDocument ) {
            this.delegate.writeStartDocument(version);
        }
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        if( !this.suppressStartDocument ) {
            this.delegate.writeStartDocument(encoding, version);
        }
    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        this.delegate.writeCharacters(text);
    }

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        this.delegate.writeCharacters(text, start, len);
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        return this.delegate.getPrefix(uri);
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        this.delegate.setPrefix(prefix, uri);
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        this.delegate.setDefaultNamespace(uri);
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        this.delegate.setNamespaceContext(context);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return this.delegate.getNamespaceContext();
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return this.delegate.getProperty(name);
    }

}
