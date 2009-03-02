/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionnaltests;

import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;


/**
 * This class tests the way a task can kill a node.
 * It will start 3 dependent tasks where one will kill the node.
 * The task will be restarted as defined in the Scheduler ini file.
 * The goal is to check that the killing task is restarted the expected number of time, and ensure the task and job will terminate.
 * At the end, the first task must be terminated, the second one failed, and the last one not started.
 * It will also check that it's not relative to the 'cancelJobOnError' value.
 *
 * @author The ProActive Team
 * @date 2 jun 08
 * @since ProActive 4.0
 */
public class TestJobKilled extends FunctionalTDefaultScheduler {

    private static String jobDescriptor = TestJobKilled.class.getResource(
            "/functionnaltests/descriptors/Job_Killed.xml").getPath();

    private SchedulerEventReceiver receiver = null;

    /**
     *  Starting and linking new scheduler ! <br/>
     *  This method will join a new scheduler and connect it as user.<br/>
     *  Then, it will register an event receiver to check the dispatched event.
     */
    @Before
    public void preRun() throws Exception {
        //Create an Event receiver AO in order to observe jobs and tasks states changes
        receiver = (SchedulerEventReceiver) PAActiveObject.newActive(SchedulerEventReceiver.class.getName(),
                new Object[] {});
        //Register as EventListener AO previously created
        schedUserInterface.addSchedulerEventListener(receiver, SchedulerEvent.JOB_SUBMITTED,
                SchedulerEvent.JOB_PENDING_TO_RUNNING, SchedulerEvent.JOB_RUNNING_TO_FINISHED,
                SchedulerEvent.JOB_REMOVE_FINISHED, SchedulerEvent.TASK_PENDING_TO_RUNNING,
                SchedulerEvent.TASK_RUNNING_TO_FINISHED, SchedulerEvent.TASK_WAITING_FOR_RESTART);
    }

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {
        log("Test 1 : Submitting job...");
        //job creation
        Job submittedJob = JobFactory.getFactory().createJob(jobDescriptor);
        //job submission
        JobId id = schedUserInterface.submit(submittedJob);

        log("Test 2 : Verifying submission...");
        // wait for event : job submitted
        receiver.waitForNEvent(1);
        ArrayList<Job> jobsList = receiver.cleanNgetJobSubmittedEvents();
        assertTrue(jobsList.size() == 1);
        Job job = jobsList.get(0);
        assertTrue(job.getId().equals(id));

        log("Test 3 : Verifying start of job execution...");
        //wait for event : job pending to running
        receiver.waitForNEvent(1);
        ArrayList<JobInfo> infosList = receiver.cleanNgetJobPendingToRunningEvents();
        assertTrue(infosList.size() == 1);
        JobInfo jEvent = infosList.get(0);
        assertTrue(jEvent.getJobId().equals(id));

        System.out
                .println("------------------------------ Test 4 : Verifying start and termination of each tasks...");
        //Check first start tasks execution
        receiver.waitForNEvent(1);
        ArrayList<TaskInfo> tEventList = receiver.cleanNgetTaskPendingToRunningEvents();
        assertTrue(tEventList.size() == 1);
        assertTrue(tEventList.get(0).getTaskId().getReadableName().equals("task1"));
        //check that the first task terminate first
        receiver.waitForNEvent(1);
        tEventList = receiver.cleanNgetTaskRunningToFinishedEvents();
        assertTrue(tEventList.size() == 1);
        assertTrue(tEventList.get(0).getTaskId().getReadableName().equals("task1"));
        //wait for the 2nd last tasks to be started
        receiver.waitForNEvent(1);
        tEventList = receiver.cleanNgetTaskPendingToRunningEvents();
        assertTrue(tEventList.size() == 1);
        assertTrue(tEventList.get(0).getTaskId().getReadableName().equals("task2"));
        //check that the task1 is restarted
        receiver.waitForNEvent(1);
        tEventList = receiver.cleanNgetTaskWaitingForRestartEvents();
        assertTrue(tEventList.size() == 1);
        assertTrue(tEventList.get(0).getTaskId().getReadableName().equals("task2"));
        //wait for the 2nd last tasks to be restarted
        receiver.waitForNEvent(1);
        tEventList = receiver.cleanNgetTaskPendingToRunningEvents();
        assertTrue(tEventList.size() == 1);
        assertTrue(tEventList.get(0).getTaskId().getReadableName().equals("task2"));
        //Verifying task2 termination (after 1 retry)
        receiver.waitForNEvent(1);
        tEventList = receiver.cleanNgetTaskRunningToFinishedEvents();
        assertTrue(tEventList.size() == 1);
        assertTrue(tEventList.get(0).getTaskId().getReadableName().equals("task2"));

        log("Test 5 : Verifying job termination...");
        //wait for event : job Running to finished
        receiver.waitForNEvent(1);
        infosList = receiver.cleanNgetjobRunningToFinishedEvents();
        assertTrue(infosList.size() == 1);
        jEvent = infosList.get(0);
        assertTrue(jEvent.getJobId().equals(id));

        log("Test 6 : Getting job result...");
        JobResult res = schedUserInterface.getJobResult(id);
        schedUserInterface.remove(id);
        //check that there is no exception in results
        assertTrue(res.getExceptionResults().size() == 1);
        //wait for event : result retrieval
        receiver.waitForNEvent(1);
        infosList = receiver.cleanNgetjobRemoveFinishedEvents();
        assertTrue(infosList.size() == 1);
        jEvent = infosList.get(0);
        assertTrue(jEvent.getJobId().equals(id));
        Map<String, TaskResult> results = res.getAllResults();
        //check that number of results correspond to number of tasks
        Assert.assertEquals(results.size(), jEvent.getNumberOfFinishedTasks());
        //check that all tasks results are defined
        Assert.assertNotNull(results.get("task1").value());
        Assert.assertNotNull(results.get("task2").getException());
    }

    /**
     * Disconnect the scheduler.
     *
     * @throws Exception if an error occurred
     */
    @After
    public void afterTestJobSubmission() throws Exception {
        log("Disconnecting from scheduler...");
        schedUserInterface.disconnect();
    }

}
