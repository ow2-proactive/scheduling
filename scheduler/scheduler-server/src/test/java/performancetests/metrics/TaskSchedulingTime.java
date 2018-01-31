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
import static org.hamcrest.Matchers.theInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.listener.JobTaskStatusListener;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskStatus;

import functionaltests.utils.SchedulerTHelper;
import performancetests.recovery.BaseRecoveryTest;


@RunWith(Parameterized.class)
public class TaskSchedulingTime extends BaseRecoveryTest {

    private static final Logger LOGGER = Logger.getLogger(TaskSchedulingTime.class);

    private static final int TASK_DURATION = 10; // in seconds

    /**
     * @return an array of parameters which is used by JUnit to create objects of TaskCreationTime,
     * where first value represents number of task in the job, and the second represents limit for TaskCreationTime (TCT).
     * The lower TCT the better.
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{{8, 3000}});
    }

    private final int taskNumber;

    private final long timeLimit;

    public TaskSchedulingTime(int taskNumber, long timeLimit) {
        this.taskNumber = taskNumber;
        this.timeLimit = timeLimit;
    }

    @Test(timeout = 3600000)
    public void taskSchedulingRate() throws Exception {
        ProActiveConfiguration.load();
        RMFactory.setOsJavaProperty();
        schedulerHelper = new SchedulerTHelper(false,
                SchedulerEfficiencyTime.SCHEDULER_CONFIGURATION_START.getPath(),
                SchedulerEfficiencyTime.RM_CONFIGURATION_START.getPath(),
                null);

        schedulerHelper.createNodeSourceWithInfiniteTimeout("local", taskNumber);

        final JobTaskStatusListener listener = JobTaskStatusListener.attachListener(schedulerHelper.getSchedulerInterface());

        final long start = System.currentTimeMillis();

        final JobId jobId = schedulerHelper.submitJob(SchedulerEfficiencyTime.createJob(taskNumber, 10000000));

        waitUntilNumberOfTasksEvents(listener, jobId, TaskStatus.RUNNING, taskNumber);

        Set<Long> ids = new HashSet<>();
        for (JobTaskStatusListener.TimestampedData<TaskInfo> tData : listener.getTaskEvents()) {
            if (tData.getData().getJobId().equals(jobId) && tData.getData().getStatus().equals(TaskStatus.RUNNING)) {
                ids.add(tData.getData().getTaskId().longValue());
            }
        }

        assertEquals(taskNumber, ids.size());

        for (JobTaskStatusListener.TimestampedData<TaskInfo> tData : listener.getTaskEvents()) {
            if (tData.getData().getJobId().equals(jobId) && (tData.getData().getStatus().equals(TaskStatus.ABORTED) ||
                    tData.getData().getStatus().equals(TaskStatus.FAILED) ||
                    tData.getData().getStatus().equals(TaskStatus.FAULTY) ||
                    tData.getData().getStatus().equals(TaskStatus.FINISHED) ||
                    tData.getData().getStatus().equals(TaskStatus.PAUSED) ||
                    tData.getData().getStatus().equals(TaskStatus.PENDING))) {
                throw new RuntimeException("There should not be task with any of these statuses.");
            }
        }

        List<Long> longList = new ArrayList<>();

        for (JobTaskStatusListener.TimestampedData<TaskInfo> tData : listener.getTaskEvents()) {
            if (tData.getData().getJobId().equals(jobId) && tData.getData().getStatus().equals(TaskStatus.RUNNING)) {
                longList.add(tData.getTimestamp());
            }
        }

        Collections.sort(longList);

        long first = longList.get(0);
        long last = longList.get(longList.size() - 1);

        long anActualTime = last - first;

        assertTrue(last > first);

        LOGGER.info(makeCSVString(TaskSchedulingTime.class.getSimpleName(),
                taskNumber,
                timeLimit,
                anActualTime,
                ((anActualTime < timeLimit) ? SUCCESS : FAILURE)));

        assertThat(String.format("Task creation rate for job with %s tasks", taskNumber),
                anActualTime,
                lessThan(timeLimit));

    }

    private long waitUntilNumberOfTasksEvents(JobTaskStatusListener listener, JobId jobId, TaskStatus taskStatus,
                                              int numberOfEvents) throws InterruptedException {
        long result = 0;

        while (result < numberOfEvents) {
            result = 0;
            for (JobTaskStatusListener.TimestampedData<TaskInfo> tData : listener.getTaskEvents()) {
                if (tData.getData().getJobId().equals(jobId) && tData.getData().getStatus().equals(taskStatus)) {
                    ++result;
                }
            }

            if (result > numberOfEvents) {
                throw new RuntimeException(String.format("There are more %s tasks %d that we expected",
                        taskStatus.toString(),
                        result));
            }
            Thread.sleep(1000);

        }
        return result;
    }

}
