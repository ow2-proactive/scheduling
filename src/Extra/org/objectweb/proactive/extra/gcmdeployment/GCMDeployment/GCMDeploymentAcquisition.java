package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment;

import java.util.List;

import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.acquisition.LookupEntry;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.acquisition.P2PEntry;


public class GCMDeploymentAcquisition {

    private List<LookupEntry> lookupEntries;

    private List<P2PEntry> p2pEntries;

    public List<LookupEntry> getLookupEntries() {
        return lookupEntries;
    }

    public void setLookupEntries(List<LookupEntry> lookupEntries) {
        this.lookupEntries = lookupEntries;
    }

    public List<P2PEntry> getP2pEntries() {
        return p2pEntries;
    }

    public void setP2pEntries(List<P2PEntry> entries) {
        p2pEntries = entries;
    }

}
