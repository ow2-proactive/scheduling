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
package org.objectweb.proactive.extra.scheduler.job;

import org.objectweb.proactive.extra.scheduler.common.job.JobId;


/**
 * Implementation of a job identification.
 * For the moment, it is represented by an integer.
 *
 * @author ProActive Team
 * @version 1.0, Jun 29, 2007
 * @since ProActive 3.2
 */
public final class JobIdImpl implements JobId {

    /** Serial version UID */
    private static final long serialVersionUID = -7367447876595953374L;
    private int id = 0;

    /**
     * ProActive empty constructor
     */
    public JobIdImpl() {
    }

    /**
     * Default Job id constructor
     *
     * @param id the id to put in the jobId
     */
    public JobIdImpl(int id) {
        this.id = id;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.job.JobId#value()
     */
    public int value() {
        return id;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.job.JobId#setValue(int)
     */
    public void setValue(int id) {
        this.id = id;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(JobId o) {
        return new Integer(id).compareTo(new Integer(o.value()));
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof JobId) {
            return ((JobId) o).value() == id;
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
        return "" + id;
    }
}
