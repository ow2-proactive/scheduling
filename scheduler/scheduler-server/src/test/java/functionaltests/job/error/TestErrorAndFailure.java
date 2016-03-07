/*
 * ################################################################
 *
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.job.error;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import functionaltests.utils.TestScheduler;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.examples.NativeTestWithRandomDefault;

import java.util.Map;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;


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
 * @since ProActive Scheduling 1.0
 */
public class TestErrorAndFailure extends SchedulerFunctionalTestNoRestart {

    @Test
    public void testErrorAndFailure() throws Throwable {

        String javaCmd = System.getProperty("java.home") + "/bin/java";
        log("Test 1 : Creating job...");
        //creating job
        TaskFlowJob submittedJob = new TaskFlowJob();
        submittedJob.setName(this.getClass().getSimpleName() + "_12_tasks");
        submittedJob
                .setDescription("12 tasks job testing the behavior of error code and normal task ending.");
        submittedJob.setMaxNumberOfExecution(10);
        NativeTask finalTask = new NativeTask();
        finalTask.setName("TestMerge");
        finalTask.setCommandLine(new String[] { javaCmd, "-cp", TestScheduler.testClasspath(),
          NativeTestWithRandomDefault.class.getName(), "final" });
        for (int i = 1; i < 6; i++) {
            NativeTask task = new NativeTask();
            task.setName("Test" + i);
            task.setCommandLine(new String[] { javaCmd, "-cp", TestScheduler.testClasspath(),
              NativeTestWithRandomDefault.class.getName(), "0" });
            finalTask.addDependence(task);
            submittedJob.addTask(task);
        }

        submittedJob.addTask(finalTask);

        //test submission and event reception
        JobId id = schedulerHelper.submitJob(submittedJob);
        log("Job submitted, id " + id.toString());
        log("Waiting for jobSubmitted Event");
        Job receivedJob = schedulerHelper.waitForEventJobSubmitted(id);
        assertEquals(receivedJob.getId(), id);
        log("Waiting for job running");
        JobInfo jInfo = schedulerHelper.waitForEventJobRunning(id);

        assertEquals(jInfo.getJobId(), id);

        //task running event may occurs several time for this test
        //TODO how to check that ?
        for (Task t : submittedJob.getTasks()) {
            log("Waiting for task running : " + t.getName());
            schedulerHelper.waitForEventTaskRunning(id, t.getName());
        }

        for (Task t : submittedJob.getTasks()) {
            log("Waiting for task finished : " + t.getName());
            schedulerHelper.waitForEventTaskFinished(id, t.getName());

        }

        log("Waiting for job finished");
        jInfo = schedulerHelper.waitForEventJobFinished(id);

        assertEquals(JobStatus.FINISHED, jInfo.getStatus());

        //check job results
        JobResult res = schedulerHelper.getJobResult(id);
        //Check the results
        Map<String, TaskResult> results = res.getAllResults();
        //check that number of results correspond to number of tasks
        assertEquals(submittedJob.getTasks().size(), results.size());

        //remove jobs and check its event
        schedulerHelper.removeJob(id);
        log("Waiting for job removed");
        jInfo = schedulerHelper.waitForEventJobRemoved(id);
        assertEquals(JobStatus.FINISHED, jInfo.getStatus());
        assertEquals(jInfo.getJobId(), id);

    }
}
