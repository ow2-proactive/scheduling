package org.ow2.proactive.scheduler.core.rmproxies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAEventProgramming;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;


@ActiveObject
public class RMProxyActiveObject {

    protected static final Logger logger = ProActiveLogger.getLogger(RMProxyActiveObject.class);

    protected ResourceManager rm;

    /** list of nodes and clean script being executed */
    private Map<Node, ScriptResult<?>> nodes = new HashMap<Node, ScriptResult<?>>();

    public RMProxyActiveObject() {
    }

    static RMProxyActiveObject createAOProxy(RMAuthentication rmAuth, Credentials creds)
            throws RMProxyCreationException {
        try {
            RMProxyActiveObject proxy = PAActiveObject.newActive(RMProxyActiveObject.class, new Object[] {});
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
        List<Node> allNodes = new ArrayList<Node>(nodeSet);
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
                logger
                        .error(
                                "ERROR : Callback method won't be executed, node won't be released. This is a critical state, check the callback method name",
                                e);
            }
            this.nodes.put(node, future);
            logger.info("Cleaning Script handled on node" + node.getNodeInformation().getURL());
        } catch (Exception e) {
            //if active object cannot be created or script has failed
            logger.error("", e);
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
                if (logger.isInfoEnabled()) {
                    logger.info("Cleaning script successfull, node freed : " +
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

}
