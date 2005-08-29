package nonregressiontest.security.keygeneration;

import org.objectweb.proactive.ext.security.KeyTools;
import org.objectweb.proactive.ext.security.ProActiveSecurity;

import testsuite.test.FunctionalTest;


/**
 * @author arnaud
 *
 */
public class SecurityTestKeyGen extends FunctionalTest {
    public SecurityTestKeyGen() {
        super("security setup - key generation ",
            "Test if the the Java(TM) Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files is available");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
    }

    /**
     * @see testsuite.test.AbstractTest#endTest()
     */
    public void endTest() throws Exception {
    }

    public boolean postConditions() throws Exception {
        try {
            ProActiveSecurity.loadProvider();
            KeyTools.genKeys(1024);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
