/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionnaltests;

import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;

import functionalTests.FunctionalTest;


/**
 * FunctionalTDefaultScheduler is the test class for the Scheduler.
 *
 * @author The ProActive Team
 * @date 2 juil. 08
 * @since ProActive 4.0
 *
 */
public class FunctionalTDefaultScheduler extends FunctionalTest {

    protected static String defaultDescriptor = FunctionalTDefaultScheduler.class.getResource(
            "config/GCMNodeSourceDeployment.xml").getPath();

    protected static URL startForkedSchedulerApplication = FunctionalTDefaultScheduler.class
            .getResource("/functionnaltests/config/StartForkedSchedulerApplication.xml");

    protected static String functionalTestRMProperties = FunctionalTDefaultScheduler.class.getResource(
            "config/functionalTRMProperties.ini").getPath();
    protected static String functionalTestSchedulerProperties = FunctionalTDefaultScheduler.class
            .getResource("config/functionalTSchedulerProperties.ini").getPath();

    protected static String schedulerDefaultURL = "//Localhost/";

    public static final String VAR_OS = "os";

    public VariableContractImpl vContract;
    public GCMApplication gcmad;

    protected SchedulerAuthenticationInterface schedulerAuth;
    protected UserSchedulerInterface schedUserInterface;

    protected String username = "jl";
    protected String password = "jl";

    public FunctionalTDefaultScheduler() {

    }

    /**
     * Performs all preparatory actions for  a test on ProActive Scheduler :
     * launches a Resource Manager with 4 local nodes
     * create a database for Scheduler
     * Launch scheduler a with a FIFO scheduling policy 
     * 
     * @throws Exception
     */
    @Before
    public void before() throws Exception {
        createVContract();
        startScheduler(false);
    }

    /**
     * Start the scheduler using a forked JVM.
     *
     * @param startWithDefaultConfiguration true if you want to start the scheduler with the default
     * 			starting configuration (UPDATE) or false for the test configuration (CREATE-DROP).
     * @throws Exception
     */
    private void startScheduler(boolean startWithDefaultConfiguration) throws Exception {
        GCMVirtualNode vn = gcmad.getVirtualNode("VN");
        Node node = vn.getANode();

        MyAO myAO = (MyAO) PAActiveObject.newActive(MyAO.class.getName(), null, node);
        schedulerAuth = myAO.createAndJoinForkedScheduler(startWithDefaultConfiguration);

        schedUserInterface = schedulerAuth.logAsUser(username, password);
    }

    /**
     * Restart the scheduler using a forked JVM.
     *
     * @param startWithDefaultConfiguration true if you want to start the scheduler with the default database
     * 			starting configuration (UPDATE) or false for the test configuration (CREATE-DROP).
     * @throws Exception
     */
    protected void restartScheduler(boolean startWithDefaultConfiguration) throws Exception {
        schedulerAuth = null;
        schedUserInterface = null;
        createVContract();
        startScheduler(startWithDefaultConfiguration);
    }

    private void createVContract() throws Exception {
        vContract = new VariableContractImpl();
        vContract.setVariableFromProgram(VAR_OS, OperatingSystem.getOperatingSystem().name(),
                VariableContractType.DescriptorDefaultVariable);
        StringBuilder properties = new StringBuilder("-Djava.security.manager");
        properties.append(" " + PAProperties.PA_HOME.getCmdLine() + PAProperties.PA_HOME.getValue());
        properties.append(" " + PAProperties.JAVA_SECURITY_POLICY.getCmdLine() +
            PAProperties.JAVA_SECURITY_POLICY.getValue());
        properties.append(" " + PAProperties.LOG4J.getCmdLine() + PAProperties.LOG4J.getValue());
        properties.append(" " + PASchedulerProperties.SCHEDULER_HOME.getCmdLine() +
            PASchedulerProperties.SCHEDULER_HOME.getValueAsString());
        properties.append(" " + PAResourceManagerProperties.RM_HOME.getCmdLine() +
            PAResourceManagerProperties.RM_HOME.getValueAsString());
        vContract.setVariableFromProgram("jvmargDefinedByTest", properties.toString(),
                VariableContractType.DescriptorDefaultVariable);
        gcmad = PAGCMDeployment.loadApplicationDescriptor(startForkedSchedulerApplication, vContract);
        gcmad.startDeployment();
    }

    @After
    public void after() {
        if (gcmad != null) {
            gcmad.kill();
        }
    }

    protected void log(String s) {
        System.out.println("------------------------------ " + s);
    }

}
