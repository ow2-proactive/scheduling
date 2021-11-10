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
package functionaltests.job.error;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.ScriptTask;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.examples.EmptyTask;
import org.ow2.proactive.scheduler.examples.NativeTestWithRandomDefault;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import functionaltests.utils.TestScheduler;


/**
 * This class tests a basic actions of a job submission to ProActive scheduler :
 * Connection to scheduler, with authentication
 * Register a monitor to Scheduler in order to receive events concerning
 * job submission.
 * 
 * This test will try many kind of possible errors.
 * The goal for this test is to terminate. If the Test timeout is reached, it is considered as failed.
 * Possible problems may come from many error count. If this job finish in a
 * reasonable time, it is considered that it passed the test.
 * Every events coming from the scheduler are also checked.
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class TestErrorAndFailure extends SchedulerFunctionalTestNoRestart {

    private static final String inerror_groovy_script = "println \"some logs\"\nthrow new Exception(\"error\")";

    private static final String ok_groovy_script = "println \"OK\"";

    @Test
    public void testErrorAndFailure() throws Throwable {

        String javaCmd = System.getProperty("java.home") + "/bin/java";
        log("Test 1 : Creating job...");
        //creating job
        TaskFlowJob submittedJob = new TaskFlowJob();
        submittedJob.setName(this.getClass().getSimpleName() + "_12_tasks");
        submittedJob.setDescription("12 tasks job testing the behavior of error code and normal task ending.");
        submittedJob.setMaxNumberOfExecution(10);
        NativeTask finalTask = new NativeTask();
        finalTask.setName("TestMerge");
        finalTask.setCommandLine(new String[] { javaCmd, "-cp", TestScheduler.testClasspath(),
                                                NativeTestWithRandomDefault.class.getName(), "final" });
        for (int i = 1; i < 6; i++) {
            NativeTask task = new NativeTask();
            task.setName("Test" + i);
            task.setCommandLine(new String[] { javaCmd, "-cp", TestScheduler.testClasspath(),
                                               NativeTestWithRandomDefault.class.getName(), "0" });
            finalTask.addDependence(task);
            submittedJob.addTask(task);
        }

        submittedJob.addTask(finalTask);

        //test submission and event reception
        JobId id = schedulerHelper.submitJob(submittedJob);
        log("Job submitted, id " + id.toString());
        log("Waiting for jobSubmitted Event");
        Job receivedJob = schedulerHelper.waitForEventJobSubmitted(id);
        assertEquals(receivedJob.getId(), id);
        log("Waiting for job running");
        JobInfo jInfo = schedulerHelper.waitForEventJobRunning(id);

        assertEquals(jInfo.getJobId(), id);

        //task running event may occurs several time for this test
        //TODO how to check that ?
        for (Task t : submittedJob.getTasks()) {
            log("Waiting for task running : " + t.getName());
            schedulerHelper.waitForEventTaskRunning(id, t.getName());
        }

        for (Task t : submittedJob.getTasks()) {
            log("Waiting for task finished : " + t.getName());
            schedulerHelper.waitForEventTaskFinished(id, t.getName());

        }

        log("Waiting for job finished");
        jInfo = schedulerHelper.waitForEventJobFinished(id);

        assertEquals(JobStatus.FINISHED, jInfo.getStatus());

        //check job results
        JobResult res = schedulerHelper.getJobResult(id);
        //Check the results
        Map<String, TaskResult> results = res.getAllResults();
        //check that number of results correspond to number of tasks
        assertEquals(submittedJob.getTasks().size(), results.size());

        //remove jobs and check its event
        schedulerHelper.removeJob(id);
        log("Waiting for job removed");
        jInfo = schedulerHelper.waitForEventJobRemoved(id);
        assertEquals(JobStatus.FINISHED, jInfo.getStatus());
        assertEquals(jInfo.getJobId(), id);

    }

    @Test
    public void testPauseJobErrorPolicyRestartAllInErrorTasks() throws Throwable {
        testRestartInError(false, false);
    }

    @Test
    public void testPauseJobErrorPolicyRestartInErrorTask() throws Throwable {
        testRestartInError(true, false);
    }

    @Test
    public void testPauseTaskErrorPolicyRestartAllInErrorTasks() throws Throwable {
        testRestartInError(false, true);
    }

    @Test
    public void testPauseTaskErrorPolicyRestartInErrorTask() throws Throwable {
        testRestartInError(true, true);
    }

    private void testRestartInError(boolean restartSingleTask, boolean pauseTaskPolicy) throws Throwable {
        TaskFlowJob submittedJob = new TaskFlowJob();
        submittedJob.setName(this.getClass().getSimpleName() +
                             (pauseTaskPolicy ? "_pauseTaskPolicy" : "_pauseJobPolicy"));
        submittedJob.setDescription("A job testing in-error behavior.");
        submittedJob.setMaxNumberOfExecution(1);
        submittedJob.setOnTaskError(pauseTaskPolicy ? OnTaskError.PAUSE_TASK : OnTaskError.PAUSE_JOB);

        File groovyScriptFile = File.createTempFile("inerrorTask", ".groovy");
        FileUtils.writeStringToFile(groovyScriptFile, inerror_groovy_script, StandardCharsets.UTF_8.toString());
        ScriptTask taskInError = new ScriptTask();
        final String errorTaskName = "errorTask";
        taskInError.setName(errorTaskName);
        taskInError.setScript(new TaskScript(new SimpleScript(groovyScriptFile.toURI().toURL(),
                                                              "groovy",
                                                              new Serializable[0])));
        JavaTask taskOk = new JavaTask();
        taskOk.setName("okTask");
        taskOk.setExecutableClassName(EmptyTask.class.getName());
        taskOk.addDependence(taskInError);
        submittedJob.addTask(taskInError);
        submittedJob.addTask(taskOk);

        JobId id = schedulerHelper.submitJob(submittedJob);
        log("Job submitted, id " + id.toString());
        log("Waiting for jobSubmitted Event");
        Job receivedJob = schedulerHelper.waitForEventJobSubmitted(id);
        assertEquals(receivedJob.getId(), id);
        log("Waiting for job running");
        JobInfo jInfo = schedulerHelper.waitForEventJobRunning(id);
        assertEquals(jInfo.getJobId(), id);
        schedulerHelper.waitForEventTaskInError(id, errorTaskName);
        JobState jobState = schedulerHelper.getSchedulerInterface().getJobState(id);
        Assert.assertEquals(pauseTaskPolicy ? JobStatus.IN_ERROR : JobStatus.PAUSED, jobState.getStatus());
        Assert.assertEquals(1, jobState.getNumberOfInErrorTasks());
        List<TaskResult> taskResults = schedulerHelper.getSchedulerInterface()
                                                      .getTaskResultAllIncarnations(jInfo.getJobId(), "errorTask");
        Assert.assertNotNull(taskResults);
        Assert.assertEquals(1, taskResults.size());
        String taskLogs = taskResults.get(0).getOutput().getAllLogs();
        Assert.assertNotNull(taskLogs);
        Assert.assertTrue(taskLogs.contains("some logs"));
        FileUtils.writeStringToFile(groovyScriptFile, ok_groovy_script, StandardCharsets.UTF_8.toString());
        if (restartSingleTask) {
            schedulerHelper.getSchedulerInterface().restartInErrorTask(id.toString(), errorTaskName);
        } else {
            schedulerHelper.restartAllInErrorTasks(id.toString());
        }
        if (!pauseTaskPolicy) {
            schedulerHelper.getSchedulerInterface().resumeJob(id);
        }
        schedulerHelper.waitForEventTaskFinished(id, errorTaskName);
        schedulerHelper.waitForEventTaskRunning(id, "okTask");
        JobInfo jobInfo = schedulerHelper.getSchedulerInterface().getJobInfo(id.toString());
        Assert.assertTrue(jobInfo.getStatus() == JobStatus.RUNNING || jobInfo.getStatus() == JobStatus.STALLED);
        schedulerHelper.waitForEventJobFinished(id);
        jobInfo = schedulerHelper.getSchedulerInterface().getJobInfo(id.toString());
        Assert.assertEquals(JobStatus.FINISHED, jobInfo.getStatus());

        jobState = schedulerHelper.getSchedulerInterface().getJobState(id);
        Assert.assertEquals(JobStatus.FINISHED, jobState.getStatus());
        Assert.assertEquals(0, jobState.getNumberOfInErrorTasks());
        Assert.assertEquals(0, jobState.getNumberOfRunningTasks());
        Assert.assertEquals(2, jobState.getNumberOfFinishedTasks());

        groovyScriptFile.delete();
    }

}
