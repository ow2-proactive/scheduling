/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Utils for creating and reading zip files.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.0
 */
@PublicAPI
public class ZipUtils {

    /**
     * Compression level of the file.
     */
    protected static final int COMP_LEVEL = 9;

    /**
     * Add the given directory into the zipStream.
     * 
     * @param directoryName the directory to be added in the zip.
     * @param iBaseFolderLength the index in the directoryName from which starts the actual zip entry name.
     * @param zos the stream to write into.
     * @throws IOException if the zip file cannot be written.
     */
    protected static void zipDirectory(String directoryName, int iBaseFolderLength, ZipOutputStream zos)
            throws IOException {
        File dirobject = new File(directoryName);
        if (dirobject.exists()) {
            if (dirobject.isDirectory()) {
                File[] fileList = dirobject.listFiles();
                // Loop through the files
                for (int i = 0; i < fileList.length; i++) {
                    if (fileList[i].isDirectory()) {
                        zipDirectory(fileList[i].getPath(), iBaseFolderLength, zos);
                    } else if (fileList[i].isFile()) {
                        zipFile(fileList[i].getPath(), iBaseFolderLength, zos);
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
     * Add a file into a zip.
     * @param filePath the file to be added in the zip.
     * @param iBaseFolderLength the index in the directoryName from which starts the actual zip entry name.
     * @param jos the stream to write into.
     * @throws IOException if the zip file cannot be written.
     */
    protected static void zipFile(String filePath, int iBaseFolderLength, ZipOutputStream jos)
            throws IOException {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            BufferedInputStream bis = new BufferedInputStream(fis);
            String fileNameEntry = filePath.substring(iBaseFolderLength).replace(File.separatorChar, '/');
            ZipEntry fileEntry = new ZipEntry(fileNameEntry);
            jos.putNextEntry(fileEntry);
            byte[] data = new byte[1024];
            int byteCount;
            while ((byteCount = bis.read(data, 0, 1024)) > -1) {
                jos.write(data, 0, byteCount);
            }
            jos.closeEntry();
            fis.close();
        } catch (ZipException e) {
            // TODO Other exceptions ?
            // Duplicate entry : ignore it.
        }
    }

    /**
     * Create a zip file on the given directoriesAndFiles argument and
     * store the create content in a file denoted by the path in the given dest argument
     * 
     * @param directoriesAndFiles the directories and files to zip (recursively)
     * @param dest the zip destination file that will contains the zipped version of the first argument.
     * @throws IOException if the zip file cannot be created.
     */
    public static void zip(String[] directoriesAndFiles, File dest) throws IOException {
        byte[] zipped = ZipUtils.zipDirectoriesAndFiles(directoriesAndFiles);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest));
        bos.write(zipped);
        bos.close();
    }

    /**
     * Create a zip file that contains all the directory listed in directories parameter.
     * @param directoriesAndFiles the list of directories and files to be zipped.
     * @throws IOException if the zip file cannot be created.
     * @return the zip file as a byte[].
     */
    public static byte[] zipDirectoriesAndFiles(String[] directoriesAndFiles) throws IOException {

        // Fill in a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Create zip stream
        ZipOutputStream zos = new ZipOutputStream(baos);
        int iBaseFolderLength = 0;
        zos.setLevel(COMP_LEVEL);

        // zip file is ready
        zipIt(zos, directoriesAndFiles, iBaseFolderLength);

        // Close the file output streams
        zos.flush();
        zos.close();
        baos.flush();
        baos.close();
        return baos.toByteArray();
    }

    /**
     * Zip directory content in the given output stream
     * 
     * @param zos the output stream in which to store the zipped directory
     * @param directoriesAndFiles a list of directories and files to be zipped
     * @param iBaseFolderLength the depth inside directory hierarchy
     * @throws IOException if a zip entry cannot be created.
     */
    protected static void zipIt(ZipOutputStream zos, String[] directoriesAndFiles, int iBaseFolderLength)
            throws IOException {
        for (String pathElement : directoriesAndFiles) {
            pathElement = removeConsecutiveFileSeparator(pathElement);
            File fileElement = new File(pathElement);
            if (fileElement.isFile()) {
                // add zip files at the root of the global jar file !
                zipFile(pathElement, pathElement.lastIndexOf(File.separator), zos);
            } else if (fileElement.isDirectory()) {
                String strBaseFolder = pathElement.endsWith(File.separator) ? pathElement : pathElement +
                    File.separator;
                iBaseFolderLength = strBaseFolder.length();
                zipDirectory(pathElement, iBaseFolderLength, zos);
            }
        }
    }

    /**
     * Remove consecutive occurrences of file separator character in s
     * 
     * @param s the string to parse.
     * @return s without consecutive occurrences of file separator character
     */
    protected static String removeConsecutiveFileSeparator(String s) {
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
     * Unzip a zip file into a directory.
     * @param zipFile The zip file to be unzipped.
     * @param dest the destination directory.
     * @throws IOException if the destination does not exist or is not a directory, or if the zip file cannot be extracted.
     */
    public static void unzip(ZipFile zipFile, File dest) throws IOException {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        if (dest.exists() && dest.isDirectory()) {
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    File destFile = new File(dest, entry.getName());
                    createFileWithPath(destFile);
                    InputStream in = new BufferedInputStream(zipFile.getInputStream(entry));
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
            }
        } else {
            throw new IOException("Destination " + dest.getAbsolutePath() +
                " is not a directory or does not exist");
        }
    }

    protected static void createFileWithPath(File f) throws IOException {
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
