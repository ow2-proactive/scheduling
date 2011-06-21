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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


/**
 * Contains various utility methods for file manipulations.
 *
 * @author The ProActive Team
 */
public final class FileUtils {

    /**
     * Uncompresses the zip archive into the specified directory.
     *
     * @param zipFile contains the archive to unzip
     * @param destDir the destination directory that will contain unzipped files
     * @return Returns true if the unzip was successful, false otherwise
     */
    public static boolean unzip(final File zipFile, final File destDir) {
        final String destDirAbsPath = destDir.getAbsolutePath() + File.separatorChar;
        final byte[] buf = new byte[2048];

        try {
            final FileInputStream fiStream = new FileInputStream(zipFile);
            final ZipInputStream ziStream = new ZipInputStream(fiStream);

            ZipEntry zipEntry = ziStream.getNextEntry();

            // Cycle through all entries and write them into the dest dir
            while (zipEntry != null) {
                File absPath = new File(destDirAbsPath + zipEntry.getName());
                if (!zipEntry.isDirectory()) {
                    String absPathName = absPath.getAbsolutePath();
                    int endIndex = absPathName.lastIndexOf(File.separatorChar + absPath.getName());
                    String absDirName = absPathName.substring(0, endIndex);
                    File absDir = new File(absDirName);
                    absDir.mkdirs();
                    FileOutputStream fos = new FileOutputStream(absPath);
                    int len;
                    while ((len = ziStream.read(buf)) > 0) {
                        fos.write(buf, 0, len);
                    }
                    fos.close();
                }
                zipEntry = ziStream.getNextEntry();
            }

            // Close the input stream
            ziStream.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Uncompresses the zip archive into the specified directory.
     *
     * @param zipFile contains the archive to unzip
     * @param destDir the destination directory that will contain unzipped files
     * @return Returns true if the unzip was successful, false otherwise
     */
    public static boolean unzip(final String zipFile, final String destDir) {
        return unzip(new File(zipFile), new File(destDir));
    }

    /**
     * compresses the specified files into a zip archive
     *
     * @param zipFile contains the zip archive name
     * @param files files to put in the archive
     * @return Returns true if the zip was successful, false otherwise
     */
    public static boolean zip(final File zipFile, final File[] files) {
        ZipOutputStream out = null;
        try {
            byte[] buffer = new byte[4096]; // Create a buffer for copying
            int bytesRead;

            out = new ZipOutputStream(new FileOutputStream(zipFile));

            for (File f : files) {
                FileInputStream in = null;
                try {
                    if (f.isDirectory())
                        continue;//Ignore directory
                    in = new FileInputStream(f); // Stream to read file
                    ZipEntry entry = new ZipEntry(f.getPath()); // Make a ZipEntry
                    out.putNextEntry(entry); // Store entry
                    while ((bytesRead = in.read(buffer)) != -1)
                        out.write(buffer, 0, bytesRead);
                    in.close();
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {

                        }
                    }
                }
            }
            out.close();
        } catch (Exception e) {
            return false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {

                }
            }
        }
        return true;
    }

    /**
     * compresses the specified files into a zip archive
     *
     * @param zipFile contains the zip archive name
     * @param files files to put in the archive
     * @return Returns true if the zip was successful, false otherwise
     */
    public static boolean zip(final String zipFile, final String[] files) {
        File[] ffiles = new File[files.length];
        for (int i = 0; i < files.length; i++) {
            ffiles[i] = new File(files[i]);
        }
        return zip(new File(zipFile), ffiles);
    }
}
