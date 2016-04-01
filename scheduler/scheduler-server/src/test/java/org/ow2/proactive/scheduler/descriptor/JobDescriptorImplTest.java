package org.ow2.proactive.scheduler.descriptor;

import java.util.HashMap;
import java.util.Vector;

import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JobDescriptorImplTest {

    @Test
    public void testThatDoLoopPausesNewlyCreatedTasksIfJobIsPaused() {
        JobDescriptorImpl pausedJob = createEmptyJobDescriptor();
        pausedJob.getInternal().setStatus(JobStatus.PAUSED);

        // Create mocks for the loop root task
        TaskId runningRootTaskId = TaskIdImpl.createTaskId(new JobIdImpl(1L,"Root"),"FirstLoopTask",1L);
        EligibleTaskDescriptorImpl mockLoopStartCurrentlyRunningTask = mock(EligibleTaskDescriptorImpl.class);
        InternalTask mockInternalLoopRootTask = mock(InternalTask.class);
        when(mockInternalLoopRootTask.getId()).thenReturn(runningRootTaskId);
        when(mockLoopStartCurrentlyRunningTask.getTaskId()).thenReturn(runningRootTaskId);
        when(mockLoopStartCurrentlyRunningTask.getInternal()).thenReturn(mockInternalLoopRootTask);
        when(mockLoopStartCurrentlyRunningTask.getChildren()).thenReturn(new Vector<TaskDescriptor>());

        // Create mocks for the new loop task
        TaskId newLoopTaskId = TaskIdImpl.createTaskId(new JobIdImpl(1L,"Root"),"SecondLoopTask",2L);
        EligibleTaskDescriptorImpl mockLoopNewCreatedTaskForLoop = mock(EligibleTaskDescriptorImpl.class);
        InternalTask mockInternalNewLoopTask = mock(InternalTask.class);
        TaskInfo mockNewTaskTaskInfo = mock(TaskInfo.class);
        when (mockNewTaskTaskInfo.getTaskId()).thenReturn(newLoopTaskId);
        when(mockInternalNewLoopTask.getId()).thenReturn(newLoopTaskId);
        when(mockInternalNewLoopTask.getTaskInfo()).thenReturn(mockNewTaskTaskInfo);
        when(mockLoopNewCreatedTaskForLoop.getTaskId()).thenReturn(newLoopTaskId);
        when(mockLoopNewCreatedTaskForLoop.getInternal()).thenReturn(mockInternalNewLoopTask);

        // Put the root loop task into running tasks, because it just terminated
        pausedJob.getRunningTasks().put(mockLoopStartCurrentlyRunningTask.getTaskId(),
                mockLoopStartCurrentlyRunningTask);

        // Put the new loop task into the Map, this is clue so that the test works
        HashMap<TaskId, InternalTask> workflowTree = new HashMap<>();
        workflowTree.put(newLoopTaskId, mockInternalNewLoopTask);

        pausedJob.doLoop(mockLoopStartCurrentlyRunningTask.getTaskId(), workflowTree,
                mockLoopNewCreatedTaskForLoop.getInternal(), mockLoopNewCreatedTaskForLoop.getInternal());

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