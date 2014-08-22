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
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.perftests.rest.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class ZipUtilityHelper {

    private static final int default_buffer_size = 1024;

    public static void main(String... args) {
        if (args == null || args.length != 2) {
            throw new IllegalArgumentException(
                "Invalid parameters, expected parameters: targetZipFile outputDirectory.");
        }
        try {
            unzip(args[0], args[1]);
        } catch (IOException error) {
            error.printStackTrace(System.out);
            System.out.flush();
        }
    }

    private static void unzip(String zipFilePath, String outputDirPath) throws IOException {
        File outputDir = new File(outputDirPath);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        ZipFile zipFile = new ZipFile(zipFilePath);
        Enumeration enu = zipFile.entries();
        while (enu.hasMoreElements()) {
            ZipEntry file = (ZipEntry) enu.nextElement();
            File f = new File(outputDir, file.getName());
            if (file.isDirectory()) {
                f.mkdirs();
                continue;
            }
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            InputStream is = null;
            OutputStream os = null;

            try {
                is = zipFile.getInputStream(file);
                os = new FileOutputStream(f);
                copy(is, os);
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ioe) {
                    }
                }
            }
        }
    }

    private static void copy(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[default_buffer_size];
        int len = 0;
        while (-1 != (len = is.read(buffer))) {
            os.write(buffer, 0, len);
        }
    }
}
