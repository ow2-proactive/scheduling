package functionaltests;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.core.rmproxies.PerUserConnectionRMProxiesManager;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;
import org.ow2.proactive.scheduler.core.rmproxies.SchedulerRMProxy;
import org.ow2.proactive.scheduler.core.rmproxies.SingleConnectionRMProxiesManager;
import org.ow2.proactive.scheduler.core.rmproxies.UserRMProxy;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.NodeSet;
import org.ow2.tests.FunctionalTest;


public class TestRMProxy extends FunctionalTest {

    static final int NODES_NUMBER = 5;

    private RMTHelper helper = RMTHelper.getDefaultInstance();
    
    private Credentials user1Credentials;

    private Credentials user2Credentials;
    
    @Before
    public void init() throws Exception {
        user1Credentials = Credentials.createCredentials(new CredData("admin", "admin"), helper
                .getRMAuth().getPublicKey());

        user2Credentials = Credentials.createCredentials(new CredData("demo", "demo"), helper
                .getRMAuth().getPublicKey());
    }
    
    @Test
    public void testProxiesManager() throws Exception {
        createNodeSource();
        
        System.out.println("\n Test with per-user connection \n");
        testRMProxies(false);

        System.out.println("\n Test with single connection \n");
        
        testRMProxies(true);
    }

    private void createNodeSource() throws Exception {
        helper.createDefaultNodeSource(NODES_NUMBER);
        helper.waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, NodeSource.DEFAULT);
        for (int i = 0; i < NODES_NUMBER; i++) {
            helper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
            helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }
    }
    
    private void testRMProxies(boolean singleUserConnection) throws Exception {
        ResourceManager rm = helper.getResourceManager();

        Assert.assertEquals(NODES_NUMBER, rm.getState().getFreeNodesNumber());

        URI rmUri = new URI("rmi://localhost:" + CentralPAPropertyRepository.PA_RMI_PORT.getValue() + "/");
        Credentials schedulerProxyCredentials = Credentials.getCredentials(PASchedulerProperties
                .getAbsolutePath(PASchedulerProperties.RESOURCE_MANAGER_CREDS.getValueAsString()));

        
        RMProxiesManager proxiesManager;
        
        if (singleUserConnection) {
            proxiesManager = new SingleConnectionRMProxiesManager(rmUri, schedulerProxyCredentials);
        } else {
            proxiesManager = new PerUserConnectionRMProxiesManager(rmUri, schedulerProxyCredentials);
        }

        UserRMProxy user1RMProxy = proxiesManager.getUserRMProxy("admin", user1Credentials);
        Assert.assertSame("Proxy manager should return cached proxy instance", user1RMProxy, proxiesManager
                .getUserRMProxy("admin", user1Credentials));

        UserRMProxy user2RMProxy = proxiesManager.getUserRMProxy("demo", user2Credentials);
        Assert.assertSame("Proxy manager should return cached proxy instance", user2RMProxy, proxiesManager
                .getUserRMProxy("demo", user2Credentials));

        requestReleaseOneNode(user1RMProxy, rm);

        testSplitNodeSet(user1RMProxy, rm);

        checkSchedulerProxy(proxiesManager);

        requestWithExtraNodes(user1RMProxy, rm);

        requestTooManyNodes(user1RMProxy, rm);

        requestReleaseAllNodes(user1RMProxy, rm);

        checkSchedulerProxy(proxiesManager);

        requestReleaseOneNode(user2RMProxy, rm);

        requestReleaseAllNodes(user2RMProxy, rm);

        requestWithTwoUsers(user1RMProxy, user2RMProxy, rm);

        checkSchedulerProxy(proxiesManager);
        
        System.out.println("Terminate user proxy1");
        proxiesManager.terminateUserRMProxy("admin");
        user1RMProxy = proxiesManager.getUserRMProxy("admin", user1Credentials);
        requestReleaseAllNodes(user1RMProxy, rm);

        System.out.println("Terminate user proxy2");
        proxiesManager.terminateUserRMProxy("demo");
        user2RMProxy = proxiesManager.getUserRMProxy("demo", user2Credentials);
        requestReleaseAllNodes(user2RMProxy, rm);
        
        System.out.println("Terminate all proxies");
        proxiesManager.terminateAllProxies();
    }
    
    private void requestWithTwoUsers(UserRMProxy proxy1, UserRMProxy proxy2, ResourceManager rm)
            throws Exception {
        System.out.println("Request nodes for two users");
        
        NodeSet nodeSet1 = proxy1.getNodes(1, null, null, null, false);
        NodeSet nodeSet2 = proxy2.getNodes(2, null, null, null, false);
        waitWhenNodeSetAcquired(nodeSet1, 1);
        waitWhenNodeSetAcquired(nodeSet2, 2);

        Assert.assertEquals(NODES_NUMBER - 3, rm.getState().getFreeNodesNumber());

        proxy1.releaseNodes(nodeSet1);
        proxy2.releaseNodes(nodeSet2);
        waitWhenNodesAreReleased(3);

        Assert.assertEquals(NODES_NUMBER, rm.getState().getFreeNodesNumber());

    }

    private void checkSchedulerProxy(RMProxiesManager proxiesManager) {
        System.out.println("Check scheduler proxy");

        SchedulerRMProxy proxy = proxiesManager.getSchedulerRMProxy();
        Assert.assertEquals(proxy.getState().getFreeNodesNumber(), NODES_NUMBER);
        Assert.assertTrue(proxy.isActive().getBooleanValue());
    }

    private void requestTooManyNodes(UserRMProxy proxy, ResourceManager rm) throws Exception {
        System.out.println("Request more nodes than RM has");

        NodeSet nodeSet = proxy.getNodes(NODES_NUMBER + 1, null, null, null, false);
        PAFuture.waitFor(nodeSet);
        Assert.assertEquals(0, nodeSet.size());
        Assert.assertEquals(NODES_NUMBER, rm.getState().getFreeNodesNumber());
    }

    private void requestWithExtraNodes(UserRMProxy proxy, ResourceManager rm) throws Exception {
        System.out.println("Request NodeSet with extra nodes");

        TopologyDescriptor topology = TopologyDescriptor.SINGLE_HOST_EXCLUSIVE;
        NodeSet nodeSet = proxy.getNodes(1, topology, null, null, false);
        PAFuture.waitFor(nodeSet);
        Assert.assertEquals(1, nodeSet.size());
        Assert.assertNotNull("Extra nodes are expected", nodeSet.getExtraNodes());
        Assert.assertEquals(NODES_NUMBER - 1, nodeSet.getExtraNodes().size());
        Assert.assertEquals(0, rm.getState().getFreeNodesNumber());
        
        proxy.releaseNodes(nodeSet);
        waitWhenNodesAreReleased(NODES_NUMBER);
        Assert.assertEquals(NODES_NUMBER, rm.getState().getFreeNodesNumber());
    }

    private void requestReleaseAllNodes(UserRMProxy proxy, ResourceManager rm) throws Exception {
        System.out.println("Request and release all nodes");

        List<NodeSet> nodeSets = new ArrayList<NodeSet>();
        for (int i = 0; i < NODES_NUMBER; i++) {
            nodeSets.add(proxy.getNodes(1, null, null, null, false));
        }

        for (NodeSet nodeSet : nodeSets) {
            waitWhenNodeSetAcquired(nodeSet, 1);
            proxy.releaseNodes(nodeSet);
        }

        waitWhenNodesAreReleased(NODES_NUMBER);

        Assert.assertEquals(NODES_NUMBER, rm.getState().getFreeNodesNumber());
    }

    private void requestReleaseOneNode(UserRMProxy proxy, ResourceManager rm) throws Exception {
        System.out.println("Request and release single node");

        NodeSet nodeSet = proxy.getNodes(1, null, null, null, false);
        waitWhenNodeSetAcquired(nodeSet, 1);

        proxy.releaseNodes(nodeSet);
        waitWhenNodesAreReleased(1);

        Assert.assertEquals(NODES_NUMBER, rm.getState().getFreeNodesNumber());
    }

    private void testSplitNodeSet(UserRMProxy proxy, ResourceManager rm) throws Exception {
        System.out.println("Request as single NodeSet, release it as two NodeSets");

        NodeSet nodeSet = proxy.getNodes(3, null, null, null, false);
        waitWhenNodeSetAcquired(nodeSet, 3);
        Assert.assertEquals(NODES_NUMBER - 3, rm.getState().getFreeNodesNumber());

        NodeSet nodeSet1 = new NodeSet();
        nodeSet1.add(nodeSet.remove(0));

        NodeSet nodeSet2 = new NodeSet();
        nodeSet2.add(nodeSet.remove(0));
        nodeSet2.add(nodeSet.remove(0));

        proxy.releaseNodes(nodeSet1);
        waitWhenNodesAreReleased(1);
        Assert.assertEquals(NODES_NUMBER - 2, rm.getState().getFreeNodesNumber());

        proxy.releaseNodes(nodeSet2);
        waitWhenNodesAreReleased(2);
        Assert.assertEquals(NODES_NUMBER, rm.getState().getFreeNodesNumber());
    }

    private void waitWhenNodesAreReleased(int nodesNumber) {
        for (int i = 0; i < nodesNumber; i++) {
            helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }
    }

    private void waitWhenNodeSetAcquired(NodeSet nodeSet, int expectedNodesNumber) {
        PAFuture.waitFor(nodeSet);
        Assert.assertEquals("Unexpected nodes number in NodeSet", expectedNodesNumber, nodeSet.size());
        for (int i = 0; i < expectedNodesNumber; i++) {
            helper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }
    }
}
