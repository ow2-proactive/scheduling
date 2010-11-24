/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.nodestate;

import static junit.framework.Assert.assertTrue;
import junit.framework.Assert;

import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.NodeSet;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;


/**
 *
 * @author ProActive team
 *
 */
public class TestConcurrentUsers extends FunctionalTest {

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        ResourceManager resourceManager = RMTHelper.connect(RMTHelper.username, RMTHelper.username);

        String hostName = ProActiveInet.getInstance().getHostname();
        String node1Name = "node1";
        String node1URL = "//" + hostName + "/" + node1Name;
        RMTHelper.createNode(node1Name);
        resourceManager.createNodeSource(NodeSource.DEFAULT, DefaultInfrastructureManager.class.getName(),
                null, StaticPolicy.class.getName(), null);
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, NodeSource.DEFAULT);
        resourceManager.addNode(node1URL, NodeSource.DEFAULT);

        // waiting for node adding event
        RMTHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node1URL);
        // waiting for the node to be free
        RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        assertTrue(resourceManager.getState().getTotalNodesNumber() == 1);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 1);

        RMTHelper.log("Test 1 - releasing of the foreign node");
        // acquiring a node
        final NodeSet ns = resourceManager.getAtMostNodes(1, null);

        // waiting for node busy event
        RMNodeEvent evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        assertTrue(ns.size() == 1);
        assertTrue(resourceManager.getState().getTotalNodesNumber() == 1);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 0);

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    ResourceManager rm2 = RMTHelper.connect("user", "pwd");
                    rm2.releaseNode(ns.get(0)).getBooleanValue();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        };
        t.start();
        t.join();

        Assert.assertEquals(1, resourceManager.getState().getTotalNodesNumber());
        Assert.assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        resourceManager.releaseNodes(ns);
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.FREE);

        assertTrue(resourceManager.getState().getTotalNodesNumber() == 1);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 1);

        RMTHelper.log("Test 2 - releasing node twice");
        resourceManager.releaseNodes(ns);

        // to make sure everything has been processed
        Thread.sleep(1000);
        assertTrue(resourceManager.getState().getTotalNodesNumber() == 1);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 1);

        RMTHelper.log("Test 3 - client crash detection");
        JVMProcessImpl nodeProcess = new JVMProcessImpl(
            new org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger());
        nodeProcess.setClassname("functionaltests.nodestate.GetAllNodes");
        nodeProcess.startProcess();

        // node busy event
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        assertTrue(resourceManager.getState().getTotalNodesNumber() == 1);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 0);

        // client does not exist anymore
        RMTHelper.log("Client does not exist anymore. Waiting for client crash detection.");
        // it should be detected by RM
        // waiting for node free event
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        assertTrue(resourceManager.getState().getTotalNodesNumber() == 1);
        assertTrue(resourceManager.getState().getFreeNodesNumber() == 1);

    }
}
