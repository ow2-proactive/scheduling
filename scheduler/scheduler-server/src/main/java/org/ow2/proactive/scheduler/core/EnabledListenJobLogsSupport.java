package org.ow2.proactive.scheduler.core;

import org.apache.log4j.Appender;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.Logger;
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
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher;
import org.ow2.proactive.scheduler.util.JobLogger;
import org.ow2.proactive.scheduler.util.TaskLogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


class EnabledListenJobLogsSupport extends ListenJobLogsSupport {

    private static final JobLogger jlogger = JobLogger.getInstance();
    private static final TaskLogger tlogger = TaskLogger.getInstance();
    private final Map<JobId, AsyncAppender> jobsToBeLogged = new HashMap<JobId, AsyncAppender>();

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
        jlogger.info(jobId, "cleaning loggers");
    }

    @Override
    synchronized void activeLogsIfNeeded(JobId jobId, TaskLauncher launcher) throws LogForwardingException {
        if (jobsToBeLogged.containsKey(jobId)) {
            launcher.activateLogs(lfs.getAppenderProvider());
        }
    }

    @Override
    synchronized void listenJobLogs(JobId jobId, AppenderProvider appenderProvider)
            throws UnknownJobException {
        jlogger.info(jobId, "listening logs");

        // create the appender to the remote listener
        Appender clientAppender = null;
        try {
            clientAppender = appenderProvider.getAppender();
        } catch (LogForwardingException e) {
            jlogger.error(jobId, "cannot create an appender", e);
            throw new InternalException("Cannot create an appender for job " + jobId, e);
        }

        boolean logIsAlreadyInitialized = jobsToBeLogged.containsKey(jobId);
        initJobLogging(jobId, clientAppender);

        JobResult result = dbManager.loadJobResult(jobId);
        if (result == null) {
            throw new UnknownJobException(jobId);
        }

        // for finished tasks, add logs events "manually"
        Collection<TaskResult> allRes = result.getAllResults().values();
        for (TaskResult tr : allRes) {
            this.flushTaskLogs(tr, clientAppender);
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
        lfs.addAppender(Log4JTaskLogs.getLoggerName(jobId), clientAppender);
    }

    private void flushTaskLogs(TaskResult tr, Appender a) {
        // if taskResult is not awaited, task is terminated
        TaskLogs logs = tr.getOutput();
        if (logs instanceof Log4JTaskLogs) {
            for (LoggingEvent le : ((Log4JTaskLogs) logs).getAllEvents()) {
                // write into socket appender directly to avoid double lines on other listeners
                a.doAppend(le);
            }
        } else {
            // TODO
        }
    }

}
