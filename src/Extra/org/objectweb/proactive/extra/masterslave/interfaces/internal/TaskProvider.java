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


/**
 * A Task Provider provides tasks to be executed and excepts results of these tasks (i.e. the Master from the slave point of view)
 * @author fviale
 *
 * @param <T> TaskWrapper of Result R
 * @param <R> result
 */
public interface TaskProvider<T extends TaskIntern<R>, R extends Serializable> {

    /**
     * Returns a task which needs to be executed
     * @param Slave the slave object which asks the tasks (stub)
     * @param slaveName the name of the slave which asks the tasks
     * @return a new task to compute
     */
    public T getTask(Slave slave, String slaveName);

    /**
     * Returns the result of a task to the provider
     * @param task the result of the completed task
     * @param slaveName the name of the slave sending the result
     * @return a new task to compute
     */
    public T sendResultAndGetTask(T task, String slaveName);
}
