/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.utils.FileUtils;


/**
 * Utils for creating and reading zip files.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.0
 */
@PublicAPI
public class ZipUtils extends FileUtils {

    private static final Logger logger = Logger.getLogger(ZipUtils.class);

    /**
     * Compression level of the file.
     */
    protected static final int COMP_LEVEL = 9;

    /**
     * Create a zip file on the given directoriesAndFiles argument and
     * store the create content in a file denoted by the path in the given dest argument
     * 
     * @param directoriesAndFiles the directories and files to zip (recursively)
     * @param dest the zip destination file that will contains the zipped version of the first argument.
     * @param crc the CRC32 of all zipentries. Can be null if no crc is needed.
     * @throws IOException if the zip file cannot be created.
     */
    public static void zip(String[] directoriesAndFiles, File dest, CRC32 crc) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(dest)))) {
            ZipUtils.zipDirectoriesAndFiles(zos, directoriesAndFiles, crc);
        }
    }

    /**
     * Create a zip file that contains all the directory listed in directories parameter.
     * @param zos output stream destination
     * @param directoriesAndFiles the list of directories and files to be zipped.
     * @param crc the CRC32 of all zipentries. Can be null if no crc is needed.
     * @throws IOException if an error occurs during the compression
     */
    public static void zipDirectoriesAndFiles(ZipOutputStream zos, String[] directoriesAndFiles, CRC32 crc)
            throws IOException {

        // Create zip stream
        zos.setLevel(COMP_LEVEL);

        // zip file is ready
        zipIt(zos, directoriesAndFiles, crc);

        // Close the file output streams
        zos.flush();
    }

    /**
     * Zip directory content in the given output stream
     * 
     * @param zos the output stream in which to store the zipped directory
     * @param directoriesAndFiles a list of directories and files to be zipped
     * @throws IOException if a zip entry cannot be created.
     */
    protected static void zipIt(ZipOutputStream zos, String[] directoriesAndFiles, CRC32 crc) throws IOException {
        for (String pathElement : directoriesAndFiles) {
            File fileElement = new File(pathElement);
            //normalize path (also remove consecutive file separator)
            pathElement = fileElement.getPath();
            int length = pathElement.lastIndexOf(File.separator) + 1;
            if (fileElement.isFile()) {
                // add zip files at the root of the global jar file !
                zipFile(pathElement, length, zos, crc);
            } else if (fileElement.isDirectory()) {
                // get only last directory
                zipDirectory(pathElement, length, zos, crc);
            }
        }
    }

    /**
     * Add the given directory into the zipStream.
     * 
     * @param directoryName the directory to be added in the zip.
     * @param iBaseFolderLength the index in the directoryName from which starts the actual zip entry name.
     * @param zos the stream to write into.
     * @param crc the CRC32 of all zipentries. Can be null if no crc is needed.
     * @throws IOException if the zip file cannot be written.
     */
    protected static void zipDirectory(String directoryName, int iBaseFolderLength, ZipOutputStream zos, CRC32 crc)
            throws IOException {
        File dirobject = new File(directoryName);
        if (dirobject.exists()) {
            if (dirobject.isDirectory()) {
                File[] fileList = dirobject.listFiles();
                // Loop through the files
                if (fileList != null) {
                    for (int i = 0; i < fileList.length; i++) {
                        if (fileList[i].isDirectory()) {
                            zipDirectory(fileList[i].getPath(), iBaseFolderLength, zos, crc);
                        } else if (fileList[i].isFile()) {
                            zipFile(fileList[i].getPath(), iBaseFolderLength, zos, crc);
                        }
                    }
                } else {
                    logger.warn("Problem accessing folder: " + dirobject);
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
     * @param crc the CRC32 of all zipentries. Can be null if no crc is needed.
     * @throws IOException if the zip file cannot be written.
     */
    protected static void zipFile(String filePath, int iBaseFolderLength, ZipOutputStream jos, CRC32 crc)
            throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath))) {
            String fileNameEntry = filePath.substring(iBaseFolderLength).replace(File.separatorChar, '/');
            ZipEntry fileEntry = new ZipEntry(fileNameEntry);
            jos.putNextEntry(fileEntry);
            byte[] data = new byte[1024];
            int byteCount;
            while ((byteCount = bis.read(data, 0, 1024)) > -1) {
                if (crc != null) {
                    crc.update(data);
                }
                jos.write(data, 0, byteCount);
            }
            jos.closeEntry();
        } catch (ZipException e) {
            // Duplicate entry : ignore it.
        } catch (IOException e) {
            logger.error("Error occurred while trying to zip file " + filePath, e);
            throw e;
        }
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
                    try (OutputStream out = new BufferedOutputStream(new FileOutputStream(destFile))) {
                        byte[] buffer = new byte[2048];
                        for (;;) {
                            int nBytes = in.read(buffer);
                            if (nBytes <= 0)
                                break;
                            out.write(buffer, 0, nBytes);
                        }
                        out.flush();
                    }
                    in.close();
                }
            }
        } else {
            throw new IOException("Destination " + dest.getAbsolutePath() + " is not a directory or does not exist");
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
