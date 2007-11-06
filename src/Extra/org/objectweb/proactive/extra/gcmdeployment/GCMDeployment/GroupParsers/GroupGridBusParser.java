package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers;

import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupGridBus;
import org.w3c.dom.Node;


public class GroupGridBusParser extends AbstractGroupParser {
    private static final String NODE_NAME = "gridbusGroup";

    @Override
    public AbstractGroup createGroup() {
        return new GroupGridBus();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);

        GroupGridBus gridbusGroup = (GroupGridBus) getGroup();

        try {
            Node argumentsNode = (Node) xpath.evaluate("paext:arguments",
                    groupNode, XPathConstants.NODESET);
            List<String> argumentsList = GCMParserHelper.parseArgumentListNode(xpath,
                    argumentsNode);

            gridbusGroup.setArgumentsList(argumentsList);
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }
    }
}
