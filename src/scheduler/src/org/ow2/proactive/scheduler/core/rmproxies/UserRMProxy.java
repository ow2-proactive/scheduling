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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.core.rmproxies;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAEventProgramming;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.core.ThrowExceptionRequest;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;
import org.ow2.proactive.utils.NodeSet;


/**
 * UserRMProxy is the ResourceManager proxy for user.
 * It is currently implemented has an active Object that forwards call to Resource Manager.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class UserRMProxy implements ResourceManager, RunActive {

    protected static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.RMPROXY);
    /**
     * Filters contains methods than can be called without throwing an exception while requesting
     * active objects standard calls. (non immediate service)
     */
    protected Set<String> filters;

    /** Reference to RM */
    protected ResourceManager rm = null;

    /** list of nodes and clean script being executed */
    private Map<Node, ScriptResult<?>> nodes;

    /**
     * ProActive constructor, DO NOT USE
     */
    public UserRMProxy() {
    }

    public UserRMProxy(Set<String> filters) {
        this.nodes = new HashMap<Node, ScriptResult<?>>();
        if (filters == null) {
            this.filters = new HashSet<String>();
        } else {
            this.filters = filters;
        }
    }

    /**
     * Connect the proxy to the Resource Manager
     *
     * @throws LoginException if authentication fails due to wrong login/pwd
     */
    @ImmediateService
    public void connect(RMAuthentication rmAuth, Credentials creds) throws LoginException {
        this.rm = rmAuth.login(creds);
    }

    /**
     * {@inheritDoc}
     */
    public void runActivity(Body body) {
        Service service = new Service(body);
        while (body.isActive()) {
            Request request = service.blockingRemoveOldest();
            if (request != null) {
                if (this.filters.contains(request.getMethodName())) {
                    service.serve(request);
                } else {
                    service.serve(new ThrowExceptionRequest(request, new IllegalStateException(
                        "Cannot perform this call, method '" + request.getMethodName() +
                            "' is neither filtered nor Immediate service.")));
                }
            }
        }

    }

    /**
     * Terminate this active Object
     */
    @ImmediateService
    public void terminateProxy() {
        try {
            //try disconnect
            PAFuture.waitFor(disconnect());
        } catch (Exception e) {
            //RM is not responding, do nothing
        }
        PAActiveObject.terminateActiveObject(false);
    }

    /**
     * Execute the given CleaningScript on the node before releasing it.
     * @see #releaseNode(Node)
     *
     * @param node the node to release
     * @param cleaningScript the cleaning script to apply to this node before releasing it
     */
    @ImmediateService
    public void releaseNode(Node node, Script<?> cleaningScript) {
        if (node != null) {
            if (cleaningScript == null) {
                releaseNode(node);
            } else {
                handleCleaningScript(node, cleaningScript);
            }
        }
    }

    /**
     * Execute the given CleaningScript on each nodes before releasing them.
     * @see #releaseNodes(NodeSet)
     *
     * @param nodes the node set to release
     * @param cleaningScript the cleaning script to apply to each node before releasing
     */
    @ImmediateService
    public void releaseNodes(NodeSet nodes, Script<?> cleaningScript) {
        if (nodes != null && nodes.size() > 0) {
            if (cleaningScript == null) {
                releaseNodes(nodes);
            } else {
                for (Node node : nodes) {
                    handleCleaningScript(node, cleaningScript);
                }
            }
        }
    }

    /**
     * Execute the given script on the given node.
     * Also register a callback on {@link #cleanCallBack(Future)} method when script has returned.
     *
     * @param node the node on which to start the script
     * @param cleaningScript the script to be executed
     */
    private void handleCleaningScript(Node node, Script<?> cleaningScript) {
        try {
            ScriptHandler handler = ScriptLoader.createHandler(node);
            ScriptResult<?> future = handler.handle(cleaningScript);
            try {
                PAEventProgramming.addActionOnFuture(future, "cleanCallBack");
            } catch (IllegalArgumentException e) {
                //TODO - linked to PROACTIVE-936 -> IllegalArgumentException is raised if method name is unknown
                //should be replaced by checked exception
                logger_dev
                        .error(
                                "ERROR : Callback method won't be executed, node won't be released. This is a critical state, check the callback method name",
                                e);
            }
            this.nodes.put(node, future);
            logger_dev.info("Cleaning Script handled on node" + node.getNodeInformation().getURL());
        } catch (Exception e) {
            //if active object cannot be created or script has failed
            logger_dev.error("", e);
            releaseNode(node);
        }
    }

    /**
     * Called when a script has returned (call is made as an active object call)
     *
     * Check the nodes to release and release the one that have to (clean script has returned)<br/>
     * Take care when renaming this method, method name is linked to {@link #handleCleaningScript(Node, Script)}
     */
    @ImmediateService
    public synchronized void cleanCallBack(Future<ScriptResult<?>> future) {
        Iterator<Entry<Node, ScriptResult<?>>> iterator = nodes.entrySet().iterator();
        NodeSet ns = new NodeSet();
        while (iterator.hasNext()) {
            Entry<Node, ScriptResult<?>> entry = iterator.next();
            if (!PAFuture.isAwaited(entry.getValue())) { // !awaited = arrived
                if (logger_dev.isInfoEnabled()) {
                    logger_dev.info("Cleaning script successfull, node freed : " +
                        entry.getKey().getNodeInformation().getURL());
                }
                ns.add(entry.getKey());
                iterator.remove();
            }
        }
        if (ns.size() > 0) {
            releaseNodes(ns);
        }
    }

    /************************ INHERITED FROM RESOURCE-MANAGER **********************/

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public NodeSet getAtMostNodes(int number, SelectionScript selectionScript) {
        return rm.getAtMostNodes(number, selectionScript);
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public NodeSet getAtMostNodes(int number, SelectionScript selectionScript, NodeSet exclusion) {
        return rm.getAtMostNodes(number, selectionScript, exclusion);
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public NodeSet getAtMostNodes(int number, List<SelectionScript> selectionScriptsList, NodeSet exclusion) {
        return rm.getAtMostNodes(number, selectionScriptsList, exclusion);
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public NodeSet getAtMostNodes(int number, TopologyDescriptor descriptor,
            List<SelectionScript> selectionScriptsList, NodeSet exclusion) {
        return rm.getAtMostNodes(number, descriptor, selectionScriptsList, exclusion);
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public BooleanWrapper disconnect() {
        return rm.disconnect();
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public BooleanWrapper releaseNode(Node node) {
        return rm.releaseNode(node);
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public BooleanWrapper releaseNodes(NodeSet nodes) {
        return rm.releaseNodes(nodes);
    }

    /**************************** UNUSED METHODS ******************************/

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper addNode(String nodeUrl) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper addNode(String nodeUrl, String sourceName) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper createNodeSource(String nodeSourceName, String infrastructureType,
            Object[] infrastructureParameters, String policyType, Object[] policyParameters) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public RMMonitoring getMonitoring() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IntWrapper getNodeSourcePingFrequency(String sourceName) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public RMState getState() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<PluginDescriptor> getSupportedNodeSourceInfrastructures() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<PluginDescriptor> getSupportedNodeSourcePolicies() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Topology getTopology() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper isActive() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper nodeIsAvailable(String nodeUrl) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper removeNode(String nodeUrl, boolean preempt) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper removeNodeSource(String sourceName, boolean preempt) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper setNodeSourcePingFrequency(int frequency, String sourceName) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper shutdown(boolean preempt) {
        return null;
    }

}
