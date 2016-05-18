package org.ow2.proactive.scheduler.core;

import java.util.concurrent.Callable;

import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobInfoImpl;
import org.ow2.proactive.scheduler.util.ServerJobAndTaskLogs;
import org.apache.log4j.Logger;


public class JobRemoveHandler implements Callable<Boolean> {

    private static final Logger logger = Logger.getLogger(SchedulingService.class);

    private final JobId jobId;

    private final SchedulingService service;

    public JobRemoveHandler(SchedulingService service, JobId jobId) {
        this.service = service;
        this.jobId = jobId;
    }

    @Override
    public Boolean call() {
        long start = 0;

        if (logger.isInfoEnabled()) {
            start = System.currentTimeMillis();
            logger.info("Removing job " + jobId);
        }

        SchedulerDBManager dbManager = service.infrastructure.getDBManager();

        TerminationData terminationData = service.jobs.killJob(jobId);
        service.submitTerminationDataHandler(terminationData);
        InternalJob job = dbManager.loadJobWithTasksIfNotRemoved(jobId);

        if (job == null) {
            return false;
        }

        job.setRemovedTime(System.currentTimeMillis());

        boolean removeFromDb = PASchedulerProperties.JOB_REMOVE_FROM_DB.getValueAsBoolean();
        dbManager.removeJob(jobId, job.getRemovedTime(), removeFromDb);

        if (logger.isInfoEnabled()) {
            logger.info("Job " + jobId + " removed in " + (System.currentTimeMillis() - start) + "ms");
        }

        ServerJobAndTaskLogs.remove(jobId, job.getHMTasks().keySet());

        // send event to front-end
        service.listener.jobStateUpdated(job.getOwner(), new NotificationData<JobInfo>(
                SchedulerEvent.JOB_REMOVE_FINISHED, new JobInfoImpl((JobInfoImpl) job.getJobInfo())));

        service.wakeUpSchedulingThread();

        return true;
    }

}
