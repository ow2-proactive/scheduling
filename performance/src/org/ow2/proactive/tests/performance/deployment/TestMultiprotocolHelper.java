package org.ow2.proactive.tests.performance.deployment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;


public class TestMultiprotocolHelper extends TestProtocolHelper {

    private TestProtocolHelper baseProtocol;

    private List<TestProtocolHelper> additionalProtocols = new ArrayList<TestProtocolHelper>();

    private String additionalProtocolsProperty;

    private String protocolsOrder;

    public TestMultiprotocolHelper(HostTestEnv serverHostEnv, TestProtocolHelper baseProtocol,
            List<TestProtocolHelper> additionalProtocols, String protocolsOrder) {
        super(serverHostEnv, baseProtocol.getProtocolName());

        this.baseProtocol = baseProtocol;
        this.protocolsOrder = protocolsOrder;

        this.additionalProtocols = additionalProtocols;

        StringBuilder protocolsBuilder = null;
        for (TestProtocolHelper protocol : additionalProtocols) {
            if (protocolsBuilder == null) {
                protocolsBuilder = new StringBuilder();
            } else {
                protocolsBuilder.append(',');
            }
            protocolsBuilder.append(protocol.getProtocolName());
        }
        additionalProtocolsProperty = protocolsBuilder.toString();
    }

    @Override
    public String prepareForDeployment() throws Exception {
        for (TestProtocolHelper protocol : additionalProtocols) {
            protocol.prepareForDeployment();
        }

        return baseProtocol.prepareForDeployment();
    }

    @Override
    public Map<String, String> getClientProActiveProperties() {
        Map<String, String> properties = new HashMap<String, String>();

        for (TestProtocolHelper protocol : additionalProtocols) {
            properties.putAll(protocol.getClientProActiveProperties());
        }
        properties.putAll(baseProtocol.getClientProActiveProperties());
        properties.put(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getName(), baseProtocol
                .getProtocolName());

        properties.put(CentralPAPropertyRepository.PA_COMMUNICATION_ADDITIONAL_PROTOCOLS.getName(),
                additionalProtocolsProperty);
        properties
                .put(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOLS_ORDER.getName(), protocolsOrder);

        return properties;
    }

    @Override
    public List<String> getAdditionalServerJavaOptions() {
        List<String> options = new ArrayList<String>();

        for (TestProtocolHelper protocol : additionalProtocols) {
            options.addAll(protocol.getAdditionalServerJavaOptions());
        }
        options.addAll(baseProtocol.getAdditionalServerJavaOptions());

        for (Iterator<String> i = options.iterator(); i.hasNext();) {
            String option = i.next();
            if (option.startsWith(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getCmdLine())) {
                i.remove();
            }
        }

        options.add(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getCmdLine() +
            baseProtocol.getProtocolName());
        options.add(CentralPAPropertyRepository.PA_COMMUNICATION_ADDITIONAL_PROTOCOLS.getCmdLine() +
            additionalProtocolsProperty);
        options.add(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOLS_ORDER.getCmdLine() +
            protocolsOrder);

        return options;
    }

}
