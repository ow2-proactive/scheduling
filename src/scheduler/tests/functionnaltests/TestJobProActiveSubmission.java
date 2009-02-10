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
import org.ow2.proactive.scheduler.common.job.JobFactory;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerEvent;
import org.ow2.proactive.scheduler.common.task.TaskEvent;
import org.ow2.proactive.scheduler.common.task.TaskResult;


/**
 * This class tests a basic actions of a job submission to ProActive scheduler :
 * Connection to scheduler, with authentication
 * Register a monitor to Scheduler in order to receive events concerning
 * job submission.
 * 
 * Submit a job (test 1). 
 * After the job submission, the test monitor all jobs states changes, in order
 * to observe its execution :
 * job submitted (test 2),
 * job pending to running (test 3),
 * the task pending to running, and task running to finished (test 4),
 * job running to finished (test 5).
 * After it retrieves job's result and check that the 
 * task result is available (test 6).
 * 
 * @author The ProActive Team
 * @date 2 jun 08
 * @since ProActive 4.0
 */
public class TestJobProActiveSubmission extends FunctionalTDefaultScheduler {

    private static String jobDescriptor = TestJobProActiveSubmission.class.getResource(
            "/functionnaltests/descriptors/Job_ProActive.xml").getPath();

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
        ArrayList<JobEvent> eventsList = receiver.cleanNgetJobPendingToRunningEvents();
        assertTrue(eventsList.size() == 1);
        JobEvent jEvent = eventsList.get(0);
        assertTrue(jEvent.getJobId().equals(id));

        log("Test 4 : Verifying start of each tasks...");
        //wait whole tasks execution : two events per task, task pending to running, and task running to finished  
        receiver.waitForNEvent(jEvent.getTotalNumberOfTasks() * 2);
        ArrayList<TaskEvent> tEventList = receiver.cleanNgetTaskPendingToRunningEvents();
        assertTrue(tEventList.size() == jEvent.getTotalNumberOfTasks());
        tEventList = receiver.cleanNgetTaskRunningToFinishedEvents();
        assertTrue(tEventList.size() == jEvent.getTotalNumberOfTasks());

        log("Test 5 : Verifying job termination...");
        //wait for event : job Running to finished
        receiver.waitForNEvent(1);
        eventsList = receiver.cleanNgetjobRunningToFinishedEvents();
        assertTrue(eventsList.size() == 1);
        jEvent = eventsList.get(0);
        assertTrue(jEvent.getJobId().equals(id));

        log("Test 6 : Getting job result...");
        JobResult res = schedUserInterface.getJobResult(id);
        schedUserInterface.remove(id);
        //check that there is no exception in results
        assertTrue(res.getExceptionResults().size() == 0);
        //wait for event : result retrieval
        receiver.waitForNEvent(1);
        eventsList = receiver.cleanNgetjobRemoveFinishedEvents();
        assertTrue(eventsList.size() == 1);
        jEvent = eventsList.get(0);
        assertTrue(jEvent.getJobId().equals(id));
        Map<String, TaskResult> results = res.getAllResults();
        //check that number of results correspond to number of tasks       
        assertTrue(jEvent.getNumberOfFinishedTasks() == results.size());
        //check that all tasks results are defined
        for (TaskResult taskRes : results.values()) {
            assertTrue(taskRes.value() != null);
        }
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
