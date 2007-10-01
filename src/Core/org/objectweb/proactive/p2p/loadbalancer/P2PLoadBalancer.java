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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.p2p.loadbalancer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.loadbalancing.LoadBalancer;
import org.objectweb.proactive.loadbalancing.LoadBalancingConstants;
import org.objectweb.proactive.loadbalancing.LoadMonitor;
import org.objectweb.proactive.loadbalancing.metrics.CPURanking.LinuxCPURanking;
import org.objectweb.proactive.p2p.service.P2PService;
import org.objectweb.proactive.p2p.service.util.P2PConstants;


public class P2PLoadBalancer extends LoadBalancer implements RunActive,
    ProActiveInternalObject {
    static int MAX_KNOWN_PEERS = 10;
    static long MAX_DISTANCE = 100;
    protected String balancerName;
    protected Random randomizer;
    protected P2PService p2pService;
    protected Vector acquaintances;
    protected Vector forBalancing;
    protected Vector forStealing;
    protected P2PLoadBalancer myThis;
    protected double ranking;

    public P2PLoadBalancer() {
    }

    protected void addToBalanceList(int n) {
        int i = 0;
        Iterator it = acquaintances.iterator();
        while ((i < n) && it.hasNext()) {
            String itAddress = null;
            try {
                P2PService oService = (P2PService) it.next();
                itAddress = oService.getAddress().stringValue();
                itAddress = itAddress.substring(0, itAddress.lastIndexOf("/")) +
                    "/" + this.balancerName;
                P2PLoadBalancer oLB = (P2PLoadBalancer) ProActiveObject.lookupActive(P2PLoadBalancer.class.getName(),
                        itAddress);

                if (forBalancing.indexOf(oLB) < 0) {
                    long distance = ping(itAddress);
                    if (distance < MAX_DISTANCE) {
                        forBalancing.add(oLB);
                        i++;
                    }
                }
            } catch (ActiveObjectCreationException e) {
                logger.error("[P2PLB] ActiveObjectCreationException");
            } catch (IOException e) {
                logger.error("[P2PLB] IOException");
            } catch (ProActiveRuntimeException e) {
                logger.error(
                    "[P2PLoadBalancing] Trying to reach a non-existing peer from " +
                    myNode.getVMInformation().getHostName());
            }
        }
        if (i >= n) {
            return;
        }

        // Still missing acquaintances
        it = acquaintances.iterator();
        while ((i < n) && it.hasNext()) {
            try {
                P2PService oService = (P2PService) it.next();
                String itAddress = oService.getAddress().stringValue();
                itAddress = itAddress.substring(0, itAddress.lastIndexOf("/")) +
                    "/" + this.balancerName;
                P2PLoadBalancer oLB = (P2PLoadBalancer) ProActiveObject.lookupActive(P2PLoadBalancer.class.getName(),
                        itAddress);

                if (!forBalancing.contains(oLB)) {
                    forBalancing.add(oLB);
                    i++;
                }
            } catch (ActiveObjectCreationException e) {
                logger.error("[P2PLB] ActiveObjectCreationException");
            } catch (IOException e) {
                logger.error("[P2PLB] IOException");
            } catch (ProActiveRuntimeException e) {
                logger.error(
                    "[P2PLoadBalancing] Trying to reach a non-existing peer from " +
                    myNode.getVMInformation().getHostName());
            }
        }
    }

    protected void addToStealList(int n) {
        int i = 0;
        Iterator it = acquaintances.iterator();
        while ((i < n) && it.hasNext()) {
            String itAddress = null;
            try {
                P2PService oService = (P2PService) it.next();
                itAddress = oService.getAddress().stringValue();
                itAddress = itAddress.substring(0, itAddress.lastIndexOf("/")) +
                    "/robinhood";
                P2PLoadBalancer oLB = (P2PLoadBalancer) ProActiveObject.lookupActive(P2PLoadBalancer.class.getName(),
                        itAddress);

                if (!forStealing.contains(oLB)) {
                    long distance = ping(itAddress);
                    if (distance < MAX_DISTANCE) {
                        forStealing.add(oLB);
                        i++;
                    }
                }
            } catch (ActiveObjectCreationException e) {
                logger.error("[P2PLB] ActiveObjectCreationException");
            } catch (IOException e) {
                logger.error("[P2PLB] IOException");
            } catch (ProActiveRuntimeException e) {
                logger.error(
                    "[P2PLoadBalancing] Trying to reach a non-existing peer from " +
                    myNode.getVMInformation().getHostName());
            }
        }
        if (i >= n) {
            return;
        }

        // Still missing acquaintances
        it = acquaintances.iterator();
        while ((i < n) && it.hasNext()) {
            String itAddress = null;
            try {
                P2PService oService = (P2PService) it.next();
                itAddress = oService.getAddress().stringValue();
                itAddress = itAddress.substring(0, itAddress.lastIndexOf("/")) +
                    "/" + this.balancerName;
                P2PLoadBalancer oLB = (P2PLoadBalancer) ProActiveObject.lookupActive(P2PLoadBalancer.class.getName(),
                        itAddress);

                if (!forStealing.contains(oLB)) {
                    forBalancing.add(oLB);
                    i++;
                }
            } catch (ActiveObjectCreationException e) {
                logger.error("[P2PLB] ActiveObjectCreationException");
            } catch (IOException e) {
                logger.error("[P2PLB] IOException");
            } catch (ProActiveRuntimeException e) {
                logger.error(
                    "[P2PLoadBalancing] Trying to reach a non-existing peer from " +
                    myNode.getVMInformation().getHostName());
            }
        }
    }

    public double getRanking() {
        return this.ranking;
    }

    protected long ping(String nodeAddress) {
        // nodeAddress come in format "protocol://host:port/nodename"
        long timeResp = Long.MAX_VALUE;
        String itAddress = new String(nodeAddress.substring(0,
                    nodeAddress.lastIndexOf("/")));
        if (itAddress.lastIndexOf(':') >= (itAddress.length() - 6)) {
            itAddress = itAddress.substring(0, itAddress.lastIndexOf(':'));
        }
        if (itAddress.lastIndexOf('/') >= 0) {
            itAddress = itAddress.substring(itAddress.lastIndexOf('/') + 1);
        }

        BufferedReader in = null;
        Runtime rtime = Runtime.getRuntime();
        Process s;
        try {
            s = rtime.exec("/bin/ping -c 3 -l 2 -q " + itAddress);
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                if (line.indexOf('=') > 0) {
                    line = line.substring(line.indexOf('=') + 2,
                            line.indexOf("ms") - 1);
                    // rtt min/avg/max/mdev = 0.246/0.339/0.413/0.069 ms, pipe 3
                    String[] pingStats = line.split("/");
                    timeResp = 1 +
                        Math.round(Double.parseDouble(pingStats[1]));
                }
            }
            in.close();
        } catch (IOException e) {
            logger.error("[P2PLB] PING ERROR! ");
        }
        return timeResp;
    }

    /**
     * This method use the P2P infrastructure to search nodes which can
     * receive its active objects.  Method extended from LoadBalancer class.
     */
    @Override
    public void startBalancing() {
        int size = forBalancing.size();
        if (size < 1) {
            return;
        }

        int badRemote = 0;

        int first = randomizer.nextInt(size);
        for (int i = 0; (i < LoadBalancingConstants.SUBSET_SIZE) && (size > 0);
                i++) {
            P2PLoadBalancer remoteP2Plb = ((P2PLoadBalancer) forBalancing.get((first +
                    i) % size));
            try {
                remoteP2Plb.getActiveObjectsFrom(myThis, ranking);
            } catch (ProActiveRuntimeException e) {
                badRemote++;
                forStealing.remove((first + i) % size);
                size--;
            }
        }
        if (badRemote > 0) {
            addToStealList(badRemote);
        }
    }

    protected void getActiveObjectsFrom(P2PLoadBalancer remoteBalancer,
        double remoteRanking) {
        if (remoteRanking < (ranking * LoadBalancingConstants.BALANCE_FACTOR)) { // I'm better than him!
            remoteBalancer.sendActiveObjectsTo(myNode);
        }
    }

    /**
    * This method use the P2P infrastructure to search nodes which I
    * can steal work.  Method extended from LoadBalancer class.
    */
    @Override
    public void stealWork() {
        int size = forStealing.size();
        if (size < 1) {
            return;
        }

        int badRemote = 0;
        int first = randomizer.nextInt(size);
        for (int i = 0;
                (i < LoadBalancingConstants.NEIGHBORS_TO_STEAL) && (size > 0);
                i++) {
            P2PLoadBalancer remoteP2Plb = ((P2PLoadBalancer) forStealing.get((first +
                    i) % size));
            try {
                remoteP2Plb.sendActiveObjectsTo(myNode, ranking);
            } catch (ProActiveRuntimeException e) {
                badRemote++;
                forStealing.remove((first + i) % size);
                size--;
            }
        }
        if (badRemote > 0) {
            addToStealList(badRemote);
        }
    }

    @Override
    public void sendActiveObjectsTo(Node remoteNode, double remoteRanking) {
        if (this.ranking < (remoteRanking * LoadBalancingConstants.STEAL_FACTOR)) { // it's better than me!
            sendActiveObjectsTo(remoteNode);
        }
    }

    public void runActivity(Body body) {
        this.myThis = (P2PLoadBalancer) ProActiveObject.getStubOnThis();
        this.balancerName = "robinhood";

        /* Updating the node reference */
        try {
            String itAddress = body.getNodeURL();
            itAddress = itAddress.substring(0, itAddress.lastIndexOf("/")) +
                "/" + P2PConstants.SHARED_NODE_NAME + "_0";
            this.myNode = NodeFactory.getNode(itAddress);
        } catch (NodeException e) {
            e.printStackTrace();
        }

        // registering myself
        try {
            String itAddress = body.getNodeURL();
            itAddress = itAddress.substring(0, itAddress.lastIndexOf("/")) +
                "/" + this.balancerName;
            ProActiveObject.register(myThis, itAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Service service = new Service(body);

        while (body.isActive()) {
            service.blockingServeOldest();
        }
    }

    public void killMePlease() {
        lm.killMePlease();
        ProActiveObject.terminateActiveObject(myThis, true);
    }

    public void init() {
        this.forBalancing = new Vector(MAX_KNOWN_PEERS);
        this.forStealing = new Vector(MAX_KNOWN_PEERS);
        this.randomizer = new Random();

        /* We update the ranking */
        LinuxCPURanking thisCPURanking = new LinuxCPURanking();
        ranking = thisCPURanking.getRanking();

        try {
            this.acquaintances = ((P2PService) P2PService.getLocalP2PService()).getAcquaintanceList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* We update the lists */
        this.addToBalanceList(MAX_KNOWN_PEERS);
        this.addToStealList(MAX_KNOWN_PEERS);

        // by now we use only P2P over Linux
        lm = new LoadMonitor(myThis, thisCPURanking);
        new Thread(lm).start();
    }
}
