package org.ow2.proactive.resourcemanager.rmnode;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Collections.emptySet;

import java.io.Serializable;
import java.security.Permission;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.authentication.principals.UserNamePrincipal;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;


/**
 * @author ActiveEon Team
 * @since 03/02/17
 */
public class AbstractRMNodeTest {

    private Client client;

    private BasicRMNode rmNode;

    @Before
    public void setUp() {
        client = new Client(null, false);
        rmNode = new BasicRMNode("1");
    }

    @Test
    public void testCopyLockStatusFromSameNode() {
        rmNode.lock(client);
        assertThat(rmNode.isLocked()).isTrue();

        boolean oldLockStatus = rmNode.isLocked();
        Client oldLockedBy = rmNode.getLockedBy();
        long oldLockTime = rmNode.getLockTime();

        rmNode.copyLockStatusFrom(rmNode);

        assertThat(rmNode.isLocked()).isEqualTo(oldLockStatus);
        assertThat(rmNode.getLockedBy()).isEqualTo(oldLockedBy);
        assertThat(rmNode.getLockTime()).isEqualTo(oldLockTime);
    }

    @Test
    public void testCopyLockStatusFromUsingNodesHavingDifferentLockStatuses() throws InterruptedException {
        rmNode.unlock(client);
        assertThat(rmNode.isLocked()).isFalse();

        TimeUnit.MILLISECONDS.sleep(1);

        BasicRMNode rmNode2 = new BasicRMNode("2");
        rmNode2.lock(client);
        assertThat(rmNode2.isLocked()).isTrue();

        rmNode.copyLockStatusFrom(rmNode2);
        assertThat(rmNode.isLocked()).isTrue();
        assertThat(rmNode.isLocked()).isEqualTo(rmNode2.isLocked);
        assertThat(rmNode.getLockedBy()).isEqualTo(rmNode2.getLockedBy());
        assertThat(rmNode.getLockTime()).isEqualTo(rmNode2.getLockTime());
    }

    @Test
    public void testCopyLockStatusFromUsingNodesHavingSameLockStatuses() throws InterruptedException {
        rmNode.lock(client);
        assertThat(rmNode.isLocked()).isTrue();

        TimeUnit.MILLISECONDS.sleep(1);

        BasicRMNode rmNode2 = new BasicRMNode("2");
        rmNode2.lock(createClient("2"));
        assertThat(rmNode2.isLocked()).isTrue();

        rmNode.copyLockStatusFrom(rmNode2);
        assertThat(rmNode.isLocked()).isTrue();
        assertThat(rmNode2.isLocked()).isTrue();
        assertThat(rmNode.isLocked()).isEqualTo(rmNode2.isLocked);
        assertThat(rmNode.getLockedBy()).isNotEqualTo(rmNode2.getLockedBy());
        assertThat(rmNode.getLockTime()).isNotEqualTo(rmNode2.getLockTime());
    }

    private Client createClient(String name) {
        Set<Principal> principals = new HashSet<>(1, 1f);
        principals.add(new UserNamePrincipal(name));

        return new Client(new Subject(false, principals, emptySet(), emptySet()), false);
    }

    @Test
    public void testLock() {
        assertThat(rmNode.isLocked()).isFalse();
        assertThat(rmNode.getLockTime()).isEqualTo(-1L);
        assertThat(rmNode.getLockedBy()).isEqualTo(null);

        rmNode.lock(client);

        assertThat(rmNode.isLocked()).isTrue();
        assertThat(rmNode.getLockTime()).isGreaterThan(0L);
        assertThat(rmNode.getLockedBy()).isEqualTo(client);
    }

    @Test
    public void testUnlock() {
        testLock();

        rmNode.unlock(client);

        assertThat(rmNode.isLocked()).isFalse();
        assertThat(rmNode.getLockTime()).isEqualTo(-1L);
        assertThat(rmNode.getLockedBy()).isEqualTo(null);
    }

    @Test
    public void testToString() {
        rmNode = Mockito.spy(rmNode);
        rmNode.toString();

        Mockito.verify(rmNode).getNodeInfo();
    }

    @Test
    public void testEqualsReflexivity() {
        assertThat(rmNode).isEqualTo(rmNode);
    }

    @Test
    public void testEqualsSymmetric() {
        BasicRMNode rmNode2 = new BasicRMNode("1");
        assertThat(rmNode).isEqualTo(rmNode2);
        assertThat(rmNode2).isEqualTo(rmNode);
    }

    @Test
    public void testEqualsTransitive() {
        BasicRMNode rmNode2 = new BasicRMNode("1");
        BasicRMNode rmNode3 = new BasicRMNode("1");

        assertThat(rmNode).isEqualTo(rmNode2);
        assertThat(rmNode2).isEqualTo(rmNode3);
        assertThat(rmNode3).isEqualTo(rmNode);
    }

    @Test
    public void testEqualsNullComparison() {
        assertThat(rmNode.equals(null)).isFalse();
    }

    @Test
    public void testHashCodeSameExpected() {
        assertThat(rmNode.hashCode()).isEqualTo(new BasicRMNode("1").hashCode());
    }

    @Test
    public void testHashCodeNotSameExpected() {
        assertThat(rmNode.hashCode()).isNotEqualTo(new BasicRMNode("2").hashCode());
    }

    private static class BasicRMNode extends AbstractRMNode {

        public BasicRMNode(String name) {
            super(null, name, "url" + name, null);
        }

        @Override
        public <T> ScriptResult<T> executeScript(Script<T> script, Map<String, Serializable> bindings) {
            return null;
        }

        @Override
        public HashMap<SelectionScript, Integer> getScriptStatus() {
            return null;
        }

        @Override
        public void clean() throws NodeException {

        }

        @Override
        public String getNodeInfo() {
            return null;
        }

        @Override
        public Node getNode() {
            return null;
        }

        @Override
        public String getVNodeName() {
            return null;
        }

        @Override
        public String getHostName() {
            return null;
        }

        @Override
        public String getDescriptorVMName() {
            return null;
        }

        @Override
        public Client getOwner() {
            return null;
        }

        @Override
        public Permission getUserPermission() {
            return null;
        }

        @Override
        public Permission getAdminPermission() {
            return null;
        }

        @Override
        public boolean isDeploying() {
            return false;
        }

        @Override
        public boolean isFree() {
            return false;
        }

        @Override
        public boolean isDown() {
            return false;
        }

        @Override
        public boolean isToRemove() {
            return false;
        }

        @Override
        public boolean isBusy() {
            return false;
        }

        @Override
        public boolean isConfiguring() {
            return false;
        }

        @Override
        public void setFree() {

        }

        @Override
        public void setBusy(Client owner) {

        }

        @Override
        public void setToRemove() {

        }

        @Override
        public void setDown() {

        }

        @Override
        public void setConfiguring(Client owner) {

        }

        @Override
        public void setJMXUrl(JMXTransportProtocol protocol, String address) {

        }

        @Override
        public String getJMXUrl(JMXTransportProtocol protocol) {
            return null;
        }

        @Override
        public boolean isProtectedByToken() {
            return false;
        }

        @Override
        public int compareTo(RMNode o) {
            return 0;
        }

    }

}
