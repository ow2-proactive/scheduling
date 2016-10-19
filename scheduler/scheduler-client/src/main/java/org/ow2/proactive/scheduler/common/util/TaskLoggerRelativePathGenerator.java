package org.ow2.proactive.scheduler.common.util;

import org.ow2.proactive.scheduler.common.task.TaskId;


public class TaskLoggerRelativePathGenerator {

    // the prefix for log file produced in localspace
    private static final String LOG_FILE_PREFIX = "TaskLogs";

    private final String relativePath;
    private final String fileName;

    public TaskLoggerRelativePathGenerator(TaskId taskId) {
        this.fileName = LOG_FILE_PREFIX + "-" + taskId.getJobId() + "-" + taskId.value() + ".log";
        this.relativePath = taskId.getJobId().toString() + "/" + fileName;
    }

    public String getRelativePath() {
        return this.relativePath;
    }

    public String getFileName() {
        return this.fileName;
    }

}
