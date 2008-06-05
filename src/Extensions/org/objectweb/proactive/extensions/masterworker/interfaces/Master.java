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
package org.objectweb.proactive.extensions.masterworker.interfaces;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;

import java.io.Serializable;
import java.net.URL;
import java.util.Collection;


/**
 * User Interface for the Master/Worker API <br/>
 * @author The ProActive Team
 *
 * @param <T> Task of result R
 * @param <R> Result Object
 */
@PublicAPI
public interface Master<T extends Task<R>, R extends Serializable> extends SubMaster<T, R> {

    /**
     * Value which specifies that a worker receives every tasks available (useful when combined to a scheduler for example)
     */
    public final int MAX_TASK_FLOODING = Integer.MAX_VALUE;

    // please keep the resource adding methods inside the tags
    // they are used in the documentation
    //@snippet-start masterworker_addresources
    /**
     * Adds the given Collection of nodes to the master <br/>
     * @param nodes a collection of nodes
     */
    void addResources(Collection<Node> nodes);

    /**
     * Adds the given descriptor to the master<br>
     * Every virtual nodes inside the given descriptor will be activated<br/>
     * @param descriptorURL URL of a deployment descriptor
     * @throws ProActiveException if a problem occurs while adding resources
     */
    void addResources(URL descriptorURL) throws ProActiveException;

    /**
     * Adds the given descriptor to the master<br>
     * Only the specified virtual node inside the given descriptor will be activated <br/>
     * @param descriptorURL URL of a deployment descriptor
     * @param virtualNodeName name of the virtual node to activate
     * @throws ProActiveException if a problem occurs while adding resources
     */
    void addResources(URL descriptorURL, String virtualNodeName) throws ProActiveException;

    /**
     * Adds a connection to the given Scheduler
     * @param schedulerURL URL to this scheduler
     * @param user user name
     * @param password password
     * @throws ProActiveException if a problem occurs while adding resources
     */
    void addResources(final String schedulerURL, String user, String password) throws ProActiveException;

    //@snippet-end masterworker_addresources

    /**
     * This method returns the number of workers currently in the worker pool
     * @return number of workers
     */

    int workerpoolSize();

    //@snippet-start masterworker_terminate
    /**
     * Terminates the worker manager and (eventually free every resources) <br/>
     * @param freeResources tells if the Worker Manager should as well free the node resources
     */
    void terminate(boolean freeResources);

    //@snippet-end masterworker_terminate

    /**
     * Tells the master to stop its current activity, and ignore all results of previously submitted tasks
     */
    void clear();

    //@snippet-start masterworker_flood
    /**
     * Sets the number of tasks initially sent to each worker
     * default is 2 tasks
     * @param number_of_tasks number of task to send
     */
    void setInitialTaskFlooding(final int number_of_tasks);

    //@snippet-end masterworker_flood
    //@snippet-start masterworker_ping
    /**
     * Sets the period at which ping messages are sent to the workers <br/>
     * @param periodMillis the new ping period
     */
    void setPingPeriod(long periodMillis);
    //@snippet-end masterworker_ping
}
