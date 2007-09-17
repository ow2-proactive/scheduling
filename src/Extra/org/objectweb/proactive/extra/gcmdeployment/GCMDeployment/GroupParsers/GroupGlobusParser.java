package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupGlobus;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupGlobusParser extends AbstractGroupParser {
    private static final String NODE_NAME_ERROR_FILE = "errorFile";
    private static final String NODE_NAME_OUTPUT_FILE = "outputFile";
    private static final String NODE_NAME_MAX_TIME = "maxTime";
    private static final String NODE_NAME_COUNT = "count";
    private static final String XPATH_GLOBUS_OPTION = "globusOption";
    private static final String NODE_NAME = "globusProcess";

    @Override
    public AbstractGroup createGroup() {
        return new GroupGlobus();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);

        GroupGlobus globusGroup = (GroupGlobus) getGroup();

        try {
            Node optionNode = (Node) xpath.evaluate(XPATH_GLOBUS_OPTION,
                    groupNode, XPathConstants.NODE);

            NodeList childNodes = optionNode.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); ++j) {
                Node child = childNodes.item(j);
                if (child.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeValue = GCMParserHelper.getElementValue(child);
                String nodeName = child.getNodeName();
                if (nodeName.equals(NODE_NAME_COUNT)) {
                    globusGroup.setCount(nodeValue);
                } else if (nodeName.equals(NODE_NAME_MAX_TIME)) {
                    globusGroup.setMaxTime(nodeValue);
                } else if (nodeName.equals(NODE_NAME_OUTPUT_FILE)) {
                    globusGroup.setStdout(nodeValue);
                } else if (nodeName.equals(NODE_NAME_ERROR_FILE)) {
                    globusGroup.setStderr(nodeValue);
                }
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }
    }
}
