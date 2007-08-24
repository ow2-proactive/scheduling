package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.PathElement;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupNG;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupNGParser extends AbstractGroupParser {
    @Override
    public AbstractGroup createGroup() {
        return new GroupNG();
    }

    public String getNodeName() {
        return "ngGroup";
    }

    @Override
    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);

        GroupNG ngGroup = (GroupNG) getGroup();

        String jobname = GCMParserHelper.getAttributeValue(groupNode, "jobname");
        ngGroup.setJobname(jobname);

        String queueName = GCMParserHelper.getAttributeValue(groupNode, "queue");
        ngGroup.setQueue(queueName);

        try {
            Node optionNode = (Node) xpath.evaluate("ngOption", groupNode,
                    XPathConstants.NODE);

            NodeList childNodes = optionNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); ++i) {
                Node childNode = childNodes.item(i);
                if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeName = childNode.getNodeName();
                String nodeExpandedValue = GCMParserHelper.getElementValue(childNode);

                if (nodeName.equals("count")) {
                    ngGroup.setCount(nodeExpandedValue);
                } else if (nodeName.equals("outputFile")) {
                    ngGroup.setStdout(nodeExpandedValue);
                } else if (nodeName.equals("errorFile")) {
                    ngGroup.setStderr(nodeExpandedValue);
                } else if (nodeName.equals("executable")) {
                    PathElement path = GCMParserHelper.parsePathElementNode(childNode);
                    ngGroup.setExecutable(path);
                }
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }
    }
}
