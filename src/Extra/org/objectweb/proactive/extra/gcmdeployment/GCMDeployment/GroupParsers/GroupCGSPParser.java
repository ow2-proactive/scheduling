package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupCGSP;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupCGSPParser extends AbstractGroupParser {
    private static final String NODE_NAME_STDERR = "stderr";
    private static final String NODE_NAME_STDOUT = "stdout";
    private static final String NODE_NAME_DIRECTORY = "directory";
    private static final String NODE_NAME_COUNT = "count";
    private static final String ATTR_QUEUE = "queue";
    private static final String ATTR_HOSTNAME = "hostname";
    private static final String NODE_NAME = "cgspGroup";

    @Override
    public AbstractGroup createGroup() {
        return new GroupCGSP();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);
        GroupCGSP cgspGroup = (GroupCGSP) getGroup();

        String hostname = GCMParserHelper.getAttributeValue(groupNode,
                ATTR_HOSTNAME);
        cgspGroup.setHostName(hostname);

        String queue = GCMParserHelper.getAttributeValue(groupNode, ATTR_QUEUE);
        cgspGroup.setQueue(queue);

        groupNode.getChildNodes();

        NodeList childNodes = groupNode.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); ++j) {
            Node child = childNodes.item(j);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String nodeName = child.getNodeName();
            String nodeValue = GCMParserHelper.getElementValue(child);

            if (nodeName.equals(NODE_NAME_COUNT)) {
                cgspGroup.setCount(nodeValue);
            } else if (nodeName.equals(NODE_NAME_DIRECTORY)) {
                cgspGroup.setDirectory(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDOUT)) {
                cgspGroup.setStdout(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDERR)) {
                cgspGroup.setStderr(nodeValue);
            }
        }
    }
}
