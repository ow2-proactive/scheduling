/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package org.ow2.proactive.scheduler.resourcemanager;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.RMUser;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.utils.NodeSet;


/**
 * The Resource Manager Proxy provides an interface with the
 * Resource Manager for Scheduler only. It connects to RMUser interface,
 * and adds the Clean Scripting management.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class ResourceManagerProxy implements InitActive, RunActive {

    private static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.RMPROXY);
    private static Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.RMPROXY);
    private static final long VERIF_TIMEOUT = 10000;
    private RMUser user;
    private HashMap<Node, ScriptResult<?>> nodes;
    private boolean running = true;

    private RMAuthentication auth = null;

    /** ProActive no Args constructor **/
    public ResourceManagerProxy() {
    }

    /** IMProxy constructor.
     *
     * @param user the User interface
     * @throws LoginException
     */
    public ResourceManagerProxy(RMAuthentication auth) {
        this.auth = auth;
    }

    /**
     * Get a IMProxy by its URI (example : "rmi://localhost:1099/" ).
     * 
     *
     * @param uriIM The URI of the Resource Manager
     * @return an instance of a Resource Manager proxy connected to the Resource Manager at the given URI
     * @throws NodeException if the resource manager proxy cannot be created on this node
     * @throws LoginException if the login or password are not correct
     * @throws ActiveObjectCreationException if the resource manager proxy cannot be created
     */
    public static ResourceManagerProxy getProxy(URI uriIM) throws RMException, NodeException,
            ActiveObjectCreationException {
        String url = uriIM.toString();

        RMAuthentication auth = RMConnection.join(url);
        return (ResourceManagerProxy) PAActiveObject.newActive(ResourceManagerProxy.class.getCanonicalName(),
                new Object[] { auth });
    }

    public boolean isAlive() {
        return user.isAlive();
    }

    // FREE NODES *********************************************
    /**
     * Simply free a Node
     * @see RMUser#freeNode(Node)
     *
     * @param node the node to free
     */
    public void freeNode(Node node) {
        logger_dev.info("Node freed : " + node.getNodeInformation().getURL());

        user.freeNode(node);
    }

    /**
     * Execute the CleaningScript on the node before freeing it.
     * @see RMUser#freeNode(Node)
     *
     * @param node the node to free
     * @param CleaningScript the cleaning script to apply to this node when freeing
     */
    public void freeNode(Node node, Script<?> cleaningScript) {
        if (node != null) {
            if (cleaningScript == null) {
                freeNode(node);
            } else {
                try {
                    ScriptHandler handler = ScriptLoader.createHandler(node);
                    nodes.put(node, handler.handle(cleaningScript));

                    logger_dev.info("Cleaning Script handled on node" + node.getNodeInformation().getURL());
                } catch (ActiveObjectCreationException e) {
                    // TODO what happen if node is down ?
                    // CHOICE 1 : return node without doing anything
                    logger_dev.error("", e);
                    freeNode(node);
                } catch (NodeException e) {
                    // TODO what happen if node is down ?
                    // CHOICE 1 : return node without doing anything
                    logger_dev.error("", e);
                    freeNode(node);
                }
            }
        }
    }

    /**
     * Simply free a NodeSet
     * @see RMUser#freeNodes(NodeSet)
     *
     * @param nodes the node set to free
     */
    public void freeNodes(NodeSet nodes) {
        logger_dev.info("Nodes freed : " + nodes.size() + " nodes");

        user.freeNodes(nodes);
    }

    /**
     * Execute the cleaningScript on the nodes before freeing them.
     * @see RMUser#freeNodes(NodeSet)
     *
     * @param nodes the nodeset to free
     * @param cleaningScript the cleaning script to apply to the freed nodes.
     */
    public void freeNodes(NodeSet nodes, Script<?> cleaningScript) {
        if (cleaningScript == null) {
            freeNodes(nodes);
        } else {
            for (Node node : nodes) {
                try {
                    ScriptHandler handler = ScriptLoader.createHandler(node);
                    ScriptResult<?> res = handler.handle(cleaningScript);
                    this.nodes.put(node, res);

                    logger_dev.info("Cleaning Script handled on node" + node.getNodeInformation().getURL());
                } catch (ActiveObjectCreationException e) {
                    // TODO Que faire si noeud mort ?
                    // CHOIX 1 : on retourne le noeud sans rien faire
                    logger_dev.error("Error during cleaning script", e);
                    freeNode(node);
                } catch (NodeException e) {
                    // TODO Que faire si noeud mort ?
                    // CHOIX 1 : on retourne le noeud sans rien faire
                    logger_dev.error("Error during cleaning script", e);
                    freeNode(node);
                }
            }
        }
    }

    // GET NODES *********************************************
    /**
     * Return a number of nodes between 0 and nbNodes matching the given selection script.
     * 
     * @param nbNodes the max number of nodes to ask for
     * @param selectionScript the script that must match the returned resources.
     * @return A node set that contains between 0 and nbNodes nodes matching the given script.
     */
    public NodeSet getAtMostNodes(int nbNodes, SelectionScript selectionScript) {
        return user.getAtMostNodes(new IntWrapper(nbNodes), selectionScript);
    }

    /**
     * Return a number of nodes between 0 and nbNodes matching the given selection script.
     * Nodes that are in the exclusion won't be returned.
     * 
     * @param nbNodes the max number of nodes to ask for
     * @param selectionScript the script that must match the returned resources.
     * @param exclusion the nodes that must not be returned
     * @return A node set that contains between 0 and nbNodes nodes matching the given script.
     */
    public NodeSet getAtMostNodes(int nbNodes, SelectionScript selectionScript, NodeSet exclusion) {
        return user.getAtMostNodes(new IntWrapper(nbNodes), selectionScript, exclusion);
    }

    /**
     * Return the exact number of nodes demanded matching the selection script or nothing (empty nodeset)
     * if the exact number of nodes has not been found.
     * 
     * @param nbNodes the number of nodes to ask for.
     * @param selectionScript the script that must match the returned resources.
     * @return the exact number of nodes demanded matching the selection script
     */
    public NodeSet getExactlyNodes(int nbNodes, SelectionScript selectionScript) {
        return user.getExactlyNodes(new IntWrapper(nbNodes), selectionScript);
    }

    // PROXY SPECIFIC METHODS ********************************
    /**
     * Shutdown this proxy by disconnecting user connection to the RM.
     */
    public void shutdownProxy() {
        if (running) {
            NodeSet ns = new NodeSet();
            ns.addAll(nodes.keySet());
            user.freeNodes(ns);
            running = false;

            if (logger.isInfoEnabled()) {
                logger.info("Infrastructure Manager Proxy Stopped");
            }

            user.disconnect();
        }
    }

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        if (auth != null) {
            try {
                user = auth.logAsUser(PASchedulerProperties.RESOURCE_MANAGER_USER.getValueAsString(),
                        PASchedulerProperties.RESOURCE_MANAGER_PASSWORD.getValueAsString());
            } catch (LoginException e) {
                throw new RuntimeException(e);
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("Infrastructure Manager Proxy started");
        }

        nodes = new HashMap<Node, ScriptResult<?>>();
    }

    /**
     * @see org.objectweb.proactive.RunActive#runActivity(org.objectweb.proactive.Body)
     */
    public void runActivity(Body body) {
        Service service = new Service(body);

        while (running && body.isActive()) {
            // Verify all nodes already managed
            verify();
            // Wait for any
            service.blockingServeOldest(VERIF_TIMEOUT);
        }
    }

    /**
     * Check the nodes to free and free the one that have to.
     */
    private void verify() {
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
            user.freeNodes(ns);
        }
    }

    /**
     * Free node considered as down.
     * The given node must be its name as a string.
     * 
     * @param nodeName a string that represents the node name to free.
     */
    public void freeDownNode(String nodeName) {
        //imcore.freeDownNode(nodeName);
    }

    /**
     * Return a state containing some informations about RM activity.
     * 
     * @return a state containing some informations about RM activity.
     */
    public RMState getRMState() {
        return user.getRMState();
    }
}
