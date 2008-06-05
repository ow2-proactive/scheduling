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
package org.objectweb.proactive.extensions.masterworker.interfaces.internal;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;

import java.net.URL;
import java.util.Collection;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * Admin interface of the Worker Manager in the Master/Worker API (allows to extend the pool of workers, or terminate the manager)<br/>
 * @author The ProActive Team
 *
 */
public interface WorkerManager extends WorkerDeadListener {

    /**
     * Asks the worker manager to activate every virtual nodes inside the given descriptor
     * and use the generated nodes as resources
     * @param descriptorURL URL of a deployment descriptor
     */
    void addResources(URL descriptorURL) throws ProActiveException;

    /**
     * Asks the worker manager to activate the given virtual nodes inside the given descriptor
     * and use the generated nodes as resources
     * @param descriptorURL URL of a deployment descriptor
     * @param virtualNodeName names of the virtual node to activate
     */
    void addResources(URL descriptorURL, String virtualNodeName) throws ProActiveException;

    /**
     * Adds the given Collection of nodes to the worker manager
     * @param nodes a collection of nodes
     */
    void addResources(Collection<Node> nodes);

    /**
     * Connects to a running scheduler by providing URL, login and password
     * @param schedulerURL url of the running scheduler
     * @param user username
     * @param password password
     * @throws ProActiveException if the scheduler cannot be found or if the login fails
     */
    void addResources(final String schedulerURL, String user, String password) throws ProActiveException;

    /**
     * Terminates the worker manager and free every resources (if asked)
     * @param freeResources tells if the Worker Manager should as well free the node resources
     * @return success
     */
    BooleanWrapper terminate(boolean freeResources);

}
