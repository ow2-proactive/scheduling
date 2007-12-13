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
package org.objectweb.proactive.core.security.crypto;

import java.io.Serializable;
import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;


public class SessionKeyFactory implements Serializable {
    // private SecureRandom      rand = new FixedSecureRandom();
    private KeyGenerator keyGen;

    public SessionKeyFactory() {
        //  myProvider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
        // Tester ici si ca n'a pas ete deja fait : cf mail...
        //  Security.addProvider(myProvider);
        try {
            // "BC" is the name of the BouncyCastle provider
            keyGen = KeyGenerator.getInstance("Rijndael", "BC");
            keyGen.init(128, new SecureRandom());

            // keyGen.init(128,rand);
        } catch (Exception e) {
            System.out.println("Exception in the Session Key Generation : " + e);
        }
    }

    public Key generateSessionKey() {
        return keyGen.generateKey();
    }
}
