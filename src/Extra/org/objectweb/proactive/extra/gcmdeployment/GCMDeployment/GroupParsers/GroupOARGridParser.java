package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.core.process.oar.OARGRIDSubProcess;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.PathElement;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupOARGrid;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupOARGridParser extends AbstractGroupParser {
    @Override
    public AbstractGroup createGroup() {
        return new GroupOARGrid();
    }

    public String getNodeName() {
        return "oarGridProcess";
    }

    @Override
    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);

        String queueName = GCMParserHelper.getAttributeValue(groupNode, "queue");

        GroupOARGrid oarGridSubProcess = (GroupOARGrid) getGroup();

        if (queueName != null) {
            oarGridSubProcess.setQueueName(queueName);
        }

        String accessProtocol = GCMParserHelper.getAttributeValue(groupNode,
                "bookedNodesAccess");

        if (accessProtocol != null) {
            oarGridSubProcess.setAccessProtocol(accessProtocol);
        }

        try {
            Node optionNode = (Node) xpath.evaluate("oarGridOption", groupNode,
                    XPathConstants.NODE);

            NodeList childNodes = optionNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); ++i) {
                Node childNode = childNodes.item(i);
                if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeName = childNode.getNodeName();
                String nodeExpandedValue = GCMParserHelper.getElementValue(childNode);
                if (nodeName.equals("resources")) {
                    oarGridSubProcess.setResources(nodeExpandedValue);
                } else if (nodeName.equals("walltime")) {
                    oarGridSubProcess.setWallTime(nodeExpandedValue);
                } else if (nodeName.equals("scriptPath")) {
                    PathElement path = GCMParserHelper.parsePathElementNode(childNode);
                    oarGridSubProcess.setScriptLocation(path);
                }
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }
    }
}
