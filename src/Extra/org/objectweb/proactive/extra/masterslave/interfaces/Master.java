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
package org.objectweb.proactive.extra.masterslave.interfaces;

import java.io.Serializable;
import java.net.URL;
import java.util.Collection;

import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.masterslave.TaskException;


/**
 * User Interface for the Master/Slave API <br/>
 * @author fviale
 *
 * @param <T> Task of result R
 * @param <R> Result Object
 */
public interface Master<T extends Task<R>, R extends Serializable> {

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
     * This method returns the number of slaves currently in the slave pool
     * @return number of slaves
     */
    public int slavepoolSize();

    /**
     * Terminates the slave manager and (eventually free every resources) <br/>
     * @param freeNodeResources tells if the Slave Manager should as well free the node resources
     * @return success
     */
    public void terminate(boolean freeResources);

    /**
     * Adds a collection of tasks to be solved by the master <br/>
     * Note that is a collection of tasks is submitted in one mode, it's impossible to submit tasks in a different mode until all the results have been retrieved (i.e. the master is empty)<br/>
     * @param tasks collection of tasks
     * @param ordered do we want to collect the results in the same order ?
     * @throws IllegalArgumentsException if the mode is changed or if a task is submitted twice
     */
    public void solveAll(Collection<T> tasks, boolean ordered)
        throws IllegalArgumentException;

    /**
     * Wait for all results, will block until all results are computed <br>
     * The ordering of the results depends on the mode used when submitted <br>
     * @return a collection of objects containing the result
     * @throws IllegalStateException if no task have been submitted
     * @throws TaskException if a task threw an Exception
     */
    public Collection<R> waitAllResults()
        throws IllegalStateException, TaskException;

    /**
     * Wait for the first result available <br>
     * Will block until at least one Result is available. <br>
     * Note that in ordered mode, the method will block until the next result in submission order is available<br>
     * @return an object containing the result
     * @throws IllegalStateException if no task have been submitted
     * @throws TaskException if the task threw an Exception
     */
    public R waitOneResult() throws IllegalStateException, TaskException;

    /**
     * Tells if the master has no more results to provide (useful in conjunction with waitOneResult)
     * @return the answer
     */
    public boolean isEmpty();

    /**
     * Tells if all results are available <br/>
     * @return the answer
     * @throws IllegalStateException if no task have been submitted
     */
    public boolean areAllResultsAvailable() throws IllegalStateException;

    /**
     * Tells if there's any result available <br/>
     * Note that if the gathering mode is set to "ordered", the method will answer true if and only if the next task in the serie is available.
     * @return the answer
     * @throws IllegalStateException if no task have been submitted
     */
    public boolean isOneResultAvailable() throws IllegalStateException;
}
