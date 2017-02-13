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
import org.objectweb.proactive.extensions.annotation.RemoteObject;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;

import scalabilityTests.framework.AbstractSchedulerUser;


/**
 * This decorator also gets the job result for the jobs it monitors
 * 
 * @author fabratu
 *
 */
@RemoteObject
public class JobResultDecorator extends JobMonitoringDecorator {

    private volatile AbstractSchedulerUser<JobId> daddy;

    protected static final Logger logger = Logger.getLogger(JobResultDecorator.class);

    public JobResultDecorator() {
    }

    public JobResultDecorator(SchedulerEventListener listener, AbstractSchedulerUser<JobId> daddy) {
        super(listener);
        this.daddy = daddy;
    }

    @Override
    protected void jobRunningToFinishedEvent(NotificationData<JobInfo> event) {
        super.jobRunningToFinishedEvent(event);

        JobId jobId = event.getData().getJobId();
        if (!this.mapOfJobs.containsKey(jobId)) {
            logger.trace("We are not waiting for the result of job ID " + jobId);
            return;
        }

        logger.trace("Trying to get the job result for job " + jobId);
        try {
            logger.info("The result for job with ID " + jobId + " is " + this.daddy.getJobResult(jobId));
        } catch (SchedulerException e) {
            logger.error("Cannot get the job result for job with id " + jobId + " from the scheduler", e);
        }

        // we have the result => stop monitoring this job
        this.stopMonitoring(jobId);
    }

}
