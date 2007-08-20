package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.PathElement;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupMPI;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupMPIParser extends AbstractGroupParser {
    @Override
    public AbstractGroup createGroup() {
        return new GroupMPI();
    }

    public String getNodeName() {
        return "mpiGroup";
    }

    @Override
    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);

        GroupMPI mpiGroup = (GroupMPI) getGroup();

        String mpiFileName = GCMParserHelper.getAttributeValue(groupNode,
                "mpiFileName");
        mpiGroup.setMpiFileName(mpiFileName);

        String hostsFileName = GCMParserHelper.getAttributeValue(groupNode,
                "hostsFileName");
        mpiGroup.setHostsFileName(hostsFileName);

        String mpiCommandOptions = GCMParserHelper.getAttributeValue(groupNode,
                "mpiCommandOptions");
        mpiGroup.setMpiCommandOptions(mpiCommandOptions);

        try {
            Node optionNode = (Node) xpath.evaluate("mpiOptions", groupNode,
                    XPathConstants.NODE);
            NodeList childNodes = optionNode.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); ++i) {
                Node childNode = childNodes.item(i);
                if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeName = childNode.getNodeName();

                if (nodeName.equals("localRelativePath")) {
                    PathElement path = GCMParserHelper.parsePathElementNode(childNode);
                    mpiGroup.setLocalPath(path);
                } else if (nodeName.equals("remoteAbsolutePath")) {
                    PathElement path = GCMParserHelper.parsePathElementNode(childNode);
                    mpiGroup.setRemotePath(path);
                } else if (nodeName.equals("processNumber")) {
                    String nodeExpandedValue = GCMParserHelper.getElementValue(childNode);
                    mpiGroup.setHostsNumber(nodeExpandedValue);
                }
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }
    }
}
