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
 * jobs are not interrupted: the scheduler is crashed after they have finished
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestWorkflowRecoveryFinished extends FunctionalTest {

    private static final String job_prefix = "/functionaltests/workflow/descriptors/flow_crash_";

    private static final String[][] jobs_1 = {
    // 1: replicate one single task
            { "T 0 ()", "T1 1 (T)", "T1*1 1 (T)", "T2 3 (T1 T1*1)" },
            // 2: loop a simple block
            { "T 0 ()", "T1 1 (T)", "T2 2 (T1)", "T1#1 3 (T2)", "T2#1 4 (T1#1)", "T3 5 (T2#1)" },
            // 3: loop on a simple block / replicate a single task
            { "T 0 ()", "T1 1 (T)", "T2 2 (T1)", "T2*1 2 (T1)", "T3 5 (T2 T2*1)", "T1#1 6 (T3)",
                    "T2#1 7 (T1#1)", "T2#1*1 7 (T1#1)", "T3#1 15 (T2#1 T2#1*1)", "T4 16 (T3#1)" },

    };

    @org.junit.Test
    public void run() throws Throwable {

        Map<Integer, JobId> jobs = new HashMap<Integer, JobId>();
        for (int i = 0; i < jobs_1.length; i++) {
            String job = new File(TestWorkflowRecoveryFinished.class.getResource(
                    job_prefix + (i + 1) + ".xml").toURI()).getAbsolutePath();
            JobId id = SchedulerTHelper.submitJob(job);
            SchedulerTHelper.log("Submitted job " + job);
            jobs.put(i, id);
        }

        for (Entry<Integer, JobId> job : jobs.entrySet()) {
            SchedulerTHelper.waitForEventJobFinished(job.getValue());
            SchedulerTHelper.log("Job finished " + job.getValue());
        }

        SchedulerTHelper.log("Crashing Scheduler...");
        SchedulerTHelper.killAndRestartScheduler(new File(SchedulerTHelper.class.getResource(
                "config/functionalTSchedulerProperties-updateDB.ini").toURI()).getAbsolutePath());
        SchedulerTHelper.getSchedulerInterface();

        for (Entry<Integer, JobId> job : jobs.entrySet()) {
            JobResult results = SchedulerTHelper.getJobResult(job.getValue());
            Map<String, Long> expectedResults = new HashMap<String, Long>();
            for (int j = 0; j < jobs_1[job.getKey()].length; j++) {
                String[] val = jobs_1[job.getKey()][j].split(" ");
                expectedResults.put(val[0], Long.parseLong(val[1]));
            }
            for (Entry<String, TaskResult> result : results.getAllResults().entrySet()) {
                Long expected = expectedResults.get(result.getKey());

                Assert.assertNotNull(job.getValue() + ": Not expecting result for task '" + result.getKey() +
                    "'", expected);
                Assert.assertTrue(job.getValue() + ": Result for task '" + result.getKey() +
                    "' is not an Long", result.getValue().value() instanceof Long);
                Assert.assertEquals(job.getValue() + ": Invalid result for task '" + result.getKey() + "'",
                        expected, (Long) result.getValue().value());

            }
            SchedulerTHelper.log("Job " + job.getValue() + " checked");
        }

    }
}
