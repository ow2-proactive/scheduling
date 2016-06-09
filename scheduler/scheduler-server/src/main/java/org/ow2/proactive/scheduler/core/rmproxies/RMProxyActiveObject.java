package org.ow2.proactive.scheduler.core.rmproxies;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAEventProgramming;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;
import org.apache.log4j.Logger;


@ActiveObject
public class RMProxyActiveObject {

    protected static final Logger logger = Logger.getLogger(RMProxyActiveObject.class);

    protected ResourceManager rm;

    /**
     * list of nodes and clean script being executed
     */
    private Map<Node, ScriptResult<?>> nodes = new HashMap<>();

    public RMProxyActiveObject() {
    }

    static RMProxyActiveObject createAOProxy(RMAuthentication rmAuth, Credentials creds)
            throws RMProxyCreationException {
        try {
            RMProxyActiveObject proxy = PAActiveObject.newActive(RMProxyActiveObject.class, new Object[]{});
            proxy.connect(rmAuth, creds);
            return proxy;
        } catch (Exception e) {
            throw new RMProxyCreationException(e);
        }
    }

    @ImmediateService
    public void connect(RMAuthentication rmAuth, Credentials creds) throws LoginException {
        this.rm = rmAuth.login(creds);
    }

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

    @ImmediateService
    public BooleanWrapper disconnect() {
        return rm.disconnect();
    }

    @ImmediateService
    public BooleanWrapper isActive() {
        return rm.isActive();
    }

    @ImmediateService
    public RMState getState() {
        return rm.getState();
    }

    @ImmediateService
    public NodeSet getNodes(Criteria criteria) {
        return rm.getNodes(criteria);
    }

    @ImmediateService
    public BooleanWrapper releaseNode(Node node) {
        return rm.releaseNode(node);
    }

    @ImmediateService
    public BooleanWrapper releaseNodes(NodeSet nodes) {
        return rm.releaseNodes(nodes);
    }

    @ImmediateService
    public boolean isNodeSetForThisRM(NodeSet nodeSet) {
        List<Node> allNodes = new ArrayList<>(nodeSet);
        if (nodeSet.getExtraNodes() != null) {
            allNodes.addAll(nodeSet.getExtraNodes());
        }
        for (Node node : allNodes) {
            BooleanWrapper nodeIsAvailable = rm.nodeIsAvailable(node.getNodeInformation().getURL());
            if (nodeIsAvailable.getBooleanValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Execute the given CleaningScript on each nodes before releasing them.
     *
     * @param nodes          the node set to release
     * @param cleaningScript the cleaning script to apply to each node before releasing
     * @param variables
     * @see #releaseNodes(NodeSet)
     */
    @ImmediateService
    public void releaseNodes(NodeSet nodes, Script<?> cleaningScript, Map<String, Serializable> variables) {
        if (nodes != null && nodes.size() > 0) {
            if (cleaningScript == null) {
                releaseNodes(nodes);
            } else {
                for (Node node : nodes) {
                    handleCleaningScript(node, cleaningScript, variables);
                }
            }
        }
    }

    /**
     * Execute the given script on the given node.
     * Also register a callback on {@link #cleanCallBack(Future)} method when script has returned.
     *
     * @param node           the node on which to start the script
     * @param cleaningScript the script to be executed
     * @param variables
     */
    private void handleCleaningScript(Node node, Script<?> cleaningScript, Map<String, Serializable> variables) {
        try {
            ScriptHandler handler = ScriptLoader.createHandler(node);
            handler.addBinding(SchedulerConstants.VARIABLES_BINDING_NAME, (Serializable) variables);
            ScriptResult<?> future = handler.handle(cleaningScript);
            try {
                PAEventProgramming.addActionOnFuture(future, "cleanCallBack");
            } catch (IllegalArgumentException e) {
                //TODO - linked to PROACTIVE-936 -> IllegalArgumentException is raised if method name is unknown
                //should be replaced by checked exception
                logger
                        .error(
                                "ERROR : Callback method won't be executed, node won't be released. This is a critical state, check the callback method name",
                                e);
            }
            this.nodes.put(node, future);
            logger.info("Cleaning Script started on node" + node.getNodeInformation().getURL());

        } catch (Exception e) {
            //if active object cannot be created or script has failed
            logger.error("", e);
            releaseNode(node);
        }
    }

    /**
     * Called when a script has returned (call is made as an active object call)
     * <p>
     * Check the nodes to release and release the one that have to (clean script has returned)
     * Take care when renaming this method, method name is linked to {@link #handleCleaningScript(Node, Script, Map)}
     */
    @ImmediateService
    public synchronized void cleanCallBack(Future<ScriptResult<?>> future) {
        Iterator<Entry<Node, ScriptResult<?>>> iterator = nodes.entrySet().iterator();
        NodeSet ns = new NodeSet();
        while (iterator.hasNext()) {
            Entry<Node, ScriptResult<?>> entry = iterator.next();
            if (!PAFuture.isAwaited(entry.getValue())) { // !awaited = arrived
                String nodeUrl = entry.getKey().getNodeInformation().getURL();
                ScriptResult<?> sResult = null;
                try {
                    sResult = future.get();
                } catch (Exception e) {
                    logger.warn("Exception occurred while executing cleaning script on node " + nodeUrl + ":", e);
                }
                printCleaningScriptInformations(nodeUrl, sResult);
                ns.add(entry.getKey());
                iterator.remove();
            }
        }
        if (ns.size() > 0) {
            releaseNodes(ns);
        }
    }

    private void printCleaningScriptInformations(String nodeUrl, ScriptResult<?> sResult){
        if (logger.isInfoEnabled()) {
            logger.info("Cleaning script successfull, node freed : " + nodeUrl);
            logger.info("Cleaning script output on node " + nodeUrl + ":");
            logger.info(sResult.getOutput());
        }
    }

}