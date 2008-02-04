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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.Helpers;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.commandbuilder.ApplicationParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.commandbuilder.ApplicationParserExecutable;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.commandbuilder.ApplicationParserProactive;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptorFactory;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptorParams;
import org.objectweb.proactive.extra.gcmdeployment.core.GCMVirtualNode;
import org.objectweb.proactive.extra.gcmdeployment.core.GCMVirtualNodeImpl;
import org.objectweb.proactive.extra.gcmdeployment.core.GCMVirtualNodeInternal;
import org.objectweb.proactive.extra.gcmdeployment.environment.Environment;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/*
 * FIXME: Improvements needed - Refactoring & Cleanup - Put all "magic strings" in a warehouse -
 * Write some comment to explain how it works
 */
public class GCMApplicationParserImpl implements GCMApplicationParser {
    private static final String OLD_DESCRIPTOR_SCHEMA = "http://www-sop.inria.fr/oasis/proactive/schema/3.2/DescriptorSchema.xsd";
    private static final String XPATH_GCMAPP = "/app:GCMApplication/";
    private static final String XPATH_VIRTUAL_NODE = XPATH_GCMAPP +
        "app:application/app:proactive/app:virtualNode";
    private static final String XPATH_NODE_PROVIDERS = XPATH_GCMAPP + "app:resources/app:nodeProvider";
    private static final String XPATH_APPLICATION = XPATH_GCMAPP + "app:application";
    private static final String XPATH_NODE_PROVIDER = "app:nodeProvider";
    private static final String XPATH_TECHNICAL_SERVICES = "app:technicalServices";
    private static final String XPATH_FILE = "app:file";
    public static final String ATTR_RP_CAPACITY = "capacity";
    protected File descriptor;
    protected VariableContract vContract;

    protected Document document;
    protected DocumentBuilderFactory domFactory;
    protected XPath xpath;

    protected List<String> schemas;
    protected CommandBuilder commandBuilder;
    protected Map<String, NodeProvider> nodeProvidersMap;
    protected Map<String, GCMVirtualNodeInternal> virtualNodes;
    protected Map<String, ApplicationParser> applicationParsersMap;

    public GCMApplicationParserImpl(File descriptor, VariableContract vContract) throws IOException,
            ParserConfigurationException, SAXException, XPathExpressionException, TransformerException {
        this(descriptor, vContract, null);
    }

    public GCMApplicationParserImpl(File descriptor, VariableContract vContract, List<String> userSchemas)
            throws IOException, ParserConfigurationException, SAXException, TransformerException,
            XPathExpressionException {
        this.descriptor = descriptor;
        this.vContract = vContract;

        nodeProvidersMap = null;
        virtualNodes = null;
        schemas = (userSchemas != null) ? new ArrayList<String>(userSchemas) : new ArrayList<String>();
        applicationParsersMap = new HashMap<String, ApplicationParser>();

        registerDefaultApplicationParsers();
        registerUserApplicationParsers();

        setupJAXP();

        try {
            InputSource processedInputSource = Environment.replaceVariables(descriptor, vContract,
                    domFactory, xpath, GCM_APPLICATION_NAMESPACE_PREFIX);
            DocumentBuilder documentBuilder = GCMParserHelper.getNewDocumentBuilder(domFactory);
            document = documentBuilder.parse(processedInputSource);

            // sanity check : make sure there isn't a ref to an old schema in the document
            //
            String noNamespaceSchema = document.getDocumentElement().getAttribute(
                    "xsi:noNamespaceSchemaLocation");
            if (noNamespaceSchema != null && noNamespaceSchema.contains(OLD_DESCRIPTOR_SCHEMA)) {
                throw new SAXException("Trying to parse an old descriptor");
            }
        } catch (SAXException e) {
            String msg = "parsing problem with document " + descriptor.getCanonicalPath();
            GCMDeploymentLoggers.GCMA_LOGGER.fatal(msg + " - " + e.getMessage());
            throw new SAXException(msg, e);
        } catch (TransformerException e) {
            String msg = "problem when evaluating variables with document " + descriptor.getCanonicalPath();
            GCMDeploymentLoggers.GCMA_LOGGER.fatal(msg + " - " + e.getMessage());
            throw new TransformerException(msg, e);
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMA_LOGGER.fatal(e);
            throw e;
        }

    }

    /**
     * override me
     */
    protected void registerUserApplicationParsers() {
    }

    public void registerApplicationParser(ApplicationParser applicationParser) {
        applicationParsersMap.put(applicationParser.getNodeName(), applicationParser);
    }

    private void registerDefaultApplicationParsers() {
        registerApplicationParser(new ApplicationParserProactive());
        registerApplicationParser(new ApplicationParserExecutable());
    }

    public void setupJAXP() throws IOException, ParserConfigurationException {
        domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        domFactory.setValidating(true);
        domFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);

        URL applicationSchema = getClass().getResource(APPLICATION_DESC_LOCATION);

        schemas.add(0, applicationSchema.toString());
        domFactory.setAttribute(JAXP_SCHEMA_SOURCE, schemas.toArray());

        XPathFactory factory = XPathFactory.newInstance();
        xpath = factory.newXPath();
        xpath.setNamespaceContext(new GCMParserHelper.ProActiveNamespaceContext());
    }

    synchronized public Map<String, NodeProvider> getNodeProviders() throws SAXException, IOException {
        if (nodeProvidersMap != null) {
            return nodeProvidersMap;
        }

        nodeProvidersMap = new HashMap<String, NodeProvider>();

        try {
            NodeList nodeProviderNodes;

            nodeProviderNodes = (NodeList) xpath.evaluate(XPATH_NODE_PROVIDERS, document,
                    XPathConstants.NODESET);

            for (int i = 0; i < nodeProviderNodes.getLength(); ++i) {
                Node nodeProviderNode = nodeProviderNodes.item(i);

                String id = GCMParserHelper.getAttributeValue(nodeProviderNode, "id");
                NodeProvider nodeProvider = new NodeProvider(id);

                NodeList gcmdNodes;
                gcmdNodes = (NodeList) xpath.evaluate(XPATH_FILE, nodeProviderNode, XPathConstants.NODESET);
                for (int j = 0; j < gcmdNodes.getLength(); j++) {
                    GCMDeploymentDescriptorParams gcmdParams = new GCMDeploymentDescriptorParams();
                    gcmdParams.setId(id);
                    String file = GCMParserHelper.getAttributeValue(gcmdNodes.item(j), "path");

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
                    gcmdParams.setVContract(vContract);

                    GCMDeploymentDescriptor gcmd = GCMDeploymentDescriptorFactory
                            .createDescriptor(gcmdParams);
                    nodeProvider.addGCMDeploymentDescriptor(gcmd);
                }

                // get fileTransfers
                /*
                 * HashSet<FileTransferBlock> fileTransferBlocks = new HashSet<FileTransferBlock>();
                 * NodeList fileTransferNodes = (NodeList) xpath.evaluate(XPATH_FILETRANSFER, node,
                 * XPathConstants.NODESET); for (int j = 0; j < fileTransferNodes.getLength(); ++j) {
                 * Node fileTransferNode = fileTransferNodes.item(j); FileTransferBlock
                 * fileTransferBlock = GCMParserHelper.parseFileTransferNode(fileTransferNode);
                 * fileTransferBlocks.add(fileTransferBlock); }
                 */
                nodeProvidersMap.put(nodeProvider.getId(), nodeProvider);
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMA_LOGGER.fatal(e.getMessage(), e);
        } catch (IOException e) {
            GCMDeploymentLoggers.GCMA_LOGGER.fatal(e.getMessage(), e);
        } catch (TransformerException e) {
            GCMDeploymentLoggers.GCMA_LOGGER.fatal(e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            GCMDeploymentLoggers.GCMA_LOGGER.fatal(e.getMessage(), e);
        }

        return nodeProvidersMap;
    }

    public CommandBuilder getCommandBuilder() throws XPathExpressionException, SAXException, IOException {
        if (commandBuilder != null) {
            return commandBuilder;
        }

        Node applicationNode = (Node) xpath.evaluate(XPATH_APPLICATION, document, XPathConstants.NODE);

        NodeList appNodes = applicationNode.getChildNodes();

        for (int i = 0; i < appNodes.getLength(); ++i) {
            Node commandNode = appNodes.item(i);
            if (commandNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            ApplicationParser applicationParser = getApplicationParserForNode(commandNode);
            if (applicationParser == null) {
                GCMDeploymentLoggers.GCMA_LOGGER.warn("No application parser registered for node <" +
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

    synchronized public Map<String, GCMVirtualNodeInternal> getVirtualNodes() throws SAXException,
            IOException {
        if (virtualNodes != null) {
            return virtualNodes;
        }

        try {
            virtualNodes = new HashMap<String, GCMVirtualNodeInternal>();

            // make sure these are parsed
            getNodeProviders();

            NodeList nodes = (NodeList) xpath.evaluate(XPATH_VIRTUAL_NODE, document, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); ++i) {
                Node node = nodes.item(i);

                // get Id
                //
                GCMVirtualNodeImpl virtualNode = new GCMVirtualNodeImpl();

                String id = GCMParserHelper.getAttributeValue(node, "id");
                virtualNode.setName(id);

                // get capacity
                //
                String capacity = GCMParserHelper.getAttributeValue(node, ATTR_RP_CAPACITY);

                virtualNode.setCapacity(capacityAsLong(capacity));

                // get technical services (if any)
                //
                Node techServices = (Node) xpath
                        .evaluate(XPATH_TECHNICAL_SERVICES, node, XPathConstants.NODE);
                if (techServices != null) {
                    GCMParserHelper.parseTechnicalServicesNode(xpath, techServices);
                }

                // get resource providers references
                //
                NodeList nodeProviderNodes = (NodeList) xpath.evaluate(XPATH_NODE_PROVIDER, node,
                        XPathConstants.NODESET);
                if (nodeProviderNodes.getLength() == 0) {
                    // Add all the Node Providers to this Virtual Node
                    for (NodeProvider nodeProvider : NodeProvider.getAllNodeProviders()) {
                        virtualNode.addNodeProviderContract(nodeProvider, GCMVirtualNode.MAX_CAPACITY);
                    }
                } else {
                    for (int j = 0; j < nodeProviderNodes.getLength(); j++) {
                        Node nodeProv = nodeProviderNodes.item(j);

                        String refId = GCMParserHelper.getAttributeValue(nodeProv, "refid");
                        capacity = GCMParserHelper.getAttributeValue(nodeProv, ATTR_RP_CAPACITY);

                        NodeProvider nodeProvider = nodeProvidersMap.get(refId);
                        virtualNode.addNodeProviderContract(nodeProvider, capacityAsLong(capacity));

                        Node nodeProviderTechServices = (Node) xpath.evaluate(XPATH_TECHNICAL_SERVICES,
                                nodeProv, XPathConstants.NODE);
                        if (techServices != null) {
                            GCMParserHelper.parseTechnicalServicesNode(xpath, nodeProviderTechServices);
                        }

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
            return GCMVirtualNode.MAX_CAPACITY;
        }

        try {
            return Long.parseLong(capacity);
        } catch (NumberFormatException e) {
            GCMDeploymentLoggers.GCMA_LOGGER.warn("Invalid value for capacity: " + capacity, new Exception());
            return GCMVirtualNode.MAX_CAPACITY;
        }
    }
}
