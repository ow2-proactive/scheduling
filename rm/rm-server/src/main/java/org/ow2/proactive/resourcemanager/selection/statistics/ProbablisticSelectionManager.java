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
package org.ow2.proactive.resourcemanager.selection.statistics;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.selection.SelectionManager;
import org.ow2.proactive.scripting.InvalidScriptException;
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

    private final static Logger logger = Logger.getLogger(ProbablisticSelectionManager.class);

    // contains an information about already executed scripts
    // script digest => node => probability
    private HashMap<String, HashMap<String, Probability>> probabilities;

    // in order to avoid OOM when the number of scripts exceeds the limit
    // we could :
    // 1. Reset all the probabilities for all scripts (simple but long to recover performance)
    // 2. Remove the oldest script execution results (too expensive and complicated)
    //	  need to store the time, update it each time, then sort when removing
    //    the system will be too CPU consuming working on the limit
    // 3. Removed the oldest added script. For this we have this queue. 
    private LinkedList<String> digestQueue = new LinkedList<>();

    public ProbablisticSelectionManager() {
    }

    public ProbablisticSelectionManager(RMCore rmcore) {
        super(rmcore);
        this.probabilities = new HashMap<>();
    }

    /**
     * Find appropriate candidates nodes for script execution, taking into
     * account "free" and "exclusion" nodes lists.
     *
     * @param scripts set of scripts to execute
     * @param nodes free nodes list provided by resource manager
     * @return candidates node list for script execution
     */
    @Override
    public List<RMNode> arrangeNodesForScriptExecution(final List<RMNode> nodes, List<SelectionScript> scripts,
            Map<String, Serializable> bindings) {

        long startTime = System.currentTimeMillis();
        boolean scriptSpecified = scripts != null && scripts.size() > 0;

        // if no scripts are specified return filtered free nodes
        if (!scriptSpecified) {
            return nodes;
        }

        // finding intersection
        HashMap<RMNode, Probability> intersectionMap = new LinkedHashMap<>();
        for (RMNode rmnode : nodes) {
            boolean intersection = true;
            double intersectionProbability = 1;
            for (SelectionScript script : scripts) {
                SelectionScript scriptWithReplacedBindings = replaceBindings(script, bindings);
                String digest = "";
                try {
                    digest = new String(scriptWithReplacedBindings.digest());
                } catch (NoSuchAlgorithmException e) {
                    logger.error(e.getMessage(), e);
                }
                if (probabilities.containsKey(digest) && probabilities.get(digest).containsKey(rmnode.getNodeURL())) {
                    double probability = probabilities.get(digest).get(rmnode.getNodeURL()).value();
                    if (Math.abs(probability - 0) < 0.0001) {
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
        Set<RMNode> nodeSet = intersectionMap.keySet();
        List<RMNode> res = new ArrayList<>(nodeSet.size());
        res.addAll(nodeSet);
        Collections.sort(res, new NodeProbabilityComparator(intersectionMap));

        if (logger.isDebugEnabled()) {
            logger.debug("The following nodes are selected for scripts execution (time is " +
                         (System.currentTimeMillis() - startTime) + " ms) :");
            if (res.size() > 0) {
                res.forEach(rmnode -> {
                    logger.debug(rmnode.getNodeURL() + " : probability " + intersectionMap.get(rmnode));
                });
            } else {
                logger.debug("None");
            }
        }
        return res;
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
    public synchronized boolean isPassed(SelectionScript script, Map<String, Serializable> bindings, RMNode rmnode) {
        String digest = null;
        SelectionScript scriptWithReplacedBindings = replaceBindings(script, bindings);
        if (logger.isTraceEnabled()) {
            logger.trace(rmnode.getNodeURL() + " : script with replaced bindings : " +
                         scriptWithReplacedBindings.getId());
        }
        try {
            digest = new String(scriptWithReplacedBindings.digest());
            if (probabilities.containsKey(digest) && probabilities.get(digest).containsKey(rmnode.getNodeURL())) {
                Probability p = probabilities.get(digest).get(rmnode.getNodeURL());
                String scriptType = scriptWithReplacedBindings.isDynamic() ? "dynamic" : "static";
                if (logger.isDebugEnabled())
                    logger.debug(rmnode.getNodeURL() + " : " + digest.hashCode() + " known " + scriptType + " script");
                return p.value() == 1;
            }
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        }

        if (logger.isDebugEnabled())
            logger.debug(rmnode.getNodeURL() + " : " + digest.hashCode() + " unknown script");
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
    public synchronized boolean processScriptResult(SelectionScript script, Map<String, Serializable> bindings,
            ScriptResult<Boolean> scriptResult, RMNode rmnode) {

        boolean result = false;

        SelectionScript scriptWithReplacedBindings = replaceBindings(script, bindings);

        try {
            String digest = new String(scriptWithReplacedBindings.digest());
            Probability probability = new Probability(Probability.defaultValue());
            if (probabilities.containsKey(digest) && probabilities.get(digest).containsKey(rmnode.getNodeURL())) {
                probability = probabilities.get(digest).get(rmnode.getNodeURL());
                assert (probability.value() >= 0 && probability.value() <= 1);
            }

            if (scriptResult == null || scriptResult.errorOccured() || !scriptResult.getResult()) {
                // error during script execution or script returned false
                if (scriptWithReplacedBindings.isDynamic()) {
                    probability.decrease();
                } else {
                    probability = Probability.ZERO;
                }
            } else {
                // script passed
                result = true;
                if (scriptWithReplacedBindings.isDynamic()) {
                    probability.increase();
                } else {
                    probability = Probability.ONE;
                }
            }

            if (!probabilities.containsKey(digest)) {
                // checking if the number of selection script does not exceeded the maximum
                if (probabilities.size() >= PAResourceManagerProperties.RM_SELECT_SCRIPT_CACHE_SIZE.getValueAsInt()) {
                    String oldest = digestQueue.poll();
                    probabilities.remove(oldest);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Removing the script: " + scriptWithReplacedBindings.hashCode() +
                                     " from the data base because the limit is reached");
                    }
                }
                // adding a new script record
                probabilities.put(digest, new HashMap<String, Probability>());
                logger.debug("Scripts cache size " + probabilities.size());
                digestQueue.offer(digest);
            }

            if (logger.isDebugEnabled()) {
                logger.debug(rmnode.getNodeURL() + " : script " + scriptWithReplacedBindings.hashCode() +
                             ", probability " + probability);
            }

            probabilities.get(digest).put(rmnode.getNodeURL().intern(), probability);

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

    private SelectionScript replaceBindings(SelectionScript script, Map<String, Serializable> bindings) {
        String scriptContent = script.fetchScript();
        if (scriptContent != null && bindings != null) {
            for (Map.Entry<String, Serializable> entry : bindings.entrySet()) {
                String reservedKeyword = entry.getKey();
                Serializable binding = entry.getValue();
                if (binding instanceof Map) {
                    scriptContent = replaceBindingKeysByTheirValue(scriptContent, (Map<String, Serializable>) binding);
                } else {
                    if (binding != null) {
                        scriptContent = scriptContent.replace(reservedKeyword, binding.toString());
                    }
                }
            }
        }
        try {
            if (scriptContent != null) {
                return new SelectionScript(scriptContent,
                                           script.getEngineName(),
                                           script.getParameters(),
                                           script.isDynamic());
            } else {
                return script;
            }
        } catch (InvalidScriptException e) {
            logger.warn("Error when replacing bindings of script (revert to use original script):" +
                        System.lineSeparator() + script.toString(), e);
            return script;
        }
    }

    private String replaceBindingKeysByTheirValue(String scriptContent, Map<String, Serializable> binding) {
        for (Map.Entry<String, Serializable> variableMapping : binding.entrySet()) {
            Serializable bindingValue = variableMapping.getValue();
            if (bindingValue != null) {
                scriptContent = scriptContent.replace(variableMapping.getKey(), bindingValue.toString());
            }
        }
        return scriptContent;
    }

}
