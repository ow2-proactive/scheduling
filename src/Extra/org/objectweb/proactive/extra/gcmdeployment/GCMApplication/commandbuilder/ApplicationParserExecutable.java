/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.gcmdeployment.GCMApplication.commandbuilder;

import java.io.IOException;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.NodeProvider;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.TechnicalServicesProperties;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class ApplicationParserExecutable extends AbstractApplicationParser {
    private static final String XPATH_PATH = "app:path";
    private static final String XPATH_NODE_PROVIDER = "app:nodeProvider";
    private static final String XPATH_COMMAND = "app:command";
    private static final String XPATH_ARG = "app:arg";
    private static final String XPATH_FILE_TRANSFER = "app:fileTransfer";
    protected static final String NODE_NAME = "executable";
    private TechnicalServicesProperties applicationTechnicalServices;

    @Override
    protected CommandBuilder createCommandBuilder() {
        return new CommandBuilderScript();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public void parseApplicationNode(Node appNode, GCMApplicationParser applicationParser, XPath xpath)
            throws XPathExpressionException, SAXException, IOException {
        super.parseApplicationNode(appNode, applicationParser, xpath);

        CommandBuilderScript commandBuilderScript = (CommandBuilderScript) commandBuilder;

        String instancesValue = GCMParserHelper.getAttributeValue(appNode, "instances");

        if (instancesValue != null) {
            commandBuilderScript.setInstances(instancesValue);
        }

        Node techServicesNode = (Node) xpath.evaluate(XPATH_TECHNICAL_SERVICES, appNode, XPathConstants.NODE);
        if (techServicesNode != null) {
            applicationTechnicalServices = GCMParserHelper
                    .parseTechnicalServicesNode(xpath, techServicesNode);
        } else {
            applicationTechnicalServices = new TechnicalServicesProperties();
        }

        NodeList nodeProviderNodes;
        nodeProviderNodes = (NodeList) xpath.evaluate(XPATH_NODE_PROVIDER, appNode, XPathConstants.NODESET);
        Map<String, NodeProvider> nodeProvidersMap = applicationParser.getNodeProviders();

        // resource providers
        //
        for (int i = 0; i < nodeProviderNodes.getLength(); ++i) {
            Node rpNode = nodeProviderNodes.item(i);
            String refid = GCMParserHelper.getAttributeValue(rpNode, "refid");
            NodeProvider nodeProvider = nodeProvidersMap.get(refid);
            if (nodeProvider != null) {
                commandBuilderScript.addDescriptor(nodeProvider);
            } else {
                // TODO - log warning
            }
        }

        Node commandNode = (Node) xpath.evaluate(XPATH_COMMAND, appNode, XPathConstants.NODE);

        String name = GCMParserHelper.getAttributeValue(commandNode, "name");
        commandBuilderScript.setCommand(name);

        Node pathNode = (Node) xpath.evaluate(XPATH_PATH, commandNode, XPathConstants.NODE);
        if (pathNode != null) {
            // path tag is optional
            commandBuilderScript.setPath(GCMParserHelper.parsePathElementNode(pathNode));
        }

        // command args
        //
        NodeList argNodes = (NodeList) xpath.evaluate(XPATH_ARG, commandNode, XPathConstants.NODESET);
        for (int i = 0; i < argNodes.getLength(); ++i) {
            Node argNode = argNodes.item(i);
            String argVal = argNode.getFirstChild().getNodeValue();
            commandBuilderScript.addArg(argVal);
        }
    }

    @Override
    public TechnicalServicesProperties getTechnicalServicesProperties() {
        return applicationTechnicalServices;
    }

}
