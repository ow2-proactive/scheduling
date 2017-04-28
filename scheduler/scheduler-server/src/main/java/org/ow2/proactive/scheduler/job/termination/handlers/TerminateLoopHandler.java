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
package org.ow2.proactive.scheduler.job.termination.handlers;

import java.util.Date;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.core.SchedulerStateUpdate;
import org.ow2.proactive.scheduler.job.ChangedTasksInfo;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.policy.ISO8601DateUtil;

import it.sauronsoftware.cron4j.InvalidPatternException;
import it.sauronsoftware.cron4j.Predictor;


public class TerminateLoopHandler {

    private static final Logger LOGGER = Logger.getLogger(TerminateLoopHandler.class);

    private final InternalJob internalJob;

    public TerminateLoopHandler(InternalJob internalJob) {
        this.internalJob = internalJob;
    }

    public boolean terminateLoopTask(FlowAction action, InternalTask initiator, ChangedTasksInfo changesInfo,
            SchedulerStateUpdate frontend) {
        // find the target of the loop
        InternalTask target = null;
        if (action.getTarget().equals(initiator.getName())) {
            target = initiator;
        } else {
            target = internalJob.findTaskUp(action.getTarget(), initiator);
        }
        boolean replicateForNextLoopIteration = internalJob.replicateForNextLoopIteration(initiator,
                                                                                          target,
                                                                                          changesInfo,
                                                                                          frontend,
                                                                                          action);
        if (replicateForNextLoopIteration && action.getCronExpr() != null) {
            for (TaskId tid : changesInfo.getNewTasks()) {
                InternalTask newTask = internalJob.getIHMTasks().get(tid);
                try {
                    Date startAt = (new Predictor(action.getCronExpr())).nextMatchingDate();
                    newTask.addGenericInformation(InternalJob.GENERIC_INFO_START_AT_KEY,
                                                  ISO8601DateUtil.parse(startAt));
                    newTask.setScheduledTime(startAt.getTime());
                } catch (InvalidPatternException e) {
                    // this will not happen as the cron expression is
                    // already being validated in FlowScript class.
                    LOGGER.debug(e.getMessage());
                }
            }
        }
        return replicateForNextLoopIteration;
    }

}
