/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Utility functions for converting files contents to a byte array,
 * and vis versa.
 *
 * @author The ProActive Team
 *
 */
@PublicAPI
public class FileToBytesConverter {

    /** Read contents of a file and return it as a byte array
     * @param file the file to read
     * @return an array of bytes containing file's data.
     * @throws IOException
     */
    public static byte[] convertFileToByteArray(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        try {
            long length = file.length();
            if (length > 0) {
                return fastConversion(fis, length);
            } else {
                return slowConversion(fis);
            }
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                // We want to throw the exception thrown by the outer try block
                // not by the close()
            }
        }
    }

    /**
     * Read content of a file of known length and return it as a byte array
     *  
     * @param fis The stream to read
     * @param length The number of byte to read
     * @return an array of bytes containing file's data
     * @throws IOException If an I/O error occurs, the file is too large or less bytes than length can be read
     */
    private static byte[] fastConversion(final FileInputStream fis, long length) throws IOException {
        if (length > Integer.MAX_VALUE) {
            throw new IOException("File too large to fit in a byte array");
        }

        final byte[] buf = new byte[(int) length];
        int offset = 0;
        while (offset < buf.length) {
            int r = fis.read(buf, offset, buf.length - offset);
            if (r >= 0) {
                offset += r;
            } else {
                throw new IOException("EOF encountered but fewer bits than expected has been read");
            }
        }

        return buf;
    }

    /**
     * Read content of a file of unknown length (or empty file) and return it as a byte array
     *  
     * @param fis the stream to read
     * @return an array of bytes containing file's data
     * @throws IOException If an I/O error occurs
     */
    private static byte[] slowConversion(final FileInputStream fis) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        long count = 0;
        int n = 0;
        while (-1 != (n = fis.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return output.toByteArray();
    }

    /** write contents of a byte array in a file
     * @param array Array of bytes to write
     * @param file object in which bytes will be written
     * @throws IOException
     */
    public static void convertByteArrayToFile(byte[] array, File file) throws IOException {
        FileOutputStream outStream = new FileOutputStream(file);
        outStream.write(array);
        outStream.close();
    }
}
