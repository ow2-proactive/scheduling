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
package performancetests.recovery;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;

import functionaltests.nodesrecovery.RecoverInfrastructureTestHelper;
import functionaltests.utils.*;
import performancetests.helper.LogProcessor;


/**
 * @author ActiveEon Team
 * @since 01/12/17
 */
@RunWith(Parameterized.class)
public class NodeRecoveryTest extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    static final String RM_CONFIGURATION_START = NodeRecoveryTest.class.getResource("/performancetests/config/rm-start.ini")
                                                                       .getPath();

    static final String RM_CONFIGURATION_RESTART = NodeRecoveryTest.class.getResource("/performancetests/config/rm-restart.ini")
                                                                         .getPath();

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { 5, 1000 }, { 100, 10000 } });
    }

    // number of nodes
    int nodesNumber;

    // time limit in milliseconds for test to pass
    int timeLimit;

    public NodeRecoveryTest(int nodesNumber, int timeLimit) {
        this.nodesNumber = nodesNumber;
        this.timeLimit = timeLimit;
    }

    private RMTHelper rmHelper;

    /**
     * This method tests performance of node recovery using local infrastructure.
     *
     * @throws Exception
     */
    @Before
    public void startKillStartRM() throws Exception {
        ProActiveConfiguration.load();
        rmHelper = new RMTHelper();
        rmHelper.startRM(RM_CONFIGURATION_START);

        assertTrue(PAResourceManagerProperties.RM_PRESERVE_NODES_ON_SHUTDOWN.getValueAsBoolean());
        assertTrue(rmHelper.isRMStarted());

        ResourceManager resourceManager = rmHelper.getResourceManager();

        assertEquals(0, resourceManager.getState().getAllNodes().size());

        rmHelper.createNodeSource(RMConstants.DEFAULT_STATIC_SOURCE_NAME, nodesNumber);

        assertEquals(nodesNumber, resourceManager.getState().getAllNodes().size());

        RecoverInfrastructureTestHelper.killRmWithStrongSigKill();

        rmHelper.startRM(RM_CONFIGURATION_RESTART);

        resourceManager = rmHelper.getResourceManager();

        assertEquals(nodesNumber, resourceManager.getState().getAllNodes().size());
    }

    @Test
    public void test() {
        assertEquals(nodesNumber, nodesRecovered());
        assertThat("Nodes recovery time for " + nodesNumber + " nodes",
                   (int) timeSpentToRecoverNodes(),
                   lessThan(timeLimit));
    }

    @After
    public void shutdownRmHelper() throws Exception {
        try {
            rmHelper.removeNodeSource(RMConstants.DEFAULT_STATIC_SOURCE_NAME);
            rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, RMConstants.DEFAULT_STATIC_SOURCE_NAME);
        } catch (Exception ignored) {

        }
        rmHelper.shutdownRM();
    }

    private long nodesRecovered() {
        final String line = LogProcessor.getFirstLineThatMatch(RMCore.END_OF_NODES_RECOVERY);
        final List<Integer> numbersFromLine = LogProcessor.getNumbersFromLine(line);

        if (!numbersFromLine.isEmpty()) {
            return numbersFromLine.get(0);
        } else {
            throw new RuntimeException("Cannot retrieve number of nodes recovered from this line: " + line);
        }
    }

    private long timeSpentToRecoverNodes() {
        long time = LogProcessor.millisecondsFromTo(RMCore.START_TO_RECOVER_NODES, RMCore.END_OF_NODES_RECOVERY);

        if (time < 0) {
            throw new RuntimeException("First occurence of " + RMCore.START_TO_RECOVER_NODES + " goes after " +
                                       RMCore.END_OF_NODES_RECOVERY);
        }

        return time;
    }

}
