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
package org.ow2.proactive.resourcemanager.rmnode;

import static com.google.common.truth.Truth.assertThat;

import java.io.Serializable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.scripting.Script;

import com.google.common.collect.Maps;


/**
 * @author ActiveEon Team
 * @since 06/02/17
 */
public class RMDeployingNodeTest {

    private RMDeployingNode deployingNode;

    @Before
    public void setUp() {
        deployingNode = createDeployingNode("deploying");
    }

    private RMDeployingNode createDeployingNode(String name) {
        Client client = Mockito.mock(Client.class);
        NodeSource nodeSource = Mockito.mock(NodeSource.class);
        return new RMDeployingNode(name, nodeSource, "command", client);
    }

    @Test
    public void testClean() throws NodeException {
        // assert that no exception is thrown.
        deployingNode.clean();
    }

    @Test
    public void testCompareToFalse() {
        RMDeployingNode secondDeployingNode = createDeployingNode("deploying2");
        assertThat(deployingNode.compareTo(secondDeployingNode)).isNotEqualTo(0);
    }

    @Test
    public void testCompareToTrue() {
        RMDeployingNode secondDeployingNode = createDeployingNode("deploying");
        assertThat(deployingNode.compareTo(secondDeployingNode)).isEqualTo(0);
    }

    @Test
    public void testConstructor() {
        assertThat(deployingNode.getState()).isEqualTo(NodeState.DEPLOYING);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testExecuteScript() {
        deployingNode.executeScript(Mockito.mock(Script.class), Maps.<String, Serializable> newHashMap());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetAdminPermission() {
        deployingNode.getAdminPermission();
    }

    @Test
    public void testGetCommandLine() {
        assertThat(deployingNode.getCommandLine()).isEqualTo("command");
    }

    @Test
    public void testGetDescription() {
        assertThat(deployingNode.getDescription()).isEmpty();
        deployingNode.setDescription("description");
        assertThat(deployingNode.getDescription()).isEqualTo("description");
    }

    @Test
    public void testGetDescriptorVMName() {
        assertThat(deployingNode.getDescriptorVMName()).isEmpty();
    }

    @Test
    public void testGetHostName() {
        assertThat(deployingNode.getHostName()).isEmpty();
    }

    @Test
    public void testGetJMXUrl() {
        assertThat(deployingNode.getJMXUrl(null)).isNull();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetNode() {
        deployingNode.getNode();
    }

    @Test
    public void testGetNodeInfo() {
        assertThat(deployingNode.getNodeInfo()).isNotNull();
    }

    @Test
    public void testGetNodeURL() {
        assertThat(deployingNode.getNodeURL()).isEqualTo("deploying://null/deploying");
    }

    @Test
    public void testGetOwner() {
        assertThat(deployingNode.getOwner()).isNull();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetScriptStatus() {
        deployingNode.getScriptStatus();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetUserPermission() {
        deployingNode.getUserPermission();
    }

    @Test
    public void testGetVNodeName() {
        assertThat(deployingNode.getVNodeName()).isEmpty();
    }

    @Test
    public void testIsBusy() {
        assertThat(deployingNode.isBusy()).isFalse();
    }

    @Test
    public void testIsConfiguring() {
        assertThat(deployingNode.isConfiguring()).isFalse();
    }

    @Test
    public void testIsDeploying() {
        assertThat(deployingNode.isDeploying()).isTrue();
    }

    @Test
    public void testIsDown() {
        assertThat(deployingNode.isDown()).isFalse();
    }

    @Test
    public void testIsFree() {
        assertThat(deployingNode.isFree()).isFalse();
    }

    @Test
    public void testIsLost() {
        assertThat(deployingNode.isLost()).isFalse();
    }

    @Test
    public void testIsProtectedByToken() {
        assertThat(deployingNode.isProtectedByToken()).isFalse();
    }

    @Test
    public void testIsToRemove() {
        assertThat(deployingNode.isToRemove()).isFalse();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetBusy() {
        deployingNode.setBusy(Mockito.mock(Client.class));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetConfiguring() {
        deployingNode.setConfiguring(Mockito.mock(Client.class));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetDown() {
        deployingNode.setDown();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetFree() {
        deployingNode.setFree();
    }

    @Test
    public void testSetJMXUrl() {
        // assert that no exception is thrown
        deployingNode.setJMXUrl(null, null);
    }

    @Test
    public void testSetLost() {
        assertThat(deployingNode.isLost()).isFalse();
        deployingNode.setLost();
        assertThat(deployingNode.isLost()).isTrue();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetToRemove() {
        deployingNode.setToRemove();
    }

    @Test
    public void testHashCode() {
        // The purpose is to check that no NPE is thrown.
        // It happens if AbstractRMNode makes use of the internal
        // field nodeURL instead of the associated getter that is overridden
        // by RMDeployingNodeImpl
        deployingNode.hashCode();
    }

    @Test
    public void testEquals() {
        // The purpose is to check that no NPE is thrown.
        // It happens if AbstractRMNode makes use of the internal
        // field nodeURL instead of the associated getter that is overridden
        // by RMDeployingNodeImpl
        deployingNode.equals(deployingNode);
    }

}
