/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
 * %$ACTIVEEON_INITIAL_DEV$
 */

package org.ow2.proactive_grid_cloud_portal.cli.utils;

import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_IO_ERROR;
import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_OTHER;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;


public class FileUtility {

    public static String md5Checksum(File file) {
        try {
            return DigestUtils.md5Hex(new FileInputStream(file));
        } catch (IOException ioe) {
            throw new CLIException(REASON_IO_ERROR, ioe);
        }
    }

    public static void writeStringToFile(File file, String content) {
        try {
            FileUtils.writeStringToFile(file, content);
        } catch (IOException ioe) {
            throw new CLIException(REASON_OTHER, ioe);
        }
        file.setReadable(true, true);
        file.setWritable(true, true);
    }

    public static String readFileToString(File file) {
        try {
            return FileUtils.readFileToString(file);
        } catch (IOException ioe) {
            throw new CLIException(REASON_IO_ERROR, ioe);
        }
    }

    public static byte[] byteArray(File file) {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException ioe) {
            throw new CLIException(REASON_IO_ERROR, ioe);
        }
    }

    public static void writeByteArrayToFile(byte[] data, File file) {
        try {
            FileUtils.writeByteArrayToFile(file, data);
        } catch (IOException ioe) {
            throw new CLIException(REASON_IO_ERROR, ioe);
        }
    }

    public static void writeObjectToFile(Object object, File file) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            ObjectOutputStream decorated = new ObjectOutputStream(outputStream);
            decorated.writeObject(object);
            decorated.flush();
        } catch (IOException ioe) {
            throw new CLIException(REASON_IO_ERROR, ioe);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public static OutputStream buildOutputStream(File file) throws IllegalArgumentException {
        if (file.exists() && !file.delete()) {
            throw new RuntimeException("Cannot delete the existing output file. " + file.getAbsolutePath());
        } else {
            File parentFile = file.getParentFile();
            if (parentFile == null) {
                throw new IllegalArgumentException(
                    "Invalid pathname. Cannot determine the parent directory. " + file.getAbsolutePath());
            }
            if (!(parentFile.exists() || parentFile.mkdirs())) {
                throw new RuntimeException("Cannot create the non-existing parent directory of the file. " +
                    file.getAbsolutePath());
            }
        }
        try {
            return new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private FileUtility() {
    }

}
