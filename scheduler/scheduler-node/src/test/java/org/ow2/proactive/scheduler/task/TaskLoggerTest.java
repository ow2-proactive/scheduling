package org.ow2.proactive.scheduler.task;

import java.io.StringWriter;
import java.util.regex.Pattern;

import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskLogger;
import org.apache.log4j.Appender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.junit.Test;

import static org.junit.Assert.*;


public class TaskLoggerTest {

    @Test
    public void printAndGetLogs() throws Exception {

        TaskLogger taskLogger = new TaskLogger(TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task",
                42L), "myhost");

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
        TaskLogger taskLogger = new TaskLogger(TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task",
                42L), "myhost");

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
        TaskLogger taskLogger = new TaskLogger(TaskIdImpl.createTaskId(new JobIdImpl(1000, "job"), "task",
                42L), "myhost");

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

        assertTrue(taskLogger.getLogs().getAllLogs(true)
                .matches("\\[" + quotedStringTaskId + "@myhost;[0-9][0-9]:[0-9][0-9]:[0-9][0-9]\\] hello \r?\n"));

        taskLogger.getErrorSink().println("error");
        assertTrue(taskLogger.getLogs().getStderrLogs(true)
                .matches("\\[" + quotedStringTaskId + "@myhost;[0-9][0-9]:[0-9][0-9]:[0-9][0-9]\\] error \r?\n"));

    }
}