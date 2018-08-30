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
package functionaltests.nodesrecovery;

import static com.google.common.truth.Truth.assertThat;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.NodeSourceStatus;

import functionaltests.monitor.RMMonitorEventReceiver;
import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;


/**
 * @author ActiveEon Team
 * @since 22/06/17
 */
public class RecoverLocalInfrastructureTest extends RMFunctionalTest {

    private static final String START_CONFIG = "/functionaltests/config/functionalTRMProperties-RM-start-clean-db-nodes-recovery-enabled.ini";

    private static final String RESTART_CONFIG = "/functionaltests/config/functionalTRMProperties-RM-restart-keep-db-nodes-recovery-enabled.ini";

    private static final String NODE_SOURCE_NAME = "LocalNodeSource" +
                                                   RecoverLocalInfrastructureTest.class.getSimpleName();

    private static final int NODE_NUMBER = 3;

    private ResourceManager resourceManager = null;

    @Before
    public void setup() throws Exception {

        this.startRmAndCheckInitialState();
    }

    @After
    public void tearDown() throws Exception {
        try {
            RecoverInfrastructureTestHelper.killNodesWithStrongSigKill();
        } catch (NodesRecoveryProcessHelper.ProcessNotFoundException e) {
            RMTHelper.log("Cannot kill the node process: " + e.getMessage());
        }
    }

    @Test
    public void testRecoverLocalInfrastructureWithAliveNodes() throws Exception {

        this.createNodeSourceAndCheckState();

        RecoverInfrastructureTestHelper.killRmWithStrongSigKill();

        this.restartRmAndCheckFinalState();

        this.checkNodesStateAfterRecovery(NODE_NUMBER, 0);
    }

    @Test
    public void testRecoverLocalInfrastructureWithDownNodes() throws Exception {

        this.createNodeSourceAndCheckState();

        RecoverInfrastructureTestHelper.killRmAndNodesWithStrongSigKill();

        this.restartRmAndCheckFinalState();

        this.checkNodesStateAfterRecovery(0, NODE_NUMBER);
    }

    @Test
    public void testRecoverUndeployedNodeSource() throws Exception {

        this.defineNodeSourceAndCheckState();

        this.restartRmAndCheckFinalState();

        this.checkNodesStateAfterRecovery(0, 0);
    }

    private void startRmWithConfig(String configurationFilePath) throws Exception {
        String rmconf = new File(RMTHelper.class.getResource(configurationFilePath).toURI()).getAbsolutePath();
        this.rmHelper.startRM(rmconf);
        this.resourceManager = this.rmHelper.getResourceManager();
    }

    private void startRmAndCheckInitialState() throws Exception {
        // start RM
        this.startRmWithConfig(START_CONFIG);
        assertThat(PAResourceManagerProperties.RM_PRESERVE_NODES_ON_SHUTDOWN.getValueAsBoolean()).isTrue();
        assertThat(this.rmHelper.isRMStarted()).isTrue();

        // check the initial state of the RM
        assertThat(this.resourceManager.getState().getAllNodes().size()).isEqualTo(0);
    }

    private void createNodeSourceAndCheckState() throws Exception {
        this.rmHelper.createNodeSourceWithNodesRecoverable(NODE_SOURCE_NAME, NODE_NUMBER);
        RMMonitorEventReceiver resourceManagerMonitor = (RMMonitorEventReceiver) this.resourceManager;

        List<RMNodeSourceEvent> nodeSourceEventPerNodeSource = resourceManagerMonitor.getInitialState()
                                                                                     .getNodeSourceEvents();
        assertThat(nodeSourceEventPerNodeSource.size()).isEqualTo(1);

        RMNodeSourceEvent rmNodeSourceEvent = nodeSourceEventPerNodeSource.get(0);
        assertThat(rmNodeSourceEvent.getSourceName()).isEqualTo(NODE_SOURCE_NAME);
        assertThat(rmNodeSourceEvent.getNodeSourceStatus()).isEqualTo(NodeSourceStatus.NODES_DEPLOYED.toString());

        assertThat(resourceManagerMonitor.getState().getAllNodes().size()).isEqualTo(NODE_NUMBER);
    }

    private void defineNodeSourceAndCheckState() throws Exception {
        this.rmHelper.defineNodeSource(NODE_SOURCE_NAME, NODE_NUMBER);
        RMMonitorEventReceiver resourceManagerMonitor = (RMMonitorEventReceiver) this.resourceManager;
        List<RMNodeSourceEvent> nodeSourceEventPerNodeSource = resourceManagerMonitor.getInitialState()
                                                                                     .getNodeSourceEvents();
        assertThat(nodeSourceEventPerNodeSource.size()).isEqualTo(1);
        assertThat(nodeSourceEventPerNodeSource.get(0).getSourceName()).isEqualTo(NODE_SOURCE_NAME);
    }

    private void restartRmAndCheckFinalState() throws Exception {
        // restart RM
        this.rmHelper = new RMTHelper();
        this.startRmWithConfig(RESTART_CONFIG);
        assertThat(PAResourceManagerProperties.RM_PRESERVE_NODES_ON_SHUTDOWN.getValueAsBoolean()).isFalse();
        assertThat(this.rmHelper.isRMStarted()).isTrue();

        // re-snapshot the RM state
        RMMonitorEventReceiver resourceManagerMonitor = (RMMonitorEventReceiver) this.resourceManager;
        List<RMNodeSourceEvent> nodeSourceEvent = resourceManagerMonitor.getInitialState().getNodeSourceEvents();

        // the node source has been recovered on restart: we should have one node source with the same name
        assertThat(nodeSourceEvent.size()).isEqualTo(1);
        assertThat(nodeSourceEvent.get(0).getSourceName()).isEqualTo(NODE_SOURCE_NAME);
    }

    private void checkNodesStateAfterRecovery(int expectedNbAliveNodes, int expectedNbDownNodes) {
        RMMonitorEventReceiver resourceManagerMonitor = (RMMonitorEventReceiver) this.resourceManager;
        // the nodes should have been recovered too, and should be alive
        Set<String> allNodes = resourceManagerMonitor.getState().getAllNodes();
        assertThat(allNodes.size()).isEqualTo(expectedNbAliveNodes + expectedNbDownNodes);
        Set<String> nodeSourceNames = new HashSet<>();
        nodeSourceNames.add(NODE_SOURCE_NAME);
        Set<String> aliveNodeUrls = this.resourceManager.listAliveNodeUrls(nodeSourceNames);
        assertThat(aliveNodeUrls.size()).isEqualTo(expectedNbAliveNodes);

        List<RMNodeEvent> nodeEvents = resourceManagerMonitor.getInitialState().getNodeEvents();
        long nbFreeNodes = nodeEvents.stream()
                                     .filter(nodeEvent -> nodeEvent.getNodeState().equals(NodeState.FREE))
                                     .count();
        assertThat(nbFreeNodes).isEqualTo(expectedNbAliveNodes);
        long nbDownNodes = nodeEvents.stream()
                                     .filter(nodeEvent -> nodeEvent.getNodeState().equals(NodeState.DOWN))
                                     .count();
        assertThat(nbDownNodes).isEqualTo(expectedNbDownNodes);

        if (expectedNbAliveNodes > 0) {
            // the recovered nodes should be usable, try to lock/unlock them to see
            BooleanWrapper lockSucceeded = this.resourceManager.lockNodes(allNodes);
            assertThat(lockSucceeded).isEqualTo(new BooleanWrapper(true));
            BooleanWrapper unlockSucceeded = this.resourceManager.unlockNodes(allNodes);
            assertThat(unlockSucceeded).isEqualTo(new BooleanWrapper(true));
        }
    }

}
