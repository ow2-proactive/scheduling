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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.SSHInfrastructureV2;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;

import functionaltests.monitor.RMMonitorEventReceiver;
import functionaltests.nodesource.TestSSHInfrastructureV2;
import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;


/**
 * @author ActiveEon Team
 * @since 22/06/17
 */
public class RecoverSSHInfrastructureV2Test extends RMFunctionalTest {

    private static final String START_CONFIG = "/functionaltests/config/functionalTRMProperties-RM-restart-clean-db.ini";

    private static final String RESTART_CONFIG = "/functionaltests/config/functionalTRMProperties-RM-restart-keep-db.ini";

    private static final String NODE_SOURCE_NAME = "testRecoverSSHInfra";

    private ResourceManager resourceManager = null;

    @Before
    public void setup() throws Exception {
        TestSSHInfrastructureV2.startSSHServer();
        RMTHelper.log("SSH server started");
        startRmAndCheckInitialState();
    }

    @After
    public void tearDown() throws Exception {
        // kill the remaining nodes that were preserved for the test
        RecoverInfrastructureTestHelper.killNodesWithStrongSigKill();
        TestSSHInfrastructureV2.stopSSHServer();
    }

    @Test
    public void testRecoverSSHInfrastructureV2WithAliveNodes() throws Exception {
        // kill only the RM by sending a SIGKILL and leave node processes alive
        RecoverInfrastructureTestHelper.killRmWithStrongSigKill();
        // nodes should be re-taken into account by the restarted RM
        restartRmAndCheckFinalState(false);
    }

    @Test
    public void testRecoverSSHInfrastructureV2WithDownNodes() throws Exception {
        // kill RM and nodes with SIGKILL
        RecoverInfrastructureTestHelper.killRmAndNodesWithStrongSigKill();
        // nodes should be re-deployed by the restarted RM
        restartRmAndCheckFinalState(true);
    }

    private void startRmWithConfig(String configurationFilePath) throws Exception {
        String rmconf = new File(RMTHelper.class.getResource(configurationFilePath).toURI()).getAbsolutePath();
        rmHelper.startRM(rmconf);
        resourceManager = rmHelper.getResourceManager();
    }

    private void startRmAndCheckInitialState() throws Exception {
        // start RM
        startRmWithConfig(START_CONFIG);
        assertThat(PAResourceManagerProperties.RM_PRESERVE_NODES_ON_SHUTDOWN.getValueAsBoolean()).isTrue();
        assertThat(rmHelper.isRMStarted()).isTrue();

        // check the initial state of the RM
        assertThat(resourceManager.getState().getAllNodes().size()).isEqualTo(0);

        resourceManager.createNodeSource(NODE_SOURCE_NAME,
                                         SSHInfrastructureV2.class.getName(),
                                         TestSSHInfrastructureV2.infraParams,
                                         StaticPolicy.class.getName(),
                                         TestSSHInfrastructureV2.policyParameters);
        RMTHelper.waitForNodeSourceCreation(NODE_SOURCE_NAME,
                                            TestSSHInfrastructureV2.NB_NODES,
                                            this.rmHelper.getMonitorsHandler());

        RMMonitorEventReceiver resourceManagerMonitor = (RMMonitorEventReceiver) resourceManager;
        ArrayList<RMNodeSourceEvent> nodeSourceEvent = resourceManagerMonitor.getInitialState().getNodeSource();
        assertThat(nodeSourceEvent.size()).isEqualTo(1);
        assertThat(nodeSourceEvent.get(0).getSourceName()).isEqualTo(NODE_SOURCE_NAME);
        assertThat(resourceManagerMonitor.getState().getAllNodes().size()).isEqualTo(TestSSHInfrastructureV2.NB_NODES);
    }

    private void restartRmAndCheckFinalState(boolean nodesShouldBeRecreated) throws Exception {
        // restart RM
        rmHelper = new RMTHelper();
        startRmWithConfig(RESTART_CONFIG);
        assertThat(PAResourceManagerProperties.RM_PRESERVE_NODES_ON_SHUTDOWN.getValueAsBoolean()).isFalse();
        assertThat(rmHelper.isRMStarted()).isTrue();

        // re-snapshot the RM state
        RMMonitorEventReceiver resourceManagerMonitor = (RMMonitorEventReceiver) resourceManager;
        ArrayList<RMNodeSourceEvent> nodeSourceEvent = resourceManagerMonitor.getInitialState().getNodeSource();

        // the node source has been recovered on restart: we should have one node source with the same name
        assertThat(nodeSourceEvent.size()).isEqualTo(1);
        assertThat(nodeSourceEvent.get(0).getSourceName()).isEqualTo(NODE_SOURCE_NAME);

        // wait for nodes to be recreated if needed
        if (nodesShouldBeRecreated) {
            rmHelper.waitForAnyMultipleNodeEvent(RMEventType.NODE_STATE_CHANGED, TestSSHInfrastructureV2.NB_NODES);
        }

        // the nodes should have been recovered too, and should be alive
        Set<String> allNodes = resourceManagerMonitor.getState().getAllNodes();
        assertThat(allNodes.size()).isEqualTo(TestSSHInfrastructureV2.NB_NODES);
        Set<String> nodeSourceNames = new HashSet<>();
        nodeSourceNames.add(NODE_SOURCE_NAME);
        Set<String> aliveNodeUrls = resourceManager.listAliveNodeUrls(nodeSourceNames);
        assertThat(aliveNodeUrls.size()).isEqualTo(TestSSHInfrastructureV2.NB_NODES);

        // the recovered nodes should be usable, try to lock/unlock them to see
        BooleanWrapper lockSucceeded = resourceManager.lockNodes(allNodes);
        assertThat(lockSucceeded).isEqualTo(new BooleanWrapper(true));
        BooleanWrapper unlockSucceeded = resourceManager.unlockNodes(allNodes);
        assertThat(unlockSucceeded).isEqualTo(new BooleanWrapper(true));
    }

}
