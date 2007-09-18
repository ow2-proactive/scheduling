package org.objectweb.proactive.extra.gcmdeployment.GCMApplication.ApplicationParsers;

import java.io.IOException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationParser;
import org.objectweb.proactive.extra.gcmdeployment.process.CommandBuilder;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


public abstract class AbstractApplicationParser implements ApplicationParser {
    protected CommandBuilder commandBuilder;
    protected XPath xpath;

    public AbstractApplicationParser() {
        commandBuilder = createCommandBuilder();
    }

    public CommandBuilder getCommandBuilder() {
        return commandBuilder;
    }

    public void parseApplicationNode(Node applicationNode,
        GCMApplicationParser applicationParser, XPath xpath)
        throws XPathExpressionException, SAXException, IOException {
        this.xpath = xpath;
    }

    protected abstract CommandBuilder createCommandBuilder();
}
