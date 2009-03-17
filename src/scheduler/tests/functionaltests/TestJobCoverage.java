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
 *  Contributor(s):
 *
 * ################################################################
 * $PROACTIVE_INITIAL_DEV$
 */
package functionaltests;

import junit.framework.Assert;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;

import functionalTests.FunctionalTest;


/**
 * TestJobCoverage check every states, changes, intermediate states of every tasks.
 *
 * <p>
 * task1 : working at first time, RESULT is placed in the precious list at the end.
 * </p>
 * <p>
 * task2 : working at 3rd time, max number of execution is 2, so EXCEPTION is expected at the end.
 * </p>
 * <p>
 * task3 : working at 3rd time, max number of execution is 4, so RESULT is expected at the end.
 * </p>
 * <p>
 * task4 : Throw an exception, expected number of execution is 1, EXCEPTION is expected at the end.
 * </p>
 * <p>
 * task5 : Throw an exception, expected number of execution is 3, EXCEPTION is expected at the end.
 * 			This task must restart on a different host each time.
 * </p>
 * <p>
 * task6 : Native task that end normally, RESULT (CODE 0) is expected at the end.
 * </p>
 * <p>
 * task7 : Native task that end with error code 12, expected number of execution is 1, RESULT (CODE 12) is expected at the end.
 * </p>
 * <p>
 * task8 : Native task that end with error code 12, expected number of execution is 2, RESULT (CODE 12) is expected at the end.
 * </p>
 * <p>
 * task9 : Started after every other tasks, Kill the runtime, expected number of execution is 2 (set by admin)
 * 			EXCEPTION is expected at the end.
 * </p>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class TestJobCoverage extends FunctionalTest {

    private static String jobDescriptor = TestJobAborted.class.getResource(
            "/functionaltests/descriptors/Job_Coverage.xml").getPath();

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {
        //job submission
        //    	SchedulerTHelper.log("Submitting job...");
        //        JobId id = SchedulerTHelper.submitJob(jobDescriptor);
        //
        //        //waiting for job termination
        //        SchedulerTHelper.log("Waiting for job to finish...");
        //        SchedulerTHelper.waitForEventJobFinished(id);
        //
        //        //checking results
        //        SchedulerTHelper.log("Checking results...");
        //        JobResult result = SchedulerTHelper.getJobResult(id);
        //        Assert.assertEquals("Working", result.getPreciousResults().get("task1").value());
        //        Assert.assertTrue(result.getResult("task2").getException().getMessage().contains("WorkingAt3rd - Status : Number is"));
        //        Assert.assertTrue(result.getResult("task3").value().toString().contains("WorkingAt3rd - Status : OK / File deleted :"));
        //        Assert.assertEquals("Throwing",result.getResult("task4").getException().getMessage());
        //        Assert.assertEquals("Throwing",result.getResult("task5").getException().getMessage());
        //        Assert.assertEquals(0,result.getResult("task6").value());
        //        Assert.assertEquals(12,result.getResult("task7").value());
        //        Assert.assertEquals(12,result.getResult("task8").value());
        //        Assert.assertEquals(result.getResult("task9").getException().getClass(),Exception.class);
        //
        //        //checking all processes
        //        SchedulerTHelper.log("Checking all received events...");
        //        SchedulerTHelper.log("Checking task1 process...");

    }

}
