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
package functionaltests.selectionscript;

import java.io.File;
import java.util.HashMap;

import org.junit.Assert;
import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;


/**
 * Test timeout in SelectionScriptExecution
 * launch a selection script that always says 'selected',
 * but before performs a Thread.sleep() two time longer
 * than defined selection script max waiting time for selection
 * script execution. So no nodes a got for this selection script.
 *
 * The resource manager do not propagate the exception outside.
 * Instead it just returns all the nodes where the selection script passed.
 *
 * @author ProActice team
 *
 */
public class SelectionScriptTimeOutTest extends FunctionalTest {

    private String selectionScriptWithtimeOutPath = this.getClass().getResource(
            "selectionScriptWithtimeOut.js").getPath();

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        RMTHelper.log("Deployment");

        ResourceManager resourceManager = RMTHelper.getResourceManager();
        RMTHelper.createGCMLocalNodeSource();
        RMTHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, NodeSource.GCM_LOCAL);

        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
            //wait for the nodes to be in free state
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }

        int scriptSleepingTime = PAResourceManagerProperties.RM_SELECT_SCRIPT_TIMEOUT.getValueAsInt() * 2;

        RMTHelper.log("Test 1 - selecting nodes with timeout script");

        //create the static selection script object
        SelectionScript script = new SelectionScript(new File(selectionScriptWithtimeOutPath),
            new String[] { Integer.toString(scriptSleepingTime) }, false);

        NodeSet nodes = resourceManager.getAtMostNodes(2, script);

        //wait node selection
        try {
            PAFuture.waitFor(nodes);
            System.out.println("Number of found nodes " + nodes.size());
            Assert.assertEquals(0, nodes.size());
        } catch (RuntimeException e) {
            Assert.assertTrue(false);
        }

        Assert.assertEquals(RMTHelper.defaultNodesNumber, resourceManager.getState().getFreeNodesNumber());

        RMTHelper.log("Test 2 - selecting nodes with script which is timed out only on some hosts");
        String nodeName = "timeoutNode";

        HashMap<String, String> vmProperties = new HashMap<String, String>();
        vmProperties.put(nodeName, "dummy");

        String nodeURL = RMTHelper.createNode(nodeName, vmProperties).getNodeInformation().getURL();
        resourceManager.addNode(nodeURL, NodeSource.GCM_LOCAL);
        RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //wait for the nodes to be in free state
        RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // now we have RMTHelper.defaultNodesNumber + 1 nodes
        script = new SelectionScript(new File(selectionScriptWithtimeOutPath), new String[] {
                Integer.toString(scriptSleepingTime), nodeName }, false);

        // selecting all nodes
        nodes = resourceManager.getAtMostNodes(RMTHelper.defaultNodesNumber + 1, script);
        // have to get RMTHelper.defaultNodesNumber due to the script timeout on one node
        Assert.assertEquals(RMTHelper.defaultNodesNumber, nodes.size());
        Assert.assertEquals(1, resourceManager.getState().getFreeNodesNumber());

        // waiting until selection manager finishes script execution for node "timeout"
        // as we don't know how long should we wait roughly estimate it as scriptSleepingTime/2  
        Thread.sleep(scriptSleepingTime / 2);

        NodeSet nodes2 = resourceManager.getAtMostNodes(1, null);
        Assert.assertEquals(1, nodes2.size());
        Assert.assertEquals(0, resourceManager.getState().getFreeNodesNumber());

        resourceManager.releaseNodes(nodes).getBooleanValue();
        resourceManager.releaseNodes(nodes2).getBooleanValue();

        nodes = resourceManager.getAtMostNodes(2, script);
        Assert.assertEquals(2, nodes.size());
        Assert
                .assertEquals(RMTHelper.defaultNodesNumber - 1, resourceManager.getState()
                        .getFreeNodesNumber());
        resourceManager.releaseNodes(nodes);
    }
}
