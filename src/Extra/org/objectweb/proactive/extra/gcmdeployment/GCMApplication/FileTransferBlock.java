package org.objectweb.proactive.extra.gcmdeployment.GCMApplication;

public class FileTransferBlock {
    protected String source;
    protected String destination;

    protected String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    protected String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
