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
package org.objectweb.proactive.extensions.masterworker.core;

import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;
import org.objectweb.proactive.extensions.masterworker.interfaces.SubMaster;
import org.objectweb.proactive.extensions.masterworker.interfaces.DivisibleTask;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.Identifiable;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.TaskIntern;

import java.io.Serializable;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * The internal version of a task, contains an internal ID and the task itself
 * @author The ProActive Team
 *
 */
public class TaskWrapperImpl implements TaskIntern<Serializable> {

    /**
     *
     */

    /**
     * The id of the task
     */
    private long id = NULL_TASK_ID;

    /**
     * The actual task object
     */
    private Task<Serializable> realTask = null;

    /**
     *
     */
    public TaskWrapperImpl() { // null task
    }

    /**
     * Creates a wrapper with the given task and id
     * @param id id of the task
     * @param realTask the user task
     */
    public TaskWrapperImpl(final long id, final Task<Serializable> realTask) {
        this.id = id;
        this.realTask = realTask;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Identifiable) {
            return id == ((Identifiable) obj).getId();
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public long getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public Task<Serializable> getTask() {
        return realTask;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return (int) id;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isNull() {
        return realTask == null;
    }

    /**
     * {@inheritDoc}
     */
    public Serializable run(final WorkerMemory memory) throws Exception {
        return this.realTask.run(memory);
    }

    /**
    * {@inheritDoc}
    */
    public Serializable run(final WorkerMemory memory, final SubMaster master) throws Exception {
        return ((DivisibleTask) this.realTask).run(memory, master);
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final Identifiable o) {
        if (o == null) {
            throw new NullPointerException();
        }

        return (int) (id - (o).getId());
    }
}
