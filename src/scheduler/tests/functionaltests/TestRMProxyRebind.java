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
package functionaltests;

import java.net.URI;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.core.rmproxies.PerUserConnectionRMProxiesManager;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxy;
import org.ow2.proactive.scheduler.core.rmproxies.SingleConnectionRMProxiesManager;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;


public class TestRMProxyRebind extends MultipleRMTBase {

    private static final int NODES_NUMBER = 3;

    private Credentials schedulerProxyCredentials;

    @Before
    public void initCredentials() throws Exception {
        schedulerProxyCredentials = Credentials.getCredentials(PASchedulerProperties
                .getAbsolutePath(PASchedulerProperties.RESOURCE_MANAGER_CREDS.getValueAsString()));
    }

    @Test
    public void testRMProxyRebind() throws Exception {
        RMTHelper helper1 = RMTHelper.getDefaultInstance();
        RMTHelper helper2 = new RMTHelper();

        // start two resource managers, they must use different RMI and JMX ports

        int rmiPort1 = CentralPAPropertyRepository.PA_RMI_PORT.getValue() + 1;
        int jmxPort1 = PAResourceManagerProperties.RM_JMX_PORT.getValueAsInt() + 1;

        int rmiPort2 = CentralPAPropertyRepository.PA_RMI_PORT.getValue() + 2;
        int jmxPort2 = PAResourceManagerProperties.RM_JMX_PORT.getValueAsInt() + 2;

        String rmUrl1 = helper1.startRM(config1.getAbsolutePath(), rmiPort1,
                PAResourceManagerProperties.RM_JMX_PORT.getCmdLine() + jmxPort1);
        createNodeSource(helper1, rmiPort1, NODES_NUMBER);

        String rmUrl2 = helper2.startRM(config2.getAbsolutePath(), rmiPort2,
                PAResourceManagerProperties.RM_JMX_PORT.getCmdLine() + jmxPort2);
        createNodeSource(helper2, rmiPort2, NODES_NUMBER);

        checkFreeNodes(helper1.getResourceManager(), NODES_NUMBER);
        checkFreeNodes(helper2.getResourceManager(), NODES_NUMBER);

        System.out.println("\n Test with per-user connection \n");
        testRebind(new URI(rmUrl1), new URI(rmUrl2), helper1, helper2, false);

        System.out.println("\n Test with single connection \n");

        // RM1 was killed, restart it
        rmiPort1 = CentralPAPropertyRepository.PA_RMI_PORT.getValue() + 3;
        jmxPort1 = PAResourceManagerProperties.RM_JMX_PORT.getValueAsInt() + 3;
        rmUrl1 = helper1.startRM(config1.getAbsolutePath(), rmiPort1, PAResourceManagerProperties.RM_JMX_PORT
                .getCmdLine() +
            jmxPort1);
        createNodeSource(helper1, rmiPort1, NODES_NUMBER);

        checkFreeNodes(helper1.getResourceManager(), NODES_NUMBER);
        checkFreeNodes(helper2.getResourceManager(), NODES_NUMBER);

        testRebind(new URI(rmUrl1), new URI(rmUrl2), helper1, helper2, true);
    }

    private void testRebind(URI rmUri1, URI rmUri2, RMTHelper helper1, RMTHelper helper2,
            boolean singleConnection) throws Exception {
        ResourceManager rm1 = helper1.getResourceManager();
        ResourceManager rm2 = helper2.getResourceManager();

        RMProxiesManager proxiesManager;

        if (singleConnection) {
            proxiesManager = new SingleConnectionRMProxiesManager(rmUri1, schedulerProxyCredentials);
        } else {
            proxiesManager = new PerUserConnectionRMProxiesManager(rmUri1, schedulerProxyCredentials);
        }

        Credentials user1Credentials = Credentials.createCredentials(new CredData("admin", "admin"), helper1
                .getRMAuth().getPublicKey());

        RMProxy proxy1 = proxiesManager.getUserRMProxy("admin", user1Credentials);
        System.out.println("Get one node with RM1");
        NodeSet rm1NodeSet1 = proxy1.getNodes(new Criteria(1));
        waitWhenNodeSetAcquired(rm1NodeSet1, 1, helper1);
        checkFreeNodes(rm1, NODES_NUMBER - 1);
        checkFreeNodes(rm2, NODES_NUMBER);

        Assert.assertEquals(NODES_NUMBER - 1, proxiesManager.getRmProxy().getState()
                .getFreeNodesNumber());

        System.out.println("Get one node with RM1");
        NodeSet rm1NodeSet2 = proxy1.getNodes(new Criteria(1));
        waitWhenNodeSetAcquired(rm1NodeSet2, 1, helper1);
        checkFreeNodes(rm1, NODES_NUMBER - 2);
        checkFreeNodes(rm2, NODES_NUMBER);

        Assert.assertEquals(NODES_NUMBER - 2, proxiesManager.getRmProxy().getState()
                .getFreeNodesNumber());
        Assert.assertTrue(proxiesManager.getRmProxy().isActive().getBooleanValue());


        System.out.println("Rebinding to " + rmUri2);
        proxiesManager.rebindRMProxiesManager(rmUri2);

        Assert.assertEquals(NODES_NUMBER, proxiesManager.getRmProxy().getState()
                .getFreeNodesNumber());
        Assert.assertTrue(proxiesManager.getRmProxy().isActive().getBooleanValue());

        waitWhenNodeSetReleased(2, helper1);

        System.out.println("Get one node with RM2");
        NodeSet rm2NodeSet1 = proxy1.getNodes(new Criteria(1));
        waitWhenNodeSetAcquired(rm2NodeSet1, 1, helper2);
        checkFreeNodes(rm1, NODES_NUMBER);
        checkFreeNodes(rm2, NODES_NUMBER - 1);

        Assert.assertEquals(NODES_NUMBER - 1, proxiesManager.getRmProxy().getState()
                .getFreeNodesNumber());

        System.out.println("Get two nodes with RM2");
        NodeSet rm2NodeSet2 = proxy1.getNodes(new Criteria(2));
        waitWhenNodeSetAcquired(rm2NodeSet2, 2, helper2);
        checkFreeNodes(rm1, NODES_NUMBER);
        checkFreeNodes(rm2, NODES_NUMBER - 3);

        Assert.assertEquals(NODES_NUMBER - 3, proxiesManager.getRmProxy().getState()
                .getFreeNodesNumber());

        System.out.println("Release one node with RM2");
        proxy1.releaseNodes(rm2NodeSet1);
        waitWhenNodeSetReleased(1, helper2);
        checkFreeNodes(rm1, NODES_NUMBER);
        checkFreeNodes(rm2, NODES_NUMBER - 2);

        Assert.assertEquals(NODES_NUMBER - 2, proxiesManager.getRmProxy().getState()
                .getFreeNodesNumber());

        System.out.println("Kill RM1");
        helper1.killRM();

        Assert.assertEquals(NODES_NUMBER - 2, proxiesManager.getRmProxy().getState()
                .getFreeNodesNumber());
        Assert.assertTrue(proxiesManager.getRmProxy().isActive().getBooleanValue());

        System.out.println("Release two nodes with RM2");
        proxy1.releaseNodes(rm2NodeSet2);
        waitWhenNodeSetReleased(2, helper2);
        checkFreeNodes(rm2, NODES_NUMBER);

        Assert.assertEquals(NODES_NUMBER, proxiesManager.getRmProxy().getState()
                .getFreeNodesNumber());
        Assert.assertTrue(proxiesManager.getRmProxy().isActive().getBooleanValue());

        System.out.println("Try to release node with terminated RM1");
        proxy1.releaseNodes(rm1NodeSet2);

        System.out.println("Try to release node with terminated RM1 one more time");
        proxy1.releaseNodes(rm1NodeSet2);

        Assert.assertEquals(NODES_NUMBER, proxiesManager.getRmProxy().getState()
                .getFreeNodesNumber());
        Assert.assertTrue(proxiesManager.getRmProxy().isActive().getBooleanValue());

        System.out.println("Terminate all proxies");
        proxiesManager.terminateAllProxies();
    }

    private void waitWhenNodeSetReleased(int nodesNumber, RMTHelper helper) {
        for (int i = 0; i < nodesNumber; i++) {
            helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }
    }

    private void waitWhenNodeSetAcquired(NodeSet nodeSet, int expectedNodesNumber, RMTHelper helper) {
        PAFuture.waitFor(nodeSet);
        Assert.assertEquals("Unexpected nodes number in NodeSet", expectedNodesNumber, nodeSet.size());
        for (int i = 0; i < expectedNodesNumber; i++) {
            helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }
    }

    private void checkFreeNodes(ResourceManager rm, int expectedNumber) {
        Assert.assertEquals("Unexpected number of free nodes", expectedNumber, rm.getState()
                .getFreeNodesNumber());
    }

}
