package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.acquisition.LookupEntry;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.acquisition.P2PEntry;


public class GCMDeploymentAcquisition {

    private List<LookupEntry> lookupEntries = new ArrayList<LookupEntry>();

    private List<P2PEntry> p2pEntries = new ArrayList<P2PEntry>();

    public List<LookupEntry> getLookupEntries() {
        return lookupEntries;
    }

    public void setLookupEntries(List<LookupEntry> lookupEntries) {
        this.lookupEntries = lookupEntries;
    }

    public List<P2PEntry> getP2PEntries() {
        return p2pEntries;
    }

    public void setP2pEntries(List<P2PEntry> entries) {
        p2pEntries = entries;
    }

}
