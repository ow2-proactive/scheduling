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

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.ow2.proactive.utils.Lambda.silent;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobStatus;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;


/**
 * This test checks that when we mark failed task as finished,
 * Scheduler does not release node, which is busy with other task.
 */
public class TestMarkedAsFinished extends SchedulerFunctionalTestNoRestart {

    private static final URL failingJob = TestMarkedAsFinished.class.getResource("/functionaltests/descriptors/Job_failing.xml");

    private static final URL normalJob = TestMarkedAsFinished.class.getResource("/functionaltests/descriptors/Job_endless.xml");

    @Test
    public void test() throws Throwable {

        // so we look all nodes except one
        // we need to have only one node available to run
        final ResourceManager resourceManager = schedulerHelper.getResourceManager();
        final List<String> allUrls = new ArrayList<>(resourceManager.listAliveNodeUrls());
        allUrls.remove(0);
        resourceManager.lockNodes(new HashSet<>(allUrls));

        JobId failingJobId = schedulerHelper.submitJob(new File(failingJob.toURI()).getAbsolutePath());

        // task failure will make whole job paused. Because onTaskError="pauseJob"
        await().until(silent(() -> schedulerHelper.getSchedulerInterface().getJobState(failingJobId).getStatus()),
                      equalTo(JobStatus.PAUSED));

        // submit normal job which should execute for a quite long time
        JobId normalJobId = schedulerHelper.submitJob(new File(normalJob.toURI()).getAbsolutePath());

        // wait until normal job is running
        schedulerHelper.waitForEventJobRunning(normalJobId);

        assertEquals(JobStatus.PAUSED, schedulerHelper.getSchedulerInterface().getJobState(failingJobId).getStatus());
        assertEquals(JobStatus.RUNNING, schedulerHelper.getSchedulerInterface().getJobState(normalJobId).getStatus());

        // after we mark as finished failed task, failing job should become FINISHED
        // however, even more important, that other job is continue to run
        schedulerHelper.getSchedulerInterface().finishInErrorTask(failingJobId.value(), "Error_Task");
        Thread.sleep(10000);

        assertEquals(JobStatus.FINISHED, schedulerHelper.getSchedulerInterface().getJobState(failingJobId).getStatus());
        assertEquals(JobStatus.RUNNING, schedulerHelper.getSchedulerInterface().getJobState(normalJobId).getStatus());

    }
}
