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
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.ScriptTask;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import functionaltests.utils.SchedulerTHelper;
import performancetests.recovery.BaseRecoveryTest;
import performancetests.recovery.JobRecoveryTest;


@RunWith(Parameterized.class)
public class SchedulerEfficiencyTime extends BaseRecoveryTest {

    public static final URL SCHEDULER_CONFIGURATION_START = SchedulerEfficiencyTime.class.getResource("/performancetests/config/scheduler-start-memory.ini");

    public static final URL RM_CONFIGURATION_START = SchedulerEfficiencyTime.class.getResource("/performancetests/config/rm-start-memory.ini");

    private static final Logger LOGGER = Logger.getLogger(SchedulerEfficiencyTime.class);

    private static final String OPTIMAL_JOB_DURATION = "OPTIMAL_JOB_DURATION";

    private static final int TASK_DURATION = 10; // in seconds

    /**
     * @return an array of parameters which is used by JUnit to create objects of SchedulerEfficiencyTime,
     * where first value represents number of task in the job, and the second represents limit for SchedulerEfficiencyTime (SER).
     * The biggest SER the better.
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { 8, 10000 } });
    }

    private final int taskNumber;

    private final long rateLimit;

    public SchedulerEfficiencyTime(int taskNumber, long rateLimit) {
        this.taskNumber = taskNumber;
        this.rateLimit = rateLimit;
    }

    @Test(timeout = 3600000)
    public void taskCreationRate() throws Exception {
        ProActiveConfiguration.load();
        RMFactory.setOsJavaProperty();
        schedulerHelper = new SchedulerTHelper(false,
                                               SCHEDULER_CONFIGURATION_START.getPath(),
                                               RM_CONFIGURATION_START.getPath(),
                                               null);

        schedulerHelper.createNodeSourceWithInfiniteTimeout("local", taskNumber);

        final JobId jobId = schedulerHelper.submitJob(createJob(taskNumber, TASK_DURATION));

        schedulerHelper.waitForEventJobFinished(jobId);

        final JobState jobState = schedulerHelper.getSchedulerInterface().getJobState(jobId);

        final Long anActualTime = computeSchedulerEfficiencyTime(jobState);

        LOGGER.info(makeCSVString(SchedulerEfficiencyTime.class.getSimpleName(),
                                  taskNumber,
                                  rateLimit,
                                  anActualTime,
                                  ((anActualTime < rateLimit) ? SUCCESS : FAILURE)));

        assertThat(String.format("Task creation rate for job with %s tasks", taskNumber),
                   anActualTime,
                   lessThan(rateLimit));

    }

    private long computeSchedulerEfficiencyTime(JobState jobState) {
        final long optimalJobDuration = Long.valueOf(jobState.getVariables().get(OPTIMAL_JOB_DURATION).getValue());
        final long actualJobDuration = jobState.getFinishedTime() - jobState.getStartTime();
        return actualJobDuration - optimalJobDuration;
    }

    public static TaskFlowJob createJob(int taskNumber, int taskDuration) throws Exception {
        final TaskFlowJob job = new TaskFlowJob();
        job.setName(String.format("EP_%d_NO_MERGE_%dSEC", taskNumber, taskDuration));
        job.setOnTaskError(OnTaskError.CANCEL_JOB);
        job.getVariables().put(OPTIMAL_JOB_DURATION,
                               new JobVariable(OPTIMAL_JOB_DURATION, String.valueOf(taskDuration * 1000)));
        for (int i = 0; i < taskNumber; i++) {
            ScriptTask task = new ScriptTask();
            task.setName("process_" + i);
            task.setScript(new TaskScript(new SimpleScript(String.format("Thread.sleep(%s * 1000)", taskDuration),
                                                           "groovy")));
            job.addTask(task);
        }
        return job;
    }

}
