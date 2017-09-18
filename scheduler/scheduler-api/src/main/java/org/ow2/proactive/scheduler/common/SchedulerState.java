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
public interface SchedulerState<T extends JobState> extends Serializable {

    /**
     * Get the finished Jobs list
     *
     * @return the finished Jobs list
     */
    Vector<T> getFinishedJobs();

    /**
     * Get the pending Jobs list
     *
     * @return the pending Jobs list
     */
    Vector<T> getPendingJobs();

    /**
     * Get the running Jobs list
     *
     * @return the running Jobs list
     */
    Vector<T> getRunningJobs();

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
    void update(T jobState);
}
