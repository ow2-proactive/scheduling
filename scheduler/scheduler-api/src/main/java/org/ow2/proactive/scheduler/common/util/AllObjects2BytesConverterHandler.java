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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;


public class AllObjects2BytesConverterHandler {

    private static final Logger logger = Logger.getLogger(AllObjects2BytesConverterHandler.class);

    private static Long secondsToWait = Long.getLong("pa.max.deserialization.seconds", 30L);

    private AllObjects2BytesConverterHandler() {
    }

    public static Map<String, Serializable> convertAllBytes2Objects(final Map<String, byte[]> target,
            final ClassLoader cl) {
        return convert("Deserialization of variables", createDeserializeCollable(target, cl));
    }

    public static Map<String, byte[]> convertAllObjects2Bytes(final Map<String, Serializable> variableMap) {
        return convert("Serialization of variables", createSerializableCallable(variableMap));
    }

    public static byte[] convertObject2Byte(final String key, final Serializable value) {
        return convertSingle("Serialization of single value", createSerializeSingleValueCollable(key, value));
    }

    public static Serializable convertByte2Object(final byte[] value) {
        return convertSingle("Deserialization of single value", createDeserializeSingleValueCollable(value));
    }

    private static <K, V> Map<K, V> convert(String action, Callable<Map<K, V>> callable) {

        Map<K, V> resultMap = new HashMap<>();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Map<K, V>> future = executor.submit(callable);

        try {
            resultMap = future.get(secondsToWait, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            logger.fatal(action + " was stuck for more than " + secondsToWait + " seconds. Killing the Java process.");
            System.exit(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdownNow();
        }

        return resultMap;

    }

    private static <V> V convertSingle(final String action, final Callable<V> callable) {

        V result = null;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<V> future = executor.submit(callable);

        try {
            result = future.get(secondsToWait, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            logger.fatal(action + " of variable was stuck for more than " + secondsToWait +
                         " seconds. Killing the Java process.");
            System.exit(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdownNow();
        }

        return result;

    }

    private static Callable<Map<String, byte[]>>
            createSerializableCallable(final Map<String, Serializable> variableMap) {
        return new Callable<Map<String, byte[]>>() {

            public Map<String, byte[]> call() throws ClassNotFoundException, IOException {
                Map<String, byte[]> serializedMap = new HashMap<String, byte[]>();
                for (String key : variableMap.keySet()) {
                    Serializable value = variableMap.get(key);
                    byte[] serialized = serialiseValue(key, value);
                    serializedMap.put(key, serialized);
                }
                return serializedMap;
            }

        };
    }

    private static Callable<Map<String, Serializable>> createDeserializeCollable(final Map<String, byte[]> target,
            final ClassLoader cl) {
        return new Callable<Map<String, Serializable>>() {

            public Map<String, Serializable> call() throws ClassNotFoundException, IOException {
                final Map<String, Serializable> deserializedMap = new HashMap<String, Serializable>(target.size());
                for (Entry<String, byte[]> e : target.entrySet()) {
                    deserializedMap.put(e.getKey(),
                                        (Serializable) Object2ByteConverter.convertByte2Object(e.getValue(), cl));
                }
                return deserializedMap;
            }
        };
    }

    private static Callable<byte[]> createSerializeSingleValueCollable(final String key, final Serializable value) {
        return new Callable<byte[]>() {
            public byte[] call() throws ClassNotFoundException, IOException {
                return serialiseValue(key, value);
            }
        };
    }

    private static Callable<Serializable> createDeserializeSingleValueCollable(final byte[] value) {
        return new Callable<Serializable>() {
            public Serializable call() throws ClassNotFoundException, IOException {
                return (Serializable) Object2ByteConverter.convertByte2Object(value);
            }
        };
    }

    private static byte[] serialiseValue(String key, Serializable value) {
        if (key != null && key.length() > 255) {
            throw new IllegalArgumentException("Key is too long, it must have 255 chars length max : " + key);
        } else {
            try {
                return Object2ByteConverter.convertObject2Byte(value);

            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot add argument " + key, e);
            }
        }
    }

}
