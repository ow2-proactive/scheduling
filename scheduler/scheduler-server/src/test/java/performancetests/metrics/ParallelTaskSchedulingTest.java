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

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.examples.EmptyTask;

import functionaltests.utils.SchedulerTHelper;
import performancetests.recovery.PerformanceTestBase;


/**
 * Performance test measure paraller task scheduling.
 * Performance test which submit 1 job,
 * with 1000 tasks parallel tasks inside.
 * task is added via API
 * RM have 50 nodes to run the tasks.
 * actual machine has only 8 cores.
 * each task will be empty.
 * scheduler started in non-forked mode.
 * test measures time from submitJob till job is finished. So final time:
 * job.getFinishedTime() - job.getStartedTime
 */
@RunWith(Parameterized.class)
public class ParallelTaskSchedulingTest extends PerformanceTestBase {

    /**
     * @return an array of parameters which is used by JUnit to create objects of ParallelTaskSchedulingTest,
     * where first value represents number of task in the job, and the second represents limit of time.
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { 1000, 50, 200000 } });
    }

    // number of tasks (all task are empty) inside single job
    private final int numberOfTasks;

    private final int numberOfNodes;

    // time limit in milliseconds
    private final int timeLimit;

    public ParallelTaskSchedulingTest(int numberOfTasks, int numberOfNodes, int timeLimit) {
        this.numberOfTasks = numberOfTasks;
        this.numberOfNodes = numberOfNodes;
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

        schedulerHelper.createNodeSourceWithInfiniteTimeout("local", numberOfNodes);

        final TaskFlowJob job = createJob(numberOfTasks);
        jobId = schedulerHelper.submitJob(job);
        schedulerHelper.waitForEventJobFinished(jobId);
        final JobState jobState = schedulerHelper.getSchedulerInterface().getJobState(jobId);

        final long timeToMeasure = jobState.getFinishedTime() - jobState.getStartTime();

        LOGGER.info(makeCSVString(ParallelTaskSchedulingTest.class.getSimpleName(),
                                  numberOfTasks,
                                  timeLimit,
                                  timeToMeasure,
                                  ((timeToMeasure < timeLimit) ? SUCCESS : FAILURE)));
    }

    private TaskFlowJob createJob(int numberOfTasks) throws UserException {
        TaskFlowJob job = new TaskFlowJob();
        for (int i = 0; i < numberOfTasks; i++) {
            JavaTask task = new JavaTask();
            task.setName("JavaTask_" + i);
            task.setExecutableClassName(EmptyTask.class.getName());
            job.addTask(task);
        }
        return job;
    }
}
