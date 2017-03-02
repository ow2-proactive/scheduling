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

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobInfoImpl;
import org.ow2.proactive.scheduler.util.ServerJobAndTaskLogs;


public class JobRemoveHandler implements Callable<Boolean> {

    private static final Logger logger = Logger.getLogger(SchedulingService.class);

    private final JobId jobId;

    private final SchedulingService service;

    public JobRemoveHandler(SchedulingService service, JobId jobId) {
        this.service = service;
        this.jobId = jobId;
    }

    private boolean isInFinishedState(InternalJob job) {
        JobStatus status = job.getStatus();
        return status == JobStatus.CANCELED || status == JobStatus.FAILED || status == JobStatus.KILLED;
    }

    @Override
    public Boolean call() {
        long start = 0;

        if (logger.isInfoEnabled()) {
            start = System.currentTimeMillis();
            logger.info("Removing job " + jobId);
        }

        SchedulerDBManager dbManager = service.getInfrastructure().getDBManager();

        InternalJob job = dbManager.loadJobWithTasksIfNotRemoved(jobId);
        TerminationData terminationData;

        // if the context is not in sync with the database
        if (job == null) {
            terminationData = service.getJobs().removeJob(jobId);
        } else {
            // if the job was already finished we just remove it from the context
            if (isInFinishedState(job)) {
                terminationData = service.getJobs().removeJob(jobId);
            } else {
                terminationData = service.getJobs().killJob(jobId);
            }
        }
        service.submitTerminationDataHandler(terminationData);

        // if the job doesn't exist in the DB anymore we can stop here
        if (job == null) {
            return false;
        }

        job.setRemovedTime(System.currentTimeMillis());

        boolean removeFromDb = PASchedulerProperties.JOB_REMOVE_FROM_DB.getValueAsBoolean();

        dbManager.removeJob(jobId, job.getRemovedTime(), removeFromDb);

        if (logger.isInfoEnabled()) {
            logger.info("Job " + jobId + " removed in " + (System.currentTimeMillis() - start) + "ms");
        }

        ServerJobAndTaskLogs.remove(jobId);

        // send event to front-end
        service.getListener()
               .jobStateUpdated(job.getOwner(),
                                new NotificationData<JobInfo>(SchedulerEvent.JOB_REMOVE_FINISHED,
                                                              new JobInfoImpl((JobInfoImpl) job.getJobInfo())));

        service.wakeUpSchedulingThread();

        return true;
    }

}
