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
package org.objectweb.proactive.extra.masterworker.interfaces;

import java.io.Serializable;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.masterworker.TaskAlreadySubmittedException;
import org.objectweb.proactive.extra.masterworker.TaskException;


/**
 * User Interface for the Master/Worker API <br/>
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
     * @param nodes a collection of nodes
     */
    void addResources(Collection<Node> nodes);

    /**
     * Adds the given descriptor to the master<br>
     * Every virtual nodes inside the given descriptor will be activated<br/>
     * @param descriptorURL URL of a deployment descriptor
     */
    void addResources(URL descriptorURL);

    /**
    * Adds the given descriptor to the master<br>
    * Only the specified virtual node inside the given descriptor will be activated <br/>
    * @param descriptorURL URL of a deployment descriptor
    * @param virtualNodeName name of the virtual node to activate
    */
    void addResources(URL descriptorURL, String virtualNodeName);

    /**
     * Adds every resource inside the given virtual node to the master <br/>
     * @param virtualnode a virtual node object
     */
    void addResources(VirtualNode virtualnode);

    /**
     * This method returns the number of workers currently in the worker pool
     * @return number of workers
     */
    int workerpoolSize();

    /**
     * Terminates the worker manager and (eventually free every resources) <br/>
     * @param freeResources tells if the Worker Manager should as well free the node resources
     */
    void terminate(boolean freeResources);

    /**
     * Adds a list of tasks to be solved by the master <br/>
     * <b>Warning</b>: the master keeps a track of task objects that have been submitted to it and which are currently computing.<br>
     * Submitting two times the same task object without waiting for the result of the first computation is not allowed.
     * @param tasks list of tasks
     * @throws TaskAlreadySubmittedException if a task is submitted twice
     */
    void solve(List<T> tasks) throws TaskAlreadySubmittedException;

    /**
     * Wait for all results, will block until all results are computed <br>
     * The ordering of the results depends on the result reception mode in use <br>
     * @return a collection of objects containing the result
     * @throws TaskException if a task threw an Exception
     */
    List<R> waitAllResults() throws TaskException;

    /**
     * Wait for the first result available <br>
     * Will block until at least one Result is available. <br>
     * Note that in SubmittedOrder mode, the method will block until the next result in submission order is available<br>
     * @return an object containing the result
     * @throws TaskException if the task threw an Exception
     */
    R waitOneResult() throws TaskException;

    /**
     * Wait for a number of results<br>
     * Will block until at least k results are available. <br>
     * The ordering of the results depends on the result reception mode in use <br>
     * @param k the number of results to wait for
     * @return a collection of objects containing the results
     * @throws TaskException if the task threw an Exception
     */
    List<R> waitKResults(int k) throws TaskException;

    /**
     * Tells if the master is completely empty (i.e. has no result to provide and no tasks submitted)
     * @return the answer
     */
    boolean isEmpty();

    /**
     * Returns the number of available results <br/>
     * @return the answer
     */
    int countAvailableResults();

    /**
     * Sets the current ordering mode <br/>
     * If reception mode is switched while computations are in progress,<br/>
     * then subsequent calls to waitResults methods will be done according to the new mode.<br/>
     * @param mode the new mode for result gathering
     */
    void setResultReceptionOrder(OrderingMode mode);

    /**
     * Sets the period at which ping messages are sent to the workers <br/>
     * @param periodMillis the new ping period
     */
    void setPingPeriod(long periodMillis);
}
