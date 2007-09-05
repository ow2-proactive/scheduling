package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupPrun;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupPrunParser extends AbstractGroupParser {
    @Override
    public AbstractGroup createGroup() {
        return new GroupPrun();
    }

    public String getNodeName() {
        return "prunProcess";
    }

    @Override
    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);

        GroupPrun prunGroup = (GroupPrun) getGroup();

        String queueName = GCMParserHelper.getAttributeValue(groupNode, "queue");
        prunGroup.setQueueName(queueName);

        try {
            Node optionNode = (Node) xpath.evaluate("prunOption", groupNode,
                    XPathConstants.NODE);

            NodeList childNodes = optionNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); ++i) {
                Node childNode = childNodes.item(i);
                if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeName = childNode.getNodeName();
                String nodeExpandedValue = GCMParserHelper.getElementValue(childNode);
                if (nodeName.equals("hostlist")) {
                    prunGroup.setHostList(nodeExpandedValue);
                } else if (nodeName.equals("hostsNumber")) {
                    prunGroup.setHostsNumber(nodeExpandedValue);
                } else if (nodeName.equals("processorPerNode")) {
                    prunGroup.setProcessorPerNodeNumber(nodeExpandedValue);
                } else if (nodeName.equals("bookingDuration")) {
                    prunGroup.setBookingDuration(nodeExpandedValue);
                } else if (nodeName.equals("outputFile")) {
                    prunGroup.setOutputFile(nodeExpandedValue);
                }
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }
    }
}
