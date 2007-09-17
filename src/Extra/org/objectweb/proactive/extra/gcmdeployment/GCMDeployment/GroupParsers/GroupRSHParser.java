package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupRSH;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupSSH;
import org.w3c.dom.Node;


public class GroupRSHParser extends AbstractGroupParser {
    private static final String ATTR_COMMAND_PATH = "commandPath";
    private static final String ATTR_USERNAME = "username";
    private static final String ATTR_HOST_LIST = "hostList";
    static final String NODE_NAME = "rshGroup";

    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);

        GroupSSH groupSSH = (GroupSSH) getGroup();

        // Mandatory attributes
        String hostList = GCMParserHelper.getAttributeValue(groupNode,
                ATTR_HOST_LIST);
        groupSSH.setHostList(hostList);

        String username = GCMParserHelper.getAttributeValue(groupNode,
                ATTR_USERNAME);
        if (username != null) {
            groupSSH.setUsername(username);
        }

        String commandPath = GCMParserHelper.getAttributeValue(groupNode,
                ATTR_COMMAND_PATH);
        if (commandPath != null) {
            groupSSH.setCommandPath(commandPath);
        }
    }

    @Override
    public AbstractGroup createGroup() {
        return new GroupRSH();
    }

    public String getNodeName() {
        return NODE_NAME;
    }
}
