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
import org.objectweb.proactive.core.config.PAProperties;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.common.FileToBytesConverter;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface;
import org.ow2.proactive.scheduler.core.AdminScheduler;
import org.ow2.proactive.scheduler.resourcemanager.ResourceManagerProxy;
import org.ow2.proactive.scheduler.util.CreateDataBase;

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

    protected UserSchedulerInterface schedUserInterface;
    protected SchedulerAuthenticationInterface schedulerAuth;

    private static String defaultDescriptor = FunctionalTDefaultScheduler.class.getResource(
            "GCMNodeSourceDeployment.xml").getPath();

    private static String defaultDBConfigFile = FunctionalTDefaultScheduler.class.getResource(
            "scheduler_db.cfg").getPath();

    private static String functionalTestRMProperties = FunctionalTDefaultScheduler.class.getResource(
            "functionalTRMProperties.ini").getPath();

    private static String AuthenticationFilesDir = FunctionalTDefaultScheduler.class.getResource(".")
            .getPath();

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

        System.out.println("Scheduler successfully created !");

        Thread.sleep(3000);
    }

    /**
     * End the test.
     *
     * @throws Exception if an error occurred
     */
    @After
    public void after() throws Exception {
        removeDataBase(defaultDBConfigFile);
    }

    /**
     * Remove the linked database from file system.
     *
     * @param configFile the path of the configuration file.
     */
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
                System.out.println("Cannot remove dabase directory : " + dataBaseDir);
            }

        } catch (FileNotFoundException e) {
            System.out.println("Cannot find config file : " + configFile + " for Database to remove");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Cannot read config file : " + configFile + " for Database to remove");
            e.printStackTrace();
        }
    }

    /**
     * Recursively remove the directory of the given path file.
     *
     * @param path the directory to remove.
     * @return true if the directory has been successfully removed.
     */
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

}
