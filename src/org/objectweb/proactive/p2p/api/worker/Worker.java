/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.p2p.api.worker;

import org.apache.log4j.Logger;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.p2p.api.problem.Problem;
import org.objectweb.proactive.p2p.api.problem.Result;
import org.objectweb.proactive.p2p.service.P2PService;
import org.objectweb.proactive.p2p.service.node.P2PNodeLookup;

import java.io.Serializable;

import java.util.NoSuchElementException;
import java.util.Vector;


/**
 * @author Alexandre di Costanzo
 *
 */
public class Worker implements Serializable, InitActive {
    protected static Logger logger = Logger.getLogger(Worker.class.getName());
    private Problem problem = null;
    private Vector infos = new Vector();
    private Worker brothers = null;
    private Worker mother = null;
    private Worker daughter = null;
    private P2PService service = null;

    public Worker() {
        // empty constructor
    }

    public Worker(Worker mother) {
        this.mother = mother;
    }

    public Result execute(Object[] params) {
        Result result = this.problem.execute(params);
        return result;
    }

    private P2PNodeLookup booking;

    public int getPeers(int n) throws ProActiveException {
        System.out.println("On cherche les peers");
        // this.booking = this.service.getNodes(n, null, null);
        System.out.println("C'est bon j'a la reservation");
        this.booking.allArrived();
        //return this.booking.size();
        return 0;
    }

    public void relaxPeers() {
        booking = null;
    }

    /**
     * @param params [][0] = Woker mother ; [][1]=Problem
     * @throws ProActiveException
     */
    public void createDaughter(Object[][] params) throws ProActiveException {
        // Nodes Creation
        //Node[] nodes = (Node[]) this.booking.toArray(new Node[this.booking.size()]);
        Node[] nodes = null;
        try {
            // Group Creation
            Object[][] paramsGroup = new Object[params.length][1];
            for (int i = 0; i < params.length; i++) {
                paramsGroup[i][0] = params[i][0];
            }

            // waiting nodes
            Thread.sleep(1000);
            this.daughter = (Worker) ProActiveGroup.newGroup(Worker.class.getName(),
                    paramsGroup, nodes);
            Group g = ProActiveGroup.getGroup(this.daughter);
            for (int i = 0; i < g.size(); i++) {
                ((Worker) g.get(i)).setProblem((Problem) params[i][1]);
            }
        } catch (ClassNotReifiableException e) {
            logger.warn("Worker is not Reifiable", e);
        } catch (ActiveObjectCreationException e) {
            logger.warn("Couldn't create an Active Object on remote JVM", e);
        } catch (NodeException e) {
            logger.warn("problem with a remote node", e);
        } catch (ClassNotFoundException e) {
            logger.warn("Worker Class was not found", e);
        } catch (InterruptedException e) {
        }
    }

    /**
     * @param params
     * @return
     */
    public Result executeDaughter(Object[] params) {
        ProActiveGroup.setScatterGroup(this.daughter);
        Result results = this.daughter.execute(params);
        while (ProActive.isAwaited(results)) {
            System.out.println("J'attends...");
            Body body = ProActive.getBodyOnThis();
            Service service = new Service(body);
            System.out.println(">>>>Service" + service.getOldest());
            service.blockingServeOldest();
        }
        return this.problem.gather((Result[]) ProActiveGroup.getGroup(results)
                                                            .toArray());
    }

    public void sendInfoToAll(Object info) {
        if (this.mother != null) {
            this.mother.receiveInfoForAll(info, false);
        }
        if (this.daughter != null) {
            this.daughter.receiveInfoForAll(info, true);
        }
    }

    public void sendInfoToBrother(Object info) {
        if (this.brothers != null) {
            this.brothers.receiveInfo(info);
        }
    }

    public void sendInfoToMother(Object info) {
        if (this.mother != null) {
            this.mother.receiveInfo(info);
        }
    }

    public void receiveInfo(Object info) {
        this.infos.add(info);
        if (this.daughter != null) {
            this.daughter.receiveInfo(info);
        }
    }

    public void receiveInfoForAll(Object info, boolean fromMother) {
        this.infos.add(info);
        if (!fromMother) {
            this.mother.receiveInfoForAll(info, false);
        }
        if (this.daughter != null) {
            this.daughter.receiveInfoForAll(info, true);
        }
    }

    public Worker getMother() {
        return mother;
    }

    public Worker getDaughters() {
        return daughter;
    }

    public Worker getBrothers() {
        return brothers;
    }

    /**
     * @return Returns the infos.
     */
    public Vector getInfos() {
        return infos;
    }

    public Object lastInfo() throws NoSuchElementException {
        return infos.lastElement();
    }

    public int haveNewInfo() {
        return this.infos.size();
    }

    /**
     * @return Returns the problem.
     */
    public Problem getProblem() {
        return problem;
    }

    public void setBrothers(Worker brothers) {
        this.brothers = brothers;
        Group group = ProActiveGroup.getGroup(this.brothers);
        group.remove(ProActive.getStubOnThis());
    }

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        // Found the P2P Service on this host
        try {
            Node p2pNode = NodeFactory.getNode("rmi://localhost:2410/" +
                    P2PService.P2P_NODE_NAME);
            Object[] ao = p2pNode.getActiveObjects(P2PService.class.getName());
            this.service = (P2PService) ao[0];
        } catch (NodeException e) {
            logger.fatal("Couldn't found a P2P Node on this host", e);
            System.exit(69);
        } catch (ActiveObjectCreationException e) {
            logger.fatal("Couldn't found a P2P Service on this host", e);
            System.exit(69);
        }
    }

    /**
     * @param problem The problem to set.
     */
    public void setProblem(Problem problem) {
        this.problem = problem;
        this.problem.setWorker((Worker) ProActive.getStubOnThis());
    }
}
