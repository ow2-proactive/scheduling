package org.ow2.proactive.scheduler.util;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.utils.appenders.FileAppender;
import org.python.icu.impl.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class TaskLoggerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();


    @Test
    public void testGetJobLogFilename() {
        JobId jobId = new JobIdImpl(1123, "readableName");
        TaskId taskId = TaskIdImpl.createTaskId(jobId, "taskreadableName", 123123);
        assertThat(TaskLogger.getTaskLogRelativePath(taskId), is("1123/1123t123123"));
    }

    @Test
    public void testFileAppender() throws IOException {
        JobId jobId = new JobIdImpl(1123, "readableName");
        TaskId taskId = TaskIdImpl.createTaskId(jobId, "taskreadableName", 123123);

        FileAppender appender = new FileAppender();
        File logFolder = folder.newFolder("logs");
        File logFile = new File(logFolder, TaskLogger.getTaskLogRelativePath(taskId));
        appender.setFilesLocation(logFolder.getAbsolutePath());
        try {
            appender.append(TaskLogger.getTaskLogRelativePath(taskId), new LoggingEvent("mylogger", Logger.getRootLogger(), Level.INFO, "HelloWorld", null));
        } catch (Exception e) {
            Assert.fail(e);
        }
        assertTrue(FileUtils.readFileToString(logFile, Charset.defaultCharset()).contains("HelloWorld"));

    }
}
