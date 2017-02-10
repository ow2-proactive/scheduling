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
package org.ow2.proactive.scheduler.common.task;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;


/**
 * Definition of a task identification.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public interface TaskId extends Comparable<TaskId>, Serializable {

    /**
     * Returns the job identifier associated with this task.
     *
     * @return the job identifier associated with this task.
     */
    JobId getJobId();

    /**
     * Return the human readable name associated with this id.
     *
     * @return the human readable name associated with this id.
     */
    String getReadableName();

    /**
    * Return the tag of the task.
    * @return the tag of the task.
    */
    String getTag();

    /**
     * Returns a String representation that is unique for the job
     * it is related to. Please look at {@link #toString} for a String
     * representation unique for the scheduler instance it was built for.
     *
     * @return a String representation that is unique for the job
     * it is related to only.
     */
    String value();

    /**
     * Returns a long representation that is unique for the job
     * it is related to.
     *
     * @return a long representation that is unique for the job
     * it is related to.
     */
    long longValue();

    /**
     * When Control Flow actions are performed on Tasks, some tasks are replicated. 
     * A task replicated by a {@link FlowActionType#IF} action
     * is differentiated from the original by an incremented Iteration Index.
     * This index is reflected in the readable name of the Task's id ({@link #getReadableName()}),
     * this methods safely extracts it and returns it as an int.
     * 
     * @return the iteration number of this task if it was replicated by a IF flow operation ({@code >= 0})
     */
    int getIterationIndex();

    /**
     * When Control Flow actions are performed on Tasks, some tasks are replicated. 
     * A task replicated by a {@link FlowActionType#REPLICATE} action
     * is differentiated from the original by an incremented Replication Index.
     * This index is reflected in the readable name of the Task's id ({@link #getReadableName()}),
     * this methods safely extracts it and returns it as an int.
     * 
     * @return the iteration number of this task if it was replicated by a IF flow operation ({@code >= 0})
     */
    int getReplicationIndex();

    /** string separator in the task name for indicating the replication index */
    String REPLICATION_SEPARATOR = "*";

    /** string separator in the task name for indicating the iteration index */
    String ITERATION_SEPARATOR = "#";

    /**
     * Returns a String representation that is unique for
     * the scheduler instance it was built for.
     *
     * @return a String representation that is unique for
     * the scheduler instance it was built for.
     */
    String toString();

}
