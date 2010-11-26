/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
package org.ow2.proactive.resourcemanager.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.selection.topology.TopologyHandler;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;


/**
 * An interface of selection manager which is responsible for
 * nodes selection from a pool of free nodes for further scripts execution. 
 *
 */
public abstract class SelectionManager {

    private final static Logger logger = ProActiveLogger.getLogger(RMLoggers.RMSELECTION);

    /** Timeout for selection script result */
    private static final int MAX_VERIF_TIMEOUT = PAResourceManagerProperties.RM_SELECT_SCRIPT_TIMEOUT
            .getValueAsInt();

    private RMCore rmcore;

    private static final int SELECTION_THEADS_NUMBER = PAResourceManagerProperties.RM_SELECTION_MAX_THREAD_NUMBER
            .getValueAsInt();

    private ExecutorService scriptExecutorThreadPool = Executors.newFixedThreadPool(SELECTION_THEADS_NUMBER);

    private Set<String> inProgress = Collections.synchronizedSet(new HashSet<String>());

    public SelectionManager() {
    }

    public SelectionManager(RMCore rmcore) {
        this.rmcore = rmcore;
    }

    /**
     * Arranges nodes for script execution based on some criteria
     * for example previous execution statistics.
     * 
     * @param nodes - nodes list for script execution
     * @param scripts - set of selection scripts
     * @return collection of arranged nodes
     */
    public abstract List<RMNode> arrangeNodes(final List<RMNode> nodes, List<SelectionScript> scripts);

    /**
     * Predicts script execution result. Allows to avoid duplicate script execution 
     * on the same node. 
     * 
     * @param script - script to execute
     * @param rmnode - target node
     * @return true if script will pass on the node 
     */
    public abstract boolean isPassed(SelectionScript script, RMNode rmnode);

    /**
     * Processes script result and updates knowledge base of 
     * selection manager at the same time.
     *
     * @param script - executed script
     * @param scriptResult - obtained script result
     * @param rmnode - node on which script has been executed
     * @return whether node is selected
     */
    public abstract boolean processScriptResult(SelectionScript script, ScriptResult<Boolean> scriptResult,
            RMNode rmnode);

    public NodeSet selectNodes(int number, TopologyDescriptor topologyDescriptor,
            List<SelectionScript> scripts, NodeSet exclusion, Client client) {

        // can throw Exception if topology is disabled
        TopologyHandler handler = RMCore.topologyManager.getHandler(topologyDescriptor);

        List<RMNode> freeNodes = rmcore.getFreeNodes();
        // filtering out the "free node list"
        // removing exclusion and checking permissions
        List<RMNode> filteredNodes = filterOut(freeNodes, exclusion, client);

        if (filteredNodes.size() == 0) {
            return new NodeSet();
        }

        // arranging nodes for script execution
        List<RMNode> arrangedNodes = arrangeNodes(filteredNodes, scripts);

        List<Node> matchedNodes = null;
        if (topologyDescriptor.isTopologyBased()) {
            // run scripts on all available nodes
            matchedNodes = runScripts(arrangedNodes, scripts);
        } else {
            // run scripts not on all nodes, but always on missing number of nodes
            // until required node set is found
            matchedNodes = new LinkedList<Node>();
            while (matchedNodes.size() < number) {
                int requiredNodesNumber = number - matchedNodes.size();
                int numberOfNodesForScriptExecution = requiredNodesNumber;

                if (numberOfNodesForScriptExecution < SELECTION_THEADS_NUMBER) {
                    // we can run "SELECTION_THEADS_NUMBER" scripts in parallel
                    // in case when we need less nodes it still useful to
                    // the full capacity of the thread pool to find nodes quicker

                    // it is not important if we find more nodes than needed
                    // subset will be selected later (topology handlers)
                    numberOfNodesForScriptExecution = SELECTION_THEADS_NUMBER;
                }

                List<RMNode> subset = arrangedNodes.subList(0, Math.min(numberOfNodesForScriptExecution,
                        arrangedNodes.size()));
                matchedNodes.addAll(runScripts(subset, scripts));
                // removing subset of arrangedNodes
                subset.clear();

                if (arrangedNodes.size() == 0) {
                    break;
                }
            }
        }

        logger.info(matchedNodes.size() + " nodes found after scripts execution for " + client);

        // now we have a list of nodes which match to selection scripts
        // selecting subset according to topology requirements
        // TopologyHandler handler = RMCore.topologyManager.getHandler(topologyDescriptor);
        List<Node> selectedNodes = handler.select(number, matchedNodes);

        logger.info(selectedNodes.size() + " nodes found after the topology is taken into account for " +
            client);
        // the nodes are selected, now mark them as busy.
        NodeSet result = new NodeSet();
        for (Node node : selectedNodes) {
            try {
                // Synchronous call
                rmcore.setBusyNode(node.getNodeInformation().getURL(), client);
                result.add(node);
            } catch (NodeException e) {
                rmcore.setDownNode(node.getNodeInformation().getURL());
            }
        }

        return result;
    }

    /**
     * Runs scripts on given set of nodes and returns matched nodes.
     * It blocks until all results are obtained.
     *
     * @param candidates nodes to execute scripts on
     * @param scripts set of scripts to execute on each node
     * @return nodes matched to all scripts
     */
    private List<Node> runScripts(List<RMNode> candidates, List<SelectionScript> scripts) {
        List<Node> matched = new LinkedList<Node>();

        if (candidates.size() == 0) {
            return matched;
        }

        // creating script executors object to be run in dedicated thread pool
        List<Callable<Node>> scriptExecutors = new LinkedList<Callable<Node>>();
        synchronized (inProgress) {
            for (RMNode node : candidates) {
                if (inProgress.contains(node.getNodeURL())) {
                    if (logger.isDebugEnabled())
                        logger.debug("Script execution is in progress on node " + node.getNodeURL() +
                            " - skipping.");
                } else {
                    inProgress.add(node.getNodeURL());
                    scriptExecutors.add(new ScriptExecutor(node, scripts, this));
                }
            }
        }

        try {
            // launching
            Collection<Future<Node>> matchedNodes = scriptExecutorThreadPool.invokeAll(scriptExecutors,
                    MAX_VERIF_TIMEOUT, TimeUnit.MILLISECONDS);
            int index = 0;

            // waiting for the results
            for (Future<Node> futureNode : matchedNodes) {
                if (!futureNode.isCancelled()) {
                    Node node = null;
                    try {
                        node = futureNode.get();
                        if (node != null) {
                            matched.add(node);
                        }
                    } catch (InterruptedException e) {
                        logger.warn("Interrupting the selection manager");
                        return matched;
                    } catch (ExecutionException e) {
                        throw (RuntimeException) e.getCause();
                    }
                } else {
                    // no script result was obtained
                    logger.warn("Timeout on " + scriptExecutors.get(index));
                }
                index++;
            }
        } catch (InterruptedException e1) {
            logger.warn("Interrupting the selection manager");
        }

        return matched;
    }

    /**
     * Removes exclusion nodes and nodes not accessible for the client
     *
     * @param freeNodes
     * @param exclusion
     * @param client
     * @return
     */
    private List<RMNode> filterOut(List<RMNode> freeNodes, NodeSet exclusion, Client client) {

        List<RMNode> filteredList = new ArrayList<RMNode>();

        for (RMNode node : freeNodes) {
            // checking the permission
            try {
                client.checkPermission(node.getUserPermission(), client +
                    " is not authorized to get the node " + node.getNodeURL() + " from " +
                    node.getNodeSource().getName());
            } catch (SecurityException e) {
                // client does not have an access to this node
                logger.debug(e.getMessage());
                continue;
            }

            if (!contains(exclusion, node)) {
                filteredList.add(node);
            }
        }
        return filteredList;
    }

    /**
     * Indicates that script execution is finished for the node with specified url.
     */
    public void scriptExecutionFinished(String nodeUrl) {
        synchronized (inProgress) {
            inProgress.remove(nodeUrl);
        }
    }

    /**
     * Handles shut down of the selection manager
     */
    public void shutdown() {
        // shutdown the thread pool without waiting for script execution completions
        scriptExecutorThreadPool.shutdownNow();
        PAActiveObject.terminateActiveObject(false);
    }

    /**
     * Return true if node contains the node set.
     *
     * @param nodeset - a list of nodes to inspect
     * @param node - a node to find
     * @return true if node contains the node set.
     */
    private boolean contains(NodeSet nodeset, RMNode node) {
        if (nodeset == null)
            return false;

        for (Node n : nodeset) {
            try {
                if (n.getNodeInformation().getURL().equals(node.getNodeInformation().getURL())) {
                    return true;
                }
            } catch (Exception e) {
                continue;
            }
        }
        return false;
    }

}
