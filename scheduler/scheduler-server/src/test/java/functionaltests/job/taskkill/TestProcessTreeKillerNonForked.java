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
package functionaltests.job.taskkill;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.ow2.proactive.process_tree_killer.ProcessTree;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.task.TaskLauncher;

import functionaltests.utils.SchedulerFunctionalTestNonForkModeWithRestart;


/**
 * @author ProActive team
 *
 * This test check whether ProcessTreeKiller kill properly detached processes launched
 * by a native Task, and only processes launched by this native task.
 *
 * It will run an iteration of test; each iteration will test the ProcessTreeKiller for killed jobs and for normally terminated jobs
 * At each test it will start a single job
 * The job will contain a JavaTask and a Native Task in non-forked mode
 *
 * Each task will run a native script (bash or dos), which will run a set 4 detached native executables
 * The test will check that those detached processes have been killed by the PTK (by listing all processes with the given
 * name and counting the number of processes)
 *
 */
public class TestProcessTreeKillerNonForked extends SchedulerFunctionalTestNonForkModeWithRestart {

    @Rule
    public Timeout testTimeout = new Timeout(10, TimeUnit.MINUTES);

    @Test
    public void testProcessTreeKiller() throws Throwable {
        schedulerHelper.addExtraNodes(2);

        Logger.getLogger(ProcessTree.class).setLevel(Level.DEBUG);
        Logger.getLogger(TaskLauncher.class).setLevel(Level.DEBUG);
        for (int i = 0; i < TestProcessTreeKillerUtil.NB_ITERATIONS; i++) {
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

            // In non-forked mode, the java executable which spawns processes manages its own process tree killer
            JavaSpawnExecutable executable = new JavaSpawnExecutable();
            executable.home = PASchedulerProperties.SCHEDULER_HOME.getValueAsString();
            task1.setCommandLine(executable.getNativeExecLauncher(false));
            job1.addTask(task1);

            String task2Name = "TestTK2";
            TaskFlowJob job2 = TestProcessTreeKillerUtil.createJavaExecutableJob(task2Name, false);

            log("************** Test with Job Killing *************");
            //submit three jobs
            JobId id1 = schedulerHelper.submitJob(job1);
            JobId id2 = schedulerHelper.submitJob(job2);
            schedulerHelper.waitForEventTaskRunning(id1, task1Name);
            schedulerHelper.waitForEventTaskRunning(id2, task2Name);

            log("************** All 2 tasks running *************");
            TestProcessTreeKillerUtil.waitUntilForkedProcessesAreRunning(TestProcessTreeKillerUtil.detachedProcNumber *
                                                                         2);
            //we should have 2 times (2 jobs) number of detached processes

            //kill the first job
            log("************** Waiting for the first job (NativeExecutable) to be killed *************");
            schedulerHelper.getSchedulerInterface().killJob(id1);
            schedulerHelper.waitForEventJobFinished(id1);
            log("************** First job killed *************");

            TestProcessTreeKillerUtil.waitUntilForkedProcessesAreRunning(TestProcessTreeKillerUtil.detachedProcNumber);

            //kill the second job
            log("************** Waiting for the second job (JavaExecutable) to be killed *************");
            schedulerHelper.getSchedulerInterface().killJob(id2);
            schedulerHelper.waitForEventJobFinished(id2);
            log("************** Second job killed *************");

            TestProcessTreeKillerUtil.waitUntilForkedProcessesAreRunning(0);

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

            task4.setCommandLine(executable.getNativeExecLauncher(true));
            task4.setPreciousLogs(true);
            job4.addTask(task4);

            //submit three jobs
            id1 = schedulerHelper.submitJob(job4);
            id2 = schedulerHelper.submitJob(job2);
            schedulerHelper.waitForEventTaskRunning(id1, task4Name);
            schedulerHelper.waitForEventTaskRunning(id2, task2Name);

            log("************** All 2 tasks running *************");

            TestProcessTreeKillerUtil.waitUntilForkedProcessesAreRunning(TestProcessTreeKillerUtil.detachedProcNumber);

            //we should have 1 time (2 jobs) number of detached processes as the first job won't spawn any process

            log("************** Waiting for both jobs to finish *************");
            schedulerHelper.waitForEventJobFinished(id1);
            schedulerHelper.waitForEventJobFinished(id2);

            schedulerHelper.waitForEventJobFinished(id1);

            log("************** Both job finished *************");

            int runningDetachedProcNumber = TestProcessTreeKillerUtil.countProcesses();
            log("************** number of processes : " + runningDetachedProcNumber);
            assertEquals(0, runningDetachedProcNumber);

            res = schedulerHelper.getJobResult(id1);
            assertEquals(JobStatus.FINISHED, res.getJobInfo().getStatus());

            res = schedulerHelper.getJobResult(id2);
            assertEquals(JobStatus.FINISHED, res.getJobInfo().getStatus());

        }
    }

}
