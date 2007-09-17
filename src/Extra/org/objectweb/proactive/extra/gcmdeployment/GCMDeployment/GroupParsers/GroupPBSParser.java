package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.PathElement;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupPBS;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupPBSParser extends AbstractGroupParser {
    private static final String NODE_NAME_SCRIPT_PATH = "scriptPath";
    private static final String NODE_NAME_OUTPUT_FILE = "outputFile";
    private static final String NODE_NAME_BOOKING_DURATION = "bookingDuration";
    private static final String NODE_NAME_PROCESSOR_PER_NODE = "processorPerNode";
    private static final String NODE_NAME_HOSTS_NUMBER = "hostsNumber";
    private static final String NODE_NAME_HOSTLIST = "hostlist";
    private static final String XPATH_PBS_OPTIONS = "pbsOptions";
    private static final String ATTR_QUEUE_NAME = "queueName";
    private static final String ATTR_INTERACTIVE = "interactive";
    private static final String NODE_NAME = "pbsProcess";

    @Override
    public AbstractGroup createGroup() {
        return new GroupPBS();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);

        GroupPBS pbsGroup = (GroupPBS) getGroup();

        String interactive = GCMParserHelper.getAttributeValue(groupNode,
                ATTR_INTERACTIVE);
        if (interactive != null) {
            pbsGroup.setInteractive(interactive);
        }

        String queueName = GCMParserHelper.getAttributeValue(groupNode,
                ATTR_QUEUE_NAME);
        if (queueName != null) {
            pbsGroup.setQueueName(queueName);
        }

        Node optionNode;
        try {
            optionNode = (Node) xpath.evaluate(XPATH_PBS_OPTIONS, groupNode,
                    XPathConstants.NODE);

            NodeList childNodes = optionNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); ++i) {
                Node childNode = childNodes.item(i);
                if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeName = childNode.getNodeName();
                String nodeExpandedValue = GCMParserHelper.getElementValue(childNode);
                if (nodeName.equals(NODE_NAME_HOSTLIST)) {
                    pbsGroup.setHostList(nodeExpandedValue);
                } else if (nodeName.equals(NODE_NAME_HOSTS_NUMBER)) {
                    pbsGroup.setHostsNumber(nodeExpandedValue);
                } else if (nodeName.equals(NODE_NAME_PROCESSOR_PER_NODE)) {
                    pbsGroup.setProcessorPerNodeNumber(nodeExpandedValue);
                } else if (nodeName.equals(NODE_NAME_BOOKING_DURATION)) {
                    pbsGroup.setBookingDuration(nodeExpandedValue);
                } else if (nodeName.equals(NODE_NAME_OUTPUT_FILE)) {
                    pbsGroup.setOutputFile(nodeExpandedValue);
                } else if (nodeName.equals(NODE_NAME_SCRIPT_PATH)) {
                    PathElement path = GCMParserHelper.parsePathElementNode(childNode);
                    pbsGroup.setScriptLocation(path);
                }
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }
    }
}
