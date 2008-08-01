/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package org.ow2.proactive.scheduler.common.job;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


/**
 * Definition of a job identification, this will be used during scheduling to identify your job.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public final class JobId implements Comparable<JobId>, Serializable {

    /** Default job name */
    public static final String DEFAULT_JOB_NAME = PASchedulerProperties.JOB_DEFAULT_NAME.getValueAsString();

    /** global id count */
    private static int currentId = 0;

    /** current instance id */
    private int id;

    /** Human readable name */
    private String readableName = DEFAULT_JOB_NAME;

    /**
     * ProActive empty constructor
     */
    public JobId() {
    }

    /**
     * Default Job id constructor
     *
     * @param id the id to put in the jobId
     */
    private JobId(int id) {
        this.id = id;
    }

    /**
     * Default Job id constructor
     * 
     * @param id the id to put in the jobId
     * @param readableName the human readable name associated with this jobid
     */
    private JobId(int id, String readableName) {
        this(id);
        this.readableName = readableName;
    }

    /**
     * To set the initial id value
     *
     * @param jobId the initial value to set
     */
    public static void setInitialValue(JobId jobId) {
        currentId = jobId.id;
    }

    /**
     * Get the next id
     *
     * @return the next available id.
     */
    public static JobId nextId() {
        return new JobId(++currentId);
    }

    /**
     * Get the next id
     *
     * @param readableName the human readable name of the the created jobid
     * @return the next available id.
     */
    public static JobId nextId(String readableName) {
        return new JobId(++currentId, readableName);
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
     * Make a new JobId with the given arguments.
     * 
     * @param str the string on which to base the id.
     * @return the new jobId
     */
    public static JobId makeJobId(String str) {
        return new JobId(Integer.parseInt(str));
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * @param jobId the id to be compared to <i>this</i> id.
     * @return  a negative integer, zero, or a positive integer as this object
     *		is less than, equal to, or greater than the specified object.
     */
    public int compareTo(JobId jobId) {
        return Integer.valueOf(id).compareTo(Integer.valueOf(jobId.id));
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if ((o != null) && o instanceof JobId) {
            return ((JobId) o).id == id;
        }

        return false;
    }

    /**
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
        return "" + this.id;
    }
}
