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
package org.ow2.proactive.scheduler.util;

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

    private void updateMdcWithTaskLogFilename(TaskId id) {
        MDC.put(FileAppender.FILE_NAME, getTaskLogFilename(id));
    }

    public static String getTaskLogFilename(TaskId id) {
        return id.getJobId().value() + "/" + id.toString();
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

}
