/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.util.converter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.ProProperties;
import org.objectweb.proactive.core.mop.PAObjectInputStream;

import sun.rmi.server.MarshalInputStream;


/**
 * This class acts as a wrapper to enable the use of different serialization code
 * depending on the proactive configuration
 *
 */
public class ByteToObjectConverter {
    // IBIS Classes and methods names we need to perform reflection
    private static final String CLOSE = "close";
    private static final String READ_OBJECT = "readObject";
    private static final String IBIS_SERIALIZATION_INPUT_STREAM = "ibis.io.IbisSerializationInputStream";
    private static final String BUFFERED_ARRAY_INPUT_STREAM = "ibis.io.BufferedArrayInputStream";
    private static final String BYTE_ARRAY_INPUT_STREAM = "java.io.ByteArrayInputStream";

    public static class MarshallStream {

        /**
         * Convert to an object using a marshall stream;
         * @param byteArray the byte array to covnert
         * @return the unserialized object
         * @throws IOException
         * @throws ClassNotFoundException
         */
        public static Object convert(byte[] byteArray)
            throws IOException, ClassNotFoundException {
            return ByteToObjectConverter.convert(byteArray,
                MakeDeepCopy.ConversionMode.MARSHALL);
        }
    }

    public static class ObjectStream {

        /**
         * Convert to an object using a regular object stream;
         * @param byteArray the byte array to covnert
         * @return the unserialized object
         * @throws IOException
         * @throws ClassNotFoundException
         */
        public static Object convert(byte[] byteArray)
            throws IOException, ClassNotFoundException {
            return ByteToObjectConverter.convert(byteArray,
                MakeDeepCopy.ConversionMode.OBJECT);
        }
    }

    public static class ProActiveObjectStream {

        /**
         * Convert to an object using a proactive object stream;
         * @param byteArray the byte array to covnert
         * @return the unserialized object
         * @throws IOException
         * @throws ClassNotFoundException
         */
        public static Object convert(byte[] byteArray)
            throws IOException, ClassNotFoundException {
            return ByteToObjectConverter.convert(byteArray,
                MakeDeepCopy.ConversionMode.PAOBJECT);
        }
    }

    private static Object convert(byte[] byteArray,
        MakeDeepCopy.ConversionMode conversionMode)
        throws IOException, ClassNotFoundException {
        final String mode = ProProperties.PA_COMMUNICATION_PROTOCOL.getValue();

        //here we check wether or not we are running in ibis
        if (Constants.IBIS_PROTOCOL_IDENTIFIER.equals(mode)) {
            return ibisConvert(byteArray);
        } else {
            return standardConvert(byteArray, conversionMode);
        }
    }

    private static Object readFromStream(ObjectInputStream objectInputStream)
        throws IOException, ClassNotFoundException {
        return objectInputStream.readObject();
    }

    private static Object standardConvert(byte[] byteArray,
        MakeDeepCopy.ConversionMode conversionMode)
        throws IOException, ClassNotFoundException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        ObjectInputStream objectInputStream = null;

        try {
            // we use enum and static calls to avoid object instanciation
            if (conversionMode == MakeDeepCopy.ConversionMode.MARSHALL) {
                objectInputStream = new MarshalInputStream(byteArrayInputStream);
            } else if (conversionMode == MakeDeepCopy.ConversionMode.PAOBJECT) {
                objectInputStream = new PAObjectInputStream(byteArrayInputStream);
            } else /*(conversionMode == ObjectToByteConverter.ConversionMode.OBJECT)*/
             {
                objectInputStream = new ObjectInputStream(byteArrayInputStream);
            }
            return ByteToObjectConverter.readFromStream(objectInputStream);
        } finally {
            // close streams;
            if (objectInputStream != null) {
                objectInputStream.close();
            }
            byteArrayInputStream.close();
        }
    }

    @SuppressWarnings("unchecked")
    private static Object ibisConvert(byte[] b)
        throws IOException, ClassNotFoundException {
        try {
            final Class cl_bais = Class.forName(BYTE_ARRAY_INPUT_STREAM);
            final Class cl_buais = Class.forName(BUFFERED_ARRAY_INPUT_STREAM);
            final Class cl_isis = Class.forName(IBIS_SERIALIZATION_INPUT_STREAM);
            final Constructor c_bais = cl_bais.getConstructor(Array.newInstance(
                        byte.class, 0).getClass());

            final Constructor c_buais = cl_buais.getConstructor(new Class[] {
                        java.io.InputStream.class
                    });
            final Constructor c_isis = cl_isis.getConstructor(new Class[] {
                        Class.forName("ibis.io.DataInputStream")
                    });

            //      final ByteArrayInputStream bi = new ByteArrayInputStream(b);
            final ByteArrayInputStream i_bais = (ByteArrayInputStream) c_bais.newInstance(b);

            //      final BufferedArrayInputStream ai = new BufferedArrayInputStream(bi);
            final Object i_buais = c_buais.newInstance(new Object[] { i_bais });

            //      final IbisSerializationInputStream si = new IbisSerializationInputStream(ai);
            final Object i_isis = c_isis.newInstance(new Object[] { i_buais });

            final Method readObjectMth = cl_isis.getMethod(READ_OBJECT);
            final Method closeMth = cl_isis.getMethod(CLOSE);

            //      final Object unserialized = si.readObject();
            final Object unserialized = readObjectMth.invoke(i_isis,
                    new Object[] {  });

            closeMth.invoke(i_isis, new Object[] {  });

            return unserialized;
        } catch (ClassNotFoundException e) {
            //TODO replace by IOException(Throwable e) java 1.6
            MakeDeepCopy.logger.warn("Check your classpath for ibis jars ");
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        } catch (SecurityException e) {
            MakeDeepCopy.logger.warn("Check your classpath for ibis jars ");
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        } catch (NoSuchMethodException e) {
            MakeDeepCopy.logger.warn("Check your classpath for ibis jars ");
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        } catch (IllegalArgumentException e) {
            MakeDeepCopy.logger.warn("Check your classpath for ibis jars ");
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        } catch (InstantiationException e) {
            MakeDeepCopy.logger.warn("Check your classpath for ibis jars ");
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        } catch (IllegalAccessException e) {
            MakeDeepCopy.logger.warn("Check your classpath for ibis jars ");
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        } catch (InvocationTargetException e) {
            MakeDeepCopy.logger.warn("Check your classpath for ibis jars ");
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        }
    }
}
