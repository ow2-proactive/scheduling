package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupSSH;
import org.w3c.dom.Node;


public class GroupSSHParser extends AbstractGroupParser {
    static final String NODE_NAME = "sshGroup";

    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);

        GroupSSH groupSSH = (GroupSSH) getGroup();

        // Mandatory attributes
        String hostList = GCMParserHelper.getAttributeValue(groupNode,
                "hostList");
        groupSSH.setHostList(hostList);

        // Optional attributes
        String domain = GCMParserHelper.getAttributeValue(groupNode, "domain");
        if (domain != null) {
            groupSSH.setDomain(domain);
        }

        String username = GCMParserHelper.getAttributeValue(groupNode,
                "username");
        if (username != null) {
            groupSSH.setUsername(username);
        }

        String commandPath = GCMParserHelper.getAttributeValue(groupNode,
                "commandPath");
        if (commandPath != null) {
            groupSSH.setCommandPath(commandPath);
        }
    }

    @Override
    public AbstractGroup createGroup() {
        return new GroupSSH();
    }

    public String getNodeName() {
        return NODE_NAME;
    }
}
