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
package org.ow2.proactive.scheduler.task.internal;

import java.util.LinkedHashSet;
import java.util.Set;

import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskStatus;


/**
 * @author ActiveEon Team
 * @since 30 Dec 2016
 */
public class InternalTaskParentFinder {

    private static InternalTaskParentFinder instance = null;

    private InternalTaskParentFinder() {
    }

    public static synchronized InternalTaskParentFinder getInstance() {
        if (instance == null) {
            instance = new InternalTaskParentFinder();
        }
        return instance;
    }

    /**
     * Depth-first search of non-skipped parent task ids
     * NOTE: this algorithm cannot be infinite as the graph cannot have cycles and loop flows are finite when skipped.
     * @param task task used to originate the search
     * @return ids of the first non-skipped tasks in the parent tree
     */
    public Set<TaskId> getFirstNotSkippedParentTaskIds(InternalTask task) {
        Set<TaskId> parentIds = new LinkedHashSet<>();
        if (task.getStatus() == TaskStatus.SKIPPED) {
            if (task.getIDependences() != null) {
                for (InternalTask parentInternalTask : task.getIDependences()) {
                    parentIds.addAll(getFirstNotSkippedParentTaskIds(parentInternalTask));
                }
            }
            // if the task is the target of the if or else branch, explore the parent tree
            if (task.getIfBranch() != null) {
                parentIds.addAll(getFirstNotSkippedParentTaskIds(task.getIfBranch()));
            }
            // if the task is the target of the continuation branch, explore both if and else trees
            if (task.getJoinedBranches() != null) {
                for (InternalTask parentInternalTask : task.getJoinedBranches()) {
                    parentIds.addAll(getFirstNotSkippedParentTaskIds(parentInternalTask));
                }
            }
        } else {
            parentIds.add(task.getId());
        }

        return parentIds;
    }

}
