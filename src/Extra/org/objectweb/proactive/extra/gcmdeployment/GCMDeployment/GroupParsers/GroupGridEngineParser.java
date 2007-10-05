package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.PathElement;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupGridEngine;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupGridEngineParser extends AbstractGroupParser {
    private static final String NODE_NAME_SCRIPT_PATH = "scriptPath";
    private static final String NODE_NAME_BOOKING_DURATION = "bookingDuration";
    private static final String NODE_NAME_PARALLEL_ENVIRONMENT = "parallelEnvironment";
    private static final String NODE_NAME_HOSTS_NUMBER = "hostsNumber";
    private static final String XPATH_GRID_ENGINE_OPTION = "gridEngineOption";
    private static final String ATTR_QUEUE = "queue";
    private static final String NODE_NAME = "gridEngineGroup";

    @Override
    public AbstractGroup createGroup() {
        return new GroupGridEngine();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);

        GroupGridEngine gridGroup = (GroupGridEngine) getGroup();

        String queueName = GCMParserHelper.getAttributeValue(groupNode,
                ATTR_QUEUE);
        gridGroup.setQueueName(queueName);

        try {
            Node optionNode = (Node) xpath.evaluate(XPATH_GRID_ENGINE_OPTION,
                    groupNode, XPathConstants.NODE);

            NodeList childNodes = optionNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); ++i) {
                Node childNode = childNodes.item(i);
                if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeName = childNode.getNodeName();
                String nodeExpandedValue = GCMParserHelper.getElementValue(childNode);
                if (nodeName.equals(NODE_NAME_HOSTS_NUMBER)) {
                    gridGroup.setHostsNumber(nodeExpandedValue);
                } else if (nodeName.equals(NODE_NAME_PARALLEL_ENVIRONMENT)) {
                    gridGroup.setParallelEnvironment(nodeExpandedValue);
                } else if (nodeName.equals(NODE_NAME_BOOKING_DURATION)) {
                    gridGroup.setBookingDuration(nodeExpandedValue);
                    //                    } else if (nodeName.equals(OUTPUT_FILE)) {
                    //                        gridGroup.setOutputFile(nodeExpandedValue);
                } else if (nodeName.equals(NODE_NAME_SCRIPT_PATH)) {
                    PathElement path = GCMParserHelper.parsePathElementNode(childNode);
                    gridGroup.setScriptLocation(path);
                }
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }
    }
}
