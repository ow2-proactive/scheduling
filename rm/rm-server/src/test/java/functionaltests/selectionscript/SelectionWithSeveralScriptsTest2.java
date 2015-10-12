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
package functionaltests.selectionscript;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.DefaultInfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;
import org.junit.Test;

import functionaltests.utils.RMFunctionalTest;

import static functionaltests.utils.RMTHelper.log;
import static org.junit.Assert.*;


/**
 *
 * This class tests RM's mechanism of resource selection with static
 * scripts.
 *
 * -launch 5 nodes without this specified JVM environment variable => not verify script
 * -launch a first node with specified JVM environment variable "myProperty1"
 * -launch a second node with specified JVM environment variables "myProperty1" and "myProperty2"
 *
 * Ask 1 node from resource manager using 2 selection scripts
 * first script checks that "myProperty1" is defined
 * second script checks that "myProperty2" is defined
 *
 * As a result second node should be selected.
 *
 */
public class SelectionWithSeveralScriptsTest2 extends RMFunctionalTest {

    private URL vmPropSelectionScriptpath = this.getClass().getResource("vmPropertySelectionScript.groovy");

    @Test
    public void action() throws Exception {
        ResourceManager resourceManager = rmHelper.getResourceManager();

        String nodeSourceName = "SelectionWithSeveralScriptsTest2";
        resourceManager.createNodeSource(nodeSourceName, DefaultInfrastructureManager.class.getName(), null,
                StaticPolicy.class.getName(), new Object[] { "ALL", "ALL" }).getBooleanValue();
        rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, nodeSourceName);

        String node1Name = "node-sel2-1";
        String node2Name = "node-sel2-2";

        //---------------------------------------------------
        //create a first node with one VM property
        //---------------------------------------------------

        HashMap<String, String> vmProp1 = new HashMap<>();
        String vmPropKey1 = "myProperty1";
        String vmPropValue1 = "myValue1";
        vmProp1.put(vmPropKey1, vmPropValue1);

        String node1URL = rmHelper.createNode(node1Name, vmProp1).getNode().getNodeInformation().getURL();
        resourceManager.addNode(node1URL, nodeSourceName);

        //wait node adding event
        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node1URL);
        //wait for the node to be in free state
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        //--------------------------------------------------
        //create a second node with two VM properties
        //---------------------------------------------------

        HashMap<String, String> vmTwoProperties = new HashMap<>();
        vmTwoProperties.put(vmPropKey1, vmPropValue1);
        String vmPropKey2 = "myProperty2";
        String vmPropValue2 = "myValue2";
        vmTwoProperties.put(vmPropKey2, vmPropValue2);

        String node2URL = rmHelper.createNode(node2Name, vmTwoProperties).getNode().getNodeInformation()
                .getURL();
        resourceManager.addNode(node2URL, nodeSourceName);

        //wait node adding event
        rmHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node2URL);
        //wait for the node to be in free state
        rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        //create the static selection script object that check vm prop1
        SelectionScript sScript1 = new SelectionScript(new File(vmPropSelectionScriptpath.toURI()),
            new String[] { vmPropKey1, vmPropValue1 }, true);

        //create the static selection script object prop2
        SelectionScript sScript2 = new SelectionScript(new File(vmPropSelectionScriptpath.toURI()),
            new String[] { vmPropKey2, vmPropValue2 }, false);

        log("Test 1");

        ArrayList<SelectionScript> scriptsList = new ArrayList<>();

        scriptsList.add(sScript1);
        scriptsList.add(sScript2);

        NodeSet nodes = resourceManager.getAtMostNodes(1, scriptsList, null);

        //wait node selection
        PAFuture.waitFor(nodes);

        assertEquals(1, nodes.size());
        assertEquals(node2URL, nodes.get(0).getNodeInformation().getURL());

        //wait for node busy event
        RMNodeEvent evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        assertEquals(NodeState.BUSY, evt.getNodeState());
        assertEquals(1, resourceManager.getState().getFreeNodesNumber());

        resourceManager.releaseNodes(nodes);
        //wait for node free event
        evt = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node2URL);
        assertEquals(NodeState.FREE, evt.getNodeState());

        assertEquals(2, resourceManager.getState().getFreeNodesNumber());
    }
}
