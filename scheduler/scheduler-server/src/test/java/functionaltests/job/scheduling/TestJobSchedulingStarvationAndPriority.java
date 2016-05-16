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

import functionaltests.utils.RMTHelper;
import functionaltests.utils.SchedulerFunctionalTestNonForkModeWithRestart;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.ParallelEnvironment;
import org.ow2.proactive.scheduler.examples.EmptyTask;
import org.ow2.proactive.scripting.SelectionScript;

import java.util.ArrayList;
import java.util.List;

/**
 * Several tests related to job scheduling priorities and starvation
 */
public class TestJobSchedulingStarvationAndPriority extends SchedulerFunctionalTestNonForkModeWithRestart {

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
        JobId jobIdHigh;

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
     * Tests that a low priority job is executed AFTER many jobs with high priorities
     *
     * @throws Exception
     */
    @Test
    public void testJobPriority() throws Exception {

        schedulerHelper.log("testJobPriority");

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        int nbJobs = RMTHelper.DEFAULT_NODES_NUMBER * 2;

        List<JobId> jobIds = new ArrayList<>(nbJobs);

        for (int i = 0; i < nbJobs; i++) {
            JobId jobId = scheduler.submit(createJobHigh());
            jobIds.add(jobId);
        }
        JobId jobIdLow = scheduler.submit(createJobLow());

        schedulerHelper.waitForEventTaskRunning(jobIdLow, "taskLow");

        long maxStartTime = 0;
        for (int i = 0; i < nbJobs; i++) {
            JobState jobState = scheduler.getJobState(jobIds.get(i));
            long startTime = jobState.getStartTime();
            maxStartTime = Math.max(maxStartTime, startTime);
        }

        JobState jobStateLow = scheduler.getJobState(jobIdLow);

        Assert.assertTrue("Low Priority Job Start time : " + jobStateLow.getStartTime() + " should be greater than the max start time of high priority jobs : " + maxStartTime, jobStateLow.getStartTime() > maxStartTime);
    }

    /**
     * Tests that, when new nodes are added in the middle of a scheduling loop, a low priority job is not executed before a high priority one
     *
     * @throws Exception
     */
    @Test
    public void testJobPriorityWithNewNodes() throws Exception {

        schedulerHelper.log("testJobPriorityWithNewNodes");

        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        int nbNewNodes = 20;

        int nbJobsHigh = RMTHelper.DEFAULT_NODES_NUMBER * 2 + nbNewNodes;
        int nbJobsLow = RMTHelper.DEFAULT_NODES_NUMBER * 2 + nbNewNodes;

        List<JobId> highJobIds = new ArrayList<>(nbJobsHigh);

        for (int i = 0; i < nbJobsHigh; i++) {
            JobId jobId = scheduler.submit(createJobHigh());
            highJobIds.add(jobId);
        }

        List<JobId> lowJobIds = new ArrayList<>(nbJobsLow);

        for (int i = 0; i < nbJobsLow; i++) {
            JobId jobId = scheduler.submit(createJobLow());
            lowJobIds.add(jobId);
        }

        schedulerHelper.waitForEventTaskRunning(highJobIds.get(0), "taskHigh");

        schedulerHelper.addExtraNodes(nbNewNodes);

        for (int i = 0; i < nbJobsLow; i++) {
            schedulerHelper.waitForEventJobFinished(highJobIds.get(i));
        }

        for (int i = 0; i < nbJobsLow; i++) {
            schedulerHelper.waitForEventJobFinished(lowJobIds.get(i));
        }

        long maxHighStartTime = 0;
        for (int i = 0; i < nbJobsHigh; i++) {
            JobState jobState = scheduler.getJobState(highJobIds.get(i));
            long startTime = jobState.getStartTime();
            if (startTime == -1) {
                maxHighStartTime = Long.MAX_VALUE;
            } else {
                maxHighStartTime = Math.max(maxHighStartTime, startTime);
            }
        }

        long minLowStartTime = Long.MAX_VALUE;
        for (int i = 0; i < nbJobsLow; i++) {
            JobState jobState = scheduler.getJobState(lowJobIds.get(i));
            long startTime = jobState.getStartTime();
            minLowStartTime = Math.min(minLowStartTime, startTime);
        }

        schedulerHelper.log("Min Low Priority Jobs Start time : " + minLowStartTime + " should be greater than the max start time of high priority jobs : " + maxHighStartTime);

        Assert.assertTrue("Min Low Priority Jobs Start time : " + minLowStartTime + " should be greater than the max start time of high priority jobs : " + maxHighStartTime, minLowStartTime > maxHighStartTime);
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
    private TaskFlowJob createJobHigh() throws Exception {
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
