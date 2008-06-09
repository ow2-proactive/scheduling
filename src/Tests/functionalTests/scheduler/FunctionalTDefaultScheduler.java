package functionalTests.scheduler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.extensions.resourcemanager.RMFactory;
import org.objectweb.proactive.extensions.resourcemanager.common.FileToBytesConverter;
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
            "/functionalTests/scheduler/GCMNodeSourceDeployment.xml").getPath();

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

        byte[] GCMDeploymentData = FileToBytesConverter.convertFileToByteArray(new File(defaultDescriptor));
        admin.createGCMNodesource(GCMDeploymentData, "GCM_Node_Source");

        ResourceManagerProxy imp = ResourceManagerProxy.getProxy(new URI("rmi://localhost:" +
            PAProperties.PA_RMI_PORT.getValue() + "/"));

        removeDataBase(defaultDBConfigFile);
        CreateDataBase.createDataBase(defaultDBConfigFile);

        AdminScheduler.createScheduler(defaultDBConfigFile, "scripts/unix/scheduler", imp,
                "org.objectweb.proactive.extensions.scheduler.policy.PriorityPolicy");
        Thread.sleep(3000);
    }

    @After
    public void endTest() throws Exception {
        removeDataBase(defaultDBConfigFile);
    }

    public static void removeDataBase(String configFile) {
        Properties props = new Properties();
        BufferedInputStream bis = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(configFile));
            props.load(bis);
            String databasePath = props.getProperty("db_path");
            String databaseName = props.getProperty("db_name");
            File dataBaseDir;

            if (databasePath.equals("")) {
                dataBaseDir = new File(databaseName);
            } else {
                dataBaseDir = new File(databasePath + File.separator + databaseName);
            }

            if (deleteDirectory(dataBaseDir)) {
                System.out.println("Scheduler database removed");
            } else {
                System.out.println("Cannot remove dabase directory : " + databasePath + File.separator +
                    databaseName);
            }

        } catch (FileNotFoundException e) {
            System.out.println("Cannot find config file : " + configFile + " for Database to remove");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Cannot read config file : " + configFile + " for Database to remove");
            e.printStackTrace();
        }
    }

    static public boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
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
