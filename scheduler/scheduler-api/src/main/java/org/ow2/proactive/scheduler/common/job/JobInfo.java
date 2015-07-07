/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.job;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * JobInfo provides some informations about a job.<br>
 * These informations and only them are able to change inside the job.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public interface JobInfo extends Serializable {

    /**
     * To get the jobId
     *
     * @return the jobId
     */
    JobId getJobId();

    /**
     * Get job owner 
     * 
     * @return job owner
     */
    String getJobOwner();

    /**
     * To get the finishedTime
     *
     * @return the finishedTime
     */
    long getFinishedTime();

    /**
     * To get the removedTime
     *
     * @return the removedTime
     */
    long getRemovedTime();

    /**
     * To get the startTime
     *
     * @return the startTime
     */
    long getStartTime();

    /**
     * To get the submittedTime
     *
     * @return the submittedTime
     */
    long getSubmittedTime();

    /**
     * To get the totalNumberOfTasks
     *
     * @return the totalNumberOfTasks
     */
    int getTotalNumberOfTasks();

    /**
     * To get the numberOfFinishedTasks
     *
     * @return the numberOfFinishedTasks
     */
    int getNumberOfFinishedTasks();

    /**
     * To get the numberOfPendingTasks
     *
     * @return the numberOfPendingTasks
     */
    int getNumberOfPendingTasks();

    /**
     * To get the numberOfRunningTasks
     *
     * @return the numberOfRunningTasks
     */
    int getNumberOfRunningTasks();

    /**
     * To get the priority.
     *
     * @return the priority.
     */
    JobPriority getPriority();

    /**
     * Return the status of the job.
     *
     * @return the status of the job.
     */
    JobStatus getStatus();

    /**
     * Get the toBeRemoved property.
     *
     * @return the toBeRemoved property.
     */
    boolean isToBeRemoved();

}
