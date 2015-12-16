package org.ow2.proactive.resourcemanager.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeInformation;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.db.RMDBManager;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoringImpl;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.rmnode.RMDeployingNode;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.selection.SelectionManager;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;

public class RMCoreTest {

    private RMCore rmCore;
    @Mock
    private Client mockedCaller;
    @Mock
    NodeSource mockedNodeSource;
    @Mock
    RMMonitoringImpl mockedMonitoring;
    @Mock
    SelectionManager mockedSelectionManager;
    @Mock
    RMDBManager dataBaseManager;
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
        rmCore = new RMCore();
        populateNodes();
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
        nodesToLock.add("unRemovableNode");
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
        nodesToUnlock.add("removableNode");
        boolean result = rmCore.unlockNodes(nodesToUnlock).getBooleanValue();
        assertEquals(false, result);
    }
    
    
    
    
    
    /**
     * 5 nodes (same nodesource):
     *  - free, deployed
     *  - not free, deployed
     *  - free, not deployed yet
     *  - not free, not deployed yet
     *  - locked
     */
    private void populateNodes() {
        Node mockedNode1 = Mockito.mock(Node.class);
        Node mockedNode2 = Mockito.mock(Node.class);
        Node mockedNode3 = Mockito.mock(Node.class);
        Node mockedNode4 = Mockito.mock(Node.class);
        Node mockedNode5 = Mockito.mock(Node.class);
        NodeInformation mockedNodeInformation1 = Mockito.mock(NodeInformation.class);
        NodeInformation mockedNodeInformation2 = Mockito.mock(NodeInformation.class);
        NodeInformation mockedNodeInformation3 = Mockito.mock(NodeInformation.class);
        NodeInformation mockedNodeInformation4 = Mockito.mock(NodeInformation.class);
        NodeInformation mockedNodeInformation5 = Mockito.mock(NodeInformation.class);
        
        when(mockedNode1.getNodeInformation()).thenReturn(mockedNodeInformation1);
        when(mockedNode2.getNodeInformation()).thenReturn(mockedNodeInformation2);
        when(mockedNode3.getNodeInformation()).thenReturn(mockedNodeInformation3);
        when(mockedNode4.getNodeInformation()).thenReturn(mockedNodeInformation4);
        when(mockedNode5.getNodeInformation()).thenReturn(mockedNodeInformation5);
        
        when(mockedNodeInformation1.getURL()).thenReturn("mockedRemovableNode");
        when(mockedNodeInformation2.getURL()).thenReturn("mockedUnremovableNode");
        when(mockedNodeInformation3.getURL()).thenReturn(RMDeployingNode.PROTOCOL_ID + "://removableNode");
        when(mockedNodeInformation4.getURL()).thenReturn(RMDeployingNode.PROTOCOL_ID + "://unRemovableNode");
        when(mockedNodeInformation5.getURL()).thenReturn("mockedBusyNode");
        
        when(mockedCaller.checkPermission(Matchers.any(Permission.class), Matchers.any(String.class))).thenReturn(true);
        
        when(mockedRemovableNodeInDeploy.isDown()).thenReturn(true);
        when(mockedUnremovableNodeInDeploy.isDown()).thenReturn(false);
        when(mockedRemovableNode.isDown()).thenReturn(true);
        when(mockedUnremovableNode.isDown()).thenReturn(true);
        when(mockedBusyNode.isDown()).thenReturn(false);
        
        when(mockedRemovableNodeInDeploy.getAdminPermission()).thenReturn(null);
        when(mockedRemovableNode.getAdminPermission()).thenReturn(null);
        
        when(mockedRemovableNodeInDeploy.getNodeSource()).thenReturn(mockedNodeSource);
        when(mockedUnremovableNodeInDeploy.getNodeSource()).thenReturn(mockedNodeSource);
        when(mockedRemovableNode.getNodeSource()).thenReturn(mockedNodeSource);
        when(mockedUnremovableNode.getNodeSource()).thenReturn(mockedNodeSource);
        when(mockedBusyNode.getNodeSource()).thenReturn(mockedNodeSource);
        
        when(mockedRemovableNode.getNode()).thenReturn(mockedNode1);
        when(mockedUnremovableNode.getNode()).thenReturn(mockedNode2);
        when(mockedRemovableNodeInDeploy.getNode()).thenReturn(mockedNode3);
        when(mockedUnremovableNodeInDeploy.getNode()).thenReturn(mockedNode4);
        when(mockedBusyNode.getNode()).thenReturn(mockedNode5);
        
        when(mockedRemovableNode.isFree()).thenReturn(true);
        when(mockedBusyNode.isFree()).thenReturn(false);
        when(mockedBusyNode.isLocked()).thenReturn(true);
        
        when(mockedSelectionManager.selectNodes(Matchers.any(Criteria.class), Matchers.any(Client.class))).thenReturn(new NodeSet());
        
        rmCore.setMonitoring(mockedMonitoring);
        rmCore.setCaller(mockedCaller);
        rmCore.setNodeSources(new HashMap<String, NodeSource>());
        rmCore.setBrokenNodeSources(new ArrayList<String>());
        rmCore.setSelectionManager(mockedSelectionManager);
        rmCore.setDataBaseManager(dataBaseManager);
        
        HashMap<String, RMNode> nodes = new HashMap<String, RMNode>(5);
        nodes.put(RMDeployingNode.PROTOCOL_ID + "://removableNode", mockedRemovableNodeInDeploy);
        nodes.put(RMDeployingNode.PROTOCOL_ID + "://unRemovableNode", mockedUnremovableNodeInDeploy);
        nodes.put("mockedRemovableNode", mockedRemovableNode);
        nodes.put("mockedUnremovableNode", mockedUnremovableNode);
        nodes.put("mockedBusyNode", mockedBusyNode);
        rmCore.setAllNodes(nodes);
        
        ArrayList<RMNode> freeNodes = new ArrayList<RMNode>(2);
        freeNodes.add(mockedRemovableNodeInDeploy);
        freeNodes.add(mockedRemovableNode);
        rmCore.setFreeNodesList(freeNodes);
        
    }
}
