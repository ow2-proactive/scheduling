/*
 *  *
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.job.scheduling;

import com.google.common.collect.ImmutableMap;
import functionaltests.utils.RMTHelper;
import functionaltests.utils.SchedulerFunctionalTestNonForkModeWithRestart;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.ParallelEnvironment;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.examples.EmptyTask;
import org.ow2.proactive.scripting.SelectionScript;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Several tests related to job scheduling priorities and starvation
 */
public class TestJobSchedulingStarvationAndPriority extends SchedulerFunctionalTestNonForkModeWithRestart {

    private static URL jobDescriptor = TestJobSchedulingStarvationAndPriority.class
            .getResource("/functionaltests/descriptors/Job_With_Replication.xml");

    private static final String REPLICATE_TASK_NAME = "task2replicate";

    private static final String REPLICATE_TASK_NAME_FILTER = REPLICATE_TASK_NAME + ".*";

    /**
     * Tests that a starvation does not occur with selection scripts. A set of high-priority task with a selection script
     * returning false does not prevent a low priority task to run.
     *
     * @throws Exception
     */
    @Test
    public void testStarvationSelectionScript() throws Exception {
        schedulerHelper.log("testStarvationSelectionScript");
        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        JobId jobIdLow;

        List<JobId> jobIds = new ArrayList<>(RMTHelper.DEFAULT_NODES_NUMBER);

        for (int i = 0; i < RMTHelper.DEFAULT_NODES_NUMBER; i++) {
            jobIds.add(scheduler.submit(createJobHighSelectFalse()));
        }
        jobIdLow = scheduler.submit(createJobLow());

        schedulerHelper.waitForEventTaskRunning(jobIdLow, "taskLow");

        for (int i = 0; i < RMTHelper.DEFAULT_NODES_NUMBER; i++) {
            schedulerHelper.killJob("" + jobIds.get(i));
        }
    }

    /**
     * Tests that a starvation does not occur with multi-node tasks :
     * A high-priority task needing more nodes than available does not prevent a low priority task to execute.
     *
     * @throws Exception
     */
    @Test
    public void testStarvationMultiNode() throws Exception {
        schedulerHelper.log("testStarvationMultiNode");
        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        JobId jobIdLow;
        JobId jobIdHigh;

        jobIdHigh = scheduler.submit(createJobHighMoreMultiNode());

        jobIdLow = scheduler.submit(createJobLow());

        schedulerHelper.waitForEventTaskRunning(jobIdLow, "taskLow");

        schedulerHelper.killJob("" + jobIdHigh);
    }

    /**
     * Tests that all replicated tasks in a low priority job are executed AFTER replicated tasks in a high priority one
     *
     * @throws Exception
     */
    @Test
    public void testJobPriorityStandard() throws Exception {
        schedulerHelper.log("testJobPriorityStandard");
        testJobPriority(false);
    }

    /**
     * Tests that all replicated tasks in a low priority job are executed AFTER replicated tasks in a high priority one
     *
     * Additionally, add extra nodes during the run and ensure that new nodes are not picked by low priority tasks
     *
     * @throws Exception
     */
    @Test
    public void testJobPriorityWithNewNodes() throws Exception {
        schedulerHelper.log("testJobPriorityWithNewNodes");
        testJobPriority(true);
    }


    public void testJobPriority(boolean addNewNodes) throws Exception {

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        int nbNewNodes = 15;

        int nbRunsHigh = RMTHelper.DEFAULT_NODES_NUMBER * 2 + (addNewNodes ? nbNewNodes * 3 : 0);
        int nbRunsLow = RMTHelper.DEFAULT_NODES_NUMBER * 2 + (addNewNodes ? nbNewNodes * 2 : 0);

        String jobDescriptorPath = new File(jobDescriptor.toURI()).getAbsolutePath();

        Job jobHigh = JobFactory.getFactory().createJob(jobDescriptorPath, ImmutableMap.of("RUNS", "" + nbRunsHigh));
        jobHigh.setPriority(JobPriority.HIGH);
        JobId jobIdHigh = schedulerHelper.submitJob(jobHigh);
        schedulerHelper.waitForEventTaskRunning(jobIdHigh, REPLICATE_TASK_NAME);
        Job jobLow = JobFactory.getFactory().createJob(jobDescriptorPath, ImmutableMap.of("RUNS", "" + nbRunsLow));
        jobLow.setPriority(JobPriority.LOW);
        JobId jobIdLow = schedulerHelper.submitJob(jobLow);

        if (addNewNodes) {
            schedulerHelper.addExtraNodes(nbNewNodes);
        }

        schedulerHelper.waitForEventJobFinished(jobIdHigh);
        schedulerHelper.waitForEventJobFinished(jobIdLow);

        Pair<Long, Long> minMaxHigh = computeMinMaxStartingTime(scheduler, jobIdHigh, REPLICATE_TASK_NAME_FILTER);
        Pair<Long, Long> minMaxLow = computeMinMaxStartingTime(scheduler, jobIdLow, REPLICATE_TASK_NAME_FILTER);

        Assert.assertTrue("Low Priority tasks min start time : " + minMaxLow.getLeft() + " should be greater than the max start time of high priority jobs : " + minMaxHigh.getRight(), minMaxLow.getLeft() > minMaxHigh.getRight());
    }

    /**
     * Computes min start time and max start time for all tasks which match a given pattern
     * If a task in the set did not start, then the set max will be Long.MAX_VALUE
     */
    Pair<Long, Long> computeMinMaxStartingTime(Scheduler scheduler, JobId jobId, String taskNameFilter) throws Exception {
        long min = Long.MAX_VALUE;
        long max = -1;
        for (TaskState state : scheduler.getJobState(jobId).getTasks()) {
            if (state.getName().matches(taskNameFilter)) {
                long startTime = state.getStartTime() > -1 ? state.getStartTime() : Long.MAX_VALUE;
                min = Math.min(min, startTime);
                max = Math.max(max, startTime);
            }
        }
        return new ImmutablePair<>(min, max);
    }

    @After
    public void releaseNodes() {
        try {
            schedulerHelper.removeExtraNodeSource();
        } catch (Exception ignored) {
        }
    }

    /*
     * Job high priority with one task, task's selection script always returns 'false' so task can't start
     */
    private TaskFlowJob createJobHighSelectFalse() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_High_SelectFalse");
        job.setPriority(JobPriority.HIGHEST);

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(EmptyTask.class.getName());
        javaTask.setName("taskSelectFalse");
        SelectionScript selScript = new SelectionScript("selected = false;", "js");
        javaTask.setSelectionScript(selScript);
        job.addTask(javaTask);

        return job;
    }

    /*
     * Job high priority with one task and with a required number of nodes greater than currently available
     */
    private TaskFlowJob createJobHighMoreMultiNode() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_High_MoreMultiNode");
        job.setPriority(JobPriority.HIGHEST);

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(EmptyTask.class.getName());
        javaTask.setName("taskMoreMultiNode");
        javaTask.setParallelEnvironment(new ParallelEnvironment(RMTHelper.DEFAULT_NODES_NUMBER + 1));
        job.addTask(javaTask);

        return job;
    }

    /*
     * Job high priority with one task and high priority
     */
    private TaskFlowJob createJobReplicate(int nbRun, JobPriority priority) throws Exception {

        String jobDescriptorPath = new File(jobDescriptor.toURI()).getAbsolutePath();

        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_High");
        job.setPriority(JobPriority.HIGHEST);

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(EmptyTask.class.getName());
        javaTask.setName("taskHigh");
        job.addTask(javaTask);

        return job;
    }

    /*
     * Job high priority with one task and low priority
     */
    private TaskFlowJob createJobLow() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_Low");
        job.setPriority(JobPriority.LOW);

        JavaTask javaTask = new JavaTask();
        javaTask.setExecutableClassName(EmptyTask.class.getName());
        javaTask.setName("taskLow");
        job.addTask(javaTask);

        return job;
    }
}
