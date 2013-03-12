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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.log4j.Logger;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.api.Nyxlet;
import org.cyclades.engine.util.GenericXMLObject;
import org.cyclades.io.Jar;
import org.cyclades.xml.comparitor.XMLComparitor;
import org.cyclades.xml.parser.XMLParserException;
import org.cyclades.xml.parser.api.XMLGeneratedObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.google.common.io.ByteStreams;
import com.google.common.base.Strings;

public class NyxletRepository {

    static Logger logger = Logger.getLogger(NyxletRepository.class);

    public NyxletRepository() { }

    public static NyxletRepository getStaticInstance () throws Exception {
        nyxletRepository.compareAndSet(null, new NyxletRepository());
        return nyxletRepository.get();
    }

    /**
     * No need to synchronize this...as it is called from within a synchronization mechanism
     *
     * @param jarPath
     * @throws Exception
     */
    private void loadNyxletFile(final File nyxlet) throws Exception {
        final String jarPath = nyxlet.getAbsolutePath();
        final String nyxlet_file_name = Strings.nullToEmpty(nyxlet.getName()).trim();
        final String implied_nyxlet_name;
        try {
            implied_nyxlet_name = nyxlet_file_name.substring(0, nyxlet_file_name.length() - 7);
        } catch(IndexOutOfBoundsException ex) {
            throw new Exception("Invalid nyxlet file name '"+jarPath+"'");
        }
        XMLGeneratedObject xmlo = null;
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(jarPath);
            xmlo = new GenericXMLObject(this.getManifestXMLString(jarFile));
            Element rootElement = xmlo.getRootElement();
            if (rootElement == null) throw new XMLParserException("Root element is null");
            NodeList nyxletList = rootElement.getElementsByTagName("nyxlet");
            if (nyxletList.getLength() < 1) {
                throw new Exception("\"nyxlet\" element is not defined in manifest!");
            }
            final Node nyxletXMLNode = nyxletList.item(0);
            String name = XMLComparitor.getAttributeOrError(nyxletXMLNode, "name");
            if( name == null ) {
                throw new Exception("Invalid nyxlet_manifest.xml in '"+jarPath+"'");
            }
            if( !name.equals(implied_nyxlet_name) ) {
                throw new Exception("In '"+jarPath+"' declared nyxlet name '"+name+"' does not match with nyxlet file name.");
            }
            Properties properties = Jar.getJarManifestMainAttributes(jarFile, JAR_MANIFEST);
            jarFile.close();
            jarFile = null;
            this.addNyxlet(Nyxlet.valueOf(nyxletXMLNode, this.loadLibrary(jarPath), properties));
        } finally {
            if( xmlo != null ) { xmlo.cleanUp(); }
            if( jarFile != null ) { jarFile.close(); }
        }
    } // end of loadNyxletFile(...)

    /**
     * Load the library part of the Nyxlet file. Please note that the
     * Class Loading scheme will be either Collective or Isolated based on the
     * variable "globalClassLoader" not being null or being null...respectively.
     *
     * XXX - No need to synchronize, called locally from synchronized method
     *
     * @param jarPath   Path to the jar file to load
     * @return The Class Loader to use for the service module
     * @throws Exception
     */
    private ClassLoader loadLibrary (String jarPath) throws Exception {
        File file = new File(jarPath);
        if (!file.exists()) {
            throw new Exception("File "+file.getName()+" does not exist.");
        }
        URL[] URLArray = { new URL("file", null, jarPath) };
        if (this.globalClassLoader == null) {
            return new DeferredDelegationURLClassLoader(URLArray, this.getClass().getClassLoader());
        }
        this.globalClassLoader = new URLClassLoader(URLArray, this.globalClassLoader);
        return this.globalClassLoader;
    } // end of loadLibrary(...)

    /**
     * No need to synchronize, called locally from a synchronized method
     *
     * @param nyxlet
     * @throws Exception
     */
    private void addNyxlet(final Nyxlet nyxlet) throws Exception {
        this.nyxletRepositoryMap.put(nyxlet.getName(), nyxlet);
        if (nyxlet.getRRDString() != null) {
            this.RRDStore.addServiceModule(nyxlet);
        }
    } // end of addNyxlet(...)

    /**
     * No need to synchronize, called locally from a synchronized method
     *
     * @param jarFile
     * @return the Nyxlet Manifest as a String
     * @throws Exception
     */
    private String getManifestXMLString (JarFile jarFile) throws Exception {
        final String eLabel = "NyxletRepository.getManifestXMLString: ";
        InputStream jarInputStream = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            JarEntry jarEntry = (JarEntry)jarFile.getEntry(NYXLET_MANIFEST);
            if (jarEntry == null) {
                throw new Exception("Could not locate " + NYXLET_MANIFEST  + " in jar file");
            }
            jarInputStream = jarFile.getInputStream(jarEntry);
            return new String(ByteStreams.toByteArray(jarInputStream));
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        } finally {
            try { jarInputStream.close(); } catch (Exception e) {};
            try { baos.close(); } catch (Exception e) {};
        }

    }

    public synchronized Set<String> keys () {
        return this.nyxletRepositoryMap.keySet();
    }

    public synchronized Nyxlet getNyxlet(Object key) {
        return this.nyxletRepositoryMap.get(key);
    }

    public Nyxlet getNyxletRRD (String RRDCategory, NyxletSession sessionDelegate) throws Exception {
        final String eLabel = "NyxletRepository.getNyxletRRD: ";
        try {
            // Detecting a service module may be very expensive...we should not synchronize on
            // this entire task...as it can lock up other service accesses...which would be quite bad.
            // Let's lock up on getting the actual RRD mechanism (in case of reload)...then release the intrinsic lock for
            // the search algorithm
            RESTfulDispatchServiceDelegateStrategy RRDStorePointer = this.getRRDStore(); // This is calling a synchronized method!
            // Get the service module from the RRD mechanism and return here. From this point on, this need not be synchronized.
            return RRDStorePointer.getServiceModuleMatch(RRDCategory, sessionDelegate);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    private synchronized RESTfulDispatchServiceDelegateStrategy getRRDStore () {
        return this.RRDStore;
    }


    /** See documentation for loadNyxletFiles(...) */
    public synchronized int reloadNyxletFiles (String[] nyxletDirectories, final boolean failOnError, final boolean isolatedClassLoader)
        throws Exception
    {
        this.destroy();
        return this.loadNyxletFiles(nyxletDirectories, failOnError, isolatedClassLoader);
    }

    /**
     * Load all of the Nyxlets files from the given directories
     *
     * @param nyxletDirectories
     * @param failOnError
     * @param isolatedClassLoader
     * @return number of loaded Nyxlet libraries
     * @throws Exception
     */
    public synchronized int loadNyxletFiles(final String[] nyxletDirectories,
                                            final boolean failOnError,
                                            final boolean isolatedClassLoader)
        throws Exception
    {
        try {
            // let GC take care of the previous copy when all clients are done with it
            this.nyxletRepositoryMap = new ConcurrentHashMap<String, Nyxlet>();
            // Do the same with the RRDStore
            this.RRDStore = new RESTfulDispatchServiceDelegateStrategy();
            // Null this out here to start from scratch
            this.globalClassLoader = null;
            // If this is  Collective class loading...initialize the global class loader to flag this state
            if (!isolatedClassLoader) {
                this.globalClassLoader = this.getClass().getClassLoader();
            }
            int loadedLibraryCount = 0;
            File libDirectory;
            File[] fileList;
            for (String nyxletDirectory : nyxletDirectories) {
                if (nyxletDirectory.startsWith("http")) {
                    logger.info("Skipping Nyxlet load of http URL (used to load properties only): " + nyxletDirectory);
                    continue;
                }
                libDirectory = new File(nyxletDirectory);
                if (!libDirectory.exists()) {
                    throw new Exception("Directory " + nyxletDirectories + " does not exist!");
                }
                if (!libDirectory.isDirectory()) {
                    throw new Exception("Path " + nyxletDirectories + " is a file and not a directory!");
                }
                if( logger.isInfoEnabled() ) {
                    logger.info("Loading nyxlets from "+libDirectory.getAbsolutePath());
                }
                fileList = libDirectory.listFiles();
                for (int i = 0; i < fileList.length; i++) {
                    try {
                        if (fileList[i].getName().endsWith(NYXLET_SUFFIX)) {
                            this.loadNyxletFile(fileList[i]);
                            loadedLibraryCount++;
                        }
                    } catch (Exception ex) {
                        if (failOnError) {
                            throw ex;
                        }
                        logger.error(ex, ex);
                    }
                }
            }
            return loadedLibraryCount;
        } catch (Exception ex) {
            // Destroy any resources already created, fail silently
            try { destroy(); } catch (Exception e) {}
            // Clear out the repository so nothing is "partly" working
            this.nyxletRepositoryMap.clear();
            // Do the same with the RRDStore
            this.RRDStore.clear();
            throw ex;
        }
    }

    public synchronized int size () {
        return this.nyxletRepositoryMap.size();
    }

    /**
     * Life cycle support
     *
     * @throws Exception
     */
    public synchronized void destroy () throws Exception {
        final NyxletRepository ndr = NyxletRepository.getStaticInstance();
        for (Object key : ndr.keys()) {
            ndr.getNyxlet(key).destroy();
        }
    }

    private static AtomicReference<NyxletRepository> nyxletRepository     = new AtomicReference<NyxletRepository>(null);
    private ConcurrentHashMap<String, Nyxlet> nyxletRepositoryMap         = new ConcurrentHashMap<String, Nyxlet>();
    private RESTfulDispatchServiceDelegateStrategy RRDStore               = new RESTfulDispatchServiceDelegateStrategy();
    private ClassLoader globalClassLoader                                 = null;
    private static final String NYXLET_MANIFEST = "nyxlet_manifest.xml";
    private static final String JAR_MANIFEST    = "META-INF/MANIFEST.MF";
    private static final String NYXLET_SUFFIX   = ".nyxlet";
    
}
