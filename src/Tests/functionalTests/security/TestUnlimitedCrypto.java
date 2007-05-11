package functionalTests.security;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import functionalTests.FunctionalTest;
import functionalTests.security.applicationlifecycle.SecurityTestApplicationLifeCycle;
import functionalTests.security.dynamicsecuritypropagation.SecurityTestContextPropagation;
import functionalTests.security.keygeneration.SecurityTestKeyGen;
import functionalTests.security.policyserver.SecurityTestPolicyServer;
import functionalTests.security.securitymanager.SecurityTestSecurityManager;
import functionalTests.security.sessionkeyexchange.SecurityTestSessionKeyExchange;
import static junit.framework.Assert.assertTrue;
@RunWith(Suite.class)
@SuiteClasses({SecurityTestKeyGen.class,
    SecurityTestPolicyServer.class,
    SecurityTestSecurityManager.class,
    SecurityTestSessionKeyExchange.class,
    SecurityTestApplicationLifeCycle.class,
    SecurityTestContextPropagation.class
})
public class TestUnlimitedCrypto extends FunctionalTest {
    static private final int KSIZE = 4096;

    @BeforeClass
    public static void checkUnlimitedCryptoIsAvailable()
        throws Exception {
        java.security.Security.addProvider(new BouncyCastleProvider());

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(KSIZE);
            KeyPair key = keyGen.generateKeyPair();

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");

            // encrypt the plaintext using the public key
            cipher.init(Cipher.ENCRYPT_MODE, key.getPublic());
            cipher.doFinal(new byte[50]);
        } catch (InvalidKeyException e) {
            System.err.println(
                "Strong Juridiction Policy Files detected, please install the Unlimited Juridiction Policy Files to be able to use ProActive' Security Framework");

            assertTrue(false);
        }
        System.err.println("toto");
    }
}
