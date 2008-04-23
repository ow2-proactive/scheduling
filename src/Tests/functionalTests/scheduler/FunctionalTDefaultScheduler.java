package functionalTests.scheduler;

import java.net.URI;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.extensions.resourcemanager.RMFactory;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMAdmin;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerConnection;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.UserSchedulerInterface;
import org.objectweb.proactive.extensions.scheduler.core.AdminScheduler;
import org.objectweb.proactive.extensions.scheduler.resourcemanager.ResourceManagerProxy;
import org.objectweb.proactive.extensions.scheduler.util.CreateDataBase;

import functionalTests.FunctionalTest;
import functionalTests.descriptor.variablecontract.javapropertiesDescriptor.Test;


public class FunctionalTDefaultScheduler extends FunctionalTest {

    protected UserSchedulerInterface schedUserInterface;
    protected SchedulerAuthenticationInterface schedulerAuth;

    private static String defaultDescriptor = Test.class.getResource(
            "/functionalTests/scheduler/Local4JVM.xml").getPath();
    private static String defaultDBConfigFile = Test.class.getResource(
            "/functionalTests/scheduler/scheduler_db.cfg").getPath();

    private String username = "jl";
    private String password = "jl";

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
        //Starting a local RM
        RMFactory.startLocal();
        RMAdmin admin = RMFactory.getAdmin();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        ProActiveDescriptor pad = PADeployment.getProactiveDescriptor(defaultDescriptor);
        admin.addNodes(pad);
        ResourceManagerProxy imp = ResourceManagerProxy.getProxy(new URI("rmi://localhost:" +
            PAProperties.PA_RMI_PORT.getValue() + "/"));
        CreateDataBase.createDataBase(defaultDBConfigFile);

        AdminScheduler.createScheduler(defaultDBConfigFile, "scripts/unix/scheduler", imp,
                "org.objectweb.proactive.extensions.scheduler.policy.PriorityPolicy");
        Thread.sleep(3000);
    }

    @After
    public void endTest() throws Exception {
        //remove Scheduler Database file
    }

    /**
     * performs connection to scheduler launched  for the test 
     * @throws Exception
     */
    public void connect() throws Exception {
        schedulerAuth = SchedulerConnection.join(null);
        // Log as user
        schedUserInterface = schedulerAuth.logAsUser(username, password);
    }

    /**
     * performs disconnection from scheduler launched  for the test 
     * @throws Exception
     */
    public void disconnect() throws Exception {
        schedUserInterface.disconnect();
    }
}
