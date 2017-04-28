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
package org.ow2.proactive.scheduler.descriptor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Vector;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class JobDescriptorImplTest {

    @Test
    public void testThatDoLoopPausesNewlyCreatedTasksIfJobIsPaused() {
        JobDescriptorImpl pausedJob = createEmptyJobDescriptor();
        pausedJob.getInternal().setStatus(JobStatus.PAUSED);

        // Create mocks for the loop root task
        TaskId runningRootTaskId = TaskIdImpl.createTaskId(new JobIdImpl(1L, "Root"), "FirstLoopTask", 1L);
        EligibleTaskDescriptorImpl mockLoopStartCurrentlyRunningTask = mock(EligibleTaskDescriptorImpl.class);
        InternalTask mockInternalLoopRootTask = mock(InternalTask.class);
        when(mockInternalLoopRootTask.getId()).thenReturn(runningRootTaskId);
        when(mockLoopStartCurrentlyRunningTask.getTaskId()).thenReturn(runningRootTaskId);
        when(mockLoopStartCurrentlyRunningTask.getInternal()).thenReturn(mockInternalLoopRootTask);
        when(mockLoopStartCurrentlyRunningTask.getChildren()).thenReturn(new Vector<TaskDescriptor>());

        // Create mocks for the new loop task
        TaskId newLoopTaskId = TaskIdImpl.createTaskId(new JobIdImpl(1L, "Root"), "SecondLoopTask", 2L);
        EligibleTaskDescriptorImpl mockLoopNewCreatedTaskForLoop = mock(EligibleTaskDescriptorImpl.class);
        InternalTask mockInternalNewLoopTask = mock(InternalTask.class);
        TaskInfo mockNewTaskTaskInfo = mock(TaskInfo.class);
        when(mockNewTaskTaskInfo.getTaskId()).thenReturn(newLoopTaskId);
        when(mockInternalNewLoopTask.getId()).thenReturn(newLoopTaskId);
        when(mockInternalNewLoopTask.getStatus()).thenReturn(TaskStatus.SUBMITTED);
        when(mockInternalNewLoopTask.getTaskInfo()).thenReturn(mockNewTaskTaskInfo);
        when(mockLoopNewCreatedTaskForLoop.getTaskId()).thenReturn(newLoopTaskId);
        when(mockLoopNewCreatedTaskForLoop.getInternal()).thenReturn(mockInternalNewLoopTask);

        // Put the root loop task into running tasks, because it just terminated
        pausedJob.getRunningTasks().put(mockLoopStartCurrentlyRunningTask.getTaskId(),
                                        mockLoopStartCurrentlyRunningTask);

        // Put the new loop task into the Map, this is clue so that the test works
        HashMap<TaskId, InternalTask> workflowTree = new HashMap<>();
        workflowTree.put(newLoopTaskId, mockInternalNewLoopTask);

        pausedJob.doLoop(mockLoopStartCurrentlyRunningTask.getTaskId(),
                         workflowTree,
                         mockLoopNewCreatedTaskForLoop.getInternal(),
                         mockLoopNewCreatedTaskForLoop.getInternal());

        verify(mockInternalNewLoopTask).setStatus(TaskStatus.PAUSED);
    }

    private JobDescriptorImpl createEmptyJobDescriptor() {
        return new JobDescriptorImpl(new InternalJob() {
            @Override
            public JobType getType() {
                return null;
            }
        });
    }

}
