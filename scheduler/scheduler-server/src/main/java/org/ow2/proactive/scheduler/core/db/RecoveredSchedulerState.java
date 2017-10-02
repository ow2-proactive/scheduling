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

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.core.SchedulerStateImpl;
import org.ow2.proactive.scheduler.core.SchedulingService;
import org.ow2.proactive.scheduler.job.ClientJobState;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


/**
 * @author ActiveEon Team
 */
public class RecoveredSchedulerState {

    private static final Logger logger = Logger.getLogger(RecoveredSchedulerState.class);

    private final Vector<InternalJob> pendingJobs;

    private final Vector<InternalJob> runningJobs;

    private final Vector<InternalJob> finishedJobs;

    private final SchedulerStateImpl<ClientJobState> schedulerState;

    public RecoveredSchedulerState(Vector<InternalJob> pendingJobs, Vector<InternalJob> runningJobs,
            Vector<InternalJob> finishedJobs) {
        this.pendingJobs = pendingJobs;
        this.runningJobs = runningJobs;
        this.finishedJobs = finishedJobs;
        schedulerState = new SchedulerStateImpl<>();
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

    public SchedulerStateImpl<ClientJobState> getSchedulerState() {
        return schedulerState;
    }

    private Vector<ClientJobState> convertToClientJobState(List<InternalJob> jobs) {
        Vector<ClientJobState> result = new Vector<>(jobs.size());
        for (InternalJob internalJob : jobs) {
            result.add(new ClientJobState(internalJob));
        }
        return result;
    }

    public void enableLiveLogsForRunningTasks(SchedulingService schedulingService) {
        logger.info("Enable live logs for running tasks if needed");
        for (InternalJob job : runningJobs) {
            for (InternalTask task : job.getITasks()) {
                enableLiveLogsForRunningTask(schedulingService, job, task);
            }
        }
    }

    private void enableLiveLogsForRunningTask(SchedulingService schedulingService, InternalJob job, InternalTask task) {
        if (task.getStatus() == TaskStatus.RUNNING && task.getExecuterInformation() != null) {
            try {
                schedulingService.getListenJobLogsSupport()
                                 .activeLogsIfNeeded(job.getId(), task.getExecuterInformation().getLauncher());
            } catch (LogForwardingException e) {
                logger.warn("Failed to activate logs for task " + task.getId(), e);
            }
        }
    }

}
