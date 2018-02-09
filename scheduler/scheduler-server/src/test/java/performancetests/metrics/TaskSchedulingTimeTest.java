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

import functionaltests.utils.SchedulerTHelper;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.ScriptTask;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import performancetests.recovery.PeformanceTestBase;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class TaskSchedulingTimeTest extends PeformanceTestBase {

    public static final URL SCHEDULER_CONFIGURATION_START = SchedulerEfficiencyMetricsTest.class.getResource("/performancetests/config/scheduler-start-memory.ini");

    public static final URL RM_CONFIGURATION_START = SchedulerEfficiencyMetricsTest.class.getResource("/performancetests/config/rm-start-memory.ini");

    private static final Logger LOGGER = Logger.getLogger(SchedulerEfficiencyMetricsTest.class);

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { 10, 2000 } });
    }

    private final int taskNumber;

    private final long timeLimit;

    private List<JobId> jobIds = new ArrayList<>();

    public TaskSchedulingTimeTest(int taskNumber, long timeLimit) {
        this.taskNumber = taskNumber;
        this.timeLimit = timeLimit;
    }

    @Test(timeout = 3600000)
    public void test() throws Exception {
        ProActiveConfiguration.load();
        RMFactory.setOsJavaProperty();
        schedulerHelper = new SchedulerTHelper(false,
                SCHEDULER_CONFIGURATION_START.getPath(),
                RM_CONFIGURATION_START.getPath(),
                null);

        schedulerHelper.createNodeSourceWithInfiniteTimeout("local", taskNumber);

        final TaskFlowJob job = SchedulerEfficiencyMetricsTest.createJob(1, 10);

        long totalTime = 0;
        for(int i = 0; i < taskNumber; ++i){
            JobId jobId = schedulerHelper.submitJob(job);
            jobIds.add(jobId );
            schedulerHelper.waitForEventJobFinished(jobId);
            final JobState jobState = schedulerHelper.getSchedulerInterface().getJobState(jobId);
            final long submittedTime = jobState.getSubmittedTime();
            final long taskStartTime = jobState.getTasks().get(0).getStartTime();
            final long timeToScheduleTask = taskStartTime - submittedTime;
            totalTime += timeToScheduleTask;
        }
        long averageTime = totalTime / taskNumber;
        LOGGER.info(makeCSVString("AverageTaskSchedulingTime",
                taskNumber,
                timeLimit,
                averageTime,
                ((averageTime < timeLimit) ? SUCCESS : FAILURE)));
    }

    @After
    public void after() throws Exception {
        if (schedulerHelper != null) {
            for (JobId jobId : jobIds) {
                if (!schedulerHelper.getSchedulerInterface().getJobState(jobId).isFinished()) {
                    schedulerHelper.getSchedulerInterface().killJob(jobId);
                }
                schedulerHelper.getSchedulerInterface().removeJob(jobId);
            }
            schedulerHelper.log("Kill Scheduler after test.");
            schedulerHelper.killScheduler();
        }
    }
}
