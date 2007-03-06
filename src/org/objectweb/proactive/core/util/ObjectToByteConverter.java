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
package org.objectweb.proactive.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.objectweb.proactive.core.ProActiveException;

import ibis.io.ArrayOutputStream;
import ibis.io.BufferedArrayOutputStream;
import ibis.io.IbisSerializationOutputStream;

import sun.rmi.server.MarshalOutputStream;


/**
 * This class acts as a wrapper to enable the use of different serialization code
 * depending on the proactive configuration
 *
 */
public class ObjectToByteConverter {
    public static byte[] convert(Object o) throws ProActiveException {
        try {
            String mode = System.getProperty("proactive.communication.protocol");

            //here we check wether or not we are running in ibis
            if ("ibis".equals(mode)) {
                return ibisConvert(o);
            } else {
                return standardConvert(o);
            }
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    protected static byte[] standardConvert(Object o) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        MarshalOutputStream objectOutputStream = new MarshalOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(o);
        objectOutputStream.flush();
        objectOutputStream.close();
        byteArrayOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    protected static byte[] ibisConvert(Object o) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ArrayOutputStream ao = new BufferedArrayOutputStream(bo);
        IbisSerializationOutputStream so = new IbisSerializationOutputStream(ao);
        so.writeObject(o);
        so.flush();
        so.close();
        return bo.toByteArray();
    }
}
