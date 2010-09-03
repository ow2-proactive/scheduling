/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
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
     * Returns the jobId.
     *
     * @return the jobId.
     */
    public JobId getJobId();

    /**
     * Return the human readable name associated to this id.
     *
     * @return the human readable name associated to this id.
     */
    public String getReadableName();

    /**
     * Get the value of the TaskId.<br />
     * As the internal implementation of this class can change, It is strongly recommended to use this method
     * to get a literal value of the ID.<br />
     * Use this value if you lost the TaskId Object returned by the scheduler.
     *
     * @return the textual representation of this TaskId
     */
    public String value();

    /**
     * When Control Flow actions are performed on Tasks, some tasks are replicated. 
     * A task replicated by a {@link FlowActionType#IF} action
     * is differentiated from the original by an incremented Iteration Index.
     * This index is reflected in the readable name of the Task's id ({@link #getReadableName()}),
     * this methods safely extracts it and returns it as an int.
     * 
     * @return the iteration number of this task if it was replicated by a IF flow operation (>= 0)
     */
    public int getIterationIndex();

    /**
     * When Control Flow actions are performed on Tasks, some tasks are replicated. 
     * A task replicated by a {@link FlowActionType#REPLICATE} action
     * is differentiated from the original by an incremented Replication Index.
     * This index is reflected in the readable name of the Task's id ({@link #getReadableName()}),
     * this methods safely extracts it and returns it as an int.
     * 
     * @return the iteration number of this task if it was replicated by a IF flow operation (>= 0)
     */
    public int getReplicationIndex();

    /** string separator in the task name for indicating the replication index */
    public final static String replicationSeparator = "*";

    /** string separator in the task name for indicating the iteration index */
    public final static String iterationSeparator = "#";
}
