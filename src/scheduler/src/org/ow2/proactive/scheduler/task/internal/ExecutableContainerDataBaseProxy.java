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
 * $PROACTIVE_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.task.internal;

import java.io.Serializable;

import org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB;
import org.ow2.proactive.scheduler.task.ExecutableContainer;


/**
 * ExecutableContainerDataBaseProxy is a class that is able to know if the ExecutableContainer stored is in dataBase or in memory.
 * According to this condition, data have to be extracted from dataBase if it is in DataBase, in memory otherwise.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class ExecutableContainerDataBaseProxy implements Serializable {

    /** Tell if the data is in dataBase or in memory. */
    private boolean inDB = false;
    /** The value of the stored data. */
    private ExecutableContainer value;
    /** The taskId corresponding to the data. */
    private InternalTask task;

    public ExecutableContainerDataBaseProxy(ExecutableContainer value, InternalTask task) {
        this.value = value;
        this.task = task;
    }

    /**
     * Get the ExecutableContainer associated to the current Task.
     * 
     * @return the ExecutableContainer associated to the current Task.
     */
    public ExecutableContainer getValue() {
        if (inDB || value == null) {
            /* impl to have the object in memory once it is back from database. */
            //			value = AbstractSchedulerDB.getInstance().getExecutableContainer(id);
            //			inDB = false;
            //			return value;
            /* impl to never have object in memory */
            return AbstractSchedulerDB.getInstance().getExecutableContainer(task.getId());
        } else {
            return value;
        }
    }

    /**
     * Delete reference to the ExecutableContainer.
     * The goal is to allow the GC to remove this object that can be huge.
     */
    public void clean() {
        inDB = true;
        value = null;
    }

}
