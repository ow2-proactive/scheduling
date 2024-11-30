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
package functionaltests.job.log;

import static functionaltests.utils.SchedulerTHelper.log;
import io.github.pixee.security.BoundedLineReader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.examples.WaitAndPrint;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.util.JobLogger;
import org.ow2.proactive.scheduler.util.ServerJobAndTaskLogs;
import org.ow2.proactive.scheduler.util.TaskLogger;
import org.ow2.proactive.scripting.SelectionScript;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;


/**
 * Test that we can retrieve job/task server logs and the semantic
 * of logs.
 * <p/>
 * - for finished job we have logs for all the tasks
 * - for pending jobs we have correct RM output & script output
 */
public class TestJobServerLogs extends SchedulerFunctionalTestNoRestart {

    public final int TASKS_IN_SIMPLE_JOB = 2;

    private static URL simpleJobDescriptor = TestJobServerLogs.class.getResource("/functionaltests/descriptors/Job_simple.xml");

    private final String SCRIPT_OUTPUT = "SCRIPT_OUTPUT_" + Math.random();

    private String logsLocation = ServerJobAndTaskLogs.getInstance().getLogsLocation();

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("kk:mm:ss:SSS");

    private Job createPendingJob() throws Exception {
        final TaskFlowJob job = new TaskFlowJob();
        JavaTask task = new JavaTask();
        task.setName("jt");
        task.setExecutableClassName(WaitAndPrint.class.getName());
        task.addArgument("sleepTime", "1");

        task.addSelectionScript(new SelectionScript("print('" + SCRIPT_OUTPUT + "'); selected = false;", "javascript"));
        job.addTask(task);

        return job;
    }

    @Test
    public void test() throws Exception {
        JobId simpleJobId = schedulerHelper.submitJob(new File(simpleJobDescriptor.toURI()).getAbsolutePath());

        String taskName = "task1";

        TaskInfo ti = schedulerHelper.waitForEventTaskRunning(simpleJobId, taskName);
        String taskLogs = schedulerHelper.getSchedulerInterface().getTaskServerLogs(simpleJobId.toString(), taskName);

        if (!taskLogs.contains("task " + ti.getTaskId() + " (" + ti.getTaskId().getReadableName() + ")" + " started")) {
            log("Incorrect task server logs:");
            log(taskLogs);
            fail("Task " + ti.getTaskId() + " was not scheduled");
        }

        schedulerHelper.waitForEventJobFinished(simpleJobId);
        String jobLogs = schedulerHelper.getSchedulerInterface().getJobServerLogs(simpleJobId.toString());
        for (int i = 0; i < TASKS_IN_SIMPLE_JOB; i++) {
            TaskId taskId = TaskIdImpl.createTaskId(simpleJobId, "task" + (i + 1), i);
            String taskIdString = taskId.toString();
            String taskIdStringQuoted = Pattern.quote(taskIdString);

            if (!matchLine(jobLogs, "task " + taskIdStringQuoted + " \\(task[12]\\) started")) {
                log("Incorrect job server logs");
                log(jobLogs);
                fail("Task " + taskIdString + " was not scheduled");
            }
            if (!matchLine(jobLogs, "task " + taskIdStringQuoted + " \\(task[12]\\) finished")) {
                log("Incorrect job server logs");
                log(jobLogs);
                fail("Task " + taskIdString + " was not finished");
            }
        }

        checkRemoval(simpleJobId);

        JobId pendingJobId = schedulerHelper.submitJob(createPendingJob());
        Thread.sleep(5000);
        jobLogs = schedulerHelper.getSchedulerInterface().getJobServerLogs(pendingJobId.toString());
        if (!jobLogs.contains("will get 0 nodes")) {
            log("Incorrect job server logs");
            log(jobLogs);
            fail("RM output is not correct");
        }

        if (!jobLogs.contains(SCRIPT_OUTPUT)) {
            log("Incorrect job server logs");
            log(jobLogs);
            fail("No script output");
        }

        checkRemoval(pendingJobId);
    }

    public void checkRemoval(JobId jobId) throws Exception {
        JobState jobState = schedulerHelper.getSchedulerInterface().getJobState(jobId);
        List<TaskState> tasks = jobState.getTasks();

        checkJobAndTaskLogFiles(jobId, tasks, true);

        // Before it was only job removal but it was bug prone (for pendingJob only)
        // because the following sequence of the events could happen:
        // 1. SchedulerMethodImpl main loop retrieves info about this forever pending job
        // 2. we call removeJob
        // 3. SchedulerMethodImpl main loop asks for RM::getRMNodes.
        //
        // Third action would print "[SelectionManager.doSelectNodes] "scheduler"..." to the task log
        // which would actually re-created removed log file which would cause an error
        // in the last line of this method
        // that is why kill the job and after wait 5s to make sure that
        // iteration of SchedulerMethodImpl main loop is finished,
        // and finally, we remove job with its log files.
        schedulerHelper.killJob(jobId.value());
        Thread.sleep(5000);
        schedulerHelper.removeJob(jobId);

        schedulerHelper.waitForEventJobRemoved(jobId);

        System.out.println("Suppose to remove all logs at " + simpleDateFormat.format(new Date()));

        checkNoLogsFromAPI(jobId, tasks);
        checkJobAndTaskLogFiles(jobId, tasks, false);
    }

    private void checkNoLogsFromAPI(JobId jobId, List<TaskState> tasks) throws Exception {
        Scheduler scheduler = schedulerHelper.getSchedulerInterface();
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

    private void checkJobAndTaskLogFiles(JobId jobId, List<TaskState> tasks, boolean shouldExist) throws Exception {
        checkFile(shouldExist, new File(logsLocation, JobLogger.getJobLogRelativePath(jobId)));
        for (TaskState taskState : tasks) {
            checkFile(shouldExist, new File(logsLocation, TaskLogger.getTaskLogRelativePath(taskState.getId())));
        }
    }

    private void checkFile(boolean shouldExist, File jobLogFile) {
        String message = String.format("Log file %s should %s", jobLogFile, shouldExist ? "exist" : "not exist");
        final boolean actualExistings = jobLogFile.exists();
        if (actualExistings != shouldExist) {
            printDiagnosticMessage();
        }
        assertEquals(message, shouldExist, actualExistings);
    }

    public static boolean matchLine(String text, String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        return pattern.matcher(text).find();
    }

    private void printDiagnosticMessage() {
        int LIMIT = 5;
        System.out.println("This test is going to fail, but before we print diagnostic message." +
                           simpleDateFormat.format(new Date()));
        // iterate over all files in the 'logsLocation'
        for (File file : FileUtils.listFiles(new File(logsLocation),
                                             TrueFileFilter.INSTANCE,
                                             TrueFileFilter.INSTANCE)) {
            try {
                BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                System.out.println(String.format("Name: %s, Size: %d, Created: %s, Modified: %s",
                                                 file.getAbsolutePath(),
                                                 attr.size(),
                                                 attr.creationTime(),
                                                 attr.lastModifiedTime()));
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                int i;
                // print up to LIMIT first lines
                for (i = 0; i < LIMIT && (line = BoundedLineReader.readLine(br, 5_000_000)) != null; ++i) {
                    System.out.println(line);
                }

                Queue<String> queue = new CircularFifoQueue<>(LIMIT);
                // reading last LIMIT lines
                for (; (line = BoundedLineReader.readLine(br, 5_000_000)) != null; ++i) {
                    queue.add(line);
                }

                if (i >= LIMIT * 2) { // if there is more line than 2*LIMIT
                    System.out.println(".......");
                    System.out.println("....... (skipped content)");
                    System.out.println(".......");
                }
                for (String l : queue) { // print rest of the file
                    System.out.println(l);
                }

                System.out.println("------------------------------------");
                System.out.println();
            } catch (IOException e) {
                System.out.println("Exception ocurred during accessing file attributes " + e);
            }
        }
    }

}
