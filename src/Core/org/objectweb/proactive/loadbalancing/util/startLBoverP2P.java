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
package org.objectweb.proactive.loadbalancing.util;

import java.rmi.AlreadyBoundException;
import java.util.Iterator;
import java.util.Vector;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.event.NodeCreationEvent;
import org.objectweb.proactive.core.event.NodeCreationEventListener;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.p2p.loadbalancer.P2PLoadBalancer;
import org.objectweb.proactive.p2p.service.P2PService;
import org.objectweb.proactive.p2p.service.StartP2PService;
import org.objectweb.proactive.p2p.service.node.P2PNodeLookup;
import org.objectweb.proactive.p2p.service.util.P2PConstants;


public class startLBoverP2P implements ProActiveInternalObject,
    NodeCreationEventListener {
    Vector arrivedNodes;
    P2PNodeLookup p2pNodeLookup;
    Vector loadBalancers;
    protected int nodesBooked;

    /**
     * @param args
     * @throws AlreadyBoundException
     * @throws ProActiveException
     * @throws Exception
     */
    public static void main(String[] args)
        throws AlreadyBoundException, ProActiveException {
        //Node n = NodeFactory.createNode("rmi://psychoquack:2805/StartTest");
        startLBoverP2P start = (startLBoverP2P) PAActiveObject.newActive(startLBoverP2P.class.getName(),
                null /*,n*/);

        start.doit("IntegrationTest");
        start.killMe();
    }

    public startLBoverP2P() {
    }

    public void doit(String JobId) throws ProActiveException {
        nodesBooked = 0;
        ProActiveConfiguration.load();
        ProActiveDescriptor pad;
        VirtualNode vn = null;
        arrivedNodes = new Vector();
        try {
            pad = PADeployment.getProactiveDescriptor(
                    "/user/sboukhal/home/TestLB.xml");
            vn = pad.getVirtualNode("IntegrationTest");
            ((VirtualNodeImpl) vn).addNodeCreationEventListener(this);
            System.out.println("Activation");
            vn.activate();
            System.out.println("/Activation");
        } catch (ProActiveException e2) {
            e2.printStackTrace();
        }

        while (nodesBooked < 9)
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }

        System.out.println("[TEST] Starting P2P Infranstructure");

        String peersFile = "/user/jbustos/home/peers.file";
        StartP2PService sp2ps = new StartP2PService(peersFile);
        try {
            sp2ps.start();
        } catch (ProActiveException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(1 * 1000);
        } catch (InterruptedException e) {
        }

        Iterator it = arrivedNodes.iterator();

        while (it.hasNext()) {
            Node remoteNode = (Node) it.next();

            sp2ps = (StartP2PService) PAActiveObject.newActive(StartP2PService.class.getName(),
                    new Object[] { peersFile }, remoteNode);
            sp2ps.start();
            PAActiveObject.terminateActiveObject(sp2ps, false);
        }

        arrivedNodes.add(PAActiveObject.getNode());

        try {
            Thread.sleep(2 * 1000);
        } catch (InterruptedException e) {
        }
        System.out.println("[TEST] Starting P2P LoadBalancer");

        String itAddress;
        loadBalancers = new Vector();
        P2PLoadBalancer p2plb = null;
        it = arrivedNodes.iterator();

        while (it.hasNext()) {
            Node n = (Node) it.next();
            itAddress = n.getNodeInformation().getURL();
            itAddress = itAddress.substring(0, itAddress.lastIndexOf("/")) +
                "/" + P2PConstants.P2P_NODE_NAME;

            p2plb = null;
            try {
                p2plb = (P2PLoadBalancer) PAActiveObject.newActive(P2PLoadBalancer.class.getName(),
                        null, itAddress);
                loadBalancers.add(p2plb);
            } catch (ActiveObjectCreationException e) {
                e.printStackTrace();
            } catch (NodeException e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(1000 * 1);
        } catch (InterruptedException e) {
        }

        it = loadBalancers.iterator();
        while (it.hasNext()) {
            ((P2PLoadBalancer) it.next()).init();
        }

        try {
            Thread.sleep(1000 * 1);
        } catch (InterruptedException e) {
        }

        try {
            JacobiDispatcher jacobiTest = new JacobiDispatcher("400", "25",
                    "3000", P2PService.getLocalP2PService());
        } catch (ProActiveException e) {
        } catch (Exception e) {
        }
    }

    public void nodeCreated(NodeCreationEvent event) {
        arrivedNodes.add(event.getNode());
        nodesBooked++;
        System.out.println("nodeCreated : " +
            event.getNode().getNodeInformation().getName());
    }

    public void killMe() {
        PAActiveObject.terminateActiveObject(true);
    }
}
