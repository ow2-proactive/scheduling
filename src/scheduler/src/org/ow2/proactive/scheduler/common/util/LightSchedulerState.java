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
package org.ow2.proactive.scheduler.common.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.job.UserIdentification;


/**
 * Subset of org.ow2.proactive.scheduler.common.SchedulerState
 * that is small and fast to serialize
 *
 */
public class LightSchedulerState implements Serializable {

    private static final long serialVersionUID = 32L;

    private List<UserJobInfo> jobs = null;

    private List<UserIdentification> users = null;

    private SchedulerStatus status = null;

    public LightSchedulerState() {
        this.jobs = new ArrayList<UserJobInfo>();
        this.users = new ArrayList<UserIdentification>();
        this.status = SchedulerStatus.STOPPED;
    }

    public LightSchedulerState(List<UserJobInfo> jobs, List<UserIdentification> users, SchedulerStatus status) {
        this.jobs = jobs;
        this.users = users;
        this.status = status;
    }

    public List<UserJobInfo> getJobs() {
        return jobs;
    }

    public List<UserIdentification> getUsers() {
        return users;
    }

    public SchedulerStatus getStatus() {
        return status;
    }

}
