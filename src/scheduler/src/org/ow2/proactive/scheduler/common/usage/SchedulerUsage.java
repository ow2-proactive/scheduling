/*
 *  *
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.usage;

import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.usage.JobUsage;

import java.util.Date;
import java.util.List;

import org.objectweb.proactive.annotation.PublicAPI;


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
     * i.e startDate <= job.finishedTime <= endDate.
     *</p>
     * @param startDate must not be null, inclusive
     * @param endDate must not be null, inclusive
     * @return a list of {@link JobUsage} objects where job finished times are between start date and end date
     * @throws NotConnectedException if the caller is not connecter
     * @throws PermissionException if the caller hasn't the permission to call this method
     */
    List<JobUsage> getMyAccountUsage(Date startDate, Date endDate) throws NotConnectedException,
            PermissionException;
}
