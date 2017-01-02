/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.utils.ObjectByteConverter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.IOException;
import java.util.LinkedList;

import static com.google.common.base.Throwables.getStackTraceAsString;


/**
 * Log4j based implementation of TaskLogs.
 * @author The ProActive Team
 * @since 2.2
 */
@PublicAPI
@XmlAccessorType(XmlAccessType.FIELD)
public class Log4JTaskLogs implements TaskLogs {

    // fix for #2456 : Credential Data and TaskLogs contain serialVersionUID based on scheduler server version
    private static final long serialVersionUID = 1L;

    /** Prefix for job logger */
    public static final String JOB_LOGGER_PREFIX = "logger.scheduler.";

    public static final String MDC_JOB_ID = "job.id";
    /** Log4j context variable name for task ids */
    public static final String MDC_TASK_ID = "task.id";
    public static final String MDC_TASK_NAME = "task.name";

    /** Log4j context variable name for task ids */
    public static final String MDC_HOST = "host";

    /** Default layout for logs */
    public static Layout getTaskLogLayout() {
        PatternLayout patternLayout;
        if (PASchedulerProperties.SCHEDULER_JOB_LOGS_PATTERN.isSet()) {
            patternLayout = new PatternLayout(PASchedulerProperties.SCHEDULER_JOB_LOGS_PATTERN.getValueAsString());
        } else {
            patternLayout = new PatternLayout("[%X{" + Log4JTaskLogs.MDC_JOB_ID + "}t%X{" +
                    Log4JTaskLogs.MDC_TASK_ID + "}@%X{" + Log4JTaskLogs.MDC_HOST +
                    "};%d{HH:mm:ss}]" + " %m %n");
        }
        return patternLayout;
    }

    public static String getLoggerName(String jobId) {
        return JOB_LOGGER_PREFIX + jobId;
    }

    public static String getLoggerName(JobId jobId) {
        return getLoggerName(jobId.toString());
    }

    /** Logger level in which stdout must be redirected */
    public static final Level STDOUT_LEVEL = Level.INFO;

    /** Logger level in which stderr must be redirected */
    public static final Level STDERR_LEVEL = Level.ERROR;

    /** The logs buffer */
    private transient LinkedList<LoggingEvent> allEvents;

    private byte[] serializedAllEvents;

    private String loggerName;

    /** New line **/
    private static final String nl = System.lineSeparator();

    /** Hibernate constructor */
    public Log4JTaskLogs() {
    }

    /**
     * Create a new Log4JTaskLogs log.
     * @param all the buffer of logging events.
     */
    public Log4JTaskLogs(LinkedList<LoggingEvent> all, String jobId) {
        this.allEvents = all;
        this.loggerName = Log4JTaskLogs.JOB_LOGGER_PREFIX + jobId;
        storeEvents();
    }

    /**
     *
     * 
     * @see org.ow2.proactive.scheduler.common.task.TaskLogs#getAllLogs(boolean
     *      timeStamp)
     */
    public synchronized String getAllLogs(boolean timeStamp) {
        restoreEvents();
        StringBuffer logs = new StringBuffer(this.allEvents.size());
        Layout l = getTaskLogLayout();
        for (LoggingEvent e : this.allEvents) {
            appendEventToLogs(e, logs, l, timeStamp);
        }

        return logs.toString();
    }

    private void appendEventToLogs(LoggingEvent logEvent, StringBuffer logs, Layout logFormat,
            boolean timeStamp) {
        if (timeStamp) {
            logs.append(logFormat.format(logEvent));
        } else {
            logs.append(logEvent.getMessage());
            logs.append(nl);
        }
    }

    /**
     * Restore the compressed byte array in the list of loggingEvent.
     */
    @SuppressWarnings("unchecked")
    private void restoreEvents() {
        if (this.allEvents == null) {
            // restore log4j events
            try {
                this.allEvents = (LinkedList<LoggingEvent>) ObjectByteConverter.byteArrayToObject(
                        this.serializedAllEvents, true);
            } catch (Exception e) {
                //store exception event in logs if we cannot convert
                LoggingEvent logError = new LoggingEvent(loggerName, Logger.getLogger(loggerName),
                    STDERR_LEVEL, "Cannot restore logging event from byte array : " +
                        getStackTraceAsString(e), e);
                this.allEvents = new LinkedList<LoggingEvent>();
                this.allEvents.add(logError);
            }
            //this.serializedAllEvents = null;
        }
    }

    /**
     * Store the list of loggingEvent in a compressed byte array.
     */
    private void storeEvents() {
        if (this.serializedAllEvents == null) {
            try {
                this.serializedAllEvents = ObjectByteConverter.objectToByteArray(this.allEvents, true);
            } catch (IOException e) {
                //create a log4j event with e inside
                LoggingEvent logError = new LoggingEvent(loggerName, Logger.getLogger(loggerName),
                    STDERR_LEVEL, "Could not convert logging event to byte array : " +
                        getStackTraceAsString(e), e);
                LinkedList<LoggingEvent> errorEvent = new LinkedList<LoggingEvent>();
                errorEvent.add(logError);
                try {
                    this.serializedAllEvents = ObjectByteConverter.objectToByteArray(errorEvent, true);
                } catch (IOException e1) {
                    Logger.getLogger(Log4JTaskLogs.class).error("Could not convert to serialized events", e1);
                }
            }
            this.allEvents = null;
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskLogs#getStderrLogs(boolean
     *      timeStamp)
     */
    public synchronized String getStderrLogs(boolean timeStamp) {
        restoreEvents();
        StringBuffer logs = new StringBuffer();
        Layout l = getTaskLogLayout();
        for (LoggingEvent e : this.allEvents) {
            if (Log4JTaskLogs.STDERR_LEVEL.equals(e.getLevel())) {
                appendEventToLogs(e, logs, l, timeStamp);
            }
        }

        return logs.toString();
    }

    @Override
    public String getStdoutLogs() {
        return getStdoutLogs(false);
    }

    @Override
    public String getStderrLogs() {
        return getStderrLogs(false);
    }

    @Override
    public String getAllLogs() {
        return getAllLogs(false);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.TaskLogs#getStdoutLogs(boolean
     *      timeStamp)
     */
    public synchronized String getStdoutLogs(boolean timeStamp) {
        restoreEvents();
        StringBuffer logs = new StringBuffer();
        Layout l = getTaskLogLayout();
        for (LoggingEvent e : this.allEvents) {
            if (Log4JTaskLogs.STDOUT_LEVEL.equals(e.getLevel())) {
                appendEventToLogs(e, logs, l, timeStamp);
            }
        }

        return logs.toString();
    }

    /**
     * Return all the currently logged events
     * @return a list containing all the currently logged events
     */
    @SuppressWarnings("unchecked")
    public synchronized LinkedList<LoggingEvent> getAllEvents() {
        restoreEvents();
        return (LinkedList<LoggingEvent>) allEvents.clone();
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        storeEvents();
        out.defaultWriteObject();
    }

}
