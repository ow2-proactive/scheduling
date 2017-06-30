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
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.db.NodeSourceData;
import org.ow2.proactive.resourcemanager.db.RMDBManager;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.rmnode.RMDeployingNode;


/**
 * @author ActiveEon Team
 * @since 06/02/17
 */
public class InfrastructureManagerTest {

    private TestingInfrastructureManager infrastructureManager;

    @Mock
    private NodeSource nodeSource;

    @Mock
    private RMDBManager dbManager;

    @Mock
    private NodeSourceData nodeSourceData;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        infrastructureManager = new TestingInfrastructureManager();
        infrastructureManager.internalConfigure();
        infrastructureManager.setRmDbManager(dbManager);
        infrastructureManager.setNodeSource(nodeSource);
        when(nodeSource.getName()).thenReturn("NodeSource#1");
        when(dbManager.getNodeSource(anyString())).thenReturn(nodeSourceData);
    }

    @Test
    public void testGetDeployingNodeUnknownNode() {
        RMDeployingNode rmNode = new RMDeployingNode("deploying", nodeSource, "command", null);

        assertThat(infrastructureManager.getDeployingNodesDeployingState()).hasSize(0);
        assertThat(infrastructureManager.getDeployingNodesLostState()).hasSize(0);

        RMDeployingNode rmNodeFound = infrastructureManager.getDeployingNode(rmNode.getNodeURL());

        assertThat(rmNodeFound).isNull();
    }

    @Test
    public void testGetDeployingNodeDeployingStateKnow() {
        RMDeployingNode rmNode = new RMDeployingNode("deploying", nodeSource, "command", null);
        infrastructureManager.addDeployingNode(rmNode);

        assertThat(infrastructureManager.getDeployingNodesDeployingState()).hasSize(1);
        assertThat(infrastructureManager.getDeployingNodesLostState()).hasSize(0);

        RMDeployingNode rmNodeFound = infrastructureManager.getDeployingNode(rmNode.getNodeURL());

        assertThat(rmNodeFound).isSameAs(rmNode);
    }

    @Test
    public void testGetDeployingNodeLostStateKnow() {
        RMDeployingNode deployingNode = new RMDeployingNode("deploying", nodeSource, "command", null);
        infrastructureManager.addDeployingNode(deployingNode);
        RMDeployingNode lostNode = new RMDeployingNode("lost", nodeSource, "command", null);
        lostNode.setLost();
        infrastructureManager.addLostNode(lostNode);

        assertThat(infrastructureManager.getDeployingNodesDeployingState()).hasSize(1);
        assertThat(infrastructureManager.getDeployingNodesLostState()).hasSize(1);

        RMDeployingNode rmNodeFound = infrastructureManager.getDeployingNode(lostNode.getNodeURL());
        assertThat(rmNodeFound).isSameAs(lostNode);
        assertThat(rmNodeFound).isNotSameAs(deployingNode);

        rmNodeFound = infrastructureManager.getDeployingNode(deployingNode.getNodeURL());
        assertThat(rmNodeFound).isSameAs(deployingNode);
        assertThat(rmNodeFound).isNotSameAs(lostNode);
    }

    @Test
    public void testGetDeployingNodeConflictingUrls() {
        RMDeployingNode deployingNode = new RMDeployingNode("deploying", nodeSource, "command", null);
        infrastructureManager.addDeployingNode(deployingNode);
        RMDeployingNode lostNode = new RMDeployingNode("deploying", nodeSource, "command", null);
        lostNode.setLost();
        infrastructureManager.addLostNode(lostNode);

        assertThat(infrastructureManager.getDeployingNodesDeployingState()).hasSize(1);
        assertThat(infrastructureManager.getDeployingNodesLostState()).hasSize(1);

        // deploying nodes have priority over lost nodes
        RMDeployingNode rmNodeFound = infrastructureManager.getDeployingNode(lostNode.getNodeURL());
        assertThat(rmNodeFound).isSameAs(deployingNode);
        assertThat(rmNodeFound).isNotSameAs(lostNode);
    }

    @Test
    public void testUpdateUnknownNode() {
        RMDeployingNode rmNode = new RMDeployingNode("deploying", nodeSource, "command", null);

        assertThat(infrastructureManager.getDeployingNodesDeployingState()).hasSize(0);
        assertThat(infrastructureManager.getDeployingNodesLostState()).hasSize(0);

        RMDeployingNode oldRmNode = infrastructureManager.update(rmNode);

        assertThat(infrastructureManager.getDeployingNodesLostState()).hasSize(0);
        assertThat(infrastructureManager.getDeployingNodesDeployingState()).hasSize(0);
        assertThat(oldRmNode).isNull();
    }

    @Test
    public void testUpdateDeployingNodeKnown() {
        RMDeployingNode rmNode = new RMDeployingNode("deploying", nodeSource, "command", null);
        infrastructureManager.addDeployingNode(rmNode);

        assertThat(infrastructureManager.getDeployingNodesDeployingState()).hasSize(1);
        assertThat(infrastructureManager.getDeployingNodesLostState()).hasSize(0);

        RMDeployingNode rmNode2 = new RMDeployingNode("deploying", nodeSource, "command2", null);

        RMDeployingNode oldRmNode = infrastructureManager.update(rmNode2);

        assertThat(oldRmNode).isSameAs(rmNode);
        assertThat(infrastructureManager.getDeployingNodesLostState()).hasSize(0);
        assertThat(infrastructureManager.getDeployingNodesDeployingState()).hasSize(1);
        assertThat(infrastructureManager.getDeployingNodesDeployingState().get(rmNode2.getNodeURL())).isSameAs(rmNode2);
    }

    @Test
    public void testUpdateLostNodeKnown() {
        RMDeployingNode deployingNode = new RMDeployingNode("deploying", nodeSource, "command", null);
        infrastructureManager.addDeployingNode(deployingNode);
        RMDeployingNode lostNode = new RMDeployingNode("lost", nodeSource, "command", null);
        lostNode.setLost();
        infrastructureManager.addLostNode(lostNode);

        assertThat(infrastructureManager.getDeployingNodesDeployingState()).hasSize(1);
        assertThat(infrastructureManager.getDeployingNodesLostState()).hasSize(1);

        RMDeployingNode lostNode2 = new RMDeployingNode("lost", nodeSource, "command2", null);
        lostNode2.setLost();

        RMDeployingNode oldRmNode = infrastructureManager.update(lostNode2);

        assertThat(oldRmNode).isSameAs(lostNode);
        assertThat(infrastructureManager.getDeployingNodesLostState()).hasSize(1);
        assertThat(infrastructureManager.getDeployingNodesDeployingState()).hasSize(1);
        assertThat(infrastructureManager.getDeployingNodesLostState().get(lostNode.getNodeURL())).isSameAs(lostNode2);
        assertThat(infrastructureManager.getDeployingNodesLostState().get(lostNode2.getNodeURL())).isSameAs(lostNode2);
    }

    @Test
    public void testUpdateLostNodeKnownConflictingUrls() {
        RMDeployingNode deployingNode = new RMDeployingNode("deploying", nodeSource, "command", null);
        infrastructureManager.addDeployingNode(deployingNode);
        RMDeployingNode lostNode = new RMDeployingNode("deploying", nodeSource, "command", null);
        lostNode.setLost();
        infrastructureManager.addLostNode(lostNode);

        assertThat(infrastructureManager.getDeployingNodesDeployingState()).hasSize(1);
        assertThat(infrastructureManager.getDeployingNodesLostState()).hasSize(1);

        RMDeployingNode lostNode2 = new RMDeployingNode("deploying", nodeSource, "command2", null);
        lostNode2.setLost();

        RMDeployingNode oldRmNode = infrastructureManager.update(lostNode2);

        assertThat(oldRmNode).isSameAs(lostNode);
        assertThat(oldRmNode).isNotSameAs(deployingNode);
        assertThat(infrastructureManager.getDeployingNodesLostState()).hasSize(1);
        assertThat(infrastructureManager.getDeployingNodesDeployingState()).hasSize(1);
        assertThat(infrastructureManager.getDeployingNodesLostState().get(lostNode.getNodeURL())).isSameAs(lostNode2);
        assertThat(infrastructureManager.getDeployingNodesLostState().get(lostNode2.getNodeURL())).isSameAs(lostNode2);
    }

    @Test
    public void testPersistInfrastructureVariables() {
        infrastructureManager.persistInfrastructureVariables();
        verify(nodeSourceData, times(1)).setInfrastructureVariables(anyMap());
        verify(dbManager, times(1)).updateNodeSource(eq(nodeSourceData));
    }

    @Test
    public void testDoNotPersistInfrastructureVariablesWhenCannotFindNodeSource() {
        when(dbManager.getNodeSource(anyString())).thenReturn(null);
        infrastructureManager.persistInfrastructureVariables();
        verify(nodeSourceData, times(0)).setInfrastructureVariables(anyMap());
        verify(dbManager, times(0)).updateNodeSource(eq(nodeSourceData));
    }

    private static final class TestingInfrastructureManager extends InfrastructureManager {

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        protected void configure(Object... parameters) {

        }

        @Override
        public void acquireNode() {

        }

        @Override
        public void acquireAllNodes() {

        }

        @Override
        public void removeNode(Node node) throws RMException {

        }

        @Override
        protected void notifyAcquiredNode(Node node) throws RMException {

        }

        @Override
        protected void initializeRuntimeVariables() {

        }

    }

}
