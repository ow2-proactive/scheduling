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

import static org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties.LOG4J_ASYNC_APPENDER_CACHE_ENABLED;
import static org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties.LOG4J_ASYNC_APPENDER_ENABLED;
import static org.ow2.proactive.scheduler.core.properties.PASchedulerProperties.SCHEDULER_JOB_LOGS_LOCATION;
import static org.ow2.proactive.scheduler.core.properties.PASchedulerProperties.getAbsolutePath;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.util.TaskLoggerRelativePathGenerator;
import org.ow2.proactive.scheduler.core.SchedulerSpacesSupport;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.utils.FileUtils;
import org.ow2.proactive.utils.appenders.AsynchChachedFileAppender;
import org.ow2.proactive.utils.appenders.AsynchFileAppender;
import org.ow2.proactive.utils.appenders.FileAppender;
import org.ow2.proactive.utils.appenders.SynchFileAppender;


public class ServerJobAndTaskLogs {

    private static final Logger logger = Logger.getLogger(ServerJobAndTaskLogs.class);

    private static final TaskLogger tlogger = TaskLogger.getInstance();

    private static final int MAX_REMOVAL_ATTEMPTS = 10;

    private SchedulerSpacesSupport spacesSupport = null;

    private static ServerJobAndTaskLogs activeInstance = null;

    public static ServerJobAndTaskLogs getInstance() {
        return LazyHolder.INSTANCE;
    }

    public static ServerJobAndTaskLogs getActiveInstance() {
        if (activeInstance == null) {
            try {
                activeInstance = PAActiveObject.turnActive(getInstance());
            } catch (Exception e) {
                logger.error("Could not create ServerJobAndTaskLogs instance", e);
            }
        }
        return activeInstance;
    }

    static final JobLogger jlogger = JobLogger.getInstance();

    public void configure() {
        if (logsLocationIsSet()) {
            if (isCleanStart()) {
                removeLogsDirectory();

            }
            removeAllFileAppendersToLogger(JobLogger.class);
            removeAllFileAppendersToLogger(TaskLogger.class);
            addNewFileAppenderToLoggerFor(JobLogger.class);
            addNewFileAppenderToLoggerFor(TaskLogger.class);
        }
    }

    public void setSpacesSupport(SchedulerSpacesSupport spacesSupport) {
        this.spacesSupport = spacesSupport;
    }

    public static void terminateActiveInstance() {
        if (activeInstance != null) {
            PAActiveObject.terminateActiveObject(activeInstance, true);
        }
    }

    public String getTaskLog(TaskId id) {
        String result = readLog(TaskLogger.getTaskLogRelativePath(id));
        return result != null ? result : "Cannot retrieve logs for task " + id;
    }

    public String getJobLog(JobId jobId, Set<TaskId> tasks) {
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

    public void remove(JobId jobId, String jobOwner) {
        jlogger.close(jobId);
        removeFolderLog(jobId.value());
        removeVisualizationFile(jobId.value());
        removePreciousLogs(jobId, jobOwner);
    }

    private void removePreciousLogs(JobId jobId, String jobOwner) {
        if (spacesSupport == null) {
            logger.warn("DataSpaces not initialized, cannot remove precious logs for job " + jobId);
            return;
        }
        try {
            Validate.notBlank(jobOwner);
            DataSpacesFileObject userspace = spacesSupport.getUserSpace(jobOwner);
            Validate.notNull(userspace,
                             "Cannot find user space for user " + jobOwner +
                                        ". User spaces are probably not configured properly.");
            deleteAllLogFiles(jobId, userspace);
            deleteLogsFolderIfEmpty(jobId, userspace);

        } catch (Exception e) {
            logger.warn("Error occurred when trying to remove precious logs for job " + jobId, e);
        }
    }

    private void deleteAllLogFiles(JobId jobId, DataSpacesFileObject userspace) throws FileSystemException {
        userspace.refresh();
        for (DataSpacesFileObject logFileObject : userspace.findFiles(new FileSelector(TaskLoggerRelativePathGenerator.getIncludePatternForAllLogFiles(jobId)))) {
            if (logger.isDebugEnabled()) {
                logger.debug("Deleting file : " + logFileObject.getRealURI());
            }
            logFileObject.delete();
        }
    }

    private void deleteLogsFolderIfEmpty(JobId jobId, DataSpacesFileObject userspace) throws FileSystemException {
        DataSpacesFileObject logsFolder = userspace.resolveFile(TaskLoggerRelativePathGenerator.getContainingFolderForLogFiles(jobId));
        if (logsFolder != null) {
            logsFolder.refresh();
            if (logsFolder.exists() && logsFolder.getChildren().isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Deleting empty folder : " + logsFolder.getRealURI());
                }
                logsFolder.delete();
            }
        }
    }

    private void removeFolderLog(String path) {
        if (logsLocationIsSet()) {
            String logsLocation = getLogsLocation();
            File logFolder = new File(logsLocation, path);
            org.apache.commons.io.FileUtils.deleteQuietly(logFolder);

            int nbAttempts = 1;
            try {
                while (logFolder.exists() && nbAttempts <= MAX_REMOVAL_ATTEMPTS) {
                    nbAttempts++;
                    logger.warn("Could not remove logs folder " + logFolder + " , retrying " + nbAttempts + "/" +
                                MAX_REMOVAL_ATTEMPTS);
                    displayFolderContentsAndCleanLoggers(logFolder);
                    boolean success = org.apache.commons.io.FileUtils.deleteQuietly(logFolder);
                    if (!success) {
                        Thread.sleep(1000);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void displayFolderContentsAndCleanLoggers(File logFolder) {
        for (File file : org.apache.commons.io.FileUtils.listFiles(logFolder, null, true)) {
            // close eventually task logger
            if (file.getName().contains("t")) {
                try {
                    TaskId taskid = TaskIdImpl.makeTaskId(file.getName());
                    tlogger.close(taskid);
                } catch (Exception e) {
                    //ignored
                }

            }
            logger.warn("Remaining file or folder : " + file);
        }
    }

    /**
     * Remove visualization file created by rest server (if present)
     * see org.ow2.proactive_grid_cloud_portal.studio.StudioRest.updateVisualization
     */
    private void removeVisualizationFile(String jobId) {
        File visualizationFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "job_" + jobId +
                                          ".zip.html");
        org.apache.commons.io.FileUtils.deleteQuietly(visualizationFile);
    }

    private boolean logsLocationIsSet() {
        return SCHEDULER_JOB_LOGS_LOCATION.isSet();
    }

    // public for testing
    public String getLogsLocation() {
        return getAbsolutePath(SCHEDULER_JOB_LOGS_LOCATION.getValueAsString());
    }

    private boolean isCleanStart() {
        return PASchedulerProperties.SCHEDULER_DB_HIBERNATE_DROPDB.getValueAsBoolean();
    }

    private String readLog(String filename) {
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

    private String readFile(File file) {
        if (file.exists()) {
            try {
                return org.apache.commons.io.FileUtils.readFileToString(file);
            } catch (IOException e) {
                logger.warn(e);
            }
        }
        return null;
    }

    void removeLogsDirectory() {
        String logsLocation = getLogsLocation();
        logger.info("Removing logs " + logsLocation);

        boolean folderRemoved = org.apache.commons.io.FileUtils.deleteQuietly(new File(logsLocation));
        if (!folderRemoved) {
            logger.error("Could not remove logs folder");
        }

        // infinite remove retries breaks the tests
        //        try {
        //            while (!org.apache.commons.io.FileUtils.deleteQuietly(new File(logsLocation))) {
        //                logger.warn("Could not delete folder " + logsLocation + " retrying");
        //                Thread.sleep(1000);
        //            }
        //        } catch (InterruptedException e) {
        //            Thread.currentThread().interrupt();
        //        }
    }

    private void addNewFileAppenderToLoggerFor(Class<?> cls) {
        Logger jobLogger = Logger.getLogger(cls);
        FileAppender appender = createFileAppender();
        jobLogger.addAppender(appender);
    }

    private void removeAllFileAppendersToLogger(Class<?> cls) {
        Logger classLogger = Logger.getLogger(cls);
        List<Appender> appendersToRemove = new ArrayList<>();
        for (Appender appender : (List<Appender>) Collections.list(classLogger.getAllAppenders())) {
            if (appender instanceof FileAppender) {
                appendersToRemove.add(appender);
            }
        }
        for (Appender appender : appendersToRemove) {
            classLogger.removeAppender(appender);
        }
    }

    public static FileAppender createFileAppender() {
        FileAppender appender;

        if (LOG4J_ASYNC_APPENDER_ENABLED.getValueAsBoolean()) {
            if (LOG4J_ASYNC_APPENDER_CACHE_ENABLED.getValueAsBoolean()) {
                appender = new AsynchChachedFileAppender();
            } else {
                appender = new AsynchFileAppender();
            }
        } else {
            appender = new SynchFileAppender();
        }
        appender.setMaxFileSize(PASchedulerProperties.SCHEDULER_JOB_LOGS_MAX_SIZE.getValueAsString());
        appender.setFilesLocation(getAbsolutePath(SCHEDULER_JOB_LOGS_LOCATION.getValueAsString()));

        return appender;
    }

    private static class LazyHolder {
        private static final ServerJobAndTaskLogs INSTANCE = new ServerJobAndTaskLogs();
    }

}
