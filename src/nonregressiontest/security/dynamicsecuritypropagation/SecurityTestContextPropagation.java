/**
 *
 */
package nonregressiontest.security.dynamicsecuritypropagation;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.ext.security.PolicyServer;
import org.objectweb.proactive.ext.security.ProActiveSecurityDescriptorHandler;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;

import nonregressiontest.security.A;

import testsuite.test.FunctionalTest;


/**
 * @author arnaud
 *
 */
public class SecurityTestContextPropagation extends FunctionalTest {
    private ProActiveSecurityManager psm = null;
    private boolean isTestValide = false;

    /**
     *
     */
    public SecurityTestContextPropagation() {
        super("security setup - security context propagation ",
            "Test the dynamic propagation of an application context");
    }

    /* (non-Javadoc)
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
        A a = (A) ProActive.newActive("nonregressiontest.security.A",
                new Object[] {  });

        System.out.println(a.hello("arnaud"));
        isTestValide = true;
    }

    /* (non-Javadoc)
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
        PolicyServer ps = ProActiveSecurityDescriptorHandler.createPolicyServer(
                "../src/nonregressiontest/security/applicationPolicy.xml");
        psm = new ProActiveSecurityManager(ps);

        // set the default security manager
        ProActiveMetaObjectFactory.newInstance().setProActiveSecurityManager(psm);
    }

    public boolean postConditions() throws Exception {
        return isTestValide;
    }

    public void endTest() throws Exception {
    }
}
