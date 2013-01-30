package org.ow2.proactive.scheduler.core;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobInfoImpl;


class JobRemoveHandler implements Callable<Boolean> {

    static final Logger logger = Logger.getLogger(SchedulingService.class);

    private final JobId jobId;

    private final SchedulingService service;

    JobRemoveHandler(SchedulingService service, JobId jobId) {
        this.service = service;
        this.jobId = jobId;
    }

    @Override
    public Boolean call() {
        logger.info("job " + jobId + " removing");

        SchedulerDBManager dbManager = service.infrastructure.getDBManager();

        TerminationData terminationData = service.jobs.killJob(jobId);
        service.submitTerminationDataHandler(terminationData);
        InternalJob job = dbManager.loadJobWithoutTasks(jobId);
        if (job == null) {
            return false;
        }

        job.setRemovedTime(System.currentTimeMillis());
        boolean removeFromDB = PASchedulerProperties.JOB_REMOVE_FROM_DB.getValueAsBoolean();
        dbManager.removeJob(jobId, job.getRemovedTime(), removeFromDB);
        logger.info("job " + jobId + " removed");

        // removing job logs
        if (PASchedulerProperties.SCHEDULER_JOB_LOGS_LOCATION.isSet()) {
            String logsLocation = PASchedulerProperties
                    .getAbsolutePath(PASchedulerProperties.SCHEDULER_JOB_LOGS_LOCATION.getValueAsString());
            for (TaskId t : job.getHMTasks().keySet()) {
                File f = new File(logsLocation + t);
                if (f.exists()) {
                    logger.info("Removing file " + f.getAbsolutePath());
                    f.delete();
                }
            }
            File f = new File(logsLocation + jobId);
            if (f.exists()) {
                logger.info("Removing file " + f.getAbsolutePath());
                f.delete();
            }
        }

        //send event to front-end
        service.listener.jobStateUpdated(job.getOwner(), new NotificationData<JobInfo>(
            SchedulerEvent.JOB_REMOVE_FINISHED, new JobInfoImpl((JobInfoImpl) job.getJobInfo())));

        return true;
    }

}
