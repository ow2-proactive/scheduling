/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.jmx;

import java.security.PublicKey;
import java.util.HashMap;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.Assert;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.jmx.JMXClientHelper;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.jmx.provider.JMXProviderUtils;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.core.jmx.SchedulerJMXHelper;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.examples.WaitAndPrint;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;
import functionaltests.SchedulerTHelper;


/**
 * Test the JMX infrastructure of the Scheduler.
 * 
 * @author ProActive team
 */
public final class SchedulerJMXTest extends FunctionalTest {

    /**
     * test function
     * @throws Exception
     */
    @org.junit.Test
    public void action() throws Exception {

        final String userLogin = "demo";
        final String userPassword = "demo";

        final String adminLogin = RMTHelper.username;
        final String adminPassword = RMTHelper.password;

        final SchedulerAuthenticationInterface auth = (SchedulerAuthenticationInterface) SchedulerTHelper
                .getSchedulerAuth();
        final PublicKey pubKey = auth.getPublicKey();

        // final Credentials userCreds =
        // Credentials.createCredentials(userLogin, userPassword, pubKey);
        final Credentials adminCreds = Credentials.createCredentials(new CredData(adminLogin, adminPassword),
                pubKey);

        final JMXServiceURL jmxRmiServiceURL = new JMXServiceURL(auth
                .getJMXConnectorURL(JMXTransportProtocol.RMI));
        final JMXServiceURL jmxRoServiceURL = new JMXServiceURL(auth
                .getJMXConnectorURL(JMXTransportProtocol.RO));
        final ObjectName allAccountsMBeanName = new ObjectName(SchedulerJMXHelper.ALLACCOUNTS_MBEAN_NAME);
        final ObjectName myAccountMBeanName = new ObjectName(SchedulerJMXHelper.MYACCOUNT_MBEAN_NAME);
        final ObjectName runtimeDataMBeanName = new ObjectName(SchedulerJMXHelper.RUNTIMEDATA_MBEAN_NAME);
        final ObjectName managementMBeanName = new ObjectName(SchedulerJMXHelper.MANAGEMENT_MBEAN_NAME);
        final String suffix = "/" + PASchedulerProperties.SCHEDULER_JMX_CONNECTOR_NAME.getValueAsString();

        {
            RMTHelper.log("Test jmxRmiServiceURL is well formed");
            Assert.assertTrue("The jmxRmiServiceURL protocol must be rmi", jmxRmiServiceURL.getProtocol()
                    .equals("rmi"));
            Assert.assertTrue("The jmxRmiServiceURL URLPath must end with " + suffix, jmxRmiServiceURL
                    .getURLPath().endsWith(suffix));
        }

        {
            RMTHelper.log("Test jmxRoServiceURL is well formed");
            Assert.assertTrue("The jmxRoServiceURL protocol must be ro", jmxRoServiceURL.getProtocol()
                    .equals("ro"));
            Assert.assertTrue("The jmxRoServiceURL URLPath must end with " + suffix, jmxRoServiceURL
                    .getURLPath().endsWith(suffix));
        }

        {
            SchedulerTHelper.log("Test jmxRmiServiceURL and jmxRoServiceURL are not equal");
            Assert.assertFalse("The jmxRmiServiceURL and jmxRoServiceURL must not be equal", jmxRmiServiceURL
                    .equals(jmxRoServiceURL));
        }

        {
            SchedulerTHelper.log("Test invalid JMX auth without creds (expect SecurityException)");
            try {
                JMXConnectorFactory.connect(jmxRmiServiceURL, new HashMap<String, Object>(0));
            } catch (Exception e) {
                Assert
                        .assertTrue(
                                "JMX auth must throw SecurityException if a client tries to connect without creds in the env",
                                e instanceof SecurityException);
            }
        }

        {
            SchedulerTHelper
                    .log("Test invalid JMX auth with null login/password creds (expect SecurityException)");
            // Create the environment
            final HashMap<String, Object> env = new HashMap<String, Object>(1);
            env.put(JMXConnector.CREDENTIALS, new Object[] { null, null });
            try {
                JMXConnectorFactory.connect(jmxRmiServiceURL, env);
            } catch (Exception e) {
                Assert
                        .assertTrue(
                                "JMX auth must throw SecurityException if a client tries to connect with null credentials the env",
                                e instanceof SecurityException);
            }
        }

        {
            SchedulerTHelper.log("Test invalid JMX auth with bad login/password creds");
            // Create the environment
            final HashMap<String, Object> env = new HashMap<String, Object>(1);
            env.put(JMXConnector.CREDENTIALS, new Object[] { "abra", "cadabra" });
            try {
                JMXConnectorFactory.connect(jmxRmiServiceURL, env);
            } catch (Exception e) {
                Assert
                        .assertTrue(
                                "JMX auth must throw SecurityException if a client tries to connect with bad login/password credentials the env",
                                e instanceof SecurityException);
            }
        }

        // Tests as user over RMI
        {
            SchedulerTHelper.log("Test as user 1 - Auth with login/pass over RMI and check connection");
            // Create the environment
            final HashMap<String, Object> env = new HashMap<String, Object>(1);
            env.put(JMXConnector.CREDENTIALS, new Object[] { userLogin, userPassword });
            // Connect to the JMX RMI Connector Server
            final JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxRmiServiceURL, env);
            final MBeanServerConnection conn = jmxConnector.getMBeanServerConnection();
            // Check that the MBean Server connection is not null
            Assert.assertNotNull("Unable to obtain the MBean server connection over RMI", conn);

            SchedulerTHelper.log("Test as user 2 - Check all mbeans are registered in the server");
            Assert.assertTrue("AllAccountsMBean is not registered", conn.isRegistered(allAccountsMBeanName));
            Assert.assertTrue("MyAccountMBean is not registered", conn.isRegistered(myAccountMBeanName));
            Assert.assertTrue("RuntimeDataMBean is not registered", conn.isRegistered(runtimeDataMBeanName));
            Assert.assertTrue("ManagementMBean is not registered", conn.isRegistered(managementMBeanName));

            SchedulerTHelper.log("Test as user 3 - Check MyAccountMBean attributes do not throw exceptions");
            final MBeanInfo info = conn.getMBeanInfo(myAccountMBeanName);
            for (final MBeanAttributeInfo att : info.getAttributes()) {
                final String attName = att.getName();
                try {
                    conn.getAttribute(myAccountMBeanName, attName);
                } catch (Exception e) {
                    Assert.fail("The attribute " + attName + " of MyAccountMBean must not throw " + e);
                }
            }

            SchedulerTHelper.log("Test as user 4 - Check RuntimeDataMBeanName attributes are correct");

            final String[] attributesToCheck = new String[] { "Status", "TotalJobsCount",
                    "FinishedJobsCount", "TotalTasksCount", "FinishedTasksCount" };

            // Get all attributes to test BEFORE JOB SUBMISSION
            AttributeList list = conn.getAttributes(runtimeDataMBeanName, attributesToCheck);
            Attribute att = (Attribute) list.get(0); // Status
            Assert.assertEquals("Incorrect value of " + att.getName() + " attribute", "Started", att
                    .getValue());
            att = (Attribute) list.get(1); // TotalJobsCount
            Assert.assertEquals("Incorrect value of " + att.getName() + " attribute", 0, att.getValue());
            att = (Attribute) list.get(2); // FinishedJobsCount
            Assert.assertEquals("Incorrect value of " + att.getName() + " attribute", 0, att.getValue());
            att = (Attribute) list.get(3); // NumberOfTasksCount
            Assert.assertEquals("Incorrect value of " + att.getName() + " attribute", 0, att.getValue());
            att = (Attribute) list.get(4); // FinishedTasksCount
            Assert.assertEquals("Incorrect value of " + att.getName() + " attribute", 0, att.getValue());

            // Create a job then submit it to the scheduler
            final int taskPerJob = 2;
            final TaskFlowJob job = new TaskFlowJob();
            for (int i = 0; i < taskPerJob; i++) {
                JavaTask task = new JavaTask();
                task.setName("" + i);
                task.setExecutableClassName(WaitAndPrint.class.getName());
                task.addArgument("sleepTime", "1");
                job.addTask(task);
            }

            // log as admin since its creds are already available
            final JobId id = SchedulerTHelper.submitJob(job);
            SchedulerTHelper.waitForEventJobFinished(id);

            // Get all attributes to test AFTER JOB EXECUTION
            list = conn.getAttributes(runtimeDataMBeanName, attributesToCheck);
            // Check SchedulerStatus
            att = (Attribute) list.get(0);
            Assert.assertEquals("Incorrect value of " + att.getName() + " attribute", "Started", att
                    .getValue());
            // Check TotalNumberOfJobs
            att = (Attribute) list.get(1);
            Assert.assertEquals("Incorrect value of " + att.getName() + " attribute", 1, att.getValue());
            // Check NumberOfFinishedJobs
            att = (Attribute) list.get(2);
            Assert.assertEquals("Incorrect value of " + att.getName() + " attribute", 1, att.getValue());
            // Check TotalNumberOfTasks
            att = (Attribute) list.get(3);
            Assert.assertEquals("Incorrect value of " + att.getName() + " attribute", taskPerJob, att
                    .getValue());
            // Check NumberOfFinishedTasks
            att = (Attribute) list.get(4);
            Assert.assertEquals("Incorrect value of " + att.getName() + " attribute", taskPerJob, att
                    .getValue());

            jmxConnector.close();
        }

        // Test as admin over RO
        {
            SchedulerTHelper.log("Test as admin 1, auth with login/creds over RO and check connection");
            // Create the environment
            final HashMap<String, Object> env = new HashMap<String, Object>(1);
            env.put(JMXConnector.CREDENTIALS, new Object[] { adminLogin, adminCreds });
            env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, JMXProviderUtils.RO_PROVIDER_PKGS);
            // Connect to the JMX RO Connector Server
            final JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxRoServiceURL, env);
            final MBeanServerConnection conn = jmxConnector.getMBeanServerConnection();
            // Check that the MBean Server connection is not null
            Assert.assertNotNull("Unable to obtain the MBean server connection over RO", conn);

            SchedulerTHelper.log("Test as admin 2 - Check ManagementMBean is registered in the MBean server");
            Assert.assertTrue("ManagementMBean is not registered", conn.isRegistered(managementMBeanName));

            RMTHelper.log("Test as admin 3 - Check ManagementMBean attributes do not throw exception");
            final MBeanInfo mInfo = conn.getMBeanInfo(managementMBeanName);
            for (final MBeanAttributeInfo att : mInfo.getAttributes()) {
                final String attName = att.getName();
                try {
                    conn.getAttribute(managementMBeanName, attName);
                } catch (Exception e) {
                    Assert.fail("The attribute " + attName + " of ManagementMBean must not throw " + e);
                }
            }

            RMTHelper.log("Test as admin 4 - Check AllAccountsMBean Username attribute");
            final String username = "Username";
            try {
                conn.setAttribute(allAccountsMBeanName, new Attribute(username, adminLogin));
            } catch (Exception e) {
                Assert.fail("Setting Username attribute of the AllAccountsMBean must not throw " + e);
            }
            String res = "";
            try {
                res = (String) conn.getAttribute(allAccountsMBeanName, username);
            } catch (Exception e) {
                Assert.fail("The attribute " + username + " of AllAccountsMBean must not throw " + e);
            }

            Assert.assertTrue("The attribute " + username + " of returns incorrect value", res
                    .equals(adminLogin));

            jmxConnector.close();
        }

        // Test simultaneous RMI and RO connections
        {
            SchedulerTHelper.log("Test simultaneous JMX-RMI and JMX-RO connections as admin");
            final HashMap<String, Object> env = new HashMap<String, Object>(1);
            env.put(JMXConnector.CREDENTIALS, new Object[] { adminLogin, adminCreds });
            // Connect to the JMX-RMI Connector Server
            final JMXConnector jmxRmiConnector = JMXConnectorFactory.connect(jmxRmiServiceURL, env);
            final MBeanServerConnection conRmi = jmxRmiConnector.getMBeanServerConnection();

            // Connect to the JMX-RO Connector Server
            env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, JMXProviderUtils.RO_PROVIDER_PKGS);
            final JMXConnector jmxRoConnector1 = JMXConnectorFactory.connect(jmxRoServiceURL, env);
            final MBeanServerConnection conRo = jmxRoConnector1.getMBeanServerConnection();

            Assert.assertFalse("In case of simultaneous RMI and RO JMX connections they must not be equal",
                    conRmi.equals(conRo));

            Assert
                    .assertFalse(
                            "In case of simultaneous RMI and RO JMX connections the connectors must not provide the same connection ids",
                            jmxRmiConnector.getConnectionId().equals(jmxRoConnector1.getConnectionId()));

            SchedulerTHelper
                    .log("Test JMX-RO connection unicity (two connections over RO must not have the same id)");
            final JMXConnector jmxRoConnector2 = JMXConnectorFactory.connect(jmxRoServiceURL, env);
            Assert
                    .assertFalse(
                            "In case of multiple RO JMX connections the connectors must not provide the same connection ids",
                            jmxRoConnector1.getConnectionId().equals(jmxRoConnector2.getConnectionId()));

            // Close all connectors
            jmxRoConnector2.close();
            jmxRoConnector1.close();
            jmxRmiConnector.close();
        }

        // Test Helper class
        {
            SchedulerTHelper.log("Test JMXClientHelper as admin over RMI with connect() method");
            final JMXClientHelper client = new JMXClientHelper(auth, new Object[] { adminLogin, adminCreds });
            final boolean isConnected1 = client.connect(); // default is over
            // RMI
            Assert.assertTrue("Unable to connect, exception is " + client.getLastException(), isConnected1);
            Assert.assertTrue("Incorrect default behavior of connect() method it must use RMI protocol",
                    client.getConnector().getConnectionId().startsWith("rmi"));
            client.disconnect();
            Assert.assertFalse("The helper disconnect() must set the helper as disconnected", client
                    .isConnected());

            final boolean isConnected2 = client.connect(JMXTransportProtocol.RO);
            Assert.assertTrue("Unable to connect, exception is " + client.getLastException(), isConnected2);
            Assert.assertTrue("The helper connect(JMXTransportProtocol.RO) method must use RO protocol",
                    client.getConnector().getConnectionId().startsWith("ro"));
            client.disconnect();
            Assert.assertFalse("The helper disconnect() must set the helper as disconnected", client
                    .isConnected());
        }
    }
}
