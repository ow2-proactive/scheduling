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
package org.ow2.proactive.scheduler.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.ow2.proactive.scheduler.common.exception.InternalException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingService;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.util.JobLogger;
import org.ow2.proactive.scheduler.util.TaskLogger;


class EnabledListenJobLogsSupport extends ListenJobLogsSupport {

    private static final JobLogger jlogger = JobLogger.getInstance();

    private static final TaskLogger tlogger = TaskLogger.getInstance();

    private final Set<JobId> jobsToBeLogged = new HashSet<>();

    private final SchedulerDBManager dbManager;

    private final LogForwardingService lfs;

    private final LiveJobs liveJobs;

    EnabledListenJobLogsSupport(SchedulerDBManager dbManager, LiveJobs liveJobs, String providerClassname)
            throws LogForwardingException {
        this.dbManager = dbManager;
        this.liveJobs = liveJobs;
        this.lfs = new LogForwardingService(providerClassname);
        this.lfs.initialize();
        logger.info("Initialized log forwarding service at " + this.lfs.getServerURI());
    }

    @Override
    void shutdown() {
        try {
            lfs.terminate();
        } catch (LogForwardingException e) {
            logger.error("Cannot terminate logging service : " + e.getMessage());
            logger.error("", e);
        }
    }

    @Override
    synchronized void cleanLoggers(JobId jobId) {
        jobsToBeLogged.remove(jobId);
        jlogger.debug(jobId, "cleaning loggers");
        lfs.removeAllAppenders(Log4JTaskLogs.getLoggerName(jobId));
    }

    @Override
    synchronized void activeLogsIfNeeded(JobId jobId, TaskLauncher launcher) throws LogForwardingException {
        if (jobsToBeLogged.contains(jobId)) {
            launcher.activateLogs(lfs.getAppenderProvider());
        }
    }

    @Override
    synchronized void listenJobLogs(JobId jobId, AppenderProvider appenderProvider) throws UnknownJobException {
        jlogger.info(jobId, "listening logs");

        // create the appender to the remote listener
        Appender clientAppender = null;
        try {
            clientAppender = appenderProvider.getAppender();
        } catch (LogForwardingException e) {
            jlogger.error(jobId, "cannot create an appender", e);
            throw new InternalException("Cannot create an appender for job " + jobId, e);
        }

        boolean logIsAlreadyInitialized = jobsToBeLogged.contains(jobId);
        initJobLogging(jobId, clientAppender);

        JobResult result = dbManager.loadJobResult(jobId);
        if (result == null) {
            throw new UnknownJobException(jobId);
        }

        // for finished tasks, add logs events "manually"
        Collection<TaskResult> allRes = result.getAllResults().values();
        for (TaskResult tr : allRes) {
            this.flushTaskLogs(tr, clientAppender, jobId);
        }

        for (RunningTaskData taskData : liveJobs.getRunningTasks(jobId)) {
            try {
                TaskLauncher taskLauncher = taskData.getLauncher();
                if (logIsAlreadyInitialized) {
                    taskLauncher.getStoredLogs(appenderProvider);
                } else {
                    taskLauncher.activateLogs(lfs.getAppenderProvider());
                }
            } catch (Exception e) {
                tlogger.error(taskData.getTask().getId(), "cannot create an appender provider", e);
            }
        }

        if (!result.getJobInfo().getStatus().isJobAlive()) {
            jlogger.info(jobId, "cleaning loggers for already finished job");
            cleanLoggers(jobId);
        }
    }

    private void initJobLogging(JobId jobId, Appender clientAppender) {
        jobsToBeLogged.add(jobId);
        lfs.addAppender(Log4JTaskLogs.getLoggerName(jobId), clientAppender);
    }

    private void flushTaskLogs(TaskResult tr, Appender a, JobId jobId) {
        // if taskResult is not awaited, task is terminated
        TaskLogs logs = tr.getOutput();
        if (logs instanceof Log4JTaskLogs) {
            for (LoggingEvent le : ((Log4JTaskLogs) logs).getAllEvents()) {
                // write into socket appender directly to avoid double lines on other listeners
                a.doAppend(le);
            }
        } else {
            a.doAppend(createLoggingEvent(jobId, logs.getStdoutLogs(false), Level.INFO));
            a.doAppend(createLoggingEvent(jobId, logs.getStderrLogs(false), Level.DEBUG));
        }
    }

    private LoggingEvent createLoggingEvent(JobId jobId, String logs, Priority level) {
        return new LoggingEvent(null, Logger.getLogger(Log4JTaskLogs.getLoggerName(jobId)), level, logs, null);
    }

}
