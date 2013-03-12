package org.cyclades.io;

import java.io.InputStream;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class Jar {
    
    public static Properties attributesToProperties (Attributes attributes) {
        Properties jarManifestProperties = new Properties();
        for (java.util.Map.Entry<Object, Object> attributeEntry : attributes.entrySet()) {
            jarManifestProperties.setProperty(attributeEntry.getKey().toString(), attributeEntry.getValue().toString());
        }
        return jarManifestProperties;
    }
    
    public static Attributes getJarManifestMainAttributes (String uri, String manifestPath) throws Exception {
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

    public static Attributes getJarManifestMainAttributes (JarFile jarFile, String manifestPath) throws Exception {
        final String eLabel = "Jar.getJarManifestProperties: ";
        InputStream jarFileInputStream = null;
        try {
            ZipEntry entry = jarFile.getEntry(manifestPath);
            if (entry == null) return new Attributes();
            jarFileInputStream = jarFile.getInputStream((JarEntry)entry);
            return getJarManifestMainAttributes(jarFileInputStream);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        } finally {
            try { jarFileInputStream.close(); } catch (Exception e) {};
        }
    }
    
    public static Attributes getJarManifestMainAttributes (InputStream is) throws Exception {
        return new Manifest(is).getMainAttributes();
    }
    
}
