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
package org.objectweb.proactive.ic2d.security.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;


public class CertificateTreeMapTransfer extends ByteArrayTransfer {
    private static final String MYTYPENAME = "CertificateTreeMap";
    private static final int MYTYPEID = registerType(MYTYPENAME);
    private static CertificateTreeMapTransfer instance;

    public static CertificateTreeMapTransfer getInstance() {
        if (instance == null) {
            instance = new CertificateTreeMapTransfer();
        }
        return instance;
    }

    @Override
    protected String[] getTypeNames() {
        return new String[] { MYTYPENAME };
    }

    @Override
    protected int[] getTypeIds() {
        return new int[] { MYTYPEID };
    }

    @Override
    protected boolean validate(Object object) {
        if ((object == null) || !(object instanceof CertificateTreeMap) ||
            (((CertificateTreeMap) object).size() == 0)) {
            return false;
        }
        return true;
    }

    @Override
    public void javaToNative(Object object, TransferData transferData) {
        if (!validate(object) || !isSupportedType(transferData)) {
            DND.error(DND.ERROR_INVALID_DATA);
        }

        // CertificateTreeMap trees = (CertificateTreeMap) object;
        try {
            // write data to a byte array and then ask super to convert to
            // pMedium
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream writeOut = new ObjectOutputStream(out);

            writeOut.writeObject(object);
            byte[] buffer = out.toByteArray();
            writeOut.close();
            super.javaToNative(buffer, transferData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object nativeToJava(TransferData transferData) {
        if (isSupportedType(transferData)) {
            byte[] buffer = (byte[]) super.nativeToJava(transferData);
            if (buffer == null) {
                return null;
            }

            Object chains = null;
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(buffer);
                ObjectInputStream readIn = new ObjectInputStream(in);
                chains = readIn.readObject();
                readIn.close();
            } catch (IOException ex) {
                return null;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return chains;
        }

        return null;
    }
}
