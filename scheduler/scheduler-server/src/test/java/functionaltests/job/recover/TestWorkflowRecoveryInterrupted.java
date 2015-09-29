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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.job.recover;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import functionaltests.utils.SchedulerFunctionalTest;
import functionaltests.utils.SchedulerTHelper;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;


/**
 * Tests the recovery after scheduler crash of workflow-enabled jobs
 * Crashes the scheduler while job are being scheduled
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestWorkflowRecoveryInterrupted extends SchedulerFunctionalTest {

    private static final String job_prefix = "/functionaltests/workflow/descriptors/flow_crash_int_";

    private static final String[] job_1 = { "T 0 ()", "T1 1 (T)", "T5 2 (T1)", "T6 2 (T1)", "T2 2 (T1)",
            "T3 7 (T2 T5 T6)", "T1#1 8 (T3)", "T5#1 9 (T1#1)", "T6#1 9 (T1#1)", "T2#1 9 (T1#1)",
            "T3#1 28 (T2#1 T5#1 T6#1)", "T1#2 29 (T3#1)", "T5#2 30 (T1#2)", "T6#2 30 (T1#2)",
            "T2#2 30 (T1#2)", "T3#2 91 (T2#2 T5#2 T6#2)", "T4 92 (T3#2)" };

    private static final String[] job_2 = { "T 0 ()", "T1 1 (T)", "T2 2 (T1)", "T1*1 1 (T)", "T2*1 2 (T1*1)",
            "T1*2 1 (T)", "T2*2 2 (T1*2)", "T3 7 (T2 T2*1 T2*2)", "T#1 8 (T3)", "T1#1 9 (T#1)",
            "T2#1 10 (T1#1)", "T1#1*1 9 (T#1)", "T2#1*1 10 (T1#1*1)", "T1#1*2 9 (T#1)", "T2#1*2 10 (T1#1*2)",
            "T3#1 31 (T2#1 T2#1*1 T2#1*2)", "T#2 32 (T3#1)", "T1#2 33 (T#2)", "T2#2 34 (T1#2)",
            "T1#2*1 33 (T#2)", "T2#2*1 34 (T1#2*1)", "T1#2*2 33 (T#2)", "T2#2*2 34 (T1#2*2)",
            "T3#2 103 (T2#2 T2#2*1 T2#2*2)" };

    @Test
    public void run() throws Throwable {
        // lpellegr: test is ignored on Windows while I am investigating the issue
        assumeTrue(OperatingSystem.getOperatingSystem() != OperatingSystem.windows);

        job1();
        job2();
        job3();
        ifInALoop();
        skippedTasksInALoop();
    }

    private void skippedTasksInALoop() throws Exception {
        File lockFile = new File(System.getProperty("java.io.tmpdir"), "skipped_tasks_in_a_loop.lock");
        lockFile.deleteOnExit();
        FileUtils.deleteQuietly(lockFile);

        JobId jobId = schedulerHelper.submitJob(new File(TestWorkflowRecoveryInterrupted.class.getResource(
          "/functionaltests/workflow/descriptors/flow_skipped_tasks_in_a_loop.xml").toURI()).getAbsolutePath());

        schedulerHelper.waitForEventTaskFinished(jobId, "NothingToDo");

        log("Crashing scheduler before the job can finish");
        schedulerHelper.killSchedulerAndNodesAndRestart(new File(SchedulerTHelper.class.getResource(
          "/functionaltests/config/functionalTSchedulerProperties-updateDB.ini").toURI()).getAbsolutePath());

        FileUtils.touch(lockFile);

        log("Job should run successfully after restart");
        schedulerHelper.waitForEventJobFinished(jobId);
        JobResult res = schedulerHelper.getJobResult(jobId);
        assertFalse(res.hadException());
    }

    // SCHEDULING-2224 If in a Loop workflow does not complete if Scheduler is restarted
    private void ifInALoop() throws Exception {
        JobId jobId = schedulerHelper.submitJob(new File(TestWorkflowRecoveryInterrupted.class.getResource(
                "/functionaltests/workflow/descriptors/flow_if_in_a_loop.xml").toURI()).getAbsolutePath());

        log("Crashing scheduler before the job can start");
        schedulerHelper.killSchedulerAndNodesAndRestart(new File(SchedulerTHelper.class.getResource(
          "/functionaltests/config/functionalTSchedulerProperties-updateDB.ini").toURI()).getAbsolutePath());

        log("Job should run successfully after restart");
        schedulerHelper.waitForEventJobFinished(jobId);
    }

    private void job3() throws Exception {
        String path;
        JobId id;

        /*** job 3 : branching */
        path = new File(TestWorkflowRecoveryInterrupted.class.getResource(job_prefix + "3.xml").toURI())
          .getAbsolutePath();
        id = schedulerHelper.submitJob(path);
        log("Submitted job " + path);
        schedulerHelper.waitForEventTaskFinished(id, "A");

        log("Task A finished, crashing scheduler...");
        schedulerHelper.killSchedulerAndNodesAndRestart(new File(SchedulerTHelper.class.getResource(
          "/functionaltests/config/functionalTSchedulerProperties-updateDB.ini").toURI()).getAbsolutePath());

        schedulerHelper.getSchedulerInterface().getJobState(id);

        schedulerHelper.waitForEventJobFinished(id);
        log("Job finished: " + path);

        JobState js = schedulerHelper.getSchedulerInterface().getJobState(id);
        assertEquals(4, js.getTasks().size());

        for (int i = 0; i < 4; i++) {
            TaskState ts = js.getTasks().get(i);
            String name = ts.getName();
            TaskStatus status = ts.getStatus();
            switch (name) {
                case "A":
                    assertEquals(TaskStatus.FINISHED, status);
                    break;
                case "B":
                    assertEquals(TaskStatus.FINISHED, status);
                    break;
                case "C":
                    assertEquals(TaskStatus.SKIPPED, status);
                    break;
                case "D":
                    assertEquals(TaskStatus.FINISHED, status);
                    break;
                default:
                    fail();
                    break;
            }
        }

        log("Job " + path + " checked");
    }

    private void job2() throws Throwable {
        String path;
        JobId id;
        Map<String, Long> expectedResults;
        JobResult results;

        /*** job 2 **/
        path = new File(TestWorkflowRecoveryInterrupted.class.getResource(job_prefix + "2.xml").toURI())
          .getAbsolutePath();
        id = schedulerHelper.submitJob(path);
        log("Submitted job " + path);
        schedulerHelper.waitForEventTaskFinished(id, "T1#1");

        log("Task T1#1 finished, crashing scheduler...");
        schedulerHelper.killSchedulerAndNodesAndRestart(new File(SchedulerTHelper.class.getResource(
          "/functionaltests/config/functionalTSchedulerProperties-updateDB.ini").toURI()).getAbsolutePath());

        schedulerHelper.waitForEventJobFinished(id);
        log("Job finished: " + path);

        expectedResults = new HashMap<>();
        for (String aJob_2 : job_2) {
            String[] val = aJob_2.split(" ");
            expectedResults.put(val[0], Long.parseLong(val[1]));
        }
        results = schedulerHelper.getJobResult(id);
        for (Entry<String, TaskResult> result : results.getAllResults().entrySet()) {
            Long expected = expectedResults.get(result.getKey());
            Assert
              .assertNotNull(path + ": Not expecting result for task '" + result.getKey() + "'",
                expected);
            assertTrue(path + ": Result for task '" + result.getKey() + "' is not an Long",
              result.getValue().value() instanceof Long);
            assertEquals(path + ": Invalid result for task '" + result.getKey() + "'", expected,
              result.getValue().value());

        }
        log("Job " + path + " checked");
    }

    private void job1() throws Throwable {
        /*** job 1 **/
        String path = new File(TestWorkflowRecoveryInterrupted.class.getResource(job_prefix + "1.xml")
                .toURI()).getAbsolutePath();
        JobId id = schedulerHelper.submitJob(path);
        log("Submitted job " + path);
        schedulerHelper.waitForEventTaskFinished(id, "T1#1");

        log("Task T1#1 finished, crashing scheduler...");
        schedulerHelper.killSchedulerAndNodesAndRestart(new File(SchedulerTHelper.class.getResource(
                "/functionaltests/config/functionalTSchedulerProperties-updateDB.ini").toURI()).getAbsolutePath());

        schedulerHelper.getSchedulerInterface().getJobState(id);

        schedulerHelper.waitForEventJobFinished(id);
        log("Job finished: " + id);

        Map<String, Long> expectedResults = new HashMap<>();
        for (String aJob_1 : job_1) {
            String[] val = aJob_1.split(" ");
            expectedResults.put(val[0], Long.parseLong(val[1]));
        }
        JobResult results = schedulerHelper.getJobResult(id);
        for (Entry<String, TaskResult> result : results.getAllResults().entrySet()) {
            Long expected = expectedResults.get(result.getKey());
            Assert
                    .assertNotNull(path + ": Not expecting result for task '" + result.getKey() + "'",
                      expected);
            assertTrue(path + ": Result for task '" + result.getKey() + "' is not an Long",
              result.getValue().value() instanceof Long);
            assertEquals(path + ": Invalid result for task '" + result.getKey() + "'", expected,
              result.getValue().value());

        }
        log("Job " + path + " checked");
    }
}
