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
package functionalTests;

import java.security.KeyPair;

import javax.crypto.Cipher;

import org.junit.BeforeClass;
import org.objectweb.proactive.core.security.KeyTools;
import org.objectweb.proactive.core.security.ProActiveSecurity;


public class SecurityTest extends FunctionalTest {
    @BeforeClass
    static public void checkUnlimitedCrypto() throws Exception {
        try {
            ProActiveSecurity.loadProvider();
            // testing the generation of RSA key whose keysize is superior to the max size allowed by
            // the "Strong Juridiction Policy Files"
            KeyPair kp = KeyTools.genKeys(4096);
            Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
            c.init(Cipher.ENCRYPT_MODE, kp.getPublic());
            c.doFinal(new byte[128]);
        } catch (Exception e) {
            throw new Exception(
                "Strong Juridiction Policy Files detected, please install the Unlimited Juridiction Policy Files to be able to use ProActive' Security Framework ");
        }
    }
}
