/*
 *  *
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.authentication.crypto;

import java.io.UnsupportedEncodingException;
import java.security.KeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
 * Encrypt data with a symmetric key that is asymmetrically encrypted.
 */
public class HybridEncryptionUtil {

    private static final String ENCRYPTED_STRING_CHARSET = "UTF-8";

    /** symmetric encryption parameters */
    // more than that breaks with default provider / config, it *is* secure nonetheless
    private static final int AES_KEYSIZE = 128;
    // should work fine with default providers
    private static final String AES_ALGO = "AES";
    // funny transformations require initial vector parameters, try to avoid them
    private static final String AES_CIPHER = "AES";
    private static final String STRING_ENCRYPTION_CIPHER = "RSA/ECB/PKCS1Padding";

    public static byte[] decrypt(PrivateKey privateKey, String cipher, HybridEncryptedData encryptedData)
            throws KeyException {
        byte[] decryptedData;
        byte[] decryptedSymmetricKey;

        // recover clear AES key using the private key
        try {
            decryptedSymmetricKey = KeyPairUtil.decrypt(privateKey, cipher, encryptedData
                    .getEncryptedSymmetricKey());
        } catch (KeyException e) {
            throw new KeyException("Could not decrypt symmetric key", e);
        }

        // recover clear credentials using the AES key
        try {
            decryptedData = KeyUtil.decrypt(new SecretKeySpec(decryptedSymmetricKey, AES_ALGO), AES_CIPHER,
                    encryptedData.getEncryptedData());
        } catch (KeyException e) {
            throw new KeyException("Could not decrypt data", e);
        }
        return decryptedData;
    }

    public static HybridEncryptedData encrypt(PublicKey publicKey, String cipher, byte[] message)
            throws KeyException {
        // generate symmetric key
        SecretKey aesKey = KeyUtil.generateKey(AES_ALGO, AES_KEYSIZE);

        byte[] encData;
        byte[] encAes;

        // encrypt AES key with public RSA key
        try {
            encAes = KeyPairUtil.encrypt(publicKey, cipher, aesKey.getEncoded());
        } catch (KeyException e) {
            throw new KeyException("Symmetric key encryption failed", e);
        }

        // encrypt clear credentials with AES key
        try {
            encData = KeyUtil.encrypt(aesKey, AES_CIPHER, message);
        } catch (KeyException e) {
            throw new KeyException("Message encryption failed", e);
        }
        return new HybridEncryptedData(encAes, encData);
    }

    public static String decryptString(HybridEncryptedData encryptedData, PrivateKey privateKey)
            throws KeyException {
        try {
            return new String(decrypt(privateKey, STRING_ENCRYPTION_CIPHER, encryptedData),
                ENCRYPTED_STRING_CHARSET);
        } catch (UnsupportedEncodingException ignored) {
            return null; // never happens, we control charset value
        }
    }

    public static HybridEncryptedData encryptString(String value, PublicKey publicKey) throws KeyException {
        try {
            byte[] valueAsBytes = value.getBytes(ENCRYPTED_STRING_CHARSET);
            return encrypt(publicKey, STRING_ENCRYPTION_CIPHER, valueAsBytes);
        } catch (UnsupportedEncodingException ignored) {
            return null; // never happens, we control charset value
        }
    }

    public static class HybridEncryptedData {
        private byte[] encryptedData;
        private byte[] encryptedSymmetricKey;

        public HybridEncryptedData(byte[] encryptedSymmetricKey, byte[] encryptedData) {
            this.encryptedSymmetricKey = encryptedSymmetricKey;
            this.encryptedData = encryptedData;
        }

        public byte[] getEncryptedData() {
            return encryptedData;
        }

        public byte[] getEncryptedSymmetricKey() {
            return encryptedSymmetricKey;
        }
    }
}
