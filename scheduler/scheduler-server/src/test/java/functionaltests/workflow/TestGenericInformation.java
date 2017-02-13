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
package functionaltests.workflow;

import java.io.Serializable;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.task.flow.FlowBlock;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import functionaltests.utils.SchedulerTHelper;


/**
 * Checking that variables $PA_JOB_NAME, $PA_JOB_ID, $PA_TASK_NAME, $PA_TASK_ID, $PA_TASK_ITERATION
 * $PA_TASK_REPLICATION are replaced by it's actual value in generic information.
 * 
 */
public class TestGenericInformation extends SchedulerFunctionalTestNoRestart {

    private final String JOB_NAME = this.getClass().getSimpleName();

    public static class TestJavaTask extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            System.out.println("OK");
            return "OK";
        }
    }

    @Test
    public void testGenericInformation() throws Throwable {
        testRegularJob();
        testWithReplication();
    }

    private TaskFlowJob createJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(JOB_NAME);
        setGenericInfo(job);

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(TestJavaTask.class.getName());
        String TASK_NAME = "task name";
        javaTask.setName(TASK_NAME);

        setGenericInfo(javaTask);
        job.addTask(javaTask);

        return job;
    }

    private void setGenericInfo(TaskFlowJob job) {
        job.addGenericInformation("PA_JOB_NAME", "$PA_JOB_NAME");
        job.addGenericInformation("PA_JOB_ID", "$PA_JOB_ID");
    }

    private static void setGenericInfo(JavaTask javaTask) {
        javaTask.addGenericInformation("PA_JOB_NAME", "$PA_JOB_NAME");
        javaTask.addGenericInformation("PA_JOB_ID", "$PA_JOB_ID");
        javaTask.addGenericInformation("PA_TASK_NAME", "$PA_TASK_NAME");
        javaTask.addGenericInformation("PA_TASK_ID", "$PA_TASK_ID");
        javaTask.addGenericInformation("PA_TASK_ITERATION", "$PA_TASK_ITERATION");
        javaTask.addGenericInformation("PA_TASK_REPLICATION", "$PA_TASK_REPLICATION");
    }

    public void testRegularJob() throws Throwable {

        JobId jobId = schedulerHelper.submitJob(createJob());
        SchedulerTHelper.log("Job submitted, id " + jobId.toString());
        JobState js = schedulerHelper.waitForEventJobSubmitted(jobId);

        checkJobState(js);
        checkTaskState(js.getTasks().get(0));
    }

    public void checkJobState(JobState jobState) {

        HashMap<String, String> expected = new HashMap<>();
        expected.put("PA_JOB_NAME", jobState.getId().getReadableName());
        expected.put("PA_JOB_ID", jobState.getId().toString());

        for (String key : expected.keySet()) {
            Assert.assertEquals("Wrong value for " + key,
                                expected.get(key),
                                jobState.getRuntimeGenericInformation().get(key));
            System.out.println(key + " is " + expected.get(key) + " - good");
        }
    }

    public void checkTaskState(TaskState taskState) {

        HashMap<String, String> expected = new HashMap<>();
        expected.put("PA_JOB_NAME", taskState.getJobId().getReadableName());
        expected.put("PA_JOB_ID", taskState.getJobId().toString());
        expected.put("PA_TASK_NAME", taskState.getName());
        expected.put("PA_TASK_ID", taskState.getId().toString());
        expected.put("PA_TASK_ITERATION", String.valueOf(taskState.getIterationIndex()));
        expected.put("PA_TASK_REPLICATION", String.valueOf(taskState.getReplicationIndex()));

        for (String key : expected.keySet()) {
            Assert.assertEquals("Wrong value for " + key,
                                expected.get(key),
                                taskState.getRuntimeGenericInformation().get(key));
            System.out.println(key + " is " + expected.get(key) + " - good");
        }
    }

    private TaskFlowJob createJobWithReplication() throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        JavaTask mainTask = new JavaTask();
        mainTask.setName("Main task");
        mainTask.setExecutableClassName(TestJavaTask.class.getName());
        mainTask.setFlowBlock(FlowBlock.START);
        String replicateScript = String.format("runs = %d", 3);
        mainTask.setFlowScript(FlowScript.createReplicateFlowScript(replicateScript));
        setGenericInfo(mainTask);
        job.addTask(mainTask);

        JavaTask replicatedTask = new JavaTask();
        replicatedTask.setExecutableClassName(TestJavaTask.class.getName());
        replicatedTask.setName("Replicated task");
        replicatedTask.addDependence(mainTask);
        replicatedTask.addArgument("taskParameter", "test");
        setGenericInfo(replicatedTask);
        job.addTask(replicatedTask);

        JavaTask lastTask = new JavaTask();
        lastTask.setExecutableClassName(TestJavaTask.class.getName());
        lastTask.setName("Replication last task");
        lastTask.setFlowBlock(FlowBlock.END);
        lastTask.addDependence(replicatedTask);
        setGenericInfo(lastTask);

        job.addTask(lastTask);

        return job;
    }

    public void testWithReplication() throws Throwable {

        JobId jobId = schedulerHelper.submitJob(createJobWithReplication());
        SchedulerTHelper.log("Job submitted, id " + jobId.toString());

        schedulerHelper.waitForEventJobFinished(jobId);

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();
        JobState js = scheduler.getJobState(jobId);

        for (TaskState taskState : js.getTasks()) {
            checkTaskState(taskState);
        }
    }

}
