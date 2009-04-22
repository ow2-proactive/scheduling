/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.job;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Proxy;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.exception.MaxJobIdReachedException;
import org.ow2.proactive.scheduler.common.job.JobId;


/**
 * Definition of a job identification, this will be used during scheduling to identify your job.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@Entity
@Table(name = "JOB_ID")
@AccessType("field")
@Proxy(lazy = false)
public final class JobIdImpl implements JobId {
    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    private long hibernateId;

    /** Default job name */
    public static final String DEFAULT_JOB_NAME = SchedulerConstants.JOB_DEFAULT_NAME;

    /** global id count */
    private static int currentId = 0;

    /** current instance id */
    @Column(name = "ID")
    private int id;

    /** Human readable name */
    @Column(name = "READABLE_NAME")
    private String readableName = DEFAULT_JOB_NAME;

    /** Hibernate default constructor */
    private JobIdImpl() {
    }

    /**
     * Default Job id constructor
     *
     * @param id the id to put in the jobId
     */
    private JobIdImpl(int id) {
        this.id = id;
    }

    /**
     * Default Job id constructor
     *
     * @param id the id to put in the jobId
     * @param readableName the human readable name associated with this jobid
     */
    private JobIdImpl(int id, String readableName) {
        this(id);
        this.readableName = readableName;
    }

    /**
     * To set the initial id value
     *
     * @param jobId the initial value to set
     */
    public static void setInitialValue(JobIdImpl jobId) {
        currentId = jobId.id;
    }

    /**
     * Get the next available Job Id
     *
     * @param readableName the human readable name of the the created jobid
     * @return the next available id.
     * @throws MaxJobIdReachedException if the maximum id for a job has been reached.
     */
    public static JobId nextId(String readableName) throws MaxJobIdReachedException {
        currentId++;
        if (currentId == Integer.MAX_VALUE) {
            currentId = 0;
            throw new MaxJobIdReachedException("The max value for JobId has been reached !");
        }
        return new JobIdImpl(currentId, readableName);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobId#getReadableName()
     */
    public String getReadableName() {
        return this.readableName;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.JobId#value()
     */
    public String value() {
        return "" + this.id;
    }

    /**
     * Make a new JobId with the given arguments.
     *
     * @param str the string on which to base the id.
     * @return the new jobId
     */
    public static JobId makeJobId(String str) {
        return new JobIdImpl(Integer.parseInt(str.trim()));
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * @param jobId the id to be compared to <i>this</i> id.
     * @return  a negative integer, zero, or a positive integer as this object
     *		is less than, equal to, or greater than the specified object.
     */
    public int compareTo(JobId jobId) {
        return Integer.valueOf(id).compareTo(Integer.valueOf(((JobIdImpl) jobId).id));
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if ((o != null) && o instanceof JobIdImpl) {
            return ((JobIdImpl) o).id == id;
        }
        return false;
    }

    /**
     * <font color="red"><b>Do not use this method to get the value as an INTEGER.<br />
     * It does not ensure that this integer value is the real Job ID Value.</b></font><br />
     * Use the {@link org.ow2.proactive.scheduler.common.job.JobId#value()} method instead.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.id;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.value();
    }

}
