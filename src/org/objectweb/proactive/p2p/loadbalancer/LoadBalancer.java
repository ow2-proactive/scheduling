/*
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 * 
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html 
 * 
 * Contributor(s): 				Javier Bustos 
 * 
 * ################################################################
 */

package org.objectweb.proactive.p2p.loadbalancer;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.BodyMap;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.migration.Migratable;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.p2p.service.P2PService;
import org.objectweb.proactive.p2p.service.node.P2PNodeLookup;

public class LoadBalancer {

	static Logger logger = Logger.getLogger(LoadBalancer.class.getName());

	private P2PService p2pSer;

	private int firstToMigrate;

	private boolean underloaded;

	private LoadMonitor lm;

	private double rank;

	public LoadBalancer(P2PService p2pSer) {
		this.p2pSer = p2pSer;
		underloaded = false;
		lm = new LoadMonitor(this);
		lm.start();
		rank = lm.getRanking();
	}

	public void register(long load) {
//		   	logger.info("*** ["+p2pSer.getAddress()+"] reporting load = "+load+" ranking = "+rank);
		if ((double) load > 90.0) {
			if (underloaded) {
				underloaded = false;
			}
			loadBalance();
		} else if ((double) load >= 20.0 * rank) {
			if (underloaded) {
				underloaded = false;
			}
		} else if (!underloaded) {
			underloaded = true;
		}
	}

	/***************************************************************************
	 * THIS IS THE METHOD CALLED BY THE MACHINE
	 **************************************************************************/

	public void loadBalance() {
		logger.info("*** [" + p2pSer.getAddress()+ "] asking for Load Balance");

		P2PNodeLookup lookup = p2pSer.getNodes(1, "p2pLoadBalancing","1");
		while (! lookup.allArrived()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		Node destNode = (Node) lookup.getAndRemoveNodes().get(0);
				if (destNode == null)
					return;
				destNode.getNodeInformation().setJobID("1");

				try {
					RuntimeFactory.getDefaultRuntime().addAcquaintance(
							destNode.getProActiveRuntime().getURL());
				} catch (ProActiveException e) {
					e.printStackTrace();
				}
				P2PloadBalance(destNode);

	}

	public void P2PloadBalance(Node destNode) {
		try {

			logger.info("****************** entering P2PloadBalance to "
					+ destNode.getNodeInformation().getURL());
			BodyMap knownBodies = LocalBodyStore.getInstance().getLocalBodies();

			if (knownBodies.size() < 1)
				return;

			int candidate = (int) (Math.random() * knownBodies.size());

			java.util.Iterator bodiesIterator = knownBodies.bodiesIterator();

			/** ******** Choosing the shortest service queue ******** */
			int minLength = Integer.MAX_VALUE;
			Body minBody = null;

			/** ******** Choosing the shortest service queue ******** */
			while (bodiesIterator.hasNext()) {
				Body activeObjectBody = (Body) bodiesIterator.next();
				Object testObject = activeObjectBody.getReifiedObject();
				boolean testSerialization = testObject instanceof Migratable;

				if (activeObjectBody.isAlive())
					if (activeObjectBody.isActive() && testSerialization) {
						int aoQueueLenght = activeObjectBody.getRequestQueue()
								.size();
						if (aoQueueLenght < minLength) {
							minLength = aoQueueLenght;
							minBody = activeObjectBody;
						}
					}
			}

			if (NodeFactory.isNodeLocal(destNode))
				return;

			if (minBody != null && minBody.isActive()) {
				logger.info("[Loadbalancer] Migrating from "
						+ minBody.getNodeURL() + " to "
						+ destNode.getNodeInformation().getURL());
				ProActive.migrateTo(minBody, destNode, true);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (MigrationException e) {
			/** ****** if you cannot migrate, is not my business ********** */
		}
	}

	public boolean AreYouUnderloaded() {
		return underloaded;
	}
}