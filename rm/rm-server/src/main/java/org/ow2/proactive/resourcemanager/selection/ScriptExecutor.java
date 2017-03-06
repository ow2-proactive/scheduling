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
package org.ow2.proactive.resourcemanager.selection;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.scripting.ScriptException;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.Criteria;


public class ScriptExecutor implements Callable<Node> {

    private final static Logger logger = Logger.getLogger(ScriptExecutor.class);

    private final Criteria criteria;

    private RMNode rmnode;

    private SelectionManager manager;

    private List<SelectionScript> selectionScriptList;

    public ScriptExecutor(RMNode rmnode, Criteria criteria, SelectionManager manager) {
        this.rmnode = rmnode;
        this.manager = manager;
        this.criteria = criteria;
        this.selectionScriptList = criteria.getScripts();
    }

    public Node call() throws Exception {
        SelectionManager.maybeSetLoggingContext(criteria);
        try {
            return executeScripts();
        } finally {
            SelectionManager.unsetLoggingContext();
        }
    }

    /**
     * Runs selection scripts and process the results
     * returns node if it matches, null otherwise
     */
    private Node executeScripts() {
        boolean selectionScriptSpecified = selectionScriptList != null && selectionScriptList.size() > 0;
        boolean nodeMatch = true;
        ScriptException exception = null;

        if (selectionScriptSpecified) {
            // initializing parallel script execution
            for (SelectionScript script : selectionScriptList) {
                if (manager.isPassed(script, rmnode)) {
                    // already executed static script
                    logger.debug(rmnode.getNodeURL() + " : " + script.hashCode() + " skipping script execution");
                    continue;
                }

                logger.info(rmnode.getNodeURL() + " : " + script.hashCode() + " executing");
                try {
                    ScriptResult<Boolean> scriptResult = rmnode.executeScript(script, criteria.getBindings());

                    // processing the results
                    if (!MOP.isReifiedObject(scriptResult) && scriptResult.getException() != null) {
                        // could not create script execution handler
                        // probably the node id down
                        logger.warn(rmnode.getNodeURL() + " : " + script.hashCode() + " exception",
                                    scriptResult.getException());
                        logger.warn(rmnode.getNodeURL() + " : pinging the node");
                        rmnode.getNodeSource().pingNode(rmnode.getNode());

                        nodeMatch = false;
                        break;
                    } else {

                        try {
                            PAFuture.waitFor(scriptResult,
                                             PAResourceManagerProperties.RM_SELECT_SCRIPT_TIMEOUT.getValueAsInt());
                        } catch (ProActiveTimeoutException e) {
                            // do not produce an exception here
                            nodeMatch = false;
                            break;
                        }

                        // display the script result and output in the scheduler logs
                        if (scriptResult != null && logger.isInfoEnabled()) {
                            logger.info(rmnode.getNodeURL() + " : " + script.hashCode() + " result " +
                                        scriptResult.getResult());

                            if (scriptResult.getOutput() != null && scriptResult.getOutput().length() > 0) {
                                logger.info(rmnode.getNodeURL() + " : " + script.hashCode() + " output\n" +
                                            scriptResult.getOutput());
                            }
                        }

                        if (scriptResult != null && scriptResult.errorOccured()) {
                            nodeMatch = false;
                            exception = new ScriptException(scriptResult.getException());
                            logger.warn(rmnode.getNodeURL() + " : exception during the script execution",
                                        scriptResult.getException());
                            break;
                        }

                        // processing script result and updating knowledge base of
                        // selection manager at the same time. Returns whether node is selected.
                        if (!manager.processScriptResult(script, scriptResult, rmnode)) {
                            nodeMatch = false;
                            break;
                        }

                    }
                } catch (Exception ex) {
                    // proactive or network exception occurred when script was executed
                    logger.warn(rmnode.getNodeURL() + " : " + script.hashCode() + " exception", ex);
                    nodeMatch = false;
                    exception = new ScriptException(ex);
                    break;
                }
            }
        }

        manager.scriptExecutionFinished(rmnode.getNodeURL());
        if (selectionScriptSpecified && logger.isDebugEnabled()) {
            if (nodeMatch) {
                logger.debug(rmnode.getNodeURL() + " : selected");
            } else {
                logger.debug(rmnode.getNodeURL() + " : not selected");
            }
        }

        // cleaning the node
        try {
            rmnode.clean();
        } catch (Throwable t) {
            logger.warn(rmnode.getNodeURL() + " : exception in cleaning", t);
            logger.warn(rmnode.getNodeURL() + " : pinging the node");
            try {
                // 'pingNode' call can fail with exception if NodeSource was destroyed
                rmnode.getNodeSource().pingNode(rmnode.getNode());
            } catch (Throwable pingError) {
                logger.warn(rmnode.getNodeURL() + " : nodeSource " + rmnode.getNodeSourceName() +
                            " seems to be removed ", pingError);
            }
            return null;
        }

        if (exception != null) {
            throw exception;
        }
        if (nodeMatch) {
            return rmnode.getNode();
        } else {
            return null;
        }
    }

    public String toString() {
        boolean selectionScriptSpecified = selectionScriptList != null && selectionScriptList.size() > 0;
        if (selectionScriptSpecified) {
            String result = "script execution on the node " + rmnode.getNodeURL() + " using the following scripts\n";
            for (SelectionScript ss : selectionScriptList) {
                result += ss.getScript() + "\n";
            }

            return result;
        } else {
            return "the node communication " + rmnode.getNodeURL();
        }
    }

    /**
     * Gets the RM node on which the script must be executed
     */
    public RMNode getRMNode() {
        return rmnode;
    }
}
