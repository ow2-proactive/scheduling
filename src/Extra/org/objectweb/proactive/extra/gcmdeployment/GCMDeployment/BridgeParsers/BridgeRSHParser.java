package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.BridgeParsers;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.bridge.AbstractBridge;
import org.objectweb.proactive.extra.gcmdeployment.process.bridge.BridgeRSH;
import org.w3c.dom.Node;


public class BridgeRSHParser extends AbstractBridgeParser {
    static final String NODE_NAME = "rshBridge";

    @Override
    public void parseBridgeNode(Node bridgeNode, XPath xpath) {
        super.parseBridgeNode(bridgeNode, xpath);

        String hostname = GCMParserHelper.getAttributeValue(bridgeNode,
                "hostname");
        String username = GCMParserHelper.getAttributeValue(bridgeNode,
                "username");

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
