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

import java.security.PublicKey;
import java.util.HashMap;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import junit.framework.Assert;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.jmx.JMXClientHelper;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.jmx.provider.JMXProviderUtils;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.core.jmx.JMXMonitoringHelper;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;


/**
 * Test the JMX infrastructure of the Resource Manager.
 * 
 * @author ProActive team
 */
public final class ResourceManagerJMXTest extends FunctionalTest {

    /**
     * test function
     * @throws Exception
     */
    @org.junit.Test
    public void action() throws Exception {
        final String userLogin = "user1";
        final String userPassword = "pwd1";
        final String adminLogin = RMTHelper.username;
        final String adminPassword = RMTHelper.password;

        final RMAuthentication auth = (RMAuthentication) RMTHelper.getRMAuth();
        final PublicKey pubKey = auth.getPublicKey();
        //final Credentials userCreds = Credentials.createCredentials(userLogin, userPassword, pubKey);
        final Credentials adminCreds = Credentials.createCredentials(adminLogin, adminPassword, pubKey);

        final JMXServiceURL jmxRmiServiceURL = new JMXServiceURL(auth
                .getJMXConnectorURL(JMXTransportProtocol.RMI));
        final JMXServiceURL jmxRoServiceURL = new JMXServiceURL(auth
                .getJMXConnectorURL(JMXTransportProtocol.RO));
        final ObjectName beanName = new ObjectName(JMXMonitoringHelper.RM_BEAN_NAME);
        final String suffix = "/" + PAResourceManagerProperties.RM_JMX_CONNECTOR_NAME.getValueAsString();

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
            RMTHelper.log("Test jmxRmiServiceURL and jmxRoServiceURL are not equal");
            Assert.assertFalse("The jmxRmiServiceURL and jmxRoServiceURL must not be equal", jmxRmiServiceURL
                    .equals(jmxRoServiceURL));
        }

        {
            RMTHelper.log("Test invalid JMX auth without creds (expect SecurityException)");
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
            RMTHelper.log("Test invalid JMX auth with null login/password creds (expect SecurityException)");
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
            RMTHelper.log("Test invalid JMX auth with bad login/password creds");
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
            RMTHelper.log("Test as user 1 - Auth with login/pass over RMI and check connection");
            // Create the environment                                   
            final HashMap<String, Object> env = new HashMap<String, Object>(1);
            env.put(JMXConnector.CREDENTIALS, new Object[] { userLogin, userPassword });
            // Connect to the JMX RMI Connector Server
            final JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxRmiServiceURL, env);
            final MBeanServerConnection conn = jmxConnector.getMBeanServerConnection();
            // Check that the MBean Server connection is not null   
            Assert.assertNotNull("Unable to obtain the MBean server connection over RMI", conn);

            RMTHelper.log("Test as user 2 - Check anonymMBean is registered in the MBean server");
            Assert.assertTrue("AnonymMBean is not registered", conn.isRegistered(beanName));

            RMTHelper.log("Test as user 3 - Check adminMBean not accessible by queryNames()");
            for (final Object o : conn.queryNames(null, null)) {
                Assert.assertFalse("AdminMBean must not be accessible from user connection", ((ObjectName) o)
                        .equals(beanName));
            }

            RMTHelper.log("Test as user 4 - Check adminMBean not accessible by queryMBeans()");
            for (final Object o : conn.queryMBeans(null, null)) {
                Assert.assertFalse("AdminMBean must not be accessible from user connection",
                        ((ObjectInstance) o).getObjectName().equals(beanName));
            }

            RMTHelper
                    .log("Test as user 5 - Check adminMBean name injection (expecting an InstanceNotFoundException)");
            try {
                conn.isRegistered(beanName);
            } catch (Exception e) {
                Assert.assertTrue("AdminMBean injection must throw InstanceNotFoundException",
                        e.getCause() instanceof InstanceNotFoundException);
            }

            RMTHelper.log("Test as user 6 - Check anonymMBean attributes are correct");

            // Start a new node and add it to the rm
            final Node node = RMTHelper.createNode("test");
            final String nodeURL = node.getNodeInformation().getURL();
            RMTHelper.getResourceManager().addNode(nodeURL).booleanValue(); // force sync

            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);

            // Get all attributes to test
            AttributeList list = conn.getAttributes(beanName, new String[] { "RMStatus",
                    "AvailableNodesCount", "FreeNodesCount" });
            // Check RMStatus
            Attribute att = (Attribute) list.get(0);
            Assert.assertEquals("Incorrect value of " + att.getName() + " attribute", "STARTED", att
                    .getValue());
            // Check AvailableNodesCount
            att = (Attribute) list.get(1);
            Assert.assertEquals("Incorrect value of " + att.getName() + " attribute", 1, att.getValue());
            // Check FreeNodesCount
            att = (Attribute) list.get(2);
            Assert.assertEquals("Incorrect value of " + att.getName() + " attribute", 1, att.getValue());

            RMTHelper.getResourceManager().removeNode(nodeURL, false);

            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);

            // Get all attributes to test
            list = conn.getAttributes(beanName, new String[] { "AvailableNodesCount", "FreeNodesCount" });
            // Check AvailableNodesCount
            att = (Attribute) list.get(0);
            Assert.assertEquals("Incorrect value of " + att.getName() + " attribute", 0, att.getValue());
            // Check FreeNodesCount
            att = (Attribute) list.get(1);
            Assert.assertEquals("Incorrect value of " + att.getName() + " attribute", 0, att.getValue());

            jmxConnector.close();
        }

        // Test as admin over RO
        {
            RMTHelper.log("Test as admin 1, auth with login/creds over RO and check connection");
            // Create the environment                                   
            final HashMap<String, Object> env = new HashMap<String, Object>(1);
            env.put(JMXConnector.CREDENTIALS, new Object[] { adminLogin, adminCreds });
            env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, JMXProviderUtils.RO_PROVIDER_PKGS);
            // Connect to the JMX RO Connector Server
            final JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxRoServiceURL, env);
            final MBeanServerConnection conn = jmxConnector.getMBeanServerConnection();
            // Check that the MBean Server connection is not null
            Assert.assertNotNull("Unable to obtain the MBean server connection over RO", conn);

            RMTHelper.log("Test as admin 2 - Check adminMBean is registered in the MBean server");
            Assert.assertTrue("AdminMBean is not registered", conn.isRegistered(beanName));

            RMTHelper.log("Test as admin 3 - Check anonymMBean not accessible by queryNames()");
            for (final Object o : conn.queryNames(null, null)) {
                Assert.assertFalse(
                        "AnonymMBean must not be accessible by queryName() from an admin connection",
                        ((ObjectName) o).equals(beanName));
            }

            RMTHelper
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
            RMTHelper.log("Test simultaneous JMX-RMI and JMX-RO connections as admin");
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

            RMTHelper
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
            RMTHelper.log("Test JMXClientHelper as admin over RMI with connect() method");
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

    }
}