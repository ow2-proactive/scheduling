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

import functionaltests.monitor.RMMonitorEventReceiver;
import functionaltests.monitor.RMMonitorsHandler;
import functionaltests.utils.*;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
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

import java.net.URI;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TestRMProxyRebind extends MultipleRMTBase {

    private static final int NODES_NUMBER = 3;

    private Credentials schedulerProxyCredentials;
    private TestRM helper1;
    private TestRM helper2;
    private int pnpPort1;
    private int jmxPort1;
    private int pnpPort2;
    private int jmxPort2;
    private ResourceManager rm1;
    private ResourceManager rm2;
    private List<RMMonitorEventReceiver> eventReceivers = new ArrayList<>();
    private RMMonitorsHandler monitorsHandler1;
    private RMMonitorsHandler monitorsHandler2;
    private List<TestNode> testNodes = new ArrayList<>();

    @BeforeClass
    public static void setUp() throws Exception {
        if (TestScheduler.isStarted()) {
            SchedulerTHelper.log("Killing previous scheduler.");
            TestScheduler.kill();
        }
        initConfigs();
    }

    @Before
    public void createRMs() throws Exception {
        schedulerProxyCredentials = Credentials.getCredentials(PASchedulerProperties
                .getAbsolutePath(PASchedulerProperties.RESOURCE_MANAGER_CREDS.getValueAsString()));
        helper1 = new TestRM();
        helper2 = new TestRM();

        pnpPort1 = CentralPAPropertyRepository.PA_RMI_PORT.getValue() + 1;
        jmxPort1 = PAResourceManagerProperties.RM_JMX_PORT.getValueAsInt() + 1;

        pnpPort2 = CentralPAPropertyRepository.PA_RMI_PORT.getValue() + 2;
        jmxPort2 = PAResourceManagerProperties.RM_JMX_PORT.getValueAsInt() + 2;

        helper1.start(config1.getAbsolutePath(), pnpPort1,
                PAResourceManagerProperties.RM_JMX_PORT.getCmdLine() + jmxPort1);

        Credentials connectedUserCreds = Credentials.createCredentials(
                new CredData(CredData.parseLogin(TestUsers.DEMO.username), CredData
                        .parseDomain(TestUsers.DEMO.username), TestUsers.DEMO.password), helper1.getAuth()
                        .getPublicKey());

        Map.Entry<RMMonitorsHandler, RMMonitorEventReceiver> entry1 = connectToRM(helper1.getUrl(), connectedUserCreds);

        monitorsHandler1 = entry1.getKey();
        rm1 = entry1.getValue();

        testNodes.addAll(RMTHelper.addNodesToDefaultNodeSource(NODES_NUMBER, new ArrayList<String>(), rm1, monitorsHandler1));

        helper2.start(config2.getAbsolutePath(), pnpPort2,
                PAResourceManagerProperties.RM_JMX_PORT.getCmdLine() + jmxPort2);

        Map.Entry<RMMonitorsHandler, RMMonitorEventReceiver> entry2 = connectToRM(helper2.getUrl(), connectedUserCreds);

        monitorsHandler2 = entry2.getKey();
        rm2 = entry2.getValue();

        testNodes.addAll(RMTHelper.addNodesToDefaultNodeSource(NODES_NUMBER, new ArrayList<String>(), rm2, monitorsHandler2));

        checkFreeNodes(rm1, NODES_NUMBER);
        checkFreeNodes(rm2, NODES_NUMBER);
    }

    @After
    public void stopRMs() throws Exception {
        if (rm1 != null) {
            PAFuture.waitFor(rm1.shutdown(true));
        }
        if (rm2 != null) {
            PAFuture.waitFor(rm2.shutdown(true));
        }
        helper1.kill();
        helper2.kill();

        for (TestNode node : testNodes) {
            try {
                node.kill();
            } catch (Exception ignored) {

            }
        }

        for (RMMonitorEventReceiver receiver : eventReceivers) {
            try {
                PAActiveObject.terminateActiveObject(receiver, true);
            } catch (Exception ignored) {

            }
        }
    }

    @Test
    public void testRMProxyRebindSingle() throws Exception {
        log("\n Test with single connection \n");
        testRebind(new URI(helper1.getUrl()), new URI(helper2.getUrl()), rm1, rm2, monitorsHandler1,
                monitorsHandler2, true);
    }

    @Test
    public void testRMProxyRebindPerUser() throws Exception {
        log("\n Test with per-user connection \n");
        testRebind(new URI(helper1.getUrl()), new URI(helper2.getUrl()), rm1, rm2, monitorsHandler1,
                monitorsHandler2, false);
    }


    private Map.Entry<RMMonitorsHandler, RMMonitorEventReceiver> connectToRM(String rmUrl, Credentials creds)
            throws Exception {
        RMMonitorsHandler monitorsHandler1 = new RMMonitorsHandler();
        /** create event receiver then turnActive to avoid deepCopy of MonitorsHandler object
         * 	(shared instance between event receiver and static helpers).
         */
        RMMonitorEventReceiver passiveEventReceiver = new RMMonitorEventReceiver(monitorsHandler1);
        RMMonitorEventReceiver receiver = PAActiveObject.turnActive(passiveEventReceiver);
        eventReceivers.add(receiver);
        receiver.init(rmUrl, creds);
        return new AbstractMap.SimpleImmutableEntry<RMMonitorsHandler, RMMonitorEventReceiver>(monitorsHandler1, receiver);
    }

    private void testRebind(URI rmUri1, URI rmUri2, ResourceManager rm1, ResourceManager rm2,
            RMMonitorsHandler monitorsHandler1, RMMonitorsHandler monitorsHandler2, boolean singleConnection)
            throws Exception {

        RMProxiesManager proxiesManager;

        if (singleConnection) {
            proxiesManager = new SingleConnectionRMProxiesManager(rmUri1, schedulerProxyCredentials);
        } else {
            proxiesManager = new PerUserConnectionRMProxiesManager(rmUri1, schedulerProxyCredentials);
        }

        Credentials user1Credentials = Credentials.createCredentials(new CredData("admin", "admin"), helper1
                .getAuth().getPublicKey());

        RMProxy proxy1 = proxiesManager.getUserRMProxy("admin", user1Credentials);
        log("Get one node with RM1");
        NodeSet rm1NodeSet1 = proxy1.getNodes(new Criteria(1));
        waitWhenNodeSetAcquired(rm1NodeSet1, 1, monitorsHandler1);
        checkFreeNodes(rm1, NODES_NUMBER - 1);
        checkFreeNodes(rm2, NODES_NUMBER);

        assertEquals(NODES_NUMBER - 1, proxiesManager.getRmProxy().getState().getFreeNodesNumber());

        log("Get one node with RM1");
        NodeSet rm1NodeSet2 = proxy1.getNodes(new Criteria(1));
        waitWhenNodeSetAcquired(rm1NodeSet2, 1, monitorsHandler1);
        checkFreeNodes(rm1, NODES_NUMBER - 2);
        checkFreeNodes(rm2, NODES_NUMBER);

        assertEquals(NODES_NUMBER - 2, proxiesManager.getRmProxy().getState().getFreeNodesNumber());
        assertTrue(proxiesManager.getRmProxy().isActive().getBooleanValue());

        log("Rebinding to " + rmUri2);
        proxiesManager.rebindRMProxiesManager(rmUri2);

        assertEquals(NODES_NUMBER, proxiesManager.getRmProxy().getState().getFreeNodesNumber());
        assertTrue(proxiesManager.getRmProxy().isActive().getBooleanValue());

        waitWhenNodeSetReleased(2, monitorsHandler1);

        log("Get one node with RM2");
        NodeSet rm2NodeSet1 = proxy1.getNodes(new Criteria(1));
        waitWhenNodeSetAcquired(rm2NodeSet1, 1, monitorsHandler2);
        checkFreeNodes(rm1, NODES_NUMBER);
        checkFreeNodes(rm2, NODES_NUMBER - 1);

        assertEquals(NODES_NUMBER - 1, proxiesManager.getRmProxy().getState().getFreeNodesNumber());

        log("Get two nodes with RM2");
        NodeSet rm2NodeSet2 = proxy1.getNodes(new Criteria(2));
        waitWhenNodeSetAcquired(rm2NodeSet2, 2, monitorsHandler2);
        checkFreeNodes(rm1, NODES_NUMBER);
        checkFreeNodes(rm2, NODES_NUMBER - 3);

        assertEquals(NODES_NUMBER - 3, proxiesManager.getRmProxy().getState().getFreeNodesNumber());

        log("Release one node with RM2");
        proxy1.releaseNodes(rm2NodeSet1);
        waitWhenNodeSetReleased(1, monitorsHandler2);
        checkFreeNodes(rm1, NODES_NUMBER);
        checkFreeNodes(rm2, NODES_NUMBER - 2);

        assertEquals(NODES_NUMBER - 2, proxiesManager.getRmProxy().getState().getFreeNodesNumber());

        log("Kill RM1");
        PAFuture.waitFor(rm1.shutdown(true));
        helper1.kill();
        rm1 = null;

        assertEquals(NODES_NUMBER - 2, proxiesManager.getRmProxy().getState().getFreeNodesNumber());
        assertTrue(proxiesManager.getRmProxy().isActive().getBooleanValue());

        log("Release two nodes with RM2");
        proxy1.releaseNodes(rm2NodeSet2);
        waitWhenNodeSetReleased(2, monitorsHandler2);
        checkFreeNodes(rm2, NODES_NUMBER);

        assertEquals(NODES_NUMBER, proxiesManager.getRmProxy().getState().getFreeNodesNumber());
        assertTrue(proxiesManager.getRmProxy().isActive().getBooleanValue());

        log("Try to release node with terminated RM1");
        proxy1.releaseNodes(rm1NodeSet2);

        log("Try to release node with terminated RM1 one more time");
        proxy1.releaseNodes(rm1NodeSet2);

        assertEquals(NODES_NUMBER, proxiesManager.getRmProxy().getState().getFreeNodesNumber());
        assertTrue(proxiesManager.getRmProxy().isActive().getBooleanValue());

        log("Terminate all proxies");
        proxiesManager.terminateAllProxies();
    }

    private void waitWhenNodeSetReleased(int nodesNumber, RMMonitorsHandler monitor) {
        for (int i = 0; i < nodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED, monitor);
        }
    }

    private void waitWhenNodeSetAcquired(NodeSet nodeSet, int expectedNodesNumber, RMMonitorsHandler monitor) {
        PAFuture.waitFor(nodeSet);
        assertEquals("Unexpected nodes number in NodeSet", expectedNodesNumber, nodeSet.size());
        for (int i = 0; i < expectedNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED, monitor);
        }
    }

    private void checkFreeNodes(ResourceManager rm, int expectedNumber) {
        assertEquals("Unexpected number of free nodes", expectedNumber, rm.getState().getFreeNodesNumber());
    }

}
