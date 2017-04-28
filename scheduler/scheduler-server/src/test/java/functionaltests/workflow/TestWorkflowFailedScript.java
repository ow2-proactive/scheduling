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
package functionaltests.workflow;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;


/**
 * Test that Default FlowAction are provided to the Scheduler in the event of a failed FlowScript execution :
 * if a FlowScript throws an exception and therefore does not create a FlowAction, a default FlowAction should be
 * created so that the flow continue. If cancelJobOnError==false and an IF action is performed, the Workflow
 * is expected to continue.
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestWorkflowFailedScript extends TRepJobs {

    private static final String ifScriptContent = "throw new java.lang.Exception(\"test exception\");";

    @Test
    public void testWorkflowFailedScript() throws Throwable {
        testIf();
    }

    /**
     * Creates  a job with 3 tasks : A B C
     * Workflow : A  -> if (B) else (C)
     * Flowscript on A throws an Exception
     * B is supposed to be executed as cancelJobOnError==false
     * C is supposed to be skipped as it is the default ELSE branch
     * A is supposed to provide a task result value containing an exception 
     */
    private void testIf() throws Throwable {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());
        job.setMaxNumberOfExecution(1);
        job.setOnTaskError(OnTaskError.CONTINUE_JOB_EXECUTION);

        JavaTask A = new JavaTask();
        A.setOnTaskError(OnTaskError.NONE);
        A.setMaxNumberOfExecution(1);
        A.setName("A");
        A.setExecutableClassName("org.ow2.proactive.scheduler.examples.EmptyTask");
        FlowScript ifScript = FlowScript.createIfFlowScript(ifScriptContent, "B", "C", null);
        A.setFlowScript(ifScript);
        job.addTask(A);

        JavaTask B = new JavaTask();
        B.setName("B");
        B.setExecutableClassName("org.ow2.proactive.scheduler.examples.EmptyTask");
        job.addTask(B);

        JavaTask C = new JavaTask();
        C.setName("C");
        C.setExecutableClassName("org.ow2.proactive.scheduler.examples.EmptyTask");
        job.addTask(C);

        JobId id = schedulerHelper.submitJob(job);
        schedulerHelper.waitForEventJobFinished(id);
        JobResult res = schedulerHelper.getJobResult(id);

        Map<String, TaskResult> results = res.getAllResults();

        // tasks A and B should produce a result, C should be SKIPPED
        Assert.assertTrue("Expected 2 results, got " + results.size(), results.size() == 2);

        for (Entry<String, TaskResult> result : results.entrySet()) {
            if (result.getKey().equals("A")) {
                // A should produce an exception
                Assert.assertTrue("Task " + result.getKey() + " should have had an exception!",
                                  result.getValue().hadException());
            } else {
                // task should be B and not C
                Assert.assertTrue("Expected result for task B, got task " + result.getKey(),
                                  result.getKey().equals("B"));

                // Task B should run without exception
                Assert.assertFalse("Task " + result.getKey() + " had an exception!", result.getValue().hadException());
            }
        }

    }

}
