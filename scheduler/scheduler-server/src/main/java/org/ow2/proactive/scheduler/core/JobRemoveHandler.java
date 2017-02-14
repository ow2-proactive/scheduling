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

        logger.info("HOUSEKEEPING# Job" + jobId + " set for deletion");
        //dbManager.removeJob(jobId, job.getRemovedTime(), removeFromDb);
        this.service.jobsToDeleteFromDB.offer(this.jobId);


        if (logger.isInfoEnabled()) {
            logger.info("Job " + jobId + " removed in " + (System.currentTimeMillis() - start) + "ms");
        }

        ServerJobAndTaskLogs.remove(jobId);

        // send event to front-end
        service.listener.jobStateUpdated(job.getOwner(),
                                         new NotificationData<JobInfo>(SchedulerEvent.JOB_REMOVE_FINISHED,
                                                                       new JobInfoImpl((JobInfoImpl) job.getJobInfo())));

        service.wakeUpSchedulingThread();

        return true;
    }

}
