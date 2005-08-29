/**
 *
 */
package nonregressiontest.security.securitymanager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.objectweb.proactive.ext.security.PolicyServer;
import org.objectweb.proactive.ext.security.ProActiveSecurityDescriptorHandler;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;

import testsuite.test.FunctionalTest;


/**
 * @author arnaud
 *
 */
public class SecurityTestSecurityManager extends FunctionalTest {
    private ProActiveSecurityManager psm = null;
    private ProActiveSecurityManager psm2 = null;

    /**
     *
     */
    public SecurityTestSecurityManager() {
        super("security setup - security manager ",
            "Test if the security manager is operational :  to be serialized and unserialized, ...");
    }

    /* (non-Javadoc)
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bout);

        out.writeObject(psm);
        out.close();

        // Get the bytes of the serialized object
        byte[] buf = bout.toByteArray();

        // retrieve policyserver
        ByteArrayInputStream bis = new ByteArrayInputStream(buf);
        ObjectInputStream is = new ObjectInputStream(bis);

        psm2 = (ProActiveSecurityManager) is.readObject();
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
        if (psm2 != null) {
            return true;
        }
        return false;
    }

    public void endTest() throws Exception {
    }
}
