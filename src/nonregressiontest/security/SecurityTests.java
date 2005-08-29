package nonregressiontest.security;

import nonregressiontest.security.applicationlifecycle.SecurityTestApplicationLifeCycle;
import nonregressiontest.security.dynamicsecuritypropagation.SecurityTestContextPropagation;
import nonregressiontest.security.keygeneration.SecurityTestKeyGen;
import nonregressiontest.security.policyserver.SecurityTestPolicyServer;
import nonregressiontest.security.securitymanager.SecurityTestSecurityManager;
import nonregressiontest.security.sessionkeyexchange.SecurityTestSessionKeyExchange;

import testsuite.test.FunctionalTest;


public class SecurityTests extends FunctionalTest {
    private boolean testSucceed = true;

    public SecurityTests() {
        super("Security Tests");
    }

    public void action() throws Exception {
        FunctionalTest test = null;

        test = new SecurityTestKeyGen();
        test.runTest();
        if (!test.postConditions()) {
            testSucceed = false;
            return;
        }

        new SecurityTestPolicyServer().runTest();
        new SecurityTestSecurityManager().runTest();
        new SecurityTestApplicationLifeCycle().runTest();
        new SecurityTestContextPropagation().runTest();
        new SecurityTestSessionKeyExchange().runTest();
    }

    public void initTest() throws Exception {
    }

    public void endTest() throws Exception {
    }

    public boolean postConditions() throws Exception {
        return testSucceed;
    }
}
