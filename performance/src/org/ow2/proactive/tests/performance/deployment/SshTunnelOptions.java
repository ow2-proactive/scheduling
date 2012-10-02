package org.ow2.proactive.tests.performance.deployment;

public class SshTunnelOptions {

    private final String keyDirectory;
    private final String knownHosts;
    private final String username;
    private final String port;

    public SshTunnelOptions(String keyDirectory, String knownHosts, String username, String port) {
        this.keyDirectory = keyDirectory;
        this.knownHosts = knownHosts;
        this.username = username;
        this.port = port;
    }

    public String getKeyDirectory() {
        return keyDirectory;
    }

    public String getKnownHosts() {
        return knownHosts;
    }

    public String getUsername() {
        return username;
    }

    public String getPort() {
        return port;
    }

}
