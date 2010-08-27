/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 *              Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;

import functionalTests.FunctionalTest;
import functionaltests.SchedulerTHelper;


/**
 * Tests the correctness of workflow-controlled jobs
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public abstract class TWorkflowJobs extends FunctionalTest {

    protected final String jobSuffix = ".xml";

    /** 
     * Each array is a job named $(array index).xml :
     * {{ job1 }, { job2 } ... { jobn }}
     * Each cell in the matrix is a task and its integer result :
     * {{"Task1_name Task1_result", "Task2_name Task2_result", .... "Taskn_name Taskn_result"}}
     * 
     * Each task and result must match exactly once for each job,
     * except when using {@link FlowActionType#IF}: there will be no
     * result for {@link TaskStatus#SKIPPED} tasks, they must be present in the
     * array but with a negative (ie -1) result value
     * 
     * @return the task names / result matrix
     */
    public abstract String[][] getJobs();

    /**
     * @return the search path on the filesystem for job descriptors
     */
    public abstract String getJobPrefix();

    /**
     * For each job described in {@link #jobs}, submit the job,
     * wait for finished state, and compare expected result for each task with the actual result
     * 
     * @throws Throwable
     */
    protected void internalRun() throws Throwable {
        String[][] jobs = getJobs();

        for (int i = 0; i < jobs.length; i++) {
            Map<String, Long> tasks = new HashMap<String, Long>();
            for (int j = 0; j < jobs[i].length; j++) {
                String[] val = jobs[i][j].split(" ");
                try {
                    tasks.put(val[0], Long.parseLong(val[1]));
                } catch (Throwable t) {
                    System.out.println(jobs[i][j]);
                    t.printStackTrace();
                }
            }
            String path = TWorkflowJobs.class.getResource(getJobPrefix() + (i + 1) + jobSuffix).getPath();
            SchedulerTHelper.log("Testing job: " + path);
            testJob(path, tasks);
        }
    }

    /**
     * See @{link {@link SchedulerTHelper#testJobSubmission(Job)}; does about the same,
     * but skips the part that expects the initial submitted task set to be identical to 
     * the finished task set.
     * 
     * @param jobToSubmit
     * @param skip tasks that will be skipped due to {@link FlowActionType#IF}, do not wait for their completion
     * @return
     * @throws Exception
     */
    public static JobId testJobSubmission(Job jobToSubmit, List<String> skip) throws Exception {
        Scheduler userInt = SchedulerTHelper.getSchedulerInterface();

        JobId id = userInt.submit(jobToSubmit);

        SchedulerTHelper.log("Job submitted, id " + id.toString());

        SchedulerTHelper.log("Waiting for jobSubmitted");
        JobState receivedstate = SchedulerTHelper.waitForEventJobSubmitted(id);
        Assert.assertEquals(id, receivedstate.getId());

        SchedulerTHelper.log("Waiting for job running");
        JobInfo jInfo = SchedulerTHelper.waitForEventJobRunning(id);

        Assert.assertEquals(jInfo.getJobId(), id);
        Assert.assertEquals(JobStatus.RUNNING, jInfo.getStatus());

        if (jobToSubmit instanceof TaskFlowJob) {
            for (Task t : ((TaskFlowJob) jobToSubmit).getTasks()) {
                if (skip != null && skip.contains(t.getName())) {
                    continue;
                }
                TaskInfo ti = SchedulerTHelper.waitForEventTaskRunning(id, t.getName());
                Assert.assertEquals(t.getName(), ti.getTaskId().getReadableName());
                Assert.assertEquals(TaskStatus.RUNNING, ti.getStatus());
            }
            for (Task t : ((TaskFlowJob) jobToSubmit).getTasks()) {
                if (skip != null && skip.contains(t.getName())) {
                    continue;
                }
                TaskInfo ti = SchedulerTHelper.waitForEventTaskFinished(id, t.getName());
                Assert.assertEquals(t.getName(), ti.getTaskId().getReadableName());
                Assert.assertTrue(TaskStatus.FINISHED.equals(ti.getStatus()));
            }
        }

        SchedulerTHelper.log("Waiting for job finished");
        jInfo = SchedulerTHelper.waitForEventJobFinished(id);
        Assert.assertEquals(JobStatus.FINISHED, jInfo.getStatus());

        SchedulerTHelper.log("Job finished");
        return id;
    }

    /**
     * Submits the job located at <code>jobPath</code>, compares its results with the ones provided
     * in <code>expectedResults</code>
     * 
     * Each task for the provided jobs is a org.ow2.proactive.scheduler.examples.IncrementJob
     * which adds all the parameters result + 1.
     * Testing each task's result enforces that all expected tasks are present, with the right dependency graph,
     * thus the correctness of the job.
     * 
     * @param jobPath
     * @param expectedResults
     * @throws Throwable
     */
    public static void testJob(String jobPath, Map<String, Long> expectedResults) throws Throwable {
        List<String> skip = new ArrayList<String>();
        for (Entry<String, Long> er : expectedResults.entrySet()) {
            if (er.getValue() < 0) {
                skip.add(er.getKey());
            }
        }
        JobId id = testJobSubmission(jobPath, skip);

        JobResult res = SchedulerTHelper.getJobResult(id);
        Assert.assertFalse(SchedulerTHelper.getJobResult(id).hadException());

        for (Entry<String, TaskResult> result : res.getAllResults().entrySet()) {
            Long expected = expectedResults.get(result.getKey());

            Assert.assertNotNull(jobPath + ": Not expecting result for task '" + result.getKey() + "'",
                    expected);
            Assert.assertTrue("Task " + result.getKey() + " should be skipped, but returned a result",
                    expected >= 0);
            if (!(result.getValue().value() instanceof Long)) {
                System.out.println(result.getValue().value() + " " + result.getValue().value().getClass());
            }
            Assert.assertTrue(jobPath + ": Result for task '" + result.getKey() + "' is not an Long", result
                    .getValue().value() instanceof Long);
            Assert.assertEquals(jobPath + ": Invalid result for task '" + result.getKey() + "'", expected,
                    (Long) result.getValue().value());
        }

        int skipped = 0;
        // tasks SKIPPED are short-circuited by an IF flow action
        // they are still in the tasks list, but do not return a result
        for (Entry<String, Long> expected : expectedResults.entrySet()) {
            if (expected.getValue() < 0) {
                Assert.assertFalse("Task " + expected.getKey() + " should be skipped, but returned a result",
                        res.getAllResults().containsKey(expected.getKey()));
                skipped++;
            }
        }

        Assert.assertEquals("Expected and actual result sets are not identical in " + jobPath + " (skipped " +
            skipped + "): ", expectedResults.size(), res.getAllResults().size() + skipped);

        SchedulerTHelper.removeJob(id);
        SchedulerTHelper.waitForEventJobRemoved(id);
    }

    public static JobId testJobSubmission(String jobDescPath, List<String> skip) throws Exception {
        Job jobToTest = JobFactory.getFactory().createJob(jobDescPath);
        return testJobSubmission(jobToTest, skip);
    }

}
