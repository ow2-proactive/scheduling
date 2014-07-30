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

        assertEquals("output\n", taskLogs.getStdoutLogs(false));
        assertEquals("error\n", taskLogs.getStderrLogs(false));
        assertEquals("error\noutput\n", taskLogs.getAllLogs(false));

        assertTrue(taskLogs.getStdoutLogs(true).matches("\\[.*\\] output \n"));
        assertTrue(taskLogs.getStderrLogs(true).matches("\\[.*\\] error \n"));
        assertTrue(taskLogs.getAllLogs(true).matches("\\[.*\\] error \n\\[.*\\] output \n"));
    }
}