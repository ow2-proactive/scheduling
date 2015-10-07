package org.ow2.proactive.scheduler.job;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.task.ClientTaskState;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ClientTaskState.class)
public class ClientJobSerializationHelperTest {


    private ClientJobSerializationHelper clientJobSerializationHelper;

    @Before
    public void init() {
        clientJobSerializationHelper = new ClientJobSerializationHelper();
    }

    @Test
    public void serializeTasksTest() throws IOException, ClassNotFoundException {
        Map<TaskId, TaskState> taskStateMap = new HashMap<>();
        ClientTaskState taskStateMock = PowerMockito.mock(ClientTaskState.class);

        taskStateMap.put(TaskIdImpl.createTaskId(new JobIdImpl(1L, "First"),"", 1L), taskStateMock);

        this.clientJobSerializationHelper.serializeTasks(taskStateMap);
        Mockito.verify(taskStateMock).restoreDependences(taskStateMap);
    }

    @Test
    public void serializeTasksWithFiveTasksTest() throws IOException, ClassNotFoundException {
        Map<TaskId, TaskState> taskStateMap = new HashMap<>();
        ClientTaskState taskStateMock = PowerMockito.mock(ClientTaskState.class);

        taskStateMap.put(TaskIdImpl.createTaskId(new JobIdImpl(1L, "First"),"", 1L), taskStateMock);
        taskStateMap.put(TaskIdImpl.createTaskId(new JobIdImpl(2L, "Second"),"", 2L), taskStateMock);
        taskStateMap.put(TaskIdImpl.createTaskId(new JobIdImpl(3L, "Third"),"", 3L), taskStateMock);
        taskStateMap.put(TaskIdImpl.createTaskId(new JobIdImpl(4L, "Fourth"),"", 4L), taskStateMock);
        taskStateMap.put(TaskIdImpl.createTaskId(new JobIdImpl(5L, "Fifth"),"", 5L), taskStateMock);

        this.clientJobSerializationHelper.serializeTasks(taskStateMap);
        Mockito.verify(taskStateMock,Mockito.times(5)).restoreDependences(taskStateMap);
    }

    @Test
    public void serializeTasksWithZeroTasksTest() throws IOException, ClassNotFoundException {
        Map<TaskId, TaskState> taskStateMap = new HashMap<>();
        ClientTaskState taskStateMock = PowerMockito.mock(ClientTaskState.class);


        this.clientJobSerializationHelper.serializeTasks(taskStateMap);
        Mockito.verify(taskStateMock,Mockito.times(0)).restoreDependences(taskStateMap);
    }

    @Test(expected=NullPointerException.class)
    public void serializeTasksThrowsNullPointerExceptionWhenNullTest() throws IOException, ClassNotFoundException {
        this.clientJobSerializationHelper.serializeTasks(null);
    }

    @Test
    public void serializeTasksNotInstanceOfClientTaskStateTest() throws IOException, ClassNotFoundException {
        Map<TaskId, TaskState> taskStateMap = new HashMap<>();
        ClientTaskState taskStateMock = PowerMockito.mock(ClientTaskState.class);
        taskStateMap.put(TaskIdImpl.createTaskId(new JobIdImpl(1L, "First"),"", 1L), new TestTaskState());
        taskStateMap.put(TaskIdImpl.createTaskId(new JobIdImpl(2L, "Second"),"", 2L), new TestTaskState());

        this.clientJobSerializationHelper.serializeTasks(taskStateMap);
        Mockito.verify(taskStateMock,Mockito.times(0)).restoreDependences(taskStateMap);
    }

    public class TestTaskState extends TaskState {

        @Override
        public void update(TaskInfo taskInfo) {

        }

        @Override
        public List<TaskState> getDependences() {
            return null;
        }

        @Override
        public TaskInfo getTaskInfo() {
            return null;
        }

        @Override
        public int getMaxNumberOfExecutionOnFailure() {
            return 0;
        }

        @Override
        public TaskState replicate() throws Exception {
            return null;
        }

        @Override
        public int getIterationIndex() {
            return 0;
        }

        @Override
        public int getReplicationIndex() {
            return 0;
        }
    }

}
