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

import java.io.File;
import java.io.Serializable;
import java.security.Permission;
import java.util.*;
import java.util.concurrent.*;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.ow2.proactive.authentication.principals.TokenPrincipal;
import org.ow2.proactive.permissions.NodeUserAllPermission;
import org.ow2.proactive.permissions.PrincipalPermission;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.NotConnectedException;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.selection.policies.ShufflePolicy;
import org.ow2.proactive.resourcemanager.selection.topology.TopologyHandler;
import org.ow2.proactive.resourcemanager.selection.topology.TopologyNodesFilter;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptException;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;
import org.ow2.proactive.utils.appenders.MultipleFileAppender;


/**
 * An interface of selection manager which is responsible for nodes selection
 * from a pool of free nodes for further scripts execution.
 *
 */
public abstract class SelectionManager {

    private final static Logger logger = Logger.getLogger(SelectionManager.class);

    private RMCore rmcore;

    private static final long DEFAULT_AUTHORIZED_SCRIPT_LOAD_PERIOD = (long) 60 * 1000;

    private static long lastAuthorizedFolderLoadingTime = 0;

    private ExecutorService scriptExecutorThreadPool;

    private Set<String> inProgress;

    protected HashSet<String> authorizedSelectionScripts = null;

    // the policy for arranging nodes
    private SelectionPolicy selectionPolicy;

    private TopologyNodesFilter topologyNodesFilter;

    public SelectionManager() {
    }

    public SelectionManager(RMCore rmcore) {
        this.topologyNodesFilter = new TopologyNodesFilter();
        this.rmcore = rmcore;
        this.scriptExecutorThreadPool = Executors.newFixedThreadPool(PAResourceManagerProperties.RM_SELECTION_MAX_THREAD_NUMBER.getValueAsInt(),
                                                                     new NamedThreadFactory("Selection manager threadpool"));
        this.inProgress = Collections.synchronizedSet(new HashSet<String>());

        String policyClassName = PAResourceManagerProperties.RM_SELECTION_POLICY.getValueAsString();
        try {
            Class<?> policyClass = Class.forName(policyClassName);
            selectionPolicy = (SelectionPolicy) policyClass.newInstance();
        } catch (Exception e) {
            logger.error("Cannot use the specified policy class: " + policyClassName, e);
            logger.warn("Using the default class: " + ShufflePolicy.class.getName());
            selectionPolicy = new ShufflePolicy();
        }

        updateAuthorizedScriptsSignatures();
    }

    /**
     * Loads authorized selection scripts.
     */
    public void updateAuthorizedScriptsSignatures() {
        String dirName = PAResourceManagerProperties.RM_EXECUTE_SCRIPT_AUTHORIZED_DIR.getValueAsStringOrNull();
        if (dirName != null && dirName.length() > 0) {
            dirName = PAResourceManagerProperties.getAbsolutePath(dirName);
            File folder = new File(dirName);

            if (folder.exists() && folder.isDirectory()) {
                logger.debug("The resource manager will accept only selection scripts from " + dirName);
                long currentTime = System.currentTimeMillis();
                long configuredAuthorizedScriptLoadPeriod = getConfiguredAuthorizedScriptLoadPeriod();
                if (currentTime - lastAuthorizedFolderLoadingTime > configuredAuthorizedScriptLoadPeriod) {
                    lastAuthorizedFolderLoadingTime = currentTime;
                    loadAuthorizedScriptsSignatures(folder);
                }
            } else {
                logger.error("Invalid dir name for authorized scripts " + dirName);
                throw new SecurityException("Invalid dir name for authorized scripts " + dirName);
            }
        }
    }

    private void loadAuthorizedScriptsSignatures(File folder) {
        authorizedSelectionScripts = new HashSet<>();
        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                try {
                    String script = SelectionScript.readFile(file);
                    logger.debug("Adding authorized selection script " + file.getAbsolutePath());
                    authorizedSelectionScripts.add(Script.digest(script.trim()));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    throw new SecurityException("Error while reading authorized script file", e);
                }
            }
        }
    }

    private long getConfiguredAuthorizedScriptLoadPeriod() {
        long configuredAuthorizedScriptLoadPeriod = DEFAULT_AUTHORIZED_SCRIPT_LOAD_PERIOD;
        if (PAResourceManagerProperties.RM_EXECUTE_SCRIPT_AUTHORIZED_DIR_REFRESHPERIOD.isSet()) {
            configuredAuthorizedScriptLoadPeriod = PAResourceManagerProperties.RM_EXECUTE_SCRIPT_AUTHORIZED_DIR_REFRESHPERIOD.getValueAsLong();
        }
        return configuredAuthorizedScriptLoadPeriod;
    }

    /**
     * Arranges nodes for script execution based on some criteria for example
     * previous execution statistics.
     * 
     * @param nodes
     *            - nodes list for script execution
     * @param scripts
     *            - set of selection scripts
     * @return collection of arranged nodes
     */
    public abstract List<RMNode> arrangeNodesForScriptExecution(final List<RMNode> nodes, List<SelectionScript> scripts,
            Map<String, Serializable> bindings);

    /**
     * Predicts script execution result. Allows to avoid duplicate script
     * execution on the same node.
     * 
     * @param script
     *            - script to execute
     * @param rmnode
     *            - target node
     * @return true if script will pass on the node
     */
    public abstract boolean isPassed(SelectionScript script, Map<String, Serializable> bindings, RMNode rmnode);

    /**
     * Processes script result and updates knowledge base of selection manager
     * at the same time.
     *
     * @param script
     *            - executed script
     * @param scriptResult
     *            - obtained script result
     * @param rmnode
     *            - node on which script has been executed
     * @return whether node is selected
     */
    public abstract boolean processScriptResult(SelectionScript script, Map<String, Serializable> bindings,
            ScriptResult<Boolean> scriptResult, RMNode rmnode);

    public NodeSet selectNodes(Criteria criteria, Client client) {

        maybeSetLoggingContext(criteria);
        try {
            return doSelectNodes(criteria, client);
        } finally {
            unsetLoggingContext();
        }

    }

    static void maybeSetLoggingContext(Criteria criteria) {
        if (criteria.getComputationDescriptors() != null) {
            // logging selection script execution into tasks logs
            MDC.put(MultipleFileAppender.FILE_NAMES, criteria.getComputationDescriptors());
        }
    }

    static void unsetLoggingContext() {
        MDC.remove(MultipleFileAppender.FILE_NAMES);
    }

    private NodeSet doSelectNodes(Criteria criteria, Client client) {
        boolean hasScripts = criteria.getScripts() != null && criteria.getScripts().size() > 0;
        boolean loggerIsDebugEnabled = logger.isDebugEnabled();
        if (loggerIsDebugEnabled) {
            logger.debug(client + " requested " + criteria.getSize() + " nodes with " + criteria.getTopology());
            if (hasScripts) {
                logger.debug("Selection scripts:");
                for (SelectionScript s : criteria.getScripts()) {
                    logger.debug(s);
                }
            }

            if (criteria.getBlackList() != null && criteria.getBlackList().size() > 0) {
                logger.debug("Black list nodes:");
                for (Node n : criteria.getBlackList()) {
                    logger.debug(n);
                }
            }
        }

        // can throw Exception if topology is disabled
        TopologyHandler handler = RMCore.topologyManager.getHandler(criteria.getTopology());

        int totalNumberOfAliveNodesRightNow = rmcore.getTotalAliveNodesNumber();

        List<RMNode> freeNodes = rmcore.getFreeNodes();
        // filtering out the "free node list"
        // removing exclusion and checking permissions
        List<RMNode> filteredNodes = filterOut(freeNodes, criteria, client);

        if (filteredNodes.size() == 0) {
            if (loggerIsDebugEnabled) {
                logger.debug(client + " will get 0 nodes");
            }
            return new NodeSet();
        }

        // arranging nodes according to the selection policy
        // if could be shuffling or node source priorities
        List<RMNode> afterPolicyNodes = selectionPolicy.arrangeNodes(criteria.getSize(), filteredNodes, client);

        List<Node> matchedNodes;
        if (hasScripts) {
            // checking if all scripts are authorized
            checkAuthorizedScripts(criteria.getScripts());

            // arranging nodes for script execution
            List<RMNode> arrangedNodes = arrangeNodesForScriptExecution(afterPolicyNodes,
                                                                        criteria.getScripts(),
                                                                        criteria.getBindings());
            List<RMNode> arrangedFilteredNodes = arrangedNodes;
            if (criteria.getTopology().isTopologyBased()) {
                arrangedFilteredNodes = topologyNodesFilter.filterNodes(criteria, arrangedNodes);
            }

            if (arrangedFilteredNodes.isEmpty()) {
                matchedNodes = new LinkedList<>();
            } else if (electedToRunOnAllNodes(criteria)) {
                // run scripts on all available nodes
                matchedNodes = runScripts(arrangedFilteredNodes, criteria);
            } else {

                // run scripts not on all nodes, but always on missing number of
                // nodes
                // until required node set is found
                matchedNodes = new LinkedList<>();
                while (matchedNodes.size() < criteria.getSize()) {
                    int numberOfNodesForScriptExecution = criteria.getSize() - matchedNodes.size();

                    if (numberOfNodesForScriptExecution < PAResourceManagerProperties.RM_SELECTION_MAX_THREAD_NUMBER.getValueAsInt()) {
                        // we can run
                        // "PAResourceManagerProperties.RM_SELECTION_MAX_THREAD_NUMBER.getValueAsInt()"
                        // scripts in parallel
                        // in case when we need less nodes it still useful to
                        // the full capacity of the thread pool to find nodes
                        // quicker

                        // it is not important if we find more nodes than needed
                        // subset will be selected later (topology handlers)
                        numberOfNodesForScriptExecution = PAResourceManagerProperties.RM_SELECTION_MAX_THREAD_NUMBER.getValueAsInt();
                    }

                    List<RMNode> subset = arrangedFilteredNodes.subList(0,
                                                                        Math.min(numberOfNodesForScriptExecution,
                                                                                 arrangedFilteredNodes.size()));
                    matchedNodes.addAll(runScripts(subset, criteria));
                    // removing subset of arrangedNodes
                    subset.clear();

                    if (arrangedFilteredNodes.size() == 0) {
                        break;
                    }
                }
                if (loggerIsDebugEnabled) {
                    logger.debug(matchedNodes.size() + " nodes found after scripts execution for " + client);
                }
            }

        } else {
            matchedNodes = new LinkedList<>();
            for (RMNode node : afterPolicyNodes) {
                matchedNodes.add(node.getNode());
            }
        }

        // now we have a list of nodes which match to selection scripts
        // selecting subset according to topology requirements
        // TopologyHandler handler =
        // RMCore.topologyManager.getHandler(topologyDescriptor);

        if (criteria.getTopology().isTopologyBased() && loggerIsDebugEnabled) {
            logger.debug("Filtering nodes with topology " + criteria.getTopology());
        }
        NodeSet selectedNodes = handler.select(criteria.getSize(), matchedNodes);

        if (selectedNodes.size() < criteria.getSize() && !criteria.isBestEffort()) {
            selectedNodes.clear();
            if (selectedNodes.getExtraNodes() != null) {
                selectedNodes.getExtraNodes().clear();
            }
        }

        // the nodes are selected, now mark them as busy.
        int counter = 0;
        for (Node node : selectedNodes) {

            try {

                if (criteria.getListUsageInfo().size() == 1) {
                    // it is mutli node execution, which means we provide the same usage info for every node
                    // Synchronous call
                    rmcore.setBusyNode(node.getNodeInformation().getURL(), client, criteria.getListUsageInfo().get(0));
                } else {
                    // in this case, we have set of nodes, where will be set of compatible tasks
                    // Synchronous call
                    rmcore.setBusyNode(node.getNodeInformation().getURL(),
                                       client,
                                       criteria.getListUsageInfo().get(counter));
                }
            } catch (NotConnectedException e) {
                // client has disconnected during getNodes request
                logger.warn(e.getMessage(), e);
                return null;
            }
            ++counter;
        }
        // marking extra selected nodes as busy
        if (selectedNodes.size() > 0 && selectedNodes.getExtraNodes() != null) {
            for (Node node : new LinkedList<>(selectedNodes.getExtraNodes())) {
                try {
                    // synchronous call
                    // here, we believe that it will be called only for multi node execution
                    rmcore.setBusyNode(node.getNodeInformation().getURL(), client, criteria.getListUsageInfo().get(0));
                } catch (NotConnectedException e) {
                    // client has disconnected during getNodes request
                    logger.warn(e.getMessage(), e);
                    return null;
                }
            }
        }

        if (logger.isInfoEnabled()) {
            String extraNodes = selectedNodes.getExtraNodes() != null && selectedNodes.getExtraNodes().size() > 0
                                                                                                                  ? " and " +
                                                                                                                    selectedNodes.getExtraNodes()
                                                                                                                                 .size() +
                                                                                                                    " extra nodes"
                                                                                                                  : "";
            logger.info(client + " requested " + criteria.getSize() + " nodes with " + criteria.getTopology() +
                        " and will get " + selectedNodes.size() + " nodes " + extraNodes +
                        " [totalNumberOfAliveNodesRightNow:" + totalNumberOfAliveNodesRightNow + ";freeNodes:" +
                        freeNodes.size() + ";filteredNodes:" + filteredNodes.size() + ";reordered after policy:" +
                        afterPolicyNodes.size() + ";selection script present:" + hasScripts +
                        ";nodes filtered by selection script:" + matchedNodes.size() + ";selectedNodes:" +
                        selectedNodes.size() + "]");
        }

        if (loggerIsDebugEnabled) {
            for (Node n : selectedNodes) {
                logger.debug(n.getNodeInformation().getURL());
            }
        }

        return selectedNodes;
    }

    private static boolean electedToRunOnAllNodes(Criteria criteria) {
        return criteria.getTopology().isTopologyBased() &&
               !criteria.getTopology().toString().equals(TopologyDescriptor.SINGLE_HOST.toString()) &&
               !criteria.getTopology().toString().equals(TopologyDescriptor.SINGLE_HOST_EXCLUSIVE.toString());
    }

    /**
     * Checks is all scripts are authorized. If not throws an exception.
     */
    private void checkAuthorizedScripts(List<SelectionScript> scripts) {
        updateAuthorizedScriptsSignatures();
        if (authorizedSelectionScripts == null || scripts == null)
            return;

        for (SelectionScript script : scripts) {
            checkContentAuthorization(script.fetchScript());
        }
    }

    private void checkContentAuthorization(String content) {
        if (content != null && !authorizedSelectionScripts.contains(Script.digest(content.trim()))) {
            // unauthorized selection script
            throw new SecurityException("Cannot execute unauthorized script: " + System.getProperty("line.separator") +
                                        content);
        }
    }

    /**
     * Runs scripts on given set of nodes and returns matched nodes. It blocks
     * until all results are obtained.
     *
     * @param candidates
     *            nodes to execute scripts on
     * @param criteria
     *            contains a set of scripts to execute on each node
     * @return nodes matched to all scripts
     */
    private List<Node> runScripts(List<RMNode> candidates, Criteria criteria) {
        List<Node> matched = new LinkedList<>();

        if (candidates.size() == 0) {
            return matched;
        }

        // creating script executors object to be run in dedicated thread pool
        List<Callable<Node>> scriptExecutors = new LinkedList<>();
        synchronized (inProgress) {
            if (inProgress.size() > 0) {
                logger.warn(inProgress.size() + " nodes are in process of script execution");
                for (String nodeName : inProgress) {
                    logger.warn(nodeName);

                }
                logger.warn("Something is wrong on these nodes");
            }
            for (RMNode node : candidates) {
                if (!inProgress.contains(node.getNodeURL())) {
                    inProgress.add(node.getNodeURL());
                    scriptExecutors.add(new ScriptExecutor(node, criteria, this));
                }
            }
        }

        try {
            // launching
            Collection<Future<Node>> matchedNodes = scriptExecutorThreadPool.invokeAll(scriptExecutors);

            // waiting for the results
            for (Future<Node> futureNode : matchedNodes) {
                Node node;
                try {
                    node = futureNode.get();
                    if (node != null) {
                        matched.add(node);
                    }
                } catch (InterruptedException e) {
                    logger.warn("Interrupting the selection manager");
                    return matched;
                } catch (ExecutionException e) {
                    logger.warn("Ignoring exception in selection script: " + e.getMessage());
                }

            }
        } catch (InterruptedException e1) {
            logger.warn("Interrupting the selection manager");
        }

        return matched;
    }

    /**
     * Removes exclusion nodes and nodes not accessible for the client
     */
    private List<RMNode> filterOut(List<RMNode> freeNodes, Criteria criteria, Client client) {

        // Get inclusion/exclusion list for the final check
        Set<String> inclusion = criteria.getAcceptableNodesUrls();
        NodeSet exclusion = criteria.getBlackList();

        // Is a token specified at the task level ?
        boolean nodeWithTokenRequested = criteria.getNodeAccessToken() != null &&
                                         !criteria.getNodeAccessToken().isEmpty();

        // If yes, add it to the client Principals list as TokenPrincipal object
        TokenPrincipal tokenPrincipal = null;
        if (nodeWithTokenRequested) {
            logger.debug("Node access token specified " + criteria.getNodeAccessToken());

            tokenPrincipal = new TokenPrincipal(criteria.getNodeAccessToken());
            client.getSubject().getPrincipals().add(tokenPrincipal);
        }

        // Can client has access to the node ?
        List<RMNode> filteredList = new ArrayList<>();
        HashSet<Permission> clientPermissions = new HashSet<>();
        for (RMNode node : freeNodes) {
            try {
                if (!clientPermissions.contains(node.getUserPermission())) {
                    client.checkPermission(node.getUserPermission(),
                                           client + " is not authorized to get the node " + node.getNodeURL() +
                                                                     " from " + node.getNodeSource().getName(),
                                           new NodeUserAllPermission());
                    // YES
                    clientPermissions.add(node.getUserPermission());
                }
            } catch (SecurityException e) {
                // NO
                logger.debug(e.getMessage());
                continue;
            }

            // If a token is specified at the client level (ie token in a task GI), and if the current node
            // is not protected by any token (ie no token specified in the NodeSourcePolicy),
            // we do not consider this node. We only consider nodes protected by the token required by the client.
            if (nodeWithTokenRequested && !node.isProtectedByToken()) {
                continue;
            }

            // if client has AllPermissions he still can get a node with any
            // token
            // we will avoid it here
            if (nodeWithTokenRequested) {
                PrincipalPermission perm = (PrincipalPermission) node.getUserPermission();
                // checking explicitly that node has this token identity
                if (!perm.hasPrincipal(tokenPrincipal)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(client + " does not have required token to get the node " + node.getNodeURL() +
                                     " from " + node.getNodeSource().getName());
                    }
                    continue;
                }
            }

            if (!contains(exclusion, node) && ((inclusion != null) ? inclusion.contains(node.getNodeURL()) : true)) {
                filteredList.add(node);
            }
        }
        return filteredList;
    }

    @ImmediateService
    public <T> List<ScriptResult<T>> executeScript(final Script<T> script, final Collection<RMNode> nodes,
            final Map<String, Serializable> bindings) {
        final long allScriptExecutionsTimeout = PAResourceManagerProperties.RM_EXECUTE_SCRIPT_TIMEOUT.getValueAsLong();
        final List<Callable<ScriptResult<T>>> scriptExecutors = new ArrayList<>(nodes.size());
        final List<String> scriptHosts = new ArrayList<>(nodes.size());

        // Execute the script on each selected node
        for (final RMNode node : nodes) {
            scriptExecutors.add(new Callable<ScriptResult<T>>() {
                @Override
                public ScriptResult<T> call() throws Exception {
                    // Execute with a timeout the script by the remote handler
                    // and always async-unlock the node, exceptions will be
                    // treated as ExecutionException
                    String nodeURL = node.getNodeURL();
                    try {
                        logger.info("Executing node script on " + nodeURL);
                        ScriptResult<T> res = node.executeScript(script, bindings);
                        PAFuture.waitFor(res, allScriptExecutionsTimeout);
                        logger.info("Node script execution on " + nodeURL + " terminated");
                        return res;
                    } catch (Exception e) {
                        logger.error("Error while executing node script and waiting for the result on " + nodeURL, e);
                        throw e;
                    } finally {
                        SelectionManager.this.rmcore.unlockNodes(Collections.singleton(nodeURL)).getBooleanValue();
                    }
                }

                @Override
                public String toString() {
                    return "executing node script on " + node.getNodeURL();
                }
            });
            scriptHosts.add(node.getHostName());
        }

        // Invoke all Callables and get the list of futures
        List<Future<ScriptResult<T>>> futures = new LinkedList<>();
        try {
            futures = this.scriptExecutorThreadPool.invokeAll(scriptExecutors,
                                                              allScriptExecutionsTimeout,
                                                              TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while waiting, unable to execute all node scripts", e);
            Thread.currentThread().interrupt();
        }

        final List<ScriptResult<T>> results = new LinkedList<>();
        List<RMNode> nodesList = new ArrayList<>(nodes);

        int index = 0;
        for (final Future<ScriptResult<T>> future : futures) {
            final String description = scriptExecutors.get(index).toString();
            String nodeURL = nodesList.get(index).getNodeURL();

            ScriptResult<T> result;
            try {
                logger.debug("Awaiting node script result on " + nodeURL);
                result = future.get();
            } catch (CancellationException e) {
                logger.error("The invoked node script was cancelled due to timeout on " + nodeURL, e);
                result = new ScriptResult<>(new ScriptException("Cancelled due to timeout expiration when " +
                                                                description, e));
            } catch (InterruptedException e) {
                logger.warn("The invoked node script was interrupted on " + nodeURL, e);
                result = new ScriptResult<>(new ScriptException("Cancelled due to interruption when " + description));
            } catch (ExecutionException e) {
                // Unwrap the root exception
                Throwable rex = e.getCause();
                logger.error("There was an issue during node script invocation on " + nodeURL, e);
                result = new ScriptResult<>(new ScriptException("Exception occurred in script call when " + description,
                                                                rex));
            }

            result.setHostname(scriptHosts.get(index));
            results.add(result);
            index++;

            logger.info("Successfully retrieved script result on " + nodeURL);
        }

        return results;
    }

    /**
     * Indicates that script execution is finished for the node with specified
     * url.
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
        // shutdown the thread pool without waiting for script execution
        // completions
        scriptExecutorThreadPool.shutdownNow();
        PAActiveObject.terminateActiveObject(false);
    }

    /**
     * Return true if node contains the node set.
     *
     * @param nodeset
     *            - a list of nodes to inspect
     * @param node
     *            - a node to find
     * @return true if node contains the node set.
     */
    private boolean contains(NodeSet nodeset, RMNode node) {
        if (nodeset == null)
            return false;

        for (Node n : nodeset) {
            try {
                if (n.getNodeInformation().getURL().equals(node.getNodeURL())) {
                    return true;
                }
            } catch (Exception e) {
                continue;
            }
        }
        return false;
    }

}
