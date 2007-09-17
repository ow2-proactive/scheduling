package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.PathElement;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupLSF;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupLSFParser extends AbstractGroupParser {
    private static final String NODE_NAME_SCRIPT_PATH = "scriptPath";
    private static final String NODE_NAME_RESOURCE_REQUIREMENT = "resourceRequirement";
    private static final String NODE_NAME_PROCESSOR = "processor";
    private static final String NODE_NAME_HOSTLIST = "hostlist";
    private static final String XPATH_LSF_OPTION = "lsfOption";
    private static final String ATTR_JOBNAME = "jobname";
    private static final String ATTR_QUEUE = "queue";

    @Override
    public AbstractGroup createGroup() {
        return new GroupLSF();
    }

    public String getNodeName() {
        return "lsfProcess";
    }

    @Override
    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);

        GroupLSF bsubGroup = (GroupLSF) getGroup();

        String interactive = GCMParserHelper.getAttributeValue(groupNode,
                "interactive");
        bsubGroup.setInteractive(interactive);

        String queueName = GCMParserHelper.getAttributeValue(groupNode,
                ATTR_QUEUE);
        bsubGroup.setQueueName(queueName);

        String jobName = GCMParserHelper.getAttributeValue(groupNode,
                ATTR_JOBNAME);
        bsubGroup.setJobName(jobName);

        try {
            Node optionNode = (Node) xpath.evaluate(XPATH_LSF_OPTION,
                    groupNode, XPathConstants.NODE);

            NodeList childNodes = optionNode.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); ++i) {
                Node childNode = childNodes.item(i);
                if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeName = childNode.getNodeName();
                String nodeValue = GCMParserHelper.getElementValue(childNode);
                if (nodeName.equals(NODE_NAME_HOSTLIST)) {
                    bsubGroup.setHostList(nodeValue);
                } else if (nodeName.equals(NODE_NAME_PROCESSOR)) {
                    bsubGroup.setProcessorNumber(nodeValue);
                } else if (nodeName.equals(NODE_NAME_RESOURCE_REQUIREMENT)) {
                    bsubGroup.setResourceRequirement(nodeValue);
                } else if (nodeName.equals(NODE_NAME_SCRIPT_PATH)) {
                    PathElement path = GCMParserHelper.parsePathElementNode(childNode);
                    bsubGroup.setScriptLocation(path);
                }
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }
    }
}
