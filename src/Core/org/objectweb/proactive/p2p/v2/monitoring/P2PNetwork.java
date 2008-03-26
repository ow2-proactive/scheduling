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
package org.objectweb.proactive.p2p.v2.monitoring;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.objectweb.proactive.p2p.v2.monitoring.event.P2PNetworkListener;


/**
 * An abstraction of a P2P Network useful for monitoring
 * Manage a list of P2PNetworkListener
 * @author The ProActive Team
 */
public class P2PNetwork {
    protected ArrayList<P2PNetworkListener> listeners = new ArrayList<P2PNetworkListener>();
    protected HashMap<String, P2PNode> senders = new HashMap<String, P2PNode>();
    protected HashMap<String, Link> links = new HashMap<String, Link>();
    protected String name;
    protected Date date;
    private int index;

    public P2PNetwork() {
    }

    public P2PNetwork(String n) {
        this.name = n;
    }

    public P2PNetwork(String n, Date d) {
        this(n);
        this.date = d;
    }

    protected void addAsSender(P2PNode p) {
        this.notifyListenersNewPeer(p);
        senders.put(p.getName(), p);
    }

    protected void addAsSender(AcquaintanceInfo i) {
        String s = i.getSender();
        P2PNode tmp = senders.get(s);
        if ((tmp == null) || (tmp.getIndex() == -1)) {
            this.addAsSender(new P2PNode(s, index++, i.getCurrentNoa(), i.getNoa()));
        } else {
            if (tmp != null) {
                tmp.setMaxNOA(i.getNoa());
                tmp.setNoa(i.getCurrentNoa());
            }
        }
    }

    public void addAsSender(String s) {
        if (senders.get(s) == null) {
            this.addAsSender(new P2PNode(s));
        }
    }

    public void addAsSender(String s, int noa, int maxNoa) {
        P2PNode p = senders.get(s);
        if (p == null) {
            p = new P2PNode(s);
            p.setNoa(noa);
            p.setMaxNOA(maxNoa);
            this.addAsSender(p);
        } else {
            p.setNoa(noa);
            p.setMaxNOA(maxNoa);
        }
    }

    /**
     * our links are considered bi-directional
     * ie a->b and b->a will be a a<->b link
     * so we switch source/destination based on lexical order
     * @param source
     * @param dest
     */
    public void addLink(String source, String dest) {
        //System.out.println(source + " <--> " + dest);
        Link l = null;
        Link previousValue = null;
        if (source.compareTo(dest) <= 0) {
            l = new Link(source, dest);
            previousValue = links.put(source + dest, l);
        } else {
            l = new Link(dest, source);
            previousValue = links.put(dest + source, l);
        }
        if (previousValue == null) {
            this.notifyListenersNewLink(l);
        }
    }

    public HashMap<String, Link> getLinks() {
        return this.links;
    }

    public HashMap<String, P2PNode> getSenders() {
        return this.senders;
    }

    public void addListener(P2PNetworkListener l) {
        listeners.add(l);
    }

    protected void notifyListenersNewPeer(P2PNode node) {
        Iterator it = listeners.iterator();

        // System.out.println("P2PNetwork.notifyListenersNewPeer() " + node);
        while (it.hasNext()) {
            System.out.println("            P2PNetwork.notifyListenersNewPeer() ");
            P2PNetworkListener element = (P2PNetworkListener) it.next();
            element.newPeer(node);
        }
    }

    protected void notifyListenersNewLink(Link l) {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            P2PNetworkListener element = (P2PNetworkListener) it.next();
            element.newLink(l);
        }
    }
}
