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
package functionaltests.policy.limit;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.ow2.proactive.scheduler.common.SchedulerConstants.PARENT_JOB_ID;

import java.io.File;
import java.net.URL;

import org.hamcrest.comparator.ComparatorMatcherBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.policy.limit.NodeUsageSchedulingPolicy;

import com.google.common.collect.ImmutableMap;

import functionaltests.utils.SchedulerFunctionalTestNodeUsagePolicy;
import functionaltests.utils.SchedulerTHelper;


public class TestNodeUsagePolicyWithRestart extends SchedulerFunctionalTestNodeUsagePolicy {

    private static URL JobReplicateSleep = TestNodeUsagePolicy.class.getResource("/functionaltests/descriptors/Job_Replicate_N_Sleep_K.xml");

    @Before
    public void before() throws Throwable {
        schedulerHelper.cleanJobs();
    }

    @After
    public void after() throws Throwable {
        schedulerHelper.cleanJobs();
    }

    @Test
    public void testRestoreTokensAfterSchedulerRestart() throws Throwable {
        JobId jobId1 = schedulerHelper.submitJob(new File(JobReplicateSleep.toURI()).getAbsolutePath(),
                                                 ImmutableMap.of("NB_REP", "10", "SLEEP", "20"),
                                                 ImmutableMap.of(NodeUsageSchedulingPolicy.MAX_NODES_USAGE, "5"));
        JobId jobId2 = schedulerHelper.submitJob(new File(JobReplicateSleep.toURI()).getAbsolutePath(),
                                                 ImmutableMap.of("NB_REP", "10", "SLEEP", "20"),
                                                 ImmutableMap.of(PARENT_JOB_ID, jobId1.value()));

        log("Waiting for jobs running");
        schedulerHelper.waitForEventJobRunning(jobId1);
        schedulerHelper.waitForEventJobRunning(jobId2);

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();
        JobState jobState1;
        JobState jobState2;
        int nbRunningTasks = 0;
        do {
            jobState1 = scheduler.getJobState(jobId1);
            jobState2 = scheduler.getJobState(jobId2);
            nbRunningTasks = jobState1.getNumberOfRunningTasks() + jobState2.getNumberOfRunningTasks();
        } while (nbRunningTasks < 5);

        log("Killing and restarting the scheduler");
        schedulerHelper.killSchedulerAndNodesAndRestart(new File(SchedulerTHelper.class.getResource("/functionaltests/config/functionalTSchedulerProperties-nodeusagepolicy.ini")
                                                                                       .toURI()).getAbsolutePath());

        scheduler = schedulerHelper.getSchedulerInterface();

        schedulerHelper.waitForEventJobFinished(jobId1);
        schedulerHelper.waitForEventJobFinished(jobId2);

        Assert.assertThat(scheduler.getJobState(jobId1).getJobInfo().getNumberOfNodesInParallel(),
                          ComparatorMatcherBuilder.<Integer> usingNaturalOrdering().lessThanOrEqualTo(5));
        Assert.assertThat(scheduler.getJobState(jobId2).getJobInfo().getNumberOfNodesInParallel(),
                          ComparatorMatcherBuilder.<Integer> usingNaturalOrdering().lessThanOrEqualTo(5));

    }
}
