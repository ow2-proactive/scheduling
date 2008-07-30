/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Utility functions for converting files contents to a byte array, 
 * and vis versa.
 * 
 * @author The ProActive Team
 *
 */
public class FileToBytesConverter {

    /** Read contents of a file and return it as a byte array
     * @param file the file to read
     * @return an array of bytes containing file's data.
     * @throws IOException 
     */
    public static byte[] convertFileToByteArray(File file) throws IOException {
        InputStream in = null;
        in = new FileInputStream(file);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        long count = 0;
        int n = 0;
        while (-1 != (n = in.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        in.close();
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
