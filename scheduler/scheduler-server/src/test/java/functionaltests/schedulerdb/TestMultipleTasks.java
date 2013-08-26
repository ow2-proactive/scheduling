package functionaltests.schedulerdb;

import junit.framework.Assert;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class TestMultipleTasks extends BaseSchedulerDBTest {

    @Test
    public void testManyTasks() throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        final int TASKS_NUMBER = 1000;

        for (int i = 0; i < TASKS_NUMBER; i++) {
            job.addTask(createDefaultTask("task-" + i));
        }

        InternalJob jobData = defaultSubmitJobAndLoadInternal(true, job);
        Assert.assertEquals(TASKS_NUMBER, jobData.getTasks().size());
    }

    @Test
    public void testDependencies() throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        JavaTask task1 = createDefaultTask("task1");
        job.addTask(task1);

        JavaTask task2 = createDefaultTask("task2");
        job.addTask(task2);

        task1.addDependence(task2);

        JavaTask task3 = createDefaultTask("task3");
        job.addTask(task3);

        task2.addDependence(task3);

        InternalJob jobData = defaultSubmitJobAndLoadInternal(true, job);
        Assert.assertEquals(3, jobData.getITasks().size());

        InternalTask taskData1 = jobData.getTask("task1");
        InternalTask taskData2 = jobData.getTask("task2");
        InternalTask taskData3 = jobData.getTask("task3");

        Assert.assertEquals(1, taskData1.getDependences().size());
        Assert.assertEquals(1, taskData2.getDependences().size());
        Assert.assertNull(taskData3.getDependences());
        Assert.assertEquals(taskData2.getId(), taskData1.getDependences().get(0).getId());
        Assert.assertEquals(taskData3.getId(), taskData2.getDependences().get(0).getId());

        Assert.assertEquals(taskData2.getTaskInfo().getTaskId(), taskData1.getDependences().get(0)
                .getTaskInfo().getTaskId());
        Assert.assertEquals(taskData3.getTaskInfo().getTaskId(), taskData2.getDependences().get(0)
                .getTaskInfo().getTaskId());

    }

}
