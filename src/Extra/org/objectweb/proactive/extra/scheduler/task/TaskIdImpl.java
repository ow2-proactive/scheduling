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
package org.objectweb.proactive.extra.scheduler.task;

import org.objectweb.proactive.extra.scheduler.common.task.TaskId;


/**
 * Definition of a task identification.
 * For the moment, it is represented by an integer.
 *
 * @author ProActive Team
 * @version 1.0, Jun 29, 2007
 * @since ProActive 3.2
 */
public final class TaskIdImpl implements TaskId {

    /** Serial version UID */
    private static final long serialVersionUID = -7367447876595953374L;

    /** task id */
    private int id = 0;

    /**
     * Default constructor. Just set the id of the task.
     *
     * @param id the task id to set.
     */
    public TaskIdImpl(int id) {
        this.id = id;
    }

    /**
     * To get the id
     *
     * @return the id
     */
    public int value() {
        return id;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(TaskId o) {
        return new Integer(id).compareTo(new Integer(o.value()));
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof TaskIdImpl) {
            return ((TaskIdImpl) o).id == id;
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return id;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "" + id;
    }
}
