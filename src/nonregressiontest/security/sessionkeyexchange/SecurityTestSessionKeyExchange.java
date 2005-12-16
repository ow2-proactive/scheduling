/**
 *
 */
package nonregressiontest.security.sessionkeyexchange;

import org.objectweb.proactive.ext.security.PolicyServer;
import org.objectweb.proactive.ext.security.ProActiveSecurityDescriptorHandler;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;

import testsuite.test.FunctionalTest;


/**
 * @author arnaud
 *
 */
public class SecurityTestSessionKeyExchange extends FunctionalTest {
    private ProActiveSecurityManager psm = null;
    private boolean testSucceed = true;

    /**
     *
     */
    public SecurityTestSessionKeyExchange() {
        super("security setup - session key exchange  ",
            "Test if the session exchange works.");
    }

    /* (non-Javadoc)
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
        try {
            ProActiveSecurityManager psm1 = psm.generateSiblingCertificate(
                    "caller");
            TestSecurityEntity entity1 = new TestSecurityEntity(psm1);

            ProActiveSecurityManager psm2 = psm.generateSiblingCertificate(
                    "Callee");
            TestSecurityEntity entity2 = new TestSecurityEntity(psm2);

            entity1.initiateSession(1, entity2);
        } catch (Exception e) {
            e.printStackTrace();
            testSucceed = false;
            throw e;
        }
    }

    /* (non-Javadoc)
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
        PolicyServer ps = ProActiveSecurityDescriptorHandler.createPolicyServer(
                "../src/nonregressiontest/security/applicationPolicy.xml");
        psm = new ProActiveSecurityManager(ps);
    }

    public boolean postConditions() throws Exception {
        return testSucceed;
    }

    public void endTest() throws Exception {
    }
}
