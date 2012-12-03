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
package org.cyclades.engine.nyxlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;

public class BuildNyxletManifest {

    private static boolean quiet = false;

    private static void help_and_exit() {
        System.out.println("./BuildNyxletManifiest [-q] NYXLET_FILE_NAME");
        System.exit(1);
    } // end of help()

    public static void main(String[] cmd) {
        File tempJarFile = null;
        File nyxletFile = null;
        boolean success = false;
        try {
            if( cmd == null || cmd.length == 0 ) {
                help_and_exit();
            }

            String nyxletFileName = null;
            for( final String p : cmd ) {
                if( p.equals("-q") ) {
                    quiet = true;
                } else {
                    nyxletFileName = p;
                }
            }
            if( nyxletFileName == null ) {
                help_and_exit();
            }

            nyxletFile = new File(nyxletFileName);
            if( !nyxletFile.exists() ) {
                System.err.println("File "+nyxletFileName+" does not exist");
                System.exit(1);
            }
            if( !nyxletFile.canRead() ) {
                System.err.println("File "+nyxletFileName+" is not readable");
                System.exit(1);
            }
            if( !nyxletFile.canWrite() ) {
                System.err.println("File "+nyxletFileName+" is not writable");
                System.exit(1);
            }
            final String nyxletFileNameOnly = nyxletFile.getName();
            Pattern p = Pattern.compile("^([^-]+).*\\.nyxlet$");
            Matcher m = p.matcher(nyxletFileNameOnly);
            if( !m.matches() ) {
                System.err.println("File "+nyxletFileName+" is not appropriate for nyxlet.");
                System.exit(1);
            }
            final String derrivedNyxletName = m.group(1);
            if( derrivedNyxletName == null || derrivedNyxletName.trim().length() == 0 ) {
                System.err.println("Unable to derrive nyxlet name from file name "+nyxletFileName);
                System.exit(1);
            }

            final JarFile nyxlet = new JarFile(nyxletFile);

            final ZipEntry nManifest = nyxlet.getEntry("nyxlet_manifest.xml");

            byte[] nyxlet_manifest_data = null;
            final InputStream in = nyxlet.getInputStream(nManifest);
            try {
                nyxlet_manifest_data = ByteStreams.toByteArray(in);
            } finally {
                if( in != null ) { in.close(); }
            }

            // read content of the manifest
            PickManifest manifestReader = new PickManifest();
            XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            reader.setContentHandler(manifestReader);
            reader.parse(new InputSource(new ByteArrayInputStream(nyxlet_manifest_data)));

            if( manifestReader.nyxletClassName != null && manifestReader.nyxletName != null && manifestReader.actionHandlersDefs != null ) {
                // nothing do be done
                if( !quiet ) {
                    System.out.println("There are no modification to be applied to the nyxlet.");
                }
                System.exit(0);
            }

            AtomicReference<String> derrivedNyxletClass = new AtomicReference<String>(null);
            final LinkedHashMap<String,LinkedHashMap<String, String>> aHandlers = inspectNyxletFile(nyxlet, derrivedNyxletClass);

            String derrivedNyxletClassName = null;
            if( manifestReader.nyxletClassName == null ) {
                derrivedNyxletClassName = derrivedNyxletClass.get();
            }

            String derrivedAcctionHandlersDefs = null;
            if( manifestReader.actionHandlersDefs == null || manifestReader.actionHandlersDefs.trim().length() == 0 ) {
                derrivedAcctionHandlersDefs = "";
                for( String actionName : aHandlers.keySet() ) {
                    final LinkedHashMap<String, String> classesForAction = aHandlers.get(actionName);
                    if( classesForAction != null && classesForAction.size() > 0 ) {
                        for( String classForAction : classesForAction.keySet() ) {
                            derrivedAcctionHandlersDefs +=
                                (derrivedAcctionHandlersDefs.length() > 0 ? "\n" : "") + actionName + "=" + classForAction;
                        }
                    }
                }
            }

            if( manifestReader.nyxletClassName    != null && manifestReader.nyxletName   != null &&
                manifestReader.actionHandlersDefs == null &&
                ( derrivedAcctionHandlersDefs == null || derrivedAcctionHandlersDefs.trim().length() == 0 ) )
            {
                // nothing do be done
                if( !quiet ) {
                    System.out.println("There are no modification to be applied to the nyxlet.");
                }
                System.exit(0);
            }

            final ByteArrayOutputStream newManifest = new ByteArrayOutputStream(16384); // 16K
            final ManifestXMLFilter manifestFilter =
                    new ManifestXMLFilter(derrivedNyxletName, derrivedNyxletClassName, derrivedAcctionHandlersDefs);

            final Transformer t = SAXTransformerFactory.newInstance().newTransformer();
            reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            manifestFilter.setParent(reader);
            reader = manifestFilter;
            t.transform(new SAXSource(reader, new InputSource(new ByteArrayInputStream(nyxlet_manifest_data))),
                        new StreamResult(newManifest));

            // Now we have new manifest. First we are going to write new jar file and then move it over the old one.
            tempJarFile = new File(nyxletFileName + ".tmp");
            final JarOutputStream tempJar = new JarOutputStream(new FileOutputStream(tempJarFile));

            final JarEntry newNyxletManifestEntry = new JarEntry("nyxlet_manifest.xml");
            tempJar.putNextEntry(newNyxletManifestEntry);
            tempJar.write(newManifest.toByteArray());
            tempJar.closeEntry();

            Enumeration<JarEntry> nyxletEntries = nyxlet.entries();
            while( nyxletEntries.hasMoreElements() ) {
                JarEntry nyxletEntry = nyxletEntries.nextElement();
                if( !nyxletEntry.getName().equals("nyxlet_manifest.xml") ) {
                    //boolean closeEntry = false;
                    //try {
                        tempJar.putNextEntry(nyxletEntry);
                        //closeEntry = true;
                        final InputStream nyxletEntryIS = nyxlet.getInputStream(nyxletEntry);
                        try {
                            ByteStreams.copy(nyxletEntryIS, tempJar);
                        } finally {
                            try { if( nyxletEntryIS != null ) { nyxletEntryIS.close(); } } catch(Exception ignore ) { }
                        }
                        tempJar.closeEntry();
                    //} catch(ZipException ignore) {
                    //} finally {
                    //    if( closeEntry ) { tempJar.closeEntry(); }
                    //}
                }
            }

            tempJar.close();
            success = true;

        } catch(Exception ex) {
            ex.printStackTrace();

        } finally {
            if( success ) {
                tempJarFile.renameTo(nyxletFile);
                if( !quiet ) {
                    System.out.println("Nyxlet file has been modified.");
                }
                System.exit(0);

            } else {
                try { if( tempJarFile != null ) { tempJarFile.delete(); } } catch(Exception ignore) { }
            }
        }
        System.exit(1);
    } // end of main(...)

    private static final class ManifestXMLFilter extends XMLFilterImpl {

        private Stack<String> currentXPath = new Stack<String>();

        private String nyxletName;
        private String nyxletClassName;
        private String actionHandlersDefs;
        private String manifest_nyxlet_attribute_NAME = null;
        private boolean suppressChars = false; // suppress

        public ManifestXMLFilter(String nyxletName, String nyxletClassName, String actionHandlersDefs) {
            super();
            this.nyxletName         = nyxletName;
            this.nyxletClassName    = nyxletClassName;
            this.actionHandlersDefs = actionHandlersDefs;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts)
            throws SAXException
        {
            final String name = qName != null ? qName : localName;
            this.currentXPath.push(name == null ? "UKNOWN" : name);

            final String cXPath = "/"+Joiner.on("/").join(this.currentXPath);
            if( cXPath.equals("/manifest/nyxlet") ) {
                if( this.nyxletName != null ) {
                    atts = new AttributesImpl(atts);
                    int nameIndex = ((AttributesImpl) atts).getIndex("name");
                    if( nameIndex != -1 ) {
                        ((AttributesImpl) atts).removeAttribute(nameIndex);
                    }
                    ((AttributesImpl) atts).addAttribute("", "name", "name", "CDATA", this.nyxletName);
                }
                if( this.nyxletClassName != null ) {
                    atts = new AttributesImpl(atts);
                    int classIndex = ((AttributesImpl) atts).getIndex("class");
                    if( classIndex != -1 ) {
                        ((AttributesImpl) atts).removeAttribute(classIndex);
                    }
                    ((AttributesImpl) atts).addAttribute("", "class", "class", "CDATA", this.nyxletClassName);
                }

            } else if( cXPath.equals("/manifest/nyxlet/attribute")) {
                this.manifest_nyxlet_attribute_NAME =
                        Strings.emptyToNull(Strings.nullToEmpty(atts.getValue("name")).trim());

            } else if( cXPath.equals("/manifest/nyxlet/attribute/value") &&
                       this.manifest_nyxlet_attribute_NAME != null &&
                       this.manifest_nyxlet_attribute_NAME.equals("actionHandlers") &&
                       this.actionHandlersDefs != null )
            {
                super.startElement(uri, localName, qName, atts);
                char[] achars = ("\n"+this.actionHandlersDefs+"\n").toCharArray();
                this.characters(achars, 0, achars.length);
                this.suppressChars = true;
                return;
            }

            super.startElement(uri, localName, qName, atts);
        } // end of startElement(...)

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            final String cXPath = "/"+Joiner.on("/").join(this.currentXPath);
            if( cXPath.equals("/manifest/nyxlet/attribute/value") ) {
                if( this.suppressChars ) { this.suppressChars = false; }

            } else if( cXPath.equals("/manifest/nyxlet/attribute")) {
                this.manifest_nyxlet_attribute_NAME = null;
            }
            this.currentXPath.pop();
        } // end of endElement(...)

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if( !this.suppressChars ) {
                super.characters(ch, start, length);
            }
        } // end of characters(...)

    } // end of class ManifestXMLFilter

    private static final class PickManifest extends DefaultHandler {

        private StringBuilder valueCapture              = new StringBuilder();
        private Stack<String> currentXPath              = new Stack<String>();
        private boolean captureAcctionHandlersDefs      = false;
        private boolean startCaptureAcctionHandlersDefs = false;
        private boolean endedCaptureAcctionHandlersDefs = false;

        public String nyxletName         = null;
        public String nyxletClassName    = null;
        public String actionHandlersDefs = null;

        public PickManifest() { super(); }

        @Override
        public void startElement(final String uri,   final String localName,
                                 final String qName, final Attributes attributes)
            throws SAXException
        {
            super.startElement(uri, localName, qName, attributes);

            final String name = qName != null ? qName : localName;
            this.currentXPath.push(name == null ? "UKNOWN" : name);

            final String cXPath = "/"+Joiner.on("/").join(this.currentXPath);
            if( cXPath.equals("/manifest/nyxlet") ) {
                this.nyxletName = Strings.emptyToNull(Strings.nullToEmpty(attributes.getValue("name")).trim());
                this.nyxletClassName = Strings.emptyToNull(Strings.nullToEmpty(attributes.getValue("class")).trim());

            } else if( cXPath.equals("/manifest/nyxlet/attribute") ) {
                final String attrName = Strings.nullToEmpty(attributes.getValue("name")).trim();
                if( attrName.length() > 0 && attrName.equals("actionHandlers") ) {
                    this.captureAcctionHandlersDefs = true;
                }

            } else if( cXPath.equals("/manifest/nyxlet/attribute/value") && this.captureAcctionHandlersDefs && !this.endedCaptureAcctionHandlersDefs ) {
                this.startCaptureAcctionHandlersDefs = true;
            }

            this.valueCapture = new StringBuilder();
        } // end of startElement(...)

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            final String cXPath = "/"+Joiner.on("/").join(this.currentXPath);
            if( cXPath.equals("/manifest/nyxlet/attribute/value") &&
                this.captureAcctionHandlersDefs &&
                this.startCaptureAcctionHandlersDefs &&
                !this.endedCaptureAcctionHandlersDefs )
            {
                this.endedCaptureAcctionHandlersDefs = true;
                if( this.valueCapture != null ) {
                    this.actionHandlersDefs = this.valueCapture.toString().trim();
                }
            }
            this.currentXPath.pop();
        } // end of endElement(...)

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            this.valueCapture.append(ch, start, length);
            super.characters(ch, start, length);
        } // end of characters(...)

    } // end of class PickManifest

    private static LinkedHashMap<String,LinkedHashMap<String, String>> inspectNyxletFile(final JarFile nyxletFile, final AtomicReference<String> nyxletClass)
        throws IOException, ClassNotFoundException
    {
        final LinkedHashMap<String,LinkedHashMap<String, String>> actionHandlers = new LinkedHashMap<String, LinkedHashMap<String,String>>();

        Enumeration<JarEntry> e = nyxletFile.entries();
        while( e.hasMoreElements() ) {
            JarEntry entry = e.nextElement();

            final String eName = entry.toString();
            if( entry.isDirectory() || !eName.endsWith(".class") ) { continue; }
            final InputStream in = nyxletFile.getInputStream(entry);
            ClassFile cf = new ClassFile(new DataInputStream(in));
            LinkedList<AnnotationsAttribute> annts = new LinkedList<AnnotationsAttribute>();
            AnnotationsAttribute invisibleAnnotations = (AnnotationsAttribute) cf.getAttribute(AnnotationsAttribute.invisibleTag);
            if( invisibleAnnotations != null ) { annts.push(invisibleAnnotations); }
            AnnotationsAttribute visibleAnnotations = (AnnotationsAttribute) cf.getAttribute(AnnotationsAttribute.visibleTag);
            if( visibleAnnotations != null ) { annts.push(visibleAnnotations); }

            for(AnnotationsAttribute anntattr : annts ) {
                final Annotation nyxletAnnt = anntattr.getAnnotation("org.cyclades.annotations.Nyxlet");
                if( nyxletAnnt != null ) {
                    nyxletClass.compareAndSet(null,  cf.getName());
                }
                final Annotation handlerAnnotation = anntattr.getAnnotation("org.cyclades.annotations.AHandler");
                if( handlerAnnotation != null ) {
                    MemberValue value = handlerAnnotation.getMemberValue("value");
                    if( value instanceof ArrayMemberValue ) {
                        for( MemberValue val : ((ArrayMemberValue)value).getValue() ) {
                            String valueName = null;
                            if( val instanceof StringMemberValue ) {
                                valueName = ((StringMemberValue)val).getValue();
                            }
                            if( valueName == null || valueName.trim().length() == 0 ) { continue; }

                            LinkedHashMap<String, String> aClasses = actionHandlers.get(val);
                            if( aClasses == null ) {
                                aClasses = new LinkedHashMap<String, String>();
                                actionHandlers.put(valueName.trim(), aClasses);
                            }
                            aClasses.put(cf.getName(), cf.getName());
                        }
                    }
                }
            }
        }
        return actionHandlers;
    } // and of inspectNyxletFile(...)

}
