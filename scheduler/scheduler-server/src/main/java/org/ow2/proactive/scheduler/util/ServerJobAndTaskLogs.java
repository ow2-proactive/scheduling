/*
 *  *
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 *  * $$ACTIVEEON_CONTRIBUTOR$$
 */

package org.ow2.proactive.scheduler.util;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.utils.FileUtils;
import org.ow2.proactive.utils.appenders.FileAppender;


public class ServerJobAndTaskLogs {

    private static final Logger logger = Logger.getLogger(ServerJobAndTaskLogs.class);

    public static void configure() {
        if (logsLocationIsSet()) {
            if (isCleanStart()) {
                removeLogsDirectory();

            }
            addNewFileAppenderToLoggerFor(JobLogger.class);
            addNewFileAppenderToLoggerFor(TaskLogger.class);
        }
    }

    public static String getTaskLog(TaskId id) {
        String result = readLog(TaskLogger.getTaskLogRelativePath(id));
        return result != null ? result : "Cannot retrieve logs for task " + id;
    }

    public static String getJobLog(JobId jobId, Set<TaskId> tasks) {
        String jobLog = readLog(JobLogger.getJobLogRelativePath(jobId));
        if (jobLog == null) {
            return "Cannot retrieve logs for job " + jobId;
        }

        StringBuilder result = new StringBuilder();
        result.append("================= Job ").append(jobId).append(" logs =================\n");
        result.append(jobLog);
        for (TaskId taskId : tasks) {
            result.append("\n================ Task ").append(taskId).append(" logs =================\n");
            result.append(getTaskLog(taskId));
        }
        return result.toString();

    }

    public static void remove(JobId jobId) {
        removeFolderLog(jobId.value());
    }

    private static void removeFolderLog(String path) {
        if (logsLocationIsSet()) {
            String logsLocation = getLogsLocation();
            File logFolder = new File(logsLocation, path);
            org.apache.commons.io.FileUtils.deleteQuietly(logFolder);
        }

    }

    private static boolean logsLocationIsSet() {
        return PASchedulerProperties.SCHEDULER_JOB_LOGS_LOCATION.isSet();
    }

    // public for testing
    public static String getLogsLocation() {
        return PASchedulerProperties
                .getAbsolutePath(PASchedulerProperties.SCHEDULER_JOB_LOGS_LOCATION.getValueAsString());
    }

    private static boolean isCleanStart() {
        return PASchedulerProperties.SCHEDULER_DB_HIBERNATE_DROPDB.getValueAsBoolean();
    }

    private static String readLog(String filename) {
        String result = null;
        for (String suffix : new String[] { ".1", "" }) {
            String contents = readFile(new File(getLogsLocation(), filename + suffix));
            if (contents != null) {
                if (result == null) {
                    result = contents;
                } else {
                    result += contents;
                }
            }
        }
        return result;
    }

    private static String readFile(File file) {
        if (file.exists()) {
            try {
                return org.apache.commons.io.FileUtils.readFileToString(file);
            } catch (IOException e) {
                logger.warn(e);
            }
        }
        return null;
    }

    static void removeLogsDirectory() {
        String logsLocation = getLogsLocation();
        logger.info("Removing logs " + logsLocation);
        FileUtils.removeDir(new File(logsLocation));
    }

    private static void addNewFileAppenderToLoggerFor(Class<?> cls) {
        Logger jobLogger = Logger.getLogger(cls);
        FileAppender appender = createFileAppender();
        jobLogger.addAppender(appender);
    }

    private static FileAppender createFileAppender() {
        FileAppender appender = new FileAppender();
        if (PASchedulerProperties.SCHEDULER_JOB_LOGS_MAX_SIZE.isSet()) {
            appender.setMaxFileSize(PASchedulerProperties.SCHEDULER_JOB_LOGS_MAX_SIZE.getValueAsString());
        }
        appender.setFilesLocation(getLogsLocation());
        return appender;
    }

}
