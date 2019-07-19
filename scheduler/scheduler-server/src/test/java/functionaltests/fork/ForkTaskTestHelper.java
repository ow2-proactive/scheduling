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
package functionaltests.fork;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;

import org.junit.Assert;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;

import functionaltests.utils.SchedulerTHelper;


public class ForkTaskTestHelper {
    public static final String TASK_NAME = "Check_Fork_Task";

    private SchedulerTHelper schedulerHelper;

    public ForkTaskTestHelper(SchedulerTHelper schedulerHelper) {
        this.schedulerHelper = schedulerHelper;
    }

    /**
     * Test whether the job is executed in the expected forked mode
     *
     * @param jobResource the path of the job to check whether it's executed in forked mode.
     *                    The job is expected to contain a task named ForkTaskTestHelper.TASK_NAME which prints the working directory
     * @param expectForked true if expect the job to run in a forked JVM; false if expect the job to run in the node JVM.
     * @throws Exception
     */
    public void testTaskIsRunningInExpectedForkedMode(String jobResource, boolean expectForked) throws Exception {
        if (expectForked) {
            testTaskGetExpectedOutput(jobResource,
                                      TASK_NAME,
                                      System.getProperty("java.io.tmpdir") + File.separator,
                                      "Task output should display a path under the system tmp directory, as the task is supposed to run in a forked JVM.");
        } else {
            testTaskGetExpectedOutput(jobResource,
                                      TASK_NAME,
                                      PASchedulerProperties.SCHEDULER_HOME.getValueAsString(),
                                      "Task output should display the proactive home path, as the task is supposed to run in the node JVM.");
        }
    }

    /**
     * Submit the job to the scheduler, check the job contains a task with expected name, and the task output result contains an expected substring 
     * @param jobResource the job resource name to submit to the scheduler
     * @param expectedTaskName the expected task name which should be defined in the job
     * @param expectedTaskOutput the task result output is expected to contain this string
     * @param errorMessage the error message when the task output doesn't contain the expected substring
     * @throws Exception
     */
    public void testTaskGetExpectedOutput(String jobResource, String expectedTaskName, String expectedTaskOutput,
            String errorMessage) throws Exception {
        Scheduler scheduler = schedulerHelper.getSchedulerInterface();
        JobId jobid = schedulerHelper.testJobSubmission(getResourceAbsolutePath(jobResource));
        Map<String, TaskResult> taskResult = scheduler.getJobResult(jobid).getAllResults();
        Assert.assertTrue(String.format("The jobs result should contain the task [%s].", expectedTaskName),
                          taskResult.containsKey(expectedTaskName));
        String taskOutput = taskResult.get(expectedTaskName).getOutput().getStdoutLogs();
        Assert.assertTrue(errorMessage, taskOutput.contains(expectedTaskOutput));
    }

    public static String getResourceAbsolutePath(String resourceName) throws URISyntaxException {
        return new File(TestTaskForkParameter.class.getResource(resourceName).toURI()).getAbsolutePath();
    }
}
