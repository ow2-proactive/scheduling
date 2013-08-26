package unitTests;

import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;


public class TestTaskIdImpl {

    @Test
    public void testGetIterationIndex() throws Exception {
        TaskId taskNoIterationIndex = TaskIdImpl.createTaskId(new JobIdImpl(1L, "job"), "task", 1, false);
        TaskId taskIterationIndexSmallerThan9 = TaskIdImpl.createTaskId(new JobIdImpl(1L, "job"), "task#1",
                1, false);
        TaskId taskIterationIndexGreaterThan9 = TaskIdImpl.createTaskId(new JobIdImpl(1L, "job"), "task#10",
                1, false);
        TaskId taskReplicatedAndIterated = TaskIdImpl.createTaskId(new JobIdImpl(1L, "job"), "task#10*10", 1,
                false);

        assertEquals(0, taskNoIterationIndex.getIterationIndex());
        assertEquals(1, taskIterationIndexSmallerThan9.getIterationIndex());
        assertEquals(10, taskIterationIndexGreaterThan9.getIterationIndex());
        assertEquals(10, taskReplicatedAndIterated.getIterationIndex());
    }

    @Test
    public void testGetReplicationIndex() throws Exception {
        TaskId taskNoReplicationIndex = TaskIdImpl.createTaskId(new JobIdImpl(1L, "job"), "task", 1, false);
        TaskId taskReplicationIndexSmallerThan9 = TaskIdImpl.createTaskId(new JobIdImpl(1L, "job"), "task*1",
                1, false);
        TaskId taskReplicationIndexGreaterThan9 = TaskIdImpl.createTaskId(new JobIdImpl(1L, "job"),
                "task*10", 1, false);
        TaskId taskReplicatedAndIterated = TaskIdImpl.createTaskId(new JobIdImpl(1L, "job"), "task#10*10", 1,
                false);

        assertEquals(0, taskNoReplicationIndex.getReplicationIndex());
        assertEquals(1, taskReplicationIndexSmallerThan9.getReplicationIndex());
        assertEquals(10, taskReplicationIndexGreaterThan9.getReplicationIndex());
        assertEquals(10, taskReplicatedAndIterated.getReplicationIndex());
    }

}
