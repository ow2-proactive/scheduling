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
    private static final String NODE_NAME_SCRIPT_PATH = "scriptPath";
    private static final String NODE_NAME_RESOURCES = "resources";
    private static final String XPATH_OAR_OPTION = "oarOption";
    private static final String ATTR_BOOKED_NODES_ACCESS = "bookedNodesAccess";
    private static final String ATTR_QUEUE = "queue";
    private static final String ATTR_INTERACTIVE = "interactive";
    private static final String NODE_NAME = "oarProcess";

    @Override
    public AbstractGroup createGroup() {
        return new GroupOAR();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);

        GroupOAR oarGroup = (GroupOAR) getGroup();

        String interactive = GCMParserHelper.getAttributeValue(groupNode,
                ATTR_INTERACTIVE);

        if (interactive != null) {
            oarGroup.setInteractive(interactive);
        }

        String queueName = GCMParserHelper.getAttributeValue(groupNode,
                ATTR_QUEUE);

        if (queueName != null) {
            oarGroup.setQueueName(queueName);
        }

        String accessProtocol = GCMParserHelper.getAttributeValue(groupNode,
                ATTR_BOOKED_NODES_ACCESS);

        if (accessProtocol != null) {
            oarGroup.setAccessProtocol(accessProtocol);
        }

        //
        // Parse options
        //
        try {
            Node optionNode = (Node) xpath.evaluate(XPATH_OAR_OPTION,
                    groupNode, XPathConstants.NODE);

            NodeList childNodes = optionNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); ++i) {
                Node childNode = childNodes.item(i);
                if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeName = childNode.getNodeName();
                if (nodeName.equals(NODE_NAME_RESOURCES)) {
                    oarGroup.setResources(GCMParserHelper.getElementValue(
                            childNode));
                } else if (nodeName.equals(NODE_NAME_SCRIPT_PATH)) {
                    PathElement path = GCMParserHelper.parsePathElementNode(childNode);
                    oarGroup.setScriptLocation(path);
                }
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }
    }
}
