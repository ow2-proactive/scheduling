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

import org.objectweb.proactive.extensions.masterworker.interfaces.internal.Identifiable;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.ResultIntern;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.TaskIntern;

import java.io.Serializable;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * A result of a task, contains the result itself, or an exception if one has been thrown
 * @author The ProActive Team
 *
 */
public class ResultInternImpl implements ResultIntern<Serializable> {

    /**
     *
     */

    /**
     * The id of the task
     */
    private long id = -1;

    /**
     * the result
     */
    private Serializable result = null;

    /**
     * when this task has thrown an exception
     */
    private boolean isException = false;

    /**
     *  the exception thrown
     */
    private Throwable exception = null;

    /**
     * Creates an empty result object for the given task
     * @param task task associated with the result
     */
    public ResultInternImpl(TaskIntern<Serializable> task) {
        this.id = task.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof Identifiable) && (id == ((Identifiable) obj).getId());

    }

    /**
     * {@inheritDoc}
     */
    public Throwable getException() {
        return exception;
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
    public Serializable getResult() {
        return result;
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
    public void setException(final Throwable e) {
        if (e == null) {
            throw new IllegalArgumentException("Exception can't be null");
        }

        this.exception = e;
        this.isException = true;
    }

    /**
     * {@inheritDoc}
     */
    public void setResult(final Serializable res) {
        this.result = res;
    }

    /**
     * {@inheritDoc}
     */
    public boolean threwException() {
        return isException;
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

    @Override
    public String toString() {
        return "ID: " + id + " Result: " + result + " Exception: " + exception;
    }
}
