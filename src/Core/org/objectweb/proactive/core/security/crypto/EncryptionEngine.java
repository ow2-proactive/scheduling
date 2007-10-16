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

import java.io.IOException;
import java.io.Serializable;
import java.security.Key;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.engines.RSAEngine;


public class EncryptionEngine implements Serializable {
    private SecureRandom rand = new FixedSecureRandom();
    private transient Cipher symmetricCipher;
    private transient Cipher asymmetricCipher;
    private transient AsymmetricBlockCipher eng;

    public EncryptionEngine() {
        try {
            //         symmetricCipher = Cipher.getInstance("Rijndael/ECB/WithCTS");
            //                                                                   "RSA"
            eng = new RSAEngine();
            symmetricCipher = Cipher.getInstance("RIJNDAEL/ECB/WithCTS", "BC");
            asymmetricCipher = Cipher.getInstance("RSA", "BC");
        } catch (Exception e) {
            System.out.println("Exception in cipher creation : " + e);
            e.printStackTrace();
        }
    }

    public Object encrypt(Serializable object, Key sessionKey) {
        try {
            symmetricCipher.init(Cipher.ENCRYPT_MODE, sessionKey, rand);

            return new SealedObject(object, symmetricCipher);
        } catch (Exception e) {
            System.out.println("Exception in encryption :" + e);
            e.printStackTrace();
        }

        return null;
    }

    private void listProvider() {
        try {
            Provider[] p = Security.getProviders();

            for (int i = 0; i < p.length; i++) {
                System.out.println(p[i]);

                for (Enumeration e = p[i].keys(); e.hasMoreElements();) {
                    System.out.println("\t" + e.nextElement());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object decrypt(Object object, Key sessionKey) {
        try {
            symmetricCipher.init(Cipher.DECRYPT_MODE, sessionKey, rand);

            return ((SealedObject) object).getObject(symmetricCipher);
        } catch (Exception e) {
            System.out.println("Exception in decryption :" + e);
            e.printStackTrace();
        }

        return null;
    }

    public Object asymmetric_encrypt(Serializable object, PublicKey key) {
        try {
            //         asymmetricCipher.init(Cipher.ENCRYPT_MODE, key, rand);
            return new SealedObject(object, asymmetricCipher);
        } catch (Exception e) {
            System.out.println("Exception in encryption :" + e);
            e.printStackTrace();
        }

        return null;
    }

    public Object asymmetric_decrypt(Object object, PrivateKey key) {
        try {
            asymmetricCipher.init(Cipher.DECRYPT_MODE, key, rand);

            return ((SealedObject) object).getObject(asymmetricCipher);
        } catch (Exception e) {
            System.out.println("Exception in decryption :" + e);
            e.printStackTrace();
        }

        return null;
    }

    // implements Serializable
    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        // Add bouncycastle security provider
        //   Provider myProvider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
        // Security.addProvider(myProvider);
        //    randomLongGenerator = new RandomLongGenerator();
        listProvider();

        try {
            symmetricCipher = Cipher.getInstance("RIJNDAEL/ECB/WithCTS", "BC");
            asymmetricCipher = Cipher.getInstance("RSA", "BC");
        } catch (Exception e) {
            System.out.println("Exception in cipher creation : " + e);
            e.printStackTrace();
        }
    }
}
