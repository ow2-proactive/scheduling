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
package functionaltests.selectionscript;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import org.junit.Assert;
import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.RMConsecutive;
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
public class SelectionScriptTimeOutTest extends RMConsecutive {

    private URL selectionScriptWithtimeOutPath = this.getClass().getResource("selectionScriptWithtimeOut.js");

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {
        RMTHelper helper = RMTHelper.getDefaultInstance();
        ResourceManager resourceManager = helper.getResourceManager();
        helper.createNodeSource();
        int nodesNumber = resourceManager.getState().getTotalNodesNumber();
        int scriptSleepingTime = 15000; //secs

        RMTHelper.log("Test 1 - selecting nodes with timeout script");

        //create the static selection script object
        SelectionScript script = new SelectionScript(new File(selectionScriptWithtimeOutPath.toURI()),
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

        Assert.assertEquals(nodesNumber, resourceManager.getState().getFreeNodesNumber());

        RMTHelper.log("Test 2 - selecting nodes with script which is timed out only on some hosts");
        String nodeName = "timeoutNode";

        HashMap<String, String> vmProperties = new HashMap<String, String>();
        vmProperties.put(nodeName, "dummy");

        String nodeURL = helper.createNode(nodeName, vmProperties).getNode().getNodeInformation().getURL();
        resourceManager.addNode(nodeURL);
        helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
        //wait for the nodes to be in free state
        helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // now we have nodesNumber + 1 nodes
        script = new SelectionScript(new File(selectionScriptWithtimeOutPath.toURI()), new String[] {
                Integer.toString(scriptSleepingTime), nodeName }, false);

        // selecting all nodes
        nodes = resourceManager.getAtMostNodes(nodesNumber + 1, script);
        // have to get nodesNumber due to the script timeout on one node
        Assert.assertEquals(nodesNumber, nodes.size());
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
        Assert.assertEquals(nodesNumber - 1, resourceManager.getState().getFreeNodesNumber());
        resourceManager.releaseNodes(nodes);
    }
}
