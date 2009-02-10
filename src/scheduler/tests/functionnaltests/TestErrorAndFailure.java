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

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerEvent;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;


/**
 * This class tests a basic actions of a job submission to ProActive scheduler :
 * Connection to scheduler, with authentication
 * Register a monitor to Scheduler in order to receive events concerning
 * job submission.
 * 
 * This test will try many kind of possible errors.
 * The goal for this test is to terminate. If the Test timeout is reached, it is considered as failed.
 * Possible problems may come from many error count. If this job finish in a
 * reasonable time, it is considered that it passed the test.
 * Every events coming from the scheduler are also checked.
 * 
 * @author The ProActive Team
 * @date 2 jun 08
 * @since ProActive 4.0
 */
public class TestErrorAndFailure extends FunctionalTDefaultScheduler {

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
                SchedulerEvent.TASK_RUNNING_TO_FINISHED);
    }

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {
        log("Test 1 : Creating job...");
        //creating job
        TaskFlowJob submittedJob = new TaskFlowJob();
        submittedJob.setName("Test 12 tasks");
        submittedJob
                .setDescription("12 tasks job testing the behavior of error code and normal task ending.");
        submittedJob.setMaxNumberOfExecution(10);
        NativeTask finalTask = new NativeTask();
        finalTask.setName("TestMerge");
        finalTask.setCommandLine(new String[] { "java", "-cp", "classes/scheduler/",
                "org.ow2.proactive.scheduler.examples.NativeTestWithRandomDefault", "final" });
        for (int i = 1; i < 12; i++) {
            NativeTask task = new NativeTask();
            task.setName("Test" + i);
            task.setCommandLine(new String[] { "java", "-cp", "classes/scheduler/",
                    "org.ow2.proactive.scheduler.examples.NativeTestWithRandomDefault", "0" });
            finalTask.addDependence(task);
            submittedJob.addTask(task);
        }
        submittedJob.addTask(finalTask);

        log("Test 2 : Submitting job...");
        //job submission
        JobId id = schedUserInterface.submit(submittedJob);

        log("Test 3 : Verifying submission...");
        // wait for event : job submitted
        receiver.waitForNEvent(1);
        ArrayList<Job> jobsList = receiver.cleanNgetJobSubmittedEvents();
        assertTrue(jobsList.size() == 1);
        Job job = jobsList.get(0);
        assertTrue(job.getId().equals(id));

        log("Test 4 : Verifying start of job execution...");
        //wait for event : job pending to running
        receiver.waitForNEvent(1);
        ArrayList<JobEvent> eventsList = receiver.cleanNgetJobPendingToRunningEvents();
        assertTrue(eventsList.size() == 1);
        JobEvent jEvent = eventsList.get(0);
        assertTrue(jEvent.getJobId().equals(id));

        log("Test 5 : Verifying job termination...");
        //wait for event : job Running to finished
        while (true) {
            receiver.waitForNEvent(1);
            eventsList = receiver.cleanNgetjobRunningToFinishedEvents();
            if (eventsList.size() == 1) {
                break;
            }
        }
        jEvent = eventsList.get(0);
        assertTrue(jEvent.getJobId().equals(id));
        assertTrue(receiver.cleanNgetTaskRunningToFinishedEvents().size() == 12);

        log("Test 6 : Getting job result...");
        JobResult res = schedUserInterface.getJobResult(id);
        schedUserInterface.remove(id);
        //Check the results
        Map<String, TaskResult> results = res.getAllResults();
        //check that number of results correspond to number of tasks
        assertTrue(jEvent.getNumberOfFinishedTasks() == results.size());
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
