/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package functionaltests.jmx;

import static org.junit.Assert.*;

import java.io.IOException;
import java.security.PublicKey;
import java.util.HashMap;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.jmx.JMXClientHelper;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.jmx.provider.JMXProviderUtils;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.core.jmx.RMJMXBeans;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;

import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;
import functionaltests.utils.TestUsers;


/**
 * Test the JMX infrastructure of the Resource Manager. This test supposes the
 * {@link Users#TEST_USERNAME} user has all permissions and the demo user is well
 * defined and have user-level permissions.
 * 
 * @author ProActive team
 */
public final class ResourceManagerJMXTest extends RMFunctionalTest {

    @Test
    public void action() throws Exception {
        final RMAuthentication auth = rmHelper.getRMAuth();
        final PublicKey pubKey = auth.getPublicKey();
        final Credentials adminCreds = Credentials.createCredentials(new CredData(TestUsers.TEST.username,
                                                                                  TestUsers.TEST.password),
                                                                     pubKey);

        final JMXServiceURL jmxRmiServiceURL = new JMXServiceURL(auth.getJMXConnectorURL(JMXTransportProtocol.RMI));
        final JMXServiceURL jmxRoServiceURL = new JMXServiceURL(auth.getJMXConnectorURL(JMXTransportProtocol.RO));
        final ObjectName allAccountsMBeanName = new ObjectName(RMJMXBeans.ALLACCOUNTS_MBEAN_NAME);
        final ObjectName myAccountMBeanName = new ObjectName(RMJMXBeans.MYACCOUNT_MBEAN_NAME);
        final ObjectName runtimeDataMBeanName = new ObjectName(RMJMXBeans.RUNTIMEDATA_MBEAN_NAME);
        final ObjectName managementMBeanName = new ObjectName(RMJMXBeans.MANAGEMENT_MBEAN_NAME);
        final String suffix = "/" + PAResourceManagerProperties.RM_JMX_CONNECTOR_NAME.getValueAsString();

        jmxURLsAreWellFormed(jmxRmiServiceURL, jmxRoServiceURL, suffix);

        jmxAuthInvalidCreds(jmxRmiServiceURL);

        jmxAuthNullLoginPassword(jmxRmiServiceURL);

        jmxAuthInvalidLoginPassword(jmxRmiServiceURL);

        jmxRMIAsUser(jmxRmiServiceURL, allAccountsMBeanName, myAccountMBeanName, runtimeDataMBeanName);

        jmxRemoteObjectAsAdmin(adminCreds, jmxRoServiceURL, allAccountsMBeanName, managementMBeanName);
        simultaneousRMIAndROConnections(adminCreds, jmxRmiServiceURL, jmxRoServiceURL);

        jmxClientHelper(auth, adminCreds);
    }

    private void jmxAuthInvalidLoginPassword(JMXServiceURL jmxRmiServiceURL) {
        RMTHelper.log("Test invalid JMX auth with bad login/password creds");
        // Create the environment
        final HashMap<String, Object> env = new HashMap<>(1);
        env.put(JMXConnector.CREDENTIALS, new Object[] { "abra", "cadabra" });
        try {
            JMXConnectorFactory.connect(jmxRmiServiceURL, env);
        } catch (Exception e) {
            assertTrue("JMX auth must throw SecurityException if a client tries to connect with bad " +
                       "login/password credentials the env", e instanceof SecurityException);
        }
    }

    private void jmxRemoteObjectAsAdmin(Credentials adminCreds, JMXServiceURL jmxRoServiceURL,
            ObjectName allAccountsMBeanName, ObjectName managementMBeanName)
            throws IOException, InstanceNotFoundException, IntrospectionException, ReflectionException {
        // Test as admin over RO
        RMTHelper.log("Test as admin 1, auth with login/creds over RO and check connection");
        // Create the environment
        final HashMap<String, Object> env = new HashMap<>(1);
        env.put(JMXConnector.CREDENTIALS, new Object[] { TestUsers.TEST.username, adminCreds });
        env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, JMXProviderUtils.RO_PROVIDER_PKGS);
        // Connect to the JMX RO Connector Server
        final JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxRoServiceURL, env);
        final MBeanServerConnection conn = jmxConnector.getMBeanServerConnection();
        // Check that the MBean Server connection is not null
        assertNotNull("Unable to obtain the MBean server connection over RO", conn);

        RMTHelper.log("Test as admin 2 - Check ManagementMBean is registered in the MBean server");
        assertTrue("ManagementMBean is not registered", conn.isRegistered(managementMBeanName));

        RMTHelper.log("Test as admin 3 - Check ManagementMBean attributes do not throw exception");
        final MBeanInfo mInfo = conn.getMBeanInfo(managementMBeanName);
        for (final MBeanAttributeInfo att : mInfo.getAttributes()) {
            final String attName = att.getName();
            try {
                conn.getAttribute(managementMBeanName, attName);
            } catch (Exception e) {
                fail("The attribute " + attName + " of ManagementMBean must not throw " + e);
            }
        }

        RMTHelper.log("Test as admin 4 - Check AllAccountsMBean Username attribute");
        final String username = "Username";
        try {
            conn.setAttribute(allAccountsMBeanName, new Attribute(username, TestUsers.TEST.username));
        } catch (Exception e) {
            fail("Setting Username attribute of the AllAccountsMBean must not throw " + e);
        }
        String res = "";
        try {
            res = (String) conn.getAttribute(allAccountsMBeanName, username);
        } catch (Exception e) {
            fail("The attribute " + username + " of AllAccountsMBean must not throw " + e);
        }

        assertTrue("The attribute " + username + " of returns incorrect value", res.equals(TestUsers.TEST.username));

        jmxConnector.close();
    }

    private void jmxURLsAreWellFormed(JMXServiceURL jmxRmiServiceURL, JMXServiceURL jmxRoServiceURL, String suffix) {
        RMTHelper.log("Test jmxRmiServiceURL is well formed");
        assertTrue("The jmxRmiServiceURL protocol must be rmi", jmxRmiServiceURL.getProtocol().equals("rmi"));
        assertTrue("The jmxRmiServiceURL URLPath must end with " + suffix,
                   jmxRmiServiceURL.getURLPath().endsWith(suffix));

        RMTHelper.log("Test jmxRoServiceURL is well formed");
        assertTrue("The jmxRoServiceURL protocol must be ro", jmxRoServiceURL.getProtocol().equals("ro"));
        assertTrue("The jmxRoServiceURL URLPath must end with " + suffix,
                   jmxRoServiceURL.getURLPath().endsWith(suffix));

        RMTHelper.log("Test jmxRmiServiceURL and jmxRoServiceURL are not equal");
        assertFalse("The jmxRmiServiceURL and jmxRoServiceURL must not be equal",
                    jmxRmiServiceURL.equals(jmxRoServiceURL));
    }

    private void jmxAuthInvalidCreds(JMXServiceURL jmxRmiServiceURL) {
        RMTHelper.log("Test invalid JMX auth without creds (expect SecurityException)");
        try {
            JMXConnectorFactory.connect(jmxRmiServiceURL, new HashMap<String, Object>(0));
        } catch (Exception e) {
            assertTrue("JMX auth must throw SecurityException if a client tries to connect without creds in the " +
                       "env", e instanceof SecurityException);
        }
    }

    private void jmxAuthNullLoginPassword(JMXServiceURL jmxRmiServiceURL) {
        RMTHelper.log("Test invalid JMX auth with null login/password creds (expect SecurityException)");
        // Create the environment
        final HashMap<String, Object> env = new HashMap<>(1);
        env.put(JMXConnector.CREDENTIALS, new Object[] { null, null });
        try {
            JMXConnectorFactory.connect(jmxRmiServiceURL, env);
        } catch (Exception e) {
            assertTrue("JMX auth must throw SecurityException if a client tries to connect with null credentials" +
                       " the env", e instanceof SecurityException);
        }
    }

    private void jmxRMIAsUser(JMXServiceURL jmxRmiServiceURL, ObjectName allAccountsMBeanName,
            ObjectName myAccountMBeanName, ObjectName runtimeDataMBeanName) throws Exception {
        // Tests as user over RMI
        RMTHelper.log("Test as user 1 - Auth with login/pass over RMI and check connection");
        // Create the environment
        final HashMap<String, Object> env = new HashMap<>(1);
        env.put(JMXConnector.CREDENTIALS, new Object[] { TestUsers.DEMO.username, TestUsers.DEMO.password });
        // Connect to the JMX RMI Connector Server
        final JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxRmiServiceURL, env);
        final MBeanServerConnection conn = jmxConnector.getMBeanServerConnection();
        // Check that the MBean Server connection is not null
        assertNotNull("Unable to obtain the MBean server connection over RMI", conn);

        RMTHelper.log("Test as user 2 - Check MyAccountMBean is registered in the MBean server");
        assertTrue("MyAccountMBean must be registered in the MBean server", conn.isRegistered(myAccountMBeanName));

        RMTHelper.log("Test as user 3 - Check MyAccountMBean attributes do not throw exceptions");
        final MBeanInfo info = conn.getMBeanInfo(myAccountMBeanName);
        for (final MBeanAttributeInfo att : info.getAttributes()) {
            final String attName = att.getName();
            try {
                conn.getAttribute(myAccountMBeanName, attName);
            } catch (Exception e) {
                fail("The attribute " + attName + " of MyAccountMBean must not throw " + e);
            }
        }

        RMTHelper.log("Test as user 3 - Check RuntimeDataMBean is registered in the MBean server");
        assertTrue("RuntimeDataMBean must be registered in the MBean server", conn.isRegistered(runtimeDataMBeanName));

        RMTHelper.log("Test as user 4 - Check RuntimeDataMBean attributes are correct");
        // Start a new node and add it to the rmHelper
        testNode = rmHelper.createNode("test");
        final Node node = testNode.getNode();
        final String nodeURL = node.getNodeInformation().getURL();
        rmHelper.getResourceManager().addNode(nodeURL).getBooleanValue(); // force sync, now the node is in configuring state

        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        // We eat configuring to free events
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        // Get all attributes to test
        AttributeList list = conn.getAttributes(runtimeDataMBeanName,
                                                new String[] { "Status", "AvailableNodesCount", "FreeNodesCount" });
        // Check RMStatus
        Attribute attribute = (Attribute) list.get(0);
        assertEquals("Incorrect value of " + attribute.getName() + " attribute", "STARTED", attribute.getValue());
        // Check AvailableNodesCount
        attribute = (Attribute) list.get(1);
        assertEquals("Incorrect value of " + attribute.getName() + " attribute", 1, attribute.getValue());
        // Check FreeNodesCount
        attribute = (Attribute) list.get(2);
        assertEquals("Incorrect value of " + attribute.getName() + " attribute", 1, attribute.getValue());

        rmHelper.getResourceManager().removeNode(nodeURL, false);

        rmHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, nodeURL);

        // Get all attributes to test
        list = conn.getAttributes(runtimeDataMBeanName, new String[] { "AvailableNodesCount", "FreeNodesCount" });
        // Check AvailableNodesCount
        attribute = (Attribute) list.get(0);
        assertEquals("Incorrect value of " + attribute.getName() + " attribute", 0, attribute.getValue());
        // Check FreeNodesCount
        attribute = (Attribute) list.get(1);
        assertEquals("Incorrect value of " + attribute.getName() + " attribute", 0, attribute.getValue());

        RMTHelper.log("Test as user 5 - Check AllAccountsMBean attributes are not accessible");
        final MBeanInfo mBeanInfo = conn.getMBeanInfo(allAccountsMBeanName);
        for (final MBeanAttributeInfo att : mBeanInfo.getAttributes()) {
            final String attName = att.getName();
            try {
                conn.getAttribute(allAccountsMBeanName, attName);
            } catch (Exception e) {
                assertTrue("The attribute " + attName + " must not be accessible with user-level permissions",
                           e instanceof RuntimeException);
            }
        }

        jmxConnector.close();
    }

    private void simultaneousRMIAndROConnections(Credentials adminCreds, JMXServiceURL jmxRmiServiceURL,
            JMXServiceURL jmxRoServiceURL) throws IOException {
        // Test simultaneous RMI and RO connections
        RMTHelper.log("Test simultaneous JMX-RMI and JMX-RO connections as admin");
        final HashMap<String, Object> env = new HashMap<>(1);
        env.put(JMXConnector.CREDENTIALS, new Object[] { TestUsers.TEST.username, adminCreds });
        // Connect to the JMX-RMI Connector Server
        final JMXConnector jmxRmiConnector = JMXConnectorFactory.connect(jmxRmiServiceURL, env);
        final MBeanServerConnection conRmi = jmxRmiConnector.getMBeanServerConnection();

        // Connect to the JMX-RO Connector Server
        env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, JMXProviderUtils.RO_PROVIDER_PKGS);
        final JMXConnector jmxRoConnector1 = JMXConnectorFactory.connect(jmxRoServiceURL, env);
        final MBeanServerConnection conRo = jmxRoConnector1.getMBeanServerConnection();

        assertFalse("In case of simultaneous RMI and RO JMX connections they must not be equal", conRmi.equals(conRo));

        assertFalse("In case of simultaneous RMI and RO JMX connections the connectors must not provide the same " +
                    "connection ids", jmxRmiConnector.getConnectionId().equals(jmxRoConnector1.getConnectionId()));

        RMTHelper.log("Test JMX-RO connection unicity (two connections over RO must not have the same " + "id)");
        final JMXConnector jmxRoConnector2 = JMXConnectorFactory.connect(jmxRoServiceURL, env);
        assertFalse("In case of multiple RO JMX connections the connectors must not provide the same connection " +
                    "ids", jmxRoConnector1.getConnectionId().equals(jmxRoConnector2.getConnectionId()));

        // Close all connectors
        jmxRoConnector2.close();
        jmxRoConnector1.close();
        jmxRmiConnector.close();
    }

    private void jmxClientHelper(RMAuthentication auth, Credentials adminCreds) throws IOException {
        // Test Helper class
        RMTHelper.log("Test JMXClientHelper as admin over RMI with connect() method");
        final JMXClientHelper client = new JMXClientHelper(auth, new Object[] { TestUsers.TEST.username, adminCreds });
        final boolean isConnected1 = client.connect(); // default is over
        // RMI
        assertTrue("Unable to connect, exception is " + client.getLastException(), isConnected1);
        assertTrue("Incorrect default behavior of connect() method it must use RMI protocol",
                   client.getConnector().getConnectionId().startsWith("rmi"));
        client.disconnect();
        assertFalse("The helper disconnect() must set the helper as disconnected", client.isConnected());

        final boolean isConnected2 = client.connect(JMXTransportProtocol.RO);
        assertTrue("Unable to connect, exception is " + client.getLastException(), isConnected2);
        assertTrue("The helper connect(JMXTransportProtocol.RO) method must use RO protocol",
                   client.getConnector().getConnectionId().startsWith("ro"));
        client.disconnect();
        assertFalse("The helper disconnect() must set the helper as disconnected", client.isConnected());
    }
}
