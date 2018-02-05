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
package performancetests.metrics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;

import functionaltests.utils.SchedulerTHelper;
import performancetests.recovery.PeformanceTestBase;


/**
 * Performance test measures submitting job with a single task,
 * and measures time from submitting this job to getting its result.
 * This test DOES require to have as many cores as there are tasks.
 */
@RunWith(Parameterized.class)
public class GetResultMetricTest extends PeformanceTestBase {

    private static final Logger LOGGER = Logger.getLogger(GetResultMetricTest.class);

    SchedulerTHelper schedulerHelper;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { 1, 10000 } });
    }

    private final int taskNumber;

    private final long timeLimit;

    private JobId jobId;

    public GetResultMetricTest(int taskNumber, int timeLimit) {
        this.taskNumber = taskNumber;
        this.timeLimit = timeLimit;
    }

    @Test(timeout = 3600000)
    public void test() throws Exception {

        final int taskDuration = 1;
        final int nodeNumber = 1;

        ProActiveConfiguration.load();
        RMFactory.setOsJavaProperty();
        schedulerHelper = new SchedulerTHelper(false,
                                               SchedulerEfficiencyMetricsTest.SCHEDULER_CONFIGURATION_START.getPath(),
                                               SchedulerEfficiencyMetricsTest.RM_CONFIGURATION_START.getPath(),
                                               null);

        schedulerHelper.createNodeSourceWithInfiniteTimeout("local", nodeNumber);

        final TaskFlowJob job = SchedulerEfficiencyMetricsTest.createJob(taskNumber, taskDuration);

        final long start = System.currentTimeMillis();

        jobId = schedulerHelper.submitJob(job);

        schedulerHelper.waitForEventJobFinished(jobId);

        final JobResult jobResult = schedulerHelper.getSchedulerInterface().getJobResult(jobId);

        long timeToGetResult = System.currentTimeMillis() - start - (taskDuration * 1000);

        LOGGER.info(makeCSVString(GetResultMetricTest.class.getSimpleName(),
                                  taskNumber,
                                  timeLimit,
                                  timeToGetResult,
                                  ((timeToGetResult < timeLimit) ? SUCCESS : FAILURE)));

        assertThat(String.format("Task creation rate for job with %s tasks", taskNumber),
                   timeToGetResult,
                   lessThan(timeLimit));
    }

    @After
    public void after() throws Exception {
        if (schedulerHelper != null) {
            if (!schedulerHelper.getSchedulerInterface().getJobState(jobId).isFinished()) {
                schedulerHelper.getSchedulerInterface().killJob(jobId);
            }
            schedulerHelper.getSchedulerInterface().removeJob(jobId);
            schedulerHelper.log("Kill Scheduler after test.");
            schedulerHelper.killScheduler();
        }
    }
}
