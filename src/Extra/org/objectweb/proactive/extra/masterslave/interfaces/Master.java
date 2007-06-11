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
import java.util.List;

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
     * Reception order mode. Results can be received in Completion Order (the default) or Submission Order
     * @author fviale
     *
     */
    public enum OrderingMode {
        /**
             * Results of tasks are received in the same order as tasks were submitted
             */
        SubmitionOrder,
        /**
         * Results of tasks are received in the same order as tasks are completed (unspecified)
         */
        CompletionOrder;
    }

    /**
     * Adds the given Collection of nodes to the master <br/>
     * @param nodes
     */
    public void addResources(Collection<Node> nodes);

    /**
     * Asks the resource manager to activate every virtual nodes inside the given descriptor <br/>
     * @param descriptorURL URL of a deployment descriptor
     */
    public void addResources(URL descriptorURL);

    /**
    * Asks the resource manager to activate the given virtual node inside the given descriptor <br/>
    * @param descriptorURL URL of a deployment descriptor
    * @param virtualNodeName name of the virtual node to activate
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
     * @param freeResources tells if the Slave Manager should as well free the node resources
     */
    public void terminate(boolean freeResources);

    /**
     * Adds a list of tasks to be solved by the master <br/>
     * @param tasks list of tasks
     * @throws IllegalArgumentsException if the mode is changed or if a task is submitted twice
     */
    public void solve(List<T> tasks) throws IllegalArgumentException;

    /**
     * Wait for all results, will block until all results are computed <br>
     * The ordering of the results depends on the result reception mode in use <br>
     * @return a collection of objects containing the result
     * @throws IllegalStateException if no task have been submitted
     * @throws TaskException if a task threw an Exception
     */
    public List<R> waitAllResults() throws IllegalStateException, TaskException;

    /**
     * Wait for the first result available <br>
     * Will block until at least one Result is available. <br>
     * Note that in SubmittedOrder mode, the method will block until the next result in submission order is available<br>
     * @return an object containing the result
     * @throws IllegalStateException if no task have been submitted
     * @throws TaskException if the task threw an Exception
     */
    public R waitOneResult() throws IllegalStateException, TaskException;

    /**
     * Wait for a number of results<br>
     * Will block until at least k results are available. <br>
     * The ordering of the results depends on the result reception mode in use <br>
     * @param k the number of results to wait for
     * @return a collection of objects containing the results
     * @throws IllegalStateException if no task have been submitted
     * @throws TaskException if the task threw an Exception
     */
    public List<R> waitKResults(int k)
        throws IllegalStateException, TaskException;

    /**
     * Tells if the master is completely empty (i.e. has no result to provide and no tasks submitted)
     * @return the answer
     */
    public boolean isEmpty();

    /**
     * Returns the number of available results <br/>
     * @return the answer
     */
    public int countAvailableResults();

    /**
     * sets the current ordering mode <br/>
     * If reception mode is switched while computations are in progress,<br/>
     * subsequent calls to waitResults methods will be done according to the new mode.<br/>
     * @param mode the new mode for result gathering
     */
    public void setResultReceptionOrder(OrderingMode mode);
}
