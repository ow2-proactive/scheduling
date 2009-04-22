/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipException;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Utils for creating and reading jar files.
 * @author The ProActive Team
 */
@PublicAPI
public class JarUtils {

    /**
     * Compression level of the jar file.
     */
    public static final int COMP_LEVEL = 9;

    /**
     * Add the given directory into the jarStream.
     * @param directoryName the directory to be added in the jar.
     * @param iBaseFolderLength the index in the directoryName from which starts the actual jar entry name.
     * @param jos the stream to write into.
     * @throws IOException if the jar file cannot be written.
     */
    public static void jarDirectory(String directoryName, int iBaseFolderLength, JarOutputStream jos)
            throws IOException {
        File dirobject = new File(directoryName);
        if (dirobject.exists()) {
            if (dirobject.isDirectory()) {
                File[] fileList = dirobject.listFiles();
                // Loop through the files
                for (int i = 0; i < fileList.length; i++) {
                    if (fileList[i].isDirectory()) {
                        jarDirectory(fileList[i].getPath(), iBaseFolderLength, jos);
                    } else if (fileList[i].isFile()) {
                        jarFile(fileList[i].getPath(), iBaseFolderLength, jos);
                    }
                }
            } else {
                throw new IOException(directoryName + " is not a directory.");
            }
        } else {
            throw new IOException("Directory " + directoryName + " does not exist.");
        }
    }

    /**
     * Add a file into a jar.
     * @param filePath the file to be added in the jar.
     * @param iBaseFolderLength the index in the directoryName from which starts the actual jar entry name.
     * @param jos the stream to write into.
     * @throws IOException if the jar file cannot be written.
     */
    private static void jarFile(String filePath, int iBaseFolderLength, JarOutputStream jos)
            throws IOException {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            BufferedInputStream bis = new BufferedInputStream(fis);
            String fileNameEntry = filePath.substring(iBaseFolderLength).replace(File.separatorChar, '/');
            JarEntry fileEntry = new JarEntry(fileNameEntry);
            jos.putNextEntry(fileEntry);
            byte[] data = new byte[1024];
            int byteCount;
            while ((byteCount = bis.read(data, 0, 1024)) > -1) {
                jos.write(data, 0, byteCount);
            }
            jos.closeEntry();
        } catch (ZipException e) {
            // TODO Other exceptions ?
            // Duplicate entry : ignore it.
        }
    }

    /**
     * Create a jar file that contains all the directory listed in directories parameter.
     * @param directoriesAndFiles the list of directories and files to be jarred.
     * @param manifestVerion the version of the jar manifest (can be null).
     * @param mainClass the main class of the jar (can be null).
     * @param jarInternalClasspath the class-path of the jar (can be null).
     * @throws IOException if the jar file cannot be created.
     * @return the jar file as a byte[].
     */
    public static byte[] jarDirectoriesAndFiles(String[] directoriesAndFiles, String manifestVerion,
            String mainClass, String jarInternalClasspath) throws IOException {

        // Fill in a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Create jar stream
        JarOutputStream jos = new JarOutputStream(baos, JarUtils.createManifest(manifestVerion, mainClass,
                jarInternalClasspath));
        int iBaseFolderLength = 0;
        jos.setLevel(COMP_LEVEL);

        // Jar file is ready
        for (String pathElement : directoriesAndFiles) {
            pathElement = removeConsecutiveFileSeparator(pathElement);
            File fileElement = new File(pathElement);
            if (fileElement.isFile()) {
                // add jar files at the root of the global jar file !
                jarFile(pathElement, pathElement.lastIndexOf(File.separator), jos);
            } else if (fileElement.isDirectory()) {
                String strBaseFolder = pathElement.endsWith(File.separator) ? pathElement : pathElement +
                    File.separator;
                iBaseFolderLength = strBaseFolder.length();
                jarDirectory(pathElement, iBaseFolderLength, jos);
            }
        }
        // Close the file output streams
        jos.flush();
        jos.close();
        baos.flush();
        baos.close();
        return baos.toByteArray();

    }

    private static Manifest createManifest(String manifestVerion, String mainClass,
            String jarInternalClasspath) {
        // Create manifest
        Manifest manifest = new Manifest();
        Attributes manifestAttr = manifest.getMainAttributes();
        //note:Must set Manifest-Version,or the manifest file will be empty!
        if (manifestVerion != null) {
            manifestAttr.putValue("Manifest-Version", manifestVerion);
            if (mainClass != null) {
                manifestAttr.putValue("Main-Class", mainClass);
            }
            if (jarInternalClasspath != null) {
                manifestAttr.putValue("Class-Path", jarInternalClasspath);
            }
        }
        return manifest;
    }

    /**
     * Remove consecutive occurrences of file separator character in s
     * @param s the string to parse.
     * @return s without consecutive occurrences of file separator character
     */
    private static String removeConsecutiveFileSeparator(String s) {
        StringBuffer res = new StringBuffer();
        boolean previousWasFileSep = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == File.separatorChar) {
                if (!previousWasFileSep) {
                    res.append(c);
                    previousWasFileSep = true;
                }
            } else {
                previousWasFileSep = false;
                res.append(c);
            }
        }
        return res.toString();
    }

    /**
     * Unjar a jar file into a directory.
     * @param jarFile The jar file to be unjared.
     * @param dest the destination directory.
     * @throws IOException if the destination does not exist or is not a directory, or if the jar file cannot be extracted.
     */
    public static void unjar(JarFile jarFile, File dest) throws IOException {
        Enumeration<JarEntry> entries = jarFile.entries();
        if (dest.exists() && dest.isDirectory()) {
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                File destFile = new File(dest, entry.getName());
                JarUtils.createFileWithPath(destFile);
                InputStream in = new BufferedInputStream(jarFile.getInputStream(entry));
                OutputStream out = new BufferedOutputStream(new FileOutputStream(destFile));
                byte[] buffer = new byte[2048];
                for (;;) {
                    int nBytes = in.read(buffer);
                    if (nBytes <= 0)
                        break;
                    out.write(buffer, 0, nBytes);
                }
                out.flush();
                out.close();
                in.close();
            }
        } else {
            throw new IOException("Destination " + dest.getAbsolutePath() +
                " is not a directory or does not exist");
        }
    }

    private static void createFileWithPath(File f) throws IOException {
        String absPath = f.getAbsolutePath();
        StringTokenizer parser = new StringTokenizer(absPath, File.separator);
        StringBuffer globalPath = new StringBuffer(File.separator);
        while (parser.countTokens() > 1) {
            globalPath = globalPath.append(parser.nextToken() + File.separator);
            File currentDir = new File(globalPath.toString());
            if (!currentDir.exists()) {
                if (!currentDir.mkdir()) {
                    throw new IOException("Cannot create directory " + currentDir.getAbsolutePath());
                }
            }
        }
        if (!f.createNewFile()) {
            throw new IOException("Cannot create file " + f.getAbsolutePath());
        }
    }

}
