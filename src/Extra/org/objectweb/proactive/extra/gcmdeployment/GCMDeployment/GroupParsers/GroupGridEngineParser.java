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
    @Override
    public AbstractGroup createGroup() {
        return new GroupGridEngine();
    }

    public String getNodeName() {
        return "gridEngineProcess";
    }

    @Override
    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);

        GroupGridEngine gridGroup = (GroupGridEngine) getGroup();

        String queueName = GCMParserHelper.getAttributeValue(groupNode, "queue");
        gridGroup.setQueueName(queueName);

        try {
            Node optionNode = (Node) xpath.evaluate("gridEngineOption",
                    groupNode, XPathConstants.NODE);

            NodeList childNodes = optionNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); ++i) {
                Node childNode = childNodes.item(i);
                if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeName = childNode.getNodeName();
                String nodeExpandedValue = GCMParserHelper.getElementValue(childNode);
                if (nodeName.equals("hostlist")) {
                    gridGroup.setHostList(nodeExpandedValue);
                } else if (nodeName.equals("hostsNumber")) {
                    gridGroup.setHostsNumber(nodeExpandedValue);
                } else if (nodeName.equals("parallelEnvironment")) {
                    gridGroup.setParallelEnvironment(nodeExpandedValue);
                } else if (nodeName.equals("bookingDuration")) {
                    gridGroup.setBookingDuration(nodeExpandedValue);
                    //                    } else if (nodeName.equals(OUTPUT_FILE)) {
                    //                        gridGroup.setOutputFile(nodeExpandedValue);
                } else if (nodeName.equals("scriptPath")) {
                    PathElement path = GCMParserHelper.parsePathElementNode(childNode);
                    gridGroup.setScriptLocation(path);
                }
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }
    }
}
