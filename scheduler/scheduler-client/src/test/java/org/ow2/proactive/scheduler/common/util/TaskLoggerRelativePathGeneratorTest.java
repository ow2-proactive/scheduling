package org.ow2.proactive.scheduler.common.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;


public class TaskLoggerRelativePathGeneratorTest {

    @Test
    public void testGenerationOfRelativePath() {
        TaskId taskId = TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L);
        assertThat(TaskLoggerRelativePathGenerator.generateRelativePath(taskId),
                is("1000/TaskLogs-1000-42.log"));
    }

}
