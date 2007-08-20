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
    @Override
    public AbstractGroup createGroup() {
        return new GroupGlobus();
    }

    public String getNodeName() {
        return "globusProcess";
    }

    @Override
    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);

        GroupGlobus globusGroup = (GroupGlobus) getGroup();

        try {
            Node optionNode = (Node) xpath.evaluate("globusOption", groupNode,
                    XPathConstants.NODE);

            NodeList childNodes = optionNode.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); ++j) {
                Node child = childNodes.item(j);
                if (child.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeValue = GCMParserHelper.getElementValue(child);
                String nodeName = child.getNodeName();
                if (nodeName.equals("count")) {
                    globusGroup.setCount(nodeValue);
                } else if (nodeName.equals("maxTime")) {
                    globusGroup.setMaxTime(nodeValue);
                } else if (nodeName.equals("outputFile")) {
                    globusGroup.setStdout(nodeValue);
                } else if (nodeName.equals("errorFile")) {
                    globusGroup.setStderr(nodeValue);
                }
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }
    }
}
