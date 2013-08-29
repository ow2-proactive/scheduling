package org.ow2.proactive.tests.performance.deployment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.amqp.AMQPConfig;
import org.ow2.proactive.tests.performance.utils.TestUtils;


public class TestAmqpProtocolHelper extends TestProtocolHelper {

    private final String host;

    private final Integer port;

    private final String username;

    private final String password;

    private final String vhost;

    private final String socketFactory;

    private final SshTunnelOptions sshOptions;

    public static TestAmqpProtocolHelper createUsingSystemProperties(HostTestEnv serverHostEnv) {
        String host = TestUtils.getRequiredProperty("test.deploy.amqp.host");
        Integer port = Integer.parseInt(TestUtils.getRequiredProperty("test.deploy.amqp.port"));
        String username = System.getProperty("test.deploy.amqp.user");
        String password = System.getProperty("test.deploy.amqp.password");
        String vhost = System.getProperty("test.deploy.amqp.vhost");
        String socketFactory = System.getProperty("test.deploy.amqp.socketfactory");
        SshTunnelOptions sshOptions = new SshTunnelOptions(System
                .getProperty("test.deploy.amqp.ssh.key_directory"), System
                .getProperty("test.deploy.amqp.ssh.known_hosts"), System
                .getProperty("test.deploy.amqp.ssh.username"), System
                .getProperty("test.deploy.amqp.ssh.port"));
        return new TestAmqpProtocolHelper(serverHostEnv, host, port, username, password, vhost,
            socketFactory, sshOptions);
    }

    public TestAmqpProtocolHelper(HostTestEnv serverHostEnv, String host, Integer port, String username,
            String password, String vhost, String socketFactory, SshTunnelOptions sshOptions) {
        super(serverHostEnv, "amqp");
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.vhost = vhost;
        this.socketFactory = socketFactory;
        this.sshOptions = sshOptions;
    }

    @Override
    public String prepareForDeployment() throws Exception {
        String url = String.format("amqp://%s:%d/", host, port.intValue());
        return url;
    }

    @Override
    public Map<String, String> getClientProActiveProperties() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getName(), protocolName);
        properties.put(AMQPConfig.PA_AMQP_BROKER_ADDRESS.getName(), host);
        properties.put(AMQPConfig.PA_AMQP_BROKER_PORT.getName(), String.valueOf(port.intValue()));
        if (notEmpty(username)) {
            properties.put(AMQPConfig.PA_AMQP_BROKER_USER.getName(), username);
        }
        if (notEmpty(password)) {
            properties.put(AMQPConfig.PA_AMQP_BROKER_PASSWORD.getName(), password);
        }
        if (notEmpty(vhost)) {
            properties.put(AMQPConfig.PA_AMQP_BROKER_VHOST.getName(), vhost);
        }
        if (notEmpty(socketFactory)) {
            properties.put(AMQPConfig.PA_AMQP_SOCKET_FACTORY.getName(), socketFactory);
        }
        if (notEmpty(sshOptions.getKeyDirectory())) {
            properties.put(AMQPConfig.PA_AMQP_SSH_KEY_DIR.getName(), sshOptions.getKeyDirectory());
        }
        if (notEmpty(sshOptions.getKnownHosts())) {
            properties.put(AMQPConfig.PA_AMQP_SSH_KNOWN_HOSTS.getName(), sshOptions.getKnownHosts());
        }
        if (notEmpty(sshOptions.getPort())) {
            properties.put(AMQPConfig.PA_AMQP_SSH_REMOTE_PORT.getName(), sshOptions.getPort());
        }
        if (notEmpty(sshOptions.getUsername())) {
            properties.put(AMQPConfig.PA_AMQP_SSH_REMOTE_USERNAME.getName(), sshOptions.getUsername());
        }
        return properties;
    }

    @Override
    public List<String> getAdditionalServerJavaOptions() {
        List<String> options = new ArrayList<String>();
        options.add(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getCmdLine() + protocolName);
        options.add(AMQPConfig.PA_AMQP_BROKER_ADDRESS.getCmdLine() + host);
        options.add(AMQPConfig.PA_AMQP_BROKER_PORT.getCmdLine() + port.intValue());
        if (notEmpty(username)) {
            options.add(AMQPConfig.PA_AMQP_BROKER_USER.getCmdLine() + username);
        }
        if (notEmpty(password)) {
            options.add(AMQPConfig.PA_AMQP_BROKER_PASSWORD.getCmdLine() + password);
        }
        if (notEmpty(vhost)) {
            options.add(AMQPConfig.PA_AMQP_BROKER_VHOST.getCmdLine() + vhost);
        }
        if (notEmpty(socketFactory)) {
            options.add(AMQPConfig.PA_AMQP_SOCKET_FACTORY.getCmdLine() + socketFactory);
        }
        if (notEmpty(sshOptions.getKeyDirectory())) {
            options.add(AMQPConfig.PA_AMQP_SSH_KEY_DIR.getCmdLine() + sshOptions.getKeyDirectory());
        }
        if (notEmpty(sshOptions.getKnownHosts())) {
            options.add(AMQPConfig.PA_AMQP_SSH_KNOWN_HOSTS.getCmdLine() + sshOptions.getKnownHosts());
        }
        if (notEmpty(sshOptions.getPort())) {
            options.add(AMQPConfig.PA_AMQP_SSH_REMOTE_PORT.getCmdLine() + sshOptions.getPort());
        }
        if (notEmpty(sshOptions.getUsername())) {
            options.add(AMQPConfig.PA_AMQP_SSH_REMOTE_USERNAME.getCmdLine() + sshOptions.getUsername());
        }
        return options;
    }

    private boolean notEmpty(String str) {
        return str != null && !str.isEmpty();
    }

}
