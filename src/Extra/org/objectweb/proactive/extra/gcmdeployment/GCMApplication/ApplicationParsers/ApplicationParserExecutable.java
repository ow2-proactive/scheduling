package org.objectweb.proactive.extra.gcmdeployment.GCMApplication.ApplicationParsers;

import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.FileTransferBlock;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.CommandBuilder;
import org.objectweb.proactive.extra.gcmdeployment.process.commandbuilder.CommandBuilderScript;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ApplicationParserExecutable extends AbstractApplicationParser {
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
        GCMApplicationParser applicationParser, XPath xpath) {
        super.parseApplicationNode(appNode, applicationParser, xpath);

        CommandBuilderScript commandBuilderScript = (CommandBuilderScript) commandBuilder;

        String instancesValue = GCMParserHelper.getAttributeValue(appNode,
                "instances");

        if (instancesValue != null) {
            commandBuilderScript.setInstances(instancesValue);
        }

        NodeList resourceProviderNodes;
        try {
            resourceProviderNodes = (NodeList) xpath.evaluate("pa:resourceProvider",
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

            Node commandNode = (Node) xpath.evaluate("pa:command", appNode,
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
            NodeList argNodes = (NodeList) xpath.evaluate("pa:arg",
                    commandNode, XPathConstants.NODESET);
            for (int i = 0; i < argNodes.getLength(); ++i) {
                Node argNode = argNodes.item(i);
                String argVal = argNode.getFirstChild().getNodeValue();
                commandBuilderScript.addArg(argVal);
            }

            // filetransfer
            //
            NodeList fileTransferNodes = (NodeList) xpath.evaluate("pa:filetransfer",
                    appNode, XPathConstants.NODESET);

            for (int i = 0; i < fileTransferNodes.getLength(); ++i) {
                Node fileTransferNode = fileTransferNodes.item(i);
                FileTransferBlock fileTransferBlock = GCMParserHelper.parseFileTransferNode(fileTransferNode);
                commandBuilderScript.addFileTransferBlock(fileTransferBlock);
            }
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
