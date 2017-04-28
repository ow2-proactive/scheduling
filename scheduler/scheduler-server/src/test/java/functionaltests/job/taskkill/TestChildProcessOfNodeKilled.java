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
package functionaltests.job.taskkill;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;

import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;
import functionaltests.utils.TestNode;


public class TestChildProcessOfNodeKilled extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    @BeforeClass
    public static void startDedicatedScheduler() throws Exception {
        schedulerHelper = new SchedulerTHelper(true, true);
    }

    @Test
    public void childProcessesForkedByTaskAreCleanedUpWhenRMNodeStarterIsKilled() throws Throwable {
        TestNode tNode = startSchedulerAndRMWithOneNode();
        startJobForkingProcesses();

        schedulerHelper.killNode(tNode.getNode().getNodeInformation().getURL());

        TestProcessTreeKiller.waitUntilAllForkedProcessesAreKilled();
    }

    private void startJobForkingProcesses() throws Exception {
        TaskFlowJob jobForkingProcesses = TestProcessTreeKiller.createJavaExecutableJob("test", false);
        schedulerHelper.submitJob(jobForkingProcesses);
        TestProcessTreeKiller.waitUntilForkedProcessesAreRunning(TestProcessTreeKiller.detachedProcNumber);
    }

    private TestNode startSchedulerAndRMWithOneNode() throws Exception {
        ResourceManager resourceManager = schedulerHelper.getResourceManager();
        testNode = schedulerHelper.createRMNodeStarterNode("test1");
        resourceManager.addNode(testNode.getNode().getNodeInformation().getURL());
        return testNode;
    }

}
