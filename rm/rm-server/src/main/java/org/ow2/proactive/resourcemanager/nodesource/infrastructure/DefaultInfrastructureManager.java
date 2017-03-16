/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.rmi.dgc.VMID;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.utils.NamesConvertor;


/**
 *
 * A default infrastructure manager. Cannot perform the deployment but is able
 * to register incoming nodes (existing nodes added by url) and remove them.
 *
 */
public class DefaultInfrastructureManager extends InfrastructureManager {

    /** logger */
    protected static Logger logger = Logger.getLogger(DefaultInfrastructureManager.class);

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
    public void configure(Object... parameters) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void acquireNode() {
        logger.info("acquireNode() implementation is empty for " + this.getClass().getSimpleName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void acquireAllNodes() {
        logger.info("acquireAllNodes() implementation is empty for " + this.getClass().getSimpleName());
    }

    /**
     * {@inheritDoc}
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
                            // do nothing, no exception treatment for node just
                            // killed before
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
    @Override
    public String getDescription() {
        return "Default infrastructure";
    }

    /**
     * Check if there are any other nodes handled by the NodeSource in the same
     * JVM of the node passed in parameter.
     * 
     * @param node
     *            Node to check if there any other node of the NodeSource in the
     *            same JVM
     * @return true there is another node in the node's JVM handled by the
     *         nodeSource, false otherwise.
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
    public void notifyAcquiredNode(Node node) throws RMException {
        nodesCount++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyDownNode(Node proactiveProgrammingNode) throws RMException {
        logger.info("[" + getClass().getSimpleName() + "] Node removal skipped because the node is down: " +
                    proactiveProgrammingNode.getNodeInformation().getURL());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutDown() {
    }

}
