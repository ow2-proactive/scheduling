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
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobInfoImpl;


/**
 * Handles the immediate deletion of the Job in the memory context and
 * schedules the Job for deletion by the Housekeeping mechanism.
 *
 * @author ActiveEon Team
 * @since 15/02/17
 */
public class HousekeepingHandler implements Callable<Boolean> {

    private static final Logger logger = Logger.getLogger(HousekeepingHandler.class);

    private final JobId jobId;

    private final SchedulingService service;

    public HousekeepingHandler(SchedulingService service, JobId jobId) {
        this.service = service;
        this.jobId = jobId;
    }

    @Override
    public Boolean call() throws Exception {
        long start = 0;

        if (logger.isInfoEnabled()) {
            start = System.currentTimeMillis();
            logger.info("HOUSEKEEPING Scheduling job " + jobId + " for removal");
        }

        SchedulerDBManager dbManager = service.getInfrastructure().getDBManager();
        InternalJob job = dbManager.loadJobWithTasksIfNotRemoved(jobId);
        if (job == null) {
            return false;
        }
        job.setRemovedTime(System.currentTimeMillis());
        this.service.getJobsToDeleteFromDB().offer(this.jobId);

        if (logger.isInfoEnabled()) {
            logger.info("HOUSEKEEPING Job " + jobId + " scheduled for removal " + "in " +
                        (System.currentTimeMillis() - start) + "ms");
        }

        service.getListener()
               .jobStateUpdated(job.getOwner(),
                                new NotificationData<JobInfo>(SchedulerEvent.JOB_REMOVE_FINISHED,
                                                              new JobInfoImpl((JobInfoImpl) job.getJobInfo())));

        return true;
    }
}
