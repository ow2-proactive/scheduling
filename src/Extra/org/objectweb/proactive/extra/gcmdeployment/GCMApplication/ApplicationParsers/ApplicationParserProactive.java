package org.objectweb.proactive.extra.gcmdeployment.GCMApplication.ApplicationParsers;

import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.PathElement;
import org.objectweb.proactive.extra.gcmdeployment.process.CommandBuilder;
import org.objectweb.proactive.extra.gcmdeployment.process.commandbuilder.CommandBuilderProActive;
import org.w3c.dom.Node;


public class ApplicationParserProactive extends AbstractApplicationParser {
    protected static final String NODE_NAME = "proactive";

    @Override
    protected CommandBuilder createCommandBuilder() {
        return new CommandBuilderProActive();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    public void parseApplicationNode(Node paNode,
        GCMApplicationParser applicationParser, XPath xpath) {
        super.parseApplicationNode(paNode, applicationParser, xpath);

        CommandBuilderProActive commandBuilderProActive = (CommandBuilderProActive) commandBuilder;

        String relPath = GCMParserHelper.getAttributeValue(paNode, "relpath");

        // TODO - what do we do with this ?
        Node javaNode;
        try {
            javaNode = (Node) xpath.evaluate("pa:java", paNode,
                    XPathConstants.NODE);

            if (javaNode != null) {
                String javaRelPath = GCMParserHelper.getAttributeValue(javaNode,
                        "relpath");
                PathElement pathElement = new PathElement();
                pathElement.setRelPath(javaRelPath);
                commandBuilderProActive.setJavaPath(pathElement);
            }

            Node configNode = (Node) xpath.evaluate("pa:configuration", paNode,
                    XPathConstants.NODE);

            if (configNode != null) {
                parseProActiveConfiguration(xpath, commandBuilderProActive,
                    configNode);
            }

            commandBuilderProActive.setVirtualNodes(applicationParser.getVirtualNodes());
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void parseProActiveConfiguration(XPath xpath,
        CommandBuilderProActive commandBuilderProActive, Node configNode)
        throws XPathExpressionException {
        Node classPathNode = (Node) xpath.evaluate("pa:proactiveClasspath",
                configNode, XPathConstants.NODE);

        List<PathElement> proactiveClassPath = GCMParserHelper.parseClasspath(xpath,
                classPathNode);

        commandBuilderProActive.setProActiveClasspath(proactiveClassPath);

        classPathNode = (Node) xpath.evaluate("pa:applicationClasspath",
                configNode, XPathConstants.NODE);

        List<PathElement> applicationClassPath = GCMParserHelper.parseClasspath(xpath,
                classPathNode);

        commandBuilderProActive.setApplicationClasspath(applicationClassPath);

        // security policy
        //
        Node securityPolicyNode = (Node) xpath.evaluate("pa:securityPolicy",
                configNode, XPathConstants.NODE);

        if (securityPolicyNode != null) {
            PathElement pathElement = GCMParserHelper.parsePathElementNode(securityPolicyNode);
            commandBuilderProActive.setSecurityPolicy(pathElement);
        }

        // log4j properties
        //
        Node log4jPropertiesNode = (Node) xpath.evaluate("pa:log4jProperties",
                configNode, XPathConstants.NODE);

        if (log4jPropertiesNode != null) {
            PathElement pathElement = GCMParserHelper.parsePathElementNode(log4jPropertiesNode);
            commandBuilderProActive.setLog4jProperties(pathElement);
        }
    }
}
