package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.BridgeParsers;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.bridge.AbstractBridge;
import org.objectweb.proactive.extra.gcmdeployment.process.bridge.BridgeRSH;
import org.w3c.dom.Node;


public class BridgeRSHParser extends AbstractBridgeParser {
    private static final String ATTR_USERNAME = "username";
    private static final String ATTR_HOSTNAME = "hostname";
    static final String NODE_NAME = "rshBridge";

    @Override
    public void parseBridgeNode(Node bridgeNode, XPath xpath) {
        super.parseBridgeNode(bridgeNode, xpath);

        String hostname = GCMParserHelper.getAttributeValue(bridgeNode,
                ATTR_HOSTNAME);
        String username = GCMParserHelper.getAttributeValue(bridgeNode,
                ATTR_USERNAME);

        BridgeRSH bridgeRSH = ((BridgeRSH) bridge);
        bridgeRSH.setHostname(hostname);
        bridgeRSH.setUsername(username);
    }

    @Override
    public AbstractBridge createBridge() {
        return new BridgeRSH();
    }

    public String getNodeName() {
        return NODE_NAME;
    }
}
