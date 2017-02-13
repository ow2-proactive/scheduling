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
package functionaltests.scripts;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.StaxJobFactory;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.task.exceptions.ForkedJvmProcessException;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import functionaltests.utils.SchedulerStartForFunctionalTest;


public class TestScriptTask extends SchedulerFunctionalTestNoRestart {

    private static URL jobDescriptor = TestScriptTask.class.getResource("/functionaltests/descriptors/Job_script_task.xml");

    private static URL job_null_returning_script_task = TestScriptTask.class.getResource("/functionaltests/descriptors/Job_null_returning_script_task.xml");

    @Test
    public void testScriptTask() throws Throwable {
        forkedTasks();
        test_getTaskResult_nullReturningScriptTask_shouldSucceed();
    }

    private void forkedTasks() throws Throwable {
        TaskFlowJob job = (TaskFlowJob) StaxJobFactory.getFactory()
                                                      .createJob(new File(jobDescriptor.toURI()).getAbsolutePath());

        JobId id = schedulerHelper.submitJob(job);
        schedulerHelper.waitForEventJobFinished(id);
        JobResult jobResult = schedulerHelper.getJobResult(id);

        // Hello Work script task
        TaskResult simpleTaskResult = jobResult.getResult("simple");
        assertEquals(true, simpleTaskResult.value());
        assertTrue(simpleTaskResult.getOutput().getAllLogs(false).contains("hello"));

        // return binding should be used as task result
        TaskResult returnTaskResult = jobResult.getResult("return");
        assertEquals("42", returnTaskResult.value().toString());

        // results binding should be avaible in dependent tasks
        TaskResult resultFromDependentTaskTaskResult = jobResult.getResult("results_from_dependent_task");
        assertEquals("42", resultFromDependentTaskTaskResult.value().toString());

        // pas properties are exposed in the script task
        TaskResult propertiesTaskResult = jobResult.getResult("properties");
        String logs = propertiesTaskResult.getOutput().getAllLogs(false);

        assertThat(logs, containsString("PA_JOB_ID=" + jobResult.getJobId().value()));
        assertThat(logs, containsString("PA_JOB_NAME=" + jobResult.getName()));
        assertThat(logs, containsString("PA_TASK_ID=" + propertiesTaskResult.getTaskId().value()));
        assertThat(logs, containsString("PA_TASK_NAME=" + propertiesTaskResult.getTaskId().getReadableName()));
        assertThat(logs, containsString("PA_TASK_ITERATION=0"));
        assertThat(logs, containsString("PA_TASK_REPLICATION=0"));

        // the script can be a file
        TaskResult fileTaskResult = jobResult.getResult("file");
        assertTrue(fileTaskResult.getOutput().getAllLogs(false).contains("Beginning of clean script"));

        TaskResult fileAndArgsTaskResult = jobResult.getResult("file_and_args");
        assertTrue(fileAndArgsTaskResult.getOutput().getAllLogs(false).contains("My_Magic_Arg"));

        // dataspaces binding should be available
        TaskResult dataspacesTaskResult = jobResult.getResult("dataspaces");

        String dataspacesLogs = dataspacesTaskResult.getOutput().getAllLogs(false);
        System.out.println(dataspacesLogs);
        String schedulerHome = System.getProperty("pa.scheduler.home");
        assertTrue(dataspacesLogs.contains("global=" + schedulerHome));
        assertTrue(dataspacesLogs.contains("user=" + schedulerHome));
        assertTrue(dataspacesLogs.contains("input=" + schedulerHome));
        assertTrue(dataspacesLogs.contains("output=" + schedulerHome));

        TaskResult multiNodeTaskResult = jobResult.getResult("multi-node");
        String mnLogs = multiNodeTaskResult.getOutput().getAllLogs(false);
        assertTrue("Invalid binding for nodesurl",
                   mnLogs.contains("nodesurl=" + (SchedulerStartForFunctionalTest.RM_NODE_NUMBER - 1)));

        // script task should be forked by default, ie it will not kill the scheduler on system.exit
        JobState jobState = schedulerHelper.getSchedulerInterface().getJobState(id);
        TaskResult killJVMTaskResult = jobResult.getResult("killJVM");
        assertTrue(killJVMTaskResult.getException() instanceof ForkedJvmProcessException);

        TaskState killJVMTaskState = jobState.getHMTasks().get(killJVMTaskResult.getTaskId());
        assertEquals(TaskStatus.FAULTY, killJVMTaskState.getStatus());
    }

    /**
     * SCHEDULING-2199 NPE is thrown when retrieving the result of a ScriptTask,
     *  if the result is 'null'.
     */
    private void test_getTaskResult_nullReturningScriptTask_shouldSucceed() throws Throwable {
        TaskFlowJob job = (TaskFlowJob) StaxJobFactory.getFactory()
                                                      .createJob(new File(job_null_returning_script_task.toURI()).getAbsolutePath());
        JobId id = schedulerHelper.submitJob(job);
        schedulerHelper.waitForEventJobFinished(id);
        TaskResult taskResult = schedulerHelper.getTaskResult(id, "task");
        assertNull(taskResult.value());
    }

}
