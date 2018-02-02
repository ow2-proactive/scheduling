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
package functionaltests.db;

import static com.google.common.truth.Truth.assertThat;

import java.security.Permission;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.AuthPermission;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.UniqueID;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.db.NodeSourceData;
import org.ow2.proactive.resourcemanager.db.RMDBManager;
import org.ow2.proactive.resourcemanager.db.RMNodeData;


/**
 * @author ActiveEon Team
 * @since 07/02/17
 */
public class RMDBManagerTest {

    private static final String NODE_SOURCE_NAME_BASE = "NodeSourceName";

    private static final String NODE_SOURCE_POLICY = "PolicyType";

    private static final String NODE_NAME_BASE = "RMNodeData";

    private static final String NODE_URL_BASE = "pnp://XXX.XXX.X.XXX:ZZZZZ/";

    private static final NodeState NODE_STATE_BASE = NodeState.FREE;

    private static final long STATE_CHANGE_TIME_BASE = 1234;

    private static final UniqueID OWNER_ID = new UniqueID("ownerId");

    private static final UniqueID PROVIDER_ID = new UniqueID("providerId");

    private static final String PERMISSION_NAME = "read,write";

    private static final String HOSTNAME = "localhost";

    private static final String[] JMX_URLS = new String[] { "url1", "url2" };

    private static final String JVM_NAME = "pnp://192.168.1.104:59357/PA_JVM0123456789";

    private RMDBManager dbManager;

    private NodeSourceData nodeSourceData;

    private Client owner;

    private Client provider;

    private Permission permission;

    @Before
    public void setUp() {
        PAResourceManagerProperties.RM_NODES_DB_OPERATIONS_DELAY.updateProperty("0");
        PAResourceManagerProperties.RM_ALIVE_EVENT_FREQUENCY.updateProperty("10000");

        dbManager = RMDBManager.createInMemoryRMDBManager();
        addNodeSourceData();

        owner = new Client();
        owner.setId(OWNER_ID);
        provider = new Client();
        provider.setId(PROVIDER_ID);
        permission = new AuthPermission(PERMISSION_NAME);
    }

    @After
    public void tearDown() {
        dbManager.close();
    }

    @Test
    public void testAddRMNodeData() {

        addRMNodeData(NODE_NAME_BASE, NODE_STATE_BASE);

        Collection<RMNodeData> allNodes = dbManager.getAllNodes();
        assertThat(allNodes).hasSize(1);

        for (RMNodeData node : allNodes) {
            assertThat(node.getName()).isEqualTo(NODE_NAME_BASE);
            assertThat(node.getNodeUrl()).isEqualTo(NODE_URL_BASE + NODE_NAME_BASE);
            assertThat(node.getState()).isEqualTo(NODE_STATE_BASE);
            assertThat(node.getStateChangeTime()).isEqualTo(STATE_CHANGE_TIME_BASE);
            assertThat(node.getOwner().getId()).isEqualTo(OWNER_ID);
            assertThat(node.getProvider().getId()).isEqualTo(PROVIDER_ID);
            assertThat(node.getUserPermission().getName()).isEqualTo(PERMISSION_NAME);
            assertThat(node.getHostname()).isEqualTo(HOSTNAME);
            assertThat(node.getJmxUrls()[0]).isEqualTo(JMX_URLS[0]);
            assertThat(node.getJmxUrls()[1]).isEqualTo(JMX_URLS[1]);
            assertThat(node.getJvmName()).isEqualTo(JVM_NAME);
        }
    }

    @Test
    public void testUpdateRMNodeData() {

        RMNodeData rmNodeData = addRMNodeData(NODE_NAME_BASE, NODE_STATE_BASE);
        rmNodeData.setState(NodeState.BUSY);
        rmNodeData.setStateChangeTime(5678);
        dbManager.updateNode(rmNodeData);

        Collection<RMNodeData> allNodes = dbManager.getAllNodes();
        assertThat(allNodes).hasSize(1);

        for (RMNodeData node : allNodes) {
            assertThat(node.getName()).isEqualTo(NODE_NAME_BASE);
            assertThat(node.getNodeUrl()).isEqualTo(NODE_URL_BASE + NODE_NAME_BASE);
            assertThat(node.getState()).isEqualTo(NodeState.BUSY);
            assertThat(node.getStateChangeTime()).isEqualTo(5678);
            assertThat(node.getOwner().getId()).isEqualTo(OWNER_ID);
            assertThat(node.getProvider().getId()).isEqualTo(PROVIDER_ID);
            assertThat(node.getUserPermission().getName()).isEqualTo(PERMISSION_NAME);
            assertThat(node.getHostname()).isEqualTo(HOSTNAME);
            assertThat(node.getJmxUrls()[0]).isEqualTo(JMX_URLS[0]);
            assertThat(node.getJmxUrls()[1]).isEqualTo(JMX_URLS[1]);
            assertThat(node.getJvmName()).isEqualTo(JVM_NAME);
        }
    }

    @Test
    public void testAddAllRMNodeData() {

        addNRMNodeData(10);

        Collection<RMNodeData> allNodes = dbManager.getAllNodes();
        assertThat(allNodes).hasSize(10);

        for (RMNodeData node : allNodes) {
            assertThat(node.getName()).startsWith(NODE_NAME_BASE);
            assertThat(node.getNodeUrl()).startsWith(NODE_URL_BASE + NODE_NAME_BASE);
            assertThat(node.getState()).isEqualTo(NODE_STATE_BASE);
            assertThat(node.getStateChangeTime()).isEqualTo(STATE_CHANGE_TIME_BASE);
            assertThat(node.getOwner().getId()).isEqualTo(OWNER_ID);
            assertThat(node.getProvider().getId()).isEqualTo(PROVIDER_ID);
            assertThat(node.getUserPermission().getName()).isEqualTo(PERMISSION_NAME);
            assertThat(node.getHostname()).isEqualTo(HOSTNAME);
            assertThat(node.getJmxUrls()[0]).isEqualTo(JMX_URLS[0]);
            assertThat(node.getJmxUrls()[1]).isEqualTo(JMX_URLS[1]);
            assertThat(node.getJvmName()).isEqualTo(JVM_NAME);
        }
    }

    @Test
    public void testRemoveRMNodeData() {

        RMNodeData rmNodeData = addRMNodeData(NODE_NAME_BASE, NODE_STATE_BASE);
        dbManager.removeNode(rmNodeData);

        Collection<RMNodeData> allNodes = dbManager.getAllNodes();
        assertThat(allNodes).hasSize(0);
    }

    @Test
    public void testGetRMNodeDataByNodeSource() {

        RMNodeData rmNodeData1 = addRMNodeData(NODE_NAME_BASE + "1", NODE_STATE_BASE);

        // Add another RMNodeData with another NodeSourceData
        RMNodeData rmNodeData2 = new RMNodeData.Builder().name(NODE_NAME_BASE + "2")
                                                         .nodeUrl(NODE_URL_BASE)
                                                         .state(NODE_STATE_BASE)
                                                         .stateChangeTime(STATE_CHANGE_TIME_BASE)
                                                         .hostname(HOSTNAME)
                                                         .jmxUrls(JMX_URLS)
                                                         .jvmName(JVM_NAME)
                                                         .build();

        NodeSourceData newNodeSourceData = new NodeSourceData();
        newNodeSourceData.setName("anotherNodeSourceName");
        newNodeSourceData.setPolicyType("aPolicyType");
        dbManager.addNodeSource(newNodeSourceData);
        rmNodeData2.setNodeSource(newNodeSourceData);
        dbManager.addNode(rmNodeData2);

        Collection<RMNodeData> nodes = dbManager.getNodesByNodeSource(nodeSourceData.getName());

        assertThat(nodes).hasSize(1);

        for (RMNodeData node : nodes) {
            assertThat(node).isEqualTo(rmNodeData1);
        }
    }

    @Test
    public void testGetSeveralRMNodeDataByNodeSource() {

        RMNodeData rmNodeData1 = addRMNodeData(NODE_NAME_BASE + "1", NODE_STATE_BASE);

        // Add another RMNodeData with another NodeSourceData
        RMNodeData rmNodeData2 = new RMNodeData.Builder().name(NODE_NAME_BASE + "2")
                                                         .nodeUrl(NODE_URL_BASE)
                                                         .state(NODE_STATE_BASE)
                                                         .stateChangeTime(STATE_CHANGE_TIME_BASE)
                                                         .hostname(HOSTNAME)
                                                         .jmxUrls(JMX_URLS)
                                                         .jvmName(JVM_NAME)
                                                         .build();

        NodeSourceData newNodeSourceData = new NodeSourceData();
        newNodeSourceData.setName("anotherNodeSourceName");
        newNodeSourceData.setPolicyType("aPolicyType");
        dbManager.addNodeSource(newNodeSourceData);
        rmNodeData2.setNodeSource(newNodeSourceData);
        dbManager.addNode(rmNodeData2);

        RMNodeData rmNodeData3 = addRMNodeData(NODE_NAME_BASE + "3", NODE_STATE_BASE);

        Collection<RMNodeData> nodes = dbManager.getNodesByNodeSource(nodeSourceData.getName());

        assertThat(nodes).hasSize(2);

        for (RMNodeData node : nodes) {
            assertThat(node).isAnyOf(rmNodeData1, rmNodeData3);
        }
    }

    @Test
    public void testAddDeployingNode() {

        addRMNodeData(NODE_NAME_BASE, NodeState.DEPLOYING);

        Collection<RMNodeData> allNodes = dbManager.getAllNodes();
        assertThat(allNodes).hasSize(1);

        for (RMNodeData node : allNodes) {
            assertThat(node.getName()).isEqualTo(NODE_NAME_BASE);
            assertThat(node.getNodeUrl()).startsWith(NODE_URL_BASE + NODE_NAME_BASE);
            assertThat(node.getState()).isEqualTo(NodeState.DEPLOYING);
            assertThat(node.getStateChangeTime()).isEqualTo(STATE_CHANGE_TIME_BASE);
            assertThat(node.getOwner().getId()).isEqualTo(OWNER_ID);
            assertThat(node.getProvider().getId()).isEqualTo(PROVIDER_ID);
            assertThat(node.getUserPermission().getName()).isEqualTo(PERMISSION_NAME);
            assertThat(node.getHostname()).isEqualTo(HOSTNAME);
            assertThat(node.getJmxUrls()[0]).isEqualTo(JMX_URLS[0]);
            assertThat(node.getJmxUrls()[1]).isEqualTo(JMX_URLS[1]);
            assertThat(node.getJvmName()).isEqualTo(JVM_NAME);
        }
    }

    @Test
    public void testRemoveDeployingNode() {

        addRMNodeData(NODE_NAME_BASE + "1", NODE_STATE_BASE);
        RMNodeData rmNodeData = addRMNodeData(NODE_NAME_BASE + "2", NodeState.DEPLOYING);
        addRMNodeData(NODE_NAME_BASE + "3", NODE_STATE_BASE);

        dbManager.removeNode(rmNodeData);

        Collection<RMNodeData> allNodes = dbManager.getAllNodes();
        assertThat(allNodes).hasSize(2);

        for (RMNodeData node : allNodes) {
            assertThat(node.getName()).startsWith(NODE_NAME_BASE);
            assertThat(node.getNodeUrl()).startsWith(NODE_URL_BASE + NODE_NAME_BASE);
            // no more deploying nodes should be returned
            assertThat(node.getState()).isNotEqualTo(NodeState.DEPLOYING);
            assertThat(node.getStateChangeTime()).isEqualTo(STATE_CHANGE_TIME_BASE);
            assertThat(node.getOwner().getId()).isEqualTo(OWNER_ID);
            assertThat(node.getProvider().getId()).isEqualTo(PROVIDER_ID);
            assertThat(node.getUserPermission().getName()).isEqualTo(PERMISSION_NAME);
            assertThat(node.getHostname()).isEqualTo(HOSTNAME);
            assertThat(node.getJmxUrls()[0]).isEqualTo(JMX_URLS[0]);
            assertThat(node.getJmxUrls()[1]).isEqualTo(JMX_URLS[1]);
            assertThat(node.getJvmName()).isEqualTo(JVM_NAME);
        }
    }

    private void addNodeSourceData() {
        nodeSourceData = new NodeSourceData();
        nodeSourceData.setName(NODE_SOURCE_NAME_BASE);
        nodeSourceData.setPolicyType(NODE_SOURCE_POLICY);
        dbManager.addNodeSource(nodeSourceData);
    }

    private RMNodeData addRMNodeData(String nodeName, NodeState state) {
        RMNodeData rmNodeData = new RMNodeData.Builder().name(nodeName)
                                                        .nodeUrl(NODE_URL_BASE + nodeName)
                                                        .owner(owner)
                                                        .provider(provider)
                                                        .userPermission(permission)
                                                        .state(state)
                                                        .stateChangeTime(STATE_CHANGE_TIME_BASE)
                                                        .hostname(HOSTNAME)
                                                        .jmxUrls(JMX_URLS)
                                                        .jvmName(JVM_NAME)
                                                        .build();

        rmNodeData.setNodeSource(nodeSourceData);
        dbManager.addNode(rmNodeData);
        return rmNodeData;
    }

    private Map<String, RMNodeData> addNRMNodeData(int nbEntries) {
        Map<String, RMNodeData> entries = new HashMap<>();

        for (int i = 0; i < nbEntries; i++) {
            RMNodeData rmNodeData = addRMNodeData(NODE_NAME_BASE + i, NODE_STATE_BASE);
            entries.put(NODE_NAME_BASE + i, rmNodeData);
        }
        return entries;
    }

}
