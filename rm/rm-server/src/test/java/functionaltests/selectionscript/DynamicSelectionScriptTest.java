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
import java.util.HashMap;

import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.TestNode;


/**
 *
 * This class tests RM's mechanism of resource selection with dynamic
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
public class DynamicSelectionScriptTest extends RMFunctionalTest {

    private URL vmPropSelectionScriptpath = this.getClass().getResource("vmPropertySelectionScript.groovy");

    private URL badSelectionScriptpath = this.getClass().getResource("badSelectionScript.js");

    private URL withoutSelectedSelectionScriptpath = this.getClass().getResource("withoutSelectedSScript.js");

    private URL fileCheckScriptPath = this.getClass().getResource("fileCheck.groovy");

    @Test
    public void action() throws Exception {
        ResourceManager resourceManager = rmHelper.getResourceManager();
        int nsSize = rmHelper.createNodeSource("DynamicSelectionScriptTest");

        String node1Name = "node1";
        String node2Name = "node2";

        HashMap<String, String> vmProperties = new HashMap<>();
        String vmPropKey = "myProperty";
        String vmPropValue = "myValue";
        vmProperties.put(vmPropKey, vmPropValue);

        TestNode node1 = rmHelper.createNode(node1Name, vmProperties);
        testNodes.add(node1);

        String node1URL = node1.getNode().getNodeInformation().getURL();
        resourceManager.addNode(node1URL);

        //wait node adding event
        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node1URL);
        //wait for the node to be in free state
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        //create the dynamic selection script object
        SelectionScript sScript = new SelectionScript(new File(vmPropSelectionScriptpath.toURI()),
                                                      new String[] { vmPropKey, vmPropValue },
                                                      true);

        log("Test 1");

        NodeSet nodes = resourceManager.getAtMostNodes(1, sScript);

        //wait node selection
        PAFuture.waitFor(nodes);

        assertEquals(1, nodes.size());
        assertEquals(nsSize, resourceManager.getState().getFreeNodesNumber());
        assertEquals(node1URL, nodes.get(0).getNodeInformation().getURL());

        RMNodeEvent evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        assertEquals(NodeState.BUSY, evt.getNodeState());

        resourceManager.releaseNode(nodes.get(0));

        //wait for node free event
        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        assertEquals(NodeState.FREE, evt.getNodeState());

        log("Test 2");

        nodes = resourceManager.getAtMostNodes(3, sScript);

        //wait node selection
        PAFuture.waitFor(nodes);

        assertEquals(1, nodes.size());
        assertEquals(nsSize, resourceManager.getState().getFreeNodesNumber());
        assertEquals(node1URL, nodes.get(0).getNodeInformation().getURL());

        //wait for node busy event
        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        assertEquals(NodeState.BUSY, evt.getNodeState());

        resourceManager.releaseNode(nodes.get(0));

        //wait for node free event
        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        assertEquals(NodeState.FREE, evt.getNodeState());

        log("Test 3");

        //add a second with JVM env var

        TestNode node2 = rmHelper.createNode(node2Name, vmProperties);
        testNodes.add(node2);
        String node2URL = node2.getNode().getNodeInformation().getURL();
        resourceManager.addNode(node2URL);

        //wait node adding event
        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL);
        //wait for the node to be in free state
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        nodes = resourceManager.getAtMostNodes(3, sScript);

        //wait node selection
        PAFuture.waitFor(nodes);

        assertEquals(2, nodes.size());
        assertEquals(nsSize, resourceManager.getState().getFreeNodesNumber());

        //wait for nodes busy event
        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        assertEquals(NodeState.BUSY, evt.getNodeState());
        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        assertEquals(NodeState.BUSY, evt.getNodeState());

        resourceManager.releaseNodes(nodes);

        //wait for node free event
        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        assertEquals(NodeState.FREE, evt.getNodeState());
        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        assertEquals(NodeState.FREE, evt.getNodeState());

        log("Test 4");

        resourceManager.removeNode(node1URL, true);
        resourceManager.removeNode(node2URL, true);

        //wait for node removed event
        rmHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, node2URL);
        rmHelper.waitForNodeEvent(RMEventType.NODE_REMOVED, node1URL);

        nodes = resourceManager.getAtMostNodes(3, sScript);

        //wait node selection
        PAFuture.waitFor(nodes);

        assertEquals(0, nodes.size());
        assertEquals(nsSize, resourceManager.getState().getFreeNodesNumber());

        log("Test 5");

        //create the bad dynamic selection script object
        SelectionScript badScript = new SelectionScript(new File(badSelectionScriptpath.toURI()),
                                                        new String[] {},
                                                        true);

        nodes = resourceManager.getAtMostNodes(3, badScript);

        //wait node selection
        PAFuture.waitFor(nodes);

        assertEquals(0, nodes.size());
        assertEquals(nsSize, resourceManager.getState().getFreeNodesNumber());

        log("Test 6");

        //create the dynamic selection script object that doesn't define 'selected'
        SelectionScript noSelectedScript = new SelectionScript(new File(withoutSelectedSelectionScriptpath.toURI()),
                                                               new String[] {},
                                                               true);

        nodes = resourceManager.getAtMostNodes(3, noSelectedScript);

        //wait node selection
        PAFuture.waitFor(nodes);

        assertEquals(0, nodes.size());
        assertEquals(nsSize, resourceManager.getState().getFreeNodesNumber());

        log("Test 7");

        // Checking the dynamicity of the node (period during which the dynamic characteristics do not change).
        // It sets to 10 secs for testing configuration.
        // So first run the dynamic script that fails checking if the file exist
        // Then create a file and call getNodes again. It must return 0 nodes.
        // Wait for 10 secs and call getNodes again. The script must be executed now
        // and we should get some nodes.

        final String FILE_NAME = System.getProperty("java.io.tmpdir") + "/dynamicity.selection";
        if (new File(FILE_NAME).exists()) {
            new File(FILE_NAME).delete();
        }
        SelectionScript fileCheck = new SelectionScript(new File(fileCheckScriptPath.toURI()),
                                                        new String[] { FILE_NAME },
                                                        true);
        log("The dynamic script checking is file exists must fail " + FILE_NAME);
        nodes = resourceManager.getAtMostNodes(1, fileCheck);
        assertEquals(0, nodes.size());

        log("Creating the file " + FILE_NAME);
        new File(FILE_NAME).createNewFile();

        log("The dynamic script checking is file exists must not be executed " + FILE_NAME);
        nodes = resourceManager.getAtMostNodes(1, fileCheck);
        assertEquals(0, nodes.size());

        Thread.sleep(10000);

        log("The dynamic script checking is file exists must pass " + FILE_NAME);
        nodes = resourceManager.getAtMostNodes(1, fileCheck);
        assertEquals(1, nodes.size());

        new File(FILE_NAME).delete();
    }
}
