/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.ext.security.crypto;

import java.security.*;


class FixedSecureRandom extends SecureRandom {
    byte[] seed = {
            (byte) 0xaa, (byte) 0xfd, (byte) 0x12, (byte) 0xf6, (byte) 0x59,
            (byte) 0xca, (byte) 0xe6, (byte) 0x34, (byte) 0x89, (byte) 0xb4,
            (byte) 0x79, (byte) 0xe5, (byte) 0x07, (byte) 0x6d, (byte) 0xde,
            (byte) 0xc2, (byte) 0xf0, (byte) 0x6c, (byte) 0xb5, (byte) 0x8f
        };

    public void nextBytes(byte[] bytes) {
        int offset = 0;

        while ((offset + seed.length) < bytes.length) {
            System.arraycopy(seed, 0, bytes, offset, seed.length);
            offset += seed.length;
        }

        System.arraycopy(seed, 0, bytes, offset, bytes.length - offset);
    }
}
