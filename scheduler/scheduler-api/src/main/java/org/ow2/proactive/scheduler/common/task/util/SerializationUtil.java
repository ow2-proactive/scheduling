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
package org.ow2.proactive.scheduler.common.task.util;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.ow2.proactive.scheduler.common.util.Object2ByteConverter;


/**
 * Utility class which facilitates the serialization/de-serialization of Java
 * objects in variable maps.
 */
public class SerializationUtil {

    private SerializationUtil() {
    }

    public static Map<String, Serializable> deserializeVariableMap(Map<String, byte[]> target)
            throws IOException, ClassNotFoundException {
        return deserializeVariableMap(target, Thread.currentThread().getContextClassLoader());
    }

    public static Map<String, Serializable> deserializeVariableMap(Map<String, byte[]> target, ClassLoader cl)
            throws IOException, ClassNotFoundException {

        if (target == null) {
            return new HashMap<>();
        }

        final Map<String, Serializable> deserialized = new HashMap<String, Serializable>(target.size());
        for (Entry<String, byte[]> e : target.entrySet()) {
            deserialized.put(e.getKey(), (Serializable) Object2ByteConverter.convertByte2Object(e.getValue(), cl));
        }
        return deserialized;
    }

    public static Map<String, byte[]> serializeVariableMap(Map<String, Serializable> variableMap) {
        Map<String, byte[]> serializedMap = new HashMap<String, byte[]>();
        for (String key : variableMap.keySet()) {
            // TODO: should do the check at the time of setting the variable
            if (key != null && key.length() > 255) {
                throw new IllegalArgumentException("Key is too long, it must have 255 chars length max : " + key);
            } else {
                try {
                    Serializable value = variableMap.get(key);
                    byte[] serialized = Object2ByteConverter.convertObject2Byte(value);
                    serializedMap.put(key, serialized);
                } catch (IOException e) {
                    throw new IllegalArgumentException("Cannot add argument " + key, e);
                }
            }
        }
        return serializedMap;
    }

    public static Serializable deserializeAndGetVariable(String key, Map<String, byte[]> variables, ClassLoader cl)
            throws IOException, ClassNotFoundException {
        Serializable serialized = null;
        if (variables.containsKey(key)) {
            serialized = (Serializable) Object2ByteConverter.convertByte2Object(variables.get(key), cl);
        }
        return serialized;
    }

    public static void serializeAndSetVariable(String key, Serializable value, Map<String, byte[]> map) {
        if (key != null && key.length() > 255) {
            throw new IllegalArgumentException("Key is too long, it must have 255 chars length max : " + key);
        } else {
            try {
                byte[] serialized = Object2ByteConverter.convertObject2Byte(value);
                map.put(key, serialized);
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot add argument " + key, e);
            }
        }
    }
}
