package org.ow2.proactive.resourcemanager.core;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.db.RMDBManager;
import org.ow2.proactive.resourcemanager.exception.AddingNodesException;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoringImpl;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.rmnode.RMDeployingNode;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.selection.SelectionManager;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;

import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class RMCoreTest {

    private RMCore rmCore;
    @Mock
    private Client mockedCaller;
    @Mock
    private NodeSource mockedNodeSource;
    @Mock
    private RMMonitoringImpl mockedMonitoring;
    @Mock
    private SelectionManager mockedSelectionManager;
    @Mock
    private RMDBManager dataBaseManager;
    @Mock
    private RMNode mockedRemovableNodeInDeploy;
    @Mock
    private RMNode mockedUnremovableNodeInDeploy;
    @Mock
    private RMNode mockedRemovableNode;
    @Mock
    private RMNode mockedUnremovableNode;
    @Mock
    private RMNode mockedBusyNode;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        populateRMCore();
    }

    /*
     * Non-existing URL (non-deployment)
     * Non preemptive
     */
    @Test
    public void testRemoveNode1() throws Throwable {
        boolean result = rmCore.removeNode("NON_EXISTING_URL_NON_DEPLOYMENT", false).getBooleanValue();
        assertEquals(false, result);
    }
    
    /*
     * Non-existing URL (non-deployment)
     * Preemptive
     */
    @Test
    public void testRemoveNode2() {
        boolean result = rmCore.removeNode("NON_EXISTING_URL_NON_DEPLOYMENT", true).getBooleanValue();
        assertEquals(false, result);
    }
    
    /*
     * existing URL (non-deployment)
     * Non preemptive
     */
    @Test
    public void testRemoveNode3() {
        
        boolean result = rmCore.removeNode("mockedRemovableNode", false).getBooleanValue();
        assertEquals(true, result);
    }
    
    /*
     * existing URL (non-deployment)
     * Preemptive
     */
    @Test
    public void testRemoveNode4() {
        
        boolean result = rmCore.removeNode("mockedRemovableNode", true).getBooleanValue();
        assertEquals(true, result);
    }
    
    /*
     * Non-existing URL (deployment)
     * Non preemptive
     */
    @Test
    public void testRemoveNode5() {
        
        boolean result = rmCore.removeNode(RMDeployingNode.PROTOCOL_ID +
                "://NON_EXISTING_URL_NON_DEPLOYMENT", false).getBooleanValue();
        assertEquals(false, result);
    }
    
    /*
     * Non-existing URL (deployment)
     * Preemptive
     */
    @Test
    public void testRemoveNode6() {
        
        boolean result = rmCore.removeNode(RMDeployingNode.PROTOCOL_ID +
                "://NON_EXISTING_URL_NON_DEPLOYMENT", true).getBooleanValue();
        assertEquals(false, result);
    }
    
    /*
     * existing URL (deployment)
     * Non preemptive
     */
    @Test
    public void testRemoveNode7() {
        
        boolean result = rmCore.removeNode(RMDeployingNode.PROTOCOL_ID +
                "://removableNode", false).getBooleanValue();
        assertEquals(false, result);
    }
    
    /*
     * existing URL (deployment)
     * Preemptive
     */
    @Test
    public void testRemoveNode8() {
        
        boolean result = rmCore.removeNode(RMDeployingNode.PROTOCOL_ID +
                "://removableNode", true).getBooleanValue();
        assertEquals(false, result);
    }
    
    @Test
    public void testReleaseNode1() {
        boolean result = rmCore.releaseNode(mockedRemovableNode.getNode()).getBooleanValue();
        assertEquals(true, result);
    }
    
    @Test
    public void testReleaseNode2() {
        boolean result = rmCore.releaseNode(mockedUnremovableNode.getNode()).getBooleanValue();
        assertEquals(true, result);
    }
    
    @Test
    public void testReleaseNode3() {
        boolean result = rmCore.releaseNode(mockedRemovableNodeInDeploy.getNode()).getBooleanValue();
        assertEquals(true, result);
    }
    
    @Test
    public void testReleaseNode4() {
        boolean result = rmCore.releaseNode(mockedRemovableNodeInDeploy.getNode()).getBooleanValue();
        assertEquals(true, result);
    }
    
    @Test
    public void testGetNodes() {
        NodeSet nodeSet = rmCore.getNodes(1, TopologyDescriptor.ARBITRARY, null, null, false);
        assertEquals(0, nodeSet.size()); // we don't check nodeSet as its content is also tested in SelectionManagerTest
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testGetNodesBadNodesNumber() {
        rmCore.getNodes(-1, TopologyDescriptor.ARBITRARY, null, null, false);
    }
    
    @Test
    public void testGetAtMostNodes() {
        NodeSet nodeSet = rmCore.getAtMostNodes(1, TopologyDescriptor.ARBITRARY, null, null);
        assertEquals(0, nodeSet.size()); // we don't check nodeSet as its content is also tested in SelectionManagerTest
    }
    
    @Test
    public void testLockNodes1() {
        Set<String> nodesToLock = new HashSet<String>(1);
        nodesToLock.add("mockedRemovableNode");
        boolean result = rmCore.lockNodes(nodesToLock).getBooleanValue();
        assertEquals(true, result);
    }
    
    @Test
    public void testLockNodes2() {
        Set<String> nodesToLock = new HashSet<String>(2);
        nodesToLock.add("mockedRemovableNode");
        nodesToLock.add(RMDeployingNode.PROTOCOL_ID + "://removableNode");
        boolean result = rmCore.lockNodes(nodesToLock).getBooleanValue();
        assertEquals(true, result);
    }
    
    @Test
    public void testLockNodes3() {
        Set<String> nodesToLock = new HashSet<String>(1);
        nodesToLock.add("mockedUnremovableNode");
        boolean result = rmCore.lockNodes(nodesToLock).getBooleanValue();
        assertEquals(false, result);
    }
    
    @Test
    public void testUnlockNodes1() {
        Set<String> nodesToUnlock = new HashSet<String>(1);
        nodesToUnlock.add("mockedBusyNode");
        boolean result = rmCore.unlockNodes(nodesToUnlock).getBooleanValue();
        assertEquals(true, result);
    }
    
    @Test
    public void testUnlockNodes2() {
        Set<String> nodesToUnlock = new HashSet<String>(1);
        nodesToUnlock.add("mockedRemovableNode");
        boolean result = rmCore.unlockNodes(nodesToUnlock).getBooleanValue();
        assertEquals(false, result);
    }
    
    /**
     * New node to existing nodesource
     */
    @Test
    public void testAddNodeNewNodeExistingNodeSource() {
        boolean result = rmCore.addNode(
                "NODE-testAddNodeNewNodeExistingNodeSource", mockedNodeSource.getName()).getBooleanValue();
        assertEquals(true, result);
    }
    
    /**
     * Existing node to existing nodesource
     */
    @Test
    public void testAddNodeExistingNodeExistingNodeSource() {
        boolean result = rmCore.addNode(
                mockedRemovableNode.getNodeName(), mockedNodeSource.getName()).getBooleanValue();
        assertEquals(true, result);
    }
    
    /**
     * Existing node to new nodesource
     */
    @Test(expected=AddingNodesException.class)
    public void testAddNodeExistingNodeNewNodeSource() {
        rmCore.addNode(mockedRemovableNode.getNodeName(),
                "NEW-NODESOURCE-testAddNodeNewNodeNewNodeSource").getBooleanValue();
    }
    
    /**
     * New node to new nodesource
     */
    @Test(expected=AddingNodesException.class)
    public void testAddNodeNewNodeNewNodeSource() {
        rmCore.addNode("NODE-testAddNodeNewNodeNewNodeSource",
                "NEW-NODESOURCE-testAddNodeNewNodeNewNodeSource").getBooleanValue();
    }
    
    @Test
    public void testRemoveNodeSourceExistingNodeSourceNoPreempt() {
        boolean result = rmCore.removeNodeSource(mockedNodeSource.getName(), false).getBooleanValue();
        assertEquals(true, result);
    }
    
    @Test
    public void testRemoveNodeSourceExistingNodeSourcePreempt() {
        boolean result = rmCore.removeNodeSource(mockedNodeSource.getName(), true).getBooleanValue();
        assertEquals(true, result);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testRemoveNodeSourceNonExistingNodeSource() {
        rmCore.removeNodeSource("NON-EXISTING-NODESOURCE", true).getBooleanValue();
    }
    
    @Test
    @Ignore("This test is ignored because mocked Nodes are not compatible with HashSet semantics")
    public void testGetState() {
        RMState rmState = rmCore.getState();
        assertEquals("Expected 2, received " + rmState.getFreeNodesNumber(), 2, rmState.getFreeNodesNumber());
        assertEquals("Expected 2, received " + rmState.getTotalAliveNodesNumber(), rmState.getTotalAliveNodesNumber());
        assertEquals("Expected 5, received " + rmState.getTotalNodesNumber(), rmState.getTotalNodesNumber());
    }
    
    /**
     * 5 nodes (same nodesource):
     *  - free, deployed
     *  - not free, deployed
     *  - free, not deployed yet
     *  - not free, not deployed yet
     *  - locked
     */
    private void populateRMCore() {

        when(mockedCaller.checkPermission(Matchers.any(Permission.class), Matchers.any(String.class)))
                .thenReturn(true);
        when(mockedSelectionManager.selectNodes(Matchers.any(Criteria.class), Matchers.any(Client.class)))
                .thenReturn(new NodeSet());

        HashMap<String, NodeSource> nodeSources = new HashMap<String, NodeSource>(1);
        configureNodeSource(mockedNodeSource, "NODESOURCE-test");
        nodeSources.put(mockedNodeSource.getName(), mockedNodeSource);

        // MockedRMNodeParameters(String url, boolean isFree, boolean isDown, boolean isLocked, NodeSource nodeSource, RMNode rmNode)
        configureRMNode(new MockedRMNodeParameters("mockedRemovableNode", true, true, false,
                mockedNodeSource, "NODESOURCE-test", mockedRemovableNode));
        configureRMNode(new MockedRMNodeParameters("mockedUnremovableNode", false, true, false,
                mockedNodeSource, "NODESOURCE-test", mockedUnremovableNode));
        configureRMNode(new MockedRMNodeParameters(RMDeployingNode.PROTOCOL_ID + "://removableNode", true, true, false,
                mockedNodeSource, "NODESOURCE-test", mockedRemovableNodeInDeploy));
        configureRMNode(new MockedRMNodeParameters(RMDeployingNode.PROTOCOL_ID + "://unRemovableNode", false, false, false,
                mockedNodeSource, "NODESOURCE-test", mockedUnremovableNodeInDeploy));
        configureRMNode(new MockedRMNodeParameters("mockedBusyNode", false, false, true,
                mockedNodeSource, "NODESOURCE-test", mockedBusyNode));

        HashMap<String, RMNode> nodes = new HashMap<String, RMNode>(5);
        nodes.put(mockedRemovableNodeInDeploy.getNodeName(), mockedRemovableNodeInDeploy);
        nodes.put(mockedUnremovableNodeInDeploy.getNodeName(), mockedUnremovableNodeInDeploy);
        nodes.put(mockedRemovableNode.getNodeName(), mockedRemovableNode);
        nodes.put(mockedUnremovableNode.getNodeName(), mockedUnremovableNode);
        nodes.put(mockedBusyNode.getNodeName(), mockedBusyNode);

        ArrayList<RMNode> freeNodes = new ArrayList<RMNode>(2);
        freeNodes.add(mockedRemovableNodeInDeploy);
        freeNodes.add(mockedRemovableNode);

        rmCore = new RMCore(nodeSources, new ArrayList<String>(), nodes, mockedCaller,
                mockedMonitoring, mockedSelectionManager, freeNodes, dataBaseManager);
    }
    
    private void configureRMNode(MockedRMNodeParameters param) {
        RMNode rmNode = param.getRmNode();
        Node mockedNode = Mockito.mock(Node.class);
        NodeInformation mockedNodeInformation = Mockito.mock(NodeInformation.class);
        when(mockedNode.getNodeInformation()).thenReturn(mockedNodeInformation);
        when(rmNode.getNode()).thenReturn(mockedNode);
        when(rmNode.getNodeName()).thenReturn(param.getUrl());
        when(rmNode.isDown()).thenReturn(param.isDown());
        when(rmNode.isFree()).thenReturn(param.isFree());
        when(rmNode.isLocked()).thenReturn(param.isLocked());
        when(mockedNodeInformation.getURL()).thenReturn(param.getUrl());
        when(mockedNodeInformation.getName()).thenReturn(param.getUrl());
        when(rmNode.getNodeSource()).thenReturn(param.getNodeSource());
        when(rmNode.getNodeSourceName()).thenReturn(param.getNodeSourceName());
        when(rmNode.getAdminPermission()).thenReturn(null);
    }

    private void configureNodeSource(NodeSource nodeSource, String nodeSourceName) {
        when(nodeSource.getName()).thenReturn(nodeSourceName);
        when(nodeSource.acquireNode(Matchers.any(String.class), Matchers.any(Client.class)))
                .thenReturn(new BooleanWrapper(true));
    }

    private class MockedRMNodeParameters {
        private String url;
        private boolean isFree;
        private boolean isDown;
        private boolean isLocked;
        private NodeSource nodeSource;
        private String nodeSourceName;
        private RMNode rmNode;

        MockedRMNodeParameters(String url, boolean isFree, boolean isDown, boolean isLocked,
                               NodeSource nodeSource, String nodeSourceName, RMNode rmNode) {
            this.url = url;
            this.isFree = isFree;
            this.isDown = isDown;
            this.isLocked = isLocked;
            this.nodeSource = nodeSource;
            this.nodeSourceName = nodeSourceName;
            this.rmNode = rmNode;
        }

        protected String getUrl() {
            return url;
        }

        protected boolean isFree() {
            return isFree;
        }

        protected boolean isDown() {
            return isDown;
        }

        public NodeSource getNodeSource() {
            return nodeSource;
        }

        public String getNodeSourceName() {
            return nodeSourceName;
        }

        public RMNode getRmNode() {
            return rmNode;
        }

        public boolean isLocked() {
            return isLocked;
        }
    }
}
