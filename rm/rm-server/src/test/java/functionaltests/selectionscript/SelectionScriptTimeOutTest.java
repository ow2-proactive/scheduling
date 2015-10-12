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

import static functionaltests.utils.RMTHelper.log;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.utils.RMFunctionalTest;

/**
 * Test timeout in SelectionScriptExecution launch a selection script that
 * always says 'selected', but before performs a Thread.sleep() two time longer
 * than defined selection script max waiting time for selection script
 * execution. So no nodes a got for this selection script.
 *
 * The resource manager do not propagate the exception outside. Instead it just
 * returns all the nodes where the selection script passed.
 *
 * @author ProActice team
 *
 */
public class SelectionScriptTimeOutTest extends RMFunctionalTest {

	private URL selectionScriptWithtimeOutPath = StaticSelectionScriptTest.class
			.getResource("selectionScriptWithtimeOut.groovy");

	@Test
	public void action() throws Exception {
		ResourceManager resourceManager = rmHelper.getResourceManager();
		rmHelper.createNodeSource();
		int nodesNumber = resourceManager.getState().getTotalNodesNumber();
		int scriptSleepingTime = 10 * 1000;

		log("Test 1 - selecting nodes with timeout script");

		// create the static selection script object
		SelectionScript script = new SelectionScript(new File(selectionScriptWithtimeOutPath.toURI()),
				new String[] { Integer.toString(scriptSleepingTime) }, false);

		NodeSet nodes = resourceManager.getAtMostNodes(2, script);

		// wait node selection
		PAFuture.waitFor(nodes);

		assertEquals(0, nodes.size());

		assertEquals(nodesNumber, resourceManager.getState().getFreeNodesNumber());

		log("Test 2 - selecting nodes with script which is timed out only on some hosts");
		String nodeName = "timeoutNode";

		HashMap<String, String> vmProperties = new HashMap<>();
		vmProperties.put(nodeName, "dummy");

		String nodeURL = rmHelper.createNode(nodeName, vmProperties).getNode().getNodeInformation().getURL();
		resourceManager.addNode(nodeURL);
		rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
		// wait for the nodes to be in free state
		rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

		// now we have nodesNumber + 1 nodes
		script = new SelectionScript(new File(selectionScriptWithtimeOutPath.toURI()),
				new String[] { Integer.toString(scriptSleepingTime), nodeName }, false);

		// selecting all nodes
		nodes = resourceManager.getAtMostNodes(nodesNumber + 1, script);
		// have to get nodesNumber due to the script timeout on one node
		assertEquals(nodesNumber, nodes.size());
		assertEquals(1, resourceManager.getState().getFreeNodesNumber());

		// waiting until selection manager finishes script execution for node
		// "timeout"
		// as we don't know how long should we wait roughly estimate it as
		// scriptSleepingTime/2
		Thread.sleep(scriptSleepingTime / 2);

		NodeSet nodes2 = resourceManager.getAtMostNodes(1, null);
		assertEquals(1, nodes2.size());
		assertEquals(0, resourceManager.getState().getFreeNodesNumber());

		resourceManager.releaseNodes(nodes).getBooleanValue();
		resourceManager.releaseNodes(nodes2).getBooleanValue();

		nodes = resourceManager.getAtMostNodes(2, script);
		assertEquals(2, nodes.size());
		assertEquals(nodesNumber - 1, resourceManager.getState().getFreeNodesNumber());
		resourceManager.releaseNodes(nodes);
	}
}
