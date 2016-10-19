package org.ow2.proactive.scheduler.common.util;

import org.ow2.proactive.scheduler.common.task.TaskId;


public class TaskLoggerRelativePathGenerator {

    // the prefix for log file produced in localspace
    private static final String LOG_FILE_PREFIX = "TaskLogs";

    private TaskLoggerRelativePathGenerator() {
    }

    public static String generateRelativePath(TaskId taskId) {
        return taskId.getJobId().toString() + "/" + LOG_FILE_PREFIX + "-" + taskId.getJobId() + "-" +
            taskId.value() + ".log";
    }

}
