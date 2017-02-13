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
package functionaltests.jmx.account;

import static org.junit.Assert.assertTrue;

import java.security.PublicKey;
import java.util.HashMap;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.core.account.RMAccountsManager;
import org.ow2.proactive.resourcemanager.core.jmx.RMJMXBeans;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.ManagementMBean;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;

import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.TestUsers;


/**
 * Tests account values for inconsistent GET (not followed by a RELEASE).
 * The scenario is ADD, GET, REMOVE.
 * 
 * This test requires the following prerequisites :
 *  - The value of the {@link PAResourceManagerProperties#RM_ACCOUNT_REFRESH_RATE} property must be
 *  big enough to not let the {@link RMAccountsManager} refresh accounts automatically. This test 
 *  will refresh accounts manually by invoking the {@link ManagementMBean#clearAccoutingCache()}.
 *  - Only one single node must be added
 *  
 * @author The ProActive Team 
 */
public final class AddGetRemoveTest extends RMFunctionalTest {

    /** GET->RELEASE duration time in ms */
    public static long GR_DURATION = 1000;

    @Test
    public void action() throws Exception {

        final ResourceManager rm = rmHelper.getResourceManager();
        // The username and thr password must be the same a used to connect to the RM
        final String adminLogin = TestUsers.TEST.username;
        final String adminPassword = TestUsers.TEST.password;

        // All accounting values are checked through JMX
        final RMAuthentication auth = rmHelper.getRMAuth();
        final PublicKey pubKey = auth.getPublicKey();
        final Credentials adminCreds = Credentials.createCredentials(new CredData(adminLogin, adminPassword), pubKey);

        final JMXServiceURL jmxRmiServiceURL = new JMXServiceURL(auth.getJMXConnectorURL(JMXTransportProtocol.RMI));
        final HashMap<String, Object> env = new HashMap<>(1);
        env.put(JMXConnector.CREDENTIALS, new Object[] { adminLogin, adminCreds });

        // Connect to the JMX RMI Connector Server
        final ObjectName myAccountMBeanName = new ObjectName(RMJMXBeans.MYACCOUNT_MBEAN_NAME);
        final ObjectName managementMBeanName = new ObjectName(RMJMXBeans.MANAGEMENT_MBEAN_NAME);
        final JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxRmiServiceURL, env);
        final MBeanServerConnection conn = jmxConnector.getMBeanServerConnection();

        long usedNodeTime = (Long) conn.getAttribute(myAccountMBeanName, "UsedNodeTime");

        // ADD, GET, RELEASE
        // 1) ADD
        testNode = rmHelper.createNode("test");
        Node node = testNode.getNode();
        final String nodeURL = node.getNodeInformation().getURL();
        rm.addNode(nodeURL).getBooleanValue();
        //we eat the configuring to free
        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, nodeURL);
        rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, nodeURL);

        // 2) GET
        final long beforeGetTime = System.currentTimeMillis();
        rm.getAtMostNodes(1, null).get(0);

        // Sleep a certain amount of time that will be the minimum amount of the GET->RELEASE duration 
        Thread.sleep(GR_DURATION);

        // 3) REMOVE  
        rm.removeNode(nodeURL, true).getBooleanValue();
        final long getRemoveMaxDuration = System.currentTimeMillis() - beforeGetTime;

        // Refresh the account manager
        conn.invoke(managementMBeanName, "clearAccoutingCache", null, null);

        // Check account values validity
        usedNodeTime = (Long) conn.getAttribute(myAccountMBeanName, "UsedNodeTime") - usedNodeTime;
        assertTrue("Invalid value of the usedNodeTime attribute", usedNodeTime >= GR_DURATION);
        assertTrue("Invalid value of the usedNodeTime attribute", usedNodeTime <= getRemoveMaxDuration);
    }
}
