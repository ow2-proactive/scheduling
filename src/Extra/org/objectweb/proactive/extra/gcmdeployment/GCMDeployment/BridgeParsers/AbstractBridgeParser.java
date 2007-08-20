package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.BridgeParsers;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.Bridge;
import org.objectweb.proactive.extra.gcmdeployment.process.bridge.AbstractBridge;
import org.w3c.dom.Node;


public abstract class AbstractBridgeParser implements BridgeParser {
    static final String ATT_ID = "id";
    static final String ATT_HOSTNAME = "hostname";
    static final String ATT_USERNAME = "username";
    static final String ATT_COMMANDPATH = "commandPath";
    protected AbstractBridge bridge;

    public AbstractBridgeParser() {
        bridge = createBridge();
    }

    public void parseBridgeNode(Node bridgeNode, XPath xpath) {
        String value;

        // Mandatory fields
        value = GCMParserHelper.getAttributeValue(bridgeNode, ATT_ID);
        bridge.setId(value);
        value = GCMParserHelper.getAttributeValue(bridgeNode, ATT_HOSTNAME);
        bridge.setHostname(value);

        // Optional fields
        value = GCMParserHelper.getAttributeValue(bridgeNode, ATT_USERNAME);
        if (value != null) {
            bridge.setUsername(value);
        }
        value = GCMParserHelper.getAttributeValue(bridgeNode, ATT_COMMANDPATH);
        if (value != null) {
            bridge.setCommandPath(value);
        }
    }

    public Bridge getBridge() {
        return bridge;
    }

    public abstract AbstractBridge createBridge();
}
