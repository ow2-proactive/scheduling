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
import org.ow2.proactive.scheduler.common.JobDescriptor;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.TaskDescriptor;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.common.task.flow.FlowBlock;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptorImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;


public class SchedulingServiceTest7 extends BaseServiceTest {

    private TaskFlowJob createTestJob() throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        JavaTask mainTask = new JavaTask();
        mainTask.setName("Main task");
        mainTask.setExecutableClassName("ReplicateMainTask");
        mainTask.setFlowBlock(FlowBlock.START);
        String replicateScript = String.format("runs = %d", 3);
        mainTask.setFlowScript(FlowScript.createReplicateFlowScript(replicateScript));
        job.addTask(mainTask);

        JavaTask replicatedTask = new JavaTask();
        replicatedTask.setExecutableClassName("ReplicatedTask");
        replicatedTask.setName("Replicated task");
        replicatedTask.addDependence(mainTask);
        replicatedTask.addArgument("taskParameter", "test");
        job.addTask(replicatedTask);

        JavaTask lastTask = new JavaTask();
        lastTask.setExecutableClassName("LastTask");
        lastTask.setName("Replication last task");
        lastTask.setFlowBlock(FlowBlock.END);
        lastTask.addDependence(replicatedTask);

        job.addTask(lastTask);

        return job;
    }

    @Test
    public void testTaskReplication() throws Exception {
        service.submitJob(createJob(createTestJob()));
        listener.assertEvents(SchedulerEvent.JOB_SUBMITTED);

        Map<JobId, JobDescriptor> jobsMap;
        JobDescriptor jobDesc;

        jobsMap = service.lockJobsToSchedule();
        assertEquals(1, jobsMap.size());
        jobDesc = jobsMap.values().iterator().next();
        Assert.assertEquals(1, jobDesc.getEligibleTasks().size());
        for (TaskDescriptor taskDesc : jobDesc.getEligibleTasks()) {
            taskStarted(jobDesc, (EligibleTaskDescriptor) taskDesc);
        }
        service.unlockJobsToSchedule(jobsMap.values());

        listener.assertEvents(SchedulerEvent.JOB_PENDING_TO_RUNNING,
                              SchedulerEvent.JOB_UPDATED,
                              SchedulerEvent.TASK_PENDING_TO_RUNNING);

        TaskId taskId;

        taskId = ((JobDescriptorImpl) jobDesc).getInternal().getTask("Main task").getId();
        TaskResultImpl result = new TaskResultImpl(taskId, "OK", null, 0);
        FlowAction action = new FlowAction(FlowActionType.REPLICATE);
        action.setDupNumber(3);
        result.setAction(action);
        service.taskTerminatedWithResult(taskId, result);

        listener.assertEvents(SchedulerEvent.TASK_REPLICATED,
                              SchedulerEvent.TASK_SKIPPED,
                              SchedulerEvent.JOB_UPDATED,
                              SchedulerEvent.TASK_RUNNING_TO_FINISHED);

        jobsMap = service.lockJobsToSchedule();
        assertEquals(1, jobsMap.size());
        jobDesc = jobsMap.values().iterator().next();
        Assert.assertEquals(3, jobDesc.getEligibleTasks().size());
        for (TaskDescriptor taskDesc : jobDesc.getEligibleTasks()) {
            taskStarted(jobDesc, (EligibleTaskDescriptor) taskDesc);
        }
        service.unlockJobsToSchedule(jobsMap.values());

        listener.assertEvents(SchedulerEvent.TASK_PENDING_TO_RUNNING,
                              SchedulerEvent.TASK_PENDING_TO_RUNNING,
                              SchedulerEvent.TASK_PENDING_TO_RUNNING);
    }

}
