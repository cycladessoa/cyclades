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

import java.util.Enumeration;
import java.util.Hashtable;

import org.cyclades.xml.generator.XMLGeneratorException;
import org.cyclades.xml.generator.XMLWriter;
import org.cyclades.xml.generator.api.XMLGeneratingObject;
import org.cyclades.xml.parser.XMLParserException;
import org.cyclades.xml.parser.api.XMLGeneratedObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Definition:
 * noun, plural. a standard of judgment or criticism; a rule or principle
 * for evaluating or testing something.
 *
 * This class is meant to represent a collection of Criterion that describe
 * the container class. This can be generated as XML so a comparison can be
 * made easily between two classes, or even a class and any generated XML.
 * See the XMLComparitor class of this package for more information and
 * examples.
 *
 * This class was created to allow for some easy to use API's for integration
 * with the XML Comparison libraries.
 *
 * (1)Criteria->(0-N)Criterion->(1-N)Constraint
 *
 */
public class Criteria extends XMLGeneratedObject implements XMLGeneratingObject {
    /**
     * General constructor.
     *
     */
    public Criteria () throws Exception {
        super(null);
    }

    /**
     * Constructor in case this needs to be created by passing in a hashtable.
     *
     * NOTE: The Hashtable parameter must be the exact format as if it were
     * retrieved by the getCriterionHashtable() method of this class!
     *
     * @param criterionHash
     */
    public Criteria (Hashtable criterionHash) throws Exception {
        super(null);
        this.criterionHash = criterionHash;
    }

    /**
     * Create this Object from an XML String. Constructor needs to call
     * super() and populate(). Call cleanUp() if you want to release the XML
     * DOM for Garbage Collection, or keep it around if you want a way
     * to reinitialize this class to defaults. Remember though that
     * keeping it around will waste memory.
     *
     * @param xmlString
     * @throws XMLParserException
     */
    public Criteria (String xmlString) throws XMLParserException {
        super(xmlString);
        // Populate this Object from the DOM created in super
        populate();
        // Release this Object's DOM for garbage collection
        cleanUp();
    }

    @SuppressWarnings("unchecked")
        private void addCriterion (Object criterionName) throws XMLComparitorException {
        final String eLabel = "Criteria.addCriterion: ";
        try {
            criterionHash.put(criterionName, new Criterion(criterionName));
        } catch (Exception e) {
            throw new XMLComparitorException(eLabel + e);
        }
    }

    private void removeCriterion (Object criterionName) throws XMLComparitorException {
        final String eLabel = "Criteria.removeCriterion: ";
        try {
            criterionHash.remove(criterionName);
        } catch (Exception e) {
            throw new XMLComparitorException(eLabel + e);
        }
    }

    /**
     * Add a constraint to this criteria representation.  Basically, a criteria
     * contains a number of criterion (0-N) which in turn contain a number of
     * constraints (1-N). For example, a criterion could be "cars" and the
     * constraints could be "Ford", and "Chevy".
     *
     * NOTE: Duplicate constratints entered override the previous one... so if
     * you enter cars->Ford twice, it will still have only one entry for
     * cars->Ford. Basically, consider it like a Hashtable.
     *
     * @param criterionName
     * @param constraintValue
     * @throws XMLComparitorException
     */
    public void addConstraint (Object criterionName, Object constraintValue) throws XMLComparitorException {
        addConstraint(criterionName, constraintValue, false);
    }

    /**
     * Add a constraint to this criteria representation.  Basically, a criteria
     * contains a number of criterion (0-N) which in turn contain a number of
     * constraints (0-N). For example, a criterion could be "cars" and the
     * constraints could be "Ford", and "Chevy".
     *
     * @param criterionName
     * @param constraintValue
     * @param omit
     * @throws XMLComparitorException
     */
    public void addConstraint (Object criterionName, Object constraintValue, boolean omit) throws XMLComparitorException {
        final String eLabel = "Criteria.addConstraint: ";
        try {
            Criterion criterion = (Criterion) criterionHash.get(criterionName);
            if (criterion == null) {
                addCriterion(criterionName);
                criterion = (Criterion) criterionHash.get(criterionName);
            }
            criterion.addConstraint(constraintValue, omit);
        } catch (Exception e) {
            throw new XMLComparitorException(eLabel + e);
        }
    }

    /**
     * Remove a constraint from a specific criterion.
     *
     * @param criterionName
     * @param constraint
     * @throws XMLComparitorException
     */
    public void removeConstraint (Object criterionName, Object constraint) throws XMLComparitorException {
        final String eLabel = "Criteria.removeConstraint: ";
        try {
            Criterion criterion = (Criterion) criterionHash.get(criterionName);
            if (criterion == null) {
                throw new Exception("Criterion " + criterionName + " does not exist!");
            }
            criterion.removeConstraint(constraint);
            // We don't need this criterion around if it does not contain
            // any constraints
            if (criterion.size() < 1) {
                removeCriterion(criterionName);
            }
        } catch (Exception e) {
            throw new XMLComparitorException(eLabel + e);
        }
    }

    /**
     * Does this constraint exist?
     *
     * @param criterionName
     * @param constraint
     * @return true if constraint exists, false otherwise
     * @throws XMLComparitorException
     */
    public boolean constraintExists (Object criterionName, Object constraint) throws XMLComparitorException {
        final String eLabel = "Criteria.constraintExists: ";
        try {
            Criterion criterion = (Criterion) criterionHash.get(criterionName);
            if (criterion == null) {
                return false;
            }
            return criterion.constraintExists(constraint);
        } catch (Exception e) {
            throw new XMLComparitorException(eLabel + e);
        }
    }

    /**
     * Represent this Criteria in an XML String
     *
     * <?xml version="1.0"?>
     *  <!DOCTYPE criteria [
     *    <!ELEMENT criteria    (criterion)>
     *    <!ELEMENT criterion   (constraint)>
     *    <!ATTLIST criterion name CDATA #REQUIRED>
     *    <!ATTLIST criterion omit CDATA #IMPLIED>
     *    <!ELEMENT constraint  (#PCDATA)>
     *    <!ELEMENT name (#PCDATA)>
     *  ]>
     */
    public String toXMLString () throws XMLGeneratorException {
        final String eLabel = "Person.toXMLString: ";
        try {
            XMLWriter writer = new XMLWriter(null, "criteria");
            Enumeration hashEnum = criterionHash.keys();
            while (hashEnum.hasMoreElements()) {
                String key = (String) hashEnum.nextElement();
                Criterion criterion = (Criterion) criterionHash.get(key);
                writer.addXML(criterion.toXMLString());
            }
            writer.done();
            return writer.toString();
        } catch (Exception e) {
            throw new XMLGeneratorException(eLabel + e);
        }
    }

    /**
     * Get the XML DOM for this criteria. Basically here for caching purposes.
     * We may want to cache our dom so we do not have to parse out the XML all
     * the time for performance.
     *
     * @return root Element
     * @throws XMLParserException
     * @throws XMLGeneratorException
     */
    public Element getXMLDOMRoot () throws XMLParserException, XMLGeneratorException {
        return XMLComparitor.parseXML(toXMLString());
    }

    /**
     * Get the criterion hashtable. May want to store this guy somehow.
     *
     * @return Criterion HashTable
     */
    public Hashtable getCriterionHashtable () {
        return criterionHash;
    }

    /**
     * Populate this objects local fields with the XML DOM provided
     * Here is the DTD so we know what the DOM looks like...
     ** <?xml version="1.0"?>
     *  <!DOCTYPE criteria [
     *    <!ELEMENT criteria    (criterion)>
     *    <!ELEMENT criterion   (constraint)>
     *    <!ATTLIST criterion name CDATA #REQUIRED>
     *    <!ATTLIST criterion omit CDATA #IMPLIED>
     *    <!ELEMENT constraint  (#PCDATA)>
     *    <!ELEMENT name (#PCDATA)>
     *  ]>
     */
    public void populate() throws XMLParserException {
        final String eLabel = "XMLParserExample.populate: ";
        try {
            // Root element comming in should be PersonRep
            Element rootElement = getRootElement();
            if (rootElement == null) {
                throw new Exception("Root element is null");
            }

            // Get all of the criterion
            NodeList criterionList = rootElement.getChildNodes();
            for (int i = 0; i < criterionList.getLength(); i++) {
                // Get criterion name and if this has an omit flag set
                String name = XMLComparitor.getAttribute(criterionList.item(i), "name");
                boolean omit = (XMLComparitor.getAttribute(criterionList.item(i), "omit") != null);
                // Get all of constraints for this criterion
                NodeList constraintList = criterionList.item(i).getChildNodes();
                for (int j = 0; j < constraintList.getLength(); j++) {
                    String constraint = constraintList.item(j).getFirstChild().getNodeValue();
                    if (constraint != null && !constraint.equals("")) {
                        addConstraint(name, constraint, omit);
                    }
                }

            }
        } catch (Exception e) {
            throw new XMLParserException(eLabel + e);
        }
    }

    private Hashtable criterionHash = new Hashtable();
}
