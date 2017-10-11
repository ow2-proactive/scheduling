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
package functionaltests.rm;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;

import functionaltests.job.taskkill.TestProcessTreeKiller;
import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;
import functionaltests.utils.TestNode;


/**
 * This test ensures that a node dies after the scheduler is restarted (and all nodes have been removed)
 */
@Ignore("test cannot work until the RM_PRESERVE_NODES_ON_EXIT property is merged")
public class TestNodeDiesAtSchedulerRestart extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    @BeforeClass
    public static void startDedicatedScheduler() throws Exception {
        schedulerHelper = new SchedulerTHelper(true, true);
    }

    @Test(timeout = 300000)
    public void nodeDiesAtSchedulerRestart() throws Throwable {
        testNode = schedulerHelper.createRMNodeStarterNode("nodeDiesAtSchedulerRestart");
        schedulerHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED, 120000);
        schedulerHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED, 10000);

        schedulerHelper.killScheduler();

        Assert.assertFalse(testNode.getNodeProcess().isFinished());

        schedulerHelper = new SchedulerTHelper(true, true);

        int exitCode = testNode.getNodeProcess().waitFor();
        Assert.assertEquals(306, exitCode);
    }

}
