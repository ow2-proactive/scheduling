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
package functionaltests.rm;

import java.net.URI;
import java.util.ArrayList;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import functionaltests.monitor.RMMonitorEventReceiver;
import functionaltests.monitor.RMMonitorsHandler;
import functionaltests.utils.RMTHelper;
import functionaltests.utils.TestRM;
import functionaltests.utils.TestUsers;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.*;


public class TestRMProxyRebind extends MultipleRMTBase {

    private static final int NODES_NUMBER = 3;

    private Credentials schedulerProxyCredentials;
    private TestRM helper1;
    private TestRM helper2;

    @Before
    public void initCredentials() throws Exception {
        schedulerProxyCredentials = Credentials.getCredentials(PASchedulerProperties
                .getAbsolutePath(PASchedulerProperties.RESOURCE_MANAGER_CREDS.getValueAsString()));
        helper1 = new TestRM();
        helper2 = new TestRM();
    }

    @After
    public void stopRMs() throws Exception {
        helper1.kill();
        helper2.kill();
    }

    @Test
    public void testRMProxyRebind() throws Exception {
        int rmiPort1 = CentralPAPropertyRepository.PA_RMI_PORT.getValue() + 1;
        int jmxPort1 = PAResourceManagerProperties.RM_JMX_PORT.getValueAsInt() + 1;

        int rmiPort2 = CentralPAPropertyRepository.PA_RMI_PORT.getValue() + 2;
        int jmxPort2 = PAResourceManagerProperties.RM_JMX_PORT.getValueAsInt() + 2;

        helper1.start(config1.getAbsolutePath(), rmiPort1,
                PAResourceManagerProperties.RM_JMX_PORT.getCmdLine() + jmxPort1);

        Credentials connectedUserCreds = Credentials.createCredentials(
                new CredData(CredData.parseLogin(TestUsers.DEMO.username), CredData
                        .parseDomain(TestUsers.DEMO.username), TestUsers.DEMO.password), helper1.getAuth()
                        .getPublicKey());

        ResourceManager rm1 = helper1.getAuth().login(connectedUserCreds);
        RMMonitorsHandler monitorsHandler1 = listenEvents(rm1);
        RMTHelper.createNodeSource(NODES_NUMBER, new ArrayList<String>(), rm1, monitorsHandler1);

        helper2.start(config2.getAbsolutePath(), rmiPort2,
                PAResourceManagerProperties.RM_JMX_PORT.getCmdLine() + jmxPort2);

        ResourceManager rm2 = helper2.getAuth().login(connectedUserCreds);

        RMMonitorsHandler monitorsHandler2 = listenEvents(rm2);

        RMTHelper.createNodeSource(NODES_NUMBER, new ArrayList<String>(), rm2, monitorsHandler2);

        checkFreeNodes(rm1, NODES_NUMBER);
        checkFreeNodes(rm2, NODES_NUMBER);

        log("\n Test with per-user connection \n");
        testRebind(new URI(helper1.getUrl()), new URI(helper2.getUrl()), rm1, rm2, monitorsHandler1,
                monitorsHandler2, false);

        log("\n Test with single connection \n");

        // RM1 was killed, restart it
        rmiPort1 = CentralPAPropertyRepository.PA_RMI_PORT.getValue() + 3;
        jmxPort1 = PAResourceManagerProperties.RM_JMX_PORT.getValueAsInt() + 3;

        helper1.start(config1.getAbsolutePath(), rmiPort1,
                PAResourceManagerProperties.RM_JMX_PORT.getCmdLine() + jmxPort1);

        rm1 = helper1.getAuth().login(connectedUserCreds);

        monitorsHandler1 = listenEvents(rm1);

        RMTHelper.createNodeSource(NODES_NUMBER, new ArrayList<String>(), rm1, monitorsHandler1);

        checkFreeNodes(rm1, NODES_NUMBER);
        checkFreeNodes(rm2, NODES_NUMBER);

        testRebind(new URI(helper1.getUrl()), new URI(helper2.getUrl()), rm1, rm2, monitorsHandler1,
                monitorsHandler2, true);
    }

    private RMMonitorsHandler listenEvents(ResourceManager rm)
            throws org.objectweb.proactive.ActiveObjectCreationException,
            org.objectweb.proactive.core.node.NodeException {
        RMMonitorsHandler monitorsHandler1 = new RMMonitorsHandler();
        /** create event receiver then turnActive to avoid deepCopy of MonitorsHandler object
         * 	(shared instance between event receiver and static helpers).
         */
        RMMonitorEventReceiver passiveEventReceiver = new RMMonitorEventReceiver(monitorsHandler1);
        RMMonitorEventReceiver eventReceiver = PAActiveObject.turnActive(passiveEventReceiver);
        PAFuture.waitFor(rm.getMonitoring().addRMEventListener(eventReceiver));
        return monitorsHandler1;
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
        helper1.kill();

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
