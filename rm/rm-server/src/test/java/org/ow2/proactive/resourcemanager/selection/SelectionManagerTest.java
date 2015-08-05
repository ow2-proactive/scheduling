package org.ow2.proactive.resourcemanager.selection;

import java.security.Permission;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeInformation;
import org.ow2.proactive.authentication.principals.UserNamePrincipal;
import org.ow2.proactive.permissions.PrincipalPermission;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.selection.topology.TopologyHandler;
import org.ow2.proactive.resourcemanager.selection.topology.TopologyManager;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;
import org.junit.After;
import org.junit.Test;
import org.mockito.Matchers;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SelectionManagerTest {

    @After
    public void tearDown() throws Exception {
        RMCore.topologyManager = null;
        System.setSecurityManager(null);
    }

    @Test
    public void selectWithDifferentPermissions() throws Exception {
        PAResourceManagerProperties.RM_SELECTION_MAX_THREAD_NUMBER.updateProperty("10");
        System.setSecurityManager(securityManagerRejectingUser());

        RMCore.topologyManager = mock(TopologyManager.class);
        RMCore rmCore = mock(RMCore.class);
        when(RMCore.topologyManager.getHandler(Matchers.<TopologyDescriptor> any())).thenReturn(
                selectAllTopology());

        SelectionManager selectionManager = createSelectionManager(rmCore);

        ArrayList<RMNode> freeNodes = new ArrayList<>();
        freeNodes.add(createMockedNode("admin"));
        freeNodes.add(createMockedNode("user"));
        when(rmCore.getFreeNodes()).thenReturn(freeNodes);

        Criteria criteria = new Criteria(2);
        criteria.setTopology(TopologyDescriptor.ARBITRARY);

        Subject subject = createUser("admin");
        NodeSet nodes = selectionManager.selectNodes(criteria, new Client(subject, false));

        assertEquals(1, nodes.size());
    }

    private SecurityManager securityManagerRejectingUser() {
        return new SecurityManager() {

            @Override
            public void checkWrite(String fd) {
                throw new SecurityException();
            }

            @Override
            public void checkPermission(Permission perm) {
                if (perm.getName().equals("Identities collection") &&
                    ((PrincipalPermission) perm).hasPrincipal(new UserNamePrincipal("user"))) {
                    throw new SecurityException();
                }
            }
        };
    }

    private Subject createUser(String userPrincipal) {
        Set<Principal> principals = new HashSet<>();
        principals.add(new UserNamePrincipal(userPrincipal));
        return new Subject(false, principals, emptySet(), emptySet());
    }

    private SelectionManager createSelectionManager(final RMCore rmCore) {
        return new SelectionManager(rmCore) {
            @Override
            public List<RMNode> arrangeNodesForScriptExecution(List<RMNode> nodes,
                    List<SelectionScript> scripts) {
                return nodes;
            }

            @Override
            public boolean isPassed(SelectionScript script, RMNode rmnode) {
                return false;
            }

            @Override
            public boolean processScriptResult(SelectionScript script, ScriptResult<Boolean> scriptResult,
                    RMNode rmnode) {
                return false;
            }
        };
    }

    private TopologyHandler selectAllTopology() {
        return new TopologyHandler() {
            @Override
            public NodeSet select(int number, List<Node> matchedNodes) {
                return new NodeSet(matchedNodes);
            }
        };
    }

    private RMNode createMockedNode(String nodeUser) {
        RMNode rmNode = mock(RMNode.class);
        when(rmNode.getNodeSource()).thenReturn(new NodeSource());

        Node node = mock(Node.class);
        when(node.getNodeInformation()).thenReturn(mock(NodeInformation.class));
        when(rmNode.getNode()).thenReturn(node);
        when(rmNode.getUserPermission()).thenReturn(
                new PrincipalPermission("permissions", singleton(new UserNamePrincipal(nodeUser))));
        return rmNode;
    }
}