/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.resourcemanager.selection;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import org.junit.After;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeInformation;
import org.ow2.proactive.authentication.principals.UserNamePrincipal;
import org.ow2.proactive.permissions.NodeUserAllPermission;
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
import org.ow2.proactive.utils.Subjects;

import com.google.common.collect.Lists;


public class SelectionManagerTest {

    @After
    public void tearDown() throws Exception {
        RMCore.topologyManager = null;
        System.setSecurityManager(null);
    }

    @Test
    public void selectWithDifferentPermissions() throws Exception {
        PAResourceManagerProperties.RM_SELECTION_MAX_THREAD_NUMBER.updateProperty("10");
        System.out.println("PAResourceManagerProperties.RM_SELECTION_MAX_THREAD_NUMBER=" +
                           PAResourceManagerProperties.RM_SELECTION_MAX_THREAD_NUMBER);
        System.setSecurityManager(securityManagerRejectingUser());

        RMCore.topologyManager = mock(TopologyManager.class);
        RMCore rmCore = mock(RMCore.class);
        when(RMCore.topologyManager.getHandler(Matchers.<TopologyDescriptor> any())).thenReturn(selectAllTopology());

        SelectionManager selectionManager = createSelectionManager(rmCore);

        ArrayList<RMNode> freeNodes = new ArrayList<>();
        freeNodes.add(createMockedNode("admin"));
        freeNodes.add(createMockedNode("user"));
        when(rmCore.getFreeNodes()).thenReturn(freeNodes);

        Criteria criteria = new Criteria(2);
        criteria.setTopology(TopologyDescriptor.ARBITRARY);

        Subject subject = Subjects.create("admin");
        NodeSet nodes = selectionManager.selectNodes(criteria, new Client(subject, false));

        assertEquals(1, nodes.size());
    }

    @Test
    public void testSelectNodesWithNoNodes() {
        RMCore rmCore = newMockedRMCore(0);
        SelectionManager selectionManager = createSelectionManager(rmCore);
        Criteria crit = new Criteria(1);
        crit.setTopology(TopologyDescriptor.ARBITRARY);
        crit.setScripts(null);
        crit.setBlackList(null);
        crit.setBestEffort(false);
        NodeSet nodeSet = selectionManager.selectNodes(crit, null);
        assertEquals(0, nodeSet.size());
    }

    @Test
    public void testSelectNodesWith1Node() {
        RMCore rmCore = newMockedRMCore(1);
        SelectionManager selectionManager = createSelectionManager(rmCore);
        Criteria crit = new Criteria(1);
        crit.setTopology(TopologyDescriptor.ARBITRARY);
        crit.setScripts(null);
        crit.setBlackList(null);
        crit.setBestEffort(true);

        Client mockedClient = mock(Client.class);
        NodeSet nodeSet = selectionManager.selectNodes(crit, mockedClient);
        assertEquals(1, nodeSet.size());
    }

    @Test
    public void testSelectNodesWith10Node() {
        RMCore rmCore = newMockedRMCore(10);
        SelectionManager selectionManager = createSelectionManager(rmCore);
        Criteria crit = new Criteria(10);
        crit.setTopology(TopologyDescriptor.ARBITRARY);
        crit.setScripts(null);
        crit.setBlackList(null);
        crit.setBestEffort(true);

        Client mockedClient = mock(Client.class);
        NodeSet nodeSet = selectionManager.selectNodes(crit, mockedClient);
        assertEquals(10, nodeSet.size());
    }

    @Test
    public void testRunScriptsWillNotBeCalled() {
        RMCore rmCore = newMockedRMCore(2);
        SelectionManager selectionManager = createSelectionManager(rmCore);
        Criteria crit = new Criteria(2);
        crit.setTopology(TopologyDescriptor.SINGLE_HOST);
        SelectionScript selectWhatever = new SelectionScript();
        crit.setScripts(Lists.newArrayList(selectWhatever));
        crit.setBlackList(null);
        crit.setBestEffort(false);
        Client mockedClient = mock(Client.class);
        selectionManager.selectNodes(crit, mockedClient);
        verify(rmCore, never()).setBusyNode(anyString(), any(Client.class));

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
                if (perm instanceof NodeUserAllPermission) {
                    throw new SecurityException();
                }
            }

            @Override
            public void checkRead(String fd) {
                // ok
            }
        };
    }

    public static SelectionManager createSelectionManager(final RMCore rmCore) {
        return new SelectionManager(rmCore) {
            @Override
            public List<RMNode> arrangeNodesForScriptExecution(List<RMNode> nodes, List<SelectionScript> scripts,
                    Map<String, Serializable> bindings) {
                return nodes;
            }

            @Override
            public boolean isPassed(SelectionScript script, Map<String, Serializable> bindings, RMNode rmnode) {
                return false;
            }

            @Override
            public boolean processScriptResult(SelectionScript script, Map<String, Serializable> bindings,
                    ScriptResult<Boolean> scriptResult, RMNode rmnode) {
                return false;
            }
        };
    }

    public static TopologyHandler selectAllTopology() {
        return new TopologyHandler() {
            @Override
            public NodeSet select(int number, List<Node> matchedNodes) {
                return new NodeSet(matchedNodes);
            }
        };
    }

    public static RMCore newMockedRMCore() {
        return newMockedRMCore(0);
    }

    public static RMCore newMockedRMCore(int nbNodes) {
        RMCore mockedRMCore = Mockito.mock(RMCore.class);
        TopologyManager mockedTopologyManager = Mockito.mock(TopologyManager.class);
        when(mockedTopologyManager.getHandler(Matchers.any(TopologyDescriptor.class))).thenReturn(selectAllTopology());
        RMCore.topologyManager = mockedTopologyManager;

        if (nbNodes > 0) {
            ArrayList<RMNode> freeNodes = new ArrayList<RMNode>(nbNodes);
            for (int i = 0; i < nbNodes; i++) {
                freeNodes.add(createMockedNode("user", "mocked-node-" + (i + 1), "mocked-node-" + (i + 1)));
            }
            when(mockedRMCore.getFreeNodes()).thenReturn(freeNodes);
        }

        return mockedRMCore;
    }

    public static RMNode createMockedNode(String nodeUser) {
        return createMockedNode(nodeUser, "", "");
    }

    public static RMNode createMockedNode(String nodeUser, String nodeName, String nodeUrl) {
        RMNode rmNode = mock(RMNode.class);
        NodeInformation mockedNodeInformation = mock(NodeInformation.class);
        Node node = mock(Node.class);
        when(mockedNodeInformation.getURL()).thenReturn(nodeUrl);
        when(mockedNodeInformation.getName()).thenReturn(nodeName);
        when(node.getNodeInformation()).thenReturn(mockedNodeInformation);
        when(rmNode.getNodeName()).thenReturn(nodeName);
        when(rmNode.getNodeSource()).thenReturn(new NodeSource());
        when(rmNode.getNode()).thenReturn(node);
        when(rmNode.getNodeURL()).thenReturn(nodeUrl);
        when(rmNode.getUserPermission()).thenReturn(new PrincipalPermission("permissions",
                                                                            singleton(new UserNamePrincipal(nodeUser))));
        return rmNode;
    }
}
