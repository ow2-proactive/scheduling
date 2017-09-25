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

import org.ow2.proactive.scheduler.common.util.AllObjects2BytesConverterHandler;


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

        final Map<String, Serializable> deserialized = AllObjects2BytesConverterHandler.convertAllBytes2Objects(target,
                                                                                                                cl);

        return deserialized;
    }

    public static Map<String, byte[]> serializeVariableMap(Map<String, Serializable> variableMap) {
        Map<String, byte[]> serializedMap = AllObjects2BytesConverterHandler.convertAllObjects2Bytes(variableMap);
        return serializedMap;
    }

}
