package org.objectweb.proactive.extra.gcmdeployment.GCMApplication.ApplicationParsers;

import java.io.IOException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationParser;
import org.objectweb.proactive.extra.gcmdeployment.process.CommandBuilder;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


public interface ApplicationParser {
    public void parseApplicationNode(Node applicationNode,
        GCMApplicationParser applicationParser, XPath xpath)
        throws XPathExpressionException, SAXException, IOException;

    CommandBuilder getCommandBuilder();

    /**
     * Returns the nodeName associated to a particular parser
     * @return the nodeName as a String
     */
    public String getNodeName();
}
