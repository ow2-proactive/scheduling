package org.ow2.proactive.resourcemanager.core;

import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.objectweb.proactive.core.util.MutableInteger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.db.RMDBManager;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.rmnode.RMNodeHelper;
import org.ow2.proactive.resourcemanager.rmnode.RMNodeImpl;

import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.*;


/**
 * @author ActiveEon Team
 * @since 06/02/17
 */
public class NodesLockRestorationManagerTest {

    private RMDBManager dbManager;

    private NodesLockRestorationManager nodesLockRestorationManager;

    private RMCore rmCore;

    @Before
    public void setUp() {
        dbManager = mock(RMDBManager.class);
        rmCore = mock(RMCore.class);

        nodesLockRestorationManager = Mockito.spy(new NodesLockRestorationManager(rmCore));

        doReturn(dbManager).when(rmCore).getDbManager();
    }

    @Test
    public void testConstructor() {
        assertThat(nodesLockRestorationManager.getNodeLockedOnPreviousRun()).isNotNull();
        assertThat(nodesLockRestorationManager.getNodeLockedOnPreviousRun()).isEmpty();
    }

    @Test
    public void testFindNodesLockedOnPreviousRun() {
        verify(dbManager, never()).findNodesLockedOnPreviousRun();
        verify(dbManager, never()).clearLockHistory();

        nodesLockRestorationManager.findNodesLockedOnPreviousRun();

        verify(dbManager).findNodesLockedOnPreviousRun();
        verify(dbManager).clearLockHistory();
    }

    @Test
    public void testHandleNotInitialized() {

        // creates a node that matches an entry in the table specifying the nodes to lock

        RMNodeImpl rmNode = RMNodeHelper.basicWithMockedInternals("ns1", "n1", "h1", "nurl1", "parurl1").getLeft();
        assertThat(rmNode.isLocked()).isFalse();

        Map<String, MutableInteger> table = Maps.newHashMap();
        MutableInteger putResult = table.put("ns", new MutableInteger(1));
        assertThat(putResult).isNull();
        doReturn(table).when(nodesLockRestorationManager).findNodesLockedOnPreviousRun();

        assertThat(table).hasSize(1);
        verify(rmCore, never()).lockNodes(anySetOf(String.class));

        nodesLockRestorationManager.handle(rmNode);

        assertThat(table).hasSize(1);
        verify(rmCore, never()).lockNodes(anySetOf(String.class));
    }

    @Test
    public void testHandleNodeAlreadyLocked() {
        RMNodeImpl rmNode = RMNodeHelper.basicWithMockedInternals("ns1", "n1", "h1", "nurl1", "parurl1").getLeft();
        rmNode.lock(null);
        assertThat(rmNode.isLocked()).isTrue();

        Map<String, MutableInteger> table = Maps.newHashMap();
        MutableInteger putResult = table.put("ns", new MutableInteger(1));
        assertThat(putResult).isNull();
        doReturn(table).when(nodesLockRestorationManager).findNodesLockedOnPreviousRun();

        nodesLockRestorationManager.initialize();
        assertThat(nodesLockRestorationManager.isInitialized()).isTrue();
        verify(nodesLockRestorationManager).findNodesLockedOnPreviousRun();

        assertThat(table).hasSize(1);
        verify(rmCore, never()).lockNodes(anySetOf(String.class));

        nodesLockRestorationManager.handle(rmNode);

        assertThat(table).hasSize(1);
        verify(rmCore, never()).lockNodes(anySetOf(String.class));
    }

    @Test
    public void testHandleNoNodesToLock() {
        RMNodeImpl rmNode = RMNodeHelper.basicWithMockedInternals("ns1", "n1", "h1", "nurl1", "parurl1").getLeft();
        assertThat(rmNode.isLocked()).isFalse();

        Map<String, MutableInteger> table = Maps.newHashMap();
        doReturn(table).when(nodesLockRestorationManager).findNodesLockedOnPreviousRun();

        nodesLockRestorationManager.initialize();
        assertThat(nodesLockRestorationManager.isInitialized()).isTrue();
        verify(nodesLockRestorationManager).findNodesLockedOnPreviousRun();

        assertThat(table).hasSize(0);
        verify(rmCore, never()).lockNodes(anySetOf(String.class));

        nodesLockRestorationManager.handle(rmNode);

        assertThat(table).hasSize(0);
        verify(rmCore, never()).lockNodes(anySetOf(String.class));
    }

    @Test
    public void testHandleMatchingNode() {
        doReturn(new BooleanWrapper(true)).when(rmCore).lockNodes(anySetOf(String.class));

        RMNodeImpl rmNode = RMNodeHelper.basicWithMockedInternals("ns1", "n1", "h1", "nurl1", "parurl1").getLeft();
        assertThat(rmNode.isLocked()).isFalse();

        Map<String, MutableInteger> table = Maps.newHashMap();
        MutableInteger putResult = table.put("ns1", new MutableInteger(1));
        assertThat(putResult).isNull();
        doReturn(table).when(nodesLockRestorationManager).findNodesLockedOnPreviousRun();

        assertThat(table).hasSize(1);
        verify(rmCore, never()).lockNodes(anySetOf(String.class));

        nodesLockRestorationManager.initialize();
        assertThat(nodesLockRestorationManager.isInitialized()).isTrue();
        verify(nodesLockRestorationManager).findNodesLockedOnPreviousRun();

        nodesLockRestorationManager.handle(rmNode);

        assertThat(table).hasSize(0);
        verify(rmCore).lockNodes(anySetOf(String.class));
    }

    @Test
    public void testHandleNonMatchingNode() {
        RMNodeImpl rmNode = RMNodeHelper.basicWithMockedInternals("ns2", "n1", "h1", "nurl1", "parurl1").getLeft();
        assertThat(rmNode.isLocked()).isFalse();

        Map<String, MutableInteger> table = Maps.newHashMap();
        MutableInteger putResult = table.put("ns1", new MutableInteger(1));
        assertThat(putResult).isNull();
        doReturn(table).when(nodesLockRestorationManager).findNodesLockedOnPreviousRun();

        assertThat(table).hasSize(1);
        verify(rmCore, never()).lockNodes(anySetOf(String.class));

        nodesLockRestorationManager.initialize();
        assertThat(nodesLockRestorationManager.isInitialized()).isTrue();
        verify(nodesLockRestorationManager).findNodesLockedOnPreviousRun();

        nodesLockRestorationManager.handle(rmNode);

        assertThat(table).hasSize(1);
        verify(rmCore, never()).lockNodes(anySetOf(String.class));
    }

    @Test
    public void testInitialize() {
        Map<String, MutableInteger> map = Maps.newHashMap();
        map.put("nodeSource", new MutableInteger(42));
        doReturn(map).when(nodesLockRestorationManager).findNodesLockedOnPreviousRun();

        assertThat(nodesLockRestorationManager.isInitialized()).isFalse();
        assertThat(nodesLockRestorationManager.getNodeLockedOnPreviousRun()).isEmpty();

        nodesLockRestorationManager.initialize();

        assertThat(nodesLockRestorationManager.isInitialized()).isTrue();
        assertThat(nodesLockRestorationManager.getNodeLockedOnPreviousRun()).hasSize(1);
    }

    @Test
    public void testIsRestorationCompleted() {
        assertThat(nodesLockRestorationManager.isRestorationCompleted()).isFalse();

        nodesLockRestorationManager.initialize();

        nodesLockRestorationManager.nodeLockedOnPreviousRun.clear();

        assertThat(nodesLockRestorationManager.isRestorationCompleted()).isTrue();
    }

    @Test
    public void testLockNodeSucceeds() {
        doReturn(new BooleanWrapper(true)).when(rmCore).lockNodes(anySetOf(String.class));
        verify(rmCore, never()).lockNodes(anySetOf(String.class));

        RMNode rmNode = RMNodeHelper.basicWithMockedInternals().getLeft();
        boolean lockResult = nodesLockRestorationManager.lockNode(rmNode);

        assertThat(lockResult).isTrue();
        verify(rmCore).lockNodes(anySetOf(String.class));
    }

    @Test
    public void testLockNodeFails() {
        doReturn(new BooleanWrapper(false)).when(rmCore).lockNodes(anySetOf(String.class));
        verify(rmCore, never()).lockNodes(anySetOf(String.class));

        RMNode rmNode = RMNodeHelper.basicWithMockedInternals().getLeft();
        boolean lockResult = nodesLockRestorationManager.lockNode(rmNode);

        assertThat(lockResult).isFalse();
        verify(rmCore).lockNodes(anySetOf(String.class));
    }

}
