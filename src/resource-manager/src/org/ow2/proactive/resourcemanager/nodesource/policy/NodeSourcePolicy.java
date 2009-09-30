/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.nodesource.policy;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 *
 * A node source policy is a set of rules of acquiring/releasing nodes.
 * It describes a time and conditions when and how many nodes have to be
 * acquired from the infrastructure.<br>
 *
 * NOTE: NodeSourcePolicy will be an active object. <br>
 *
 * To define a new node source policy
 * - implement {@link NodeSourcePolicy#configure(Object...)} and setup there all policy parameters which are available through UI
 * - define activation and disactivation policy behavior by implementing corresponding methods
 * - define how policy should react on nodes adding request initiated by user in {@link NodeSourcePolicy#handleUserChanges()}
 * - add the name of new policy class to the resource manager configuration file (config/rm/nodesource/policies).
 *
 */
public abstract class NodeSourcePolicy implements Serializable {

    /** logger */
    private static Logger logger = ProActiveLogger.getLogger(RMLoggers.POLICY);
    /** Node source of the policy */
    protected NodeSource nodeSource;

    /**
     * Configure a policy with given parameters.
     * @param policyParameters parameters defined by user
     * @throws RMException if parameters are incorrect
     */
    public abstract void configure(Object... policyParameters) throws RMException;

    /**
     * Activates the policy.
     * @return true if the policy has been activated successfully, false otherwise.
     */
    public abstract BooleanWrapper activate();

    /**
     * Shutdown the policy
     */
    public void shutdown() {
        nodeSource.finishNodeSourceShutdown();
        PAActiveObject.terminateActiveObject(false);
    }

    /**
     * Policy description for UI
     * @return policy description
     */
    public abstract String getDescription();

    /**
     * Sets a policy node source
     * @param nodeSource policy node source
     */
    public void setNodeSource(NodeSource nodeSource) {
        this.nodeSource = nodeSource;
    }

    /**
     * Asynchronous request to acquires n nodes from infrastructure
     * @param n number of nodes
     */
    public void acquireNodes(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Negative nodes number " + n);
        }
        if (n == 0) {
            return;
        }
        info("Acquiring " + n + " nodes");
        for (int i = 0; i < n; i++) {
            nodeSource.acquireNode();
        }
    }

    /**
     * Asynchronous request to acquires all possible nodes from infrastructure
     */
    protected void acquireAllNodes() {
        info("Acquiring all nodes");
        nodeSource.acquireAllNodes();
    }

    /**
     * Removes n nodes from the node source
     *
     * @param n number of nodes
     * @param preemtive if true remove nodes immediately without waiting while they will be freed
     */
    protected void removeNodes(int n, boolean preemptive) {
        info("Removing " + n + " nodes");
        nodeSource.getRMCore().removeNodes(n, this.nodeSource.getName(), preemptive);
    }

    /**
     * Removed all nodes from the node source
     * @param preemptive if true remove nodes immediately without waiting while they will be freed
     */
    protected void removeAllNodes(boolean preemptive) {
        info("Releasing all nodes");
        nodeSource.getRMCore().removeAllNodes(this.nodeSource.getName(), preemptive);
    }

    /**
     * Prints a formatted info message to the logging system
     * @param message to print
     */
    protected void info(String message) {
        logger.info("[" + nodeSource.getName() + "] " + message);
    }

    /**
     * Prints a formatted debug message to the logging system
     * @param message to print
     */
    protected void debug(String message) {
        logger.debug("[" + nodeSource.getName() + "] " + message);
    }
}
