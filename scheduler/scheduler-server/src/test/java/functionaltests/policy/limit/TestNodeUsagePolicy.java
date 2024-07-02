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
import org.ow2.proactive.resourcemanager.common.RMStateNodeUrls;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.policy.limit.NodeUsageSchedulingPolicy;

import com.google.common.collect.ImmutableMap;

import functionaltests.utils.SchedulerFunctionalTestNodeUsagePolicy;


public class TestNodeUsagePolicy extends SchedulerFunctionalTestNodeUsagePolicy {

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
    public void testSubmitSingleJobWithMaxNodes() throws Throwable {
        JobId jobId = schedulerHelper.submitJob(new File(JobReplicateSleep.toURI()).getAbsolutePath(),
                                                ImmutableMap.of("NB_REP", "20", "SLEEP", "10"),
                                                ImmutableMap.of(NodeUsageSchedulingPolicy.MAX_NODES_USAGE, "5"));
        log("Waiting for jobs finished");
        schedulerHelper.waitForEventJobFinished(jobId);

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();
        JobState jobState = scheduler.getJobState(jobId);
        Assert.assertEquals(5, jobState.getJobInfo().getNumberOfNodesInParallel());
    }

    @Test
    public void testSubmitParentAndChildJobWithMaxNodes() throws Throwable {
        JobId jobId1 = schedulerHelper.submitJob(new File(JobReplicateSleep.toURI()).getAbsolutePath(),
                                                 ImmutableMap.of("NB_REP", "10", "SLEEP", "10"),
                                                 ImmutableMap.of(NodeUsageSchedulingPolicy.MAX_NODES_USAGE, "5"));
        JobId jobId2 = schedulerHelper.submitJob(new File(JobReplicateSleep.toURI()).getAbsolutePath(),
                                                 ImmutableMap.of("NB_REP", "10", "SLEEP", "10"),
                                                 ImmutableMap.of(PARENT_JOB_ID, jobId1.value()));

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();
        ResourceManager resourceManager = schedulerHelper.getResourceManager();

        log("Waiting for jobs finished");
        JobState jobState1;
        JobState jobState2;
        do {
            RMStateNodeUrls nodeStates = resourceManager.getState().getRmNodeUrls();
            int busyNodes = nodeStates.getAliveNodesUrls().size() - nodeStates.getFreeNodesUrls().size();
            Assert.assertThat(busyNodes,
                              ComparatorMatcherBuilder.<Integer> usingNaturalOrdering().lessThanOrEqualTo(5));
            jobState1 = scheduler.getJobState(jobId1);
            jobState2 = scheduler.getJobState(jobId2);
        } while (jobState1.getJobInfo().getStatus().isJobAlive() && jobState2.getJobInfo().getStatus().isJobAlive());

        // there can a slight discrepancy between the job state and the event
        schedulerHelper.waitForEventJobFinished(jobId1);
        schedulerHelper.waitForEventJobFinished(jobId2);

        Assert.assertThat(scheduler.getJobState(jobId1).getJobInfo().getNumberOfNodesInParallel(),
                          ComparatorMatcherBuilder.<Integer> usingNaturalOrdering().lessThanOrEqualTo(5));
        Assert.assertThat(scheduler.getJobState(jobId2).getJobInfo().getNumberOfNodesInParallel(),
                          ComparatorMatcherBuilder.<Integer> usingNaturalOrdering().lessThanOrEqualTo(5));
    }

    @Test
    public void testSubmitParentAndChildJobWithDifferentMaxNodes() throws Throwable {
        JobId jobId1 = schedulerHelper.submitJob(new File(JobReplicateSleep.toURI()).getAbsolutePath(),
                                                 ImmutableMap.of("NB_REP", "10", "SLEEP", "10"),
                                                 ImmutableMap.of(NodeUsageSchedulingPolicy.MAX_NODES_USAGE, "3"));
        JobId jobId2 = schedulerHelper.submitJob(new File(JobReplicateSleep.toURI()).getAbsolutePath(),
                                                 ImmutableMap.of("NB_REP", "10", "SLEEP", "10"),
                                                 ImmutableMap.of(NodeUsageSchedulingPolicy.MAX_NODES_USAGE,
                                                                 "4",
                                                                 PARENT_JOB_ID,
                                                                 jobId1.value()));

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();
        ResourceManager resourceManager = schedulerHelper.getResourceManager();

        log("Waiting for jobs finished");
        JobState jobState1;
        JobState jobState2;
        do {
            RMStateNodeUrls nodeStates = resourceManager.getState().getRmNodeUrls();
            int busyNodes = nodeStates.getAliveNodesUrls().size() - nodeStates.getFreeNodesUrls().size();
            Assert.assertThat(busyNodes,
                              ComparatorMatcherBuilder.<Integer> usingNaturalOrdering().lessThanOrEqualTo(7));
            jobState1 = scheduler.getJobState(jobId1);
            jobState2 = scheduler.getJobState(jobId2);
        } while (jobState1.getJobInfo().getStatus().isJobAlive() && jobState2.getJobInfo().getStatus().isJobAlive());

        // there can a slight discrepancy between the job state and the event
        schedulerHelper.waitForEventJobFinished(jobId1);
        schedulerHelper.waitForEventJobFinished(jobId2);

        Assert.assertThat(scheduler.getJobState(jobId1).getJobInfo().getNumberOfNodesInParallel(),
                          ComparatorMatcherBuilder.<Integer> usingNaturalOrdering().lessThanOrEqualTo(3));
        Assert.assertThat(scheduler.getJobState(jobId2).getJobInfo().getNumberOfNodesInParallel(),
                          ComparatorMatcherBuilder.<Integer> usingNaturalOrdering().lessThanOrEqualTo(4));
    }
}
