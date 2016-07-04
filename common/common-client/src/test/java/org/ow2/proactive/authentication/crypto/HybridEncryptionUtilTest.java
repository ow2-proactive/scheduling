package org.ow2.proactive.authentication.crypto;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.ow2.proactive.authentication.crypto.HybridEncryptionUtil.HybridEncryptedData;


public class HybridEncryptionUtilTest {

    @Test
    public void encrypt_decrypt() throws Exception {
        KeyPair keyPair = KeyPairUtil.generateKeyPair("RSA", 2048);
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        HybridEncryptedData encryptedData = HybridEncryptionUtil.encryptString("hello", publicKey);

        String decryptedData = HybridEncryptionUtil.decryptString(encryptedData, privateKey);

        assertEquals("hello", decryptedData);
    }

}