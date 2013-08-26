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

import java.io.File;
import java.net.URL;

import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.examples.WaitAndPrint;
import org.ow2.proactive.scripting.SelectionScript;
import org.junit.Assert;
import org.junit.Test;


/**
 * 
 * Test that we can retrieve job/task server logs and the semantic
 * of logs.
 * 
 * - for finished job we have logs for all the tasks
 * - for pending jobs we have correct RM output & script output
 * 
 */
public class TestJobServerLogs extends SchedulerConsecutive {

    public final int TASKS_IN_SIMPLE_JOB = 2;
    private static URL simpleJobDescriptor = TestJobTaskFlowSubmission.class
            .getResource("/functionaltests/descriptors/Job_simple.xml");

    private final String SCRIPT_OUTPUT = "SCRIPT_OUTPUT_" + Math.random();

    private Job createPendingJob() throws Exception {
        final TaskFlowJob job = new TaskFlowJob();
        JavaTask task = new JavaTask();
        task.setName("jt");
        task.setExecutableClassName(WaitAndPrint.class.getName());
        task.addArgument("sleepTime", "1");

        task.addSelectionScript(new SelectionScript("print('" + SCRIPT_OUTPUT + "'); selected = false;",
            "javascript"));
        job.addTask(task);

        return job;
    }

    @Test
    public void test() throws Exception {
        JobId simpleJobId = SchedulerTHelper.submitJob(new File(simpleJobDescriptor.toURI())
                .getAbsolutePath());

        String taskName = "task1";

        TaskInfo ti = SchedulerTHelper.waitForEventTaskRunning(simpleJobId, taskName);
        String taskLogs = SchedulerTHelper.getSchedulerInterface().getTaskServerLogs(simpleJobId.toString(),
                taskName);

        if (!taskLogs.contains("task " + ti.getTaskId() + " scheduling")) {
            System.out.println("Incorrect task server logs");
            System.out.println(taskLogs);
            Assert.fail("Task " + ti.getTaskId() + " was not scheduled");
        }

        SchedulerTHelper.waitForEventJobFinished(simpleJobId);
        String jobLogs = SchedulerTHelper.getSchedulerInterface().getJobServerLogs(simpleJobId.toString());
        for (int i = 0; i < TASKS_IN_SIMPLE_JOB; i++) {
            if (!jobLogs.contains("task " + simpleJobId + "000" + i + " scheduling")) {
                System.out.println("Incorrect job server logs");
                System.out.println(jobLogs);
                Assert.fail("Task " + simpleJobId + "000" + i + " was not scheduled");
            }
            if (!jobLogs.contains("task " + simpleJobId + "000" + i + " finished")) {
                System.out.println("Incorrect job server logs");
                System.out.println(jobLogs);
                Assert.fail("Task " + simpleJobId + "000" + i + " was not finished");
            }
        }

        JobId pendingJobId = SchedulerTHelper.submitJob(createPendingJob());
        Thread.sleep(5000);
        jobLogs = SchedulerTHelper.getSchedulerInterface().getJobServerLogs(pendingJobId.toString());
        if (!jobLogs.contains("0 nodes found after scripts execution")) {
            System.out.println("Incorrect job server logs");
            System.out.println(jobLogs);
            Assert.fail("RM output is not correct");
        }

        if (!jobLogs.contains(SCRIPT_OUTPUT)) {
            System.out.println("Incorrect job server logs");
            System.out.println(jobLogs);
            Assert.fail("No script output");
        }
        SchedulerTHelper.removeJob(pendingJobId);
        SchedulerTHelper.waitForEventJobRemoved(pendingJobId);
    }
}
