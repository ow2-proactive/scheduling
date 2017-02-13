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

import java.security.PublicKey;
import java.util.HashMap;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.Assert;
import org.junit.Ignore;
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
 * Tests account values for inconsistent ADD and GET (not followed by a RELEASE and REMOVE).
 * The scenario is ADD, GET.
 * 
 * This test requires the following prerequisites :
 *  - The value of the {@link PAResourceManagerProperties.RM_ACCOUNT_REFRESH_RATE} property must be 
 *  big enough to not let the {@link RMAccountsManager} refresh accounts automatically. This test 
 *  will refresh accounts manually by invoking the {@link ManagementMBean#clearAccoutingCache()}.
 *  - Only one single node must be added
 *  
 * @author The ProActive Team 
 */
@Ignore
public final class AddGetTest extends RMFunctionalTest {

    /** GET->RELEASE duration time in ms */
    public static long GR_DURATION = 1000;

    /**
     * Test function.
     * @throws Exception
     */
    @org.junit.Test
    public void action() throws Exception {

        final ResourceManager rm = rmHelper.getResourceManager();
        // The username and thr password must be the same a used to connect to the RM
        final String adminLogin = TestUsers.TEST.username;
        final String adminPassword = TestUsers.TEST.password;

        // All accounting values are checked through JMX
        final RMAuthentication auth = (RMAuthentication) rmHelper.getRMAuth();
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

        // Tests on database
        //(nodeprovider=demo)                      

        // Ensure that no refreshes was done and all account values are correctly initialized        
        AttributeList atts = conn.getAttributes(myAccountMBeanName,
                                                new String[] { "UsedNodeTime", "ProvidedNodeTime",
                                                               "ProvidedNodesCount" });
        long usedNodeTime = (Long) ((Attribute) atts.get(0)).getValue();
        long providedNodeTime = (Long) ((Attribute) atts.get(1)).getValue();
        int providedNodesCount = (Integer) ((Attribute) atts.get(2)).getValue();

        Assert.assertEquals("The accounts must not be refreshed automatically therefore the LastRefreshDurationInMilliseconds must be 0",
                            (Long) 0l,
                            (Long) conn.getAttribute(managementMBeanName, "LastRefreshDurationInMilliseconds"));
        Assert.assertTrue("The usedNodeTime attribute must be 0", usedNodeTime == 0);
        Assert.assertTrue("The providedNodeTime attribute must be 0", providedNodeTime == 0);
        Assert.assertTrue("The providedNodesCount attribute must be 0", providedNodesCount == 0);

        // ADD, GET
        // 1) ADD
        final long beforeAddTime = System.currentTimeMillis();
        testNode = rmHelper.createNode("test");
        Node node = testNode.getNode();
        final String nodeURL = node.getNodeInformation().getURL();
        rm.addNode(nodeURL).getBooleanValue();
        //we eat the configuring to free
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // 2) GET
        final long beforeGetTime = System.currentTimeMillis();
        node = rm.getAtMostNodes(1, null).get(0);

        // Sleep a certain amount of time that will be the minimum amount of the GET duration 
        Thread.sleep(GR_DURATION);

        // Refresh the account manager
        conn.invoke(managementMBeanName, "clearAccoutingCache", null, null);

        final long currentTime = System.currentTimeMillis();
        final long addRefreshMaxDuration = currentTime - beforeAddTime;
        final long getRefreshMaxDuration = currentTime - beforeGetTime;

        // Check account values validity                      
        atts = conn.getAttributes(myAccountMBeanName,
                                  new String[] { "UsedNodeTime", "ProvidedNodeTime", "ProvidedNodesCount" });
        usedNodeTime = (Long) ((Attribute) atts.get(0)).getValue();
        providedNodeTime = (Long) ((Attribute) atts.get(1)).getValue();
        providedNodesCount = (Integer) ((Attribute) atts.get(2)).getValue();

        Assert.assertTrue("Invalid value of the usedNodeTime attribute",
                          (usedNodeTime >= GR_DURATION) && (usedNodeTime <= addRefreshMaxDuration));
        Assert.assertTrue("Invalid value of the providedNodeTime attribute",
                          (providedNodeTime >= usedNodeTime) && (providedNodeTime <= getRefreshMaxDuration));
        Assert.assertTrue("Invalid value of the providedNodesCount attribute", (providedNodesCount == 1));
    }
}
