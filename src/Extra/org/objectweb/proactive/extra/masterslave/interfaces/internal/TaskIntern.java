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
package org.objectweb.proactive.extra.masterslave.interfaces.internal;

import java.io.Serializable;

import org.objectweb.proactive.extra.masterslave.interfaces.Task;


/**
 * Internal view of a task in the Master/Slave API<br/>
 * Adds the possibility to set the result for a task<br/>
 * Adds the notion of a "Task ID"<br/>
 * @author fviale
 *
 * @param <R>
 */
public interface TaskIntern<R extends Serializable> extends Task<R>, Comparable {
    /**
     * get the actual task
     * @return the task
     */
    Task<R> getTask();

    /**
     * get the result of the task
     * @return the result
     */
    public R getResult();

    /**
     * get the id of the task
     * @return the id
     */
    public long getId();

    /**
     * sets the result of the task
     * @param res the result
     */
    public void setResult(R res);

    /**
     * tells if the task is null (nothing to do)
     * @return
     */
    public boolean isNull();

    /**
     * tells if the task threw a functional exception
     * @return answer
     */
    public boolean threwException();

    /**
     * returns the actual functional exception thrown by the task
     * @return the exception
     * @throws an IllegalStateException is no Exception was thrown
     */
    public Throwable getException() throws IllegalStateException;

    /**
     * sets the exception thrown by the task
     * @param e the exception
     */
    public void setException(Throwable e);
}
