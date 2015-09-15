/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskLogs;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.common.util.logforwarder.appenders.AsyncAppenderWithStorage;
import org.ow2.proactive.scheduler.common.util.logforwarder.util.LoggingOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;


public class TaskLogger {

    private static final Logger logger = Logger.getLogger(TaskLogger.class);

    // the prefix for log file produced in localspace
    private static final String LOG_FILE_PREFIX = "TaskLogs";

    private static final String MAX_LOG_SIZE_PROPERTY = "pas.launcher.logs.maxsize";

    // default log size, counted in number of log events
    private static final int DEFAULT_LOG_MAX_SIZE = 1024;

    private static final String FILE_APPENDER_NAME = "TASK_LOGGER_FILE_APPENDER";

    private AsyncAppenderWithStorage taskLogAppender;

    private TaskId taskId;
    private String hostname;

    private final PrintStream outputSink;
    private final PrintStream errorSink;

    private final AtomicBoolean loggersFinalized = new AtomicBoolean(false);
    private final AtomicBoolean loggersActivated = new AtomicBoolean(false);

    public TaskLogger(TaskId taskId, String hostname) {
        logger.debug("Create task logger");

        this.taskId = taskId;
        this.hostname = hostname;

        Logger taskLogger = createLog4jLogger(taskId);

        outputSink = new PrintStream(new LoggingOutputStream(taskLogger, Log4JTaskLogs.STDOUT_LEVEL), true);
        errorSink = new PrintStream(new LoggingOutputStream(taskLogger, Log4JTaskLogs.STDERR_LEVEL), true);
    }

    private Logger createLog4jLogger(TaskId taskId) {
        LogLog.setQuietMode(true); // error about log should not be logged

        Logger taskLogger = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + taskId.getJobId() + "." +
            taskId.value());
        taskLogger.setLevel(Log4JTaskLogs.STDOUT_LEVEL);
        taskLogger.setAdditivity(false);

        resetLogContextForImmediateService();

        taskLogger.removeAllAppenders();

        taskLogAppender = new AsyncAppenderWithStorage(getLogMaxSize(taskId));
        taskLogger.addAppender(taskLogAppender);
        return taskLogger;
    }

    private int getLogMaxSize(TaskId taskId) {
        String logMaxSizeProp = System.getProperty(MAX_LOG_SIZE_PROPERTY);
        int logMaxSize = DEFAULT_LOG_MAX_SIZE;
        if (logMaxSizeProp != null && !logMaxSizeProp.isEmpty()) {
            try {
                logMaxSize = Integer.parseInt(logMaxSizeProp);
            } catch (NumberFormatException e) {
                logger.warn(MAX_LOG_SIZE_PROPERTY +
                    " property is not correctly defined. Logs size is bounded to default value " +
                    DEFAULT_LOG_MAX_SIZE + " for task " + taskId, e);
            }
        }
        return logMaxSize;
    }

    public TaskLogs getLogs() {
        return new Log4JTaskLogs(taskLogAppender.getStorage(), this.taskId.getJobId().value());
    }

    public File createFileAppender(File pathToFolder) throws IOException {
        if (taskLogAppender.getAppender(FILE_APPENDER_NAME) != null) {
            throw new IllegalStateException("Only one file appender can be created");
        }
        String logFileName = LOG_FILE_PREFIX + "-" + taskId.getJobId() + "-" + taskId.value() + ".log";

        File logFile = new File(pathToFolder, logFileName);
        FileUtils.touch(logFile);
        logFile.setWritable(true, false);

        FileAppender fap = new FileAppender(Log4JTaskLogs.getTaskLogLayout(), logFile.getAbsolutePath(),
            false);
        fap.setName(FILE_APPENDER_NAME);
        taskLogAppender.addAppender(fap);

        return logFile;
    }

    public PrintStream getOutputSink() {
        return outputSink;
    }

    public PrintStream getErrorSink() {
        return errorSink;
    }

    public void activateLogs(AppenderProvider logSink) {
        logger.info("Activating logs for task " + this.taskId + " (" + taskId.getReadableName() + ")");
        if (this.loggersActivated.get()) {
            logger.info("Logs for task " + this.taskId + " are already activated");
            return;
        }
        this.loggersActivated.set(true);

        // create appender
        Appender appender;
        try {
            appender = logSink.getAppender();
        } catch (LogForwardingException e) {
            logger.error("Cannot create log appender.", e);
            return;
        }
        // fill appender
        if (!this.loggersFinalized.get()) {
            taskLogAppender.addAppender(appender);
        } else {
            logger.info("Logs for task " + this.taskId + " are closed. Flushing buffer...");
            // Everything is closed: reopen and close...
            for (LoggingEvent e : taskLogAppender.getStorage()) {
                appender.doAppend(e);
            }
            appender.close();
            this.loggersActivated.set(false);
            return;
        }
        logger.info("Activated logs for task " + this.taskId);
    }

    public void getStoredLogs(AppenderProvider logSink) {
        Appender appender;
        try {
            appender = logSink.getAppender();
        } catch (LogForwardingException e) {
            logger.error("Cannot create log appender.", e);
            return;
        }
        taskLogAppender.appendStoredEvents(appender);
    }

    // need to reset MDC because calling thread is not active thread (immediate service)
    public void resetLogContextForImmediateService() {
        MDC.put(Log4JTaskLogs.MDC_TASK_ID, this.taskId.value());
        MDC.put(Log4JTaskLogs.MDC_TASK_NAME, this.taskId.getReadableName());
        MDC.put(Log4JTaskLogs.MDC_HOST, hostname);
    }

    public void close() {
        synchronized (this.loggersFinalized) {
            if (!loggersFinalized.get()) {
                logger.debug("Terminating loggers for task " + this.taskId + " (" + taskId.getReadableName() +
                    ")" + "...");
                this.flushStreams();

                this.loggersFinalized.set(true);
                this.loggersActivated.set(false);

                removeTaskLogFile();

                // Unhandle loggers
                if (taskLogAppender != null) {
                    taskLogAppender.close();
                }
                logger.debug("Task logger closed");
            }
        }
    }

    private void removeTaskLogFile() {
        FileAppender fileAppender = (FileAppender) taskLogAppender.getAppender(FILE_APPENDER_NAME);
        if (fileAppender != null && fileAppender.getFile() != null) {
            FileUtils.deleteQuietly(new File(fileAppender.getFile()));
        }
    }

    private void flushStreams() {
        this.outputSink.flush();
        this.errorSink.flush();
    }

}
