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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;

import functionaltests.monitor.RMMonitorEventReceiver;
import functionaltests.utils.NodesRecoveryProcessHelper;
import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;


/**
 * @author ActiveEon Team
 * @since 22/06/17
 */
public class RecoverLocalInfrastructureTest extends RMFunctionalTest {

    private static final String START_CONFIG = "/functionaltests/config/functionalTRMProperties-RM-restart-clean-db.ini";

    private static final String RESTART_CONFIG = "/functionaltests/config/functionalTRMProperties-RM-restart-keep-db.ini";

    private static final String NODE_SOURCE_NAME = "LocalNodeSource" +
                                                   RecoverLocalInfrastructureTest.class.getSimpleName();

    private static final int NODE_NUMBER = 3;

    private ResourceManager resourceManager = null;

    @Before
    public void prepareRM() throws Exception {
        startRmAndCheckInitialState();
    }

    @After
    public void killNodes() throws Exception {
        // kill the remaining nodes that were preserved for the test
        try {
            NodesRecoveryProcessHelper.findRmPidAndSendSigKill("RMNodeStarter");
        } catch (Exception e) {
            // we know that doing this will cause exceptions, keep silent after the test
        }
    }

    @Test
    public void testRecoverLocalInfrastructureWithAliveNodes() throws Exception {
        // kill the RM only by sending a SIGKILL and leave node processes alive
        NodesRecoveryProcessHelper.findRmPidAndSendSigKill("RMStarterForFunctionalTest");
        // nodes should be re-taken into account by the restarted RM
        restartRmAndCheckFinalState(false);
    }

    @Test
    public void testRecoverLocalInfrastructureWithDownNodes() throws Exception {
        // kill the RM and also kill all node processes
        rmHelper.killRM();
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
        Assert.assertTrue("Nodes preservation should be activated on RM start for the test",
                          PAResourceManagerProperties.RM_PRESERVE_NODES_ON_EXIT.getValueAsBoolean());
        assertThat(rmHelper.isRMStarted()).isTrue();

        // check the initial state of the RM
        assertThat(resourceManager.getState().getAllNodes().size()).isEqualTo(0);
        rmHelper.createNodeSource(NODE_SOURCE_NAME, NODE_NUMBER);
        RMMonitorEventReceiver resourceManagerMonitor = (RMMonitorEventReceiver) resourceManager;
        ArrayList<RMNodeSourceEvent> nodeSourceEventPerNodeSource = resourceManagerMonitor.getInitialState()
                                                                                          .getNodeSource();
        assertThat(nodeSourceEventPerNodeSource.size()).isEqualTo(1);
        assertThat(nodeSourceEventPerNodeSource.get(0).getSourceName()).isEqualTo(NODE_SOURCE_NAME);
        assertThat(resourceManagerMonitor.getState().getAllNodes().size()).isEqualTo(NODE_NUMBER);
    }

    private void restartRmAndCheckFinalState(boolean nodesShouldBeRecreated) throws Exception {
        // restart RM
        rmHelper = new RMTHelper();
        startRmWithConfig(RESTART_CONFIG);
        Assert.assertFalse("Nodes preservation should not be activated on RM restart for the test",
                           PAResourceManagerProperties.RM_PRESERVE_NODES_ON_EXIT.getValueAsBoolean());
        assertThat(rmHelper.isRMStarted()).isTrue();

        // re-snapshot the RM state
        RMMonitorEventReceiver resourceManagerMonitor = (RMMonitorEventReceiver) resourceManager;
        ArrayList<RMNodeSourceEvent> nodeSourceEvent = resourceManagerMonitor.getInitialState().getNodeSource();

        // the node source has been recovered on restart: we should have one node source with the same name
        assertThat(nodeSourceEvent.size()).isEqualTo(1);
        assertThat(nodeSourceEvent.get(0).getSourceName()).isEqualTo(NODE_SOURCE_NAME);

        // wait for nodes to be recreated if needed
        if (nodesShouldBeRecreated) {
            rmHelper.waitForAnyMultipleNodeEvent(RMEventType.NODE_STATE_CHANGED, NODE_NUMBER);
        }

        // the nodes should have been recovered too, and should be alive
        Set<String> allNodes = resourceManagerMonitor.getState().getAllNodes();
        assertThat(allNodes.size()).isEqualTo(NODE_NUMBER);
        Set<String> nodeSourceNames = new HashSet<>();
        nodeSourceNames.add(NODE_SOURCE_NAME);
        Set<String> aliveNodeUrls = resourceManager.listAliveNodeUrls(nodeSourceNames);
        assertThat(aliveNodeUrls.size()).isEqualTo(NODE_NUMBER);

        // the recovered nodes should be usable, try to lock/unlock them to see
        BooleanWrapper lockSucceeded = resourceManager.lockNodes(allNodes);
        assertThat(lockSucceeded).isEqualTo(new BooleanWrapper(true));
        BooleanWrapper unlockSucceeded = resourceManager.unlockNodes(allNodes);
        assertThat(unlockSucceeded).isEqualTo(new BooleanWrapper(true));
    }

}
