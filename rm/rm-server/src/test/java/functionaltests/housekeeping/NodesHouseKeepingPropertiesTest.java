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
package functionaltests.housekeeping;

import static com.google.common.truth.Truth.assertThat;

import java.io.File;

import org.junit.Test;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;

import functionaltests.nodesrecovery.NodesRecoveryProcessHelper;
import functionaltests.nodesrecovery.RecoverInfrastructureTestHelper;
import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;


public class NodesHouseKeepingPropertiesTest extends RMFunctionalTest {

    private static final String RM_CONFIG_FILE_PATH = "/functionaltests/config/functionalTRMProperties-remove-nodes-unavailable-for-two-min-every-min.ini";

    private static final String NODE_SOURCE_NAME = "LocalNodeSource-" +
                                                   NodesHouseKeepingPropertiesTest.class.getSimpleName();

    private static final int NODE_NUMBER = 4;

    private static final String EVERY_MINUTE_CRON_EXPRESSION = "* * * * *";

    private static final int REMOVE_DOWN_NODES_AFTER_MINUTES = 2;

    @Test
    public void testDownNodesAreRemovedAfterUnavailableMaxPeriodExpired() throws Exception {
        String rmConfigurationFile = new File(RMTHelper.class.getResource(RM_CONFIG_FILE_PATH)
                                                             .toURI()).getAbsolutePath();
        this.rmHelper.startRM(rmConfigurationFile);
        ResourceManager resourceManager = this.rmHelper.getResourceManager();

        assertThat(PAResourceManagerProperties.RM_UNAVAILABLE_NODES_REMOVAL_FREQUENCY.getValueAsString()).isEqualTo(EVERY_MINUTE_CRON_EXPRESSION);
        assertThat(PAResourceManagerProperties.RM_UNAVAILABLE_NODES_MAX_PERIOD.getValueAsInt()).isEqualTo(REMOVE_DOWN_NODES_AFTER_MINUTES);

        assertThat(this.rmHelper.isRMStarted()).isTrue();
        assertThat(resourceManager.getState().getAllNodes().size()).isEqualTo(0);

        this.rmHelper.createNodeSource(NODE_SOURCE_NAME, NODE_NUMBER);
        assertThat(resourceManager.getState().getAllNodes().size()).isEqualTo(4);

        findNodeProcessAndKill();

        this.rmHelper.waitForAnyMultipleNodeEvent(RMEventType.NODE_STATE_CHANGED, NODE_NUMBER);
        assertThat(resourceManager.listAliveNodeUrls()).isEmpty();
        assertThat(resourceManager.getState().getAllNodes().size()).isEqualTo(4);

        int expiredDownNodeMillis = REMOVE_DOWN_NODES_AFTER_MINUTES * 60 * 1000;
        int nextDownNodesRemovalAttemptMillis = 60 * 1000;
        Thread.sleep(expiredDownNodeMillis + nextDownNodesRemovalAttemptMillis); //NOSONAR we need to wait for the cron tick
        assertThat(resourceManager.getState().getAllNodes().size()).isEqualTo(0);
    }

    private void findNodeProcessAndKill() throws Exception {
        boolean nodeProcessFound = true;
        while (nodeProcessFound) {
            try {
                RecoverInfrastructureTestHelper.killNodesWithStrongSigKill();
            } catch (NodesRecoveryProcessHelper.ProcessNotFoundException e) {
                nodeProcessFound = false;
            }
        }
    }

}
