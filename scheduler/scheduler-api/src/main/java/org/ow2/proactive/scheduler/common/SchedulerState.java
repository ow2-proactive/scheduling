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
package org.ow2.proactive.scheduler.common;

import java.io.Serializable;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.JobState;


/**
 * This interface is a representation of the whole scheduler jobs list state.<br>
 * It is basically represented by 3 lists of jobs, and their scheduling status.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
@XmlRootElement(name = "schedulerstate")
public interface SchedulerState extends Serializable {

    /**
     * Get the finished Jobs list
     *
     * @return the finished Jobs list
     */
    Vector<JobState> getFinishedJobs();

    /**
     * Get the pending Jobs list
     *
     * @return the pending Jobs list
     */
    Vector<JobState> getPendingJobs();

    /**
     * Get the running Jobs list
     *
     * @return the running Jobs list
     */
    Vector<JobState> getRunningJobs();

    /**
     * Get the status of the scheduler
     *
     * @return the status of the scheduler
     */
    @XmlElement(name = "status")
    SchedulerStatus getStatus();

    /**
     * Returns the list of connected users.
     *
     * @return the list of connected users.
     */
    SchedulerUsers getUsers();

    /**
     * Updates the scheduler state given the event passed as a parameter
     */
    void update(SchedulerEvent eventType);

    /**
     * Updates the scheduler state given the event passed as a parameter
     */
    void update(NotificationData<?> notification);

    /**
     * Updates the scheduler state given the event passed as a parameter
     */
    void update(JobState jobState);
}
