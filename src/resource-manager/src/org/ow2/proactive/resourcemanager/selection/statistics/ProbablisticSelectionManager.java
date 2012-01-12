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
package org.ow2.proactive.resourcemanager.selection.statistics;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.selection.SelectionManager;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;


/**
 * An implementation of {@link SelectionManager} interface, based on
 * probabilistic approach. The purpose of selection manager is to choose the nodes for
 * further selection scripts execution. The goal is to find optimal strategy
 * of nodes selection from the nodes pool to minimize scripts execution.
 *
 * In order to do that we assume that script will pass on the node with
 * some probability. This probability will be increased or decreased accordingly
 * depending on execution results.
 *
 * The selection of nodes sorted by its probability for each particular script
 * gives an optimal strategy for scripts execution. For several scripts join probabilities
 * are calculated for each nodes.
 *
 */
@ActiveObject
public class ProbablisticSelectionManager extends SelectionManager {

    private final static Logger logger = ProActiveLogger.getLogger(RMLoggers.RMSELECTION);

    // contains an information about already executed scripts
    // script digest => node => probability
    private HashMap<String, HashMap<RMNode, Probability>> probabilities;

    // in order to avoid OOM when the number of scripts exceeds the limit
    // we could :
    // 1. Reset all the probabilities for all scripts (simple but long to recover performance)
    // 2. Remove the oldest script execution results (too expensive and complicated)
    //	  need to store the time, update it each time, then sort when removing
    //    the system will be too CPU consuming working on the limit
    // 3. Removed the oldest added script. For this we have this queue. 
    private LinkedList<String> digestQueue = new LinkedList<String>();

    public ProbablisticSelectionManager() {
    }

    public ProbablisticSelectionManager(RMCore rmcore) {
        super(rmcore);
        this.probabilities = new HashMap<String, HashMap<RMNode, Probability>>();
    }

    /**
     * Find appropriate candidates nodes for script execution, taking into
     * account "free" and "exclusion" nodes lists.
     *
     * @param selectionScriptList - set of scripts to execute
     * @param freeNodes - free nodes list provided by resource manager
     * @param exclusionNodes - exclusion nodes list
     * @return candidates node list for script execution
     */
    @Override
    public List<RMNode> arrangeNodesForScriptExecution(final List<RMNode> nodes, List<SelectionScript> scripts) {

        long startTime = System.currentTimeMillis();
        logger.debug("Arranging nodes");

        boolean scriptSpecified = scripts != null && scripts.size() > 0;

        // if no scripts are specified return filtered free nodes
        if (!scriptSpecified) {
            logger.debug("Selection script was not specified");
            return nodes;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Selection scripts count is " + scripts.size() + ": ");
            for (SelectionScript script : scripts) {
                logger.debug("Content of the script with id " + script.hashCode() + ":\n" +
                    script.getScript());
            }
        }

        try {
            // finding intersection
            HashMap<RMNode, Probability> intersectionMap = new HashMap<RMNode, Probability>();
            for (RMNode rmnode : nodes) {
                boolean intersection = true;
                double intersectionProbability = 1;
                for (SelectionScript script : scripts) {
                    String digest = new String(script.digest());
                    if (probabilities.containsKey(digest) && probabilities.get(digest).containsKey(rmnode)) {
                        double probability = probabilities.get(digest).get(rmnode).value();
                        if (probability == 0) {
                            intersection = false;
                            break;
                        } else {
                            intersectionProbability *= probability;
                        }
                    } else {
                        intersectionProbability *= Probability.defaultValue();
                    }
                }

                if (intersection) {
                    intersectionMap.put(rmnode, new Probability(intersectionProbability));
                }
            }

            // sorting results based on calculated probability
            List<RMNode> res = new ArrayList<RMNode>();
            res.addAll(intersectionMap.keySet());
            Collections.sort(res, new NodeProbabilityComparator(intersectionMap));

            if (logger.isDebugEnabled()) {
                logger.debug("The following nodes are selected for scripts execution (time is " +
                    (System.currentTimeMillis() - startTime) + " ms) :");
                if (res.size() > 0) {
                    for (RMNode rmnode : res) {
                        logger.debug("Node url: " + rmnode.getNodeURL() + ", probability: " +
                            intersectionMap.get(rmnode));
                    }
                } else {
                    logger.debug("None");
                }
            }
            return res;
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
            return new ArrayList<RMNode>();
        }
    }

    /**
     * Predicts script execution result. Allows to avoid duplicate script execution
     * on the same node.
     *
     * @param script - script to execute
     * @param rmnode - target node
     * @return true if script will pass on the node
     */
    @Override
    public synchronized boolean isPassed(SelectionScript script, RMNode rmnode) {
        String digest;
        try {
            digest = new String(script.digest());
            if (probabilities.containsKey(digest) && probabilities.get(digest).containsKey(rmnode)) {
                Probability p = probabilities.get(digest).get(rmnode);
                String scriptType = script.isDynamic() ? "dynamic" : "static";
                if (logger.isDebugEnabled())
                    logger.debug("Known " + scriptType + " script " + script.hashCode() + " for node " +
                        rmnode.getNodeURL());
                return p.value() == 1;
            }
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        }

        if (logger.isDebugEnabled())
            logger.debug("Unknown script " + script.hashCode() + " for node " + rmnode.getNodeURL());
        return false;
    }

    /**
     * Processes script result and updates knowledge base of
     * selection manager at the same time.
     *
     * @param script - executed script
     * @param scriptResult - obtained script result
     * @param rmnode - node on which script has been executed
     * @return whether node is selected
     */
    @Override
    public synchronized boolean processScriptResult(SelectionScript script,
            ScriptResult<Boolean> scriptResult, RMNode rmnode) {

        boolean result = false;

        try {
            String digest = new String(script.digest());
            Probability probability = new Probability(Probability.defaultValue());
            if (probabilities.containsKey(digest) && probabilities.get(digest).containsKey(rmnode)) {
                probability = probabilities.get(digest).get(rmnode);
                assert (probability.value() >= 0 && probability.value() <= 1);
            }

            if (scriptResult == null || scriptResult != null && scriptResult.errorOccured()) {
                // error during script execution
            } else if (!scriptResult.getResult()) {
                // script failed
                if (script.isDynamic()) {
                    probability.decrease();
                } else {
                    probability = Probability.ZERO;
                }
            } else {
                // script passed
                result = true;
                if (script.isDynamic()) {
                    probability.increase();
                } else {
                    probability = Probability.ONE;
                }
            }

            if (!probabilities.containsKey(digest)) {
                // checking if the number of selection script does not exceeded the maximum
                if (probabilities.size() >= PAResourceManagerProperties.RM_SELECT_SCRIPT_CACHE_SIZE
                        .getValueAsInt()) {
                    String oldest = digestQueue.poll();
                    probabilities.remove(oldest);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Removing the script: " + script.hashCode() +
                            " from the data base because the limit is reached");
                    }
                }
                // adding a new script record
                probabilities.put(digest, new HashMap<RMNode, Probability>());
                logger.debug("Scripts cache size " + probabilities.size());
                digestQueue.offer(digest);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Adding data to knowledge base - script: " + script.hashCode() + ", node: " +
                    rmnode.getNodeURL() + ", probability: " + probability);
            }

            probabilities.get(digest).put(rmnode, probability);

        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        }

        return result;
    }

    /**
     * @see org.ow2.proactive.authentication.Loggable#getLogger()
     */
    public Logger getLogger() {
        return logger;
    }

}
