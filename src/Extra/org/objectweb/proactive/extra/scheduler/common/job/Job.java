/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.extra.scheduler.common.job;

import java.io.Serializable;


/**
 * Definition of a job for the user.
 *
 * @author ProActive Team
 * @version 1.0, Sept 13, 2007
 * @since ProActive 3.2
 */
public abstract class Job implements Serializable {

    /** Name of the job */
    protected String name = "Default name";

    /** Maximum amount of running time that the job can not exceed */
    protected long runtimeLimit = -1;

    /** Is this job has to cancel when an exception occurs in a task */
    protected boolean cancelOnException = false;

    /** Short description of this job */
    protected String description = "Default description";

    /** Job priority */
    protected JobPriority priority = JobPriority.NORMAL;

    /** ProActive Empty Constructor */
    public Job() {
    }

    /**
     * To get the type
     *
     * @return the type
     */
    public abstract JobType getType();

    /**
     * To get the cancelOnException
     *
     * @return the cancelOnException
     */
    public boolean isCancelOnException() {
        return cancelOnException;
    }

    /**
     * Set to true if you want to cancel the job when an exception occurs in a task.
     *
     * @param cancelOnException the cancelOnException to set
     */
    public void setCancelOnException(boolean cancelOnException) {
        this.cancelOnException = cancelOnException;
    }

    /**
     * To get the description
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * To set a short description for the job.
     *
     * @param description the description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * To get the name of the job.
     *
     * @return the name of the job.
     */
    public String getName() {
        return name;
    }

    /**
     * To set the name of the job.
     *
     * @param name the name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * To get the runtimeLimit
     *
     * @return the runtimeLimit
     */
    public long getRuntimeLimit() {
        return runtimeLimit;
    }

    /**
     * @param runtimeLimit the runtimeLimit to set
     */
    public void setRuntimeLimit(long runtimeLimit) {
        this.runtimeLimit = runtimeLimit;
    }

    /**
     * To get the priority of the job.
     *
     * @return the priority of the job.
     */
    public JobPriority getPriority() {
        return priority;
    }

    /**
     * To set the priority of the job.
     *
     * @param priority the priority to set.
     */
    public void setPriority(JobPriority priority) {
        this.priority = priority;
    }
}
