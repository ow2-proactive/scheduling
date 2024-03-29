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
import java.util.HashSet;
import java.util.Set;

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
    private static final String NODES_COUNT_KEY = "nodesCount";

    /**
     * key to retrieve the set of URL of node that are down in the
     * {@link InfrastructureManager#persistedInfraVariables} map
     */
    private static final String DOWN_NODES_URL_KEY = "downNodesUrl";

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
            String nodeUrl = node.getNodeInformation().getURL();
            if (containsDownNodeUrl(nodeUrl)) {
                removeDownNodeUrl(nodeUrl);
            } else if (!isThereNodesInSameJVM(node)) {
                this.nodeSource.executeInParallel(() -> {
                    try {
                        logger.info("Terminating the runtime " + node.getProActiveRuntime().getURL());
                        node.getProActiveRuntime().killRT(false);
                    } catch (Exception e) {
                        // do nothing, no exception treatment for node just
                        // killed before
                    }
                });
            }
            decrementNodesCount();
        } catch (Exception e) {
            throw new RMException(e);
        }
    }

    /**
     * Node source string representation
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    /**
     * Node source description
     */
    @Override
    public String getDescription() {
        return "A default infrastructure manager.";
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
        incrementNodesCount();
        removeDownNodeUrl(node.getNodeInformation().getURL());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyDownNode(String nodeName, String nodeUrl, Node proactiveProgrammingNode) throws RMException {
        addDownNodeUrl(nodeUrl);
    }

    @Override
    public void onDownNodeReconnection(Node node) {
        removeDownNodeUrl(node.getNodeInformation().getURL());
    }

    @Override
    public void removeDownNodePriorToNotify(String nodeName) {
        logger.info("[" + getClass().getSimpleName() + "] Node removal skipped because the node is down: " + nodeName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutDown() {
    }

    @Override
    protected void initializePersistedInfraVariables() {
        this.persistedInfraVariables.put(NODES_COUNT_KEY, 0);
        this.persistedInfraVariables.put(DOWN_NODES_URL_KEY, new HashSet<>());
    }

    // Below are wrapper methods around the runtime variables map

    private void incrementNodesCount() {
        setPersistedInfraVariable(() -> {
            int updated = (int) this.persistedInfraVariables.get(NODES_COUNT_KEY) + 1;
            this.persistedInfraVariables.put(NODES_COUNT_KEY, updated);
            return null;
        });
    }

    private void decrementNodesCount() {
        setPersistedInfraVariable(() -> {
            int updated = (int) this.persistedInfraVariables.get(NODES_COUNT_KEY) - 1;
            this.persistedInfraVariables.put(NODES_COUNT_KEY, updated);
            return null;
        });
    }

    private void addDownNodeUrl(String downNodeUrl) {
        setPersistedInfraVariable(() -> {
            ((Set<String>) this.persistedInfraVariables.get(DOWN_NODES_URL_KEY)).add(downNodeUrl);
            return null;
        });
    }

    private boolean containsDownNodeUrl(String downNodeUrl) {
        return getPersistedInfraVariable(() -> ((Set<String>) this.persistedInfraVariables.get(DOWN_NODES_URL_KEY)).contains(downNodeUrl));
    }

    private void removeDownNodeUrl(String notDownNodeUrl) {
        setPersistedInfraVariable(() -> {
            ((Set<String>) this.persistedInfraVariables.get(DOWN_NODES_URL_KEY)).remove(notDownNodeUrl);
            return null;
        });
    }

}
