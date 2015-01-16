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
import java.util.List;
import java.util.regex.Pattern;

import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.examples.WaitAndPrint;
import org.ow2.proactive.scheduler.util.ServerJobAndTaskLogs;
import org.ow2.proactive.scripting.SelectionScript;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Test that we can retrieve job/task server logs and the semantic
 * of logs.
 * <p/>
 * - for finished job we have logs for all the tasks
 * - for pending jobs we have correct RM output & script output
 */
public class TestJobServerLogs extends SchedulerConsecutive {

    public final int TASKS_IN_SIMPLE_JOB = 2;
    private static URL simpleJobDescriptor = TestJobTaskFlowSubmission.class
            .getResource("/functionaltests/descriptors/Job_simple.xml");

    private final String SCRIPT_OUTPUT = "SCRIPT_OUTPUT_" + Math.random();
    private String logsLocation = ServerJobAndTaskLogs.getLogsLocation();

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

        if (!taskLogs.contains("task " + ti.getTaskId() + " (" + ti.getTaskId().getReadableName() + ")" + " scheduling")) {
            System.out.println("Incorrect task server logs:");
            System.out.println(taskLogs);
            Assert.fail("Task " + ti.getTaskId() + " was not scheduled");
        }

        SchedulerTHelper.waitForEventJobFinished(simpleJobId);
        String jobLogs = SchedulerTHelper.getSchedulerInterface().getJobServerLogs(simpleJobId.toString());
        for (int i = 0; i < TASKS_IN_SIMPLE_JOB; i++) {
        if (!matchLine(jobLogs, "task " + simpleJobId + "000" + i + " \\(task[12]\\) scheduling")) {
                System.out.println("Incorrect job server logs");
                System.out.println(jobLogs);
                Assert.fail("Task " + simpleJobId + "000" + i + " was not scheduled");
            }
            if (!matchLine(jobLogs, "task " + simpleJobId + "000" + i + " \\(task[12]\\) finished")) {
                System.out.println("Incorrect job server logs");
                System.out.println(jobLogs);
                Assert.fail("Task " + simpleJobId + "000" + i + " was not finished");
            }
        }

        checkRemoval(simpleJobId);

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

        checkRemoval(pendingJobId);
    }

    public void checkRemoval(JobId jobId) throws Exception {
        JobState jobState = SchedulerTHelper.getSchedulerInterface().getJobState(jobId);
        List<TaskState> tasks = jobState.getTasks();

        checkJobAndTaskLogFiles(jobId, tasks, true);

        SchedulerTHelper.removeJob(jobId);
        SchedulerTHelper.waitForEventJobRemoved(jobId);

        checkNoLogsFromAPI(jobId, tasks);
        checkJobAndTaskLogFiles(jobId, tasks, false);
    }

    private void checkNoLogsFromAPI(JobId jobId, List<TaskState> tasks) throws Exception {
        Scheduler scheduler = SchedulerTHelper.getSchedulerInterface();
        try {
            scheduler.getJobServerLogs(jobId.toString());
            fail("getJobServerLogs should throw an exception for a removed job");
        } catch (UnknownJobException expected) {
        }
        for (TaskState taskState : tasks) {
            try {
                scheduler.getTaskServerLogs(jobId.toString(), taskState.getName());
                fail("getTaskServerLogs should throw an exception for a removed job");
            } catch (UnknownJobException expected) {
            }
        }
    }

    private void checkJobAndTaskLogFiles(JobId jobId, List<TaskState> tasks, boolean shouldExist)
            throws Exception {
        checkFile(shouldExist, new File(logsLocation, jobId.toString()));
        for (TaskState taskState : tasks) {
            checkFile(shouldExist, new File(logsLocation, taskState.getId().toString()));
        }
    }

    private void checkFile(boolean shouldExist, File jobLogFile) {
        String message = String.format("Log file %s should %s", jobLogFile, shouldExist ? "exist"
                : "not exist");
        assertEquals(message, shouldExist, jobLogFile.exists());
    }

    public static boolean matchLine(String text, String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        return pattern.matcher(text).find();
    }

}
