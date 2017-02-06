package org.ow2.proactive.resourcemanager.core;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.objectweb.proactive.core.util.MutableInteger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.rmnode.RMNodeHelper;
import org.ow2.proactive.resourcemanager.rmnode.RMNodeImpl;

import com.google.common.collect.HashBasedTable;


/**
 * @author ActiveEon Team
 * @since 06/02/17
 */
public class NodesLockRestorationManagerTest {

    private NodesLockRestorationManager nodesLockRestorationManager;

    private RMCore rmCore;

    @Before
    public void setUp() {
        rmCore = mock(RMCore.class);

        nodesLockRestorationManager = Mockito.spy(new NodesLockRestorationManager(rmCore));

        HashBasedTable<String, String, MutableInteger> table = HashBasedTable.create();
        table.put("row", "column", new MutableInteger(42));
        doReturn(table).when(nodesLockRestorationManager).findNodesLockedOnPreviousRun();
    }

    @Test
    public void testHandleNotInitialized() throws Exception {

        // creates a node that matches an entry in the table specifying the nodes to lock

        RMNodeImpl rmNode = RMNodeHelper.basicWithMockedInternals("ns1", "n1", "h1", "nurl1", "parurl1").getLeft();
        assertThat(rmNode.isLocked()).isFalse();

        HashBasedTable<String, String, MutableInteger> table = HashBasedTable.create();
        MutableInteger putResult = table.put("ns1", "h1", new MutableInteger(1));
        assertThat(putResult).isNull();
        doReturn(table).when(nodesLockRestorationManager).findNodesLockedOnPreviousRun();

        assertThat(table).hasSize(1);
        verify(rmCore, never()).lockNodes(anySet());

        nodesLockRestorationManager.handle(rmNode);

        assertThat(table).hasSize(1);
        verify(rmCore, never()).lockNodes(anySet());
    }

    @Test
    public void testHandleNodeAlreadyLocked() throws Exception {
        RMNodeImpl rmNode = RMNodeHelper.basicWithMockedInternals("ns1", "n1", "h1", "nurl1", "parurl1").getLeft();
        rmNode.lock(null);
        assertThat(rmNode.isLocked()).isTrue();

        HashBasedTable<String, String, MutableInteger> table = HashBasedTable.create();
        MutableInteger putResult = table.put("ns1", "h1", new MutableInteger(1));
        assertThat(putResult).isNull();
        doReturn(table).when(nodesLockRestorationManager).findNodesLockedOnPreviousRun();

        nodesLockRestorationManager.initialize();
        assertThat(nodesLockRestorationManager.isInitialized()).isTrue();
        verify(nodesLockRestorationManager).findNodesLockedOnPreviousRun();

        assertThat(table).hasSize(1);
        verify(rmCore, never()).lockNodes(anySet());

        nodesLockRestorationManager.handle(rmNode);

        assertThat(table).hasSize(1);
        verify(rmCore, never()).lockNodes(anySet());
    }

    @Test
    public void testHandleNoNodesToLock() throws Exception {
        RMNodeImpl rmNode = RMNodeHelper.basicWithMockedInternals("ns1", "n1", "h1", "nurl1", "parurl1").getLeft();
        assertThat(rmNode.isLocked()).isFalse();

        HashBasedTable<String, String, MutableInteger> table = HashBasedTable.create();
        doReturn(table).when(nodesLockRestorationManager).findNodesLockedOnPreviousRun();

        nodesLockRestorationManager.initialize();
        assertThat(nodesLockRestorationManager.isInitialized()).isTrue();
        verify(nodesLockRestorationManager).findNodesLockedOnPreviousRun();

        assertThat(table).hasSize(0);
        verify(rmCore, never()).lockNodes(anySet());

        nodesLockRestorationManager.handle(rmNode);

        assertThat(table).hasSize(0);
        verify(rmCore, never()).lockNodes(anySet());
    }

    @Test
    public void testHandleMatchingNode() throws Exception {
        doReturn(new BooleanWrapper(true)).when(rmCore).lockNodes(anySet());

        RMNodeImpl rmNode = RMNodeHelper.basicWithMockedInternals("ns1", "n1", "h1", "nurl1", "parurl1").getLeft();
        assertThat(rmNode.isLocked()).isFalse();

        HashBasedTable<String, String, MutableInteger> table = HashBasedTable.create();
        MutableInteger putResult = table.put("ns1", "h1", new MutableInteger(1));
        assertThat(putResult).isNull();
        doReturn(table).when(nodesLockRestorationManager).findNodesLockedOnPreviousRun();

        assertThat(table).hasSize(1);
        verify(rmCore, never()).lockNodes(anySet());

        nodesLockRestorationManager.initialize();
        assertThat(nodesLockRestorationManager.isInitialized()).isTrue();
        verify(nodesLockRestorationManager).findNodesLockedOnPreviousRun();

        nodesLockRestorationManager.handle(rmNode);

        assertThat(table).hasSize(0);
        verify(rmCore).lockNodes(anySet());
    }

    @Test
    public void testHandleNonMatchingNode() throws Exception {
        RMNodeImpl rmNode = RMNodeHelper.basicWithMockedInternals("ns2", "n1", "h1", "nurl1", "parurl1").getLeft();
        assertThat(rmNode.isLocked()).isFalse();

        HashBasedTable<String, String, MutableInteger> table = HashBasedTable.create();
        MutableInteger putResult = table.put("ns1", "h1", new MutableInteger(1));
        assertThat(putResult).isNull();
        doReturn(table).when(nodesLockRestorationManager).findNodesLockedOnPreviousRun();

        assertThat(table).hasSize(1);
        verify(rmCore, never()).lockNodes(anySet());

        nodesLockRestorationManager.initialize();
        assertThat(nodesLockRestorationManager.isInitialized()).isTrue();
        verify(nodesLockRestorationManager).findNodesLockedOnPreviousRun();

        nodesLockRestorationManager.handle(rmNode);

        assertThat(table).hasSize(1);
        verify(rmCore, never()).lockNodes(anySet());
    }

    @Test
    public void testInitialize() throws Exception {
        assertThat(nodesLockRestorationManager.isInitialized()).isFalse();
        assertThat(nodesLockRestorationManager.getNodeLockedOnPreviousRun()).isEmpty();

        nodesLockRestorationManager.initialize();

        assertThat(nodesLockRestorationManager.isInitialized()).isTrue();
        assertThat(nodesLockRestorationManager.getNodeLockedOnPreviousRun()).hasSize(1);
    }

    @Test
    public void testIsRestorationCompleted() throws Exception {
        assertThat(nodesLockRestorationManager.isRestorationCompleted()).isFalse();

        nodesLockRestorationManager.initialize();

        nodesLockRestorationManager.nodeLockedOnPreviousRun.clear();

        assertThat(nodesLockRestorationManager.isRestorationCompleted()).isTrue();
    }

    @Test
    public void testLockNodeSucceeds() throws Exception {
        doReturn(new BooleanWrapper(true)).when(rmCore).lockNodes(anySet());
        verify(rmCore, never()).lockNodes(anySet());

        RMNode rmNode = RMNodeHelper.basicWithMockedInternals().getLeft();
        boolean lockResult = nodesLockRestorationManager.lockNode(rmNode);

        assertThat(lockResult).isTrue();
        verify(rmCore).lockNodes(anySet());
    }

    @Test
    public void testLockNodeFails() throws Exception {
        doReturn(new BooleanWrapper(false)).when(rmCore).lockNodes(anySet());
        verify(rmCore, never()).lockNodes(anySet());

        RMNode rmNode = RMNodeHelper.basicWithMockedInternals().getLeft();
        boolean lockResult = nodesLockRestorationManager.lockNode(rmNode);

        assertThat(lockResult).isFalse();
        verify(rmCore).lockNodes(anySet());
    }

}
