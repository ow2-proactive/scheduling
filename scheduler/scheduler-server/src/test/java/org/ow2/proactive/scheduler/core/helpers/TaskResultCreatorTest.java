package org.ow2.proactive.scheduler.core.helpers;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.ow2.proactive.db.DatabaseManagerException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptorImpl;
import org.ow2.proactive.scheduler.descriptor.JobDescriptorImpl;
import org.ow2.proactive.scheduler.descriptor.TaskDescriptor;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TaskResultCreatorTest {

    @Test(expected = UnknownTaskException.class)
    public void testThatNullTaskThrowsException() throws UnknownTaskException {
        TaskResultCreator taskResultCreator = new TaskResultCreator();

        taskResultCreator.getTaskResult(null, null, null);
    }

    @Test
    public void testThatNotFoundTaskResultIsHandled() throws UnknownTaskException {
        TaskResultCreator taskResultCreator = new TaskResultCreator();
        TaskResultImpl mockedTaskResultImpl = mock(TaskResultImpl.class);
        Map<TaskId, TaskResult> loadTaskResultsValue = new HashMap<>();
        loadTaskResultsValue.put(this.createTaskID(), mockedTaskResultImpl);

        SchedulerDBManager mockedschedulerDbManager = mock(SchedulerDBManager.class);
        when(mockedschedulerDbManager.loadTasksResults(any(JobId.class), any(List.class)))
                .thenThrow(DatabaseManagerException.class);

        taskResultCreator.getTaskResult(mockedschedulerDbManager, this.getMockedInternalJob(this.getMockedJobDescriptorWithPausedTask()), this.getMockedInternalTask());
    }

    @Test
    public void testThatEmptyTaskResultIsUsedWhenResultIsNotInDatabase() throws UnknownTaskException {
        TaskResultCreator taskResultCreator = spy(TaskResultCreator.class);
        TaskResultImpl mockedTaskResultImpl = mock(TaskResultImpl.class);
        doReturn(mockedTaskResultImpl).when(taskResultCreator)
                .getEmptyTaskResultWithTaskIdAndExecutionTime(any(InternalTask.class), any(Throwable.class), any(TaskLogs.class));
        Map<TaskId, TaskResult> loadTaskResultsValue = new HashMap<>();
        loadTaskResultsValue.put(this.createTaskID(), mockedTaskResultImpl);

        SchedulerDBManager mockedschedulerDbManager = mock(SchedulerDBManager.class);
        when(mockedschedulerDbManager.loadTasksResults(any(JobId.class), any(List.class)))
                .thenThrow(DatabaseManagerException.class);

        taskResultCreator.getTaskResult(mockedschedulerDbManager, this.getMockedInternalJob(this.getMockedJobDescriptorWithPausedTask()), this.getMockedInternalTask());

        verify(mockedTaskResultImpl).setPropagatedVariables(any(Map.class));

    }

    @Test
    public void testThatGetEmptyTaskResultWithTaskIdAndExecutionTimeSetsCorrectTime() {
        TaskResultCreator taskResultCreator = new TaskResultCreator();

        InternalTask mockedInternalTask = mock(InternalTask.class);
        when(mockedInternalTask.getStartTime()).thenReturn(System.currentTimeMillis() - 1);

        TaskResultImpl taskResult = taskResultCreator.getEmptyTaskResultWithTaskIdAndExecutionTime(mockedInternalTask, null, null);
        // Between 1 millisecond and 1 second.
        assertThat(taskResult.getTaskDuration(), Matchers.greaterThan(0L));
        assertThat(taskResult.getTaskDuration(), Matchers.lessThan(1000L));


    }

    @Test
    public void testThatPropagatedVariablesAreExtractedFromParents() throws UnknownTaskException {
        Map<String, byte[]> fakeVariableMap = new HashMap<>();
        fakeVariableMap.put("ParentVar", "5623g".getBytes());
        TaskResultCreator taskResultCreator = new TaskResultCreator();
        TaskResultImpl mockedParentTaskResultImpl = mock(TaskResultImpl.class);
        Map<TaskId, TaskResult> loadParentTaskResultsValue = new HashMap<>();
        loadParentTaskResultsValue.put(this.createTaskID(), mockedParentTaskResultImpl);
        when(mockedParentTaskResultImpl.getPropagatedVariables()).thenReturn(fakeVariableMap);

        SchedulerDBManager mockedschedulerDbManager = mock(SchedulerDBManager.class);

        when(mockedschedulerDbManager.loadTasksResults(any(JobId.class), any(List.class)))
                .thenReturn(loadParentTaskResultsValue);

        TaskResult taskResult = taskResultCreator.getTaskResult(mockedschedulerDbManager, this.getMockedInternalJobTaskFlowType(this.getMockedJobDescriptorWithPausedTask()), this.getMockedInternalTask());

        when(taskResult.getPropagatedVariables()).thenCallRealMethod();
        assertThat(new String(taskResult.getPropagatedVariables().get("ParentVar")), is("5623g"));
    }

    @Test
    public void testThatJobVariablesAreUsedIfTaskHasNoParents() throws UnknownTaskException {
        TaskResultCreator taskResultCreator = new TaskResultCreator();

        SchedulerDBManager mockedschedulerDbManager = mock(SchedulerDBManager.class);
        when(mockedschedulerDbManager.loadTasksResults(any(JobId.class), any(List.class)))
                .thenThrow(new DatabaseManagerException());


        InternalJob mockedInternalJob = this.getMockedInternalJobTaskFlowType(this.getMockedJobDescriptorWithPausedTaskWithoutParent());

        Map<String, String> fakeVariableMap = new HashMap<>();
        fakeVariableMap.put("TestVar", "h234");
        when(mockedInternalJob.getVariables()).thenReturn(fakeVariableMap);

        TaskResult taskResult = taskResultCreator.getTaskResult(mockedschedulerDbManager, mockedInternalJob, this.getMockedInternalTask());

        verify(mockedInternalJob, atLeastOnce()).getVariables();

        assertThat(new String(taskResult
                        .getPropagatedVariables()
                        .get("TestVar")),
                is("h234"));
    }

    @Test
    public void testThatGetPropagatedVariablesAreExtractedFromParents() throws UnknownTaskException {
        TaskResultCreator taskResultCreator = new TaskResultCreator();
        TaskResultImpl mockedTaskResultImpl = mock(TaskResultImpl.class);
        Map<TaskId, TaskResult> loadTaskResultsValue = new HashMap<>();
        loadTaskResultsValue.put(this.createTaskID(), mockedTaskResultImpl);

        when(mockedTaskResultImpl.getPropagatedVariables())
                .thenReturn(new HashMap<String, byte[]>());

        SchedulerDBManager mockedschedulerDbManager = mock(SchedulerDBManager.class);
        when(mockedschedulerDbManager.loadTasksResults(any(JobId.class), any(List.class)))
                .thenReturn(loadTaskResultsValue);

        taskResultCreator.getTaskResult(mockedschedulerDbManager, this.getMockedInternalJobTaskFlowType(this.getMockedJobDescriptorWithPausedTask()), this.getMockedInternalTask());

        verify(mockedTaskResultImpl, atLeastOnce()).getPropagatedVariables();
    }

    @Test
    public void testRunningTasksAreCheckedForATaskId() throws UnknownTaskException {
        TaskResultCreator taskResultCreator = new TaskResultCreator();
        TaskResultImpl mockedTaskResultImpl = mock(TaskResultImpl.class);
        Map<TaskId, TaskResult> loadTaskResultsValue = new HashMap<>();
        loadTaskResultsValue.put(this.createTaskID(), mockedTaskResultImpl);

        SchedulerDBManager mockedschedulerDbManager = mock(SchedulerDBManager.class);
        when(mockedschedulerDbManager.loadTasksResults(any(JobId.class), any(List.class)))
                .thenReturn(loadTaskResultsValue);

        JobDescriptorImpl mockedJobDescriptorHasRunningTask = this.getMockedJobDescriptorWithRunningTask();

        taskResultCreator.getTaskResult(mockedschedulerDbManager, this.getMockedInternalJob(mockedJobDescriptorHasRunningTask), this.getMockedInternalTask());

        verify(mockedJobDescriptorHasRunningTask, atLeastOnce()).getRunningTasks();
    }

    @Test
    public void testPausedTasksAreCheckedForATaskId() throws UnknownTaskException {
        TaskResultCreator taskResultCreator = new TaskResultCreator();
        TaskResultImpl mockedTaskResultImpl = mock(TaskResultImpl.class);
        Map<TaskId, TaskResult> loadTaskResultsValue = new HashMap<>();
        loadTaskResultsValue.put(this.createTaskID(), mockedTaskResultImpl);

        SchedulerDBManager mockedschedulerDbManager = mock(SchedulerDBManager.class);
        when(mockedschedulerDbManager.loadTasksResults(any(JobId.class), any(List.class)))
                .thenReturn(loadTaskResultsValue);

        JobDescriptorImpl mockedJobDescriptorHasPausedTask = this.getMockedJobDescriptorWithPausedTask();

        taskResultCreator.getTaskResult(mockedschedulerDbManager, this.getMockedInternalJob(mockedJobDescriptorHasPausedTask), this.getMockedInternalTask());

        verify(mockedJobDescriptorHasPausedTask, atLeastOnce()).getPausedTasks();
    }

    private JobId createJobId() {
        return new JobIdImpl(1L, "TestJob");
    }

    private TaskId createTaskID() {
        return TaskIdImpl.createTaskId(this.createJobId(), "TestTask", 10);
    }

    private TaskId createParentTaskID() {
        return TaskIdImpl.createTaskId(this.createJobId(), "TestTask", 1);
    }

    private InternalJob getMockedInternalJobTaskFlowType(JobDescriptorImpl mockedJobDescriptor) {
        InternalJob mockedInternalJob = mock(InternalJob.class);
        when(mockedInternalJob.getJobDescriptor()).thenReturn(mockedJobDescriptor);
        when(mockedInternalJob.getType()).thenReturn(JobType.TASKSFLOW);
        when(mockedInternalJob.getVariables()).thenReturn(new HashMap<String, String>());
        return mockedInternalJob;
    }

    private InternalJob getMockedInternalJob(JobDescriptorImpl mockedJobDescriptor) {
        InternalJob mockedInternalJob = mock(InternalJob.class);
        when(mockedInternalJob.getJobDescriptor()).thenReturn(mockedJobDescriptor);
        return mockedInternalJob;
    }

    private JobDescriptorImpl getMockedJobDescriptor() {
        JobDescriptorImpl mockedJobDescriptor = mock(JobDescriptorImpl.class);
        return mockedJobDescriptor;
    }

    private EligibleTaskDescriptorImpl createParentTaskDescriptor() {
        EligibleTaskDescriptorImpl mockedEligibleTaskDescriptorImpl
                = mock(EligibleTaskDescriptorImpl.class);
        TaskId parentId = this.createParentTaskID();
        doReturn(parentId).when(mockedEligibleTaskDescriptorImpl).getTaskId();
        return mockedEligibleTaskDescriptorImpl;
    }

    private Vector<TaskDescriptor> createParentVector() {
        Vector<TaskDescriptor> parentVector = new Vector<>();
        parentVector.add(createParentTaskDescriptor());
        return parentVector;
    }

    private EligibleTaskDescriptor createEligibleTaskDescriptor(Vector<TaskDescriptor> fakeParentVector) {
        EligibleTaskDescriptorImpl mockedEligibleTaskDescriptorImpl =
                mock(EligibleTaskDescriptorImpl.class);

        when(mockedEligibleTaskDescriptorImpl.getParents())
                .thenReturn(fakeParentVector);

        return mockedEligibleTaskDescriptorImpl;
    }

    private Map<TaskId, EligibleTaskDescriptor> mockedPausedOrRunningTaskMap() {
        EligibleTaskDescriptor eligibleTaskDescriptor = this.createEligibleTaskDescriptor(this.createParentVector());
        Map<TaskId, EligibleTaskDescriptor> mockedMap = mock(HashMap.class);
        when(mockedMap.get(any())).thenReturn(eligibleTaskDescriptor);
        return mockedMap;
    }

    private Map<TaskId, EligibleTaskDescriptor> mockedPausedOrRunningTaskMapWithoutParentTasks() {
        EligibleTaskDescriptor eligibleTaskDescriptor = this.createEligibleTaskDescriptor(new Vector<TaskDescriptor>());
        Map<TaskId, EligibleTaskDescriptor> mockedMap = mock(HashMap.class);
        when(mockedMap.get(any())).thenReturn(eligibleTaskDescriptor);
        return mockedMap;
    }

    private JobDescriptorImpl getMockedJobDescriptorWithPausedTaskWithoutParent() {
        JobDescriptorImpl mockedJobDescriptor = this.getMockedJobDescriptor();
        Map fakeMap = this.mockedPausedOrRunningTaskMapWithoutParentTasks();
        when(mockedJobDescriptor.getPausedTasks()).thenReturn(fakeMap);
        return mockedJobDescriptor;
    }

    private JobDescriptorImpl getMockedJobDescriptorWithPausedTask() {
        JobDescriptorImpl mockedJobDescriptor = this.getMockedJobDescriptor();
        Map fakeMap = this.mockedPausedOrRunningTaskMap();
        when(mockedJobDescriptor.getPausedTasks()).thenReturn(fakeMap);
        return mockedJobDescriptor;
    }

    private JobDescriptorImpl getMockedJobDescriptorWithRunningTask() {
        JobDescriptorImpl mockedJobDescriptor = this.getMockedJobDescriptor();
        Map fakeMap = this.mockedPausedOrRunningTaskMap();
        when(mockedJobDescriptor.getRunningTasks()).thenReturn(fakeMap);
        return mockedJobDescriptor;
    }

    private InternalTask getMockedInternalTask() {
        InternalTask mockedInternalTask = mock(InternalScriptTask.class);
        TaskInfo mockedTaskInfo = this.getMockedTaskInfo();
        when(mockedInternalTask.getTaskInfo()).thenReturn(mockedTaskInfo);
        when(mockedInternalTask.getId()).thenReturn(this.createTaskID());
        when(mockedInternalTask.handleResultsArguments()).thenReturn(true);
        return mockedInternalTask;
    }

    private TaskInfo getMockedTaskInfo() {
        TaskInfo mockedTaskInfo = mock(TaskInfo.class);
        when(mockedTaskInfo.getJobId()).thenReturn(this.createJobId());
        when(mockedTaskInfo.getTaskId()).thenReturn(this.createTaskID());
        return mockedTaskInfo;
    }

}