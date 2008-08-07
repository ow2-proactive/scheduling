/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task;

import java.util.LinkedList;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;


/**
 * Log4j based implementation of TaskLogs.
 * @author The ProActive Team
 * @since 2.2
 */
public class Log4JTaskLogs implements TaskLogs {

    /** Prefix for job logger */
    public static final String JOB_LOGGER_PREFIX = "logger.scheduler.";

    /** Appender name for jobs */
    public static final String JOB_APPENDER_NAME = "JobLoggerAppender";

    /** Log4j context variable name for task ids */
    public static final String MDC_TASK_ID = "taskid";

    /** Default layout for logs */
    public static Layout getTaskLogLayout() {
        return new PatternLayout("[%X{" + Log4JTaskLogs.MDC_TASK_ID + "}@%d{HH:mm:ss}]" + " %m %n");
    }

    /** Logger level in which stdout must be redirected */
    public static final Level STDOUT_LEVEL = Level.INFO;

    /** Logger level in which stderr must be redirected */
    public static final Level STDERR_LEVEL = Level.ERROR;

    /** The logs buffer */
    private LinkedList<LoggingEvent> allEvents;

    /** New line **/
    private static final String nl = System.getProperty("line.separator");

    /**
     * Create a new Log4JTaskLogs log.
     * @param all the buffer of logging events.
     */
    public Log4JTaskLogs(LinkedList<LoggingEvent> all) {
        this.allEvents = all;
    }

    /**
     *
     * 
     * @see org.ow2.proactive.scheduler.common.task.TaskLogs#getAllLogs(boolean
     *      timeStamp)
     */
    public String getAllLogs(boolean timeStamp) {
        StringBuffer logs = new StringBuffer(this.allEvents.size());
        Layout l = getTaskLogLayout();
        for (LoggingEvent e : this.allEvents) {
            logs.append(timeStamp ? l.format(e) : e.getMessage());
            logs.append(nl);
        }

        return logs.toString();
    }

    /**
     *
     * 
     * @see org.ow2.proactive.scheduler.common.task.TaskLogs#getStderrLogs(boolean
     *      timeStamp)
     */
    public String getStderrLogs(boolean timeStamp) {
        StringBuffer logs = new StringBuffer();
        Layout l = getTaskLogLayout();
        for (LoggingEvent e : this.allEvents) {
            if (Log4JTaskLogs.STDERR_LEVEL.equals(e.getLevel())) {
                logs.append(timeStamp ? l.format(e) : e.getMessage());
                logs.append(nl);
            }
        }

        return logs.toString();
    }

    /**
     *
     * 
     * @see org.ow2.proactive.scheduler.common.task.TaskLogs#getStdoutLogs(boolean
     *      timeStamp)
     */
    public String getStdoutLogs(boolean timeStamp) {
        StringBuffer logs = new StringBuffer();
        Layout l = getTaskLogLayout();
        for (LoggingEvent e : this.allEvents) {
            if (Log4JTaskLogs.STDOUT_LEVEL.equals(e.getLevel())) {
                logs.append(timeStamp ? l.format(e) : e.getMessage());
                logs.append(nl);
            }
        }

        return logs.toString();
    }

    /**
     * Return all the currently logged events
     * @return a list containing all the currently logged events
     */
    public LinkedList<LoggingEvent> getAllEvents() {
        return (LinkedList<LoggingEvent>) allEvents.clone();
    }
}
