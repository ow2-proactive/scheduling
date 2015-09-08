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
package org.ow2.proactive.scheduler.task;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


/**
 * Definition of a task identification. For the moment, it is represented by an
 * integer.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@XmlAccessorType(XmlAccessType.FIELD)
public final class TaskIdImpl implements TaskId {

    /** Task id */
    private long id;

    /** Human readable name */
    private String readableName = SchedulerConstants.TASK_DEFAULT_NAME;

    /** tag of the task */
    private String tag;

    /** Job id */
    @XmlTransient
    private JobId jobId = null;

    /**
     * Default constructor. Just set the id of the task.
     *
     * @param jobId the task id to set.
     */
    private TaskIdImpl(JobId jobId, long id, boolean applyJobFactor) {
        this.jobId = jobId;
        if (applyJobFactor) {
            this.id = (jobId.hashCode() * getJobFactor()) + id;
        } else {
            this.id = id;
        }
    }

    public static long getJobFactor() {
        return PASchedulerProperties.JOB_FACTOR.getValueAsInt();
    }

    /**
     * Set id and name.
     *
     * @param jobId the task id to set.
     * @param name the human readable task name.
     */
    private TaskIdImpl(JobId jobId, String name, long id, boolean applyJobFactor) {
        this(jobId, id, applyJobFactor);
        this.readableName = name;
    }

    /**
     * Create task id, and set task name.
     *
     * @param jobId the id of the enclosing job. Permit a generation of a task id based on the jobId.
     * @param readableName Set the task name in the returned task id as well.
     * @return new task id with task name set.
     */
    public static TaskId createTaskId(JobId jobId, String readableName, long id, boolean applyJobFactor) {
        return new TaskIdImpl(jobId, readableName, id, applyJobFactor);
    }

    /**
     * Returns the jobId.
     *
     * @return the jobId.
     */
    public JobId getJobId() {
        return jobId;
    }

    public void setJobId(JobId jobId) {
        this.jobId = jobId;
    }

    @Override
    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag){this.tag = tag;}


    /**
     * Return the human readable name associated to this id.
     *
     * @return the human readable name associated to this id.
     */
    public String getReadableName() {
        return this.readableName;
    }


    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * @param taskId the taskId to be compared.
     * @return  a negative integer, zero, or a positive integer as this object
     *		is less than, equal to, or greater than the specified object.
     */
    public int compareTo(TaskId taskId) {
        return Long.valueOf(id).compareTo(((TaskIdImpl) taskId).id);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if ((o != null) && o instanceof TaskIdImpl) {
            return ((TaskIdImpl) o).id == id;
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (int) (id % Integer.MAX_VALUE);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return value();
    }

    /**
     * {@inheritDoc}
     */
    public String value() {
        return "" + this.id;
    }

    /**
     * Set readable name of this TaskId
     * 
     * @param readableName new name
     */
    public void setReadableName(String readableName) {
        this.readableName = readableName;
    }

    /**
     * @see TaskId#getIterationIndex()
     */
    public int getIterationIndex() {
        // implementation note :
        // this has to match what is done in InternalTask#setName(String)
        int iterationPos;
        if ((iterationPos = this.readableName.indexOf(TaskId.iterationSeparator)) != -1) {
            int replicationPos = this.readableName.indexOf(TaskId.replicationSeparator);
            if (replicationPos == -1) {
                replicationPos = readableName.length();
            }
            int read = Integer.parseInt(this.readableName.substring(iterationPos + 1, replicationPos));
            return Math.max(0, read);
        }
        return 0;
    }

    /**
     * @see TaskId#getReplicationIndex()
     */
    public int getReplicationIndex() {
        // implementation note :
        // this has to match what is done in InternalTask#setName(String)
        int pos;
        if ((pos = this.readableName.indexOf(TaskId.replicationSeparator)) != -1) {
            int read = Integer.parseInt(this.readableName.substring(pos + 1));
            return Math.max(0, read);
        }
        return 0;
    }
}
