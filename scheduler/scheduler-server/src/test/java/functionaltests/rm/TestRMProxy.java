/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package functionaltests.rm;

import functionaltests.utils.RMTHelper;
import functionaltests.utils.SchedulerTHelper;
import functionaltests.utils.TestScheduler;
import org.junit.*;
import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.core.rmproxies.PerUserConnectionRMProxiesManager;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxy;
import org.ow2.proactive.scheduler.core.rmproxies.SingleConnectionRMProxiesManager;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;
import org.ow2.tests.ProActiveTest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.*;


public class TestRMProxy extends ProActiveTest {

    static final int NODES_NUMBER = 3;

    private static Credentials user1Credentials;

    private static Credentials user2Credentials;

    private RMProxiesManager proxiesManager;

    static String nsName = "test";

    private static RMTHelper rmHelper;

    @BeforeClass
    public static void setUp() throws Exception {
        if (TestScheduler.isStarted()) {
            SchedulerTHelper.log("Killing previous scheduler.");
            TestScheduler.kill();
        }
        rmHelper = new RMTHelper();
        rmHelper.getResourceManager();

        user1Credentials = Credentials.createCredentials(new CredData("admin", "admin"), rmHelper.getRMAuth()
                .getPublicKey());

        user2Credentials = Credentials.createCredentials(new CredData("demo", "demo"), rmHelper.getRMAuth()
                .getPublicKey());

        rmHelper.createNodeSource(nsName, NODES_NUMBER);
    }


    @Test
    public void testProxiesManagerPerUser() throws Exception {
        log("\n Test with per-user connection \n");
        testRMProxies(false);
    }

    @Test
    public void testProxiesManagerSingle() throws Exception {
        log("\n Test with single connection \n");
        testRMProxies(true);
    }

    @After
    public void terminateProxies() {
        log("Terminate all proxies");
        if (proxiesManager != null) {
            try {
                proxiesManager.terminateAllProxies();
            } catch (Exception ignored) {

            }
        }
    }

    @AfterClass
    public static void deleteNS() throws Exception {
        try {
            rmHelper.removeNodeSource(nsName);
            rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nsName);
        } catch (Exception ignored) {

        }
        rmHelper.shutdownRM();
    }


    private void testRMProxies(boolean singleUserConnection) throws Exception {
        ResourceManager rm = rmHelper.getResourceManager();

        assertEquals(NODES_NUMBER, rm.getState().getFreeNodesNumber());

        URI rmUri = new URI(RMTHelper.getLocalUrl());
        Credentials schedulerProxyCredentials = Credentials.getCredentials(PASchedulerProperties
                .getAbsolutePath(PASchedulerProperties.RESOURCE_MANAGER_CREDS.getValueAsString()));

        if (singleUserConnection) {
            proxiesManager = new SingleConnectionRMProxiesManager(rmUri, schedulerProxyCredentials);
        } else {
            proxiesManager = new PerUserConnectionRMProxiesManager(rmUri, schedulerProxyCredentials);
        }

        RMProxy user1RMProxy = proxiesManager.getUserRMProxy("admin", user1Credentials);
        assertSame("Proxy manager should return cached proxy instance", user1RMProxy,
                proxiesManager.getUserRMProxy("admin", user1Credentials));

        RMProxy user2RMProxy = proxiesManager.getUserRMProxy("demo", user2Credentials);
        assertSame("Proxy manager should return cached proxy instance", user2RMProxy,
                proxiesManager.getUserRMProxy("demo", user2Credentials));

        requestReleaseOneNode(user1RMProxy, rm);

        testSplitNodeSet(user1RMProxy, rm);

        checkSchedulerProxy(proxiesManager);

        requestWithExtraNodes(user1RMProxy, rm);

        requestTooManyNodes(user1RMProxy, rm);

        requestReleaseAllNodes(user1RMProxy, rm);

        checkSchedulerProxy(proxiesManager);

        requestReleaseOneNode(user2RMProxy, rm);

        requestReleaseAllNodes(user2RMProxy, rm);

        requestWithTwoUsers(user1RMProxy, user2RMProxy, rm);

        checkSchedulerProxy(proxiesManager);

        log("Terminate user proxy1");
        proxiesManager.terminateRMProxy("admin");
        user1RMProxy = proxiesManager.getUserRMProxy("admin", user1Credentials);
        requestReleaseAllNodes(user1RMProxy, rm);

        log("Terminate user proxy2");
        proxiesManager.terminateRMProxy("demo");
        user2RMProxy = proxiesManager.getUserRMProxy("demo", user2Credentials);
        requestReleaseAllNodes(user2RMProxy, rm);

    }

    private void requestWithTwoUsers(RMProxy proxy1, RMProxy proxy2, ResourceManager rm) throws Exception {
        log("Request nodes for two users");

        NodeSet nodeSet1 = proxy1.getNodes(new Criteria(1));
        NodeSet nodeSet2 = proxy2.getNodes(new Criteria(2));
        waitWhenNodeSetAcquired(nodeSet1, 1);
        waitWhenNodeSetAcquired(nodeSet2, 2);

        assertEquals(NODES_NUMBER - 3, rm.getState().getFreeNodesNumber());

        proxy1.releaseNodes(nodeSet1);
        proxy2.releaseNodes(nodeSet2);
        waitWhenNodesAreReleased(3);

        assertEquals(NODES_NUMBER, rm.getState().getFreeNodesNumber());

    }

    private void checkSchedulerProxy(RMProxiesManager proxiesManager) {
        log("Check scheduler proxy");

        RMProxy proxy = proxiesManager.getRmProxy();
        assertEquals(proxy.getState().getFreeNodesNumber(), NODES_NUMBER);
        assertTrue(proxy.isActive().getBooleanValue());
    }

    private void requestTooManyNodes(RMProxy proxy, ResourceManager rm) throws Exception {
        log("Request more nodes than RM has");

        Criteria criteria = new Criteria(NODES_NUMBER + 1);
        criteria.setBestEffort(false);
        NodeSet nodeSet = proxy.getNodes(criteria);
        PAFuture.waitFor(nodeSet);
        assertEquals(0, nodeSet.size());
        assertEquals(NODES_NUMBER, rm.getState().getFreeNodesNumber());
    }

    private void requestWithExtraNodes(RMProxy proxy, ResourceManager rm) throws Exception {
        log("Request NodeSet with extra nodes");

        TopologyDescriptor topology = TopologyDescriptor.SINGLE_HOST_EXCLUSIVE;
        Criteria criteria = new Criteria(1);
        criteria.setTopology(topology);
        NodeSet nodeSet = proxy.getNodes(criteria);
        PAFuture.waitFor(nodeSet);
        assertEquals(1, nodeSet.size());
        Assert.assertNotNull("Extra nodes are expected", nodeSet.getExtraNodes());
        assertEquals(NODES_NUMBER - 1, nodeSet.getExtraNodes().size());
        assertEquals(0, rm.getState().getFreeNodesNumber());

        proxy.releaseNodes(nodeSet);
        waitWhenNodesAreReleased(NODES_NUMBER);
        assertEquals(NODES_NUMBER, rm.getState().getFreeNodesNumber());
    }

    private void requestReleaseAllNodes(RMProxy proxy, ResourceManager rm) throws Exception {
        log("Request and release all nodes");

        List<NodeSet> nodeSets = new ArrayList<>();
        for (int i = 0; i < NODES_NUMBER; i++) {
            nodeSets.add(proxy.getNodes(new Criteria(1)));
        }

        for (NodeSet nodeSet : nodeSets) {
            waitWhenNodeSetAcquired(nodeSet, 1);
            proxy.releaseNodes(nodeSet);
        }

        waitWhenNodesAreReleased(NODES_NUMBER);

        assertEquals(NODES_NUMBER, rm.getState().getFreeNodesNumber());
    }

    private void requestReleaseOneNode(RMProxy proxy, ResourceManager rm) throws Exception {
        log("Request and release single node");

        NodeSet nodeSet = proxy.getNodes(new Criteria(1));
        waitWhenNodeSetAcquired(nodeSet, 1);

        proxy.releaseNodes(nodeSet);
        waitWhenNodesAreReleased(1);

        assertEquals(NODES_NUMBER, rm.getState().getFreeNodesNumber());
    }

    private void testSplitNodeSet(RMProxy proxy, ResourceManager rm) throws Exception {
        log("Request as single NodeSet, release it as two NodeSets");

        NodeSet nodeSet = proxy.getNodes(new Criteria(3));
        waitWhenNodeSetAcquired(nodeSet, 3);
        assertEquals(NODES_NUMBER - 3, rm.getState().getFreeNodesNumber());

        NodeSet nodeSet1 = new NodeSet();
        nodeSet1.add(nodeSet.remove(0));

        NodeSet nodeSet2 = new NodeSet();
        nodeSet2.add(nodeSet.remove(0));
        nodeSet2.add(nodeSet.remove(0));

        proxy.releaseNodes(nodeSet1);
        waitWhenNodesAreReleased(1);
        assertEquals(NODES_NUMBER - 2, rm.getState().getFreeNodesNumber());

        proxy.releaseNodes(nodeSet2);
        waitWhenNodesAreReleased(2);
        assertEquals(NODES_NUMBER, rm.getState().getFreeNodesNumber());
    }

    private void waitWhenNodesAreReleased(int nodesNumber) throws Exception {
        for (int i = 0; i < nodesNumber; i++) {
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }
    }

    private void waitWhenNodeSetAcquired(NodeSet nodeSet, int expectedNodesNumber) throws Exception {
        PAFuture.waitFor(nodeSet);
        assertEquals("Unexpected nodes number in NodeSet", expectedNodesNumber, nodeSet.size());
        for (int i = 0; i < expectedNodesNumber; i++) {
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }
    }
}
