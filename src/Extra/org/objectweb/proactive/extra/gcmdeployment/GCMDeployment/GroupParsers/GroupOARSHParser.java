package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupOARSH;
import org.w3c.dom.Node;


public class GroupOARSHParser extends GroupSSHParser {
    private static final String ATTR_JOB_ID = "jobId";

    @Override
    public AbstractGroup createGroup() {
        return new GroupOARSH();
    }

    @Override
    public void parseGroupNode(Node groupNode, XPath xpath) {
        super.parseGroupNode(groupNode, xpath);

        GroupOARSH oarshGroup = (GroupOARSH) getGroup();

        String jobId = GCMParserHelper.getAttributeValue(groupNode, ATTR_JOB_ID);

        oarshGroup.setJobId(jobId);
    }

    @Override
    public String getNodeName() {
        return "oarshGroup";
    }
}
