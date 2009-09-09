package functionaltests.nodestate;

import static junit.framework.Assert.assertTrue;
import junit.framework.Assert;

import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.utils.NodeSet;

import functionalTests.FunctionalTest;
import functionaltests.RMTHelper;


/**
 *
 * @author ProActive team
 *
 */
public class TestConcurrentUsers extends FunctionalTest {

    /** Actions to be Perform by this test.
     * The method is called automatically by Junit framework.
     * @throws Exception If the test fails.
     */
    @org.junit.Test
    public void action() throws Exception {

        RMAdmin admin = RMTHelper.getAdminInterface();

        String hostName = ProActiveInet.getInstance().getHostname();
        String node1Name = "node1";
        String node1URL = "//" + hostName + "/" + node1Name;
        RMTHelper.createNode(node1Name);
        admin.addNode(node1URL);

        // waiting for node adding event
        RMTHelper.waitForNodeEvent(RMEventType.NODE_ADDED, node1URL);

        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

        RMTHelper.log("Test 1 - releasing of the foreign node");
        // acquiring a node
        final NodeSet ns = admin.getAtMostNodes(1, null);

        // waiting for node busy event
        RMNodeEvent evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        assertTrue(ns.size() == 1);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 0);

        Thread t = new Thread() {
            public void run() {
                try {
                    RMAuthentication auth = RMConnection.join(null);
                    Credentials cred = Credentials.createCredentials(RMTHelper.username, RMTHelper.password,
                            auth.getPublicKey());
                    RMAdmin admin = auth.logAsAdmin(cred);
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
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.FREE);

        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

        RMTHelper.log("Test 2 - releasing node twice");
        admin.freeNodes(ns);

        // to make sure everything has been processed
        Thread.sleep(1000);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

        RMTHelper.log("Test 3 - client crash detection");
        JVMProcessImpl nodeProcess = new JVMProcessImpl(
            new org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger());
        nodeProcess.setClassname("functionaltests.nodestate.GetAllNodes");
        nodeProcess.startProcess();

        // node busy event
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.BUSY);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 0);

        // client does not exist anymore
        RMTHelper.log("Client does not exist anymore. Waiting for client crash detection.");
        // it should be detected by RM
        // waiting for node free event
        evt = RMTHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED, node1URL);
        Assert.assertEquals(evt.getNodeState(), NodeState.FREE);
        assertTrue(admin.getTotalNodesNumber().intValue() == 1);
        assertTrue(admin.getFreeNodesNumber().intValue() == 1);

    }
}
