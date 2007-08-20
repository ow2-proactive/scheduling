package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.BridgeParsers;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extra.gcmdeployment.process.Bridge;
import org.w3c.dom.Node;


public interface BridgeParser {
    public void parseBridgeNode(Node bridgeNode, XPath xpath);

    public Bridge getBridge();

    /**
     * Returns the nodeName associated to a particular parser
     * @return the nodeName as a String
     */
    public String getNodeName();
}
