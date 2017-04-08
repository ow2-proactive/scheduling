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
package org.ow2.proactive.scheduler.core.db;

import java.util.List;
import java.util.Vector;

import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.core.SchedulerStateImpl;
import org.ow2.proactive.scheduler.job.ClientJobState;
import org.ow2.proactive.scheduler.job.InternalJob;


/**
 * @author ActiveEon Team
 */
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
