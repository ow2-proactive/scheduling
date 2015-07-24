/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package functionaltests.workflow.nativetask;

import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.junit.Test;

import functionaltests.utils.SchedulerFunctionalTest;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.*;


/**
 * This class tests a basic actions of a job submission to ProActive scheduler :
 * Connection to scheduler, with authentication
 * Register a monitor to Scheduler in order to receive events concerning
 * job submission.
 *
 * Submit a Native job (test 1).
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
 * @since ProActive Scheduling 1.0
 */
public class TestJobNativeSubmission extends SchedulerFunctionalTest {

    @Test
    public void run() throws Throwable {

        //test submission and event reception
        TaskFlowJob job = new TaskFlowJob();

        NativeTask successfulTask = new NativeTask();
        successfulTask.setName("successfulTask");
        if (OperatingSystem.getOperatingSystem() == OperatingSystem.windows) {
            successfulTask.setCommandLine("cmd", "/C", "ping 127.0.0.1 -n 10", ">", "NUL");
        } else {
            successfulTask.setCommandLine("ping", "-c", "5", "127.0.0.1");
        }
        job.addTask(successfulTask);

        NativeTask invalidCommandTask = new NativeTask();
        invalidCommandTask.setName("invalidCommandTask");
        invalidCommandTask.addDependence(successfulTask);
        invalidCommandTask.setCommandLine("invalid_command");

        job.addTask(invalidCommandTask);

        // SCHEDULING-1987
        NativeTask taskReadingInput = new NativeTask();
        taskReadingInput.setName("taskReadingInput");
        if (OperatingSystem.getOperatingSystem() == OperatingSystem.windows) {
            taskReadingInput.setCommandLine("choice"); // wait for y/n
        } else {
            taskReadingInput.setCommandLine("cat"); // cat hangs for user's input
        }

        job.addTask(taskReadingInput);

        JobId id = schedulerHelper.submitJob(job);

        log("Job submitted, id " + id.toString());

        log("Waiting for jobSubmitted Event");
        JobState receivedState = schedulerHelper.waitForEventJobSubmitted(id);

        assertEquals(receivedState.getId(), id);

        log("Waiting for job running");
        JobInfo jInfo = schedulerHelper.waitForEventJobRunning(id);
        assertEquals(jInfo.getJobId(), id);
        assertEquals(JobStatus.RUNNING, jInfo.getStatus());

        schedulerHelper.waitForEventTaskRunning(id, successfulTask.getName());
        TaskInfo tInfo = schedulerHelper.waitForEventTaskFinished(id, successfulTask.getName());

        assertEquals(TaskStatus.FINISHED, tInfo.getStatus());

        schedulerHelper.waitForEventTaskRunning(id, invalidCommandTask.getName());
        tInfo = schedulerHelper.waitForEventTaskFinished(id, invalidCommandTask.getName());

        assertEquals(TaskStatus.FAULTY, tInfo.getStatus());

        TaskInfo taskReadingInputInfo = schedulerHelper.waitForEventTaskFinished(id, taskReadingInput.getName());

        if (OperatingSystem.getOperatingSystem() == OperatingSystem.windows) {
            assertEquals(TaskStatus.FAULTY, taskReadingInputInfo.getStatus()); // choice fails when input is closed
        } else {
            assertEquals(TaskStatus.FINISHED, taskReadingInputInfo.getStatus());
        }

        schedulerHelper.waitForEventJobFinished(id);

        // remove job
        schedulerHelper.removeJob(id);
        schedulerHelper.waitForEventJobRemoved(id);
    }
}
