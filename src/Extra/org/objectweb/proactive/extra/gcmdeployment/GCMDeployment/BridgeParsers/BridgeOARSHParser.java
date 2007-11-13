package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.BridgeParsers;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.bridge.AbstractBridge;
import org.objectweb.proactive.extra.gcmdeployment.process.bridge.BridgeOARSH;
import org.w3c.dom.Node;


public class BridgeOARSHParser extends BridgeSSHParser {
    private static final String ATTR_JOB_ID = "jobId";
    static final String NODE_NAME = "oarshBridge";

    @Override
    public AbstractBridge parseBridgeNode(Node bridgeNode, XPath xpath) {
        BridgeOARSH bridge = (BridgeOARSH) super.parseBridgeNode(bridgeNode,
                xpath);

        String jobId = GCMParserHelper.getAttributeValue(bridgeNode, ATTR_JOB_ID);

        bridge.setJobId(jobId);

        return bridge;
    }

    @Override
    public AbstractBridge createBridge() {
        return new BridgeOARSH();
    }

    public String getBaseNodeName() {
        return NODE_NAME;
    }
}
