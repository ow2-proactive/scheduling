package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.PathElement;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupOAR;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupOARParser extends AbstractGroupParser {
    @Override
    public AbstractGroup createGroup() {
        return new GroupOAR();
    }

    public String getNodeName() {
        return "oarProcess";
    }

    @Override
    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);

        GroupOAR oarGroup = (GroupOAR) getGroup();

        String interactive = GCMParserHelper.getAttributeValue(groupNode,
                "interactive");

        if (interactive != null) {
            oarGroup.setInteractive(interactive);
        }

        String queueName = GCMParserHelper.getAttributeValue(groupNode, "queue");

        if (queueName != null) {
            oarGroup.setQueueName(queueName);
        }

        String accessProtocol = GCMParserHelper.getAttributeValue(groupNode,
                "bookedNodesAccess");

        if (accessProtocol != null) {
            oarGroup.setAccessProtocol(accessProtocol);
        }

        //
        // Parse options
        //
        try {
            Node optionNode = (Node) xpath.evaluate("oarOption", groupNode,
                    XPathConstants.NODE);

            NodeList childNodes = optionNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); ++i) {
                Node childNode = childNodes.item(i);
                if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeName = childNode.getNodeName();
                if (nodeName.equals("resources")) {
                    oarGroup.setResources(GCMParserHelper.getElementValue(
                            childNode));
                } else if (nodeName.equals("scriptPath")) {
                    PathElement path = GCMParserHelper.parsePathElementNode(childNode);
                    oarGroup.setScriptLocation(path);
                }
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }
    }
}
