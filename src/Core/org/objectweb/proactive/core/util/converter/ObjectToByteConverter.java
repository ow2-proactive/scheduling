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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.mop.PAObjectOutputStream;
import org.objectweb.proactive.core.mop.SunMarshalOutputStream;
import org.objectweb.proactive.core.util.converter.MakeDeepCopy.ConversionMode;


/**
 * This class acts as a wrapper to enable the use of different serialization code
 * depending on the proactive configuration
 *
 */
public class ObjectToByteConverter {
    private static final String IBIS_SERIALIZATION_OUTPUT_STREAM = "ibis.io.IbisSerializationOutputStream";
    private static final String BUFFERED_ARRAY_OUTPUT_STREAM = "ibis.io.BufferedArrayOutputStream";
    private static final String BYTE_ARRAY_OUTPUT_STREAM = "java.io.ByteArrayOutputStream";
    private static final String CLOSE = "close";
    private static final String FLUSH = "flush";
    private static final String WRITE_OBJECT = "writeObject";

    public static class MarshallStream {

        /**
         * Convert to an object using a marshall stream;
         * @param byteArray the byte array to covnert
         * @return the unserialized object
         * @throws IOException
         * @throws ClassNotFoundException
         */

        /**
         * Convert an object to a byte array using a marshall stream
         * @param o The object to convert.
         * @return The object converted to a byte array
         * @throws IOException
         */
        public static byte[] convert(Object o) throws IOException {
            return ObjectToByteConverter.convert(o, ConversionMode.MARSHALL);
        }
    }

    public static class ObjectStream {

        /**
         * Convert an object to a byte array using a regular object stream
         * @param o The object to convert.
         * @return The object converted to a byte array
         * @throws IOException
         */
        public static byte[] convert(Object o) throws IOException {
            return ObjectToByteConverter.convert(o, ConversionMode.OBJECT);
        }
    }

    public static class ProActiveObjectStream {

        /**
         * Convert an object to a byte array using a proactive object stream
         * @param o The object to convert.
         * @return The object converted to a byte array
         * @throws IOException
         */
        public static byte[] convert(Object o) throws IOException {
            return ObjectToByteConverter.convert(o, ConversionMode.PAOBJECT);
        }
    }

    private static byte[] convert(Object o, ConversionMode conversionMode)
        throws IOException {
        final String mode = PAProperties.PA_COMMUNICATION_PROTOCOL.getValue();

        //here we check wether or not we are running in ibis
        if (Constants.IBIS_PROTOCOL_IDENTIFIER.equals(mode)) {
            return ibisConvert(o);
        } else {
            return standardConvert(o, conversionMode);
        }
    }

    private static void writeToStream(ObjectOutputStream objectOutputStream,
        Object o) throws IOException {
        objectOutputStream.writeObject(o);
        objectOutputStream.flush();
    }

    private static byte[] standardConvert(Object o,
        ConversionMode conversionMode) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;

        try {
            // we use enum and static calls to avoid object instanciation
            if (conversionMode == ConversionMode.MARSHALL) {
                objectOutputStream = new SunMarshalOutputStream(byteArrayOutputStream);
            } else if (conversionMode == ConversionMode.OBJECT) {
                objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            } else if (conversionMode == ConversionMode.PAOBJECT) {
                objectOutputStream = new PAObjectOutputStream(byteArrayOutputStream);
            }

            ObjectToByteConverter.writeToStream(objectOutputStream, o);
            return byteArrayOutputStream.toByteArray();
        } finally {
            // close streams
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
            byteArrayOutputStream.close();
        }
    }

    @SuppressWarnings("unchecked")
    private static byte[] ibisConvert(Object o) throws IOException {
        try {
            final Class cl_baos = Class.forName(BYTE_ARRAY_OUTPUT_STREAM);
            final Class cl_buaos = Class.forName(BUFFERED_ARRAY_OUTPUT_STREAM);
            final Class cl_isos = Class.forName(IBIS_SERIALIZATION_OUTPUT_STREAM);
            final Constructor c_baos = cl_baos.getConstructor();
            final Constructor c_buaos = cl_buaos.getConstructor(new Class[] {
                        java.io.OutputStream.class
                    });
            final Constructor c_isos = cl_isos.getConstructor(new Class[] {
                        Class.forName("ibis.io.DataOutputStream")
                    });

            //          final ByteArrayOutputStream bo = new ByteArrayOutputStream();
            final ByteArrayOutputStream i_baos = (ByteArrayOutputStream) c_baos.newInstance(new Object[] {
                        
                    });

            //	        final BufferedArrayOutputStream ao = new BufferedArrayOutputStream(bo);
            final Object i_buaos = c_buaos.newInstance(new Object[] { i_baos });

            //	        final IbisSerializationOutputStream so = new IbisSerializationOutputStream(ao);
            final Object i_isos = c_isos.newInstance(new Object[] { i_buaos });

            final Method writeObjectMth = cl_isos.getMethod(WRITE_OBJECT,
                    new Class[] { Object.class });
            final Method flushMth = cl_isos.getMethod(FLUSH);
            final Method closeMth = cl_isos.getMethod(CLOSE);

            //	        so.writeObject(o);
            writeObjectMth.invoke(i_isos, new Object[] { o });

            //			so.flush();
            flushMth.invoke(i_isos, new Object[] {  });

            //			so.close();
            closeMth.invoke(i_isos, new Object[] {  });

            return i_baos.toByteArray();
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
            e.printStackTrace();
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        }
    }
}
