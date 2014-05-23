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
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Test checks that if task is created with 'preciousLogs' attribute set then task output is copied
 * into the job output space.
 * <p/>
 * Test creates jobs with native, java and forked java tasks and checks that task log
 * was copied into output data space ant log file contains task output and output
 * of pre- and post- scripts (and fork environment script for forked java task). 
 * 
 * @author ProActive team
 *
 */
public class TestPreciousLogs extends SchedulerConsecutive {

    public static class TestJavaTask extends JavaExecutable {

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            System.out.println(TASK_OUTPUT);
            return TASK_OUTPUT;
        }

    }

    private File output;

    @Before
    public void createDataSpacesDirs() throws IOException {
        output = createTmpDir("output");
        System.out.println("Tmp output dir: " + output.getAbsolutePath());
    }

    @After
    public void removeDataSpacesDirs() throws IOException {
        removeDir(output);
    }

    static final String TASK_OUTPUT = "TestTaskOutput";

    private void testPreciousLogs(boolean createJavaTask, boolean forkEnv) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());
        job.setOutputSpace(output.toURI().toString());

        Map<String, List<String>> expectedOutput = new LinkedHashMap<String, List<String>>();

        for (int i = 0; i < 3; i++) {
            String forkOutput = "forkOutput-" + i;
            String preOutput = "preOutput-" + i;
            String postOutput = "postOutput-" + i;

            List<String> expectedTaskOutput = new ArrayList<String>();
            expectedTaskOutput.add(TASK_OUTPUT);
            expectedTaskOutput.add(preOutput);
            expectedTaskOutput.add(postOutput);

            Task task;
            if (createJavaTask) {
                JavaTask javaTask = new JavaTask();
                javaTask.setExecutableClassName(TestJavaTask.class.getName());

                if (forkEnv) {
                    ForkEnvironment env = new ForkEnvironment();
                    env.setEnvScript(createScript(forkOutput));
                    javaTask.setForkEnvironment(env);
                    expectedTaskOutput.add(forkOutput);
                }

                task = javaTask;
            } else {
                NativeTask nativeTask = new NativeTask();

                File script = new File(getClass().getResource(
                        "/functionaltests/executables/test_echo_task.sh").getFile());
                if (!script.exists()) {
                    Assert.fail("Can't find script " + script.getAbsolutePath());
                }
                nativeTask.setCommandLine(script.getAbsolutePath());
                task = nativeTask;
            }

            task.setMaxNumberOfExecution(1);
            task.setCancelJobOnError(true);
            task.setPreciousLogs(true);
            task.setName("Task-" + i);
            task.setPreScript(createScript(preOutput));
            task.setPostScript(createScript(postOutput));

            expectedOutput.put(task.getName(), expectedTaskOutput);

            job.addTask(task);
        }

        JobId jobId = SchedulerTHelper.submitJob(job);
        SchedulerTHelper.waitForEventJobFinished(jobId);

        Scheduler scheduler = SchedulerTHelper.getSchedulerInterface();
        JobResult jobResult = scheduler.getJobResult(jobId);
        Map<String, TaskResult> results = jobResult.getAllResults();
        for (TaskResult taskResult : results.values()) {
            if (taskResult.getException() != null) {
                taskResult.getException().printStackTrace();
                Assert.fail("Task failed with exception " + taskResult.getException());
            }
            System.out.println("Task output:");
            System.out.println(taskResult.getOutput().getAllLogs(false));
        }

        for (String taskName : expectedOutput.keySet()) {

            File taskLog = new File(output, String.format("TaskLogs-%s-%s.log", jobId.value(), results.get(
                    taskName).getTaskId().value()));
            if (!taskLog.exists()) {
                Assert.fail("Task log file " + taskLog.getAbsolutePath() + " doesn't exist");
            }
            String output = new String(FileToBytesConverter.convertFileToByteArray(taskLog));
            System.out.println("Log file for " + taskName + ":");
            System.out.println(output);

            for (String expectedLine : expectedOutput.get(taskName)) {
                Assert.assertTrue("Output doesn't contain line " + expectedLine, output
                        .contains(expectedLine));
            }
        }
    }

    @Test
    public void testPreciousLogsTransfer() throws Exception {
        if (OperatingSystem.getOperatingSystem() == OperatingSystem.unix) {
            System.out.println("Test native task");
            testPreciousLogs(false, false);
        }

        System.out.println("Test java task");
        testPreciousLogs(true, false);

        System.out.println("Test forked java task");
        testPreciousLogs(true, true);
    }

    static SimpleScript createScript(String scriptOutput) throws Exception {
        return new SimpleScript(String.format("print('%s')", scriptOutput), "js");
    }

    static void removeDir(File dir) throws IOException {
        if (dir == null) {
            return;
        }
        for (File file : dir.listFiles()) {
            if (!file.delete()) {
                throw new IOException("Failed to delete file " + file.getAbsolutePath());
            }
        }
        if (!dir.delete()) {
            throw new IOException("Failed to delete dir " + dir.getAbsolutePath());
        }
    }

    static File createTmpDir(String suffix) throws IOException {
        File file = File.createTempFile("test", ".input");
        if (!file.delete()) {
            throw new IOException("Failed to delete file " + file.getAbsolutePath());
        }
        if (!file.mkdir()) {
            throw new IOException("Failed to crete dir " + file.getAbsolutePath());
        }
        return file;
    }

}
