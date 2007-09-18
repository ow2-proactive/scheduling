package org.objectweb.proactive.extra.gcmdeployment.GCMApplication.ApplicationParsers;

import java.io.IOException;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.FileTransferBlock;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.CommandBuilder;
import org.objectweb.proactive.extra.gcmdeployment.process.commandbuilder.CommandBuilderScript;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class ApplicationParserExecutable extends AbstractApplicationParser {
    private static final String PA_RESOURCE_PROVIDER = "pa:resourceProvider";
    private static final String PA_COMMAND = "pa:command";
    private static final String PA_ARG = "pa:arg";
    private static final String PA_FILE_TRANSFER = "pa:fileTransfer";
    protected static final String NODE_NAME = "executable";

    @Override
    protected CommandBuilder createCommandBuilder() {
        return new CommandBuilderScript();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public void parseApplicationNode(Node appNode,
        GCMApplicationParser applicationParser, XPath xpath)
        throws XPathExpressionException, SAXException, IOException {
        super.parseApplicationNode(appNode, applicationParser, xpath);

        CommandBuilderScript commandBuilderScript = (CommandBuilderScript) commandBuilder;

        String instancesValue = GCMParserHelper.getAttributeValue(appNode,
                "instances");

        if (instancesValue != null) {
            commandBuilderScript.setInstances(instancesValue);
        }

        NodeList resourceProviderNodes;
        resourceProviderNodes = (NodeList) xpath.evaluate(PA_RESOURCE_PROVIDER,
                appNode, XPathConstants.NODESET);
        Map<String, GCMDeploymentDescriptor> resourceProvidersMap = applicationParser.getResourceProviders();

        // resource providers
        //
        for (int i = 0; i < resourceProviderNodes.getLength(); ++i) {
            Node rpNode = resourceProviderNodes.item(i);
            String refid = GCMParserHelper.getAttributeValue(rpNode, "refid");
            GCMDeploymentDescriptor deploymentDescriptor = resourceProvidersMap.get(refid);
            if (deploymentDescriptor != null) {
                commandBuilderScript.addDescriptor(deploymentDescriptor);
            } else {
                // TODO - log warning
            }
        }

        Node commandNode = (Node) xpath.evaluate(PA_COMMAND, appNode,
                XPathConstants.NODE);

        String name = GCMParserHelper.getAttributeValue(commandNode, "name");
        commandBuilderScript.setCommand(name);

        Node pathNode = (Node) xpath.evaluate("pa:path", commandNode,
                XPathConstants.NODE);
        if (pathNode != null) {
            // path tag is optional
            commandBuilderScript.setPath(GCMParserHelper.parsePathElementNode(
                    pathNode));
        }

        // command args
        //
        NodeList argNodes = (NodeList) xpath.evaluate(PA_ARG, commandNode,
                XPathConstants.NODESET);
        for (int i = 0; i < argNodes.getLength(); ++i) {
            Node argNode = argNodes.item(i);
            String argVal = argNode.getFirstChild().getNodeValue();
            commandBuilderScript.addArg(argVal);
        }

        // filetransfer
        //
        NodeList fileTransferNodes = (NodeList) xpath.evaluate(PA_FILE_TRANSFER,
                appNode, XPathConstants.NODESET);

        for (int i = 0; i < fileTransferNodes.getLength(); ++i) {
            Node fileTransferNode = fileTransferNodes.item(i);
            FileTransferBlock fileTransferBlock = GCMParserHelper.parseFileTransferNode(fileTransferNode);
            commandBuilderScript.addFileTransferBlock(fileTransferBlock);
        }
    }
}
