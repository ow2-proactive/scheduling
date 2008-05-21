package org.objectweb.proactive.p2p.daemon;

public class DaemonState {

    Priorities priority;
    Services services;

    public Priorities getPriority() {
        return priority;
    }

    public void setPriority(Priorities priority) {
        this.priority = priority;
    }

    public Services getServices() {
        return services;
    }

    public void setServices(Services services) {
        this.services = services;
    }

}
