/**
 *
 */
package nonregressiontest.security.policyserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.objectweb.proactive.ext.security.PolicyServer;
import org.objectweb.proactive.ext.security.ProActiveSecurityDescriptorHandler;

import testsuite.test.FunctionalTest;


/**
 * @author arnaud
 *
 */
public class SecurityTestPolicyServer extends FunctionalTest {
    private PolicyServer policyServer = null;
    private PolicyServer ps = null;

    /**
     *
     */
    public SecurityTestPolicyServer() {
        super("security setup - policy server ",
            "Test if the policy server is able to load a policy file, to be serialized and unserialized");
    }

    /* (non-Javadoc)
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bout);

        out.writeObject(policyServer);
        out.close();

        // Get the bytes of the serialized object
        byte[] buf = bout.toByteArray();

        // retrieve policyserver
        ByteArrayInputStream bis = new ByteArrayInputStream(buf);
        ObjectInputStream is = new ObjectInputStream(bis);

        ps = (PolicyServer) is.readObject();
    }

    /* (non-Javadoc)
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
        policyServer = ProActiveSecurityDescriptorHandler.createPolicyServer(
                "../src/nonregressiontest/security/applicationPolicy.xml");
    }

    public boolean postConditions() throws Exception {
        if (ps != null) {
            return true;
        }
        return false;
    }

    public void endTest() throws Exception {
    }
}
