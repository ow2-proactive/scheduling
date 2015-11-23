/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.authentication.crypto;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.Cipher;


/**
 * Asymmetric cryptography utilities for KeyPair generation, encryption and decryption
 * <p>
 * Refer to the <a href="http://java.sun.com/j2se/1.5.0/docs/guide/security/jce/JCERefGuide.html">Java Cryptography Extension Reference Guide</a> to
 * determine which parameters are best for key algorithm, key size and cipher;
 * although "RSA", 1024 and "RSA/ECB/PKCS1Padding" should be good enough in most
 * cases.
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 1.1
 * 
 */
public class KeyPairUtil {

    /**
     * Generates a pair of public and private keys
     * 
     * @param algorithm algorithm used for key generation, ie RSA
     * @param size size of the generated key, must be power of 2 and greater than 512
     * @param privPath path to file to which the generated private key will be saved
     * @param pubPath path to file to which the generated public key will be saved
     * @throws KeyException key generation or saving failed
     */
    public static void generateKeyPair(String algorithm, int size, String privPath, String pubPath)
            throws KeyException {
        KeyPair keyPair = generateKeyPair(algorithm, size);

        PrivateKey privKey = keyPair.getPrivate();
        PublicKey pubKey = keyPair.getPublic();

        FileOutputStream out = null;

        try {
            out = new FileOutputStream(new File(privPath));
            out.write(privKey.getEncoded());
            out.close();
        } catch (Exception e) {
            throw new KeyException("Cannot write private key to disk", e);
        }

        try {
            out = new FileOutputStream(new File(pubPath));
            out.write((algorithm + "\n").getBytes());
            out.write((size + "\n").getBytes());
            out.write(pubKey.getEncoded());
            out.close();
        } catch (Exception e) {
            throw new KeyException("Cannot write public key to disk", e);
        }
    }

    public static KeyPair generateKeyPair(String algorithm, int size) throws KeyException {
        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new KeyException("Cannot initialize keypair generator", e);
        }

        SecureRandom random = new SecureRandom();
        keyGen.initialize(size, random);

        return keyGen.generateKeyPair();
    }

    /**
     * Encrypt a message using asymmetric keys
     * 
     * @param pubKey public key used for encryption
     * @param cipherParams cipher parameters: transformations (ie RSA/ECB/NoPadding)
     * @param message the message to encrypt
     * @return the encrypted message
     * @throws KeyException encryption failed, public key recovery failed
     */
    public static byte[] encrypt(PublicKey pubKey, String cipherParams, byte[] message) throws KeyException {

        Cipher ciph = null;
        try {
            ciph = Cipher.getInstance(cipherParams);
            ciph.init(Cipher.ENCRYPT_MODE, pubKey);
        } catch (Exception e) {
            throw new KeyException("Could not initialize cipher", e);
        }

        byte[] res = null;
        try {
            res = ciph.doFinal(message);
        } catch (Exception e) {
            throw new KeyException("Could not encrypt message.", e);
        }

        return res;
    }

    /**
     * Decrypt a message using asymmetric keys
     * 
     * @param privKey Private key used for decryption
     * @param cipherParams cipher parameters: transformations (ie RSA/ECB/NoPadding)
     * @param message the encrypted message
     * @return the decrypted message
     * @throws KeyException private key recovery failed, decryption failed
     */
    public static byte[] decrypt(PrivateKey privKey, String cipherParams, byte[] message) throws KeyException {

        Cipher ciph = null;
        try {
            ciph = Cipher.getInstance(cipherParams);
            ciph.init(Cipher.DECRYPT_MODE, privKey);
        } catch (Exception e) {
            throw new KeyException("Could not initialize cipher", e);
        }

        byte[] res = null;
        try {
            res = ciph.doFinal(message);
        } catch (Exception e) {
            throw new KeyException("Could not descrypt message.", e);
        }

        return res;
    }

}
