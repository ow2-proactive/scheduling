package org.ow2.proactive.resourcemanager.rmnode;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import java.security.Permission;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;

/**
 * @author ActiveEon Team
 * @since 18/01/17
 */
public class RMNodeImplTest {

    private RMNodeImpl rmNode;

    @Before
    public void setUp() {
        rmNode = createRmNode();
    }

    @Test
    public void testLock() {
        assertThat(rmNode.isLocked()).isFalse();
        assertThat(rmNode.getLockedBy()).isNull();
        assertThat(rmNode.getLockTime()).isEqualTo(-1);

        rmNode.lock(rmNode.getProvider());

        assertThat(rmNode.isLocked()).isTrue();
        assertThat(rmNode.getLockedBy()).isEqualTo(rmNode.getProvider());
        assertThat(rmNode.getLockTime()).isGreaterThan(1L);
    }

    @Test
    public void testUnlock() {
        rmNode.lock(rmNode.getProvider());
        rmNode.unlock(rmNode.getProvider());

        assertThat(rmNode.isLocked()).isFalse();
        assertThat(rmNode.getLockedBy()).isNull();
        assertThat(rmNode.getLockTime()).isEqualTo(-1L);
    }

    @Test
    public void testGetNodeInfo() {
        assertThat(rmNode.getNodeInfo()).contains("State: ");
        assertThat(rmNode.getNodeInfo()).contains("Locked: false");

        rmNode.lock(rmNode.getProvider());

        assertThat(rmNode.getNodeInfo()).contains("Locked: true ");
    }

    public static RMNodeImpl createRmNode() {
        Node node = Mockito.mock(Node.class);
        NodeSource nodeSource = Mockito.mock(NodeSource.class);
        Client provider = Mockito.mock(Client.class);
        Permission permission = Mockito.mock(Permission.class);

        NodeInformation nodeInformation = Mockito.mock(NodeInformation.class);
        VMInformation vmInformation = Mockito.mock(VMInformation.class);
        ProActiveRuntime proActiveRuntime = Mockito.mock(ProActiveRuntime.class);

        when(nodeSource.getName()).thenReturn("nodeSourceName");
        when(node.getNodeInformation()).thenReturn(nodeInformation);
        when(nodeInformation.getName()).thenReturn("name");
        when(nodeInformation.getURL()).thenReturn("nodeInformationUrl");
        when(nodeInformation.getVMInformation()).thenReturn(vmInformation);
        when(vmInformation.getHostName()).thenReturn("hostName");
        when(node.getProActiveRuntime()).thenReturn(proActiveRuntime);
        when(proActiveRuntime.getURL()).thenReturn("proactiveRuntimeUrl");

        return new RMNodeImpl(node, nodeSource, provider, permission);
    }

}