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
package org.ow2.proactive.scheduler.job;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskInfoImpl;


public class ClientJobStateTest {

    @BeforeClass
    public static void configureLogger() throws Exception {
        BasicConfigurator.configure(new NullAppender());
    }

    @Test
    public void testRestoreDependenciesSerializationCallsDefaultReadObjectAndSerializationHelper()
            throws NoSuchFieldException, IllegalAccessException, IOException, ClassNotFoundException,
            NoSuchMethodException, InvocationTargetException {
        // Create a ClientJobState
        ClientJobState jobState = new ClientJobState(new TestJobState());

        ClientJobSerializationHelper helperMock = Mockito.mock(ClientJobSerializationHelper.class);
        this.setPrivateField(ClientJobState.class.getDeclaredField("clientJobSerializationHelper"),
                             jobState,
                             helperMock);

        Method method = jobState.getClass().getDeclaredMethod("readObject", ObjectInputStream.class);
        method.setAccessible(true);
        ObjectInputStream mockedObjectInputStream = Mockito.mock(ObjectInputStream.class);
        method.invoke(jobState, mockedObjectInputStream);

        Mockito.verify(mockedObjectInputStream).defaultReadObject();
        Mockito.verify(helperMock).serializeTasks(Mockito.any(Map.class));
    }

    /**
     * Sets a private field.
     *
     * @param privateField The private field to set.
     * @param target       Instance of class, in which to set the field.
     * @param value        Value to set the field to.
     */
    private void setPrivateField(Field privateField, Object target, Object value) throws IllegalAccessException {
        privateField.setAccessible(true);
        privateField.set(target, value);
        privateField.setAccessible(false);
    }

    @Test
    public void testNumberOfTasksInJobInfoUpdatedWhenUpdateTask() throws Exception {
        JobInfoImpl jobInfo = createJobInfo();
        ClientJobState jobState = new ClientJobState(createJobState(jobInfo));

        jobInfo.setNumberOfFinishedTasks(3);
        jobInfo.setNumberOfPendingTasks(2);
        jobInfo.setNumberOfRunningTasks(1);
        TaskInfoImpl updatedTask = createTaskInfo(jobInfo);

        jobState.update(updatedTask);

        assertEquals(1, jobState.getJobInfo().getNumberOfRunningTasks());
        assertEquals(2, jobState.getJobInfo().getNumberOfPendingTasks());
        assertEquals(3, jobState.getJobInfo().getNumberOfFinishedTasks());
    }

    private JobInfoImpl createJobInfo() {
        JobInfoImpl jobInfo = new JobInfoImpl();
        JobIdImpl jobId = new JobIdImpl(1000, "job");
        jobInfo.setJobId(jobId);
        return jobInfo;
    }

    private TaskInfoImpl createTaskInfo(JobInfoImpl jobInfo) {
        TaskInfoImpl updatedTask = new TaskInfoImpl();

        updatedTask.setJobInfo(jobInfo);
        updatedTask.setTaskId(TaskIdImpl.createTaskId(jobInfo.getJobId(), "task", 1));
        return updatedTask;
    }

    private JobState createJobState(final JobInfoImpl jobInfo) {
        return new JobState() {
            @Override
            public void update(TaskInfo info) {

            }

            @Override
            public void update(JobInfo jobInfo) {

            }

            @Override
            public JobInfo getJobInfo() {
                return jobInfo;
            }

            @Override
            public List<TaskState> getTasks() {
                List<TaskState> tasks = new ArrayList<>(0);
                tasks.add(new TaskState() {
                    @Override
                    public void update(TaskInfo taskInfo) {

                    }

                    @Override
                    public List<TaskState> getDependences() {
                        return null;
                    }

                    @Override
                    public TaskInfo getTaskInfo() {
                        TaskInfoImpl taskInfo = new TaskInfoImpl();
                        taskInfo.setJobInfo(jobInfo);
                        taskInfo.setTaskId(TaskIdImpl.createTaskId(jobInfo.getJobId(), "task", 1));
                        return taskInfo;
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
                });
                return tasks;
            }

            @Override
            public Map<TaskId, TaskState> getHMTasks() {
                return null;
            }

            @Override
            public String getOwner() {
                return null;
            }

            @Override
            public JobType getType() {
                return null;
            }
        };
    }

    private class TestJobState extends JobState {

        private final List<TaskState> listOfTasks = new ArrayList<>(0);

        @Override
        public void update(TaskInfo info) {

        }

        @Override
        public void update(JobInfo jobInfo) {

        }

        @Override
        public JobInfo getJobInfo() {
            return new JobInfoImpl();
        }

        @Override
        public List<TaskState> getTasks() {
            return this.listOfTasks;
        }

        @Override
        public Map<TaskId, TaskState> getHMTasks() {
            return new HashMap<>(0);
        }

        @Override
        public String getOwner() {
            return "testOwnder";
        }

        @Override
        public JobType getType() {
            return JobType.TASKSFLOW;
        }
    }

}
