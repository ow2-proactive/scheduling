/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.LongWrapper;
import org.objectweb.proactive.loadbalancing.CPURanking;
import org.objectweb.proactive.loadbalancing.LinuxCPURanking;
import org.objectweb.proactive.loadbalancing.LoadBalancer;
import org.objectweb.proactive.loadbalancing.LoadBalancingConstants;
import org.objectweb.proactive.loadbalancing.LoadMonitorLinux;
import org.objectweb.proactive.p2p.service.P2PService;


public class P2PLoadBalancer extends LoadBalancer implements RunActive {
	static int MAX_KNOWN_PEERS = 10;
	static long MAX_DISTANCE = 100;
    protected Random randomizer;
	protected P2PService p2pService;
    protected Vector acquaintances, forBalancing, forStealing;
    protected Node myNode;
    protected P2PLoadBalancer myThis;
    
    public P2PLoadBalancer() {}

    protected void addToBalanceList(int n) {
    	int i=0;
    	Iterator it = acquaintances.iterator();
    	while (i < n && it.hasNext()) {
    		P2PService oService = (P2PService) it.next();
    		String itAddress = oService.getAddress().stringValue();
    		itAddress = itAddress.substring(0,itAddress.lastIndexOf("/"))+"/robinhood";
    		try {
				P2PLoadBalancer oLB =  (P2PLoadBalancer) ProActive.lookupActive(P2PLoadBalancer.class.getName(),itAddress);
				
				if (!forBalancing.contains(oLB)) {
					long distance = System.currentTimeMillis() - oLB.ping(new LongWrapper(System.currentTimeMillis())).longValue();
					if (distance < MAX_DISTANCE && oLB.getRanking() > this.ranking * LoadBalancingConstants.RANKING_EPSILON) {
						forBalancing.add(oLB);
						i++;
						}
					}
				} catch (ActiveObjectCreationException e) {
				} catch (IOException e) {
				}
    		}
    	if (i >= n) return;
    	
    	// Still missing acquaintances

    	it = acquaintances.iterator();
    	while (i < n && it.hasNext()) {
    		P2PService oService = (P2PService) it.next();
    		String itAddress = oService.getAddress().stringValue();
    		itAddress = itAddress.substring(0,itAddress.lastIndexOf("/"))+"/robinhood";
    		try {
				P2PLoadBalancer oLB =  (P2PLoadBalancer) ProActive.lookupActive(P2PLoadBalancer.class.getName(),itAddress);
				
				if (!forBalancing.contains(oLB)) {
					if (oLB.getRanking() > this.ranking * LoadBalancingConstants.RANKING_EPSILON) {
						forBalancing.add(oLB);
						i++;
						}
					}
				} catch (ActiveObjectCreationException e) {
				} catch (IOException e) {
				}
    		}
}
    
    public LongWrapper ping (LongWrapper lw) {
    	return lw;
    }
    
    public double getRanking() {
    	return this.ranking;
    }
    
    protected void addToStealList(int n) {
    	int i=0;
    	Iterator it = acquaintances.iterator();
    	while (i < n && it.hasNext()) {
    		P2PService oService = (P2PService) it.next();
    		String itAddress = oService.getAddress().stringValue();
    		itAddress = itAddress.substring(0,itAddress.lastIndexOf("/"))+"/robinhood";
    		try {
				P2PLoadBalancer oLB =  (P2PLoadBalancer) ProActive.lookupActive(P2PLoadBalancer.class.getName(),itAddress);
				
				if (!forStealing.contains(oLB)) {
					long distance = System.currentTimeMillis() - oLB.ping(new LongWrapper(System.currentTimeMillis())).longValue();
					if (distance < MAX_DISTANCE && oLB.getRanking() < this.ranking * LoadBalancingConstants.STEAL_PONDERATION) {
						forStealing.add(oLB);
						i++;
						}
					}
				} catch (ActiveObjectCreationException e) {
				} catch (IOException e) {
				}
    		}
    	if (i >= n) return;
    	
    	// Still missing acquaintances

    	it = acquaintances.iterator();
    	while (i < n && it.hasNext()) {
    		P2PService oService = (P2PService) it.next();
    		String itAddress = oService.getAddress().stringValue();
    		itAddress = itAddress.substring(0,itAddress.lastIndexOf("/"))+"/robinhood";
    		try {
				P2PLoadBalancer oLB =  (P2PLoadBalancer) ProActive.lookupActive(P2PLoadBalancer.class.getName(),itAddress);
				
				if (!forStealing.contains(oLB)) {
					if (oLB.getRanking() < this.ranking * LoadBalancingConstants.STEAL_PONDERATION) {
						forBalancing.add(oLB);
						i++;
						}
					}
				} catch (ActiveObjectCreationException e) {
				} catch (IOException e) {
				}
    		}
    	
    	
    }
    /**
     * This method use the P2P infrastructure to search nodes which can
     * receive its active objects.  Method extended from LoadBalancer class.
     */
    public void startBalancing() {
    	int size = forBalancing.size();
    	if (size < 1) return;
    	
    	boolean fixList = false;
    	int badLinks = 0;
    	
    	int first = randomizer.nextInt(size);
    	for (int i = 0; i < LoadBalancingConstants.SUBSET_SIZE; i++) {
  		P2PLoadBalancer remoteP2Plb = ((P2PLoadBalancer) forBalancing.get((first+i)%size));
  		try {
    		remoteP2Plb.sendActiveObjectsTo(myNode);
  		} catch (RemoteException e) {
  			fixList = true;
  			forBalancing.remove(remoteP2Plb);
  			badLinks++;
  			}
    	}
    	
    	if (fixList) addToBalanceList(badLinks); 
    }

    /**
     * This method use the P2P infrastructure to search nodes which I
     * can steal work.  Method extended from LoadBalancer class.
     */
    public void stealWork() {
    	int size = forStealing.size();
    	if (size < 1) return;
    	
    	boolean fixList = false;
    	int badLinks = 0;
    	
    	int first = randomizer.nextInt(size);
    	for (int i = 0; i < LoadBalancingConstants.NEIGHBORS_TO_STEAL; i++) {
    		P2PLoadBalancer remoteP2Plb = ((P2PLoadBalancer) forStealing.get((first+i)%size));
    		try {
    			remoteP2Plb.sendActiveObjectsTo(myNode);
    			} catch (RemoteException e) {
    				fixList = true;
    				forStealing.remove(remoteP2Plb);
    				badLinks ++;
    		}
    	}
    	
    	if (fixList) addToStealList(badLinks); 
    }

public void runActivity(Body body) {

	this.acquaintances = p2pService.getAcquaintanceList();
	this.forBalancing = new Vector(MAX_KNOWN_PEERS);
	this.forStealing = new Vector(MAX_KNOWN_PEERS);
    this.randomizer = new Random();
    this.underloaded = false;
    this.myThis = (P2PLoadBalancer) ProActive.getStubOnThis();

    /* Updating the node reference */
    try {
    	myNode = ProActive.getNode();
		} catch (NodeException e) {
			ProActive.terminateActiveObject(myThis,true);
		}

    // registering myself
    try {
		ProActive.register(myThis,"///robinhood");
	} catch (IOException e) {
		ProActive.terminateActiveObject(myThis,true);
	}

	// by now we use only P2P over Linux
    lm = new LoadMonitorLinux(myThis);
    new Thread(lm).start();

    /* And we update the ranking */
    CPURanking thisCPURanking = new LinuxCPURanking();
    ranking = thisCPURanking.getRanking();
    
    /* We update the lists */
    this.addToBalanceList(MAX_KNOWN_PEERS);
    this.addToStealList(MAX_KNOWN_PEERS);
    
	Service service = new Service(body);
	while (body.isActive()) {
	    service.blockingServeOldest(); 
	    }
	}

	public void killMePlease() {
		ProActive.terminateActiveObject(myThis,true);
	}
}
