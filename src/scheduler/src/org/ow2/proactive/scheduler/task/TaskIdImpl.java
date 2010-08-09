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
package org.ow2.proactive.scheduler.task;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Proxy;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.JobIdImpl;


/**
 * Definition of a task identification. For the moment, it is represented by an
 * integer.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@Entity
@Table(name = "TASK_ID")
@AccessType("field")
@Proxy(lazy = false)
public final class TaskIdImpl implements TaskId {
    /**  */
    private static final long serialVersionUID = 21L;

    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    private long hId;

    /**
     * Multiplicative factor for job id (taskId will be :
     * this_factor*jobID+taskID)
     */
    public static final int JOB_FACTOR = PASchedulerProperties.JOB_FACTOR.getValueAsInt();

    /** the global id count */
    private static int currentId = 0;

    /** task id */
    @Column(name = "ID")
    private long id;

    /** Human readable name */
    @Column(name = "READABLE_NAME")
    private String readableName = SchedulerConstants.TASK_DEFAULT_NAME;

    /** Job id */
    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.EAGER, targetEntity = JobIdImpl.class)
    private JobId jobId = null;

    /** Hibernate default constructor */
    private TaskIdImpl() {
    }

    /**
     * Default constructor. Just set the id of the task.
     *
     * @param jobId the task id to set.
     */
    private TaskIdImpl(JobId jobId) {
        this.jobId = jobId;
        this.id = (jobId.hashCode() * JOB_FACTOR) + (++currentId);
    }

    /**
     * Set id and name.
     *
     * @param jobId the task id to set.
     * @param name the human readable task name.
     */
    private TaskIdImpl(JobId jobId, String name) {
        this(jobId);
        this.readableName = name;
    }

    /**
     * To reinitialize the initial id value
     */
    public static void initialize() {
        currentId = 0;
    }

    /**
     * Get the next id.
     *
     * @param jobId the id of the enclosing job. Permit a generation of a task id based on the jobId.
     * @return the next available id.
     */
    public static synchronized TaskId nextId(JobId jobId) {
        return new TaskIdImpl(jobId);
    }

    /**
     * Get the next id, and set task name.
     *
     * @param jobId the id of the enclosing job. Permit a generation of a task id based on the jobId.
     * @param readableName Set the task name in the returned task id as well.
     * @return the next available id with task name set.
     */
    public static synchronized TaskId nextId(JobId jobId, String readableName) {
        return new TaskIdImpl(jobId, readableName);
    }

    /**
     * Returns the jobId.
     *
     * @return the jobId.
     */
    public JobId getJobId() {
        return jobId;
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
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * @param taskId the taskId to be compared.
     * @return  a negative integer, zero, or a positive integer as this object
     *		is less than, equal to, or greater than the specified object.
     */
    public int compareTo(TaskId taskId) {
        return Long.valueOf(id).compareTo(Long.valueOf(((TaskIdImpl) taskId).id));
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
}
