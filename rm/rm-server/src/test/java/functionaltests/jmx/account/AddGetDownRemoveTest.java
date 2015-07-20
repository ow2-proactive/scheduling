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
package functionaltests.jmx.account;

import java.security.PublicKey;
import java.util.HashMap;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

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
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.utils.Criteria;
import org.junit.Assert;
import org.junit.Test;

import functionaltests.RMConsecutive;
import functionaltests.RMTHelper;


/**
 * Tests account values for inconsistent GET (not followed by a RELEASE).
 * The scenario is ADD, GET, DOWN, REMOVE.
 * 
 * This test requires the following prerequisites :
 *  - The value of the {@link PAResourceManagerProperties#RM_ACCOUNT_REFRESH_RATE} property must be
 *  big enough to not let the {@link RMAccountsManager} refresh accounts automatically. This test 
 *  will refresh accounts manually by invoking the {@link ManagementMBean#clearAccoutingCache()}.
 *  - Only one single node must be added
 *  
 * @author The ProActive Team 
 */
public final class AddGetDownRemoveTest extends RMConsecutive {

    /** GET->RELEASE duration time in ms */
    public static long GR_DURATION = 1000;

    @Test
    public void action() throws Exception {

        // The username and thr password must be the same a used to connect to the RM
        final ResourceManager r = rmHelper.getResourceManager();
        // All accounting values are checked through JMX
        final RMAuthentication auth = rmHelper.getRMAuth();
        final PublicKey pubKey = auth.getPublicKey();
        final Credentials adminCreds = Credentials.createCredentials(new CredData(
          RMTHelper.Users.TEST_USERNAME, RMTHelper.Users.TEST_PASSWORD), pubKey);

        final JMXServiceURL jmxRmiServiceURL = new JMXServiceURL(
            auth.getJMXConnectorURL(JMXTransportProtocol.RMI));
        final HashMap<String, Object> env = new HashMap<>(1);
        env.put(JMXConnector.CREDENTIALS, new Object[] { RMTHelper.Users.TEST_USERNAME, adminCreds });

        // Connect to the JMX RMI Connector Server
        final ObjectName myAccountMBeanName = new ObjectName(RMJMXBeans.MYACCOUNT_MBEAN_NAME);
        final ObjectName managementMBeanName = new ObjectName(RMJMXBeans.MANAGEMENT_MBEAN_NAME);
        final JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxRmiServiceURL, env);
        final MBeanServerConnection conn = jmxConnector.getMBeanServerConnection();

        long usedNodeTime = (Long) conn.getAttribute(myAccountMBeanName, "UsedNodeTime");

        // ADD, GET, DOWN, REMOVE
        // 1) ADD
        final String name = "AddGetDownRemoveTest";
        Node node = rmHelper.createNode(name).getNode();
        final String nodeURL = node.getNodeInformation().getURL();
        r.addNode(nodeURL).getBooleanValue();

        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, NodeSource.DEFAULT);
        r.setNodeSourcePingFrequency(5000, NodeSource.DEFAULT);

        // wait for node from configuring to free
        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, nodeURL);
        rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, nodeURL);

        // 2) GET the same node
        final long beforeGetTime = System.currentTimeMillis();
        node = r.getNodes(new Criteria(1)).get(0);

        // Sleep a certain amount of time that will be the minimum amount of the GET->RELEASE duration 
        Thread.sleep(GR_DURATION);

        // 3) Kill the node to ensure that the RM considers it as being DOWN
        try {
            node.getProActiveRuntime().killRT(false);
        } catch (Exception e) {
        }
        while (r.nodeIsAvailable(nodeURL).getBooleanValue()) {
            RMTHelper.log("Node is available " + nodeURL);
            Thread.sleep(100);
        }

        final long getDownMaxDuration = System.currentTimeMillis() - beforeGetTime;

        // 4) REMOVE  
        r.removeNode(nodeURL, true).getBooleanValue();

        // Refresh the account manager
        conn.invoke(managementMBeanName, "clearAccoutingCache", null, null);

        // Check account values validity
        usedNodeTime = (Long) conn.getAttribute(myAccountMBeanName, "UsedNodeTime") - usedNodeTime;
        Assert.assertTrue("Invalid value of the usedNodeTime attribute : " + usedNodeTime +
            " while expected is " + GR_DURATION, (usedNodeTime >= GR_DURATION) &&
            (usedNodeTime <= getDownMaxDuration));
    }
}
