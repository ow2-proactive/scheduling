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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


class Object2ByteConverter {

    public static byte[] convertObject2Byte(Object o) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            Object2ByteConverter.writeToStream(objectOutputStream, o);
            return byteArrayOutputStream.toByteArray();
        } finally {
            // close streams
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
            byteArrayOutputStream.close();
        }
    }

    static Object convertByte2Object(byte[] byteArray) throws IOException, ClassNotFoundException {
        return convertByte2Object(byteArray, null);
    }

    static Object convertByte2Object(byte[] byteArray, ClassLoader cl) throws IOException, ClassNotFoundException {
        InputStream is = new ByteArrayInputStream(byteArray);
        ObjectInputStream objectInputStream = null;

        try {
            if (cl == null) {
                objectInputStream = new ObjectInputStream(is);
            } else {
                objectInputStream = new ObjectInputStreamWithClassLoader(is, cl);
            }

            return objectInputStream.readObject();
        } finally {
            // close streams;
            if (objectInputStream != null) {
                objectInputStream.close();
            }
            is.close();
        }
    }

    private static void writeToStream(ObjectOutputStream objectOutputStream, Object o) throws IOException {
        objectOutputStream.writeObject(o);
        objectOutputStream.flush();
    }

    private static class ObjectInputStreamWithClassLoader extends ObjectInputStream {
        private ClassLoader cl;

        public ObjectInputStreamWithClassLoader(InputStream in, ClassLoader cl) throws IOException {
            super(in);
            this.cl = cl;
        }

        protected Class<?> resolveClass(java.io.ObjectStreamClass v)
                throws java.io.IOException, ClassNotFoundException {
            if (cl == null) {
                return super.resolveClass(v);
            } else {
                // should not use directly loadClass due to jdk bug 6434149
                return Class.forName(v.getName(), true, this.cl);
            }
        }
    }

}
