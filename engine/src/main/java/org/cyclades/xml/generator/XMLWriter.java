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
import java.util.Stack;

public class XMLWriter {
    /**
     * Constructor. If you do not specify encoding (null) then you can
     * use the ouput of this class as a part of another XMLDoc. In theory,
     * if an object oriented XML generation is to take place,
     * every XMLDefinedObject should contain a XMLWriter, and every
     * object in an XML structure should implement XMLDefinedObject.
     *
     * @param encoding  Can be null
     * @param docRoot The name of the root node of this XMLDoc
     * @throws XMLGeneratorException
     */
    @SuppressWarnings("unchecked")
        public XMLWriter (String encoding, String docRoot) throws XMLGeneratorException {
        final String eLabel = "XMLdoc.XMLWriter: ";

        try {
            if (docRoot == null) {
                throw new Exception ("Must specify doc root name.");
            }

            root = new XMLGen(encoding, docRoot, null);
            elementLevelStack.push(root);
        } catch (Exception e) {
            throw new XMLGeneratorException(eLabel + e);
        }
    }

    /**
     * Same as above but with attributes
     *
     * @param encoding
     * @param docRoot
     * @param attributes
     * @throws XMLGeneratorException
     */
    @SuppressWarnings("unchecked")
        public XMLWriter (String encoding, String docRoot, Hashtable attributes) throws XMLGeneratorException {
        final String eLabel = "XMLdoc.XMLWriter: ";

        try {
            if (docRoot == null) {
                throw new Exception ("Must specify doc root name.");
            }

            root = new XMLGen(encoding, docRoot, attributes);
            elementLevelStack.push(root);
        } catch (Exception e) {
            throw new XMLGeneratorException(eLabel + e);
        }
    }

    /**
     * Add a node to this structure. This will actually push one on the
     * node stack. This will be the current node after this call.
     *
     * @param elementName   Element name of this node
     * @param attributes    Any attributes that are going to be in this node
     * @throws XMLGeneratorException
     */
    @SuppressWarnings("unchecked")
        public void addNode (String elementName, Hashtable attributes) throws XMLGeneratorException {
        final String eLabel = "XMLDoc.addNode: ";
        try {
            elementLevelStack.push(new XMLGen(null, elementName, attributes));
        } catch (Exception e) {
            throw new XMLGeneratorException(eLabel + e);
        }
    }

    /**
     * Add a leaf node, this will not add a node to the stack, rather
     * add leaf nodes to the node/element on top of the stack.
     *
     * @param elementName Name of element/node
     * @param attributes    Attributes if any
     * @param data  Data to be added to the leaf node
     * @param cData Is this a CData section?
     * @throws XMLGeneratorException
     */
    public void addLeafNode (String elementName, Hashtable attributes, String data, boolean cData) throws XMLGeneratorException {
        final String eLabel = "XMLDoc.addLeafNode: ";
        try {

            XMLGen xml = (XMLGen)elementLevelStack.peek();
            xml.addElement(elementName, attributes, data, cData);
        } catch (Exception e) {
            throw new XMLGeneratorException(eLabel + e);
        }
    }

    /**
     * Add raw data to whatever nodes data section we are currently
     * in. (Current node is on top of the stack)
     *
     * @param data  The data to add
     * @throws XMLGeneratorException
     */
    public void addRaw (String data) throws XMLGeneratorException {
        final String eLabel = "XMLDoc.addRaw: ";
        try {

            XMLGen xml = (XMLGen)elementLevelStack.peek();
            xml.addRaw(data);
        } catch (Exception e) {
            throw new XMLGeneratorException(eLabel + e);
        }
    }

    /**
     * Same as above addRaw method
     *
     * @param data
     * @throws XMLGeneratorException
     */
    public void addXML (String data) throws XMLGeneratorException {
        addRaw(data);
    }

    /**
     * Commit the current node into the next one on the stack. Make
     * the next one in line the current one. This is called when you are
     * finished processing the curent node. In short, this brings you up
     * to one level in the document to work on the higer node.
     *
     * @throws XMLGeneratorException
     */
    public void commitCurrentNode () throws XMLGeneratorException {
        final String eLabel = "XMLDoc.commitNode: ";
        try {

            if (elementLevelStack.size() <= 1) {
                throw new Exception(eLabel + "Error, already at root node.");
            }

            XMLGen xmlChild = (XMLGen)elementLevelStack.pop();
            XMLGen xmlParent = (XMLGen)elementLevelStack.peek();
            xmlChild.done();
            xmlParent.addRaw(xmlChild.toString());
        } catch (Exception e) {
            throw new XMLGeneratorException(eLabel + e);
        }
    }

    /**
     * Call this when you are finished working on the document.
     * This will automatically commit all the nodes for you. You need
     * to call this when you are done!
     *
     * @throws XMLGeneratorException
     */
    public void flush () throws XMLGeneratorException {
        final String eLabel = "XMLWriter.flush: ";
        try {
            int size = elementLevelStack.size() - 1;
            for (int i = 0; i < size; i++) {
                commitCurrentNode();
            }
            root.done();
        } catch (Exception e) {
            throw new XMLGeneratorException(eLabel + e);
        }
    }

    /**
     * Same as flush() method, just different name for convenience
     *
     * @throws XMLGeneratorException
     */
    public void done () throws XMLGeneratorException {
        final String eLabel = "XMLWriter.done: ";
        try {
            flush();
        } catch (Exception e) {
            throw new XMLGeneratorException(eLabel + e);
        }
    }

    public String toString () {
        return root.toString();
    }

    private Stack elementLevelStack = new Stack();
    private XMLGen root = null;
    public static final String  ENCODING_SCHEME_UTF8        = "UTF-8";
    public static final String  ENCODING_SCHEME_US_ASCII    = "US-ASCII";
}
