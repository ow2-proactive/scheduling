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

import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.masterslave.core.TaskException;


/**
 * Internal version of the Master Interface in the Master/Slave API <br/>
 * @author fviale
 *
 * @param <T> Task of result R
 * @param <R> Result Object
 */
public interface MasterIntern {

    /**
     * Adds the given Collection of nodes to the master <br/>
     * @param nodes
     */
    public void addResources(Collection<Node> nodes);

    /**
    * Asks the resource manager to activate the given virtual nodes inside the given descriptor <br/>
    * @param descriptorURL URL of a deployment descriptor
    * @param virtualNodeName names of virtual nodes to activate
    */
    public void addResources(URL descriptorURL, String virtualNodeName);

    /**
     * Adds the given virtual node to the master <br/>
     * @param virtualnode
     */
    public void addResources(VirtualNode virtualnode);

    /**
     * Terminates the slave manager and (eventually free every resources) <br/>
     * @param freeNodeResources tells if the Slave Manager should as well free the node resources
     * @return success
     */
    public void terminate(boolean freeResources);

    /**
     * Adds a task to be solved by the master <br/>
     * @param task
     */
    public void solve(TaskIntern task) throws IllegalArgumentException;

    /**
     * Adds a collection of tasks to be solved by the master <br/>
     * @param tasks
     */
    public void solveAll(Collection<TaskIntern> tasks)
        throws IllegalArgumentException;

    /**
     * Wait for all results, will block until all results are computed <br/>
     * The ordering of the results in unspecified <br/>
     * @return a collection of objects containing the result and the original task associated
     * @throws IllegalStateException if no task have been submitted
     * @throws TaskException if a task threw an Exception
     */
    public Collection<ResultIntern> waitAllResults()
        throws IllegalStateException, TaskException;

    /**
     * Wait for the first result available <br/>
     * Will block until at least one Result is available. <br/>
     * @return an object containing the result and the original task associated
     * @throws IllegalStateException if no task have been submitted
     * @throws TaskException if the task threw an Exception
     */
    public ResultIntern waitOneResult()
        throws IllegalStateException, TaskException;

    /**
     * Wait for the result of this task <br/>
     * Will block until the specific result is available <br/>
     * If the task is not known InvalidArgumentException is thrown <br/>
     * @param task task to wait for
     * @return expected result
     * @throws IllegalArgumentException if the task is invalid
     * @throws TaskException if the task threw an Exception
     */
    public ResultIntern waitResultOf(TaskIntern task)
        throws IllegalArgumentException, TaskException;

    /**
     * Wait for the result of this list of tasks <br/>
     * Will block until all the results are available. <br/>
     * This method is the only one which guaranties that the results are received in a specified order (the same order as the given list of tasks) <br/>
     * If a task is not known an InvalidArgumentException is thrown <br/>
     * @param tasks an order list of tasks to wait for
     * @return an ordered list of results
     * @throws IllegalArgumentException if the task is invalid
     * @throws TaskException if a task threw an Exception
     */
    public List<ResultIntern> waitResultsOf(List<TaskIntern> tasks)
        throws IllegalArgumentException, TaskException;

    /**
     * Tells if all results are available <br/>
     * @return the answer
     * @throws IllegalStateException if no task have been submitted
     */
    public boolean areAllResultsAvailable() throws IllegalStateException;

    /**
     * Tells if there's any result available <br/>
     * @return the answer
     * @throws IllegalStateException if no task have been submitted
     */
    public boolean isOneResultAvailable() throws IllegalStateException;

    /**
     * Tells if result of a specific task is available <br/>
     * @param task the associated task
     * @return the answer
     * @throws IllegalArgumentException if the task is invalid
     */
    public boolean isResultAvailable(TaskIntern task)
        throws IllegalArgumentException;
}
