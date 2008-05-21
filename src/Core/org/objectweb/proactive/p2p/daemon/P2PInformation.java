package org.objectweb.proactive.p2p.daemon;

import java.util.List;


public class P2PInformation {

    public List<String> peerList;

    public List<String> getPeerList() {
        return peerList;
    }

    public void setPeerList(List<String> peerList) {
        this.peerList = peerList;
    }

}
