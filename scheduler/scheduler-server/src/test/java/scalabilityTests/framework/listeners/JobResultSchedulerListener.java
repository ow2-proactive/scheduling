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
package scalabilityTests.framework.listeners;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.extensions.annotation.RemoteObject;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;

import scalabilityTests.framework.AbstractSchedulerUser;


/**
 * This Scheduler Listener will get the job result as 
 * 	soon as the it receives the job finished notification
 *  from the scheduler 
 * 
 * @author fabratu
 *
 */
@RemoteObject
public class JobResultSchedulerListener extends SimpleSchedulerListener {

    private volatile AbstractSchedulerUser<JobId> daddy;

    protected static final Logger logger = Logger.getLogger(JobResultSchedulerListener.class);

    public JobResultSchedulerListener() {
        this.daddy = null;
    }

    public JobResultSchedulerListener(AbstractSchedulerUser<JobId> daddy) {
        this.daddy = daddy;
    }

    public void setResultFetcher(AbstractSchedulerUser<JobId> daddy) {
        if (!(daddy instanceof StubObject))
            throw new IllegalArgumentException("Must be the stub of a Remote Object!");
        this.daddy = daddy;
    }

    @Override
    public void jobStateUpdatedEvent(NotificationData<JobInfo> jobNotification) {
        super.jobStateUpdatedEvent(jobNotification);
        if (jobNotification.getEventType().equals(SchedulerEvent.JOB_RUNNING_TO_FINISHED) ||
            jobNotification.getEventType().equals(SchedulerEvent.JOB_PENDING_TO_FINISHED)) {
            jobRunningToFinishedEvent(jobNotification);
        }
    }

    private void jobRunningToFinishedEvent(NotificationData<JobInfo> jobNotification) {
        JobId jobId = jobNotification.getData().getJobId();
        logger.trace("Trying to get the job result for job " + jobId);
        try {
            logger.info("The result for job with ID " + jobId + " is " + this.daddy.getJobResult(jobId));
        } catch (SchedulerException e) {
            logger.error("Cannot get the job result for job with id " + jobId + " from the scheduler", e);
        }
    }
}
