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
package org.ow2.proactive.scheduler.task;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;


/**
 * Definition of a task identification.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@XmlAccessorType(XmlAccessType.FIELD)
public final class TaskIdImpl implements TaskId {

    // Task identifier
    private long id;

    /** Human readable name */
    private String readableName = SchedulerConstants.TASK_DEFAULT_NAME;

    /** tag of the task */
    private String tag;

    private JobId jobId;

    private TaskIdImpl(JobId jobId, long taskId) {
        this.jobId = jobId;
        this.id = taskId;
    }

    private TaskIdImpl(JobId jobId, String name, long taskId) {
        this(jobId, taskId);
        this.readableName = name;
    }

    /**
     * Create task id, and set task name.
     *
     * @param jobId        the id of the enclosing job.
     * @param readableName the human readable name of the task.
     * @param taskId       the task identifier value.
     * @return new TaskId instance.
     */
    public static TaskId createTaskId(JobId jobId, String readableName, long taskId) {
        return new TaskIdImpl(jobId, readableName, taskId);
    }

    /**
     * Create task id, and set task name + tag.
     *
     * @param jobId        the id of the enclosing job.
     * @param readableName the human readable name of the task.
     * @param taskId       the task identifier value.
     * @param tag          the tag of the task.
     * @return new TaskId instance.
     */
    public static TaskId createTaskId(JobId jobId, String readableName, long taskId, String tag) {
        TaskIdImpl t = new TaskIdImpl(jobId, readableName, taskId);
        t.setTag(tag);
        return t;
    }

    /**
     * {@inheritDoc}
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

    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Return the human readable name associated to this id.
     *
     * @return the human readable name associated to this id.
     */
    public String getReadableName() {
        return this.readableName;
    }

    /**
     * Set readable name of this TaskId.
     *
     * @param readableName the new human readable name.
     */
    public void setReadableName(String readableName) {
        this.readableName = readableName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(TaskId that) {
        return Long.compare(this.id, ((TaskIdImpl) that).id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        TaskIdImpl taskId = (TaskIdImpl) o;

        return id == taskId.id;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (int) (id % Integer.MAX_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value() {
        return Long.toString(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long longValue() {
        return id;
    }

    /**
     * @see TaskId#getIterationIndex()
     */
    @Override
    public int getIterationIndex() {
        // implementation note :
        // this has to match what is done in InternalTask#setName(String)
        int iterationPos;
        if ((iterationPos = this.readableName.indexOf(TaskId.ITERATION_SEPARATOR)) != -1) {
            int replicationPos = this.readableName.indexOf(TaskId.REPLICATION_SEPARATOR);
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
    @Override
    public int getReplicationIndex() {
        // implementation note :
        // this has to match what is done in InternalTask#setName(String)
        int pos;
        if ((pos = this.readableName.indexOf(TaskId.REPLICATION_SEPARATOR)) != -1) {
            int read = Integer.parseInt(this.readableName.substring(pos + 1));
            return Math.max(0, read);
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return jobId.value() + 't' + value();
    }

}
