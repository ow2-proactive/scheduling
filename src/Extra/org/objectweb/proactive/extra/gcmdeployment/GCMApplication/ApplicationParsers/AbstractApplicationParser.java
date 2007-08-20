package org.objectweb.proactive.extra.gcmdeployment.GCMApplication.ApplicationParsers;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationParser;
import org.objectweb.proactive.extra.gcmdeployment.process.CommandBuilder;
import org.w3c.dom.Node;


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
        GCMApplicationParser applicationParser, XPath xpath) {
        this.xpath = xpath;
    }

    protected abstract CommandBuilder createCommandBuilder();
}
