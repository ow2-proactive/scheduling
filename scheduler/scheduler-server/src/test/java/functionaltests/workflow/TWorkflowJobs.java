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

import static functionaltests.utils.SchedulerTHelper.log;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.*;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;

import functionaltests.utils.SchedulerFunctionalTestNonForkedModeNoRestart;
import functionaltests.utils.SchedulerTHelper;


/**
 * Tests the correctness of loop / if based workflow-controlled jobs
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public abstract class TWorkflowJobs extends SchedulerFunctionalTestNonForkedModeNoRestart {

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
     * For each job described in {@link #data.jobs}, submit the job,
     * wait for finished state, and compare expected result for each task with the actual result
     * 
     * @throws Throwable
     */
    protected void internalRun() throws Throwable {
        String[][] jobs = getJobs();

        /*
         * Parse job data. The format of an entry is the following (square
         * brackets designate optional content):
         * 
         * <task name> <result>[ ([<dependency1>[ <dependency2>[ ...]]])]
         * 
         * i.e. there should be space-separated task name and expected result
         * and optionally a list of space-separated list of dependencies in
         * parentheses. The order of dependencies is arbitrary. Dependencies
         * will be checked only if the list is present.
         *
         * Examples of valid entries:
         * 
         * "T1 1", "T1 1 ()", "T1 1 ()", "T1 1 (T)", "T2 1 (T0 T1)"
         * 
         */
        for (int i = 0; i < jobs.length; i++) {
            Map<String, Long> tasks = new HashMap<>();
            for (int j = 0; j < jobs[i].length; j++) {
                String[] val = jobs[i][j].split(" ");
                try {
                    tasks.put(val[0], Long.parseLong(val[1]));
                } catch (Throwable t) {
                    System.out.println(jobs[i][j]);
                    t.printStackTrace();
                }
            }

            // parse dependences info, if present
            Map<String, Set<String>> dependences = new HashMap<>();
            for (int j = 0; j < jobs[i].length; j++) {
                String[] val = jobs[i][j].split(" ", 3);
                if (val.length == 3) {
                    try {
                        String deps = val[2].substring(1, val[2].length() - 1);
                        dependences.put(val[0], new HashSet<>(Arrays.asList(deps.split(" "))));
                    } catch (Throwable t) {
                        throw new RuntimeException("Error parsing dependencies for entry: " + jobs[i][j], t);
                    }
                }
            }

            String path = new File(TWorkflowJobs.class.getResource(getJobPrefix() + (i + 1) + jobSuffix)
                                                      .toURI()).getAbsolutePath();
            log("Testing job: " + path);
            testJob(path, tasks, dependences);
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
    public static JobId testJobSubmission(SchedulerTHelper schedulerHelper, Job jobToSubmit, List<String> skip)
            throws Exception {
        JobId id = schedulerHelper.submitJob(jobToSubmit);

        log("Job submitted, id " + id.toString());

        log("Waiting for jobSubmitted");
        JobState receivedstate = schedulerHelper.waitForEventJobSubmitted(id);
        Assert.assertEquals(id, receivedstate.getId());

        log("Waiting for job running");
        JobInfo jInfo = schedulerHelper.waitForEventJobRunning(id);

        Assert.assertEquals(jInfo.getJobId(), id);
        Assert.assertEquals(JobStatus.RUNNING, jInfo.getStatus());

        if (jobToSubmit instanceof TaskFlowJob) {
            for (Task t : ((TaskFlowJob) jobToSubmit).getTasks()) {
                if (skip != null && skip.contains(t.getName())) {
                    continue;
                }
                TaskInfo ti = schedulerHelper.waitForEventTaskRunning(id, t.getName());
                Assert.assertEquals(t.getName(), ti.getTaskId().getReadableName());
                Assert.assertEquals(TaskStatus.RUNNING, ti.getStatus());
            }
            for (Task t : ((TaskFlowJob) jobToSubmit).getTasks()) {
                if (skip != null && skip.contains(t.getName())) {
                    continue;
                }
                TaskInfo ti = schedulerHelper.waitForEventTaskFinished(id, t.getName());
                Assert.assertEquals(t.getName(), ti.getTaskId().getReadableName());
                Assert.assertTrue(TaskStatus.FINISHED.equals(ti.getStatus()));
            }
        }

        log("Waiting for job finished");
        jInfo = schedulerHelper.waitForEventJobFinished(id);
        Assert.assertEquals(JobStatus.FINISHED, jInfo.getStatus());

        log("Job finished");
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
     * @param expectedDependences 
     * @throws Throwable
     */
    public void testJob(String jobPath, Map<String, Long> expectedResults, Map<String, Set<String>> expectedDependences)
            throws Throwable {

        List<String> skip = new ArrayList<>();
        for (Entry<String, Long> er : expectedResults.entrySet()) {
            if (er.getValue() < 0) {
                skip.add(er.getKey());
            }
        }
        Job jobToTest = JobFactory.getFactory().createJob(jobPath);
        JobId id = testJobSubmission(schedulerHelper, jobToTest, skip);

        JobResult res = schedulerHelper.getJobResult(id);
        Assert.assertFalse(schedulerHelper.getJobResult(id).hadException());

        compareResults(jobPath, expectedResults, res);

        JobState js = schedulerHelper.getSchedulerInterface().getJobState(id);
        compareDependences(js, expectedDependences);

        schedulerHelper.removeJob(id);
        schedulerHelper.waitForEventJobRemoved(id);
    }

    public void compareResults(String prefix, Map<String, Long> expectedResults, JobResult jobResult) throws Throwable {
        for (Entry<String, TaskResult> result : jobResult.getAllResults().entrySet()) {
            Long expected = expectedResults.get(result.getKey());

            Assert.assertNotNull(prefix + ": Not expecting result for task '" + result.getKey() + "'", expected);
            Assert.assertTrue("Task " + result.getKey() + " should be skipped, but returned a result", expected >= 0);
            if (!(result.getValue().value() instanceof Long)) {
                System.out.println(result.getValue().value() + " " + result.getValue().value().getClass());
            }
            Assert.assertTrue(prefix + ": Result for task '" + result.getKey() + "' is not an Long",
                              result.getValue().value() instanceof Long);
            Assert.assertEquals(prefix + ": Invalid result for task '" + result.getKey() + "'",
                                expected,
                                (Long) result.getValue().value());
        }

        int skipped = 0;
        // tasks SKIPPED are short-circuited by an IF flow action
        // they are still in the tasks list, but do not return a result
        for (Entry<String, Long> expected : expectedResults.entrySet()) {
            if (expected.getValue() < 0) {
                Assert.assertFalse("Task " + expected.getKey() + " should be skipped, but returned a result",
                                   jobResult.getAllResults().containsKey(expected.getKey()));
                skipped++;
            }
        }

        Assert.assertEquals("Expected and actual result sets are not identical in " + prefix + " (skipped " + skipped +
                            "): ", expectedResults.size(), jobResult.getAllResults().size() + skipped);
    }

    public void compareDependences(JobState js, Map<String, Set<String>> expectedDependences) {
        for (TaskState ts : js.getTasks()) {
            String taskName = ts.getId().getReadableName();
            if (expectedDependences.containsKey(taskName)) {
                // expected dependences for this task
                Set<String> expected = expectedDependences.get(taskName);
                // actual dependences of this task
                List<TaskState> actualDepTasks = ts.getDependences();
                Set<String> actual = new HashSet<>();
                if (actualDepTasks != null && actualDepTasks.size() != 0) {
                    for (TaskState d : actualDepTasks) {
                        actual.add(d.getId().getReadableName());
                    }
                } else {
                    actual.add("");
                }
                // compare expected to actual
                Assert.assertEquals("Dependence mismatch for task " + taskName, expected, actual);
            }
        }
    }
}
