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
package org.ow2.proactive.resourcemanager.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.RestrictedService;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
import org.ow2.proactive.scripting.ScriptException;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.ScriptWithResult;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;


/**
 * An interface of selection manager which is responsible for
 * nodes selection from a pool of free nodes for further scripts execution. 
 *
 */
public abstract class SelectionManager extends RestrictedService implements InitActive {

    private final static Logger logger = ProActiveLogger.getLogger(RMLoggers.RMSELECTION);

    /** Timeout for selection script result */
    private static final int MAX_VERIF_TIMEOUT = PAResourceManagerProperties.RM_SELECT_SCRIPT_TIMEOUT
            .getValueAsInt();

    private RMCore rmcore;

    public SelectionManager() {
    }

    public SelectionManager(RMCore rmcore) {
        this.rmcore = rmcore;
    }

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        registerTrustedService(rmcore);
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

    /**
     * Executes set of scripts on "number" nodes.
     * Returns "future" script results for further analysis.
     */
    private HashMap<RMNode, List<ScriptWithResult>> executeScripts(List<SelectionScript> selectionScriptList,
            Iterator<RMNode> nodesIterator, int number) {
        HashMap<RMNode, List<ScriptWithResult>> scriptExecutionResults = new HashMap<RMNode, List<ScriptWithResult>>();
        while (nodesIterator.hasNext() && scriptExecutionResults.keySet().size() < number) {
            RMNode rmnode = nodesIterator.next();
            scriptExecutionResults.put(rmnode, executeScripts(rmnode, selectionScriptList));
        }
        return scriptExecutionResults;
    }

    /**
    * Executes set of scripts on a given node.
    * Returns "future" script results for further analysis.
    */
    private List<ScriptWithResult> executeScripts(RMNode rmnode, List<SelectionScript> selectionScriptList) {
        List<ScriptWithResult> scriptExecitionResults = new LinkedList<ScriptWithResult>();

        for (SelectionScript script : selectionScriptList) {
            if (isPassed(script, rmnode)) {
                // already executed static script
                logger.info("Skipping script execution " + script.hashCode() + " on node " +
                    rmnode.getNodeURL());
                scriptExecitionResults.add(new ScriptWithResult(script, new ScriptResult<Boolean>(true)));
                continue;
            }

            logger.info("Executing script " + script.hashCode() + " on node " + rmnode.getNodeURL());
            ScriptResult<Boolean> scriptResult = rmnode.executeScript(script);
            scriptExecitionResults.add(new ScriptWithResult(script, scriptResult));
        }

        return scriptExecitionResults;
    }

    /**
     * Processes script execution results, updating selection manager knowledge base.
     * Returns a set of selected nodes.
     */
    private NodeSet processScriptsResults(HashMap<RMNode, List<ScriptWithResult>> scriptsExecutionResults) {

        // deadline for scripts execution
        long deadline = System.currentTimeMillis() + MAX_VERIF_TIMEOUT;

        NodeSet result = new NodeSet();
        for (RMNode rmnode : scriptsExecutionResults.keySet()) {
            // checking whether all scripts are passed or not for the node
            boolean scriptPassed = true;
            for (ScriptWithResult swr : scriptsExecutionResults.get(rmnode)) {
                ScriptResult<Boolean> scriptResult = swr.getScriptResult();

                if (!MOP.isReifiedObject(scriptResult)) {
                    // could not create script execution handler
                    // probably the node id down
                    logger.warn("Cannot execute script " + swr.getScript().hashCode() + " on the node " +
                        rmnode.getNodeURL() + " - " + scriptResult.getException().getMessage());
                    logger.warn("Checking if the node " + rmnode.getNodeURL() + " is still alive");
                    rmnode.getNodeSource().getPinger().pingNode(rmnode.getNodeURL());
                    continue;
                }

                try {
                    // calculating time to wait script result
                    long timeToWait = deadline - System.currentTimeMillis();
                    if (timeToWait <= 0)
                        timeToWait = 1; //ms
                    PAFuture.waitFor(scriptResult, timeToWait);
                } catch (ProActiveTimeoutException e) {
                    // no script result was obtained
                    scriptResult = null;
                    String message = "Time out while waiting the end of " + swr.getScript().hashCode() +
                        " script execution on the node " + rmnode.getNodeURL();
                    logger.warn(message);
                    throw new ScriptException(message, e);
                }

                if (scriptResult != null && scriptResult.errorOccured()) {
                    throw new ScriptException(scriptResult.getException());
                }

                // processing script result and updating knowledge base of
                // selection manager at the same time. Returns whether node is selected.
                if (!processScriptResult(swr.getScript(), scriptResult, rmnode)) {
                    scriptPassed = false;
                }
            }

            if (scriptPassed) {
                try {
                    rmnode.clean();
                    rmcore.setBusyNode(rmnode.getNodeURL());
                    result.add(rmnode.getNode());
                } catch (NodeException e) {
                    rmcore.setDownNode(rmnode.getNodeURL());
                }
            }
        }

        return result;
    }

    public NodeSet findAppropriateNodes(int nb, List<SelectionScript> selectionScriptList, NodeSet exclusion) {

        ArrayList<RMNode> freeNodes = rmcore.getFreeNodes();
        NodeSet result = new NodeSet();
        // getting sorted by probability candidates nodes from selection manager
        Collection<RMNode> candidatesNodes = arrangeNodesForScriptExecution(selectionScriptList, freeNodes,
                exclusion);
        boolean scriptSpecified = selectionScriptList != null && selectionScriptList.size() > 0;

        // if no script specified no execution is required
        // in this case selection manager just return a list of free nodes
        if (!scriptSpecified) {
            for (RMNode rmnode : candidatesNodes) {
                if (result.size() == nb) {
                    break;
                }
                try {
                    rmnode.clean();
                    rmcore.setBusyNode(rmnode.getNodeURL());
                    result.add(rmnode.getNode());
                } catch (NodeException e) {
                    rmcore.setDownNode(rmnode.getNodeURL());
                }
            }
            return result;
        }

        // scripts were specified
        // start execution on candidates set until we have enough
        // candidates or test each node
        Iterator<RMNode> nodesIterator = candidatesNodes.iterator();
        HashMap<RMNode, List<ScriptWithResult>> scriptsExecutionResults;

        while (nodesIterator.hasNext() && result.size() < nb) {
            scriptsExecutionResults = executeScripts(selectionScriptList, nodesIterator, nb - result.size());
            try {
                result.addAll(processScriptsResults(scriptsExecutionResults));
            } catch (ScriptException e) {
                rmcore.freeNodes(result);
                throw e;
            }
        }

        logger.info("Number of found nodes is " + result.size());
        return result;

    }
}
