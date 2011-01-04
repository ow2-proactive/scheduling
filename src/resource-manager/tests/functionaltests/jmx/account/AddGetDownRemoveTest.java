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

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.Assert;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.core.account.RMAccountsManager;
import org.ow2.proactive.resourcemanager.core.jmx.RMJMXHelper;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.ManagementMBean;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;


/**
 * Tests account values for inconsistent GET (not followed by a RELEASE).
 * The scenario is ADD, GET, DOWN, REMOVE.
 * 
 * This test requires the following prerequisites :
 *  - The value of the {@link PAResourceManagerProperties.RM_ACCOUNT_REFRESH_RATE} property must be 
 *  big enough to not let the {@link RMAccountsManager} refresh accounts automatically. This test 
 *  will refresh accounts manually by invoking the {@link ManagementMBean#refreshAllAccounts()}.
 *  - Only one single node must be added
 *  
 * @author The ProActive Team 
 */
public final class AddGetDownRemoveTest extends FunctionalTest {

    /** GET->RELEASE duration time in ms */
    public static long GR_DURATION = 1000;

    /**
     * Test function.
     * @throws Exception
     */
    @org.junit.Test
    public void action() throws Exception {
        final ResourceManager r = RMTHelper.getResourceManager();

        // The username and thr password must be the same a used to connect to the RM
        final String adminLogin = RMTHelper.username;
        final String adminPassword = RMTHelper.password;

        // All accounting values are checked through JMX
        final RMAuthentication auth = (RMAuthentication) RMTHelper.getRMAuth();
        final PublicKey pubKey = auth.getPublicKey();
        final Credentials adminCreds = Credentials.createCredentials(new CredData(adminLogin, adminPassword),
                pubKey);

        final JMXServiceURL jmxRmiServiceURL = new JMXServiceURL(auth
                .getJMXConnectorURL(JMXTransportProtocol.RMI));
        final HashMap<String, Object> env = new HashMap<String, Object>(1);
        env.put(JMXConnector.CREDENTIALS, new Object[] { adminLogin, adminCreds });

        // Connect to the JMX RMI Connector Server
        final ObjectName myAccountMBeanName = new ObjectName(RMJMXHelper.MYACCOUNT_MBEAN_NAME);
        final ObjectName managementMBeanName = new ObjectName(RMJMXHelper.MANAGEMENT_MBEAN_NAME);
        final JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxRmiServiceURL, env);
        final MBeanServerConnection conn = jmxConnector.getMBeanServerConnection();

        // Tests on database
        //(nodeprovider=demo)                      

        // Ensure that no refreshes was done and all account values are correctly initialized        
        AttributeList atts = conn.getAttributes(myAccountMBeanName, new String[] { "UsedNodeTime",
                "ProvidedNodeTime", "ProvidedNodesCount" });
        long usedNodeTime = (Long) ((Attribute) atts.get(0)).getValue();
        long providedNodeTime = (Long) ((Attribute) atts.get(1)).getValue();
        int providedNodesCount = (Integer) ((Attribute) atts.get(2)).getValue();

        Assert
                .assertTrue(
                        "The accounts must not be refreshed automatically therefore the LastRefreshDurationInMilliseconds must be 0",
                        (Long) conn.getAttribute(managementMBeanName, "LastRefreshDurationInMilliseconds") == 0l);
        Assert.assertTrue("The usedNodeTime attribute must be 0", usedNodeTime == 0);
        Assert.assertTrue("The providedNodeTime attribute must be 0", providedNodeTime == 0);
        Assert.assertTrue("The providedNodesCount attribute must be 0", providedNodesCount == 0);

        // ADD, GET, DOWN, REMOVE
        // 1) ADD
        final String name = "test";
        Node node = RMTHelper.createNode(name);
        final String nodeURL = node.getNodeInformation().getURL();
        r.addNode(nodeURL).getBooleanValue();

        // wait for node from configuring to free
        RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // 2) GET
        final long beforeGetTime = System.currentTimeMillis();
        node = r.getAtMostNodes(1, null).get(0);

        // Sleep a certain amount of time that will be the minimum amount of the GET->RELEASE duration 
        Thread.sleep(GR_DURATION);

        // 3) Kill the node to ensure that the RM considers it as being DOWN
        node.getProActiveRuntime().killNode(name);
        while (r.nodeIsAvailable(nodeURL).getBooleanValue()) {
            Thread.sleep(500);
        }

        final long getDownMaxDuration = System.currentTimeMillis() - beforeGetTime;

        // 4) REMOVE  
        r.removeNode(nodeURL, true).getBooleanValue();

        // Refresh the account manager
        conn.invoke(managementMBeanName, "refreshAllAccounts", null, null);

        // Wait until the refresh is done 
        long refreshDuration;
        do {
            Thread.sleep(500);
            refreshDuration = (Long) conn.getAttribute(managementMBeanName,
                    "LastRefreshDurationInMilliseconds");
        } while (refreshDuration <= 0);

        // Check account values validity
        usedNodeTime = (Long) conn.getAttribute(myAccountMBeanName, "UsedNodeTime");
        Assert.assertTrue("Invalid value of the usedNodeTime attribute : " + usedNodeTime +
            " while expected is " + GR_DURATION, (usedNodeTime >= GR_DURATION) &&
            (usedNodeTime <= getDownMaxDuration));
    }
}
