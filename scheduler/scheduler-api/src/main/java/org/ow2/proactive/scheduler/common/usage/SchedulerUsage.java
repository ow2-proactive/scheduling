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
package org.ow2.proactive.scheduler.common.usage;

import java.util.Date;
import java.util.List;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;


/**
 * Scheduler interface for accounting information, usage data and statistics.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.4
 */
@PublicAPI
public interface SchedulerUsage {

    /**
     * Returns details on job and task execution times for the caller's executions.
     * <p>
     * Only the jobs finished between the start date and the end date will be returned:
     * i.e {@code startDate <= job.finishedTime <= endDate}.
     *</p>
     * @param startDate must not be null, inclusive
     * @param endDate must not be null, inclusive
     * @return a list of {@link JobUsage} objects where job finished times are between start date and end date
     * @throws NotConnectedException if the caller is not connected
     * @throws PermissionException if the caller hasn't the permission to call this method
     */
    List<JobUsage> getMyAccountUsage(Date startDate, Date endDate) throws NotConnectedException, PermissionException;

    /**
     * Returns details on job and task execution times for a given user's executions.
     * <p>
     * Only the jobs finished between the start date and the end date will be returned:
     * i.e {@code startDate <= job.finishedTime <= endDate}.
     * <p>
     * If user is the same as the caller, then it will fallback to to {@link #getMyAccountUsage(Date, Date)}.
     *
     * @param user must match a username as defined in the Scheduler's users
     * @param startDate must not be null, inclusive
     * @param endDate must not be null, inclusive
     * @return a list of {@link JobUsage} objects where job finished times are between start date and end date
     * @throws NotConnectedException if the caller is not connected
     * @throws PermissionException if the caller hasn't the permission to call this method
     */
    List<JobUsage> getAccountUsage(String user, Date startDate, Date endDate)
            throws NotConnectedException, PermissionException;
}
