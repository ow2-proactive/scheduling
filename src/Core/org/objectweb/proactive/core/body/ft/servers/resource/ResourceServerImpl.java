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
package org.objectweb.proactive.core.body.ft.servers.resource;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.ft.servers.FTServer;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.p2p.service.P2PService;
import org.objectweb.proactive.p2p.service.StartP2PService;
import org.objectweb.proactive.p2p.service.node.P2PNodeLookup;


/**
 * @author cdelbe
 * @since 2.2
 */
public class ResourceServerImpl implements ResourceServer {
    //logger
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.FAULT_TOLERANCE);

    // global server
    private FTServer server;

    // list of free ProActiveRuntime
    private List<Node> freeNodes;

    // OR use p2p infracstructure
    private P2PService serviceP2P;

    // number of returned free nodes
    private int nodeCounter;

    public ResourceServerImpl(FTServer server) {
        this.server = server;
        this.freeNodes = new ArrayList<Node>();
        this.nodeCounter = 0;
    }

    public ResourceServerImpl(FTServer server, String p2pServerURL) {
        this(server);
        try {
            Vector<String> v = new Vector<String>(1);
            v.add(p2pServerURL);
            StartP2PService startServiceP2P = new StartP2PService(v);
            PAProperties.PA_P2P_PORT.setValue("2603");
            startServiceP2P.start();
            this.serviceP2P = startServiceP2P.getP2PService();
            logger.info("[RESOURCE] Running on p2p network");
        } catch (ProActiveException e) {
            logger.error("**ERROR** Unable to reach p2p network");
            e.printStackTrace();
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.resource.ResourceServer#addFreeNode(org.objectweb.proactive.core.node.Node)
     */
    public void addFreeNode(Node n) throws RemoteException {
        logger.info("[RESSOURCE] A node is added : " +
            n.getNodeInformation().getURL());
        this.freeNodes.add(n);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.resource.ResourceServer#getFreeNode()
     */
    public Node getFreeNode() throws RemoteException {
        this.nodeCounter++;
        Node n = null;
        if (this.freeNodes.isEmpty()) {
            // use p2p service if any
            if (this.serviceP2P != null) {
                P2PNodeLookup p2pNodeLookup = this.serviceP2P.getNodes(1, "FT",
                        "1"); // SET JOB-ID
                n = (Node) ((p2pNodeLookup.getNodes(30000)).firstElement());
            } else {
                logger.error(
                    "[RESSOURCE] **ERROR** There is no resource nodes !");
                return null;
            }
        } else {
            n = (this.freeNodes.get(nodeCounter % (this.freeNodes.size())));
        }
        try {
            // testing free node
            n.getNumberOfActiveObjects();
        } catch (NodeException e) {
            // free node is unreachable !
            logger.info("[RESSOURCE] An unreachable node is removed.");
            this.freeNodes.remove(n);
            this.nodeCounter = 0;
            n = getFreeNode();
        }
        logger.info("[RESSOURCE] Return a node : " +
            n.getNodeInformation().getURL());
        return n;
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.resource.ResourceServer#initialize()
     */
    public void initialize() throws RemoteException {
        this.freeNodes = new ArrayList<Node>();
        this.nodeCounter = 0;
    }
}
