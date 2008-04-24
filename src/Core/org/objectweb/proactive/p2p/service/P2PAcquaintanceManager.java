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
package org.objectweb.proactive.p2p.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.group.ExceptionInGroup;
import org.objectweb.proactive.core.group.ExceptionListException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.IntMutableWrapper;
import org.objectweb.proactive.p2p.service.exception.PeerDoesntExist;
import org.objectweb.proactive.p2p.service.messages.AcquaintanceRequest;
import org.objectweb.proactive.p2p.service.util.P2PConstants;


/**
 * Updating the group of exportAcquaintances of the P2P service.
 * 
 * @author Alexandre di Costanzo
 * 
 */
public class P2PAcquaintanceManager implements InitActive, RunActive, Serializable, P2PConstants,
        ProActiveInternalObject {

    /**
     * The maximum waiting time before considering an ACQ request is lost and
     * should be resent
     */
    private static long MAX_WAIT_TIME = 10000;
    private final static Logger logger = ProActiveLogger.getLogger(Loggers.P2P_ACQUAINTANCES);
    static public int NOA = Integer.parseInt(PAProperties.PA_P2P_NOA.getValue());

    // static public int NOA = new NOAPowerLawGenerator(1, 9, -3).nextInt();

    // protected Random rand = new Random();
    private Random randomizer = new Random();

    /**
     * A ProActive Stub on the local P2PService
     */
    private P2PService localService = null;
    private P2PService acquaintancesActived = null;
    protected AcquaintancesWrapper acquaintances;

    // store the name of awaited replies for setting acquaintances
    protected HashMap<String, DatedRequest> awaitedReplies = new HashMap<String, DatedRequest>();

    // list of prefered acquaintances
    private HashSet<String> preferedAcquaintancesURLs = new HashSet<String>();

    /**
     * The empty constructor for activating.
     */
    public P2PAcquaintanceManager() {
        // empty constructor
    }

    /**
     * Construct a new <code>P2PAcquaintanceManager</code>.
     * 
     * @param localService
     *            a reference to the local P2P service.
     * @param prefferedAcq
     *            a list of first-contact acquaintances
     */
    public P2PAcquaintanceManager(P2PService localService, Vector<String> prefferedAcq) {
        this.localService = localService;
        setPreferedAcq(prefferedAcq);
    }

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        // String nodeUrl = body.getNodeURL();
        this.acquaintances = new AcquaintancesWrapper();
        logger.debug("Group of exportAcquaintances successfuly created");
    }

    /**
     * @see org.objectweb.proactive.RunActive#runActivity(org.objectweb.proactive.Body)
     */
    public void runActivity(Body body) {
        Service service = new Service(body);
        body.getRequestQueue();
        while (body.isActive()) {
            if (this.acquaintances.size() > 0) {
                // Register the local P2P service in all exportAcquaintances
                logger.debug("Sending heart-beat");
                try {
                    this.acquaintances.getAcquaintances().heartBeat();
                } catch (ExceptionListException e) {
                    tidyUpGroup(e);
                }
                logger.debug("Heart-beat sent");
            }

            if (this.getEstimatedNumberOfAcquaintances() < NOA) {
                // this.dumpTables();
                lookForNewPeers();
            } else if (this.acquaintances.size() > NOA) {
                // we should drop some here
                // do we go for all at once or just one at a time?
                logger.info("I have too many neighbors!");
                this.dropRandomPeer();
            } else {
                logger.info("I have reached the maximum of acquaintance ");
            }
            waitTTU(service);
            // this.dumpTables();
            this.cleanAwaitedReplies();
        }
    }

    private void tidyUpGroup(ExceptionListException e) {
        Iterator<ExceptionInGroup> it = e.iterator();
        while (it.hasNext()) {
            ExceptionInGroup exceptionInGroup = (ExceptionInGroup) it.next();
            Group<P2PService> g = PAGroup.getGroup(this.acquaintances.getAcquaintances());
            g.remove(exceptionInGroup.getObject());
        }
    }

    /**
     * 
     */
    protected void lookForNewPeers() {
        // How many peers ?
        if (this.getEstimatedNumberOfAcquaintances() < NOA) {
            // Looking for new peers
            logger.debug("NOA is " + NOA + " - Size of P2PAcquaintanceManager is " +
                this.getEstimatedNumberOfAcquaintances() +
                " looking for new acquaintances through prefered ones");
            this.connectToPreferedAcquaintances();
        }

        // How many peers ?
        if (this.getEstimatedNumberOfAcquaintances() < NOA) {
            // Looking for new peers
            logger.debug("NOA is " + NOA + " - Size of P2PAcquaintanceManager is " +
                this.getEstimatedNumberOfAcquaintances() +
                " looking for new acquaintances through exploration");

            // Sending exploring message
            // System.out.println(">>>>>>>>>>>>>>>>>
            // P2PAcquaintanceManager.runActivity()");
            this.localService.explore();
            logger.debug("Explorating message sent");
        }
    }

    protected void waitTTU(Service service) {
        // Waiting TTU & serving requests
        logger.debug("Waiting for " + P2PService.TTU + "ms");
        long endTime = System.currentTimeMillis() + P2PService.TTU;
        service.blockingServeOldest(P2PService.TTU);
        while (System.currentTimeMillis() < endTime) {
            try {
                service.blockingServeOldest(endTime - System.currentTimeMillis());
            } catch (ProActiveRuntimeException e) {
                e.printStackTrace();
                logger.debug("Certainly because the body is not active", e);
            }
        }
        logger.debug("End waiting");
    }

    public void connectToPreferedAcquaintances() {
        int size = this.preferedAcquaintancesURLs.size();

        // int index = 0;
        // System.out.println(
        // "P2PAcquaintanceManager.connectToPreferedAcquaintances() number of
        // URLs " +
        // preferedAcquaintancesURLs.size());
        // while(!this.peers.isEmpty()) {
        // for (int i = 0; i < size; i++) {
        HashSet<String> newSet = new HashSet<String>();
        String tmp = null;
        Iterator it = this.preferedAcquaintancesURLs.iterator();
        while (it.hasNext() && (this.getEstimatedNumberOfAcquaintances() < NOA)) {
            // remove it from the current HashSet
            // and add it in the temporary one
            tmp = (String) it.next();
            // newSet.add(tmp);
            it.remove();
            String peerUrl = buildCorrectUrl(urlAdderP2PNodeName(tmp));

            // System.out.println(
            // "P2PAcquaintanceManager.connectToPreferedAcquaintances() " +
            // peerUrl);
            try {
                Node distNode = NodeFactory.getNode(peerUrl);
                P2PService peer = (P2PService) distNode.getActiveObjects(P2PService.class.getName())[0];
                if ( // !peer.equals(this.localService) &&
                !this.contains(peer).booleanValue()) {
                    // Send a message to the remote peer to register myself
                    System.out.println("P2PAcquaintanceManager requesting peer " + peerUrl);
                    // peer.registerRequest(this.localService);
                    startAcquaintanceHandShake(peerUrl, peer);
                } else {
                    newSet.add(peerUrl);
                }
            } catch (Exception e) {
                System.out.println("The peer at " + peerUrl + " couldn't be contacted");
                e.printStackTrace();
                // put it back for later use
                newSet.add(peerUrl);
                // remove it from the awaited
                // awaitedReplies.remove(peerUrl));
            }
        }
        if (this.size().intValue() == 0) {
            logger.info("No peer could be found to join the network");
            // System.out
            // .println("P2PAcquaintanceManager.connectToPreferedAcquaintances()
            // urls available " + this.preferedAcquaintancesURLs.size());
        } else {
            // add all the remaining urls
            // System.out
            // .println("P2PAcquaintanceManager.connectToPreferedAcquaintances()
            // adding the remaining urls " +
            // this.preferedAcquaintancesURLs.size());
            newSet.addAll(this.preferedAcquaintancesURLs);
        }
        this.preferedAcquaintancesURLs = newSet;
        // System.out
        // .println("P2PAcquaintanceManager.connectToPreferedAcquaintances() at
        // the end " + this.preferedAcquaintancesURLs.size());
    }

    /**
     * Remove awaited requests which have timeouted
     */
    @SuppressWarnings("unchecked")
    public void cleanAwaitedReplies() {
        // System.out.println("P2PAcquaintanceManager.cleanAwaitedReplies()
        // still " + awaitedReplies.size() );
        ArrayList<String> urls = new ArrayList<String>();
        Set<Map.Entry<String, DatedRequest>> map = awaitedReplies.entrySet();
        Iterator it = map.iterator();
        while (it.hasNext()) {
            Map.Entry<String, DatedRequest> entry = (Map.Entry<String, DatedRequest>) it.next();

            // System.out.println("P2PAcquaintanceManager.cleanAwaitedReplies()
            // request sent at " + ((DatedRequest) entry.getValue()).getTime()
            // );
            // System.out.println("P2PAcquaintanceManager.cleanAwaitedReplies()
            // now " + System.currentTimeMillis());
            if ((System.currentTimeMillis() - (entry.getValue()).getTime()) > MAX_WAIT_TIME) {
                System.out.println("xxxxx Peer " + entry.getKey() + " did not reply to our request");
                // this guy did not reply so we should put it back in the
                // preferedACQList
                urls.add(entry.getKey());
                it.remove();
            }
        }
        it = urls.iterator();
        while (it.hasNext()) {
            this.preferedAcquaintancesURLs.add((String) it.next());
        }
    }

    /**
     * Starts an acquaintance handshake Send a message to the peer and add it to
     * the awaited replies list
     * 
     * @param peerUrl
     * @param peer
     */
    public void startAcquaintanceHandShake(String peerUrl, P2PService peer) {
        this.localService.transmit(new AcquaintanceRequest(1), peer);

        // peer.message(new AcquaintanceRequest(1,
        // this.localService.generateUuid(), this.localService));
        System.out.println("XXXXXX putting in awaited List " + peerUrl);
        awaitedReplies.put(buildCorrectUrl(peerUrl), new DatedRequest(peer, System.currentTimeMillis()));
    }

    /**
     * @return An active object to make group method call.
     */
    public P2PService getActiveGroup() {
        return this.acquaintancesActived;
    }

    /**
     * Add a peer in the group of acquaintances Add only if not already present
     * and still some space left (NOA)
     * 
     * @param peer
     *            the peer to add.
     * @return add succesfull
     */
    public Vector<String> add(P2PService peer) {
        return this.add(buildCorrectUrl(PAActiveObject.getActiveObjectNodeUrl(peer)), peer);
    }

    /**
     * Add a peer in the group of acquaintances Add only if not already present
     * and still some space left (NOA)
     * 
     * @param peerUrl
     *            the url of the peer
     * @param peer
     *            the peer to add
     * @return add succesfull
     */
    public Vector<String> add(String peerUrl, P2PService peer) {
        boolean result = false;
        try {
            if (this.shouldBeAcquaintance(peer)) {
                if (!peerUrl.matches(".*cannot contact the body.*")) {
                    result = this.acquaintances.add(peer, peerUrl);
                    logger.info("Acquaintance " + peerUrl + " " + result + " added");
                }
                return null;
            }
        } catch (Exception e) {
            this.acquaintances.remove(peer, peerUrl);
            logger.debug("Problem when adding peer", e);
        }
        return this.getAcquaintancesURLs();
    }

    public void acqAccepted(String url, P2PService peer) {
        logger.info("P2PAcquaintanceManager.acqAccepted() got a reply from " + url);
        System.out.println("URL:" + url + " PEER:" + peer);
        // we remove it from the awaited answers
        // if we don't do so, it might be refused because of the NOA limit
        this.removeFromAwaited(url);
        this.add(url, peer);
        System.out.println("P2PAcquaintanceManager.acqAccepted() adding " + "--" + url + "--");
        this.preferedAcquaintancesURLs.add(url);
        Iterator it = this.preferedAcquaintancesURLs.iterator();
        while (it.hasNext()) {
            System.out.println("            " + it.next());
        }
    }

    public void acqRejected(String url, Vector<String> s) {
        logger.info("P2PAcquaintanceManager.acqRejected() " + url);
        // this.removeFromReply(url,s);
        this.removeFromAwaited(url);
        this.addToPreferedAcq(s);
        // we add it back
        this.preferedAcquaintancesURLs.add(url);
    }

    public void removeFromAwaited(String url) {
        String[] tmp = this.getAwaitedRepliesUrls();
        for (int i = 0; i < tmp.length; i++) {
            System.out.println("--" + tmp[i] + "--");
        }
        logger.info("Removing --" + url + "-- from awaited peers " + awaitedReplies.remove(url));
    }

    /**
     * Remove the peer from the current acquaintaces Add the acquaintancesURLs
     * to the prefered acquaintances
     * 
     * @param peer
     * @param acquaintancesURLs
     */
    public void remove(P2PService peer, Vector<String> acquaintancesURLs) {
        boolean result = this.acquaintances.remove(peer, buildCorrectUrl(PAActiveObject
                .getActiveObjectNodeUrl(peer)));
        if (acquaintancesURLs != null) {
            this.addToPreferedAcq(acquaintancesURLs);
        }

        // boolean result = this.acquaintances.remove(peer);
        if (result) {
            logger.info("Peer successfully removed");
        } else {
            logger.debug("Peer not removed");
        }
    }

    protected void dropRandomPeer() {
        try {
            // pick up a random peer in the list
            P2PService p = randomPeer();
            // logger.info(" I have decided to drop " + p.getAddress());
            this.remove(p, null);
            p.remove(this.localService, this.getAcquaintancesURLs());
        } catch (PeerDoesntExist e) {

        }
    }

    // public void dumpAcquaintances() {
    // acquaintances.dumpAcquaintances();
    // }
    public Vector<String> getAcquaintancesURLs() {
        return new Vector<String>(Arrays.asList(this.acquaintances.getAcquaintancesURLs()));
    }

    /**
     * Returns the number of elements in this group.
     * 
     * @return the number of elements in this group.
     */
    public IntMutableWrapper size() {
        return new IntMutableWrapper(this.acquaintances.size());
    }

    public int getEstimatedNumberOfAcquaintances() {
        return this.acquaintances.size() + awaitedReplies.size();
    }

    /**
     * Returns <tt>true</tt> if this collection contains the specified
     * element. More formally, returns <tt>true</tt> if and only if this
     * collection contains at least one element <tt>e</tt> such that
     * <tt>(o==null ? e==null : o.equals(e))</tt>.
     * 
     * @param service
     *            element whose presence in this collection is to be tested.
     * @return <tt>true</tt> if this collection contains the specified
     *         element.
     */
    public BooleanMutableWrapper contains(P2PService service) {
        return new BooleanMutableWrapper(this.acquaintances.contains(service));
    }

    /**
     * @return a random acquaintance reference.
     */
    // TODO: unsafe code
    public P2PService randomPeer() throws PeerDoesntExist {
        if (this.acquaintances.size() > 0) {
            int random = this.randomizer.nextInt(this.acquaintances.size());
            return this.acquaintances.get(random);
        } else
            throw new PeerDoesntExist();
    }

    /**
     * @return the list of current acquaintances.
     */
    public Vector getAcquaintanceList() {
        return new Vector(this.acquaintances.getAcquaintancesAsGroup());
    }

    public P2PService getAcquaintances() {
        return this.acquaintances.getAcquaintances();
    }

    // /**
    // * Calls the transmit() method of the message m
    // * @param m
    // */
    // public void transmit(Message m) {
    // // m.transmit(this.acquaintances.getAcquaintances());
    // m.transmit(localService);
    // }
    public int getMaxNOA() {
        return NOA;
    }

    public boolean shouldBeAcquaintance(P2PService remoteService) {
        if (this.contains(remoteService).booleanValue()) {
            logger.debug("The remote peer is already known");
            return false;
        }
        return acceptAnotherAcquaintance();
    }

    /**
     * Indicates wether or not a new acquaintance should be accepted This is
     * defined using a probability Always accept if 0 <=
     * getEstimatedNumberOfAcquaintances() < NOA Accept with probability P if
     * NOA <= getEstimatedNumberOfAcquaintances() < 2*NOA Reject otherwise The
     * probability is linear y = -1/NOA*estimatedNumberOfAcquaintances + 2
     * 
     * @return a boolean
     */
    protected boolean acceptAnotherAcquaintance() {
        if (this.getEstimatedNumberOfAcquaintances() < NOA) {
            logger.debug("NOA not reached: I should be an acquaintance");
            return true;
        }
        if (this.getEstimatedNumberOfAcquaintances() > (2 * NOA)) {
            logger.debug("2*NOA reached, I refuse the acquaintance");
            return false;
        }
        // we are in the grey area, only accept with some probability
        // first compute the probability according to the max number
        logger.info("estimatedNOA " + this.getEstimatedNumberOfAcquaintances());
        double prob = (-(1.0 / this.getMaxNOA()) * this.getEstimatedNumberOfAcquaintances()) + 2;
        logger.info("Probability to accept set to " + prob);
        return (randomizer.nextDouble() <= prob);
        // {
        // logger.debug("Accepted after probability check");
        // return true;
        // }
        //		
        //      
        // logger.debug("Random said: I should not be an acquaintance");
        // return false;
    }

    public void setPreferedAcq(Vector<String> v) {
        logger.debug("SET PREFFERED ACQUAINTANCE LIST");
        this.preferedAcquaintancesURLs = new HashSet<String>();
        Iterator it = v.iterator();
        while (it.hasNext()) {
            String p = buildCorrectUrl((String) it.next());
            System.out.println(p);
            this.preferedAcquaintancesURLs.add(p);
        }
    }

    /**
     * Add the given peer urls to the current prefered acquaintances list
     * 
     * @param v
     *            the list of acquaintances
     */
    public void addToPreferedAcq(Vector<String> v) {
        this.preferedAcquaintancesURLs.addAll(v);
    }

    public String[] getAwaitedRepliesUrls() {
        return this.awaitedReplies.keySet().toArray(new String[] {});
    }

    /**
     * Add the default name of the P2P Node to a specified <code>url</code>.
     * 
     * @param url
     *            the url.
     * @return the <code>url</code> with the name of the P2P Node.
     */
    private static String urlAdderP2PNodeName(String url) {
        if (url.indexOf(P2P_NODE_NAME) <= 0) {
            url += ("/" + P2P_NODE_NAME);
        }
        return url;
    }

    /**
     * Add rmi:// in front of all URLS
     * 
     * @param s
     * @return
     */
    private String buildCorrectUrl(String s) {
        if (s.indexOf("//") < 0) {
            s = "//" + s;
        }
        if (s.indexOf("rmi:") < 0) {
            s = "rmi:" + s;
        }
        if (s.indexOf(P2PConstants.P2P_NODE_NAME) < 0) {
            s = s + "/" + P2PConstants.P2P_NODE_NAME;
        }
        return s;
    }

    public void setMaxNOA(int noa) {
        logger.info("P2PAcquaintanceManager.setNOA() changing noa from " + NOA + " to " + noa);
        P2PAcquaintanceManager.NOA = noa;
    }

    // public void setMaxNOA(int noa) {
    // logger.info("P2PAcquaintanceManager.setMaxNOA() changing noa from " + NOA
    // + " to " + noa);
    // P2PAcquaintanceManager.MaxNOA = noa;
    // }
    public void dumpTables() {
        System.out.println("----- Prefered Acquaintances ---");
        Iterator it = preferedAcquaintancesURLs.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }

        System.out.println("---------------------------------------------");
        System.out.println("----- Awaited Replies ---" + this.awaitedReplies.size());
        Set<Map.Entry<String, DatedRequest>> map = awaitedReplies.entrySet();
        Iterator it2 = map.iterator();
        while (it2.hasNext()) {
            Map.Entry<String, DatedRequest> entry = (Map.Entry<String, DatedRequest>) it2.next();

            System.out.println(entry.getKey() + " requested at " + (entry.getValue()).getTime());
        }

        System.out.println("---------------------------------------------");
    }

    /**
     * A class to remember when an ACQ request has been issued
     * 
     * @author fhuet
     */
    private class DatedRequest {
        protected P2PService service;
        protected long time;

        DatedRequest(P2PService s, long t) {
            this.service = s;
            this.time = t;
        }

        public long getTime() {
            return this.time;
        }

        public P2PService getP2PService() {
            return service;
        }
    }
}
