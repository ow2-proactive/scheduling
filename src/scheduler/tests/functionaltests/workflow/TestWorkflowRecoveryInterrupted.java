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
package functionaltests.workflow;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskResult;

import org.ow2.tests.FunctionalTest;
import functionaltests.SchedulerTHelper;


/**
 * Tests the recovery after scheduler crash of workflow-enabled jobs
 * Crashes the scheduler while job are being scheduled
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestWorkflowRecoveryInterrupted extends FunctionalTest {

    private static final String job_prefix = "/functionaltests/workflow/descriptors/flow_crash_int_";

    private static final String[] job_1 = { "T 0", "T1 1", "T5 2", "T6 2", "T2 2", "T3 7", "T1#1 8",
            "T5#1 9", "T6#1 9", "T2#1 9", "T3#1 28", "T1#2 29", "T5#2 30", "T6#2 30", "T2#2 30", "T3#2 91",
            "T4 92" };

    private static final String[] job_2 = { "T 0", "T1 1", "T2 2", "T1*1 1", "T2*1 2", "T1*2 1", "T2*2 2",
            "T3 7", "T#1 8", "T1#1 9", "T2#1 10", "T1#1*1 9", "T2#1*1 10", "T1#1*2 9", "T2#1*2 10",
            "T3#1 31", "T#2 32", "T1#2 33", "T2#2 34", "T1#2*1 33", "T2#2*1 34", "T1#2*2 33", "T2#2*2 34",
            "T3#2 103" };

    @org.junit.Test
    public void run() throws Throwable {

        /*** job 1 **/
        String path = new File(TestWorkflowRecoveryInterrupted.class.getResource(job_prefix + "1.xml")
                .toURI()).getAbsolutePath();
        JobId id = SchedulerTHelper.submitJob(path);
        SchedulerTHelper.log("Submitted job " + path);
        SchedulerTHelper.waitForEventTaskFinished(id, "T1#1");

        SchedulerTHelper.log("Task T1#1 finished, crashing scheduler...");
        SchedulerTHelper.killAndRestartScheduler(new File(SchedulerTHelper.class.getResource(
                "config/functionalTSchedulerProperties-updateDB.ini").toURI()).getAbsolutePath());
        SchedulerTHelper.getSchedulerInterface();

        SchedulerTHelper.getSchedulerInterface().getJobState(id);

        SchedulerTHelper.waitForEventJobFinished(id);
        SchedulerTHelper.log("Job finished: " + path);

        Map<String, Long> expectedResults = new HashMap<String, Long>();
        for (int j = 0; j < job_1.length; j++) {
            String[] val = job_1[j].split(" ");
            expectedResults.put(val[0], Long.parseLong(val[1]));
        }
        JobResult results = SchedulerTHelper.getJobResult(id);
        for (Entry<String, TaskResult> result : results.getAllResults().entrySet()) {
            Long expected = expectedResults.get(result.getKey());
            Assert
                    .assertNotNull(path + ": Not expecting result for task '" + result.getKey() + "'",
                            expected);
            Assert.assertTrue(path + ": Result for task '" + result.getKey() + "' is not an Long", result
                    .getValue().value() instanceof Long);
            Assert.assertEquals(path + ": Invalid result for task '" + result.getKey() + "'", expected,
                    (Long) result.getValue().value());

        }
        SchedulerTHelper.log("Job " + path + " checked");

        /*** job 2 **/
        path = new File(TestWorkflowRecoveryInterrupted.class.getResource(job_prefix + "2.xml").toURI())
                .getAbsolutePath();
        id = SchedulerTHelper.submitJob(path);
        SchedulerTHelper.log("Submitted job " + path);
        SchedulerTHelper.waitForEventTaskFinished(id, "T1#1");

        SchedulerTHelper.log("Task T1#1 finished, crashing scheduler...");
        SchedulerTHelper.killAndRestartScheduler(new File(SchedulerTHelper.class.getResource(
                "config/functionalTSchedulerProperties-updateDB.ini").toURI()).getAbsolutePath());
        SchedulerTHelper.getSchedulerInterface();

        SchedulerTHelper.getSchedulerInterface().getJobState(id);

        SchedulerTHelper.waitForEventJobFinished(id);
        SchedulerTHelper.log("Job finished: " + path);

        expectedResults = new HashMap<String, Long>();
        for (int j = 0; j < job_2.length; j++) {
            String[] val = job_2[j].split(" ");
            expectedResults.put(val[0], Long.parseLong(val[1]));
        }
        results = SchedulerTHelper.getJobResult(id);
        for (Entry<String, TaskResult> result : results.getAllResults().entrySet()) {
            Long expected = expectedResults.get(result.getKey());
            Assert
                    .assertNotNull(path + ": Not expecting result for task '" + result.getKey() + "'",
                            expected);
            Assert.assertTrue(path + ": Result for task '" + result.getKey() + "' is not an Long", result
                    .getValue().value() instanceof Long);
            Assert.assertEquals(path + ": Invalid result for task '" + result.getKey() + "'", expected,
                    (Long) result.getValue().value());

        }
        SchedulerTHelper.log("Job " + path + " checked");

    }
}
