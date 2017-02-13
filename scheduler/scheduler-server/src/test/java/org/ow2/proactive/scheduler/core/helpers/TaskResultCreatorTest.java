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
package org.ow2.proactive.scheduler.core.helpers;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.ow2.proactive.db.DatabaseManagerException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.job.JobVariable;
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
        when(mockedschedulerDbManager.loadTasksResults(any(JobId.class),
                                                       any(List.class))).thenThrow(DatabaseManagerException.class);

        taskResultCreator.getTaskResult(mockedschedulerDbManager,
                                        this.getMockedInternalJob(this.getMockedJobDescriptorWithPausedTask()),
                                        this.getMockedInternalTask());
    }

    @Test
    public void testThatEmptyTaskResultIsUsedWhenResultIsNotInDatabase() throws UnknownTaskException {
        TaskResultCreator taskResultCreator = spy(TaskResultCreator.class);
        TaskResultImpl mockedTaskResultImpl = mock(TaskResultImpl.class);
        doReturn(mockedTaskResultImpl).when(taskResultCreator).getEmptyTaskResult(any(InternalTask.class),
                                                                                  any(Throwable.class),
                                                                                  any(TaskLogs.class));
        Map<TaskId, TaskResult> loadTaskResultsValue = new HashMap<>();
        loadTaskResultsValue.put(this.createTaskID(), mockedTaskResultImpl);

        SchedulerDBManager mockedschedulerDbManager = mock(SchedulerDBManager.class);
        when(mockedschedulerDbManager.loadTasksResults(any(JobId.class),
                                                       any(List.class))).thenThrow(DatabaseManagerException.class);

        taskResultCreator.getTaskResult(mockedschedulerDbManager,
                                        this.getMockedInternalJob(this.getMockedJobDescriptorWithPausedTask()),
                                        this.getMockedInternalTask());

        verify(mockedTaskResultImpl).setPropagatedVariables(any(Map.class));

    }

    @Test
    public void testThatGetEmptyTaskResultWithTaskIdAndExecutionTimeSetsCorrectTime() {
        TaskResultCreator taskResultCreator = new TaskResultCreator();

        InternalTask mockedInternalTask = mock(InternalTask.class);
        when(mockedInternalTask.getStartTime()).thenReturn(System.currentTimeMillis() - 1);

        TaskResultImpl taskResult = taskResultCreator.getEmptyTaskResult(mockedInternalTask, null, null);
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

        when(mockedschedulerDbManager.loadTasksResults(any(JobId.class),
                                                       any(List.class))).thenReturn(loadParentTaskResultsValue);

        TaskResult taskResult = taskResultCreator.getTaskResult(mockedschedulerDbManager,
                                                                this.getMockedInternalJobTaskFlowType(this.getMockedJobDescriptorWithPausedTask()),
                                                                this.getMockedInternalTask());

        when(taskResult.getPropagatedVariables()).thenCallRealMethod();
        assertThat(new String(taskResult.getPropagatedVariables().get("ParentVar")), is("5623g"));
    }

    @Test
    public void testThatJobVariablesAreUsedIfTaskHasNoParents() throws UnknownTaskException {
        TaskResultCreator taskResultCreator = new TaskResultCreator();

        SchedulerDBManager mockedschedulerDbManager = mock(SchedulerDBManager.class);
        when(mockedschedulerDbManager.loadTasksResults(any(JobId.class),
                                                       any(List.class))).thenThrow(new DatabaseManagerException());

        InternalJob mockedInternalJob = this.getMockedInternalJobTaskFlowType(this.getMockedJobDescriptorWithPausedTaskWithoutParent());

        Map<String, JobVariable> fakeVariableMap = new HashMap<>();
        fakeVariableMap.put("TestVar", new JobVariable("TestVar", "h234"));
        when(mockedInternalJob.getVariables()).thenReturn(fakeVariableMap);

        Map<String, String> fakeReplacementVariableMap = new HashMap<>();
        fakeReplacementVariableMap.put("TestVar", "h234");
        when(mockedInternalJob.getVariablesAsReplacementMap()).thenReturn(fakeReplacementVariableMap);

        TaskResult taskResult = taskResultCreator.getTaskResult(mockedschedulerDbManager,
                                                                mockedInternalJob,
                                                                this.getMockedInternalTask());

        verify(mockedInternalJob, atLeastOnce()).getVariablesAsReplacementMap();

        assertThat(new String(taskResult.getPropagatedVariables().get("TestVar")), is("h234"));
    }

    @Test
    public void testThatTaskVariablesAreUsedIfTaskisInDB() throws UnknownTaskException {
        TaskResultCreator taskResultCreator = new TaskResultCreator();
        TaskResultImpl mockedTaskResultImpl = mock(TaskResultImpl.class);
        Map<String, byte[]> fakeVariableMap = new HashMap<>();
        fakeVariableMap.put("TestVar", new String("h234").getBytes());
        when(mockedTaskResultImpl.getPropagatedVariables()).thenReturn(fakeVariableMap);

        SchedulerDBManager mockedschedulerDbManager = mock(SchedulerDBManager.class);
        when(mockedschedulerDbManager.loadLastTaskResult(any(TaskId.class))).thenReturn(mockedTaskResultImpl);

        InternalJob mockedInternalJob = this.getMockedInternalJobTaskFlowType(this.getMockedJobDescriptorWithPausedTaskWithoutParent());

        TaskResult taskResult = taskResultCreator.getTaskResult(mockedschedulerDbManager,
                                                                mockedInternalJob,
                                                                this.getMockedInternalTask());

        verify(mockedTaskResultImpl, atLeastOnce()).getPropagatedVariables();

        assertThat(new String(taskResult.getPropagatedVariables().get("TestVar")), is("h234"));
    }

    @Test
    public void testThatGetPropagatedVariablesAreExtractedFromParents() throws UnknownTaskException {
        TaskResultCreator taskResultCreator = new TaskResultCreator();
        TaskResultImpl mockedTaskResultImpl = mock(TaskResultImpl.class);
        Map<TaskId, TaskResult> loadTaskResultsValue = new HashMap<>();
        loadTaskResultsValue.put(this.createTaskID(), mockedTaskResultImpl);

        when(mockedTaskResultImpl.getPropagatedVariables()).thenReturn(new HashMap<String, byte[]>());

        SchedulerDBManager mockedschedulerDbManager = mock(SchedulerDBManager.class);
        when(mockedschedulerDbManager.loadTasksResults(any(JobId.class),
                                                       any(List.class))).thenReturn(loadTaskResultsValue);

        taskResultCreator.getTaskResult(mockedschedulerDbManager,
                                        this.getMockedInternalJobTaskFlowType(this.getMockedJobDescriptorWithPausedTask()),
                                        this.getMockedInternalTask());

        verify(mockedTaskResultImpl, atLeastOnce()).getPropagatedVariables();
    }

    @Test
    public void testRunningTasksAreCheckedForATaskId() throws UnknownTaskException {
        TaskResultCreator taskResultCreator = new TaskResultCreator();
        TaskResultImpl mockedTaskResultImpl = mock(TaskResultImpl.class);
        Map<TaskId, TaskResult> loadTaskResultsValue = new HashMap<>();
        loadTaskResultsValue.put(this.createTaskID(), mockedTaskResultImpl);

        SchedulerDBManager mockedschedulerDbManager = mock(SchedulerDBManager.class);
        when(mockedschedulerDbManager.loadTasksResults(any(JobId.class),
                                                       any(List.class))).thenReturn(loadTaskResultsValue);

        JobDescriptorImpl mockedJobDescriptorHasRunningTask = this.getMockedJobDescriptorWithRunningTask();

        taskResultCreator.getTaskResult(mockedschedulerDbManager,
                                        this.getMockedInternalJob(mockedJobDescriptorHasRunningTask),
                                        this.getMockedInternalTask());

        verify(mockedJobDescriptorHasRunningTask, atLeastOnce()).getRunningTasks();
    }

    @Test
    public void testPausedTasksAreCheckedForATaskId() throws UnknownTaskException {
        TaskResultCreator taskResultCreator = new TaskResultCreator();
        TaskResultImpl mockedTaskResultImpl = mock(TaskResultImpl.class);
        Map<TaskId, TaskResult> loadTaskResultsValue = new HashMap<>();
        loadTaskResultsValue.put(this.createTaskID(), mockedTaskResultImpl);

        SchedulerDBManager mockedschedulerDbManager = mock(SchedulerDBManager.class);
        when(mockedschedulerDbManager.loadTasksResults(any(JobId.class),
                                                       any(List.class))).thenReturn(loadTaskResultsValue);

        JobDescriptorImpl mockedJobDescriptorHasPausedTask = this.getMockedJobDescriptorWithPausedTask();

        taskResultCreator.getTaskResult(mockedschedulerDbManager,
                                        this.getMockedInternalJob(mockedJobDescriptorHasPausedTask),
                                        this.getMockedInternalTask());

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
        when(mockedInternalJob.getVariables()).thenReturn(new HashMap<String, JobVariable>());
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
        EligibleTaskDescriptorImpl mockedEligibleTaskDescriptorImpl = mock(EligibleTaskDescriptorImpl.class);
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
        EligibleTaskDescriptorImpl mockedEligibleTaskDescriptorImpl = mock(EligibleTaskDescriptorImpl.class);

        when(mockedEligibleTaskDescriptorImpl.getParents()).thenReturn(fakeParentVector);

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
