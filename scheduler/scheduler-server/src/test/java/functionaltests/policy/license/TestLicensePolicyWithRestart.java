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

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.python.google.common.collect.ImmutableMap;

import functionaltests.utils.SchedulerFunctionalTestLicensePolicy;
import functionaltests.utils.SchedulerTHelper;


public class TestLicensePolicyWithRestart extends SchedulerFunctionalTestLicensePolicy {

    private static URL JobSimpleJobLicensePolicy = TestLicensePolicyWithRestart.class.getResource("/functionaltests/descriptors/Job_simple_job_license_policy.xml");

    @Test
    public void testRestoreTokensAfterSchedulerRestart() throws Throwable {

        log("Starting testRestoreTokensAfterSchedulerRestart");

        JobId jobId0 = schedulerHelper.submitJob(new File(JobSimpleJobLicensePolicy.toURI()).getAbsolutePath(),
                                                 ImmutableMap.of("LICENSES", "software_A"));
        JobId jobId1 = schedulerHelper.submitJob(new File(JobSimpleJobLicensePolicy.toURI()).getAbsolutePath(),
                                                 ImmutableMap.of("LICENSES", "software_B"));

        JobId jobId2 = schedulerHelper.submitJob(new File(JobSimpleJobLicensePolicy.toURI()).getAbsolutePath(),
                                                 ImmutableMap.of("LICENSES", "software_A"));

        JobId jobId3 = schedulerHelper.submitJob(new File(JobSimpleJobLicensePolicy.toURI()).getAbsolutePath(),
                                                 ImmutableMap.of("LICENSES", "software_B"));

        log("Waiting for jobs running");
        schedulerHelper.waitForEventJobRunning(jobId0);
        schedulerHelper.waitForEventJobRunning(jobId1);

        log("Waiting for jobs submitted");
        schedulerHelper.waitForEventJobSubmitted(jobId2);
        schedulerHelper.waitForEventJobSubmitted(jobId3);

        log("Killing and restarting the scheduler");
        schedulerHelper.killSchedulerAndNodesAndRestart(new File(SchedulerTHelper.class.getResource("/functionaltests/config/functionalTSchedulerProperties-licensepolicy.ini")
                                                                                       .toURI()).getAbsolutePath());

        log("Waiting for jobs finished");
        schedulerHelper.waitForEventJobFinished(jobId0);
        schedulerHelper.waitForEventJobFinished(jobId1);
        schedulerHelper.waitForEventJobFinished(jobId2);
        schedulerHelper.waitForEventJobFinished(jobId3);

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();
        JobState jobState0 = scheduler.getJobState(jobId0);
        JobState jobState1 = scheduler.getJobState(jobId1);
        JobState jobState2 = scheduler.getJobState(jobId2);
        JobState jobState3 = scheduler.getJobState(jobId3);

        boolean jobs0and1ExecutedInParallel = !((jobState0.getFinishedTime() < jobState1.getStartTime()) ||
                                                (jobState1.getFinishedTime() < jobState0.getStartTime()));

        boolean jobs2and3ExecutedInParallel = !((jobState2.getFinishedTime() < jobState3.getStartTime()) ||
                                                (jobState3.getFinishedTime() < jobState2.getStartTime()));

        boolean jobs0and2ExecutedOneByOne = (jobState0.getFinishedTime() < jobState2.getStartTime()) ||
                                            (jobState2.getFinishedTime() < jobState0.getStartTime());

        boolean jobs1and3ExecutedOneByOne = (jobState1.getFinishedTime() < jobState3.getStartTime()) ||
                                            (jobState3.getFinishedTime() < jobState1.getStartTime());

        Assert.assertTrue(jobs0and2ExecutedOneByOne);
        Assert.assertTrue(jobs1and3ExecutedOneByOne);
        Assert.assertTrue(jobs0and1ExecutedInParallel);
        Assert.assertTrue(jobs2and3ExecutedInParallel);
    }

}
