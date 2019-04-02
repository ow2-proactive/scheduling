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
package functionaltests.policy.license;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.python.google.common.collect.ImmutableMap;

import functionaltests.utils.SchedulerFunctionalTestLicensePolicy;


public class TestLicensePolicy extends SchedulerFunctionalTestLicensePolicy {

    private static URL JobSimpleJobLicensePolicy = TestLicensePolicy.class.getResource("/functionaltests/descriptors/Job_simple_job_license_policy.xml");

    private static URL JobSimpleTaskLicensePolicy = TestLicensePolicy.class.getResource("/functionaltests/descriptors/Job_simple_task_license_policy.xml");

    private static URL JobSimpleJobAndTaskLicensePolicy = TestLicensePolicy.class.getResource("/functionaltests/descriptors/Job_simple_job_and_task_license_policy.xml");

    private static URL JobSimpleNoRunnableTaskLicensePolicy = TestLicensePolicy.class.getResource("/functionaltests/descriptors/Job_simple_no_runnable_task_license_policy.xml");

    @Test
    public void testJobsUseSameLicenseExclusiveExecution() throws Throwable {

        JobId jobId0 = schedulerHelper.submitJob(new File(JobSimpleJobLicensePolicy.toURI()).getAbsolutePath(),
                                                 ImmutableMap.of("LICENSES", "software_A"));
        JobId jobId1 = schedulerHelper.submitJob(new File(JobSimpleJobLicensePolicy.toURI()).getAbsolutePath(),
                                                 ImmutableMap.of("LICENSES", "software_A"));

        log("Waiting for jobs finished");
        schedulerHelper.waitForEventJobFinished(jobId0);
        schedulerHelper.waitForEventJobFinished(jobId1);

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();
        JobState jobState0 = scheduler.getJobState(jobId0);
        JobState jobState1 = scheduler.getJobState(jobId1);

        boolean jobsExecutedOneByOne = (jobState0.getFinishedTime() < jobState1.getStartTime()) ||
                                       (jobState1.getFinishedTime() < jobState0.getStartTime());
        Assert.assertTrue(jobsExecutedOneByOne);
    }

    @Test
    public void testJobsUseDistinctLicensesParallelExecution() throws Throwable {

        JobId jobId0 = schedulerHelper.submitJob(new File(JobSimpleJobLicensePolicy.toURI()).getAbsolutePath(),
                                                 ImmutableMap.of("LICENSES", "software_A"));
        JobId jobId1 = schedulerHelper.submitJob(new File(JobSimpleJobLicensePolicy.toURI()).getAbsolutePath(),
                                                 ImmutableMap.of("LICENSES", "software_B"));

        log("Waiting for jobs finished");
        schedulerHelper.waitForEventJobFinished(jobId0);
        schedulerHelper.waitForEventJobFinished(jobId1);

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();
        JobState jobState0 = scheduler.getJobState(jobId0);
        JobState jobState1 = scheduler.getJobState(jobId1);

        boolean jobsExecutedInParallel = !((jobState0.getFinishedTime() < jobState1.getStartTime()) ||
                                           (jobState1.getFinishedTime() < jobState0.getStartTime()));
        Assert.assertTrue(jobsExecutedInParallel);
    }

    /**
     * Tests that two independent tasks do not run at the same time, due to license limitation.
     *
     * @throws Exception
     */

    @Test
    public void testTasksUseSameLicenseExclusiveExecution() throws Throwable {

        JobId jobId = schedulerHelper.submitJob(new File(JobSimpleTaskLicensePolicy.toURI()).getAbsolutePath(),
                                                ImmutableMap.of("LICENSES", "software_A", "LICENSES_2", "software_A"));

        log("Waiting for job finished");
        schedulerHelper.waitForEventJobFinished(jobId);

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();
        TaskState taskState0 = scheduler.getJobState(jobId).getTasks().get(0);
        TaskState taskState1 = scheduler.getJobState(jobId).getTasks().get(1);

        boolean tasksExecutedOneByOne = (taskState0.getFinishedTime() < taskState1.getStartTime()) ||
                                        (taskState1.getFinishedTime() < taskState0.getStartTime());
        Assert.assertTrue(tasksExecutedOneByOne);
    }

    @Test
    public void testTasksUseDistinctLicensesParallelExecution() throws Throwable {

        JobId jobId = schedulerHelper.submitJob(new File(JobSimpleTaskLicensePolicy.toURI()).getAbsolutePath(),
                                                ImmutableMap.of("LICENSES", "software_A", "LICENSES_2", "software_B"));

        log("Waiting for job finished");
        schedulerHelper.waitForEventJobFinished(jobId);

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();
        TaskState taskState0 = scheduler.getJobState(jobId).getTasks().get(0);
        TaskState taskState1 = scheduler.getJobState(jobId).getTasks().get(1);

        boolean tasksExecutedInParallel = !((taskState0.getFinishedTime() < taskState1.getStartTime()) ||
                                            (taskState1.getFinishedTime() < taskState0.getStartTime()));
        Assert.assertTrue(tasksExecutedInParallel);
    }

    @Test
    public void testInterleavedTasksExecution() throws Throwable {

        JobId jobId0 = schedulerHelper.submitJob(new File(JobSimpleJobAndTaskLicensePolicy.toURI()).getAbsolutePath(),
                                                 ImmutableMap.of("LICENSES_JOB",
                                                                 "software_A",
                                                                 "LICENSES_TASK",
                                                                 "software_C"));
        JobId jobId1 = schedulerHelper.submitJob(new File(JobSimpleJobAndTaskLicensePolicy.toURI()).getAbsolutePath(),
                                                 ImmutableMap.of("LICENSES_JOB",
                                                                 "software_B",
                                                                 "LICENSES_TASK",
                                                                 "software_C"));

        log("Waiting for job finished");
        schedulerHelper.waitForEventJobFinished(jobId0);
        schedulerHelper.waitForEventJobFinished(jobId1);

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        TaskState taskStateParallel0 = scheduler.getJobState(jobId0).getTasks().get(0);
        TaskState taskStateParallel1 = scheduler.getJobState(jobId1).getTasks().get(0);
        TaskState taskStateExclusive0 = scheduler.getJobState(jobId0).getTasks().get(1);
        TaskState taskStateExclusive1 = scheduler.getJobState(jobId1).getTasks().get(1);

        boolean tasksExecutedInParallel = !((taskStateParallel0.getFinishedTime() < taskStateParallel1.getStartTime()) ||
                                            (taskStateParallel1.getFinishedTime() < taskStateParallel0.getStartTime()));
        Assert.assertTrue(taskStateParallel0.getFinishedTime() + " " + taskStateParallel1.getStartTime() + " " +
                          taskStateParallel1.getFinishedTime() + " " + taskStateParallel0.getStartTime(),
                          tasksExecutedInParallel);

        boolean tasksExecutedOneByOne = (taskStateExclusive0.getFinishedTime() < taskStateExclusive1.getStartTime()) ||
                                        (taskStateExclusive1.getFinishedTime() < taskStateExclusive0.getStartTime());
        Assert.assertTrue(taskStateExclusive0.getFinishedTime() + " " + taskStateExclusive1.getStartTime() + " " +
                          taskStateExclusive1.getFinishedTime() + " " + taskStateExclusive0.getStartTime(),
                          tasksExecutedOneByOne);
    }

    @Test
    public void testTaskUseShareSameLicenseOfItsJobAndFinish() throws Throwable {

        JobId jobId = schedulerHelper.submitJob(new File(JobSimpleJobAndTaskLicensePolicy.toURI()).getAbsolutePath(),
                                                ImmutableMap.of("LICENSES_JOB",
                                                                "software_A",
                                                                "LICENSES_TASK",
                                                                "software_A"));
        log("Waiting for job finished");
        schedulerHelper.waitForEventJobFinished(jobId);
        Assert.assertTrue(true);
    }

    @Test
    public void testJobAndTaskUseSameLicenseExclusiveExecution() throws Throwable {

        JobId jobId0 = schedulerHelper.submitJob(new File(JobSimpleTaskLicensePolicy.toURI()).getAbsolutePath(),
                                                 ImmutableMap.of("LICENSES", "software_A", "LICENSES_2", "software_B"));

        JobId jobId1 = schedulerHelper.submitJob(new File(JobSimpleJobLicensePolicy.toURI()).getAbsolutePath(),
                                                 ImmutableMap.of("LICENSES", "software_A"));

        log("Waiting for jobs finished");
        schedulerHelper.waitForEventJobFinished(jobId0);
        schedulerHelper.waitForEventJobFinished(jobId1);

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();
        TaskState taskStateJob0 = scheduler.getJobState(jobId0).getTasks().get(0);
        JobState jobState1 = scheduler.getJobState(jobId1);

        boolean taskAndJobExecutedOneByOne = (taskStateJob0.getFinishedTime() < jobState1.getStartTime()) ||
                                             (jobState1.getFinishedTime() < taskStateJob0.getStartTime());
        Assert.assertTrue(taskAndJobExecutedOneByOne);
    }

    @Test
    public void testNonRunnableTaskNotBlockingTokens() throws Throwable {

        JobId jobId0 = schedulerHelper.submitJob(new File(JobSimpleNoRunnableTaskLicensePolicy.toURI()).getAbsolutePath(),
                                                 ImmutableMap.of("LICENSES", "software_A", "LICENSES_2", "software_B"));

        log("Waiting for job submitted");
        schedulerHelper.waitForEventJobSubmitted(jobId0);

        JobId jobId1 = schedulerHelper.submitJob(new File(JobSimpleTaskLicensePolicy.toURI()).getAbsolutePath(),
                                                 ImmutableMap.of("LICENSES", "software_A", "LICENSES_2", "software_B"));

        log("Waiting for job finished");
        schedulerHelper.waitForEventJobFinished(jobId1);

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();
        assertEquals(JobStatus.PENDING, scheduler.getJobState(jobId0).getStatus());
    }
}
