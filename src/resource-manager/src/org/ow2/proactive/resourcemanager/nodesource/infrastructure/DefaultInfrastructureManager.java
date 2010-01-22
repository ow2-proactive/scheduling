/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.rmi.dgc.VMID;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.utils.NamesConvertor;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 *
 * A default infrastructure manager.
 * Cannot perform the deployment but is able to register incoming nodes
 * (existing nodes added by url) and remove them.
 *
 */
public class DefaultInfrastructureManager extends InfrastructureManager {

    /**  */
    private static final long serialVersionUID = 200;
    /** logger*/
    protected static Logger logger = ProActiveLogger.getLogger(RMLoggers.NODESOURCE);
    /** registered nodes number */
    protected int nodesCount = 0;

    /**
     * Proactive default constructor.
     */
    public DefaultInfrastructureManager() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(Object... parameters) throws RMException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void acquireNode() {
        logger.error("acquireNode() is invalid operation");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void acquireAllNodes() {
        logger.error("acquireAllNodes() is invalid operation");
    }

    /**
     * Remove the node from underlying infrastructure. Terminates proactive runtime if there is no more nodes.
     * @param node node to release
     * @throws RMException if any problems occurred
     */
    @Override
    public void removeNode(Node node) throws RMException {
        try {
            logger.info("Terminating the node " + node.getNodeInformation().getName());
            if (!isThereNodesInSameJVM(node)) {
                final Node n = node;
                nodeSource.executeInParallel(new Runnable() {
                    public void run() {
                        try {
                            logger.info("Terminating the runtime " + n.getProActiveRuntime().getURL());
                            n.getProActiveRuntime().killRT(false);
                        } catch (Exception e) {
                            //do nothing, no exception treatment for node just killed before
                        }
                    }
                });
            }
            nodesCount--;
        } catch (Exception e) {
            throw new RMException(e);
        }
    }

    /**
     * Node source string representation
     */
    @Override
    public String toString() {
        return NamesConvertor.beautifyName(this.getClass().getSimpleName());
    }

    /**
     * Node source description
     */
    public String getDescription() {
        return "Default infrastructure";
    }

    /**
     * Check if there are any other nodes handled by the NodeSource in the same JVM of the node
     * passed in parameter.
     * @param node Node to check if there any other node of the NodeSource in the same JVM
     * @return true there is another node in the node's JVM handled by the nodeSource, false otherwise.
     */
    public boolean isThereNodesInSameJVM(Node node) {
        VMID nodeID = node.getVMInformation().getVMID();
        String nodeToTestUrl = node.getNodeInformation().getURL();
        Collection<Node> nodesList = nodeSource.getAliveNodes();
        for (Node n : nodesList) {
            if (!n.getNodeInformation().getURL().equals(nodeToTestUrl) &&
                n.getVMInformation().getVMID().equals(nodeID)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerAcquiredNode(Node node) throws RMException {
        nodesCount++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutDown() {
    }
}
