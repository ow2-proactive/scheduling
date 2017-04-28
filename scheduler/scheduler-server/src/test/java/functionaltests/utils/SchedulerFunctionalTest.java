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
package functionaltests.utils;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.tests.ProActiveTest;


/**
 * 
 * The parent class for all consecutive functional tests.
 *
 */
public class SchedulerFunctionalTest extends ProActiveTest {

    protected static final Logger logger = Logger.getLogger("SchedulerTests");

    protected static SchedulerTHelper schedulerHelper;

    protected TestNode testNode;

    protected List<TestNode> testNodes = new ArrayList<>();

    @Rule
    public Timeout testTimeout = new Timeout(CentralPAPropertyRepository.PA_TEST_TIMEOUT.getValue(),
                                             TimeUnit.MILLISECONDS);

    protected Job parseXml(String workflowFile) throws JobCreationException {
        return Jobs.parseXml(getClass().getResource(workflowFile).getPath());
    }

    /**
     * Kill all standalone nodes created by the test
     */
    @After
    public void killTestNodes() {
        try {
            if (testNode != null) {
                testNode.kill();
            }
        } catch (InterruptedException e) {
        }
        for (TestNode tn : testNodes) {
            try {
                tn.kill();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Remove the "extra" node source and checks that the number of alive nodes after the test is the default
     *
     * @throws Exception
     */
    public static void cleanupScheduler() throws Exception {
        if (schedulerHelper.isStarted()) {
            SchedulerTHelper.log("Do not kill the scheduler after test, but clean extra nodes.");
            schedulerHelper.removeExtraNodeSource();

            ResourceManager resourceManager = schedulerHelper.getResourceManager();
            int numberOfNodesAfterTest = resourceManager.listAliveNodeUrls().size();

            if (resourceManager.listAliveNodeUrls().size() != RMTHelper.DEFAULT_NODES_NUMBER) {
                SchedulerTHelper.log("Unexpected number of nodes after test: " + numberOfNodesAfterTest +
                                     ", scheduler will be restarted and test declared failing.");
                schedulerHelper.killScheduler();
                fail("Unexpected number of nodes after test : " + numberOfNodesAfterTest);
            }
        }
    }

}
