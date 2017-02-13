/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.common.task;

import static org.junit.Assert.*;

import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;


public class Log4JTaskLogsTest {

    @Test
    public void one_line_one_event() throws Exception {
        LinkedList<LoggingEvent> logEvents = new LinkedList<>();
        logEvents.add(new LoggingEvent(null, Logger.getLogger("test"), Log4JTaskLogs.STDERR_LEVEL, "error", null));
        logEvents.add(new LoggingEvent(null, Logger.getLogger("test"), Log4JTaskLogs.STDOUT_LEVEL, "output", null));

        Log4JTaskLogs taskLogs = new Log4JTaskLogs(logEvents, "123");

        assertEquals(String.format("output%n"), taskLogs.getStdoutLogs(false));
        assertEquals(String.format("error%n"), taskLogs.getStderrLogs(false));
        assertEquals(String.format("error%noutput%n"), taskLogs.getAllLogs(false));

        assertTrue(taskLogs.getStdoutLogs(true).matches(String.format("\\[.*\\] output %n")));
        assertTrue(taskLogs.getStderrLogs(true).matches(String.format("\\[.*\\] error %n")));
        assertTrue(taskLogs.getAllLogs(true).matches(String.format("\\[.*\\] error %n\\[.*\\] output %n")));
    }
}
