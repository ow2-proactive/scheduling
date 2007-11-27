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
package org.objectweb.proactive.extra.gcmdeployment.GCMApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.ApplicationParsers.ApplicationParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.ApplicationParsers.ApplicationParserExecutable;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.ApplicationParsers.ApplicationParserProactive;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptorFactory;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptorParams;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.Helpers;
import org.objectweb.proactive.extra.gcmdeployment.core.VirtualNode;
import org.objectweb.proactive.extra.gcmdeployment.core.VirtualNodeImpl;
import org.objectweb.proactive.extra.gcmdeployment.core.VirtualNodeInternal;
import org.objectweb.proactive.extra.gcmdeployment.process.CommandBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/*
 * FIXME: Improvements needed
 *  - Refactoring & Cleanup
 *         - Put all "magic strings" in a warehouse
 *  - Write some comment to explain how it works
 */
public class GCMApplicationParserImpl implements GCMApplicationParser {
    private static final String XPATH_GCMAPP = "/pa:GCMApplication/";
    private static final String XPATH_VIRTUAL_NODE = XPATH_GCMAPP +
        "pa:application/pa:proactive/pa:virtualNode";
    private static final String XPATH_NODE_PROVIDERS = XPATH_GCMAPP +
        "pa:resources/pa:nodeProvider";
    private static final String XPATH_APPLICATION = XPATH_GCMAPP +
        "pa:application";
    private static final String XPATH_NODE_PROVIDER = "pa:nodeProvider";
    private static final String XPATH_FILETRANSFER = "pa:filetransfer";
    private static final String XPATH_FILE = "pa:file";
    public static final String ATTR_RP_CAPACITY = "capacity";
    protected File descriptor;
    protected Document document;
    protected DocumentBuilderFactory domFactory;
    protected List<String> schemas;
    protected XPath xpath;
    protected DocumentBuilder documentBuilder;
    protected CommandBuilder commandBuilder;
    protected Map<String, NodeProvider> nodeProvidersMap;
    protected Map<String, VirtualNodeInternal> virtualNodes;
    protected Map<String, ApplicationParser> applicationParsersMap;

    public GCMApplicationParserImpl(File descriptor) throws IOException {
        this(descriptor, null);
    }

    public GCMApplicationParserImpl(File descriptor, List<String> userSchemas)
        throws IOException {
        this.descriptor = descriptor;
        nodeProvidersMap = null;
        virtualNodes = null;
        schemas = (userSchemas != null) ? new ArrayList<String>(userSchemas)
                                        : new ArrayList<String>();
        applicationParsersMap = new HashMap<String, ApplicationParser>();

        registerDefaultApplicationParsers();
        registerUserApplicationParsers();

        setup();
        InputSource inputSource = new InputSource(new FileInputStream(
                    descriptor));
        try {
            document = documentBuilder.parse(inputSource);
        } catch (SAXException e) {
            GCMDeploymentLoggers.GCMA_LOGGER.fatal(e.getMessage());
        }
    }

    /**
     * override me
     */
    protected void registerUserApplicationParsers() {
    }

    public void registerApplicationParser(ApplicationParser applicationParser) {
        applicationParsersMap.put(applicationParser.getNodeName(),
            applicationParser);
    }

    private void registerDefaultApplicationParsers() {
        registerApplicationParser(new ApplicationParserProactive());
        registerApplicationParser(new ApplicationParserExecutable());
    }

    public void setup() throws IOException {
        domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        domFactory.setValidating(true);
        domFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);

        String deploymentSchema = getClass()
                                      .getResource(APPLICATION_DESC_LOCATION)
                                      .toString();

        String commonTypesSchema = getClass().getResource(COMMON_TYPES_LOCATION)
                                       .toString();

        schemas.add(0, deploymentSchema);
        //        schemas.add(0, commonTypesSchema);
        domFactory.setAttribute(JAXP_SCHEMA_SOURCE, schemas.toArray());

        try {
            documentBuilder = domFactory.newDocumentBuilder();
            documentBuilder.setErrorHandler(new GCMParserHelper.MyDefaultHandler());

            XPathFactory factory = XPathFactory.newInstance();
            xpath = factory.newXPath();
            xpath.setNamespaceContext(new GCMParserHelper.ProActiveNamespaceContext(
                    GCM_DESCRIPTOR_NAMESPACE));
        } catch (ParserConfigurationException e) {
            GCMDeploymentLoggers.GCMA_LOGGER.fatal(e.getMessage());
        }
    }

    synchronized public Map<String, NodeProvider> getNodeProviders()
        throws SAXException, IOException {
        if (nodeProvidersMap != null) {
            return nodeProvidersMap;
        }

        nodeProvidersMap = new HashMap<String, NodeProvider>();

        try {
            NodeList nodeProviderNodes;

            nodeProviderNodes = (NodeList) xpath.evaluate(XPATH_NODE_PROVIDERS,
                    document, XPathConstants.NODESET);

            for (int i = 0; i < nodeProviderNodes.getLength(); ++i) {
                Node nodeProviderNode = nodeProviderNodes.item(i);

                String id = GCMParserHelper.getAttributeValue(nodeProviderNode,
                        "id");
                NodeProvider nodeProvider = new NodeProvider(id);

                NodeList gcmdNodes;
                gcmdNodes = (NodeList) xpath.evaluate(XPATH_FILE,
                        nodeProviderNode, XPathConstants.NODESET);
                for (int j = 0; j < gcmdNodes.getLength(); j++) {
                    GCMDeploymentDescriptorParams gcmdParams = new GCMDeploymentDescriptorParams();
                    gcmdParams.setId(id);
                    String file = GCMParserHelper.getAttributeValue(gcmdNodes.item(
                                j), "path");

                    // TODO support URL here
                    File desc = null;
                    if (file.startsWith(File.separator)) {
                        // Absolute path
                        desc = new File(file);
                    } else {
                        // Path is relative to this descriptor
                        desc = new File(descriptor.getParent(), file);
                    }
                    Helpers.checkDescriptorFileExist(desc);
                    gcmdParams.setGCMDescriptor(desc);

                    GCMDeploymentDescriptor gcmd = GCMDeploymentDescriptorFactory.createDescriptor(gcmdParams);
                    nodeProvider.addGCMDeploymentDescriptor(gcmd);
                }

                // get fileTransfers
                /*
                HashSet<FileTransferBlock> fileTransferBlocks = new HashSet<FileTransferBlock>();
                NodeList fileTransferNodes = (NodeList) xpath.evaluate(XPATH_FILETRANSFER,
                        node, XPathConstants.NODESET);
                for (int j = 0; j < fileTransferNodes.getLength(); ++j) {
                    Node fileTransferNode = fileTransferNodes.item(j);
                    FileTransferBlock fileTransferBlock = GCMParserHelper.parseFileTransferNode(fileTransferNode);
                    fileTransferBlocks.add(fileTransferBlock);
                }
                */
                nodeProvidersMap.put(nodeProvider.getId(), nodeProvider);
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMA_LOGGER.fatal(e.getMessage(), e);
        } catch (IOException e) {
            // TODO: handle exception
        }

        return nodeProvidersMap;
    }

    public CommandBuilder getCommandBuilder()
        throws XPathExpressionException, SAXException, IOException {
        if (commandBuilder != null) {
            return commandBuilder;
        }

        Node applicationNode = (Node) xpath.evaluate(XPATH_APPLICATION,
                document, XPathConstants.NODE);

        NodeList appNodes = applicationNode.getChildNodes();

        for (int i = 0; i < appNodes.getLength(); ++i) {
            Node commandNode = appNodes.item(i);
            if (commandNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            ApplicationParser applicationParser = getApplicationParserForNode(commandNode);
            if (applicationParser == null) {
                GCMDeploymentLoggers.GCMA_LOGGER.warn(
                    "No application parser registered for node <" +
                    commandNode.getNodeName() + ">");
            } else {
                applicationParser.parseApplicationNode(commandNode, this, xpath);
                commandBuilder = applicationParser.getCommandBuilder();
            }
        }

        return commandBuilder;
    }

    private ApplicationParser getApplicationParserForNode(Node commandNode) {
        ApplicationParser applicationParser = applicationParsersMap.get(commandNode.getNodeName());
        return applicationParser;
    }

    synchronized public Map<String, VirtualNodeInternal> getVirtualNodes()
        throws SAXException, IOException {
        if (virtualNodes != null) {
            return virtualNodes;
        }

        try {
            virtualNodes = new HashMap<String, VirtualNodeInternal>();

            // make sure these are parsed
            getNodeProviders();

            NodeList nodes = (NodeList) xpath.evaluate(XPATH_VIRTUAL_NODE,
                    document, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); ++i) {
                Node node = nodes.item(i);

                // get Id
                //
                VirtualNodeImpl virtualNode = new VirtualNodeImpl();

                String id = GCMParserHelper.getAttributeValue(node, "id");
                virtualNode.setName(id);

                // get capacity
                //
                String capacity = GCMParserHelper.getAttributeValue(node,
                        ATTR_RP_CAPACITY);

                virtualNode.setNbRequiredNodes(capacityAsLong(capacity));

                // get resource providers references
                //
                NodeList nodeProviderNodes = (NodeList) xpath.evaluate(XPATH_NODE_PROVIDER,
                        node, XPathConstants.NODESET);
                if (nodeProviderNodes.getLength() == 0) {
                    // Add all the Node Providers to this Virtual Node
                    for (NodeProvider nodeProvider : NodeProvider.getAllNodeProviders()) {
                        virtualNode.addNodeProviderContract(nodeProvider,
                            VirtualNode.MAX_CAPACITY);
                    }
                } else {
                    for (int j = 0; j < nodeProviderNodes.getLength(); j++) {
                        Node nodeProv = nodeProviderNodes.item(j);

                        String refId = GCMParserHelper.getAttributeValue(nodeProv,
                                "refid");
                        capacity = GCMParserHelper.getAttributeValue(nodeProv,
                                ATTR_RP_CAPACITY);

                        NodeProvider nodeProvider = nodeProvidersMap.get(refId);
                        virtualNode.addNodeProviderContract(nodeProvider,
                            capacityAsLong(capacity));
                    }
                }

                virtualNodes.put(virtualNode.getName(), virtualNode);
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMA_LOGGER.fatal(e.getMessage(), e);
        }

        return virtualNodes;
    }

    static private long capacityAsLong(String capacity) {
        if (capacity == null) {
            return VirtualNode.MAX_CAPACITY;
        }

        try {
            return Long.parseLong(capacity);
        } catch (NumberFormatException e) {
            GCMDeploymentLoggers.GCMA_LOGGER.warn(
                "Invalid value for capacity: " + capacity, new Exception());
            return VirtualNode.MAX_CAPACITY;
        }
    }
}
