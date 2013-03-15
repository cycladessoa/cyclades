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
package org.cyclades.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import com.google.common.io.ByteStreams;

/**
 * This class will encapsulate some general methods for IO that have been
 * found to be rewritten frequently.
 *
 */
public class FileUtils {

    public static void writeToFile (String fromURI, String toURI) throws Exception {
        writeToFile (fromURI, toURI, StreamUtils.DEFAULT_BUFFER_SIZE);
    }
    public static void writeToFile (String fromURI, String toURI, int bufferSize) throws Exception {
        final String eLabel = "FileUtils.writeToFile(String, String): ";
        InputStream is = null;
        OutputStream os = null;
        try {
            if (bufferSize < 1) throw new Exception("Invalid buffer size: " + bufferSize);
            is = ResourceRequestUtils.getInputStream(fromURI, null);
            os = new FileOutputStream(new File(toURI));
            ByteStreams.copy(is, os);
        } catch (Exception e) {
            throw new Exception(eLabel + e);

        } finally {
            try {
                is.close();
            } catch (Exception e) {}
            try {
                os.close();
            } catch (Exception e) {}
        }
    }

    public static void writeToFile (byte[] data, String URI) throws Exception {
        writeToFile (data, URI, StreamUtils.DEFAULT_BUFFER_SIZE);
    }
    public static void writeToFile (byte[] data, String URI, int bufferSize) throws Exception {
        final String eLabel = "FileUtils.writeToFile(byte[], String): ";
        InputStream is = null;
        OutputStream os = null;
        try {
            if (bufferSize < 1) throw new Exception("Invalid buffer size: " + bufferSize);
            is = new ByteArrayInputStream(data);
            os = new FileOutputStream(new File(URI));
            ByteStreams.copy(is, os);
        } catch (Exception e) {
            throw new Exception(eLabel + e);

        } finally {
            try {
                is.close();
            } catch (Exception e) {}
            try {
                os.close();
            } catch (Exception e) {}
        }
    }

    /**
     * This method will create the directory denoted by the parameter if it doesn't
     * exist
     * example path: /tmp/mydir
     *
     * @param path Path String denoting directory
     * @throws Exception
     */
    public static void verifyOutputDirectory (String path) throws Exception {
        verifyOutputDirectory(new File(path));
    }
    
    /**
     * This method will create the directory denoted by the parameter if it doesn't
     * exist
     *
     * @param outDirectory File denoting directory
     * @throws Exception
     */
    public static void verifyOutputDirectory (File outDirectory) throws Exception {
        final String eLabel = "FileUtils.verifyOutputDirectory: ";
        try {
            if (!outDirectory.exists()) {
                if (!outDirectory.mkdirs()) throw new Exception("Failed to create directory: " + outDirectory.getPath());
            } else if (!outDirectory.isDirectory()) {
                throw new Exception("Path is to file, not directory: " + outDirectory.getPath());
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    /**
     * This method will create the directory denoted by the parameter if it doesn't exist.
     * The path parameter is expected to resolve to a file
     * example path: /tmp/mydir/file.txt
     *
     * @param path Path denoting resource/file
     * @throws Exception
     */
    public static void verifyFileOutputDirectory (String path) throws Exception {
        verifyFileOutputDirectory(new File(path));
    }
    
    /**
     * This method will create the directory denoted by the parameter if it doesn't exist.
     * The outputFile parameter is expected to resolve to a file
     * example path: /tmp/mydir/file.txt
     *
     * @param outputFile File denoting resource/file
     * @throws Exception
     */
    public static void verifyFileOutputDirectory (File outputFile) throws Exception {
        final String eLabel = "FileUtils.verifyFileOutputDirectory: ";
        try {
            File parentDirectory = outputFile.getParentFile();
            if (parentDirectory != null) verifyOutputDirectory(parentDirectory);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    /**
     * Delete a directories contents. If deleteChildDirectories is true, then recursively
     * delete all child directories also, otherwise just delete the files.
     *
     * @param directoryPath     Path of directory whose contents to delete
     * @param deleteChildDirectories    True to recursively delete all directories
     * @throws Exception
     */
    public static boolean deleteDirectoryContents (String directoryPath,
                                                   boolean deleteChildDirectories) throws Exception {
        final String eLabel = "FileUtils.deleteDirectoryContents: ";
        try {
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                return false;
            }
            if (!directory.isDirectory()) {
                throw new Exception("Not a directory!!!");
            }
            File[] children = directory.listFiles();
            for (int i = 0; i < children.length; i++) {
                if (children[i].isFile()) {
                    if (!children[i].delete()) {
                        throw new Exception ("Could not delete file: " + children[i].getName());
                    }
                } else {
                    if (deleteChildDirectories) {
                        deleteDirectoryContents(children[i].getPath(), true);
                    }
                    if (!children[i].delete()) {
                        throw new Exception ("Could not delete directory: " + children[i].getName());
                    }
                }
            }
            return true;
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    /**
     * Copy the contents of one directory to another. If moveChildDirectories is true, all
     * child directories will recursively be copied over.
     *
     * @param sourceDirectory   Source directory to copy
     * @param destinationDirectory      Destination directory to copy
     * @param moveChildDirectories      Recursively copy sub directories?
     *
     * @throws Exception
     */
    public static void copyDirectoryContents (String sourceDirectory,
                                              String destinationDirectory, boolean moveChildDirectories) throws Exception {
        final String eLabel = "FileUtils.copyDirectoryContents: ";
        try {
            File directory = new File(sourceDirectory);
            if (!directory.exists() || !directory.isDirectory()) {
                throw new Exception("Not a valid directory: " + sourceDirectory);
            }
            // Create destination directory if missing
            verifyOutputDirectory(destinationDirectory);
            File targetDirectory = new File (destinationDirectory);
            File[] sourceChildren = directory.listFiles();
            for (int i = 0; i < sourceChildren.length; i++) {
                if (sourceChildren[i].isFile()) {
                    File newFile = new File(targetDirectory, sourceChildren[i].getName());
                    copyFile(sourceChildren[i].getPath(), newFile.getPath());
                } else {
                    if (moveChildDirectories) {
                        File newDirectory = new File(targetDirectory, sourceChildren[i].getName());
                        deleteDirectoryContents(newDirectory.getPath(), true);
                        verifyOutputDirectory(newDirectory.getPath());
                        copyDirectoryContents(sourceChildren[i].getPath(), newDirectory.getPath(), true);
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

    /**
     * Copy a source URI to a destination File
     *
     * @param sourceURI A URI, can be another file or a URL
     * @param destinationFileURI A file URI
     * @throws Exception
     */
    public static void copyFile (String sourceURI, String destinationFileURI) throws Exception {
        final String eLabel = "FileUtils.copyFile: ";
        try {
            writeToFile(sourceURI, destinationFileURI);
        } catch (Exception e) {
            throw new Exception(eLabel + e);
        }
    }

}
