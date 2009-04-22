/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
import java.util.HashMap;

import nodestate.FunctionalTDefaultRM;
import nodestate.RMEventReceiver;

import org.junit.Assert;
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
public class staticSelectionScriptTest extends FunctionalTDefaultRM {

    private String vmPropSelectionScriptpath = this.getClass().getResource("vmPropertySelectionScript.js")
            .getPath();

    private String badSelectionScriptpath = this.getClass().getResource("badSelectionScript.js").getPath();

    private String withoutSelectedSelectionScriptpath = this.getClass().getResource(
            "withoutSelectedSScript.js").getPath();

    private String vmPropKey = "myProperty";
    private String vmPropValue = "myValue";

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
                RMEventType.NODESOURCE_CREATED, RMEventType.NODE_STATE_CHANGED };

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
        String node1URL = "rmi://" + hostName + "/" + node1Name;
        String node2URL = "rmi://" + hostName + "/" + node2Name;

        HashMap<String, String> vmProperties = new HashMap<String, String>();
        vmProperties.put(this.vmPropKey, this.vmPropValue);

        createNode(node1Name, vmProperties);
        admin.addNode(node1URL);

        //wait node adding event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == 1);

        //create the static selection script object
        SelectionScript sScript = new SelectionScript(new File(vmPropSelectionScriptpath), new String[] {
                this.vmPropKey, this.vmPropValue }, false);

        log("Test 1");

        NodeSet nodes = admin.getAtMostNodes(new IntWrapper(1), sScript);

        //wait node selection
        PAFuture.waitFor(nodes);

        assertTrue(nodes.size() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb);
        assertTrue(nodes.get(0).getNodeInformation().getURL().equals(node1URL));

        //wait for node busy event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesBusyEvents().size() == 1);

        admin.freeNode(nodes.get(0));

        //wait for node free event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesFreeEvents().size() == 1);

        log("Test 2");

        nodes = admin.getAtMostNodes(new IntWrapper(3), sScript);

        //wait node selection
        PAFuture.waitFor(nodes);

        assertTrue(nodes.size() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb);
        assertTrue(nodes.get(0).getNodeInformation().getURL().equals(node1URL));

        //wait for node busy event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesBusyEvents().size() == 1);

        admin.freeNode(nodes.get(0));

        //wait for node free event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesFreeEvents().size() == 1);

        log("Test 3");

        //add a second with JVM env var
        createNode(node2Name, vmProperties);
        admin.addNode(node2URL);

        //wait node adding event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == 1);

        nodes = admin.getAtMostNodes(new IntWrapper(3), sScript);

        //wait node selection
        PAFuture.waitFor(nodes);

        assertTrue(nodes.size() == 2);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb);

        //wait for node busy event
        receiver.waitForNEvent(2);
        assertTrue(receiver.cleanNgetNodesBusyEvents().size() == 2);

        admin.freeNodes(nodes);

        //wait for node free event
        receiver.waitForNEvent(2);
        assertTrue(receiver.cleanNgetNodesFreeEvents().size() == 2);

        log("Test 4");

        admin.removeNode(node1URL, true);
        admin.removeNode(node2URL, true);

        //wait for node removed event
        receiver.waitForNEvent(2);
        assertTrue(receiver.cleanNgetNodesremovedEvents().size() == 2);

        nodes = admin.getAtMostNodes(new IntWrapper(3), sScript);

        //wait node selection
        PAFuture.waitFor(nodes);

        assertTrue(nodes.size() == 0);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb);

        log("Test 5");

        //create the bad static selection script object
        SelectionScript badScript = new SelectionScript(new File(badSelectionScriptpath), new String[] {},
            false);

        nodes = admin.getAtMostNodes(new IntWrapper(3), badScript);

        //wait node selection
        try {
            PAFuture.waitFor(nodes);
            System.out.println("Number of found nodes " + nodes.size());
            Assert.assertTrue(false);
        } catch (RuntimeException e) {
        }

        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb);

        log("Test 6");

        //create the static selection script object that doesn't define 'selected'
        SelectionScript noSelectedScript = new SelectionScript(new File(withoutSelectedSelectionScriptpath),
            new String[] {}, false);

        nodes = admin.getAtMostNodes(new IntWrapper(3), noSelectedScript);

        //wait node selection
        try {
            PAFuture.waitFor(nodes);
            System.out.println("Number of found nodes " + nodes.size());
            Assert.assertTrue(false);
        } catch (RuntimeException e) {
        }
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb);
    }

}
