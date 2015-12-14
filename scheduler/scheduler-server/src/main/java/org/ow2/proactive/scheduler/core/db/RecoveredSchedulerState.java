/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core.db;

import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.core.SchedulerStateImpl;
import org.ow2.proactive.scheduler.job.ClientJobState;
import org.ow2.proactive.scheduler.job.InternalJob;

import java.util.List;
import java.util.Vector;

public class RecoveredSchedulerState {

    private final Vector<InternalJob> pendingJobs;

    private final Vector<InternalJob> runningJobs;

    private final Vector<InternalJob> finishedJobs;

    private final SchedulerStateImpl schedulerState;

    public RecoveredSchedulerState(Vector<InternalJob> pendingJobs, Vector<InternalJob> runningJobs,
                                   Vector<InternalJob> finishedJobs) {
        this.pendingJobs = pendingJobs;
        this.runningJobs = runningJobs;
        this.finishedJobs = finishedJobs;
        schedulerState = new SchedulerStateImpl();
        schedulerState.setPendingJobs(convertToClientJobState(pendingJobs));
        schedulerState.setRunningJobs(convertToClientJobState(runningJobs));
        schedulerState.setFinishedJobs(convertToClientJobState(finishedJobs));
    }

    public Vector<InternalJob> getPendingJobs() {
        return pendingJobs;
    }

    public Vector<InternalJob> getRunningJobs() {
        return runningJobs;
    }

    public Vector<InternalJob> getFinishedJobs() {
        return finishedJobs;
    }

    public SchedulerStateImpl getSchedulerState() {
        return schedulerState;
    }

    private Vector<JobState> convertToClientJobState(List<InternalJob> jobs) {
        Vector<JobState> result = new Vector<>(jobs.size());
        for (InternalJob internalJob : jobs) {
            result.add(new ClientJobState(internalJob));
        }
        return result;
    }

}
