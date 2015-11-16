/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests;

import java.io.Serializable;
import java.util.HashMap;

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
import org.junit.Assert;
import org.junit.Test;


/**
 * Checking that variables $PAS_JOB_NAME, $PAS_JOB_ID, $PAS_TASK_NAME, $PAS_TASK_ID, $PAS_TASK_ITERATION
 * $PAS_TASK_REPLICATION are replaced by it's actual value in generic information.
 * 
 */
public class TestGenericInformation extends SchedulerConsecutive {

    private final String TASK_NAME = "task name";
    private final String JOB_NAME = this.getClass().getSimpleName();

    public static class TestJavaTask extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            System.out.println("OK");
            return "OK";
        }
    }

    @Test
    public void action() throws Throwable {
        testRegularJob();
        testWithReplication();
    }

    private TaskFlowJob createJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(JOB_NAME);
        setGenericInfo(job);

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(TestJavaTask.class.getName());
        javaTask.setName(TASK_NAME);

        setGenericInfo(javaTask);
        job.addTask(javaTask);

        return job;
    }

    private void setGenericInfo(TaskFlowJob job) {
        job.addGenericInformation("PAS_JOB_NAME", "$PAS_JOB_NAME");
        job.addGenericInformation("PAS_JOB_ID", "$PAS_JOB_ID");
    }

    private static void setGenericInfo(JavaTask javaTask) {
        javaTask.addGenericInformation("PAS_JOB_NAME", "$PAS_JOB_NAME");
        javaTask.addGenericInformation("PAS_JOB_ID", "$PAS_JOB_ID");
        javaTask.addGenericInformation("PAS_TASK_NAME", "$PAS_TASK_NAME");
        javaTask.addGenericInformation("PAS_TASK_ID", "$PAS_TASK_ID");
        javaTask.addGenericInformation("PAS_TASK_ITERATION", "$PAS_TASK_ITERATION");
        javaTask.addGenericInformation("PAS_TASK_REPLICATION", "$PAS_TASK_REPLICATION");
    }

    public void testRegularJob() throws Throwable {

        JobId jobId = SchedulerTHelper.submitJob(createJob());
        SchedulerTHelper.log("Job submitted, id " + jobId.toString());
        JobState js = SchedulerTHelper.waitForEventJobSubmitted(jobId);

        checkJobState(js);
        checkTaskState(js.getTasks().get(0));
    }

    public void checkJobState(JobState jobState) {

        HashMap<String, String> expected = new HashMap<String, String>();
        expected.put("PAS_JOB_NAME", jobState.getId().getReadableName());
        expected.put("PAS_JOB_ID", jobState.getId().toString());

        for (String key : expected.keySet()) {
            Assert.assertEquals("Wrong value for " + key, expected.get(key), jobState
                    .getGenericInformations().get(key));
            System.out.println(key + " is " + expected.get(key) + " - good");
        }
    }

    public void checkTaskState(TaskState taskState) {

        HashMap<String, String> expected = new HashMap<String, String>();
        expected.put("PAS_JOB_NAME", taskState.getJobId().getReadableName());
        expected.put("PAS_JOB_ID", taskState.getJobId().toString());
        expected.put("PAS_TASK_NAME", taskState.getName());
        expected.put("PAS_TASK_ID", taskState.getId().toString());
        expected.put("PAS_TASK_ITERATION", String.valueOf(taskState.getIterationIndex()));
        expected.put("PAS_TASK_REPLICATION", String.valueOf(taskState.getReplicationIndex()));

        for (String key : expected.keySet()) {
            Assert.assertEquals("Wrong value for " + key, expected.get(key), taskState
                    .getGenericInformations().get(key));
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

        JobId jobId = SchedulerTHelper.submitJob(createJobWithReplication());
        SchedulerTHelper.log("Job submitted, id " + jobId.toString());

        SchedulerTHelper.waitForEventJobFinished(jobId);

        Scheduler scheduler = SchedulerTHelper.getSchedulerInterface();
        JobState js = scheduler.getJobState(jobId);

        for (TaskState taskState : js.getTasks()) {
            checkTaskState(taskState);
        }
    }

}
