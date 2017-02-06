package org.ow2.proactive.resourcemanager.rmnode;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.NodeState;


/**
 * @author ActiveEon Team
 * @since 18/01/17
 */
public class RMNodeImplTest {

    private Client client;

    private RMNodeImpl rmNode;

    // Mocked ProActive node used by rmNode
    private Node node;

    @Before
    public void setUp() {
        client = Mockito.mock(Client.class);

        Pair<RMNodeImpl, Node> pair = RMNodeHelper.basicWithMockedInternals();
        rmNode = pair.getLeft();
        node = pair.getRight();
    }

    @Test
    public void testClean() throws IOException, NodeException {
        rmNode.clean();
        Mockito.verify(node).killAllActiveObjects();
    }

    @Test
    public void testCompareToEquals() throws Exception {
        RMNodeImpl rmNode2 = RMNodeHelper.basicWithMockedInternals().getLeft();
        assertThat(rmNode.compareTo(rmNode2)).isEqualTo(0);
        assertThat(rmNode.compareTo(rmNode2)).isEqualTo(rmNode.getNodeURL().compareTo(rmNode2.getNodeURL()));
    }

    @Test
    public void testCompareToDifferentVNodeNames() throws Exception {
        RMNodeImpl rmNode2 = RMNodeHelper.basicWithMockedInternals().getLeft();
        rmNode2 = Mockito.spy(rmNode2);

        when(rmNode2.getVNodeName()).thenReturn("vNodeName2");

        assertThat(rmNode.getVNodeName()).isNotNull();
        assertThat(rmNode2.getVNodeName()).isNotNull();

        int nodesComparison = rmNode.compareTo(rmNode2);
        assertThat(nodesComparison).isNotEqualTo(0);
        assertThat(nodesComparison).isEqualTo(rmNode.getVNodeName().compareTo(rmNode2.getVNodeName()));
    }

    @Test
    public void testCompareToDifferentHostNames() throws Exception {
        RMNodeImpl rmNode2 = RMNodeHelper.basicWithMockedInternals("name",
                "hostName2",
                "nodeInformationUrl",
                "proactiveRuntimeUrl")
                .getLeft();

        int nodesComparison = rmNode.compareTo(rmNode2);
        assertThat(nodesComparison).isNotEqualTo(0);
        assertThat(nodesComparison).isEqualTo(rmNode.getHostName().compareTo(rmNode2.getHostName()));
    }

    @Test
    public void testCompareToDifferentDescriptorVMNames() throws Exception {
        RMNodeImpl rmNode2 = RMNodeHelper.basicWithMockedInternals("name",
                "hostName",
                "nodeInformationUrl",
                "proactiveRuntimeUrl2")
                .getLeft();

        int nodesComparison = rmNode.compareTo(rmNode2);
        assertThat(nodesComparison).isNotEqualTo(0);
        assertThat(nodesComparison).isEqualTo(rmNode.getDescriptorVMName().compareTo(rmNode2.getDescriptorVMName()));
    }

    @Test
    public void testConstructor() {
        assertThat(rmNode.isProtectedByToken()).isFalse();
        assertThat(rmNode.getScriptStatus()).isEmpty();
        assertThat(rmNode.getState()).isEqualTo(NodeState.FREE);
    }

    @Test
    public void testGetAdminPermission() throws Exception {
        assertThat(rmNode.getAdminPermission()).isNotSameAs(rmNode.getAdminPermission());
    }

    @Test
    public void testGetNodeInfo() {
        assertThat(rmNode.getNodeInfo()).contains("State: ");
        assertThat(rmNode.getNodeInfo()).contains("Locked: false");

        rmNode.lock(rmNode.getProvider());

        assertThat(rmNode.getNodeInfo()).contains("Locked: true ");
    }

    @Test
    public void testIsBusyFalse() {
        rmNode.setState(NodeState.TO_BE_REMOVED);
        assertThat(rmNode.isBusy()).isFalse();
    }

    @Test
    public void testIsBusyTrue() {
        rmNode.setState(NodeState.BUSY);
        assertThat(rmNode.isBusy()).isTrue();
    }

    @Test
    public void testIsConfiguringFalse() {
        rmNode.setState(NodeState.BUSY);
        assertThat(rmNode.isConfiguring()).isFalse();
    }

    @Test
    public void testIsConfiguringTrue() {
        rmNode.setState(NodeState.CONFIGURING);
        assertThat(rmNode.isConfiguring()).isTrue();
    }

    @Test
    public void testIsDeploying() {
        assertThat(rmNode.isDeploying()).isFalse();
    }

    @Test
    public void testIsDownFalse() {
        rmNode.setState(NodeState.DEPLOYING);
        assertThat(rmNode.isDown()).isFalse();
    }

    @Test
    public void testIsDownTrue() {
        rmNode.setState(NodeState.DOWN);
        assertThat(rmNode.isDown()).isTrue();
    }

    @Test
    public void testIsFreeFalse() {
        rmNode.setState(NodeState.DOWN);
        assertThat(rmNode.isFree()).isFalse();
    }

    @Test
    public void testIsFreeTrue() {
        rmNode.setState(NodeState.FREE);
        assertThat(rmNode.isFree()).isTrue();
    }

    @Test
    public void testIsToRemoveFalse() {
        rmNode.setState(NodeState.LOST);
        assertThat(rmNode.isToRemove()).isFalse();
    }

    @Test
    public void testIsToRemoveTrue() {
        rmNode.setState(NodeState.TO_BE_REMOVED);
        assertThat(rmNode.isToRemove()).isTrue();
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
    public void testSetBusy() throws InterruptedException {
        long stateChangeTime = rmNode.getStateChangeTime();

        assertThat(rmNode.getOwner()).isNotEqualTo(client);
        assertThat(rmNode.isBusy()).isFalse();
        assertThat(rmNode.getState()).isNotEqualTo(NodeState.BUSY);

        TimeUnit.MILLISECONDS.sleep(1);
        rmNode.setBusy(client);

        assertThat(rmNode.getOwner()).isEqualTo(client);
        assertThat(rmNode.isBusy()).isTrue();
        assertThat(rmNode.getState()).isEqualTo(NodeState.BUSY);
        assertThat(rmNode.getStateChangeTime()).isGreaterThan(stateChangeTime);
    }

    @Test
    public void testSetConfiguring() throws InterruptedException {
        Client owner = rmNode.getOwner();
        long stateChangeTime = rmNode.getStateChangeTime();

        assertThat(owner).isNotEqualTo(client);
        assertThat(rmNode.isConfiguring()).isFalse();
        assertThat(rmNode.getState()).isNotEqualTo(NodeState.CONFIGURING);
        assertThat(rmNode.getState()).isNotEqualTo(NodeState.DOWN);

        TimeUnit.MILLISECONDS.sleep(1);
        rmNode.setConfiguring(client);

        // Not sure to understand why Client instance is passed to setConfiguring since
        // the internal value is not updated based on the parameter. However, the
        // behaviour has not changed since years, so there might be some reasons (or not)
        assertThat(owner).isEqualTo(owner);
        assertThat(rmNode.isConfiguring()).isTrue();
        assertThat(rmNode.getState()).isEqualTo(NodeState.CONFIGURING);
        assertThat(rmNode.getStateChangeTime()).isGreaterThan(stateChangeTime);
    }

    @Test
    public void testSetConfiguringRmNodeIniatiallyDown() {
        rmNode.setDown();

        Client owner = rmNode.getOwner();
        long stateChangeTime = rmNode.getStateChangeTime();

        assertThat(owner).isNotEqualTo(client);
        assertThat(rmNode.isConfiguring()).isFalse();
        assertThat(rmNode.getState()).isEqualTo(NodeState.DOWN);

        rmNode.setConfiguring(client);

        assertThat(owner).isEqualTo(owner);
        assertThat(rmNode.isConfiguring()).isFalse();
        assertThat(rmNode.getState()).isEqualTo(NodeState.DOWN);
        assertThat(rmNode.getStateChangeTime()).isEqualTo(stateChangeTime);
    }

    @Test
    public void testSetDown() throws InterruptedException {
        long stateChangeTime = rmNode.getStateChangeTime();

        assertThat(rmNode.isDown()).isFalse();
        assertThat(rmNode.getState()).isNotEqualTo(NodeState.DOWN);

        TimeUnit.MILLISECONDS.sleep(1);
        rmNode.setDown();

        assertThat(rmNode.isDown()).isTrue();
        assertThat(rmNode.getState()).isEqualTo(NodeState.DOWN);
        assertThat(rmNode.getStateChangeTime()).isGreaterThan(stateChangeTime);
    }

    @Test
    public void testSetFree() throws InterruptedException {
        long stateChangeTime = rmNode.getStateChangeTime();

        rmNode.setState(NodeState.DOWN);
        assertThat(rmNode.isFree()).isFalse();
        assertThat(rmNode.getState()).isNotEqualTo(NodeState.FREE);

        TimeUnit.MILLISECONDS.sleep(1);
        rmNode.setFree();

        assertThat(rmNode.isFree()).isTrue();
        assertThat(rmNode.getState()).isEqualTo(NodeState.FREE);
        assertThat(rmNode.getStateChangeTime()).isGreaterThan(stateChangeTime);
    }

    @Test
    public void testSetJMXUrlRmiProtocol() throws Exception {
        assertThat(rmNode.getJMXUrl(JMXTransportProtocol.RMI)).isNull();
        rmNode.setJMXUrl(JMXTransportProtocol.RMI, "aeiouy");
        assertThat(rmNode.getJMXUrl(JMXTransportProtocol.RMI)).isEqualTo("aeiouy");
        rmNode.setJMXUrl(JMXTransportProtocol.RMI, "ho ho ho");
        assertThat(rmNode.getJMXUrl(JMXTransportProtocol.RMI)).isEqualTo("ho ho ho");
        assertThat(rmNode.getJMXUrl(JMXTransportProtocol.RO)).isNull();
    }

    @Test
    public void testSetJMXUrlRoProtocol() throws Exception {
        assertThat(rmNode.getJMXUrl(JMXTransportProtocol.RO)).isNull();
        rmNode.setJMXUrl(JMXTransportProtocol.RO, "aeiouy");
        assertThat(rmNode.getJMXUrl(JMXTransportProtocol.RO)).isEqualTo("aeiouy");
        rmNode.setJMXUrl(JMXTransportProtocol.RO, "ho ho ho");
        assertThat(rmNode.getJMXUrl(JMXTransportProtocol.RO)).isEqualTo("ho ho ho");
        assertThat(rmNode.getJMXUrl(JMXTransportProtocol.RMI)).isNull();
    }

    @Test
    public void testSetProtectedByToken() throws Exception {
        assertThat(rmNode.isProtectedByToken()).isFalse();
        rmNode.setProtectedByToken(true);
        assertThat(rmNode.isProtectedByToken()).isTrue();
        rmNode.setProtectedByToken(false);
        assertThat(rmNode.isProtectedByToken()).isFalse();
    }

    @Test
    public void testSetState() {
        assertThat(rmNode.getState()).isNotEqualTo(NodeState.DOWN);
        rmNode.setState(NodeState.DOWN);
        assertThat(rmNode.getState()).isEqualTo(NodeState.DOWN);
    }

    @Test
    public void testSetStateIdempotent() {
        assertThat(rmNode.getState()).isEqualTo(NodeState.FREE);
        rmNode.setState(NodeState.FREE);
        assertThat(rmNode.getState()).isEqualTo(NodeState.FREE);
    }

    @Test
    public void testSetToRemove() throws InterruptedException {
        long stateChangeTime = rmNode.getStateChangeTime();

        assertThat(rmNode.isToRemove()).isFalse();
        assertThat(rmNode.getState()).isNotEqualTo(NodeState.TO_BE_REMOVED);

        TimeUnit.MILLISECONDS.sleep(1);
        rmNode.setToRemove();

        assertThat(rmNode.isToRemove()).isTrue();
        assertThat(rmNode.getState()).isEqualTo(NodeState.TO_BE_REMOVED);
        assertThat(rmNode.getStateChangeTime()).isGreaterThan(stateChangeTime);
    }

    @Test
    public void testUnlock() {
        rmNode.lock(rmNode.getProvider());
        rmNode.unlock(rmNode.getProvider());

        assertThat(rmNode.isLocked()).isFalse();
        assertThat(rmNode.getLockedBy()).isNull();
        assertThat(rmNode.getLockTime()).isEqualTo(-1L);
    }

}
