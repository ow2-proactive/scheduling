package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.Group;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.w3c.dom.Node;


public abstract class AbstractGroupParser implements GroupParser {
    protected AbstractGroup group;

    public AbstractGroupParser() {
        group = createGroup();
    }

    public void parseGroupNode(Node groupNode, XPath xpath) {
        String id = GCMParserHelper.getAttributeValue(groupNode, "id");

        group.setId(id);
        // TODO        
    }

    public Group getGroup() {
        return group;
    }

    public abstract AbstractGroup createGroup();
}
