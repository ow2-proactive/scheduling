package org.ow2.proactive.tests.performance.deployment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.amqp.AMQPConfig;
import org.ow2.proactive.tests.performance.utils.TestUtils;


public class TestAmqpProtocolHelper extends TestProtocolHelper {

    private String host;

    private Integer port;

    public static TestAmqpProtocolHelper createUsingSystemProperties(HostTestEnv serverHostEnv) {
        String host = TestUtils.getRequiredProperty("test.deploy.amqp.host");
        Integer port = Integer.parseInt(TestUtils.getRequiredProperty("test.deploy.amqp.port"));
        return new TestAmqpProtocolHelper(serverHostEnv, host, port);
    }

    public TestAmqpProtocolHelper(HostTestEnv serverHostEnv, String host, Integer port) {
        super(serverHostEnv, "amqp");
        this.host = host;
        this.port = port;
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
        return properties;
    }

    @Override
    public List<String> getAdditionalServerJavaOptions() {
        List<String> options = new ArrayList<String>();
        options.add(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getCmdLine() + protocolName);
        options.add(AMQPConfig.PA_AMQP_BROKER_ADDRESS.getCmdLine() + host);
        options.add(AMQPConfig.PA_AMQP_BROKER_PORT.getCmdLine() + port.intValue());
        return options;
    }

}
