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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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

    private final List<JobId> jobIds;

    private final SchedulingService service;

    public JobRemoveHandler(SchedulingService service, JobId jobId) {
        this.service = service;
        this.jobIds = Collections.singletonList(jobId);
    }

    public JobRemoveHandler(SchedulingService service, List<JobId> jobIds) {
        this.service = service;
        this.jobIds = jobIds;
    }

    private boolean isInFinishedState(InternalJob job) {
        JobStatus status = job.getStatus();
        return status == JobStatus.CANCELED || status == JobStatus.FAILED || status == JobStatus.KILLED;
    }

    @Override
    public Boolean call() {
        boolean allJobsWereRemoved;
        try {
            long start = 0;

            if (jobIds.isEmpty()) {
                logger.info("No jobs to remove. You should not create JobRemoveHandler with empty list of ids.");
                return false;
            }
            if (logger.isInfoEnabled()) {
                start = System.currentTimeMillis();
                logger.info("Removing jobs " + jobIds.stream().map(JobId::value).collect(Collectors.joining(", ")));
            }

            SchedulerDBManager dbManager = service.getInfrastructure().getDBManager();

            List<InternalJob> jobs = dbManager.loadJobWithTasksIfNotRemoved(jobIds.toArray(new JobId[0]));

            // if the context is not in sync with the database
            if (jobs.size() != jobIds.size()) {
                for (JobId jobId : jobIds) {
                    service.submitTerminationDataHandler(service.getJobs().removeJob(jobId));
                }
                allJobsWereRemoved = false;
            } else {
                long removedTime = System.currentTimeMillis();

                for (InternalJob job : jobs) {
                    // if the job was already finished we just remove it from the context
                    if (isInFinishedState(job)) {
                        service.submitTerminationDataHandler(service.getJobs().removeJob(job.getId()));
                    } else {
                        service.submitTerminationDataHandler(service.getJobs().killJob(job.getId()));
                    }
                    job.setRemovedTime(removedTime);
                }

                boolean removeFromDb = PASchedulerProperties.JOB_REMOVE_FROM_DB.getValueAsBoolean();
                dbManager.removeJob(jobIds, removedTime, removeFromDb);

                for (InternalJob job : jobs) {
                    ServerJobAndTaskLogs.getInstance().remove(job.getId(), job.getOwner(), job.getCredentials());
                    if (logger.isInfoEnabled()) {
                        logger.info("Job " + job.getId() + " removed in " + (System.currentTimeMillis() - start) +
                                    "ms");
                    }
                    // send event to front-end
                    service.getListener()
                           .jobStateUpdated(job.getOwner(),
                                            new NotificationData<>(SchedulerEvent.JOB_REMOVE_FINISHED,
                                                                   new JobInfoImpl((JobInfoImpl) job.getJobInfo())));
                }
                allJobsWereRemoved = true;
            }

            service.wakeUpSchedulingThread();
        } catch (Exception e) {
            logger.error("Error while removing list of jobs (" +
                         jobIds.stream().map(JobId::value).collect(Collectors.joining(", ")) + ") due to : " +
                         e.getMessage(), e);
            throw e;
        }

        return allJobsWereRemoved;
    }

}
