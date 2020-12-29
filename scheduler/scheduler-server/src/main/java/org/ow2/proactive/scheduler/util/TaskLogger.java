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
package org.ow2.proactive.scheduler.util;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.utils.appenders.FileAppender;


public class TaskLogger {

    private static final String PREFIX = "task ";

    private static TaskLogger instance = null;

    private Logger logger = Logger.getLogger(TaskLogger.class);

    private TaskLogger() {
    }

    public static synchronized TaskLogger getInstance() {
        if (instance == null) {
            instance = new TaskLogger();
        }
        return instance;
    }

    private String format(TaskId id, String message) {
        return PREFIX + id + " (" + id.getReadableName() + ") " + message;
    }

    public void warn(TaskId id, String message) {
        updateMdcWithTaskLogFilename(id);
        logger.warn(format(id, message));
        MDC.remove(FileAppender.FILE_NAME);
    }

    public void warn(TaskId id, String message, Throwable th) {
        updateMdcWithTaskLogFilename(id);
        logger.warn(format(id, message), th);
        MDC.remove(FileAppender.FILE_NAME);
    }

    public void info(TaskId id, String message) {
        updateMdcWithTaskLogFilename(id);
        logger.info(format(id, message));
        MDC.remove(FileAppender.FILE_NAME);
    }

    public void info(TaskId id, String message, Throwable th) {
        updateMdcWithTaskLogFilename(id);
        logger.info(format(id, message), th);
        MDC.remove(FileAppender.FILE_NAME);
    }

    public void debug(TaskId id, String message) {
        if (logger.isDebugEnabled()) {
            updateMdcWithTaskLogFilename(id);
            logger.debug(format(id, message));
            MDC.remove(FileAppender.FILE_NAME);
        }
    }

    public void debug(TaskId id, String message, Throwable th) {
        if (logger.isDebugEnabled()) {
            updateMdcWithTaskLogFilename(id);
            logger.debug(format(id, message), th);
            MDC.remove(FileAppender.FILE_NAME);
        }
    }

    public void trace(TaskId id, String message) {
        if (logger.isTraceEnabled()) {
            updateMdcWithTaskLogFilename(id);
            logger.trace(format(id, message));
            MDC.remove(FileAppender.FILE_NAME);
        }
    }

    public void trace(TaskId id, String message, Throwable th) {
        if (logger.isTraceEnabled()) {
            updateMdcWithTaskLogFilename(id);
            logger.trace(format(id, message), th);
            MDC.remove(FileAppender.FILE_NAME);
        }
    }

    public void error(TaskId id, String message, Throwable th) {
        updateMdcWithTaskLogFilename(id);
        logger.error(format(id, message), th);
        MDC.remove(FileAppender.FILE_NAME);
    }

    public void error(TaskId id, String message) {
        updateMdcWithTaskLogFilename(id);
        logger.error(format(id, message));
        MDC.remove(FileAppender.FILE_NAME);
    }

    public void close(TaskId id) {
        updateMdcWithTaskLogFilename(id);
        logger.debug(format(id, "closing logger"));
        for (Appender appender : (List<Appender>) Collections.list(logger.getAllAppenders())) {
            if (appender != null && appender instanceof FileAppender) {
                appender.close();
            }
        }
        MDC.remove(FileAppender.FILE_NAME);
    }

    private void updateMdcWithTaskLogFilename(TaskId id) {
        MDC.put(FileAppender.FILE_NAME, getTaskLogRelativePath(id));
    }

    public static String getTaskLogRelativePath(TaskId id) {
        return id.getJobId().value() + "/" + id.toString();
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

}
