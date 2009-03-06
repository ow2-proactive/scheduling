/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package selectionscript;

import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import nodestate.FunctionalTDefaultRM;
import nodestate.RMEventReceiver;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;


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
public class SelectionWithSeveralScriptsTest extends FunctionalTDefaultRM {

    private String vmPropSelectionScriptpath = this.getClass().getResource("vmPropertySelectionScript.js")
            .getPath();

    private String vmPropKey1 = "myProperty1";
    private String vmPropValue1 = "myValue1";

    private String vmPropKey2 = "myProperty2";
    private String vmPropValue2 = "myValue2";

    /** Actions to be Perform by this test.
    * The method is called automatically by Junit framework. 
    * @throws Exception If the test fails.
    */
    @org.junit.Test
    public void action() throws Exception {

        log("Deployment");

        System.out.println(monitor.isAlive());
        System.out.println(admin.isAlive());

        RMEventType[] eventsList = { RMEventType.NODE_ADDED, RMEventType.NODE_REMOVED,
                RMEventType.NODESOURCE_CREATED, RMEventType.NODE_BUSY, RMEventType.NODE_FREE, };

        RMEventReceiver receiver = (RMEventReceiver) PAActiveObject.newActive(
                RMEventReceiver.class.getName(), new Object[] { monitor, eventsList });

        receiver.cleanEventLists();
        super.deployDefault();

        //wait for creation of GCM Node Source event, and deployment of its nodes
        receiver.waitForNEvent(defaultDescriptorNodesNb + 1);
        receiver.cleanEventLists();

        String hostName = ProActiveInet.getInstance().getHostname();

        String node1Name = "node1";
        String node2Name = "node2";
        String node3Name = "node3";
        String node1URL = "rmi://" + hostName + "/" + node1Name;
        String node2URL = "rmi://" + hostName + "/" + node2Name;
        String node3URL = "rmi://" + hostName + "/" + node3Name;

        //---------------------------------------------------
        //create a first node with the two VM properties
        //---------------------------------------------------

        HashMap<String, String> vmTwoProperties = new HashMap<String, String>();
        vmTwoProperties.put(this.vmPropKey1, this.vmPropValue1);
        vmTwoProperties.put(this.vmPropKey2, this.vmPropValue2);

        createNode(node1Name, vmTwoProperties);
        admin.addNode(node1URL);

        //wait node adding event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == 1);

        //--------------------------------------------------
        //create a second node with only the first VM property
        //---------------------------------------------------

        HashMap<String, String> vmProp1 = new HashMap<String, String>();
        vmProp1.put(this.vmPropKey1, this.vmPropValue1);

        createNode(node2Name, vmProp1);
        admin.addNode(node2URL);

        //wait node adding event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == 1);

        //--------------------------------------------------
        //create a third node with only the second VM property
        //---------------------------------------------------

        HashMap<String, String> vmProp2 = new HashMap<String, String>();
        vmProp1.put(this.vmPropKey2, this.vmPropValue2);

        createNode(node3Name, vmProp2);
        admin.addNode(node3URL);

        //wait node adding event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == 1);

        //create the static selection script object that check vm prop1
        SelectionScript sScript1 = new SelectionScript(new File(vmPropSelectionScriptpath), new String[] {
                this.vmPropKey1, this.vmPropValue1 }, true);

        //create the static selection script object prop2
        SelectionScript sScript2 = new SelectionScript(new File(vmPropSelectionScriptpath), new String[] {
                this.vmPropKey2, this.vmPropValue2 }, false);

        log("Test 1");

        ArrayList<SelectionScript> scriptsList = new ArrayList<SelectionScript>();

        scriptsList.add(sScript1);
        scriptsList.add(sScript2);

        NodeSet nodes = admin.getAtMostNodes(new IntWrapper(defaultDescriptorNodesNb), scriptsList, null);

        //wait node selection
        PAFuture.waitFor(nodes);

        assertTrue(nodes.size() == 1);
        assertTrue(nodes.get(0).getNodeInformation().getURL().equals(node1URL));

        //wait for node busy event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesBusyEvents().size() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb + 2);

        admin.freeNodes(nodes);
        //wait for node free event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesFreeEvents().size() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb + 3);

        log("Test 2");

        nodes = admin.getAtMostNodes(new IntWrapper(defaultDescriptorNodesNb), scriptsList, nodes);

        //wait node selection
        PAFuture.waitFor(nodes);

        assertTrue(nodes.size() == 0);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb + 3);

    }
}
