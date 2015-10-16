package functionaltests.job.serialization;


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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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

        List<TaskState> listWithOneElement = deserializedClientJobState
                .getHMTasks().get(clientTaskState2.getId()).getDependences();

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
            return "Task"+this.taskInfo.getJobId().value();
        }
    }
}
