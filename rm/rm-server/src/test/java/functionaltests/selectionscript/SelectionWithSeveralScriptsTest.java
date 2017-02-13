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
package functionaltests.selectionscript;

import static functionaltests.utils.RMTHelper.log;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.TestNode;


/**
 *
 * This class tests RM's mechanism of resource selection with static
 * scripts.
 *
 * -launch nodes with a specified JVM environment variable => verify script
 * -launch 5 nodes without this specified JVM environment variable => not verify script
 *
 *  1/ ask 1 node with a selection that check this environment variable,
 *  and verify that a node can be provided by RM
 *  2/ ask 3 nodes with specific environment var, and check that just one node can be provided
 *  3/ add a second node with specific JVM env var and ask 3 nodes with specific environment var,
 *   and check that two nodes can be provided with one getAtMostNodes method call.
 *  4/ remove the node with specified environment var, end check that no node
 *  can be provided
 *  5/ ask a node with a selection script that provides execution error,
 *  and check that error handling is correct.
 *  6/ ask a node with a selection script that doesn't return
 *  the 'selected' return value
 *
 * @author ProActive team
 *
 */
public class SelectionWithSeveralScriptsTest extends RMFunctionalTest {

    private URL vmPropSelectionScriptpath = this.getClass().getResource("vmPropertySelectionScript.groovy");

    @Test
    public void action() throws Exception {
        ResourceManager resourceManager = rmHelper.getResourceManager();

        String nodeSourceName = "selection-several-ns";
        resourceManager.createNodeSource(nodeSourceName,
                                         DefaultInfrastructureManager.class.getName(),
                                         null,
                                         StaticPolicy.class.getName(),
                                         new Object[] { "ALL", "ALL" })
                       .getBooleanValue();
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nodeSourceName);

        String node1Name = "node-sel-1";
        String node2Name = "node-sel-2";
        String node3Name = "node-sel-3";
        //---------------------------------------------------
        //create a first node with the two VM properties
        //---------------------------------------------------

        HashMap<String, String> vmTwoProperties = new HashMap<>();
        String vmPropKey1 = "myProperty1";
        String vmPropValue1 = "myValue1";
        vmTwoProperties.put(vmPropKey1, vmPropValue1);
        String vmPropKey2 = "myProperty2";

        String vmPropValue2 = "myValue2";
        vmTwoProperties.put(vmPropKey2, vmPropValue2);
        TestNode node1 = rmHelper.createNode(node1Name, vmTwoProperties);
        testNodes.add(node1);
        String node1URL = node1.getNode().getNodeInformation().getURL();
        resourceManager.addNode(node1URL, nodeSourceName);

        //wait node adding event
        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node1URL);
        //wait for the nodes to be in free state
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        Assert.assertEquals(1, resourceManager.listAliveNodeUrls().size());

        //--------------------------------------------------
        //create a second node with only the first VM property
        //---------------------------------------------------

        HashMap<String, String> vmProp1 = new HashMap<>();
        vmProp1.put(vmPropKey1, vmPropValue1);

        TestNode node2 = rmHelper.createNode(node2Name, vmProp1);
        testNodes.add(node2);
        String node2URL = node2.getNode().getNodeInformation().getURL();
        resourceManager.addNode(node2URL, nodeSourceName);

        //wait node adding event
        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL);
        //wait for the nodes to be in free state
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        Assert.assertEquals(2, resourceManager.listAliveNodeUrls().size());

        //--------------------------------------------------
        //create a third node with only the second VM property
        //---------------------------------------------------

        HashMap<String, String> vmProp2 = new HashMap<>();
        vmProp1.put(vmPropKey2, vmPropValue2);

        TestNode node3 = rmHelper.createNode(node3Name, vmProp2);
        testNodes.add(node3);
        String node3URL = node3.getNode().getNodeInformation().getURL();
        resourceManager.addNode(node3URL, nodeSourceName);

        //wait node adding event
        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node3URL);
        //wait for the nodes to be in free state
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        Assert.assertEquals(3, resourceManager.listAliveNodeUrls().size());

        //create the static selection script object that check vm prop1
        SelectionScript sScript1 = new SelectionScript(new File(vmPropSelectionScriptpath.toURI()),
                                                       new String[] { vmPropKey1, vmPropValue1 },
                                                       true);

        //create the static selection script object prop2
        SelectionScript sScript2 = new SelectionScript(new File(vmPropSelectionScriptpath.toURI()),
                                                       new String[] { vmPropKey2, vmPropValue2 },
                                                       false);

        log("Test 1");

        ArrayList<SelectionScript> scriptsList = new ArrayList<>();

        scriptsList.add(sScript1);
        scriptsList.add(sScript2);

        NodeSet nodes = resourceManager.getAtMostNodes(1, scriptsList, null);

        //wait node selection
        PAFuture.waitFor(nodes);

        assertEquals(1, nodes.size());
        assertEquals(node1URL, nodes.get(0).getNodeInformation().getURL());

        //wait for node busy event
        RMNodeEvent evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        assertEquals(NodeState.BUSY, evt.getNodeState());

        assertEquals(2, resourceManager.getState().getFreeNodesNumber());

        resourceManager.releaseNodes(nodes);
        //wait for node free event
        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        assertEquals(NodeState.FREE, evt.getNodeState());

        assertEquals(3, resourceManager.getState().getFreeNodesNumber());

        log("Test 2");

        nodes = resourceManager.getAtMostNodes(3, scriptsList, nodes);

        //wait node selection
        PAFuture.waitFor(nodes);

        assertEquals(0, nodes.size());
        assertEquals(3, resourceManager.getState().getFreeNodesNumber());
    }
}
