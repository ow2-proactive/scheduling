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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionaltests.job.taskkill;

import functionaltests.utils.SchedulerFunctionalTestWithRestart;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.process_tree_killer.ProcessTree;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.task.TaskLauncher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;


/**
 * @author ProActive team
 *
 * This test check whether ProcessTreeKiller kill properly detached processes launched
 * by a native Task, and only processes launched by this native task.
 *
 * It will run an iteration of test; each iteration will test the ProcessTreeKiller for killed jobs and for normally terminated jobs
 * At each test it will start a single job
 * The job will contain a JavaTask, a Native Task and a Forked Java Task
 *
 * Each task will run a native script (bash or dos), which will run a set 4 detached native executables
 * The test will check that those detached processes have been killed by the PTK (by listing all processes with the given
 * name and counting the number of processes
 *
 */
public class TestProcessTreeKiller extends SchedulerFunctionalTestWithRestart {

    public static URL launchersDir = TestProcessTreeKiller.class
            .getResource("/functionaltests/executables/TestSleep.exe");

    private final static int wait_kill_time = 60000;

    public static final int detachedProcNumber = 4;

    private static final int NB_ITERATIONS = 1;

    private static final String unixSleepName = "sleep";
    private static final String windowsSleepName = "TestSleep.exe";

    @Rule
    public Timeout testTimeout = new Timeout(10, TimeUnit.MINUTES);

    @Test
    public void testProcessTreeKiller() throws Throwable {
        schedulerHelper.addExtraNodes(2);

        Logger.getLogger(ProcessTree.class).setLevel(Level.DEBUG);
        Logger.getLogger(TaskLauncher.class).setLevel(Level.DEBUG);
        for (int i = 0; i < NB_ITERATIONS; i++) {
            log("***************************************************");
            log("************** Iteration " + i + " *************************");
            log("***************************************************");

            log("Creating job...");

            // create job 1 NativeExecutable
            TaskFlowJob job1 = new TaskFlowJob();
            job1.setName(this.getClass().getSimpleName() + "_1");
            job1.setDescription("a command that spawn processes");

            NativeTask task1 = new NativeTask();
            String task1Name = "TestPTK1";
            task1.setName(task1Name);

            String workingDir = new File(TestProcessTreeKiller.launchersDir.toURI()).getParentFile()
                    .getCanonicalPath();
            task1.setForkEnvironment(new ForkEnvironment(workingDir));
            JavaSpawnExecutable executable = new JavaSpawnExecutable();
            executable.home = PASchedulerProperties.SCHEDULER_HOME.getValueAsString();
            task1.setCommandLine(executable.getNativeExecLauncher(false));
            job1.addTask(task1);

            String task2Name = "TestTK2";
            TaskFlowJob job2 = createJavaExecutableJob(task2Name, true);

            log("************** Test with Job Killing *************");
            //submit three jobs
            JobId id1 = schedulerHelper.submitJob(job1);
            JobId id2 = schedulerHelper.submitJob(job2);
            schedulerHelper.waitForEventTaskRunning(id1, task1Name);
            schedulerHelper.waitForEventTaskRunning(id2, task2Name);

            log("************** All 2 tasks running *************");
            TestProcessTreeKiller.waitUntilForkedProcessesAreRunning(detachedProcNumber * 2);
            //we should have 2 times (2 jobs) number of detached processes

            //kill the first job
            log("************** Waiting for the first job (NativeExecutable) to be killed *************");
            schedulerHelper.getSchedulerInterface().killJob(id1);
            schedulerHelper.waitForEventJobFinished(id1);
            log("************** First job killed *************");

            TestProcessTreeKiller.waitUntilForkedProcessesAreRunning(detachedProcNumber);

            //kill the second job
            log("************** Waiting for the second job (JavaExecutable) to be killed *************");
            schedulerHelper.getSchedulerInterface().killJob(id2);
            schedulerHelper.waitForEventJobFinished(id2);
            log("************** Second job killed *************");

            TestProcessTreeKiller.waitUntilForkedProcessesAreRunning(0);

            JobResult res = schedulerHelper.getJobResult(id1);
            assertEquals(JobStatus.KILLED, res.getJobInfo().getStatus());

            res = schedulerHelper.getJobResult(id2);
            assertEquals(JobStatus.KILLED, res.getJobInfo().getStatus());

            log("************** Test with Normal Job termination *************");

            // The test for normal termination in case of NativeExecutable is slightly different
            // we don't spawn here a native zombie process that will wait forever, because that would mean that the
            // NativeExecutable task will also wait forever !
            // This is related to the current implementation of NativeExecutable, as long as there are still some IO streamed
            // from the subprocesses of the main process, the task will wait
            // TODO improve the test by finding a way to run a detached process without IO redirection
            TaskFlowJob job4 = new TaskFlowJob();
            job4.setName(this.getClass().getSimpleName() + "_4");
            job4.setDescription("a command that spawn processes");

            NativeTask task4 = new NativeTask();
            String task4Name = "TestPTK4";
            task4.setName(task4Name);

            task4.setForkEnvironment(new ForkEnvironment(workingDir));
            task4.setCommandLine(executable.getNativeExecLauncher(true));
            job4.addTask(task4);

            //submit three jobs
            id1 = schedulerHelper.submitJob(job4);
            id2 = schedulerHelper.submitJob(job2);
            schedulerHelper.waitForEventTaskRunning(id1, task4Name);
            schedulerHelper.waitForEventTaskRunning(id2, task2Name);

            log("************** All 2 tasks running *************");

            TestProcessTreeKiller.waitUntilForkedProcessesAreRunning(detachedProcNumber);

            //we should have 1 time (2 jobs) number of detached processes as the first job won't spawn any process

            log("************** Waiting for first job (NativeExecutable) to finish *************");
            //wait for the first job to finish normally

            schedulerHelper.waitForEventJobFinished(id1);

            log("************** First job finished *************");

            int runningDetachedProcNumber = countProcesses();
            log("************** number of processes : " + runningDetachedProcNumber);
            assertEquals(detachedProcNumber, runningDetachedProcNumber);

            log("************** Waiting for second job (JavaExecutable) to finish *************");
            //wait for the second job to finish normally
            schedulerHelper.waitForEventJobFinished(id2);
            log("************** Second job finished *************");

            runningDetachedProcNumber = countProcesses();
            log("************** number of processes : " + runningDetachedProcNumber);
            assertEquals(0, runningDetachedProcNumber);

            res = schedulerHelper.getJobResult(id1);
            assertEquals(JobStatus.FINISHED, res.getJobInfo().getStatus());

            res = schedulerHelper.getJobResult(id2);
            assertEquals(JobStatus.FINISHED, res.getJobInfo().getStatus());

        }
    }

    public static TaskFlowJob createJavaExecutableJob(String name, boolean forked) throws UserException {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(name);
        job.setDescription("A command that spawns processes");

        JavaTask task = new JavaTask();
        if (forked) {
            task.setForkEnvironment(new ForkEnvironment());
        }
        task.addArgument("sleep", 3);
        task.addArgument("tname", name);
        task.addArgument("home", PASchedulerProperties.SCHEDULER_HOME.getValueAsString());
        task.setName(name);
        task.setExecutableClassName(JavaSpawnExecutable.class.getName());
        job.addTask(task);
        return job;
    }

    /*
     * Process are killed asynchronously, need wait some time
     */
    public static void waitUntilForkedProcessesAreRunning(int expectedNumber) throws Exception {
        log("************** Waiting until " + expectedNumber +
          " processes are left *************");
        int runningDetachedProcNumber = 0;
        long stopTime = System.currentTimeMillis() + wait_kill_time;
        while (System.currentTimeMillis() < stopTime) {
            runningDetachedProcNumber = countProcesses();

            if (runningDetachedProcNumber == expectedNumber) {
                break;
            } else {
                Thread.sleep(500);
            }
        }
        assertEquals(expectedNumber, runningDetachedProcNumber);
        log("************** " + expectedNumber + " processes are now running *************");
    }

    public static void waitUntilAllForkedProcessesAreKilled() throws Exception {
        waitUntilForkedProcessesAreRunning(0);
    }

    /*
     * Process are killed asynchronously, need wait some time
     */
    public static int countProcesses() throws Exception {
        switch (OperatingSystem.getOperatingSystem()) {
            case windows:
                return getProcessNumberWindows(windowsSleepName);
            case unix:
                return getProcessNumber(unixSleepName);
            default:
                throw new IllegalStateException("Unsupported operating system");
        }
    }

    public static int getProcessNumber(String executableName) throws IOException {
        int toReturn = 0;
        String line;
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", "ps -f -u $(whoami)");
        processBuilder.redirectErrorStream();
        Process p = processBuilder.start();
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        log("Scanning processes");
        while ((line = input.readLine()) != null) {
            log("Process: " + line);
            if (line.contains(executableName)) {
                toReturn++;
            }
        }
        input.close();
        return toReturn;
    }

    public static int getProcessNumberWindows(String executableName) throws IOException {
        int toReturn = 0;
        String line;
        Process p = Runtime.getRuntime().exec("tasklist");
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((line = input.readLine()) != null) {
            if (line.toLowerCase().contains(executableName.toLowerCase())) {
                toReturn++;
            }
        }
        input.close();
        return toReturn;
    }
}
