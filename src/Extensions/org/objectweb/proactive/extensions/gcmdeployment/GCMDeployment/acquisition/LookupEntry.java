package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.acquisition;

public class LookupEntry {

    private String protocol;
    private String hostList;
    private int port;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHostList() {
        return hostList;
    }

    public void setHostList(String hostList) {
        this.hostList = hostList;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
