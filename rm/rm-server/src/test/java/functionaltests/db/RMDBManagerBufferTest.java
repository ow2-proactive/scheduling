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
import static org.mockito.Mockito.*;

import java.security.Permission;
import java.util.AbstractMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.db.NodeSourceData;
import org.ow2.proactive.resourcemanager.db.RMDBManager;
import org.ow2.proactive.resourcemanager.db.RMDBManagerBuffer;
import org.ow2.proactive.resourcemanager.db.RMNodeData;


public class RMDBManagerBufferTest {

    private static final String NODE_SOURCE_NAME_BASE = "NodeSourceName";

    private static final String NODE_SOURCE_POLICY = "PolicyType";

    private static final String NODE_NAME_BASE = "RMNodeData";

    private static final String NODE_URL = "pnp://XXX.XXX.X.XXX:ZZZZZ/";

    private static final NodeState NODE_STATE_BASE = NodeState.FREE;

    private static final long STATE_CHANGE_TIME_BASE = 1234;

    private static final String HOSTNAME = "localhost";

    private static final String[] JMX_URLS = new String[] { "url1", "url2" };

    private static final String JVM_NAME = "pnp://192.168.1.104:59357/PA_JVM0123456789";

    // 3 seconds
    private static final String NODE_DB_OPERATION_DELAY = "3000";

    // 2 seconds
    private static final Long ACCEPTABLE_DATABASE_OPERATION_TIME_LESS_THAN_DELAY = Long.valueOf(NODE_DB_OPERATION_DELAY) -
                                                                                   1000L;

    // 1 second
    private static final Long ACCEPTABLE_INSTRUCTIONS_TIME_LESS_THAN_DELAY = Long.valueOf(NODE_DB_OPERATION_DELAY) -
                                                                             2000L;

    private RMDBManager dbManager;

    private RMDBManagerBuffer dbManagerBuffer;

    private NodeSourceData nodeSourceData;

    private Client owner;

    private Client provider;

    private Permission permission;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testLongDelayLeadsToNodeCanBeRetrievedAfterDelay() {
        setPropertiesAndCreateDBManager(NODE_DB_OPERATION_DELAY, "true");

        long beforeAdd = System.currentTimeMillis();
        addRMNodeData(NODE_NAME_BASE, NODE_STATE_BASE);
        checkPendingNodeOperationsNbOperations(1);
        long afterAdd = System.currentTimeMillis();

        assertThat(dbManager.getAllNodes().size()).isEqualTo(1);
        checkPendingNodeOperationsIsEmpty();
        long afterRetrieve = System.currentTimeMillis();

        // node add is asynchronous
        assertThat(afterAdd - beforeAdd).isLessThan(ACCEPTABLE_INSTRUCTIONS_TIME_LESS_THAN_DELAY);
        // node retrieve should synchronize after asynchronous add
        assertThat(afterRetrieve - beforeAdd).isGreaterThan(Long.valueOf(NODE_DB_OPERATION_DELAY));
    }

    @Test
    public void testNoDelayLeadsToNodeCanBeRetrievedFast() {
        setPropertiesAndCreateDBManager("0", "true");

        long beforeAdd = System.currentTimeMillis();
        addRMNodeData(NODE_NAME_BASE, NODE_STATE_BASE);
        checkPendingNodeOperationsIsEmpty();

        assertThat(dbManager.getAllNodes().size()).isEqualTo(1);
        long afterRetrieve = System.currentTimeMillis();

        // two synchronous database operations should be fast
        assertThat(afterRetrieve - beforeAdd).isLessThan(ACCEPTABLE_DATABASE_OPERATION_TIME_LESS_THAN_DELAY);
    }

    @Test
    public void testLongDelayAndImmediatelyUpdatedNodeLeadsToUpdatedNodeCanOnlyBeRetrievedAfterDelay() {
        setPropertiesAndCreateDBManager(NODE_DB_OPERATION_DELAY, "true");

        long beforeAdd = System.currentTimeMillis();
        RMNodeData rmNodeData = addRMNodeData(NODE_NAME_BASE, NODE_STATE_BASE);
        checkPendingNodeOperationsNbOperations(1);

        updateRMNodeData(rmNodeData, NodeState.BUSY);
        checkPendingNodeOperationsNbOperations(2);
        long afterUpdate = System.currentTimeMillis();

        assertThat(dbManager.getNodeByNameAndUrl(NODE_NAME_BASE, NODE_URL).getState()).isEqualTo(NodeState.BUSY);
        checkPendingNodeOperationsIsEmpty();
        long afterRetrieve = System.currentTimeMillis();

        // node add and update are asynchronous (update cannot proceed before add)
        assertThat(afterUpdate - beforeAdd).isLessThan(ACCEPTABLE_INSTRUCTIONS_TIME_LESS_THAN_DELAY);
        // node retrieve should synchronize after asynchronous add
        assertThat(afterRetrieve - beforeAdd).isGreaterThan(Long.valueOf(NODE_DB_OPERATION_DELAY));
    }

    @Test
    public void testNoDelayAndSynchronousUpdatesDisabledLeadsToImmediatelyUpdatedNodeCanBeRetrievedFast() {
        setPropertiesAndCreateDBManager("0", "false");

        long beforeAdd = System.currentTimeMillis();
        RMNodeData rmNodeData = addRMNodeData(NODE_NAME_BASE, NODE_STATE_BASE);
        checkPendingNodeOperationsIsEmpty();

        updateRMNodeData(rmNodeData, NodeState.BUSY);
        checkPendingNodeOperationsIsEmpty();

        assertThat(dbManager.getNodeByNameAndUrl(NODE_NAME_BASE, NODE_URL).getState()).isEqualTo(NodeState.BUSY);
        long afterRetrieve = System.currentTimeMillis();

        assertThat(afterRetrieve - beforeAdd).isLessThan(ACCEPTABLE_DATABASE_OPERATION_TIME_LESS_THAN_DELAY);
    }

    @Test
    public void testLongDelayLeadsToLaterUpdatedNodeCanBeRetrievedFast() {
        setPropertiesAndCreateDBManager(NODE_DB_OPERATION_DELAY, "true");

        RMNodeData rmNodeData = addRMNodeData(NODE_NAME_BASE, NODE_STATE_BASE);
        checkPendingNodeOperationsNbOperations(1);

        assertThat(dbManager.getAllNodes().size()).isEqualTo(1);
        checkPendingNodeOperationsIsEmpty();

        long beforeUpdate = System.currentTimeMillis();
        updateRMNodeData(rmNodeData, NodeState.BUSY);
        checkPendingNodeOperationsIsEmpty();

        assertThat(dbManager.getNodeByNameAndUrl(NODE_NAME_BASE, NODE_URL).getState()).isEqualTo(NodeState.BUSY);
        long afterRetrieve = System.currentTimeMillis();

        assertThat(afterRetrieve - beforeUpdate).isLessThan(ACCEPTABLE_DATABASE_OPERATION_TIME_LESS_THAN_DELAY);
    }

    @Test
    public void testLongDelayAndSynchronousUpdatesDisabledLeadsToLaterUpdatedNodeCanOnlyBeRetrievedAfterDelay() {
        setPropertiesAndCreateDBManager(NODE_DB_OPERATION_DELAY, "false");
        assertThat(PAResourceManagerProperties.RM_NODES_DB_SYNCHRONOUS_UPDATES.getValueAsBoolean()).isEqualTo(false);

        RMNodeData rmNodeData = addRMNodeData(NODE_NAME_BASE, NODE_STATE_BASE);
        checkPendingNodeOperationsNbOperations(1);

        assertThat(dbManager.getAllNodes().size()).isEqualTo(1);
        checkPendingNodeOperationsIsEmpty();

        long beforeUpdate = System.currentTimeMillis();
        updateRMNodeData(rmNodeData, NodeState.BUSY);
        checkPendingNodeOperationsNbOperations(1);

        assertThat(dbManager.getNodeByNameAndUrl(NODE_NAME_BASE, NODE_URL).getState()).isEqualTo(NodeState.BUSY);
        checkPendingNodeOperationsIsEmpty();
        long afterRetrieve = System.currentTimeMillis();

        assertThat(afterRetrieve - beforeUpdate).isGreaterThan(Long.valueOf(NODE_DB_OPERATION_DELAY));
    }

    private void setPropertiesAndCreateDBManager(String nodeDbOperationDelay, String aFalse) {
        PAResourceManagerProperties.RM_NODES_DB_OPERATIONS_DELAY.updateProperty(nodeDbOperationDelay);
        PAResourceManagerProperties.RM_NODES_DB_SYNCHRONOUS_UPDATES.updateProperty(aFalse);
        dbManager = RMDBManager.createInMemoryRMDBManager();
        dbManager = spy(dbManager);
        dbManagerBuffer = dbManager.getBuffer();
        addNodeSourceData();
    }

    private void addNodeSourceData() {
        nodeSourceData = new NodeSourceData();
        nodeSourceData.setName(NODE_SOURCE_NAME_BASE);
        nodeSourceData.setPolicyType(NODE_SOURCE_POLICY);
        dbManager.addNodeSource(nodeSourceData);
    }

    private RMNodeData addRMNodeData(String nodeName, NodeState state) {
        RMNodeData rmNodeData = new RMNodeData(nodeName,
                                               NODE_URL,
                                               owner,
                                               provider,
                                               permission,
                                               state,
                                               STATE_CHANGE_TIME_BASE,
                                               HOSTNAME,
                                               JMX_URLS,
                                               JVM_NAME);
        rmNodeData.setNodeSource(nodeSourceData);
        dbManager.addNode(rmNodeData);
        return rmNodeData;
    }

    private void updateRMNodeData(RMNodeData rmNodeData, NodeState nodeState) {
        rmNodeData.setState(nodeState);
        dbManager.updateNode(rmNodeData);
    }

    private void checkPendingNodeOperationsIsEmpty() {
        List<AbstractMap.SimpleImmutableEntry<RMNodeData, RMDBManagerBuffer.DatabaseOperation>> pendingNodeOperations;
        pendingNodeOperations = dbManagerBuffer.listPendingNodeOperations();
        assertThat(pendingNodeOperations.size()).isEqualTo(0);
    }

    private void checkPendingNodeOperationsNbOperations(int nb) {
        List<AbstractMap.SimpleImmutableEntry<RMNodeData, RMDBManagerBuffer.DatabaseOperation>> pendingNodeOperations;
        pendingNodeOperations = dbManagerBuffer.listPendingNodeOperations();
        assertThat(pendingNodeOperations.size()).isEqualTo(nb);
    }

}
