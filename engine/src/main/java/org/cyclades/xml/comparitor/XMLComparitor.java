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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;
import java.io.StringReader;
import org.cyclades.xml.parser.XMLParserException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import java.io.File;
import java.util.Vector;

/**
 * This class will compare two XML Strings to see if they are similar.
 * We need to include special logic, so it is not as straight forward as
 * simply comparing the dom for matching elements. You create this class
 * using the "template" XML you want to compare other XML documents with.
 * As long as the "compared" XML contains an exact subset of the "template" XML
 * then it is a considered match. We need to take into consideration the
 * logic of specifying a node that is not to exist in the compared node.
 * For our purposes we will have a reserved attribute that will flag a
 * leaf node as one we will not want to see in the compared node.
 *
 * RULES FOR ADDING AN OMIT ENTRY:
 *  The "omit" attribute needs to be defined on it's own...meaning one leaf defined
 *  all the way from the root. Here is an example of what it would look like:
 *  <root>
 *      <person omit="true>         (Note omit flag needs to be at base element for that child of root)
 *          <age>22</age>           (Note only one entry per omit command)
 *      </person>
 *  </root>
 *
 *
 *
 * A matching node means a node that has the exact same structure, including
 * attributes.
 *
 * reserved attribute for boolean logic: omit=true
 *
 * BUG: The parser needs the XML string free of all new lines and such. I tried
 * parsing a readable file and it did not come out right for this to work. For
 * example, nodes that were not leaf nodes were given a #text child.
 *
 * OPTIMIZATION: If repetitive comparisons are to be made between  XML
 * files (Objects?) then cache the DOM and use the methods that have Element
 * types as parameters instead of Strings. You want to avoid parsing the XML
 * over and over again as this is the most time consuming action.
 *
 * NOTE: Adding in the COMPARITOR a node which contains more than one leaf element will result in an
 * attempted match for a node that matches exactly with a subset of the compared.
 *
 */
public class XMLComparitor {
    /**
     * Constructor that takes in a String value as a comparator.
     *
     * @param xmlToMatch
     * @throws XMLParserException
     */
    public XMLComparitor (String xmlToMatch) throws XMLParserException {
        final String eLabel = "XMLComparitor.XMLComparitor: ";
        try {
            templateRootElement = parseXML(xmlToMatch);
            if (templateRootElement == null) {
                throw new Exception(eLabel + "Root element came back null!");
            }
        } catch (Exception e) {
            throw new XMLParserException(eLabel + e);
        }
    }

    /**
     * Constructor that takes in an Element parameter as a comparator. This
     * Element should be the root node of a parsed XML document
     *
     * @param xmlToMatch
     * @throws XMLParserException
     */
    public XMLComparitor (Element xmlToMatch) throws XMLParserException {
        final String eLabel = "XMLComparitor.XMLComparitor: ";
        try {
            if (xmlToMatch == null) {
                throw new Exception(eLabel + "Root element sent in as null!");
            }
            templateRootElement = xmlToMatch;
        } catch (Exception e) {
            throw new XMLParserException(eLabel + e);
        }
    }

    /**
     * Parse the XML String into a DOM
     *
     * @param xml   String to parse
     * @return  Root element of DOM
     * @throws XMLParserException
     */
    public static Element parseXML (String xml) throws XMLParserException {
        final String eLabel = "XMLComparitor.parseXML: ";
        if (xml == null) {
            return null;
        }
        try {
            // Get the factory
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            // Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // Parse using builder to get DOM representation of the XML file
            Document dom = db.parse(new InputSource(new StringReader(xml)));
            // Get the root element
            return dom.getDocumentElement();
        } catch (Exception e) {
            throw new XMLParserException (eLabel + e);
        }
    }

    /**
     * Parse the XML File into a DOM
     *
     * @param xmlFile   File to parse
     * @return  Root element of DOM
     * @throws XMLParserException
     */
    public static Element parseXML (File xmlFile) throws XMLParserException {
        final String eLabel = "XMLComparitor.parseXML: ";
        if (xmlFile == null) {
            return null;
        }
        try {
            // Get the factory
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            // Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // Parse using builder to get DOM representation of the XML file
            Document dom = db.parse(xmlFile);
            // Get the root element
            return dom.getDocumentElement();
        } catch (Exception e) {
            throw new XMLParserException (eLabel + e);
        }
    }

    /**
     * Tests if the passed in String is a match to the String passed into constructor
     * of this class
     *
     * @param xml
     * @return true is match, false if otherwise
     * @throws XMLParserException
     */
    public boolean isMatch (String xml) throws XMLParserException {
        final String eLabel = "XMLComparitor.isMatch: ";
        try {
            Element rootElement = parseXML(xml);
            try {
                matchElement(templateRootElement, rootElement);
            } catch (Exception ex) {
                //System.out.println(eLabel + ex);
                return false;
            }
        } catch (Exception e) {
            throw new XMLParserException(eLabel + e);
        }
        return true;
    }

    /**
     * Tests if the passed in String is a match to the String passed into constructor
     * of this class
     *
     * @param xml
     * @return true if match, false if otherwise
     * @throws XMLParserException
     */
    public boolean isMatch (Element xml) throws XMLParserException {
        final String eLabel = "XMLComparitor.isMatch: ";
        try {
            matchElement(templateRootElement, xml);
        } catch (Exception ex) {
            //System.out.println(eLabel + ex);
            return false;
        }
        return true;
    }

    /**
     * Will continue to try and match the two DOMS until it exhausts all nodes and possible
     * matches, ultimately throwing an exception that will end the recursive traversal of the node
     * trees. As long as the compared has a subset defined in the comparator there will be no exceptions
     * thrown.
     *
     * @param comparitor
     * @param compared
     * @throws XMLParserException
     * @throws XMLComparitorException
     */
    private void matchElement (Node comparitor, Node compared) throws XMLParserException, XMLComparitorException {
        final String eLabel = "XMLComparitor.matchElement: ";
        boolean omit = false;
        try {
            NodeList nl = comparitor.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                omit = (getAttribute(nl.item(i),OMIT_FLAG) != null);
                if (omit) {
                    mOmit = true;
                }
                if(!nl.item(i).hasChildNodes()) {
                    if (nl.item(i).getNodeValue() == null) {
                        Vector list = getMatchingChildNodes(compared, nl.item(i).getNodeName());
                        if (list == null || list.size() < 1) {
                            throw new Exception(eLabel + "Missing node. See node:[" + nl.item(i).getNodeName() + "]");
                        }
                        boolean failure = true;
                        for (int j = 0; j < list.size(); j++) {
                            if (((Node)list.elementAt(j)).getFirstChild() == null) {
                                try {
                                    compareAttributes(nl.item(i), (Node)list.elementAt(j));
                                    failure = false;
                                    break;
                                } catch (Exception e) {
                                    if (j == list.size() - 1) {
                                        throw new Exception ("Exception checking empty nodes: " + e);
                                    }
                                }
                            }
                        }
                        if (!failure) {
                            continue;
                        }
                        throw new Exception(eLabel + "Nested node encountered in Compared where there is blank in Comparator. See node:[" + nl.item(i).getNodeName() + "]");
                    } else {
                        boolean match = false;
                        NodeList children = compared.getChildNodes();
                        for (int j = 0; j < children.getLength(); j++) {
                            match = nl.item(i).getNodeValue().equals(children.item(j).getNodeValue());
                            if (match) {
                                if (mOmit) {
                                    throw new XMLComparitorException("Omit match found. Element:[" + comparitor.getNodeName() + "]");
                                }
                                break;
                            }
                        }
                        if (!match) throw new XMLParserException(eLabel + "XML mismatch. Leaf match not found for: [" + comparitor.getNodeName() + "][" + nl.item(i).getNodeValue() + "]");
                    }
                } else {
                    Vector list = getMatchingChildNodes(compared, nl.item(i).getNodeName());
                    if ( list.size() < 1 ) {
                        throw new XMLParserException(eLabel + "Node name:[" + nl.item(i).getNodeName() + "] does not exist");
                    } else {
                        for (int j = 0; j < list.size(); j++) {
                            try {
                                compareAttributes(nl.item(i), (Node)list.elementAt(j));
                                try {
                                    matchElement(nl.item(i), (Node)list.elementAt(j));
                                } catch (XMLComparitorException ex) {
                                    throw new XMLComparitorException("" + ex);
                                } catch (Exception ex) {
                                    throw new XMLParserException("" + ex);
                                }
                                break;
                            } catch (XMLParserException exception) {
                                if (j == list.size() - 1) {
                                    if (omit) {
                                        mOmit = false;
                                        break;
                                    }
                                    throw new Exception (eLabel + exception);
                                }
                            }
                        }
                    }
                }
            }
        } catch (XMLComparitorException e) {
            throw new XMLComparitorException(eLabel + e);
        } catch (Exception e) {
            throw new XMLParserException(eLabel + e);
        }
    }

    /**
     * Will compare the attributes of a node, throwing an exception if a mismatch is encountered.
     * The attribute "omit" will be ignored here. As long as compared contains the attributes that
     * are in comparator and their corresponding values, this will not throw an exception.
     *
     * @param comparitor
     * @param compared
     * @throws XMLParserException
     */
    private void compareAttributes(Node comparitor, Node compared) throws XMLParserException {
        final String eLabel = "XMLComparitor.compareAttributes: ";
        try {
            //System.out.println(eLabel + "Comparing attributes to tags:[" + comparitor.getNodeName() + "][" +  compared.getNodeName() + "]");
            NamedNodeMap comparitorMap = comparitor.getAttributes();
            NamedNodeMap comparedMap = compared.getAttributes();
            if (comparitorMap == null && comparedMap == null) {
                // This is ok, both nodes are not Elements
                return;
            }
            for (int i = 0; i < comparitorMap.getLength(); i++) {
                Node comparatorNode = comparitorMap.item(i);
                String key = comparatorNode.getNodeName();
                // If this is an omit special request just skip the
                // key word here. We understand it is a reserve word
                // and we will take care of it above
                if (key.toUpperCase().equals(OMIT_FLAG.toUpperCase())) {
                    continue;
                }
                String value = comparatorNode.getNodeValue();
                //System.out.println(eLabel + key + " " + value);
                Node comparedNode = comparedMap.getNamedItem(key);
                if (comparedNode == null) {
                    throw new Exception("Attribute [" + key + "] not found in compared XML tag [" + comparitor.getNodeName() + "]");
                }
                String valueToCompare = comparedNode.getNodeValue();
                //System.out.println(eLabel + key + " " + valueToCompare);
                if (!value.equals(valueToCompare)) {
                    throw new Exception("Value of attribute [" + key + "] not equal in compared XML tag [" + comparitor.getNodeName() + "]");
                }
            }
        } catch (Exception e) {
            throw new XMLParserException(eLabel + e);
        }
    }

    public static String getAttributeOrError(Node node, String attrName) throws XMLParserException {
        final String aValue = XMLComparitor.getAttribute(node, attrName);
        if( aValue == null || aValue.trim().equals("") ) {
            throw new XMLParserException(attrName+" attribute must exist!");
        }
        return aValue.trim();
    }

    public static String getAttributeOrNull(Node node, String attrName) throws XMLParserException {
        final String aValue = XMLComparitor.getAttribute(node, attrName);
        if( aValue == null || aValue.trim().equals("") ) {
            return null;
        }
        return aValue.trim();
    }

    /**
     * Get the attribute specified for a node
     *
     * @param node
     * @param matchString
     * @return String value of attribute
     * @throws XMLParserException
     */
    public static String getAttribute(Node node, String matchString) throws XMLParserException {
        final String eLabel = "XMLComparitor.getAttribute: ";
        String returnString = null;
        try {
            NamedNodeMap map = node.getAttributes();
            if (map == null) {
                // This is ok, both nodes are not Elements
                return null;
            }
            for (int i = 0; i < map.getLength(); i++) {
                Node comparatorNode = map.item(i);
                String key = comparatorNode.getNodeName();
                if (key.toUpperCase().equals(matchString.toUpperCase())) {
                    returnString =  comparatorNode.getNodeValue();
                }
            }
        } catch (Exception e) {
            throw new XMLParserException(eLabel + e);
        }
        return returnString;
    }

    /**
     * Get a Vector list of the children of this node matching this name
     *
     * @param node
     * @param name
     * @return Vector<Node>
     * @throws XMLParserException
     */
    public static Vector<Node> getMatchingChildNodes (Node node, String name) throws XMLParserException {
        final String eLabel = "XMLComparitor.getMatchingChildNodes: ";
        Vector<Node> nodeVector = new Vector<Node>();
        try {
            NodeList nl = node.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i).getNodeName().equals(name)) {
                    nodeVector.add(nl.item(i));
                }
            }
        } catch (Exception e) {
            throw new XMLParserException(eLabel + e);
        }
        return nodeVector;
    }

    /**
     * Return the highest node of a list, based on its long value
     *
     * @param list              The list to search
     * @param attributeName     The attribute to parse as a long
     * @return Node
     * @throws Exception
     */
    public static Node getLargestChildNodeLong (NodeList list, String attributeName) throws Exception {
        final String eLabel = "XMLComparitor.getLargestChildNodeLong: ";
        try {
            Node highestNode = null;
            for (int i = 0; i < list.getLength(); i++) {
                if (highestNode == null || Long.parseLong(getAttribute(list.item(i), attributeName)) >
                    Long.parseLong(getAttribute(highestNode, attributeName))) {
                    highestNode = list.item(i);
                }
            }
            return highestNode;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    /**
     * Return the highest node of a list, based on its float value
     *
     * @param list              The list to search
     * @param attributeName     The attribute to parse as a float
     * @return Node
     * @throws Exception
     */
    public static Node getLargestChildNodeFloat (NodeList list, String attributeName) throws Exception {
        final String eLabel = "XMLComparitor.getLargestChildNodeFloat: ";
        try {
            Node highestNode = null;
            for (int i = 0; i < list.getLength(); i++) {
                if (highestNode == null || Float.parseFloat(getAttribute(list.item(i), attributeName)) >
                    Float.parseFloat(getAttribute(highestNode, attributeName))) {
                    highestNode = list.item(i);
                }
            }
            return highestNode;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    /**
     * Get matching child node, allow a create if none exist...
     *
     * @param doc               The document object to traverse
     * @param parentNode        The parent node of the document
     * @param tagValue          The value to search for
     * @param create            If true, create a node if not found
     * @param attributeName     The name of the attribute to use for a value
     * @return Node
     * @throws Exception
     */
    public static Node getChildNode (Document doc, Node parentNode, String tagValue, boolean create, String attributeName) throws Exception {
        final String eLabel = "XMLComparitor.getChildNode: ";
        try {
            Node child = null;
            NodeList list = parentNode.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                if (tagValue.equals(XMLComparitor.getAttribute(list.item(i), attributeName))) {
                    return list.item(i);
                }
            }
            if (create) {
                child = addNewChildNode(doc, parentNode, tagValue, attributeName);
            }
            return child;
        } catch (Exception e) {
            throw new Exception (eLabel + e);
        }
    }

    /**
     * Add a new child node with the given attribute value for the "tag" attribute
     *
     * @param doc               The document object to traverse
     * @param parentNode        The parent node of the document
     * @param tagValue          The value to search for
     * @param attributeName     The name of the attribute to use for a value
     * @return Node
     * @throws Exception
     */
    public static Node addNewChildNode (Document doc, Node parentNode, String tagValue, String attributeName) throws Exception {
        final String eLabel = "XMLComparitor.addNewChildNode: ";
        try {
            Node newChildNode = doc.createElement("node");
            NamedNodeMap childAtts = newChildNode.getAttributes();
            Attr tag = doc.createAttribute(attributeName);
            tag.setValue(tagValue);
            childAtts.setNamedItem(tag);
            parentNode.appendChild(newChildNode);
            return newChildNode;
        } catch (Exception e) {
            throw new Exception (eLabel + e);
        }
    }

    /**
     * Main to run as example for implementation. Typically, no files will be
     * accessed. We will use only Strings, but can use files. Also, make sure the
     * files and the strings have no new lines or white space around tags. This
     * breaks the logic of this class because of parser inconsistencies.
     *
     * @param args
     */
    public static void main (String[] args) {
        try {
            if (args.length != 2) {
                System.out.println("useage: cmd comparator compared");
                return;
            }

            File file1 = new File(args[0]);
            File file2 = new File(args[1]);

            XMLComparitor xmlc = new XMLComparitor(parseXML(file1));
            if (xmlc.isMatch(parseXML(file2))) {
                System.out.println("MATCH");
            } else {
                System.out.println("NON_MATCH");
            }
        } catch (Exception e) {
            System.out.println("" + e);
        }
    }

    public Element getDomRootElement () {
        return templateRootElement;
    }

    private Element templateRootElement;
    private boolean mOmit;
    private final static String OMIT_FLAG = "omit";

}
