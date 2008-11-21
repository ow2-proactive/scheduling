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
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.utils.FileToBytesConverter;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface;
import org.ow2.proactive.scheduler.core.AdminScheduler;
import org.ow2.proactive.scheduler.core.db.CreateDataBase;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.resourcemanager.ResourceManagerProxy;

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

    private static String functionalTestRMProperties = FunctionalTDefaultScheduler.class.getResource(
            "functionalTRMProperties.ini").getPath();

    protected static String schedulerDefaultURL = "//Localhost/" +
        PASchedulerProperties.SCHEDULER_DEFAULT_NAME;

    private static String defaultDBConfigFile = null;

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

        defaultDBConfigFile = PASchedulerProperties
                .getAbsolutePath(PASchedulerProperties.SCHEDULER_DEFAULT_DBCONFIG_FILE.getValueAsString());

        removeDataBase(defaultDBConfigFile);
        CreateDataBase.createDataBase(defaultDBConfigFile);

        AdminScheduler.createScheduler(rmp, "org.ow2.proactive.scheduler.policy.PriorityPolicy");

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
        if (defaultDBConfigFile != null) {
            removeDataBase(defaultDBConfigFile);
        }
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
