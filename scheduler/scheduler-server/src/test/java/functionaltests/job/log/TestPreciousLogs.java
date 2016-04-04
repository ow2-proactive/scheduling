/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package functionaltests.job.log;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import org.junit.Assert;
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

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Test checks that if task is created with 'preciousLogs' attribute set then task output is copied
 * into the user space.
 * <p/>
 * Test creates jobs with native, java and forked java tasks and checks that task log
 * was copied into user data space ant log file contains task output and output
 * of pre- and post- scripts (and fork environment script for forked java task).
 *
 * @author ProActive team
 */
public class TestPreciousLogs extends SchedulerFunctionalTestNoRestart {

    public static class TestJavaTask extends JavaExecutable {

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            getOut().println(TASK_OUTPUT);
            return TASK_OUTPUT;
        }

    }

    static final String TASK_OUTPUT = "TestTaskOutput";

    private void testPreciousLogs(boolean createJavaTask, boolean forkEnv) throws Exception {
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
            task.setOnTaskError(OnTaskError.CANCEL_JOB);
            task.setPreciousLogs(true);
            task.setName("Task-" + i);
            task.setPreScript(createScript(preOutput));
            task.setPostScript(createScript(postOutput));

            expectedOutput.put(task.getName(), expectedTaskOutput);

            job.addTask(task);
        }

        JobId jobId = schedulerHelper.testJobSubmission(job);

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        String userURI = scheduler.getUserSpaceURIs().get(0);
        String userPath = new File(new URI(userURI)).getAbsolutePath();

        JobResult jobResult = scheduler.getJobResult(jobId);
        Map<String, TaskResult> results = jobResult.getAllResults();
        for (String taskName : expectedOutput.keySet()) {

            File taskLog = new File(userPath, String.format("TaskLogs-%s-%s.log", jobId.value(), results.get(
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

        System.out.println("Test script task");
        testPreciousLogs(true, false);

        System.out.println("Test forked java task");
        testPreciousLogs(true, true);
    }

    static SimpleScript createScript(String scriptOutput) throws Exception {
        return new SimpleScript(String.format("print('%s')", scriptOutput), "js");
    }

}
