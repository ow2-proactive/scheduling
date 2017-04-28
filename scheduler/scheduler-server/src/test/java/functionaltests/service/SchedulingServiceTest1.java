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
package functionaltests.service;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.task.TaskResultImpl;


public class SchedulingServiceTest1 extends BaseServiceTest {

    private TaskFlowJob createTestJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName());
        JavaTask task1 = new JavaTask();
        task1.setExecutableClassName("class");
        task1.setName("task1");
        job.addTask(task1);
        return job;
    }

    @Test
    public void testLockJobsToSchedule() throws Exception {
        Map<JobId, JobDescriptor> jobsMap;

        service.submitJob(createJob(createTestJob()));
        jobsMap = service.lockJobsToSchedule();
        assertEquals(1, jobsMap.size());
        runInAnotherThread(new TestRunnable() {
            @Override
            public void run() {
                Assert.assertEquals(0, service.lockJobsToSchedule().size());
            }
        });
        service.unlockJobsToSchedule(jobsMap.values());

        jobsMap = service.lockJobsToSchedule();
        assertEquals(1, jobsMap.size());
        runInAnotherThread(new TestRunnable() {
            @Override
            public void run() throws Exception {
                service.submitJob(createJob(createTestJob()));
                Map<JobId, JobDescriptor> jobsMap = service.lockJobsToSchedule();
                Assert.assertEquals(1, jobsMap.size());
                service.unlockJobsToSchedule(jobsMap.values());
            }
        });
        service.unlockJobsToSchedule(jobsMap.values());

        jobsMap = service.lockJobsToSchedule();
        assertEquals(2, jobsMap.size());
        service.unlockJobsToSchedule(jobsMap.values());

        infrastructure.assertRequests(0);
    }

    @Test
    public void testSimpleJob() throws Exception {
        service.submitJob(createJob(createTestJob()));
        listener.assertEvents(SchedulerEvent.JOB_SUBMITTED);

        Map<JobId, JobDescriptor> jobsMap;
        JobDescriptor jobDesc;

        jobsMap = service.lockJobsToSchedule();

        assertEquals(1, jobsMap.size());
        jobDesc = jobsMap.values().iterator().next();
        Assert.assertEquals(1, jobDesc.getEligibleTasks().size());

        taskStarted(jobDesc, jobDesc.getEligibleTasks().iterator().next());

        service.unlockJobsToSchedule(jobsMap.values());

        jobsMap = service.lockJobsToSchedule();
        assertEquals(1, jobsMap.size());
        jobDesc = jobsMap.values().iterator().next();
        Assert.assertEquals(0, jobDesc.getEligibleTasks().size());
        service.unlockJobsToSchedule(jobsMap.values());

        TaskId taskId = jobDesc.getInternal().getTask("task1").getId();
        service.taskTerminatedWithResult(taskId, new TaskResultImpl(taskId, "Result", null, 0));

        jobsMap = service.lockJobsToSchedule();

        // when a job finishes, it isn't removed from the hibernate context unless
        // the housekeeping mechanism is enabled
        assertEquals(1, jobsMap.size());

        listener.assertEvents(SchedulerEvent.JOB_PENDING_TO_RUNNING,
                              SchedulerEvent.JOB_UPDATED,
                              SchedulerEvent.TASK_PENDING_TO_RUNNING,
                              SchedulerEvent.TASK_RUNNING_TO_FINISHED,
                              SchedulerEvent.JOB_RUNNING_TO_FINISHED,
                              SchedulerEvent.JOB_UPDATED);

        infrastructure.assertRequests(1);
    }

}
