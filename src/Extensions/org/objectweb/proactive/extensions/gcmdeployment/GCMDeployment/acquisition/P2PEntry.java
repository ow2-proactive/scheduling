package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.acquisition;

import java.util.ArrayList;
import java.util.List;


public class P2PEntry {

    private LocalClientEntry localClient;
    private List<String> hostsList;
    private int nodesToAsk;

    public P2PEntry() {
        this.localClient = new LocalClientEntry();
        this.hostsList = new ArrayList<String>();
        this.nodesToAsk = 0;
    }

    public LocalClientEntry getLocalClient() {
        return localClient;
    }

    public void setLocalClient(LocalClientEntry localClient) {
        this.localClient = localClient;
    }

    public List<String> getHostsList() {
        return hostsList;
    }

    public void setHostsList(List<String> hostsList) {
        this.hostsList = hostsList;
    }

    public int getNodesToAsk() {
        return nodesToAsk;
    }

    public void setNodesToAsk(int nodesToAsk) {
        this.nodesToAsk = nodesToAsk;
    }

}
