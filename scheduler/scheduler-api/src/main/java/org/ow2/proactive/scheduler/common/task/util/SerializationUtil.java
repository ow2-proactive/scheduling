/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 * $$PROACTIVE_INITIAL_DEV$$
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
        final Map<String, Serializable> deserialized = new HashMap<String, Serializable>(target.size());
        for (Entry<String, byte[]> e : target.entrySet()) {
            deserialized.put(e.getKey(), (Serializable) Object2ByteConverter.convertByte2Object(e.getValue(),
                    cl));
        }
        return deserialized;
    }

    public static Map<String, byte[]> serializeVariableMap(Map<String, Serializable> variableMap) {
        Map<String, byte[]> serializedMap = new HashMap<String, byte[]>();
        for (String key : variableMap.keySet()) {
            // TODO: should do the check at the time of setting the variable
            if (key != null && key.length() > 255) {
                throw new IllegalArgumentException("Key is too long, it must have 255 chars length max : " +
                    key);
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

    public static Serializable deserializeAndGetVariable(String key, Map<String, byte[]> variables,
            ClassLoader cl) throws IOException, ClassNotFoundException {
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
