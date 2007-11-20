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
package org.objectweb.proactive.extra.infrastructuremanager.nodesource.dynamic;

import java.util.HashMap;
import java.util.Vector;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNodeImpl;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.IMNodeSource;
import org.objectweb.proactive.p2p.service.P2PService;
import org.objectweb.proactive.p2p.service.StartP2PService;
import org.objectweb.proactive.p2p.service.node.P2PNodeLookup;


/**
 * Implementation of a Peer to Peer dynamic node source.
 * TODO the methods {@link #getNode()} and {@link #releaseNode(IMNode)} must be implemented
 * @author proactive team
 *
 */
public class P2PNodeSource extends DynamicNodeSource implements InitActive {
    private static final long serialVersionUID = -9077907016230441233L;
    private P2PService p2pService;

    public P2PNodeSource(String id, int nbMaxNodes, int nice, int ttr) {
        super("P2PNS:" + id, nbMaxNodes, nice, ttr);
    }

    public P2PNodeSource() {
    }

    @Override
    public void initActivity(Body body) {
        super.initActivity(body);
        try {
            Vector<String> v = new Vector<String>();
            v.add("//lo.inria.fr:6695/");
            StartP2PService startServiceP2P = new StartP2PService(v);
            startServiceP2P.start();
            this.p2pService = startServiceP2P.getP2PService();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private HashMap<IMNode, P2PNodeLookup> lookups = new HashMap<IMNode, P2PNodeLookup>();

    @Override
    protected void releaseNode(IMNode node) {
        try {
            System.out.println(
                "[DYNAMIC P2P SOURCE] P2PNodeSource.releaseNode(" +
                node.getNodeInformation().getURL() + ")");
        } catch (NodeException e1) {
            e1.printStackTrace();
        }
        P2PNodeLookup p2pNodeLookup = this.lookups.get(node);
        try {
            p2pNodeLookup.killNode(node.getNodeInformation().getURL());
        } catch (NodeException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected IMNode getNode() {
        System.out.println("[DYNAMIC P2P SOURCE] P2PNodeSource.getNode()");
        // TODO Auto-generated method stub
        P2PNodeLookup p2pNodeLookup = this.p2pService.getNodes(1,
                "SchedulerNodes", "Scheduler");
        Node n = (Node) ((p2pNodeLookup.getNodes()).firstElement());
        IMNode imn = new IMNodeImpl(n, "SchedulerNodes", "PADNAME",
                (IMNodeSource) ProActiveObject.getStubOnThis());
        this.lookups.put(imn, p2pNodeLookup);
        return imn;
    }
}
