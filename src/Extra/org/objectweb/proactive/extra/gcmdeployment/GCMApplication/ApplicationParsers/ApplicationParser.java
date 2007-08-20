package org.objectweb.proactive.extra.gcmdeployment.GCMApplication.ApplicationParsers;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationParser;
import org.objectweb.proactive.extra.gcmdeployment.process.CommandBuilder;
import org.w3c.dom.Node;


public interface ApplicationParser {
    public void parseApplicationNode(Node applicationNode,
        GCMApplicationParser applicationParser, XPath xpath);

    CommandBuilder getCommandBuilder();

    /**
     * Returns the nodeName associated to a particular parser
     * @return the nodeName as a String
     */
    public String getNodeName();
}
