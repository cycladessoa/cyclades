package org.cyclades.io;

import java.io.InputStream;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class Jar {
    
    public static Properties getJarManifestMainAttributes (String uri, String manifestPath) throws Exception {
        final String eLabel = "Jar.getJarManifestProperties: ";
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(uri);
            return getJarManifestMainAttributes(jarFile, manifestPath);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        } finally {
            try { jarFile.close(); } catch (Exception e) {};
        }
    }

    public static Properties getJarManifestMainAttributes (JarFile jarFile, String manifestPath) throws Exception {
        final String eLabel = "Jar.getJarManifestProperties: ";
        InputStream jarFileInputStream = null;
        try {
            ZipEntry entry = jarFile.getEntry(manifestPath);
            if (entry == null) return new Properties();
            jarFileInputStream = jarFile.getInputStream((JarEntry)entry);
            return getJarManifestMainAttributes(jarFileInputStream);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        } finally {
            try { jarFileInputStream.close(); } catch (Exception e) {};
        }
    }
    
    public static Properties getJarManifestMainAttributes (InputStream is) throws Exception {
        final String eLabel = "Jar.getJarManifestMainAttributes: ";
        Properties jarManifestProperties = new Properties();
        try {
            Manifest mf = new Manifest(is);
            for (java.util.Map.Entry<Object, Object> attributeEntry : mf.getMainAttributes().entrySet()) {
                jarManifestProperties.setProperty(attributeEntry.getKey().toString(), attributeEntry.getValue().toString());
            }
            return jarManifestProperties;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }
    
}
