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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.util.converter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.mop.PAObjectOutputStream;
import org.objectweb.proactive.core.util.converter.MakeDeepCopy.ConversionMode;

import ibis.io.BufferedArrayOutputStream;
import ibis.io.IbisSerializationOutputStream;

import sun.rmi.server.MarshalOutputStream;


/**
 * This class acts as a wrapper to enable the use of different serialization code
 * depending on the proactive configuration
 *
 */
public class ObjectToByteConverter {
    public static class MarshallStream {
        public static byte[] convert(Object o) throws ProActiveException {
            return ObjectToByteConverter.convert(o, ConversionMode.MARSHALL);
        }
    }

    public static class ObjectStream {
        public static byte[] convert(Object o) throws ProActiveException {
            return ObjectToByteConverter.convert(o, ConversionMode.OBJECT);
        }
    }

    public static class ProActiveObjectStream {
        public static byte[] convert(Object o) throws ProActiveException {
            return ObjectToByteConverter.convert(o, ConversionMode.PAOBJECT);
        }
    }

    private static byte[] convert(Object o, ConversionMode conversionMode)
        throws ProActiveException {
        try {
            final String mode = ProActiveConfiguration.getInstance()
                                                      .getProperty(Constants.PROPERTY_PA_COMMUNICATION_PROTOCOL);

            //here we check wether or not we are running in ibis
            if ("ibis".equals(mode)) {
                return ibisConvert(o);
            } else {
                return standardConvert(o, conversionMode);
            }
        } catch (IOException e) {
            throw new ProActiveException(e);
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
                objectOutputStream = new MarshalOutputStream(byteArrayOutputStream);
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

    private static byte[] ibisConvert(Object o) throws IOException {
        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        final BufferedArrayOutputStream ao = new BufferedArrayOutputStream(bo);
        final IbisSerializationOutputStream so = new IbisSerializationOutputStream(ao);
        so.writeObject(o);
        so.flush();
        so.close();
        return bo.toByteArray();
    }
}
