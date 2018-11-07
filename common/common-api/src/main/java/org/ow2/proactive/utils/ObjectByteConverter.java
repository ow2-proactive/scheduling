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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.codec.binary.Base64;
import org.objectweb.proactive.core.util.converter.ObjectToByteConverter;


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
     */
    public static final byte[] objectToByteArray(Object obj) {
        return objectToByteArray(obj, false);
    }

    /**
     * Convert the given Serializable Object into a byte array.
     * <p>
     * The returned byteArray can be compressed by setting compress boolean argument value to <code>true</code>.
     *
     * @param obj      the Serializable object to be compressed
     * @param compress true if the returned byteArray must be also compressed, false if no compression is required.
     * @return a compressed (or not) byteArray representing the Serialization of the given object.
     */
    public static final byte[] objectToByteArray(Object obj, boolean compress) {
        if (obj == null) {
            return null;
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {
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

                // Create an expandable byte array to hold the compressed data.
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

                    // Compress the data
                    byte[] buf = new byte[512];
                    while (!compressor.finished()) {
                        int count = compressor.deflate(buf);
                        bos.write(buf, 0, count);
                    }
                    // Return the COMPRESSED data
                    return bos.toByteArray();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not convert to byte array object ", e);
        }
    }

    /**
     * Convert the given byte array into the corresponding object.
     *
     * @param input the byteArray to be convert as an object.
     * @return the object corresponding to the given byteArray.
     */
    public static Object byteArrayToObject(byte[] input) {
        return byteArrayToObject(input, false);
    }

    /**
     * Convert the given byte array into the corresponding object.
     * <p>
     * The given byteArray can be uncompressed if it has been compressed before.
     *
     * @param input      the byteArray to be convert as an object.
     * @param uncompress true if the given byteArray must be also uncompressed, false if no compression was made on it.
     * @return the object corresponding to the given byteArray.
     */
    public static Object byteArrayToObject(byte[] input, boolean uncompress) {
        if (input == null) {
            return null;
        }
        if (uncompress) {
            // Uncompress the bytes
            Inflater decompressor = new Inflater();
            decompressor.setInput(input);

            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                // Create an expandable byte array to hold the compressed data.
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
                throw new RuntimeException("Compressed data format is invalid : " + dfe.getMessage(), dfe);
            } catch (IOException e) {
                throw new RuntimeException("Could not convert to serialized object: " + e.getMessage(), e);
            }
        }
        //here, input byteArray is uncompressed if needed
        try (ByteArrayInputStream bais = new ByteArrayInputStream(input);
                ObjectInputStream ois = new ObjectInputStream(bais)) {
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Could not uncompress to byte array object: " + e.getMessage(), e);
        }
    }

    public static String serializableToBase64String(Serializable input) {
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

    public static Serializable base64StringToSerializable(String input) {
        if (input == null) {
            return null;
        }
        return (Serializable) byteArrayToObject(base64StringToByteArray(input));
    }

    public static Map<String, byte[]> mapOfBase64StringToByteArray(Map<String, String> input) {
        if (input == null) {
            return null;
        }
        HashMap<String, byte[]> answer = new HashMap<>(input.size());
        input.forEach((key, value) -> {
            answer.put(key, base64StringToByteArray(value));
        });
        return answer;
    }

    public static Map<String, Serializable> mapOfBase64StringToSerializable(Map<String, String> input) {
        if (input == null) {
            return null;
        }
        HashMap<String, Serializable> answer = new HashMap<>(input.size());
        input.forEach((key, value) -> {
            answer.put(key, base64StringToSerializable(value));
        });
        return answer;

    }

    public static Map<String, Serializable> mapOfByteArrayToSerializable(Map<String, byte[]> input) {
        if (input == null) {
            return null;
        }

        HashMap<String, Serializable> answer = new HashMap<>(input.size());
        input.forEach((key, value) -> {
            answer.put(key, (Serializable) byteArrayToObject(value));
        });
        return answer;

    }

    public static Map<String, byte[]> mapOfSerializableToByteArray(Map<String, Serializable> input) {
        if (input == null) {
            return null;
        }
        HashMap<String, byte[]> answer = new HashMap<>(input.size());
        input.forEach((key, value) -> {
            try {
                answer.put(key, ObjectToByteConverter.ObjectStream.convert(value));
            } catch (IOException e) {
                throw new RuntimeException("Error when converting variables to byte array ", e);
            }
        });
        return answer;
    }

}
