package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupFura;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupFuraParser extends AbstractGroupParser {
    private static final String NODE_NAME_MAXTIME = "maxtime";
    private static final String NODE_NAME_STDIN = "stdin";
    private static final String NODE_NAME_STDERR = "stderr";
    private static final String NODE_NAME_STDOUT = "stdout";
    private static final String NODE_NAME_DESCRIPTION = "description";
    private static final String ATTR_JOB_NAME = "jobName";
    private static final String NODE_NAME = "furaGroup";

    @Override
    public AbstractGroup createGroup() {
        return new GroupFura();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);

        GroupFura furaGroup = (GroupFura) getGroup();

        String jobName = GCMParserHelper.getAttributeValue(groupNode,
                ATTR_JOB_NAME);
        furaGroup.setJobName(jobName);

        groupNode.getChildNodes();

        NodeList childNodes = groupNode.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); ++j) {
            Node child = childNodes.item(j);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String nodeName = child.getNodeName();
            String nodeValue = GCMParserHelper.getElementValue(child);

            if (nodeName.equals(NODE_NAME_DESCRIPTION)) {
                furaGroup.setDescription(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDOUT)) {
                furaGroup.setStdout(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDERR)) {
                furaGroup.setStderr(nodeValue);
            } else if (nodeName.equals(NODE_NAME_STDIN)) {
                furaGroup.setStdin(nodeValue);
            } else if (nodeName.equals(NODE_NAME_MAXTIME)) {
                furaGroup.setMaxTime(nodeValue);
            }
        }
    }
}
