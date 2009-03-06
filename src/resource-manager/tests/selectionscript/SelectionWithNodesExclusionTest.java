package selectionscript;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import nodestate.FunctionalTDefaultRM;
import nodestate.RMEventReceiver;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;


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
public class SelectionWithNodesExclusionTest extends FunctionalTDefaultRM {

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

        log("Test 1");

        NodeSet nodeSetWithNodeToExclude = admin.getAtMostNodes(new IntWrapper(1), null);

        //wait for node selection
        PAFuture.waitFor(nodeSetWithNodeToExclude);

        assertTrue(nodeSetWithNodeToExclude.size() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb - 1);

        //wait for node busy event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesBusyEvents().size() == 1);

        admin.freeNodes(nodeSetWithNodeToExclude);

        //wait for node free event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesFreeEvents().size() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb);

        //get nodes with the previous node excluded
        NodeSet nodes = admin.getAtMostNodes(new IntWrapper(defaultDescriptorNodesNb),
                new ArrayList<SelectionScript>(), nodeSetWithNodeToExclude);

        //wait for node selection
        PAFuture.waitFor(nodes);

        //wait for node busy event
        receiver.waitForNEvent(defaultDescriptorNodesNb - 1);
        assertTrue(receiver.cleanNgetNodesBusyEvents().size() == defaultDescriptorNodesNb - 1);

        // booked all nodes minus the node to exclude
        assertTrue(nodes.size() == defaultDescriptorNodesNb - 1);
        //excluded node stays in free state
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

        admin.freeNodes(nodes);
        //wait for nodes freed event
        receiver.waitForNEvent(defaultDescriptorNodesNb - 1);

        assertTrue(receiver.cleanNgetNodesFreeEvents().size() == defaultDescriptorNodesNb - 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb);

        log("Test 2");

        //create the dynamic selection script object
        SelectionScript dummyDynamicScript = new SelectionScript(new File(dummySelectionScriptPath),
            new String[] {}, true);

        //get nodes with the previous node excluded
        nodes = admin.getAtMostNodes(new IntWrapper(defaultDescriptorNodesNb), dummyDynamicScript,
                nodeSetWithNodeToExclude);

        //wait for node selection
        PAFuture.waitFor(nodes);

        //wait for nodes busy event
        receiver.waitForNEvent(defaultDescriptorNodesNb - 1);
        assertTrue(receiver.cleanNgetNodesBusyEvents().size() == defaultDescriptorNodesNb - 1);

        // booked all nodes minus the node to exclude
        assertTrue(nodes.size() == defaultDescriptorNodesNb - 1);
        //excluded node stays in free state
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

        admin.freeNodes(nodes);
        //wait for node free event
        receiver.waitForNEvent(defaultDescriptorNodesNb - 1);
        assertTrue(receiver.cleanNgetNodesFreeEvents().size() == defaultDescriptorNodesNb - 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb);

        log("Test 3");

        //create the static selection script object
        SelectionScript dummyStaticScript = new SelectionScript(new File(dummySelectionScriptPath),
            new String[] {}, false);

        //get nodes with the previous node excluded
        nodes = admin.getAtMostNodes(new IntWrapper(defaultDescriptorNodesNb), dummyStaticScript,
                nodeSetWithNodeToExclude);

        //wait for node selection
        PAFuture.waitFor(nodes);

        //wait for nodes busy event
        receiver.waitForNEvent(defaultDescriptorNodesNb - 1);
        assertTrue(receiver.cleanNgetNodesBusyEvents().size() == defaultDescriptorNodesNb - 1);

        // booked all nodes minus the node to exclude
        assertTrue(nodes.size() == defaultDescriptorNodesNb - 1);
        //excluded node stays in free state
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

        admin.freeNodes(nodes);
        //wait for node free event
        receiver.waitForNEvent(defaultDescriptorNodesNb - 1);
        assertTrue(receiver.cleanNgetNodesFreeEvents().size() == defaultDescriptorNodesNb - 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb);

        log("Test 4");

        //deploy two other nodes

        String hostName = ProActiveInet.getInstance().getHostname();

        String node1Name = "node1";
        String node2Name = "node2";
        String node1URL = "rmi://" + hostName + "/" + node1Name;
        String node2URL = "rmi://" + hostName + "/" + node2Name;

        HashMap<String, String> vmProperties = new HashMap<String, String>();
        vmProperties.put(this.vmPropKey, this.vmPropValue);

        createNode(node1Name, vmProperties);
        admin.addNode(node1URL);

        createNode(node2Name, vmProperties);
        admin.addNode(node2URL);

        //wait for nodes added events
        receiver.waitForNEvent(2);
        assertTrue(receiver.cleanNgetNodesAddedEvents().size() == 2);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb + 2);

        //create the dynamic selection script object
        SelectionScript checkPropDynamicSScript = new SelectionScript(new File(vmPropSelectionScriptpath),
            new String[] { this.vmPropKey, this.vmPropValue }, true);
        Node node1ToExclude = NodeFactory.getNode(node1URL);

        nodeSetWithNodeToExclude = new NodeSet();
        nodeSetWithNodeToExclude.add(node1ToExclude);

        //get nodes with the previous node1 excluded
        nodes = admin.getAtMostNodes(new IntWrapper(defaultDescriptorNodesNb), checkPropDynamicSScript,
                nodeSetWithNodeToExclude);

        //wait for node selection
        PAFuture.waitFor(nodes);

        //wait for nodes busy event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesBusyEvents().size() == 1);

        // booked all nodes minus the node to exclude
        assertTrue(nodes.size() == 1);
        //excluded node stays in free state
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb + 1);

        //unique node got is node2
        assertTrue(nodes.get(0).getNodeInformation().getURL().equals(node2URL));

        admin.freeNodes(nodes);
        //wait for node free event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesFreeEvents().size() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb + 2);

        log("Test 5");

        //create the static selection script object
        SelectionScript checkPropStaticSScript = new SelectionScript(new File(vmPropSelectionScriptpath),
            new String[] { this.vmPropKey, this.vmPropValue }, false);

        Node node2ToExclude = NodeFactory.getNode(node2URL);

        nodeSetWithNodeToExclude = new NodeSet();
        nodeSetWithNodeToExclude.add(node2ToExclude);

        //get nodes with the previous node1 excluded
        nodes = admin.getAtMostNodes(new IntWrapper(defaultDescriptorNodesNb), checkPropStaticSScript,
                nodeSetWithNodeToExclude);

        //wait for node selection
        PAFuture.waitFor(nodes);

        //wait for nodes busy event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesBusyEvents().size() == 1);

        // booked all nodes minus the node to exclude
        assertTrue(nodes.size() == 1);
        //excluded node stays in free state
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb + 1);

        //unique node got is node2
        assertTrue(nodes.get(0).getNodeInformation().getURL().equals(node1URL));

        admin.freeNodes(nodes);
        //wait for node free event
        receiver.waitForNEvent(1);
        assertTrue(receiver.cleanNgetNodesFreeEvents().size() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb + 2);

        log("Test 6");

        nodeSetWithNodeToExclude.add(node1ToExclude);

        //get nodes with the previous node1 excluded
        nodes = admin.getAtMostNodes(new IntWrapper(defaultDescriptorNodesNb), checkPropStaticSScript,
                nodeSetWithNodeToExclude);

        //wait for node selection
        PAFuture.waitFor(nodes);

        assertTrue(admin.getFreeNodesNumber().intValue() == defaultDescriptorNodesNb + 2);
    }
}
