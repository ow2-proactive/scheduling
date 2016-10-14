package org.ow2.proactive.scheduler.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;


public class TaskLoggerTest {

    @Test
    public void testGetJobLogFilename() {
        JobId jobId = new JobIdImpl(1123, "readableName");
        TaskId taskId = TaskIdImpl.createTaskId(jobId, "taskreadableName", 123123);
        assertThat(TaskLogger.getTaskLogRelativePath(taskId), is("1123/1123t123123"));
    }
}
