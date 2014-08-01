package org.ow2.proactive.scheduler.common.task;

import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;

import static org.junit.Assert.*;


public class Log4JTaskLogsTest {

    @Test
    public void one_line_one_event() throws Exception {
        LinkedList<LoggingEvent> logEvents = new LinkedList<LoggingEvent>();
        logEvents.add(
          new LoggingEvent(null, Logger.getLogger("test"), Log4JTaskLogs.STDERR_LEVEL, "error", null));
        logEvents.add(
          new LoggingEvent(null, Logger.getLogger("test"), Log4JTaskLogs.STDOUT_LEVEL, "output", null));

        Log4JTaskLogs taskLogs = new Log4JTaskLogs(logEvents, "123");

        assertEquals(String.format("output%n"), taskLogs.getStdoutLogs(false));
        assertEquals(String.format("error%n"), taskLogs.getStderrLogs(false));
        assertEquals(String.format("error%noutput%n"), taskLogs.getAllLogs(false));

        assertTrue(taskLogs.getStdoutLogs(true).matches(String.format("\\[.*\\] output %n")));
        assertTrue(taskLogs.getStderrLogs(true).matches(String.format("\\[.*\\] error %n")));
        assertTrue(taskLogs.getAllLogs(true).matches(String.format("\\[.*\\] error %n\\[.*\\] output %n")));
    }
}