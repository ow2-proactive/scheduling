package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extra.gcmdeployment.process.Group;
import org.w3c.dom.Node;


public interface GroupParser {
    public void parseGroupNode(Node groupNode, XPath xpath);

    public Group getGroup();

    /**
     * Returns the nodeName associated to a particular parser
     * @return the nodeName as a String
     */
    public String getNodeName();
}
