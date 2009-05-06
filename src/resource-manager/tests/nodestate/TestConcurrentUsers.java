package nodestate;

import static junit.framework.Assert.assertTrue;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.utils.NodeSet;


/**
 *
 * @author ProActive team
 *
 */
public class TestConcurrentUsers extends FunctionalTDefaultRM {

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        RMEventType[] eventsList = { RMEventType.NODE_ADDED, RMEventType.NODE_STATE_CHANGED,
                RMEventType.NODE_REMOVED };

        RMEventReceiver receiver = (RMEventReceiver) PAActiveObject.newActive(
                RMEventReceiver.class.getName(), new Object[] { monitor, eventsList });

        receiver.cleanEventLists();
        RMFactory.setOsJavaProperty();

        String hostName = ProActiveInet.getInstance().getHostname();
        String node1Name = "node1";
        String node1URL = "//" + hostName + "/" + node1Name;
        createNode(node1Name);
        admin.addNode(node1URL);

        // waiting for node adding event
        receiver.waitForNEvent(1);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

        log("Test 1 - releasing of the foreign node");
        // acquiring a node
        final NodeSet ns = admin.getAtMostNodes(new IntWrapper(1), null);
        // waiting for node busy event
        receiver.waitForNEvent(1);
        assertTrue(ns.size() == 1);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 0);

        Thread t = new Thread() {
            public void run() {
                try {
                    RMAuthentication auth = RMConnection.join(null);
                    RMAdmin admin = auth.logAsAdmin(username, password);
                    admin.freeNode(ns.get(0));
                } catch (Exception e) {
                }
            }
        };
        t.start();
        t.join();

        // to make sure everything has been processed
        Thread.sleep(1000);

        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 0);

        admin.freeNodes(ns);

        receiver.waitForNEvent(1);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

        log("Test 2 - releasing node twice");
        admin.freeNodes(ns);

        // to make sure everything has been processed
        Thread.sleep(1000);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

        log("Test 3 - client crash detection");
        JVMProcessImpl nodeProcess = new JVMProcessImpl(
            new org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger());
        nodeProcess.setClassname("nodestate.GetAllNodes");
        nodeProcess.startProcess();

        // node busy event
        receiver.waitForNEvent(1);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 0);

        // client does not exist anymore
        log("Client does not exist anymore. Waiting for client crash detection.");
        // it should be detected by RM
        // waiting fot node free event
        receiver.waitForNEvent(1);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

    }
}