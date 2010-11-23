/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.selection;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
import org.ow2.proactive.scripting.ScriptException;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;


public class ScriptExecutor implements Callable<Node> {

    private final static Logger logger = ProActiveLogger.getLogger(RMLoggers.RMSELECTION);
    private final static int TIMEOUT = PAResourceManagerProperties.RM_SELECT_SCRIPT_TIMEOUT.getValueAsInt();
    private RMNode rmnode;
    private SelectionManager manager;
    private List<SelectionScript> selectionScriptList;

    public ScriptExecutor(RMNode rmnode, List<SelectionScript> selectionScriptList, SelectionManager manager) {
        this.rmnode = rmnode;
        this.manager = manager;
        this.selectionScriptList = selectionScriptList;
    }

    /**
     * Runs selection scripts and process the results
     * returns node if it matches, null otherwise
     */
    public Node call() throws Exception {

        //LinkedList<ScriptResult<Boolean>> scriptExecitionResults = new LinkedList<ScriptResult<Boolean>>();
        boolean selectionScriptSpecified = selectionScriptList != null && selectionScriptList.size() > 0;
        boolean nodeMatch = true;
        ScriptException exception = null;

        if (selectionScriptSpecified) {
            // initializing parallel script execution
            for (SelectionScript script : selectionScriptList) {
                if (manager.isPassed(script, rmnode)) {
                    // already executed static script
                    logger.info("Skipping script execution " + script.hashCode() + " on node " +
                        rmnode.getNodeURL());
                    //scriptExecitionResults.add(new ScriptResult<Boolean>(true));
                    continue;
                }

                logger.info("Executing script " + script.hashCode() + " on node " + rmnode.getNodeURL());
                ScriptResult<Boolean> scriptResult = rmnode.executeScript(script);

                // SCHEDULING-883 : scripts must be executed sequentially to avoid unexpected script side effect
                //scriptExecitionResults.add(scriptResult);

                // processing the results
                if (!MOP.isReifiedObject(scriptResult) && scriptResult.getException() != null) {
                    // could not create script execution handler
                    // probably the node id down
                    logger.warn("Cannot execute script " + script.hashCode() + " on the node " +
                        rmnode.getNodeURL(), scriptResult.getException());
                    logger.warn("Checking if the node " + rmnode.getNodeURL() + " is still alive");
                    rmnode.getNodeSource().pingNode(rmnode.getNodeURL());

                    nodeMatch = false;
                    break;
                } else {

                    try {
                        PAFuture.waitFor(scriptResult, TIMEOUT);
                    } catch (ProActiveTimeoutException e) {
                        // do not produce an exception here
                        nodeMatch = false;
                        break;
                    }

                    if (scriptResult != null && scriptResult.errorOccured()) {
                        nodeMatch = false;
                        exception = new ScriptException(scriptResult.getException());
                        break;
                    }

                    // processing script result and updating knowledge base of
                    // selection manager at the same time. Returns whether node is selected.
                    if (!manager.processScriptResult(script, scriptResult, rmnode)) {
                        nodeMatch = false;
                    }

                }
            }
        }

        // cleaning the node
        try {
            rmnode.clean();
        } catch (Throwable t) {
            logger.warn("Exception while cleaning the node " + rmnode.getNodeURL() + ": " + t.getMessage());
            logger.warn("Checking if the node " + rmnode.getNodeURL() + " is alive");
            rmnode.getNodeSource().pingNode(rmnode.getNodeURL());
        }

        manager.scriptExecutionFinished(rmnode.getNodeURL());
        if (logger.isDebugEnabled())
            logger.debug("Node " + rmnode.getNodeURL() + " matched: " + nodeMatch);

        if (exception != null) {
            throw exception;
        }
        if (nodeMatch) {
            return rmnode.getNode();
        }
        return null;
    }

    public String toString() {
        boolean selectionScriptSpecified = selectionScriptList != null && selectionScriptList.size() > 0;
        if (selectionScriptSpecified) {
            String result = "script execution on the node " + rmnode.getNodeURL() +
                " using the following scripts\n";
            for (SelectionScript ss : selectionScriptList) {
                result += ss.getScript() + "\n";
            }

            return result;
        } else {
            return "the node communication " + rmnode.getNodeURL();
        }
    }
}
