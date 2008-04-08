/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.extensions.scheduler.resourcemanager;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

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
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.extensions.resourcemanager.common.RMConstants;
import org.objectweb.proactive.extensions.resourcemanager.exception.RMException;
import org.objectweb.proactive.extensions.resourcemanager.frontend.NodeSet;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMConnection;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMUser;
import org.objectweb.proactive.extensions.scheduler.common.scripting.Script;
import org.objectweb.proactive.extensions.scheduler.common.scripting.ScriptHandler;
import org.objectweb.proactive.extensions.scheduler.common.scripting.ScriptLoader;
import org.objectweb.proactive.extensions.scheduler.common.scripting.ScriptResult;
import org.objectweb.proactive.extensions.scheduler.common.scripting.SelectionScript;


/**
 * The Resource Manager Proxy provides an interface with the
 * Resource Manager. It connects to RMUser interface,
 * and adds the Post Scripting management.
 *
 * @author The ProActive Team
 * @version 1.0, Jun 15, 2007
 * @since ProActive 3.2
 */
public class ResourceManagerProxy implements InitActive, RunActive, RMConstants {
    private static final long VERIF_TIMEOUT = 10000;
    private static Logger logger = ProActiveLogger.getLogger(Loggers.SCHEDULER);
    private RMUser user;
    private HashMap<Node, ScriptResult<?>> nodes;
    private boolean running = true;

    /** ProActive no Args constructor **/
    public ResourceManagerProxy() {
    } //proactive no arg constructor

    /** IMProxy constructor.
     *
     * @param user the User interface
     */
    public ResourceManagerProxy(RMUser user) {
        this.user = user;
    }

    /**
     * Get a IMProxy by its URI (example : "rmi://localhost:1099/" ).
     *
     *
     * @param uriIM
     * @return
     * @throws ActiveObjectCreationException
     * @throws IOException
     * @throws NodeException
     */
    public static ResourceManagerProxy getProxy(URI uriIM) throws ActiveObjectCreationException, IOException,
            NodeException {
        try {
            String url = uriIM.toString();
            if (!url.endsWith("/")) {
                url += "/";
            }
            RMUser user = RMConnection.connectAsUser(url + NAME_ACTIVE_OBJECT_RMUSER);
            return (ResourceManagerProxy) PAActiveObject.newActive(ResourceManagerProxy.class
                    .getCanonicalName(), new Object[] { user });
        } catch (RMException e) {
            throw new ActiveObjectCreationException(e);
        }
    }

    public StringWrapper echo() {
        return new StringWrapper("User Interface says " + user.echo());

    }

    // FREE NODES *********************************************
    /**
     * Simply free a Node
     * @see RMUser#freeNode(Node)
     *
     * @param node
     */
    public void freeNode(Node node) {
        if (logger.isInfoEnabled()) {
            logger.info("Node freed : " + node.getNodeInformation().getURL());
        }

        user.freeNode(node);
    }

    /**
     * Execute the postScript on the node before freeing it.
     * @see RMUser#freeNode(Node)
     *
     * @param node
     * @param postScript
     */
    public void freeNode(Node node, Script<?> postScript) {
        if (node != null) {
            if (postScript == null) {
                freeNode(node);
            } else {
                try {
                    ScriptHandler handler = ScriptLoader.createHandler(node);
                    nodes.put(node, handler.handle(postScript));

                    if (logger.isInfoEnabled()) {
                        logger.info("Post Script handled on node" + node.getNodeInformation().getURL());
                    }
                } catch (ActiveObjectCreationException e) {
                    // TODO Que faire si noeud mort ?
                    // CHOIX 1 : on retourne le noeud sans rien faire
                    e.printStackTrace();
                    freeNode(node);
                } catch (NodeException e) {
                    // TODO Que faire si noeud mort ?
                    // CHOIX 1 : on retourne le noeud sans rien faire
                    e.printStackTrace();
                    freeNode(node);
                }
            }
        }
    }

    /**
     * Simply free a NodeSet
     * @see RMUser#freeNodes(NodeSet)
     *
     * @param nodes
     */
    public void freeNodes(NodeSet nodes) {
        if (logger.isInfoEnabled()) {
            logger.info("Nodes freed : " + nodes.size() + " nodes");
        }

        user.freeNodes(nodes);
    }

    /**
     * Execute the postScript on the nodes before freeing them.
     * @see RMUser#freeNodes(NodeSet)
     *
     * @param nodes
     * @param postScript
     */
    public void freeNodes(NodeSet nodes, Script<?> postScript) {
        if (postScript == null) {
            freeNodes(nodes);
        } else {
            for (Node node : nodes) {
                try {
                    ScriptHandler handler = ScriptLoader.createHandler(node);
                    ScriptResult<?> res = handler.handle(postScript);
                    this.nodes.put(node, res);

                    if (logger.isInfoEnabled()) {
                        logger.info("Post Script handled on node" + node.getNodeInformation().getURL());
                    }
                } catch (ActiveObjectCreationException e) {
                    // TODO Que faire si noeud mort ?
                    // CHOIX 1 : on retourne le noeud sans rien faire
                    logger.error("Error during post script", e);
                    freeNode(node);
                } catch (NodeException e) {
                    // TODO Que faire si noeud mort ?
                    // CHOIX 1 : on retourne le noeud sans rien faire
                    logger.error("Error during post script", e);
                    freeNode(node);
                }
            }
        }
    }

    // GET NODES *********************************************
    public NodeSet getAtMostNodes(int nbNodes, SelectionScript selectionScript) {
        return user.getAtMostNodes(new IntWrapper(nbNodes), selectionScript);
    }

    public NodeSet getExactlyNodes(int nbNodes, SelectionScript selectionScript) {
        return user.getExactlyNodes(new IntWrapper(nbNodes), selectionScript);
    }

    // GET INFORMATIONS **************************************
    public IntWrapper getNumberOfAllResources() {
        return user.getTotalNodesNumber();
    }

    public IntWrapper getNumberOfFreeResource() {
        return user.getFreeNodesNumber();
    }

    public BooleanWrapper hasFreeResources() {
        return new BooleanWrapper(getNumberOfFreeResource().intValue() != 0);
    }

    // PROXY SPECIFIC METHODS ********************************
    public void shutdownProxy() {
        if (running) {
            NodeSet ns = new NodeSet();
            ns.addAll(nodes.keySet());
            user.freeNodes(ns);
            running = false;

            if (logger.isInfoEnabled()) {
                logger.info("RM Proxy Stopped");
            }
        }
    }

    public void initActivity(Body body) {
        if (logger.isInfoEnabled()) {
            logger.info("Infrastructure Manager Proxy started");
        }

        nodes = new HashMap<Node, ScriptResult<?>>();
    }

    public void runActivity(Body body) {
        Service service = new Service(body);

        while (running && body.isActive()) {
            // Verify all nodes already managed
            verify();
            // Wait for any
            service.blockingServeOldest(VERIF_TIMEOUT);
        }
    }

    private void verify() {
        Iterator<Entry<Node, ScriptResult<?>>> iterator = nodes.entrySet().iterator();
        NodeSet ns = new NodeSet();

        while (iterator.hasNext()) {
            Entry<Node, ScriptResult<?>> entry = iterator.next();

            if (!PAFuture.isAwaited(entry.getValue())) { // !awaited = arrived
                if (logger.isInfoEnabled()) {
                    logger.info("Post script successfull, node freed : " +
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

    public void freeDownNode(String nodeName) {
        //imcore.freeDownNode(nodeName);
    }

    public RMState getRMState() {
        //user.getRMState();
        //how to ensure that only one connection is performed
        return null;
    }
}
