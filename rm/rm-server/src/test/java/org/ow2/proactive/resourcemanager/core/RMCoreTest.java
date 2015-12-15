package org.ow2.proactive.resourcemanager.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeInformation;
import org.ow2.proactive.resourcemanager.authentication.Client;
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
    private Client mockedCaller;
    private RMNode mockedRemovableNodeInDeploy;
    private RMNode mockedUnremovableNodeInDeploy;
    private RMNode mockedRemovableNode;
    private RMNode mockedUnremovableNode;
    
    
    @Before
    public void setUp() {
        rmCore = new RMCore();
        mockedRemovableNodeInDeploy = Mockito.mock(RMNode.class);
        mockedUnremovableNodeInDeploy = Mockito.mock(RMNode.class);
        mockedRemovableNode = Mockito.mock(RMNode.class);
        mockedUnremovableNode = Mockito.mock(RMNode.class);
        NodeSource mockedNodeSource = Mockito.mock(NodeSource.class);
        mockedCaller = Mockito.mock(Client.class);
        RMMonitoringImpl mockedMonitoring = Mockito.mock(RMMonitoringImpl.class);
        SelectionManager mockedSelectionManager = Mockito.mock(SelectionManager.class);
        Node mockedNode1 = Mockito.mock(Node.class);
        Node mockedNode2 = Mockito.mock(Node.class);
        Node mockedNode3 = Mockito.mock(Node.class);
        Node mockedNode4 = Mockito.mock(Node.class);
        NodeInformation mockedNodeInformation1 = Mockito.mock(NodeInformation.class);
        NodeInformation mockedNodeInformation2 = Mockito.mock(NodeInformation.class);
        NodeInformation mockedNodeInformation3 = Mockito.mock(NodeInformation.class);
        NodeInformation mockedNodeInformation4 = Mockito.mock(NodeInformation.class);
        
        when(mockedNode1.getNodeInformation()).thenReturn(mockedNodeInformation1);
        when(mockedNode2.getNodeInformation()).thenReturn(mockedNodeInformation2);
        when(mockedNode3.getNodeInformation()).thenReturn(mockedNodeInformation3);
        when(mockedNode4.getNodeInformation()).thenReturn(mockedNodeInformation4);
        
        when(mockedNodeInformation1.getURL()).thenReturn("mockedRemovableNode");
        when(mockedNodeInformation2.getURL()).thenReturn("mockedUnremovableNode");
        when(mockedNodeInformation3.getURL()).thenReturn(RMDeployingNode.PROTOCOL_ID + "://removableNode");
        when(mockedNodeInformation4.getURL()).thenReturn(RMDeployingNode.PROTOCOL_ID + "://unRemovableNode");
        
        when(mockedCaller.checkPermission(Matchers.any(Permission.class), Matchers.any(String.class))).thenReturn(true);
        when(mockedRemovableNodeInDeploy.isDown()).thenReturn(true);
        when(mockedUnremovableNodeInDeploy.isDown()).thenReturn(false);
        when(mockedRemovableNode.isDown()).thenReturn(true);
        when(mockedUnremovableNode.isDown()).thenReturn(true);
        
        when(mockedRemovableNodeInDeploy.getAdminPermission()).thenReturn(null);
        when(mockedRemovableNode.getAdminPermission()).thenReturn(null);
        
        when(mockedRemovableNodeInDeploy.getNodeSource()).thenReturn(mockedNodeSource);
        when(mockedUnremovableNodeInDeploy.getNodeSource()).thenReturn(mockedNodeSource);
        when(mockedRemovableNode.getNodeSource()).thenReturn(mockedNodeSource);
        when(mockedUnremovableNode.getNodeSource()).thenReturn(mockedNodeSource);
        
        when(mockedRemovableNode.getNode()).thenReturn(mockedNode1);
        when(mockedUnremovableNode.getNode()).thenReturn(mockedNode2);
        when(mockedRemovableNodeInDeploy.getNode()).thenReturn(mockedNode3);
        when(mockedUnremovableNodeInDeploy.getNode()).thenReturn(mockedNode4);
        
        //selectionManager.selectNodes(criteria, caller);
        when(mockedSelectionManager.selectNodes(Matchers.any(Criteria.class), Matchers.any(Client.class))).thenReturn(new NodeSet());
        
        rmCore.setMonitoring(mockedMonitoring);
        rmCore.setCaller(mockedCaller);
        rmCore.setNodeSources(new HashMap<String, NodeSource>());
        rmCore.setBrokenNodeSources(new ArrayList<String>());
        rmCore.setSelectionManager(mockedSelectionManager);
        HashMap<String, RMNode> nodes = new HashMap<String, RMNode>();
        nodes.put(RMDeployingNode.PROTOCOL_ID + "://removableNode", mockedRemovableNodeInDeploy);
        nodes.put(RMDeployingNode.PROTOCOL_ID + "://unRemovableNode", mockedUnremovableNodeInDeploy);
        nodes.put("mockedRemovableNode", mockedRemovableNode);
        nodes.put("mockedUnremovableNode", mockedUnremovableNode);
        rmCore.setAllNodes(nodes);
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
        rmCore.getNodes(1, TopologyDescriptor.ARBITRARY, null, null, false);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testGetNodesBadNodesNumber() {
        rmCore.getNodes(-1, TopologyDescriptor.ARBITRARY, null, null, false);
        
    }
    
}
