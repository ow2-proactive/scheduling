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
package org.ow2.proactive.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.codec.binary.Base64;


/**
 * Utility functions for converting object to a byte array,
 * and vice versa.
 * <p>
 * This class can also compress stream
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.0
 */
public final class ObjectByteConverter {

    /**
     * Convert the given Serializable Object into a byte array.
     * 
     * @param obj the Serializable object to be compressed
     * @return a byteArray representing the Serialization of the given object.
     * @throws IOException if an I/O exception occurs when writing the output byte array
     */
    public static final byte[] objectToByteArray(Object obj) throws IOException {
        return objectToByteArray(obj, false);
    }

    /**
     * Convert the given Serializable Object into a byte array.
     * <p>
     * The returned byteArray can be compressed by setting compress boolean argument value to <code>true</code>.
     * 
     * @param obj the Serializable object to be compressed
     * @param compress true if the returned byteArray must be also compressed, false if no compression is required.
     * @return a compressed (or not) byteArray representing the Serialization of the given object.
     * @throws IOException if an I/O exception occurs when writing the output byte array
     */
    public static final byte[] objectToByteArray(Object obj, boolean compress) throws IOException {
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        if (obj == null) {
            return null;
        }
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.flush();
            if (!compress) {
                // Return the UNCOMPRESSED data
                return baos.toByteArray();
            } else {
                // Compressor with highest level of compression
                Deflater compressor = new Deflater();
                compressor.setLevel(Deflater.BEST_COMPRESSION);
                // Give the compressor the data to compress
                compressor.setInput(baos.toByteArray());
                compressor.finish();

                ByteArrayOutputStream bos = null;
                try {
                    // Create an expandable byte array to hold the compressed data.
                    bos = new ByteArrayOutputStream();
                    // Compress the data
                    byte[] buf = new byte[512];
                    while (!compressor.finished()) {
                        int count = compressor.deflate(buf);
                        bos.write(buf, 0, count);
                    }
                    // Return the COMPRESSED data
                    return bos.toByteArray();
                } finally {
                    if (bos != null) {
                        bos.close();
                    }
                }
            }
        } finally {
            if (oos != null) {
                oos.close();
            }
            if (baos != null) {
                baos.close();
            }
        }
    }

    /**
     * Convert the given byte array into the corresponding object.
     * 
     * @param input the byteArray to be convert as an object.
     * @return the object corresponding to the given byteArray.
     * @throws IOException if an I/O exception occurs when writing the returned object
     * @throws ClassNotFoundException if class represented by given byteArray is not found.
     */
    public static Object byteArrayToObject(byte[] input) throws IOException, ClassNotFoundException {
        return byteArrayToObject(input, false);
    }

    /**
     * Convert the given byte array into the corresponding object.
     * <p>
     * The given byteArray can be uncompressed if it has been compressed before.
     * 
     * @param input the byteArray to be convert as an object.
     * @param uncompress true if the given byteArray must be also uncompressed, false if no compression was made on it.
     * @return the object corresponding to the given byteArray.
     * @throws IOException if an I/O exception occurs when writing the returned object
     * @throws ClassNotFoundException if class represented by given byteArray is not found.
     */
    public static Object byteArrayToObject(byte[] input, boolean uncompress)
            throws IOException, ClassNotFoundException {
        if (input == null) {
            return null;
        }
        if (uncompress) {
            // Uncompress the bytes
            Inflater decompressor = new Inflater();
            decompressor.setInput(input);

            ByteArrayOutputStream bos = null;
            try {
                // Create an expandable byte array to hold the compressed data.
                bos = new ByteArrayOutputStream();
                // Compress the data
                byte[] buf = new byte[512];
                while (!decompressor.finished()) {
                    int count = decompressor.inflate(buf);
                    bos.write(buf, 0, count);
                }
                decompressor.end();
                // set the UNCOMPRESSED data
                input = bos.toByteArray();
            } catch (DataFormatException dfe) {
                //convert into io exception to fit previous behavior
                throw new IOException("Compressed data format is invalid : " + dfe.getMessage(), dfe);
            } finally {
                if (bos != null) {
                    bos.close();
                }
            }
        }
        //here, input byteArray is uncompressed if needed
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            bais = new ByteArrayInputStream(input);
            ois = new ObjectInputStream(bais);
            return ois.readObject();
        } finally {
            if (ois != null) {
                ois.close();
            }
            if (bais != null) {
                bais.close();
            }
        }
    }

    public static String serializableToBase64String(Serializable input) throws IOException {
        return byteArrayToBase64String(objectToByteArray(input));
    }

    public static String byteArrayToBase64String(byte[] input) {
        if (input == null) {
            return null;
        }
        return new String(Base64.encodeBase64(input));
    }

    public static byte[] base64StringToByteArray(String input) {
        if (input == null) {
            return null;
        }
        return Base64.decodeBase64(input);
    }

    public static Serializable base64StringToSerializable(String input) throws IOException, ClassNotFoundException {
        if (input == null) {
            return null;
        }
        return (Serializable) byteArrayToObject(base64StringToByteArray(input));
    }

    public static Map<String, byte[]> mapOfBase64StringToByteArray(Map<String, String> input) {
        if (input == null) {
            return new HashMap<>();
        }
        HashMap<String, byte[]> answer = new HashMap<>(input.size());
        for (Map.Entry<String, String> entry : input.entrySet()) {
            answer.put(entry.getKey(), base64StringToByteArray(entry.getValue()));
        }
        return answer;
    }

    public static Map<String, Serializable> mapOfByteArrayToSerializable(Map<String, byte[]> input)
            throws IOException, ClassNotFoundException {
        if (input == null) {
            return new HashMap<>();
        }
        HashMap<String, Serializable> answer = new HashMap<>(input.size());
        for (Map.Entry<String, byte[]> entry : input.entrySet()) {
            answer.put(entry.getKey(), (Serializable) byteArrayToObject(entry.getValue()));
        }
        return answer;
    }

}
