/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package functionaltests.jmx;

import functionalTests.FunctionalTest;


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
        /** 
         //Test became useless, next test coming soon
         final String userLogin = "user";
         final String userPassword = "pwd";
         final String adminLogin = "demo";
         final String adminPassword = "demo";

         final SchedulerAuthenticationInterface auth = (SchedulerAuthenticationInterface) SchedulerTHelper.getSchedulerAuth();
         
         final PublicKey pubKey = auth.getPublicKey();
         //final Credentials userCreds = Credentials.createCredentials(userLogin, userPassword, pubKey);
         final Credentials adminCreds = Credentials.createCredentials(adminLogin, adminPassword, pubKey);

         final JMXServiceURL jmxRmiServiceURL = new JMXServiceURL(auth
         .getJMXConnectorURL(JMXTransportProtocol.RMI));
         final JMXServiceURL jmxRoServiceURL = new JMXServiceURL(auth
         .getJMXConnectorURL(JMXTransportProtocol.RO));
         final ObjectName beanName = new ObjectName(SchedulerJMXHelper.SCHEDULER_BEAN_NAME);
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

         SchedulerTHelper.log("Test as user 2 - Check anonymMBean is registered in the MBean server");
         Assert.assertTrue("AnonymMBean is not registered", conn.isRegistered(beanName));
         
         
         SchedulerTHelper.log("Test as user 3 - Check adminMBean not accessible by queryNames()");
         for (final Object o : conn.queryNames(null, null)) {
         Assert.assertFalse("AdminMBean must not be accessible from user connection", ((ObjectName) o)
         .equals(beanName));
         }

         SchedulerTHelper.log("Test as user 4 - Check adminMBean not accessible by queryMBeans()");
         for (final Object o : conn.queryMBeans(null, null)) {
         Assert.assertFalse("AdminMBean must not be accessible from user connection",
         ((ObjectInstance) o).getObjectName().equals(beanName));
         }
         SchedulerTHelper
         .log("Test as user 5 - Check adminMBean name injection (expecting an InstanceNotFoundException)");
         try {
         conn.isRegistered(beanName);
         } catch (Exception e) {
         Assert.assertTrue("AdminMBean injection must throw InstanceNotFoundException",
         e.getCause() instanceof InstanceNotFoundException);
         }

         SchedulerTHelper.log("Test as user 6 - Check anonymMBean attributes are correct");

         final String[] attributesToCheck = new String[] { "SchedulerStatus", "TotalNumberOfJobs",
         "NumberOfFinishedJobs", "TotalNumberOfTasks", "NumberOfFinishedTasks" };

         // Get all attributes to test BEFORE JOB SUBMISSION
         AttributeList list = conn.getAttributes(beanName, attributesToCheck);
         Attribute att = (Attribute) list.get(0); // SchedulerStatus
         Assert.assertEquals("Incorrect value of " + att.getName() + " attribute", "Started", att
         .getValue());
         att = (Attribute) list.get(1); // TotalNumberOfJobs
         Assert.assertEquals("Incorrect value of " + att.getName() + " attribute", 0, att.getValue());
         att = (Attribute) list.get(2); // NumberOfFinishedJobs
         Assert.assertEquals("Incorrect value of " + att.getName() + " attribute", 0, att.getValue());
         att = (Attribute) list.get(3); // TotalNumberOfTasks
         Assert.assertEquals("Incorrect value of " + att.getName() + " attribute", 0, att.getValue());
         att = (Attribute) list.get(4); // NumberOfFinishedTasks
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
         list = conn.getAttributes(beanName, attributesToCheck);
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

         SchedulerTHelper.log("Test as admin 2 - Check adminMBean is registered in the MBean server");
         Assert.assertTrue("AdminMBean is not registered", conn.isRegistered(beanName));
        
         SchedulerTHelper.log("Test as admin 3 - Check anonymMBean not accessible by queryNames()");
         for (final Object o : conn.queryNames(null, null)) {
         Assert.assertFalse(
         "AnonymMBean must not be accessible by queryName() from an admin connection",
         ((ObjectName) o).equals(beanName));
         }
         SchedulerTHelper
         .log("Test as admin 4 - Check adminMBean attributes can be called without throwing exceptions");
         final MBeanInfo info = conn.getMBeanInfo(beanName);
         for (final MBeanAttributeInfo att : info.getAttributes()) {
         try {
         conn.getAttribute(beanName, att.getName());
         } catch (Exception e) {
         Assert.fail("The attribute " + att + " of adminMBean must not throw " + e);
         }
         }
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
         final boolean isConnected1 = client.connect(); // default is over RMI
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
         **/

    }
}
