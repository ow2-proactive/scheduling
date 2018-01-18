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
package functionaltests.scripts.clean;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.common.job.JobId;

import functionaltests.monitor.RMMonitorsHandler;
import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import functionaltests.utils.SchedulerFunctionalTestWithRestart;


/**
 * Test checks that a cleaning script on a multi-node task terminates and does not keep nodes busy
 */
public class TestCleaningScriptMultiNodes extends SchedulerFunctionalTestNoRestart {

    private static URL jobDescriptor = TestCleaningScriptMultiNodes.class.getResource("/functionaltests/descriptors/Job_CleaningScript_multinode.xml");

    @Test
    public void testCleaningScriptMultiNodes() throws Throwable {
        //submit job
        JobId id = schedulerHelper.submitJob(new File(jobDescriptor.toURI()).getAbsolutePath());
        //connect to RM
        schedulerHelper.addExtraNodes(4);

        //wait job is running
        schedulerHelper.waitForEventJobRunning(id);

        //wait for job to be finished
        schedulerHelper.waitForEventJobFinished(id);

        // the job is finished before the last tasks cleaning scripts are complete, so we need to wait a bit
        schedulerHelper.log("Wait for the nodes the be released");
        waitUntilAllNodesAreFreeOrFail(schedulerHelper.getResourceManager(), 30);

    }

    private void waitUntilAllNodesAreFreeOrFail(ResourceManager rm, int nbSeconds) throws InterruptedException {
        int cpt = nbSeconds;
        boolean busyNodes = true;
        do {
            Thread.sleep(1000);
            cpt--;
            busyNodes = !rm.listAliveNodeUrls().equals(rm.getState().getFreeNodes());
        } while (busyNodes && cpt > 0);
        if (busyNodes) {
            Assert.fail("Busy nodes remaining after " + nbSeconds + " seconds");
        }

    }
}
