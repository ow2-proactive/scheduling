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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.mop.PAObjectInputStream;

import ibis.io.BufferedArrayInputStream;
import ibis.io.IbisSerializationInputStream;

import sun.rmi.server.MarshalInputStream;


/**
 * This class acts as a wrapper to enable the use of different serialization code
 * depending on the proactive configuration
 *
 */
public class ByteToObjectConverter {
    public static class MarshallStream {
        public static Object convert(byte[] byteArray)
            throws IOException, ClassNotFoundException {
            return ByteToObjectConverter.convert(byteArray,
                MakeDeepCopy.ConversionMode.MARSHALL);
        }
    }

    public static class ObjectStream {
        public static Object convert(byte[] byteArray)
            throws IOException, ClassNotFoundException {
            return ByteToObjectConverter.convert(byteArray,
                MakeDeepCopy.ConversionMode.OBJECT);
        }
    }

    public static class ProActiveObjectStream {
        public static Object convert(byte[] byteArray)
            throws IOException, ClassNotFoundException {
            return ByteToObjectConverter.convert(byteArray,
                MakeDeepCopy.ConversionMode.PAOBJECT);
        }
    }

    private static Object convert(byte[] byteArray,
        MakeDeepCopy.ConversionMode conversionMode)
        throws IOException, ClassNotFoundException {
        final String mode = ProActiveConfiguration.getInstance()
                                                  .getProperty(Constants.PROPERTY_PA_COMMUNICATION_PROTOCOL);

        //here we check wether or not we are running in ibis
        if ("ibis".equals(mode)) {
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

    private static Object ibisConvert(byte[] b)
        throws IOException, ClassNotFoundException {
        final ByteArrayInputStream bo = new ByteArrayInputStream(b);
        final BufferedArrayInputStream ao = new BufferedArrayInputStream(bo);
        final IbisSerializationInputStream so = new IbisSerializationInputStream(ao);
        final Object unserialized = so.readObject();
        so.readArray(b);
        so.close();
        return unserialized;
    }
}
