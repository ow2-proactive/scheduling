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
package org.ow2.proactive.scheduler.core.helpers;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.policy.ExtendedSchedulerPolicy;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.policy.ISO8601DateUtil;


public class StartAtUpdater {

    public boolean updateStartAt(InternalJob job, String startAt, SchedulerDBManager dbManager) {

        long scheduledTime = ISO8601DateUtil.toDate(startAt).getTime();

        Set<TaskId> updatedTasks = updateStartAtAndTasksScheduledTime(job, startAt, scheduledTime);

        boolean updatedTasksNotEmpty = !updatedTasks.isEmpty();

        if (updatedTasksNotEmpty) {
            dbManager.updateTaskSchedulingTime(job, scheduledTime);
        }

        return updatedTasksNotEmpty;

    }

    private Set<TaskId> updateStartAtAndTasksScheduledTime(InternalJob job, String startAt, long scheduledTime) {

        List<InternalTask> internalTasks = job.getITasks();
        Set<TaskId> updatedTasks = new HashSet<>(internalTasks.size());

        if (resetJobGenericInformation(job, startAt)) {

            for (InternalTask td : internalTasks) {
                td.setScheduledTime(scheduledTime);
                updatedTasks.add(td.getId());
                job.getJobDescriptor().updateTaskScheduledTime(td.getId(), scheduledTime);
            }
        }

        return updatedTasks;
    }

    private boolean resetJobGenericInformation(InternalJob job, String startAt) {

        Map<String, String> genericInformation = job.getRuntimeGenericInformation();

        if (isValidStartAt(genericInformation, startAt)) {
            genericInformation.put(ExtendedSchedulerPolicy.GENERIC_INFORMATION_KEY_START_AT, startAt);
            job.setGenericInformation(genericInformation);
            return true;
        }

        return false;
    }

    private boolean isValidStartAt(Map<String, String> genericInformation, String startAt) {
        String found = genericInformation.get(ExtendedSchedulerPolicy.GENERIC_INFORMATION_KEY_START_AT);

        // null is not allowed as generic information key,
        // this assumption should always hold in our context
        if (found != null && found.equals(startAt)) {
            return false;
        }

        return true;
    }

}
