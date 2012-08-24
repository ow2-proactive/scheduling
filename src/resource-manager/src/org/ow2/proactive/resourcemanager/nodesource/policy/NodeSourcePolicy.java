/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.nodesource.utils.NamesConvertor;


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
@ActiveObject
public abstract class NodeSourcePolicy implements Serializable {

    /** logger */
    private static Logger logger = Logger.getLogger(NodeSourcePolicy.class);
    /** Node source of the policy */
    protected NodeSource nodeSource;

    // Users who can get nodes for computations from this node source
    @Configurable(description = "ME|users=name1,name2,groups=group1,group2|ALL")
    private AccessType userAccessType = AccessType.ALL;

    // Users who can add/remove nodes to/from this node source
    @Configurable(description = "ME|users=name1,name2,groups=group1,group2|ALL")
    private AccessType providerAccessType = AccessType.ME;

    /**
     * Configure a policy with given parameters.
     * @param policyParameters parameters defined by user
     * @throws IllegalArgumentException if parameters are incorrect
     */
    public BooleanWrapper configure(Object... policyParameters) {
        if (policyParameters != null && policyParameters.length >= 2) {
            try {
                userAccessType = AccessType.valueOf(policyParameters[0].toString());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Incorrect parameter value " + policyParameters[0]);
            }
            try {
                providerAccessType = AccessType.valueOf(policyParameters[1].toString());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Incorrect parameter value " + policyParameters[1]);
            }
        }

        // else using default values
        return new BooleanWrapper(true);
    }

    /**
     * Activates the policy.
     * @return true if the policy has been activated successfully, false otherwise.
     */
    public abstract BooleanWrapper activate();

    /**
     * Shutdown the policy
     */
    public void shutdown(Client initiator) {
        nodeSource.finishNodeSourceShutdown(initiator);
        // the policy shutdown is finished and it has to be removed from clients
        // list of the resource manager
        nodeSource.getRMCore().disconnect(Client.getId(PAActiveObject.getStubOnThis()));

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
        Thread.currentThread().setName("Node Source Policy \"" + nodeSource.getName() + "\"");
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
     * Removes a node from the node source
     *
     * @param url the URL of the node
     * @param preemtive if true remove nodes immediately without waiting while they will be freed
     */
    protected void removeNode(String url, boolean preemptive) {
        info("Removing node at " + url);
        nodeSource.getRMCore().removeNode(url, preemptive);
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

    /**
     * Returns a node source administration type.
     * Could be ME, MY_GROUPS, ALL
     *
     * @return a node source administration type
     */
    public AccessType getProviderAccessType() {
        return providerAccessType;
    }

    /**
     * Returns a nodes access type.
     * Could be ME, MY_GROUPS, PROVIDER, PROVIDER_GROUPS, ALL
     *
     * @return a nodes access type
     */
    public AccessType getUserAccessType() {
        return userAccessType;
    }

    /**
     * Policy string representation.
     */
    @Override
    public String toString() {
        return NamesConvertor.beautifyName(this.getClass().getSimpleName()) + " user access type [" +
            userAccessType + "]" + ", provider access type [" + providerAccessType + "]";
    }
}
