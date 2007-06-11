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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.masterslave.core;

import java.io.Serializable;

import org.objectweb.proactive.extra.masterslave.interfaces.SlaveMemory;
import org.objectweb.proactive.extra.masterslave.interfaces.Task;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.Identified;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskIntern;


/**
 * The internal wrapper of a task, contains an internal ID, the task result and the exception eventually thrown
 * @author fviale
 *
 */
public class TaskWrapperImpl implements TaskIntern {
    // The id of the task
    private long id = -1;

    // The actual task object
    private Task realTask = null;

    // the result
    private Serializable result = null;

    // when this task has thrown an exception
    private boolean isException = false;

    // the exception thrown
    private Throwable exception = null;

    /**
     *
     */
    public TaskWrapperImpl() { // null task
    }

    /**
     * Creates a wrapper with the given task and id
     * @param id
     * @param realTask the user task
     */
    public TaskWrapperImpl(long id, Task realTask) {
        this.id = id;
        this.realTask = realTask;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof Identified) {
            return id == ((Identified) obj).getId();
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskIntern#getId()
     */
    public long getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskIntern#getTask()
     */
    public Task getTask() {
        return realTask;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (int) id;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskIntern#isNull()
     */
    public boolean isNull() {
        return realTask == null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Task#run(org.objectweb.proactive.extra.masterslave.interfaces.SlaveMemory)
     */
    public Serializable run(SlaveMemory memory) throws Exception {
        return this.realTask.run(memory);
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        if (o == null) {
            throw new NullPointerException();
        } else if (o instanceof Identified) {
            return (int) (id - ((Identified) o).getId());
        } else {
            throw new IllegalArgumentException("" + o);
        }
    }

    public String toString() {
        return "ID: " + id + " Result: " + result + " Exception: " + exception;
    }
}
