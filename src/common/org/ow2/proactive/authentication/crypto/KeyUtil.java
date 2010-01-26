/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.authentication.crypto;

import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


/**
 * Symmetric cryptography utilities for Key generation, encryption and decryption
 * <p>
 * Refer to the Java Cryptography Extension Reference Guide at
 * {@link http://java.sun.com/j2se/1.5.0/docs/guide/security/jce/JCERefGuide.html} to
 * determine which parameters are best for key algorithm, key size and cipher;
 * although "AES", 128 and "AES" should be good enough in most
 * cases.
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 1.1
 * 
 */
public class KeyUtil {

    /**
     * Generates a secret symmetric key
     * 
     * @param algorithm algorithm used for key generation, ie AES
     * @param size size of the generated key, must be one of 128, 192, 256. Use 128 when unsure,
     *             default configurations and providers should refuse to use longer keys.
     * @return the generated key
     * @throws KeyException key generation or saving failed
     */
    public static SecretKey generateKey(String algorithm, int size) throws KeyException {
        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new KeyException("Cannot initialize key generator", e);
        }

        SecureRandom random = new SecureRandom();
        keyGen.init(size, random);

        return keyGen.generateKey();
    }

    /**
     * Encrypt a message using a symmetric key
     * 
     * @param key secret key used for encryption
     * @param cipherParams cipher parameters: transformations, ie AES
     * @param message the message to encrypt
     * @return the encrypted message
     * @throws KeyException encryption failed, public key recovery failed
     */
    public static byte[] encrypt(SecretKey key, String cipherParams, byte[] message) throws KeyException {
        Cipher ciph = null;
        try {
            ciph = Cipher.getInstance(cipherParams);
            ciph.init(Cipher.ENCRYPT_MODE, key);
        } catch (Exception e) {
            throw new KeyException("Coult not initialize cipher", e);
        }

        byte[] res = null;
        try {
            res = ciph.doFinal(message);
        } catch (Exception e) {
            throw new KeyException("Could not encrypt message", e);
        }
        return res;
    }

    /**
     * Decrypt a message using a symmetric key
     * 
     * @param key secret key used for decryption
     * @param cipherParams cipher parameters: transformations, ie AES
     * @param message the encrypted message
     * @return the decrypted message
     * @throws KeyException private key recovery failed, decryption failed
     */
    public static byte[] decrypt(SecretKey key, String cipherParams, byte[] message) throws KeyException {
        Cipher ciph = null;
        try {
            ciph = Cipher.getInstance(cipherParams);
            ciph.init(Cipher.DECRYPT_MODE, key);
        } catch (Exception e) {
            e.printStackTrace();
            throw new KeyException("Coult not initialize cipher", e);
        }

        byte[] res = null;
        try {
            res = ciph.doFinal(message);
        } catch (Exception e) {
            throw new KeyException("Could not decrypt message", e);
        }
        return res;
    }

}
