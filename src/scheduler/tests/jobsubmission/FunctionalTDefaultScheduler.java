package jobsubmission;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.config.PAProperties;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.common.FileToBytesConverter;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerConnection;
import org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface;
import org.ow2.proactive.scheduler.core.AdminScheduler;
import org.ow2.proactive.scheduler.resourcemanager.ResourceManagerProxy;
import org.ow2.proactive.scheduler.util.CreateDataBase;

import functionalTests.FunctionalTest;


public class FunctionalTDefaultScheduler extends FunctionalTest {

    protected UserSchedulerInterface schedUserInterface;
    protected SchedulerAuthenticationInterface schedulerAuth;

    private static String defaultDescriptor = FunctionalTDefaultScheduler.class.getResource(
            "/jobsubmission/GCMNodeSourceDeployment.xml").getPath();

    private static String defaultDBConfigFile = FunctionalTDefaultScheduler.class.getResource(
            "/jobsubmission//scheduler_db.cfg").getPath();

    private static String functionalTestRMProperties = FunctionalTDefaultScheduler.class.getResource(
            "/jobsubmission/functionalTRMProperties.ini").getPath();

    private static String AuthenticationFilesDir = FunctionalTDefaultScheduler.class.getResource(
            "/jobsubmission/").getPath();

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

        PAResourceManagerProperties.updateProperties(functionalTestRMProperties);

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

        ResourceManagerProxy rmp = ResourceManagerProxy.getProxy(new URI("rmi://localhost:" +
            PAProperties.PA_RMI_PORT.getValue() + "/"));

        removeDataBase(defaultDBConfigFile);
        CreateDataBase.createDataBase(defaultDBConfigFile);

        AdminScheduler.createScheduler(defaultDBConfigFile, AuthenticationFilesDir, rmp,
                "org.ow2.proactive.scheduler.policy.PriorityPolicy");

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
