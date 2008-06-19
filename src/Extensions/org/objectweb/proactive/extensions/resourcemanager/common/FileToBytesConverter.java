package org.objectweb.proactive.extensions.resourcemanager.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.objectweb.proactive.extensions.resourcemanager.exception.RMException;


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
     * @throws RMException if the reading fails.
     */
    public static byte[] convertFileToByteArray(File file) throws RMException {
        InputStream in = null;
        try {
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
        } catch (Exception e) {
            throw new RMException(e);
        }
    }

    /** write contents of a byte array in a file
     * @param array Array of bytes to write
     * @param file object in which bytes will be written
     * @throws RMException if a problem occurs
     */
    public static void convertByteArrayToFile(byte[] array, File file) throws RMException {
        try {
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(array);
            outStream.close();
        } catch (Exception e) {
            throw new RMException(e);
        }

    }
}
