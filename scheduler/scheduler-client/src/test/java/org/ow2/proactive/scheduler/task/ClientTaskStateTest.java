package org.ow2.proactive.scheduler.task;


import org.junit.Test;
import org.mockito.Mockito;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class ClientTaskStateTest {

    @Test
    public void testRestoreDependenciesSerializationCallsDefaultReadObject() throws NoSuchFieldException, IllegalAccessException, IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        ClientTaskState clientTaskState = new ClientTaskState(new TestTaskState());

        Method method = clientTaskState.getClass().getDeclaredMethod("readObject", ObjectInputStream.class);
        method.setAccessible(true);
        ObjectInputStream mockedObjectInputStream = Mockito.mock(ObjectInputStream.class);
        method.invoke(clientTaskState, mockedObjectInputStream);

        Mockito.verify(mockedObjectInputStream).defaultReadObject();
    }

    @Test
    public void testRestoreDependenciesSerializationCreatesEmptyArrayList() throws NoSuchFieldException, IllegalAccessException, IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        TestTaskState testTaskState = new TestTaskState();
        ClientTaskState clientTaskState = new ClientTaskState(testTaskState);

        // Fill the dependencies ArrayList
        clientTaskState.getDependences().add(testTaskState);

        // Check that there is one task in the dependencies list
        assertThat(clientTaskState.getDependences()).hasSize(1);

        // Call then readObject method
        Method method = clientTaskState.getClass().getDeclaredMethod("readObject", ObjectInputStream.class);
        method.setAccessible(true);
        ObjectInputStream mockedObjectInputStream = Mockito.mock(ObjectInputStream.class);
        Object r = method.invoke(clientTaskState, mockedObjectInputStream);

        // Check that the ArrayList is now an empty list
        assertThat(clientTaskState.getDependences()).hasSize(0);
    }

    public class TestTaskState extends TaskState {

        List<TaskState> dependencies = new ArrayList<>(0);

        @Override
        public void update(TaskInfo taskInfo) {

        }

        @Override
        public List<TaskState> getDependences() {
            return this.dependencies;
        }

        @Override
        public TaskInfo getTaskInfo() {
            return new TaskInfoImpl();
        }

        @Override
        public int getMaxNumberOfExecutionOnFailure() {
            return 0;
        }

        @Override
        public TaskState replicate() throws Exception {
            return this;
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
