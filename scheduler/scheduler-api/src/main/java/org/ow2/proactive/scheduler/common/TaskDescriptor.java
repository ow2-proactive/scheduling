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

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;


/**
 * This class represents a task for the policy.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public interface TaskDescriptor extends Serializable {

    /**
     * To get the children
     *
     * @return the children
     */
    Vector<TaskDescriptor> getChildren();

    /**
     * To get the id of the corresponding task
     *
     * @return the id of the corresponding task
     */
    TaskId getTaskId();

    /**
     * To get the parents
     *
     * @return the parents
     */
    Vector<TaskDescriptor> getParents();

    /**
     * To get the jobId
     *
     * @return the jobId
     */
    JobId getJobId();

    /**
     * Get the number of nodes needed for this task (by default: 1).
     *
     * @return the number of Nodes Needed
     */
    int getNumberOfNodesNeeded();

    /**
     * Return the number of children remaining.
     *
     * @return the number of children remaining.
     */
    int getChildrenCount();

    /**
     * Get the number of attempt the core has made to start this task.
     *
     * @return the number of attempt the core has made to start this task.
     */
    int getAttempt();

}
