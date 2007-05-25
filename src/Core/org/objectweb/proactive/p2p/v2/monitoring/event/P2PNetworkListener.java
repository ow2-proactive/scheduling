package org.objectweb.proactive.p2p.v2.monitoring.event;

import org.objectweb.proactive.p2p.v2.monitoring.Link;
import org.objectweb.proactive.p2p.v2.monitoring.P2PNode;


/**
 * Should be implemented by classes interested in
 * monitoring a P2P Network
 * @author fhuet
 */
public interface P2PNetworkListener {
    public void newPeer(P2PNode node);

    public void newLink(Link link);
}
