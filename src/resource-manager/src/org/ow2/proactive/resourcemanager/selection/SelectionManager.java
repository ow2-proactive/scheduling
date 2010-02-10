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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
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

    private ExecutorService scriptExecutorThreadPool = Executors
            .newFixedThreadPool(PAResourceManagerProperties.RM_SELECTION_MAX_THREAD_NUMBER.getValueAsInt());

    private Set<String> inProgress = Collections.synchronizedSet(new HashSet<String>());

    public SelectionManager() {
    }

    public SelectionManager(RMCore rmcore) {
        this.rmcore = rmcore;
    }

    /**
     * Arranges nodes for script execution, taking into
     * account "free" and "exclusion" nodes lists.  
     * 
     * @param selectionScriptList - set of scripts to execute
     * @param freeNodes - free nodes list provided by resource manager
     * @param exclusionNodes - exclusion nodes list
     * @return collection of arranged nodes
     */
    public abstract Collection<RMNode> arrangeNodesForScriptExecution(
            List<SelectionScript> selectionScriptList, final Collection<RMNode> freeNodes,
            NodeSet exclusionNodes);

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

    public NodeSet findAppropriateNodes(int nb, List<SelectionScript> selectionScriptList, NodeSet exclusion) {

        ArrayList<RMNode> freeNodes = rmcore.getFreeNodes();
        NodeSet result = new NodeSet();
        // getting sorted by probability candidates nodes from selection manager
        Collection<RMNode> candidatesNodes = arrangeNodesForScriptExecution(selectionScriptList, freeNodes,
                exclusion);

        // here we will contact the node either for script execution and cleaning or just
        // for cleaning.
        // So parallelizing this action and delegate execution to the thread pool
        Iterator<RMNode> nodesIterator = candidatesNodes.iterator();

        while (nodesIterator.hasNext() && result.size() < nb) {
            int numberOfNodesNeeded = nb - result.size();

            ArrayList<Callable<RMNode>> scriptExecutors = new ArrayList<Callable<RMNode>>();
            for (int i = 0; i < numberOfNodesNeeded && nodesIterator.hasNext(); i++) {
                RMNode rmnode = nodesIterator.next();
                if (inProgress.contains(rmnode.getNodeURL())) {
                    if (logger.isDebugEnabled())
                        logger.debug("Script execution is in progress on node " + rmnode.getNodeURL() +
                            " - skipping.");
                    i--;
                } else {
                    inProgress.add(rmnode.getNodeURL());
                    scriptExecutors.add(new ScriptExecutor(rmnode, selectionScriptList, this));
                }
            }

            try {
                Collection<Future<RMNode>> matchedNodes = scriptExecutorThreadPool.invokeAll(scriptExecutors,
                        MAX_VERIF_TIMEOUT, TimeUnit.MILLISECONDS);
                int index = 0;

                for (Future<RMNode> futureNode : matchedNodes) {
                    if (!futureNode.isCancelled()) {
                        RMNode node = null;
                        try {
                            node = futureNode.get();
                        } catch (InterruptedException e) {
                            logger.warn("Interrupting the selection manager");
                            return result;
                        } catch (ExecutionException e) {
                            throw (RuntimeException) e.getCause();
                        }
                        if (node != null) {
                            try {
                                rmcore.setBusyNode(node.getNodeURL());
                                result.add(node.getNode());
                            } catch (NodeException e) {
                                rmcore.setDownNode(node.getNodeURL());
                            }
                        }
                    } else {
                        // no script result was obtained
                        logger.warn("Timeout on " + scriptExecutors.get(index));
                    }
                    index++;
                }
            } catch (InterruptedException e1) {
                logger.warn("Interrupting the selection manager");
                return result;
            }

        }

        logger.info("Number of found nodes is " + result.size());
        return result;

    }

    /**
     * Indicates that script execution is finished for the node with specified url.
     */
    public synchronized void scriptExecutionFinished(String nodeUrl) {
        inProgress.remove(nodeUrl);
    }
}
