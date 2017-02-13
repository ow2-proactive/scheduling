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
package functionaltests.job.serialization;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.job.ClientJobState;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.JobInfoImpl;
import org.ow2.proactive.scheduler.task.ClientTaskState;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskInfoImpl;


public class ClientJobStateSerializationTest {

    @Test
    public void JobStateSerializationRestoresAllDependenciesFromClientTaskSates() {
        // Task1 depends on task2
        ClientTaskState clientTaskState1 = new ClientTaskState(new TestTaskState(1L));
        ClientTaskState clientTaskState2 = new ClientTaskState(new TestTaskState(2L));
        clientTaskState2.getDependences().add(clientTaskState1);

        // Create JClientJobState which contains task1 and task2
        TestJobState testJobState = new TestJobState(1);
        testJobState.getHMTasks().put(clientTaskState1.getId(), clientTaskState1);
        testJobState.getHMTasks().put(clientTaskState2.getId(), clientTaskState2);
        ClientJobState clientJobState = new ClientJobState(testJobState);

        //Serialize and de-serialize the ClientJobState instance
        ClientJobState deserializedClientJobState = null;
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            new ObjectOutputStream(output).writeObject(clientJobState);
            deserializedClientJobState = (ClientJobState) new ObjectInputStream(new ByteArrayInputStream(output.toByteArray())).readObject();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Serialization must not fail with exception. " + e.getMessage());
        }

        List<TaskState> listWithOneElement = deserializedClientJobState.getHMTasks()
                                                                       .get(clientTaskState2.getId())
                                                                       .getDependences();

        assertThat(listWithOneElement.size(), is(1));
    }

    //********** HELPER TEST CLASSES ****************//

    public class TestJobState extends JobState {

        final JobInfoImpl jobInfo;

        final Map<TaskId, TaskState> taskMap = new HashMap<>();

        public TestJobState(long number) {
            this.jobInfo = new JobInfoImpl();
            this.jobInfo.setJobId(new JobIdImpl(number, Long.toString(number)));
        }

        @Override
        public void update(TaskInfo info) {

        }

        @Override
        public void update(JobInfo jobInfo) {

        }

        @Override
        public JobInfo getJobInfo() {
            return this.jobInfo;
        }

        @Override
        public ArrayList<TaskState> getTasks() {
            return new ArrayList<>(this.taskMap.values());
        }

        @Override
        public Map<TaskId, TaskState> getHMTasks() {
            return this.taskMap;
        }

        @Override
        public String getOwner() {
            return null;
        }

        @Override
        public JobType getType() {
            return null;
        }
    }

    public class TestTaskState extends TaskState {

        final List<TaskState> dependencies = new ArrayList<>();

        final TaskInfoImpl taskInfo;

        public TestTaskState(long number) {
            this.taskInfo = new TaskInfoImpl();
            taskInfo.setJobId(new JobIdImpl(number, Long.toString(number)));
            this.taskInfo.setTaskId(TaskIdImpl.createTaskId(taskInfo.getJobId(), Long.toString(number), number));
        }

        @Override
        public void update(TaskInfo taskInfo) {

        }

        @Override
        public List<TaskState> getDependences() {
            return this.dependencies;
        }

        @Override
        public TaskInfo getTaskInfo() {
            return this.taskInfo;
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

        @Override
        public String toString() {
            return "Task" + this.taskInfo.getJobId().value();
        }
    }
}
