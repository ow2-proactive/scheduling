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
package org.objectweb.proactive.extra.infrastructuremanager.dataresource.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMDataResource;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.NodeSet;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.IMNodeSource;
import org.objectweb.proactive.extra.scheduler.common.scripting.ScriptResult;
import org.objectweb.proactive.extra.scheduler.common.scripting.VerifyingScript;


/**
 * Implementation of the {@link IMDataResource} interface,
 * using a {@link IMNodeSource} that provides the nodes to handle.
 * @author proactive team
 *
 */
public class IMDataResourceImpl implements IMDataResource, Serializable {

    /**  */
    private static final long serialVersionUID = -3170872605593251201L;
    private static final int MAX_VERIF_TIMEOUT = 120000;
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.IM_DATARESOURCE);

    // Attributes
    private IMNodeSource nodeManager;

    //----------------------------------------------------------------------//
    // CONSTRUCTORS
    /**
     * The {@link IMNodeManager} given in parameter must be not null.
     */
    public IMDataResourceImpl(IMNodeSource nodeSource) {
        this.nodeManager = nodeSource;
    }

    public void init() {
    }

    /**
     * find which {@link IMNode} correspond to the {@link Node} given in parameter
     * and change its state to 'free'.
     */
    public void freeNode(Node node) {
        ListIterator<IMNode> iterator = nodeManager.getBusyNodes().listIterator();

        String nodeURL = null;
        try {
            nodeURL = node.getNodeInformation().getURL();
        } catch (RuntimeException e) {
            logger.debug("A Runtime exception occured " +
                "while obtaining information on the node," +
                "the node must be down (it will be detected later)", e);
            // node is down,
            // will be detected later
            return;
        }
        while (iterator.hasNext()) {
            IMNode imnode = iterator.next();

            // Si le noeud correspond
            if (imnode.getNodeURL().equals(nodeURL)) {
                imnode.clean(); // Nettoyage du noeud
                nodeManager.setFree(imnode);
                break;
            }
        }
    }

    public void freeNodes(NodeSet nodes) {
        for (Node node : nodes)
            freeNode(node);
    }

    public void freeNodes(VirtualNode vnode) {
        ListIterator<IMNode> iterator = nodeManager.getBusyNodes().listIterator();
        while (iterator.hasNext()) {
            IMNode imnode = iterator.next();
            if (imnode.getVNodeName().equals(vnode.getName())) {
                imnode.clean(); // Cleaning the node
                nodeManager.setFree(imnode);
            }
        }
    }

    /**
     * The {@link #getAtMostNodes(IntWrapper, VerifyingScript)} method has three way to handle the request :
     * if there is no script, it returns at most the first nb free nodes.
     * If the script is a dynamic script, the method will test the resources,
     * until nb nodes verifies the script or if there is no node left.
     * In the case of a static script,
     * it will return in priority the nodes on which the given script
     * has already been verified.
     */
    public NodeSet getAtMostNodes(IntWrapper nb, VerifyingScript verifyingScript) {
        ArrayList<IMNode> nodes = nodeManager.getNodesByScript(verifyingScript,
                true);
        StringBuffer order = new StringBuffer();
        for (IMNode n : nodes)
            order.append(n.getHostName() + " ");
        logger.info("Nodes = " + order);
        NodeSet result = new NodeSet();
        int found = 0;
        if (verifyingScript == null) {
            logger.info("No verif script");
            while (!nodes.isEmpty() && (found < nb.intValue())) {
                IMNode imnode = nodes.remove(0);
                imnode.clean();
                try {
                    result.add(imnode.getNode());
                    nodeManager.setBusy(imnode);
                    found++;
                } catch (NodeException e) {
                    nodeManager.setDown(imnode);
                }
            }
        } else if (!verifyingScript.isDynamic()) {
            logger.info("Static verif script");
            while (!nodes.isEmpty() && (found < nb.intValue())) {
                IMNode node = nodes.remove(0);
                if (node.getScriptStatus().containsKey(verifyingScript) &&
                        node.getScriptStatus().get(verifyingScript)
                                .equals(IMNode.VERIFIED_SCRIPT)) {
                    node.clean();
                    try {
                        result.add(node.getNode());
                        nodeManager.setBusy(node);
                        found++;
                    } catch (NodeException e) {
                        nodeManager.setDown(node);
                    }
                } else {
                    break;
                }
            }

            Vector<ScriptResult<Boolean>> scriptResults = new Vector<ScriptResult<Boolean>>();
            Vector<IMNode> nodeResults = new Vector<IMNode>();
            int launched = found;
            while (!nodes.isEmpty() && (launched++ < nb.intValue())) {
                nodeResults.add(nodes.get(0));
                ScriptResult<Boolean> sr = nodes.get(0)
                                                .executeScript(verifyingScript);
                if (MOP.isReifiedObject(sr)) { // should check isAFuture...
                    scriptResults.add(sr);
                } else {
                    // scriptResult is an exception ?
                    if (sr.errorOccured()) {
                        logger.warn("======> A script result is ignored ...");
                    } else {
                        throw new RuntimeException("COMPRENDS PAS LA ...");
                    }
                }
                nodes.remove(0);
            }

            // Recupere les resultats
            while (!scriptResults.isEmpty() && !nodes.isEmpty() &&
                    (found < nb.intValue())) {
                try {
                    int idx = ProFuture.waitForAny(scriptResults,
                            MAX_VERIF_TIMEOUT);
                    IMNode imnode = nodeResults.remove(idx);
                    ScriptResult<Boolean> res = scriptResults.remove(idx);
                    if (res.errorOccured()) {
                        // nothing to do, just let the node in the free list
                        logger.info("Error occured executing verifying script",
                            res.getException());
                    } else if (res.getResult()) {
                        // Result OK
                        nodeManager.setVerifyingScript(imnode, verifyingScript);
                        imnode.clean();
                        try {
                            result.add(imnode.getNode());
                            nodeManager.setBusy(imnode);
                            found++;
                        } catch (NodeException e) {
                            nodeManager.setDown(imnode);
                            // try on a new node
                            nodeResults.add(nodes.get(0));
                            scriptResults.add(nodes.remove(0)
                                                   .executeScript(verifyingScript));
                        }
                    } else {
                        // result is false
                        nodeManager.setNotVerifyingScript(imnode,
                            verifyingScript);
                        // try on a new node
                        nodeResults.add(nodes.get(0));
                        scriptResults.add(nodes.remove(0)
                                               .executeScript(verifyingScript));
                    }
                } catch (ProActiveException e) {
                    // TODO Auto-generated catch block
                    // Wait For Any Timeout... 
                    // traitement special
                    e.printStackTrace();
                }
            }
        } else {
            logger.info("Dynamic verif script");
            Vector<ScriptResult<Boolean>> scriptResults = new Vector<ScriptResult<Boolean>>();
            Vector<IMNode> nodeResults = new Vector<IMNode>();

            // lance la verif sur les nb premier
            int launched = 0;
            while (!nodes.isEmpty() && (launched++ < nb.intValue())) {
                nodeResults.add(nodes.get(0));
                ScriptResult<Boolean> r = nodes.get(0)
                                               .executeScript(verifyingScript);
                scriptResults.add(r);
                nodes.remove(0);
            }

            // Recupere les resultats
            while (!scriptResults.isEmpty() && !nodes.isEmpty() &&
                    (found < nb.intValue())) {
                try {
                    //					int idx = ProActive.waitForAny(scriptResults, MAX_VERIF_TIMEOUT);
                    int idx = ProFuture.waitForAny(scriptResults);
                    IMNode imnode = nodeResults.remove(idx);
                    ScriptResult<Boolean> res = scriptResults.remove(idx);
                    if (res.errorOccured()) {
                        // nothing to do, just let the node in the free list
                        logger.info("Error occured executing verifying script",
                            res.getException());
                    } else if (res.getResult()) {
                        // Result OK
                        nodeManager.setVerifyingScript(imnode, verifyingScript);
                        imnode.clean();
                        try {
                            result.add(imnode.getNode());
                            nodeManager.setBusy(imnode);
                            found++;
                        } catch (NodeException e) {
                            nodeManager.setDown(imnode);
                            // try on a new node
                            nodeResults.add(nodes.get(0));
                            scriptResults.add(nodes.remove(0)
                                                   .executeScript(verifyingScript));
                        }
                    } else {
                        // result is false
                        nodeManager.setNotVerifyingScript(imnode,
                            verifyingScript);
                        // try on a new node
                        nodeResults.add(nodes.get(0));
                        scriptResults.add(nodes.remove(0)
                                               .executeScript(verifyingScript));
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    // Wait For Any Timeout... 
                    // traitement special
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    public NodeSet getExactlyNodes(IntWrapper nb,
        VerifyingScript verifyingScript) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Notifies the {@link IMNodeSource} that the given node is down.
     */
    public void nodeIsDown(IMNode imNode) {
        nodeManager.setDown(imNode);
    }
}
