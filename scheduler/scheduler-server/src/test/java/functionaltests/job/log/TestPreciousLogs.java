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

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.*;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.utils.FileToBytesConverter;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;


/**
 * Test checks that if task is created with 'preciousLogs' attribute set then task output is copied
 * into the user space.
 * <p/>
 * Test creates jobs with native, java and forked java tasks and checks that task log
 * was copied into user data space ant log file contains task output and output
 * of pre- and post- scripts (and fork environment script for forked java task).
 *
 * Test checks as well that log file contain all executions of a task in case of error
 *
 * @author ProActive team
 */
public class TestPreciousLogs extends SchedulerFunctionalTestNoRestart {

    public static class TestJavaTask extends JavaExecutable {

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            getOut().println(TASK_OUTPUT);
            getOut().flush();
            return TASK_OUTPUT;
        }

    }

    public static class TestJavaTaskWithError extends JavaExecutable {

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            getOut().println(TASK_OUTPUT);
            getOut().flush();
            throw new RuntimeException("wanted error");
        }

    }

    private static final int NB_EXECUTIONS = 2;

    private static final String TASK_OUTPUT = "TestTaskOutput";

    private void testPreciousLogs(boolean createJavaTask, boolean forkEnv, boolean generateError) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());

        Map<String, List<String>> expectedOutput = new LinkedHashMap<>();

        for (int i = 0; i < 3; i++) {
            String forkOutput = "forkOutput-" + i;
            String preOutput = "preOutput-" + i;
            String postOutput = "postOutput-" + i;

            List<String> expectedTaskOutput = new ArrayList<>();
            expectedTaskOutput.add(TASK_OUTPUT);
            expectedTaskOutput.add(preOutput);
            if (!generateError) {
                expectedTaskOutput.add(postOutput);
            }

            Task task;
            if (createJavaTask) {
                JavaTask javaTask = new JavaTask();
                if (generateError) {
                    javaTask.setExecutableClassName(TestJavaTaskWithError.class.getName());
                } else {
                    javaTask.setExecutableClassName(TestJavaTask.class.getName());
                }

                if (forkEnv) {
                    ForkEnvironment env = new ForkEnvironment();
                    env.setEnvScript(createScript(forkOutput));
                    javaTask.setForkEnvironment(env);
                    expectedTaskOutput.add(forkOutput);
                }

                task = javaTask;
            } else {
                NativeTask nativeTask = new NativeTask();

                File script = new File(getClass().getResource("/functionaltests/executables/test_echo_task.sh")
                                                 .getFile());
                if (!script.exists()) {
                    Assert.fail("Can't find script " + script.getAbsolutePath());
                }
                nativeTask.setCommandLine(script.getAbsolutePath());
                task = nativeTask;
            }

            if (generateError) {
                task.setMaxNumberOfExecution(NB_EXECUTIONS);
            } else {
                task.setMaxNumberOfExecution(1);
                task.setOnTaskError(OnTaskError.CANCEL_JOB);
            }
            task.setPreciousLogs(true);
            task.setName("Task-" + i);
            task.setPreScript(createScript(preOutput));
            task.setPostScript(createScript(postOutput));

            expectedOutput.put(task.getName(), expectedTaskOutput);

            job.addTask(task);
        }

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        String userURI = scheduler.getUserSpaceURIs().get(0);
        File userFile = new File(new URI(userURI));

        // Clean all log files in user space
        for (File logFile : FileUtils.listFiles(userFile, new String[] { "log" }, true)) {
            FileUtils.deleteQuietly(logFile);
        }

        JobId jobId = schedulerHelper.testJobSubmission(job, true, false);

        JobResult jobResult = scheduler.getJobResult(jobId);
        Map<String, TaskResult> results = jobResult.getAllResults();
        for (String taskName : expectedOutput.keySet()) {

            File taskLog = new File(userFile.toString() + "/" + jobId.value(),
                                    String.format("TaskLogs-%s-%s.log",
                                                  jobId.value(),
                                                  results.get(taskName).getTaskId().value()));
            if (!taskLog.exists()) {
                Assert.fail("Task log file " + taskLog.getAbsolutePath() + " doesn't exist");
            }
            String output = new String(FileToBytesConverter.convertFileToByteArray(taskLog));
            System.out.println("Log file for " + taskName + ":");
            System.out.println(output);

            for (String expectedLine : expectedOutput.get(taskName)) {
                Assert.assertTrue("Output doesn't contain line " + expectedLine, output.contains(expectedLine));
                int expectedOccurrences = (generateError ? NB_EXECUTIONS : 1);
                Assert.assertEquals("Output doesn't contain " + expectedOccurrences + " occurrences of line " +
                                    expectedLine, expectedOccurrences, StringUtils.countMatches(output, expectedLine));
            }
        }
    }

    @Test
    public void testPreciousLogsTransferNativeTask() throws Exception {
        Assume.assumeTrue(OperatingSystem.getOperatingSystem() == OperatingSystem.unix);

        System.out.println("Test native task");
        testPreciousLogs(false, false, false);
    }

    @Test
    public void testPreciousLogsTransferJavaTask() throws Exception {
        System.out.println("Test java task");
        testPreciousLogs(true, false, false);
    }

    @Test
    public void testPreciousLogsTransferForkedJavaTask() throws Exception {
        System.out.println("Test forked java task");
        testPreciousLogs(true, true, false);
    }

    @Test
    public void testPreciousLogsTransferForkedJavaTaskWithError() throws Exception {
        System.out.println("Test forked java task with error");
        testPreciousLogs(true, true, true);
    }

    static SimpleScript createScript(String scriptOutput) throws Exception {
        return new SimpleScript(String.format("print('%s')", scriptOutput), "js");
    }

}
