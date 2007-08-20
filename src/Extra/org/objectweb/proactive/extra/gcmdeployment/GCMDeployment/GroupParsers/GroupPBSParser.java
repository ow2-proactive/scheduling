package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.core.process.pbs.PBSSubProcess;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.PathElement;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupPBS;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupPBSParser extends AbstractGroupParser {
    @Override
    public AbstractGroup createGroup() {
        return new GroupPBS();
    }

    public String getNodeName() {
        return "pbsProcess";
    }

    @Override
    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);

        GroupPBS pbsGroup = (GroupPBS) getGroup();

        String interactive = GCMParserHelper.getAttributeValue(groupNode,
                "interactive");
        if (interactive != null) {
            pbsGroup.setInteractive(interactive);
        }

        String queueName = GCMParserHelper.getAttributeValue(groupNode,
                "queueName");
        if (queueName != null) {
            pbsGroup.setQueueName(queueName);
        }

        Node optionNode;
        try {
            optionNode = (Node) xpath.evaluate("pbsOptions", groupNode,
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
                    pbsGroup.setHostList(nodeExpandedValue);
                } else if (nodeName.equals("hostsNumber")) {
                    pbsGroup.setHostsNumber(nodeExpandedValue);
                } else if (nodeName.equals("processorPerNode")) {
                    pbsGroup.setProcessorPerNodeNumber(nodeExpandedValue);
                } else if (nodeName.equals("bookingDuration")) {
                    pbsGroup.setBookingDuration(nodeExpandedValue);
                } else if (nodeName.equals("outputFile")) {
                    pbsGroup.setOutputFile(nodeExpandedValue);
                } else if (nodeName.equals("scriptPath")) {
                    PathElement path = GCMParserHelper.parsePathElementNode(childNode);
                    pbsGroup.setScriptLocation(path);
                }
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }
    }
}
