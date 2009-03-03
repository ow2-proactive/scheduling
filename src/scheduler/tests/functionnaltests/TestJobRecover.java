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

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;


/**
 * This class tests the failure of the scheduler.
 * Even if it is in pending, running, or finished list, the scheduled jobs must be restarted
 * as expected after the scheduler restart.
 * This test case is about the behavior of the scheduler after a failure.
 *
 * This test will first commit 3 jobs.
 * When the each one will be in each list (pending, running, finished), the scheduler will
 * be interrupt abnormally.
 * After restart, It will check that every data, tags, status are those expected.
 * It will finally check if the scheduling process will terminate.
 *
 * @author The ProActive Team
 * @date 2 jun 08
 * @since ProActive 4.0
 */
public class TestJobRecover extends FunctionalTDefaultScheduler {

    private static String jobDescriptor = TestJobRecover.class.getResource(
            "/functionnaltests/descriptors/Job_PI_recover.xml").getPath();

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
        log("Submitting job...");
        //job creation
        Job submittedJob = JobFactory.getFactory().createJob(jobDescriptor);
        //job submission
        JobId idJ1 = schedUserInterface.submit(submittedJob);
        JobId idJ2 = schedUserInterface.submit(submittedJob);
        JobId idJ3 = schedUserInterface.submit(submittedJob);
        log("Waiting for job to be placed...");
        //waiting until each job is placed in each list
        int ready = 0;
        while (true) {
            receiver.waitForNEvent(1);
            int jse = receiver.cleanNgetJobSubmittedEvents().size();
            int jptre = receiver.cleanNgetJobPendingToRunningEvents().size();
            int jrtfe = receiver.cleanNgetjobRunningToFinishedEvents().size();
            if (jse > 0) {
                ready += jse;
            }
            if (jptre > 0) {
                ready += jptre * 10;
            }
            if (jrtfe > 0) {
                ready += jrtfe * 100;
            }
            if (ready == 123) {
                log("Interrupting scheduling process...");
                //interrupt the scheduler
                killProActive();
                break;
            }
        }
        Thread.sleep(3000);
        log("Restart Scheduler...");
        //restart it...
        super.restartScheduler(true);
        this.preRun();

        log("Check scheduling process...");
        //...and check that the scheduling process is as expected
        ready = 1 + receiver.cleanNgetjobRunningToFinishedEvents().size();//one job already finished + maybe one more
        while (true) {
            receiver.waitForNEvent(1);
            int jrtf = receiver.cleanNgetjobRunningToFinishedEvents().size();
            if (jrtf > 0) {
                ready += jrtf;
            }
            if (ready == 3) {
                break;
            }
        }
        //check result job 1
        JobResult result = schedUserInterface.getJobResult(idJ1);
        Assert.assertEquals(6, result.getAllResults().size());
        for (int i = 1; i <= 6; i++) {
            Assert.assertNotNull(result.getAllResults().get("Computation" + i).value());
            Assert.assertNull(result.getAllResults().get("Computation" + i).getException());
        }
        //check result job 2
        result = schedUserInterface.getJobResult(idJ2);
        Assert.assertEquals(6, result.getAllResults().size());
        for (int i = 1; i <= 6; i++) {
            Assert.assertNotNull(result.getAllResults().get("Computation" + i).value());
            Assert.assertNull(result.getAllResults().get("Computation" + i).getException());
        }
        //check result job 3
        result = schedUserInterface.getJobResult(idJ3);
        Assert.assertEquals(6, result.getAllResults().size());
        for (int i = 1; i <= 6; i++) {
            Assert.assertNotNull(result.getAllResults().get("Computation" + i).value());
            Assert.assertNull(result.getAllResults().get("Computation" + i).getException());
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
