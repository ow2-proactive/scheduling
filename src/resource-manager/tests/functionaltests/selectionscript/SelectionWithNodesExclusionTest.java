/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package functionaltests.selectionscript;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.Assert;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;


/**
 *
 * This class tests RM's mechanism of resource selection with dynamic
 * scripts.
 *
 * - get a node and give back it, then tries to get nodes excluding the first got (test 1)
 *
 * - get nodes with a dummy dynamic selection script that always says "ok", with always same node excluded (test 2)
 *
 * - get nodes with a dummy static selection script that always says "ok", with always same node excluded (test 3)
 *
 * - launch 2 nodes 'special' nodes with a specific java property, that can be selected with a selection script,
 *  and try to get nodes with a selection script that checks this JVM property ,
 *  with putting one of these two 'special nodes in exclusion list, so just one node is provided. ( test 4).
 *
 * - same test as test 4 but with a static script (test 5).
 *
 * - exclude the two special nodes, and get nodes with with the specific java property,
 * no nodes are returned (test 6).
 *
 *
 * @author ProActive team
 *
 */
public class SelectionWithNodesExclusionTest extends FunctionalTest {

    private String vmPropSelectionScriptpath = this.getClass().getResource("vmPropertySelectionScript.js")
            .getPath();

    private String dummySelectionScriptPath = this.getClass().getResource("dummySelectionScript.js")
            .getPath();

    private String vmPropKey = "myProperty";
    private String vmPropValue = "myValue";

    /** Actions to be Perform by this test.
    * The method is called automatically by Junit framework.
    * @throws Exception If the test fails.
    */
    @org.junit.Test
    public void action() throws Exception {

        RMTHelper.log("Deployment");

        RMAdmin admin = RMTHelper.getAdminInterface();
        RMTHelper.createGCMLocalNodeSource();
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, NodeSource.GCM_LOCAL);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        }

        RMTHelper.log("Test 1");

        NodeSet nodeSetWithNodeToExclude = admin.getAtMostNodes(1, null);

        //wait for node selection
        PAFuture.waitFor(nodeSetWithNodeToExclude);

        assertTrue(nodeSetWithNodeToExclude.size() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == RMTHelper.defaultNodesNumber - 1);

        //wait for node busy event
        RMNodeEvent evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);

        admin.freeNodes(nodeSetWithNodeToExclude);

        //wait for node free event
        evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        Assert.assertEquals(evt.getNodeState(), NodeState.FREE);

        assertTrue(admin.getFreeNodesNumber().intValue() == RMTHelper.defaultNodesNumber);

        //get nodes with the previous node excluded
        NodeSet nodes = admin.getAtMostNodes(RMTHelper.defaultNodesNumber, new ArrayList<SelectionScript>(),
                nodeSetWithNodeToExclude);

        //wait for node selection
        PAFuture.waitFor(nodes);

        for (int i = 0; i < RMTHelper.defaultNodesNumber - 1; i++) {
            evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        // booked all nodes minus the node to exclude
        assertTrue(nodes.size() == RMTHelper.defaultNodesNumber - 1);
        //excluded node stays in free state
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

        admin.freeNodes(nodes);

        //wait for nodes freed event
        for (int i = 0; i < RMTHelper.defaultNodesNumber - 1; i++) {
            evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        assertTrue(admin.getFreeNodesNumber().intValue() == RMTHelper.defaultNodesNumber);

        RMTHelper.log("Test 2");

        //create the dynamic selection script object
        SelectionScript dummyDynamicScript = new SelectionScript(new File(dummySelectionScriptPath),
            new String[] {}, true);

        //get nodes with the previous node excluded
        nodes = admin.getAtMostNodes(RMTHelper.defaultNodesNumber, dummyDynamicScript,
                nodeSetWithNodeToExclude);

        //wait for node selection
        PAFuture.waitFor(nodes);

        //wait for nodes busy event
        for (int i = 0; i < RMTHelper.defaultNodesNumber - 1; i++) {
            evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        // booked all nodes minus the node to exclude
        assertTrue(nodes.size() == RMTHelper.defaultNodesNumber - 1);
        //excluded node stays in free state
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

        admin.freeNodes(nodes);

        //wait for nodes freed event
        for (int i = 0; i < RMTHelper.defaultNodesNumber - 1; i++) {
            evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        assertTrue(admin.getFreeNodesNumber().intValue() == RMTHelper.defaultNodesNumber);

        RMTHelper.log("Test 3");

        //create the static selection script object
        SelectionScript dummyStaticScript = new SelectionScript(new File(dummySelectionScriptPath),
            new String[] {}, false);

        //get nodes with the previous node excluded
        nodes = admin.getAtMostNodes(RMTHelper.defaultNodesNumber, dummyStaticScript,
                nodeSetWithNodeToExclude);

        //wait for node selection
        PAFuture.waitFor(nodes);

        //wait for nodes busy event
        for (int i = 0; i < RMTHelper.defaultNodesNumber - 1; i++) {
            evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        }

        // booked all nodes minus the node to exclude
        assertTrue(nodes.size() == RMTHelper.defaultNodesNumber - 1);
        //excluded node stays in free state
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

        admin.freeNodes(nodes);

        //wait for node free event
        for (int i = 0; i < RMTHelper.defaultNodesNumber - 1; i++) {
            evt = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        }

        assertTrue(admin.getFreeNodesNumber().intValue() == RMTHelper.defaultNodesNumber);

        RMTHelper.log("Test 4");

        String node1Name = "node1";
        String node2Name = "node2";

        HashMap<String, String> vmProperties = new HashMap<String, String>();
        vmProperties.put(this.vmPropKey, this.vmPropValue);

        String node1URL = RMTHelper.createNode(node1Name, vmProperties).getNodeInformation().getURL();
        admin.addNode(node1URL, NodeSource.GCM_LOCAL);

        String node2URL = RMTHelper.createNode(node2Name, vmProperties).getNodeInformation().getURL();
        admin.addNode(node2URL, NodeSource.GCM_LOCAL);

        Thread.sleep(5000);
        RMTHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node1URL);
        RMTHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL);

        //wait for nodes added events
        assertTrue(admin.getFreeNodesNumber().intValue() == RMTHelper.defaultNodesNumber + 2);

        //create the dynamic selection script object
        SelectionScript checkPropDynamicSScript = new SelectionScript(new File(vmPropSelectionScriptpath),
            new String[] { this.vmPropKey, this.vmPropValue }, true);
        Node node1ToExclude = NodeFactory.getNode(node1URL);

        nodeSetWithNodeToExclude = new NodeSet();
        nodeSetWithNodeToExclude.add(node1ToExclude);

        //get nodes with the previous node1 excluded
        nodes = admin.getAtMostNodes(RMTHelper.defaultNodesNumber, checkPropDynamicSScript,
                nodeSetWithNodeToExclude);

        //wait for node selection
        PAFuture.waitFor(nodes);

        //wait for nodes busy event
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);

        // booked all nodes minus the node to exclude
        assertTrue(nodes.size() == 1);
        //excluded node stays in free state
        assertTrue(admin.getFreeNodesNumber().intValue() == RMTHelper.defaultNodesNumber + 1);

        //unique node got is node2
        assertTrue(nodes.get(0).getNodeInformation().getURL().equals(node2URL));

        admin.freeNodes(nodes);
        //wait for node free event
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.FREE);

        assertTrue(admin.getFreeNodesNumber().intValue() == RMTHelper.defaultNodesNumber + 2);

        RMTHelper.log("Test 5");

        //create the static selection script object
        SelectionScript checkPropStaticSScript = new SelectionScript(new File(vmPropSelectionScriptpath),
            new String[] { this.vmPropKey, this.vmPropValue }, false);

        Node node2ToExclude = NodeFactory.getNode(node2URL);

        nodeSetWithNodeToExclude = new NodeSet();
        nodeSetWithNodeToExclude.add(node2ToExclude);

        //get nodes with the previous node1 excluded
        nodes = admin.getAtMostNodes(RMTHelper.defaultNodesNumber, checkPropStaticSScript,
                nodeSetWithNodeToExclude);

        //wait for node selection
        PAFuture.waitFor(nodes);

        //wait for nodes busy event
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);

        // booked all nodes minus the node to exclude
        assertTrue(nodes.size() == 1);
        //excluded node stays in free state
        assertTrue(admin.getFreeNodesNumber().intValue() == RMTHelper.defaultNodesNumber + 1);

        //unique node got is node2
        assertTrue(nodes.get(0).getNodeInformation().getURL().equals(node1URL));

        admin.freeNodes(nodes);

        //wait for node free event
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.FREE);

        assertTrue(admin.getFreeNodesNumber().intValue() == RMTHelper.defaultNodesNumber + 2);

        RMTHelper.log("Test 6");

        nodeSetWithNodeToExclude.add(node1ToExclude);

        //get nodes with the previous node1 excluded        
        nodes = admin.getAtMostNodes(RMTHelper.defaultNodesNumber, checkPropStaticSScript,
                nodeSetWithNodeToExclude);

        //wait for node selection
        PAFuture.waitFor(nodes);

        assertTrue(admin.getFreeNodesNumber().intValue() == RMTHelper.defaultNodesNumber + 2);
    }
}
