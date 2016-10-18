package org.ow2.proactive.scheduler.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.StringWriter;
import java.util.regex.Pattern;

import org.apache.log4j.Appender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.job.JobIdImpl;


public class TaskLoggerTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void printAndGetLogs() throws Exception {

        TaskLogger taskLogger = new TaskLogger(
            TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L), "myhost");

        assertEquals("", taskLogger.getLogs().getAllLogs(false));

        taskLogger.getOutputSink().println("hello");
        assertEquals(String.format("hello%n"), taskLogger.getLogs().getAllLogs(false));
        assertEquals(String.format("hello%n"), taskLogger.getLogs().getStdoutLogs(false));

        taskLogger.getErrorSink().println("error");
        assertEquals(String.format("hello%nerror%n"), taskLogger.getLogs().getAllLogs(false));
        assertEquals(String.format("error%n"), taskLogger.getLogs().getStderrLogs(false));
    }

    @Test
    public void logStreaming() throws Exception {
        TaskLogger taskLogger = new TaskLogger(
            TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L), "myhost");

        final StringWriter stringAppender = new StringWriter();
        AppenderProvider stringAppenderProvider = new AppenderProvider() {
            @Override
            public Appender getAppender() throws LogForwardingException {
                return new WriterAppender(new PatternLayout("%m%n"), stringAppender);
            }
        };

        taskLogger.activateLogs(stringAppenderProvider);

        assertEquals("", stringAppender.toString());

        taskLogger.getOutputSink().println("hello");

        // async appender, it gets buffered
        assertEquals("", stringAppender.toString());

        for (int i = 0; i < 1000; i++) {
            taskLogger.getOutputSink().println("hello");
        }
        assertNotEquals("", stringAppender.toString());
    }

    @Test
    public void getStoredLogs() throws Exception {
        TaskLogger taskLogger = new TaskLogger(
            TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L), "myhost");

        final StringWriter stringAppender = new StringWriter();
        AppenderProvider stringAppenderProvider = new AppenderProvider() {
            @Override
            public Appender getAppender() throws LogForwardingException {
                return new WriterAppender(new PatternLayout("%m%n"), stringAppender);
            }
        };

        taskLogger.getStoredLogs(stringAppenderProvider);
        assertEquals("", stringAppender.toString());

        taskLogger.getOutputSink().println("hello");

        taskLogger.getStoredLogs(stringAppenderProvider);
        assertEquals(String.format("hello%n"), stringAppender.toString());
    }

    @Test
    public void logPattern() throws Exception {
        TaskId taskId = TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task", 42L);
        TaskLogger taskLogger = new TaskLogger(taskId, "myhost");

        assertEquals("", taskLogger.getLogs().getAllLogs(false));

        taskLogger.getOutputSink().println("hello");

        String quotedStringTaskId = Pattern.quote(taskId.toString());

        assertTrue(taskLogger.getLogs().getAllLogs(true).matches(
                "\\[" + quotedStringTaskId + "@myhost;[0-9][0-9]:[0-9][0-9]:[0-9][0-9]\\] hello \r?\n"));

        taskLogger.getErrorSink().println("error");
        assertTrue(taskLogger.getLogs().getStderrLogs(true).matches(
                "\\[" + quotedStringTaskId + "@myhost;[0-9][0-9]:[0-9][0-9]:[0-9][0-9]\\] error \r?\n"));

    }

    @Test
    public void createFileAppenderTest() throws Exception {
        JobId jobid = new JobIdImpl(1000, "job");
        TaskId taskId = TaskIdImpl.createTaskId(jobid, "task", 42L);
        TaskLogger taskLogger = new TaskLogger(taskId, "myhost");

        File logFolder = tempFolder.newFolder("logfolder");

        File logAppender = taskLogger.createFileAppender(logFolder);

        System.out.println(logAppender.getParentFile().getName());

        assertEquals(logAppender.getParentFile().getName(), jobid.value());

        assertEquals(logAppender.getName(),
                TaskLogger.LOG_FILE_PREFIX + "-" + taskId.getJobId() + "-" + taskId.value() + ".log");

    }
}