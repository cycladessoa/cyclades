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

import java.util.Hashtable;
import java.util.Enumeration;

import org.cyclades.xml.generator.XMLGeneratorException;
import org.cyclades.xml.generator.XMLWriter;

public class Criterion {
    public Criterion (Object name) {
        this.name = name;
    }

    @SuppressWarnings("unchecked")
        public void addConstraint (Object constraint, boolean omit) throws XMLComparitorException {
        final String eLabel = "Criterion.addConstraint: ";
        try {
            // Hashtable ensures no dupes
            constraintHash.put(constraint, new Constraint(constraint, omit));
        } catch (Exception e) {
            throw new XMLComparitorException(eLabel + e);
        }

    }

    public void removeConstraint (Object constraint) throws XMLComparitorException {
        final String eLabel = "Criterion.removeConstraint: ";
        try {
            // Hashtable ensures no dupes
            constraintHash.remove(constraint);
        } catch (Exception e) {
            throw new XMLComparitorException(eLabel + e);
        }

    }

    public String toString () {
        return name.toString();
    }

    @SuppressWarnings("unchecked")
        public String toXMLString () throws XMLGeneratorException {
        final String eLabel = "Person.toXMLString: ";
        try {
            StringBuffer sb = new StringBuffer();
            Hashtable atts = new Hashtable();
            atts.put("name", name);
            XMLWriter writer = new XMLWriter(null, "criterion", atts);
            Enumeration hashEnum = constraintHash.keys();
            boolean hasNonOmitNodes = false;
            while (hashEnum.hasMoreElements()) {
                String key = (String) hashEnum.nextElement();
                Constraint constraint = (Constraint) constraintHash.get(key);
                Hashtable atts2 = null;
                if (constraint.omit()) {
                    atts2 = new Hashtable();
                    atts2.put("omit", "true");
                    atts2.put("name", name);
                    XMLWriter writer2 = new XMLWriter(null, "criterion", atts2);
                    writer2.addLeafNode("constraint", null, constraint.getValue().toString(), false);
                    writer2.done();
                    sb.append(writer2.toString());
                } else {
                    hasNonOmitNodes = true;
                    writer.addLeafNode("constraint", null, constraint.getValue().toString(), false);
                }
            }
            writer.done();
            if (hasNonOmitNodes) {
                return writer.toString() + sb.toString();
            } else {
                // Return only the omit criterion so we do not have an empty element
                return sb.toString();
            }
        } catch (Exception e) {
            throw new XMLGeneratorException(eLabel + e);
        }
    }

    public int size () {
        return constraintHash.size();
    }

    public boolean constraintExists (Object constraint) throws XMLComparitorException {
        final String eLabel = "Criterion.constraintExists: ";
        try {
            return constraintHash.containsKey(constraint);
        } catch (Exception e) {
            throw new XMLComparitorException(eLabel + e);
        }
    }

    private Object name;
    private Hashtable constraintHash = new Hashtable();
}
