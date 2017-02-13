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
package org.ow2.proactive.scheduler.task;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;


public class ClientTaskStateTest {

    @Test
    public void testRestoreDependenciesSerializationCallsDefaultReadObject()
            throws NoSuchFieldException, IllegalAccessException, IOException, ClassNotFoundException,
            NoSuchMethodException, InvocationTargetException {
        ClientTaskState clientTaskState = new ClientTaskState(new TestTaskState());

        Method method = clientTaskState.getClass().getDeclaredMethod("readObject", ObjectInputStream.class);
        method.setAccessible(true);
        ObjectInputStream mockedObjectInputStream = Mockito.mock(ObjectInputStream.class);
        method.invoke(clientTaskState, mockedObjectInputStream);

        Mockito.verify(mockedObjectInputStream).defaultReadObject();
    }

    @Test
    public void testRestoreDependenciesSerializationCreatesEmptyArrayList()
            throws NoSuchFieldException, IllegalAccessException, IOException, ClassNotFoundException,
            NoSuchMethodException, InvocationTargetException {
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
