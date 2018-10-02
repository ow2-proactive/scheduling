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
package functionaltests.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import functionaltests.utils.UserType;


/**
 * Test jobs status when the scheduler is paused
 */
public class TestJobWhenSchedulerPaused extends SchedulerFunctionalTestNoRestart {

    private static String simpleJobPath = "/functionaltests/descriptors/Job_simple.xml";

    private static String threeSleepingTasksJobPath = "/functionaltests/descriptors/Job_3tasks_2s.xml";

    private static URL simpleJobUrl = TestJobWhenSchedulerPaused.class.getResource(simpleJobPath);

    private static URL threeSleepingTasksJobUrl = TestJobWhenSchedulerPaused.class.getResource(threeSleepingTasksJobPath);

    private final static int EVENT_TIMEOUT = 5000;

    @Test
    public void testJobsDontRunWhenSchedulerPaused() throws Throwable {

        Scheduler schedAdminInterface = schedulerHelper.getSchedulerInterface(UserType.ADMIN);

        // Get the initial job number
        int initialJobsNumber = refreshAndGetTotalJobCount(schedAdminInterface);

        // Pause the scheduler
        schedAdminInterface.pause();
        schedulerHelper.waitForEventSchedulerState(SchedulerEvent.PAUSED);
        assertTrue(schedAdminInterface.getStatus().equals(SchedulerStatus.PAUSED));

        // Ensure that every job newly submitted will be pending
        JobId jobId1 = schedulerHelper.submitJob(new File(simpleJobUrl.toURI()).getAbsolutePath());
        JobId jobId2 = schedulerHelper.submitJob(new File(simpleJobUrl.toURI()).getAbsolutePath());

        schedulerHelper.waitForEventJobSubmitted(jobId1, EVENT_TIMEOUT);
        schedulerHelper.waitForEventJobSubmitted(jobId2, EVENT_TIMEOUT);

        // Without sleeping, we could could test if jobs are blocked in pending state ..
        // .. without waiting long enough that they run
        Thread.sleep(10000);

        JobState js1 = schedAdminInterface.getJobState(jobId1);
        assertEquals(JobStatus.PENDING, js1.getStatus());

        JobState js2 = schedAdminInterface.getJobState(jobId2);
        assertEquals(JobStatus.PENDING, js2.getStatus());

        // Kill + Remove + Clean + Resume
        schedulerHelper.killJob(jobId1.toString());
        schedulerHelper.killJob(jobId2.toString());

        schedulerHelper.removeJob(jobId1);
        schedulerHelper.removeJob(jobId2);
        schedulerHelper.waitForEventJobRemoved(jobId1, EVENT_TIMEOUT);
        schedulerHelper.waitForEventJobRemoved(jobId2, EVENT_TIMEOUT);

        assertEquals(initialJobsNumber, refreshAndGetTotalJobCount(schedAdminInterface));
        schedulerHelper.checkNodesAreClean();

        assertTrue(schedAdminInterface.resume());
    }

    @Test
    public void testRunningJobsTerminate() throws Throwable {

        Scheduler schedAdminInterface = schedulerHelper.getSchedulerInterface(UserType.ADMIN);

        // Get the initial job number
        int initialJobsNumber = refreshAndGetTotalJobCount(schedAdminInterface);

        // Submit a job
        JobId jobId = schedulerHelper.submitJob(new File(threeSleepingTasksJobUrl.toURI()).getAbsolutePath());

        // Ensure it is running
        schedulerHelper.waitForEventJobRunning(jobId);

        // Pause the scheduler and ensure it is paused
        schedAdminInterface.pause();
        schedulerHelper.waitForEventSchedulerState(SchedulerEvent.PAUSED);
        assertTrue(schedAdminInterface.getStatus().equals(SchedulerStatus.PAUSED));

        // Ensure the job terminates
        schedulerHelper.waitForEventJobFinished(jobId, 30000);
        assertEquals(JobStatus.FINISHED, schedAdminInterface.getJobState(jobId).getStatus());

        // Kill + Remove + Clean + Resume
        schedulerHelper.killJob(jobId.toString());

        schedulerHelper.removeJob(jobId);
        schedulerHelper.waitForEventJobRemoved(jobId, EVENT_TIMEOUT);

        assertEquals(initialJobsNumber, refreshAndGetTotalJobCount(schedAdminInterface));
        schedulerHelper.checkNodesAreClean();

        assertTrue(schedAdminInterface.resume());
    }

    private int refreshAndGetTotalJobCount(Scheduler schedAdminInterface) throws Exception {
        return getTotalJobsCount(schedAdminInterface.getState());
    }

    private int getTotalJobsCount(SchedulerState state) {
        return state.getFinishedJobs().size() + state.getPendingJobs().size() + state.getRunningJobs().size();
    }
}
