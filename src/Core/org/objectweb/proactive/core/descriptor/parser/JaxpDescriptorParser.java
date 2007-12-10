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
package org.objectweb.proactive.core.descriptor.parser;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorImpl;
import org.objectweb.proactive.core.descriptor.data.VirtualMachine;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeInternal;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeLookup;
import org.objectweb.proactive.core.descriptor.legacyparser.ProActiveDescriptorConstants;
import org.objectweb.proactive.core.descriptor.services.FaultToleranceService;
import org.objectweb.proactive.core.descriptor.services.P2PDescriptorService;
import org.objectweb.proactive.core.descriptor.services.RMIRegistryLookupService;
import org.objectweb.proactive.core.descriptor.services.TechnicalServiceXmlType;
import org.objectweb.proactive.core.descriptor.services.UniversalService;
import org.objectweb.proactive.core.process.AbstractListProcessDecorator;
import org.objectweb.proactive.core.process.DependentListProcess;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.ExternalProcessDecorator;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.process.JVMProcess.PriorityLevel;
import org.objectweb.proactive.core.process.filetransfer.FileTransferDefinition;
import org.objectweb.proactive.core.process.filetransfer.FileTransferWorkShop;
import org.objectweb.proactive.core.process.glite.GLiteProcess;
import org.objectweb.proactive.core.process.globus.GlobusProcess;
import org.objectweb.proactive.core.process.gridengine.GridEngineSubProcess;
import org.objectweb.proactive.core.process.loadleveler.LoadLevelerProcess;
import org.objectweb.proactive.core.process.lsf.LSFBSubProcess;
import org.objectweb.proactive.core.process.mpi.MPIProcess;
import org.objectweb.proactive.core.process.nordugrid.NGProcess;
import org.objectweb.proactive.core.process.oar.OARGRIDSubProcess;
import org.objectweb.proactive.core.process.oar.OARSubProcess;
import org.objectweb.proactive.core.process.pbs.PBSSubProcess;
import org.objectweb.proactive.core.process.prun.PrunSubProcess;
import org.objectweb.proactive.core.process.rsh.maprsh.MapRshProcess;
import org.objectweb.proactive.core.process.unicore.UnicoreProcess;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


public class JaxpDescriptorParser implements ProActiveDescriptorConstants {
    static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    public static final String DESCRIPTOR_NAMESPACE = "urn:proactive:deployment:3.3";
    public static final String SECURITY_NAMESPACE = "urn:proactive:security:1.0";
    public static final String RMI_DEFAULT_PORT = "1099";
    public static final String XMLNS_PREFIX = "pa:";
    public static final String MAIN_DEFINITIONS = "//pa:mainDefinition";

    // security
    public static final String SECURITY_TAG = "//pa:security";
    public static final String SECURITY_FILE = "pa:file";
    public static final String SECURITY_FILE_URI = "uri";

    // variables
    public static final String VARIABLES_DESCRIPTOR = "//pa:descriptorVariable";
    public static final String VARIABLES_PROGRAM = "//pa:programVariable";
    public static final String VARIABLES_DESCRIPTOR_DEFAULT = "//pa:descriptorDefaultVariable";
    public static final String VARIABLES_PROGRAM_DEFAULT = "//pa:programDefaultVariable";
    public static final String VARIABLES_JAVAPROPERTY = "//pa:javaPropertyVariable";
    public static final String VARIABLES_JAVAPROPERTY_DESCRIPTOR = "//pa:javaPropertyDescriptorDefault";
    public static final String VARIABLES_JAVAPROPERTY_PROGRAM = "//pa:javaPropertyProgramDefault";
    public static final String VARIABLES_INCLUDE_XML_FILE = "//pa:includeXMLFile";
    public static final String VARIABLES_INCLUDE_PROPERTY_FILE = "//pa:includePropertyFile";

    // virtual nodes
    public static final String VIRTUAL_NODES_DEFINITIONS = "//pa:componentDefinition/pa:virtualNodesDefinition/pa:virtualNode";
    public static final String VIRTUAL_NODES_ACQUISITIONS = "//pa:componentDefinition/pa:virtualNodesAcquisition/pa:virtualNode";

    // deployment
    public static final String DEPLOYMENT = "//pa:deployment";
    public static final String REGISTER = "pa:register";
    public static final String VM_MAPPING = "pa:mapping/pa:map/pa:jvmSet/pa:vmName";
    public static final String CURRENT_VM_MAPPING = "pa:mapping/pa:map/pa:jvmSet/pa:currentJVM";
    public static final String LOOKUP = "pa:lookup";
    public static final String JVM_CREATION = "//pa:creation/pa:processReference";
    public static final String JVM_ACQUISITION = "//pa:acquisition/pa:serviceReference";

    // infrastructure
    public static final String INFRASTRUCTURE = "//pa:infrastructure";
    public static final String PROCESS_DEFINITIONS = "//pa:processDefinition/*";
    public static final String SERVICE_DEFINITIONS = "//pa:serviceDefinition/*";

    // technical services
    public static final String TECHNICAL_SERVICES = "//pa:technicalServiceDefinition";
    protected ProActiveDescriptorImpl proActiveDescriptor;
    private XPath xpath;
    private String xmlDescriptorUrl;
    private VariableContract variableContract;
    static Logger logger = ProActiveLogger.getLogger(Loggers.XML);

    protected class MyDefaultHandler extends DefaultHandler {
        private CharArrayWriter buff = new CharArrayWriter();
        private String errMessage = "";

        /* With a handler class, just override the methods you need to use
        */

        // Start Error Handler code here
        @Override
        public void warning(SAXParseException e) {
            logger.warn("Warning Line " + e.getLineNumber() + ": " +
                e.getMessage() + "\n");
        }

        @Override
        public void error(SAXParseException e) throws SAXParseException {
            errMessage = new String("Error Line " + e.getLineNumber() + ": " +
                    e.getMessage() + "\n");
            logger.error(errMessage);
            throw e;
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXParseException {
            errMessage = new String("Error Line " + e.getLineNumber() + ": " +
                    e.getMessage() + "\n");
            logger.fatal(errMessage);
            throw e;
        }
    }

    public JaxpDescriptorParser(String xmlDescriptorUrl,
        VariableContract variableContract)
        throws MalformedURLException, SAXException {
        domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        domFactory.setValidating(true);
        domFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);

        String deploymentSchema = getClass()
                                      .getResource("/org/objectweb/proactive/core/descriptor/xml/schemas/deployment/3.3/deployment.xsd")
                                      .toString();
        String securitySchemav1_0 = getClass()
                                        .getResource("/org/objectweb/proactive/core/descriptor/xml/schemas/security/1.0/security.xsd")
                                        .toString();
        String securitySchemav1_1 = getClass()
                                        .getResource("/org/objectweb/proactive/core/descriptor/xml/schemas/security/1.1/security.xsd")
                                        .toString();
        domFactory.setAttribute(JAXP_SCHEMA_SOURCE,
            new Object[] {
                deploymentSchema, securitySchemav1_0, securitySchemav1_1
            });
        this.xmlDescriptorUrl = xmlDescriptorUrl;
        this.variableContract = variableContract;
    }

    public void parse() throws SAXException, IOException, ProActiveException {
        DocumentBuilder builder;
        try {
            builder = domFactory.newDocumentBuilder();
            builder.setErrorHandler(new MyDefaultHandler());

            proActiveDescriptor = new ProActiveDescriptorImpl(xmlDescriptorUrl);
            proActiveDescriptor.setVariableContract(variableContract);

            logger.debug("Parsing " + xmlDescriptorUrl);
            document = builder.parse(xmlDescriptorUrl);
            XPathFactory factory = XPathFactory.newInstance();

            xpath = factory.newXPath();
            xpath.setNamespaceContext(new ProActiveNamespaceContext());

            handleVariables();

            handleSecurity();

            handleMainDefinitions();

            handleComponentDefinitions();

            handleDeployment();

            handleFileTransfer();

            handleInfrastructure();

            handleTechnicalServices();
        } catch (ParserConfigurationException e) {
            throw new ProActiveException(e);
        } catch (XPathExpressionException e) {
            throw new ProActiveException(e);
        }
    }

    private void handleSecurity() throws SAXException, XPathExpressionException {
        NodeList nodes = (NodeList) xpath.evaluate(SECURITY_TAG, document,
                XPathConstants.NODESET);
        Node securityNode = nodes.item(0);
        if (securityNode != null) {
            NodeList fileSubNodes = (NodeList) xpath.evaluate(SECURITY_FILE,
                    securityNode, XPathConstants.NODESET);
            if (fileSubNodes.getLength() == 1) {
                String securityFile = getNodeExpandedValue(fileSubNodes.item(0)
                                                                       .getAttributes()
                                                                       .getNamedItem(SECURITY_FILE_URI));
                if ((securityFile == null) || (securityFile.length() <= 0)) {
                    throw new SAXException("Empty security file");
                }

                File f = new File(securityFile);
                if (!f.isAbsolute()) {
                    File descriptorPath = new File(this.proActiveDescriptor.getUrl());
                    String descriptorDir = descriptorPath.getParent();
                    if (descriptorDir != null) {
                        securityFile = descriptorDir + File.separator +
                            securityFile;
                    }
                }

                logger.debug("creating ProActiveSecurityManager : " +
                    securityFile);
                proActiveDescriptor.createProActiveSecurityManager(securityFile);
            } else { // TODO : Policy node
            }
        }
    }

    private void handleMainDefinitions()
        throws XPathExpressionException, SAXException {
        NodeList nodes = (NodeList) xpath.evaluate(MAIN_DEFINITIONS, document,
                XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            String id = getNodeExpandedValue(node.getAttributes()
                                                 .getNamedItem("id"));
            String className = getNodeExpandedValue(node.getAttributes()
                                                        .getNamedItem("class"));

            if (className == null) {
                throw new org.xml.sax.SAXException(
                    "class Tag without any mainDefinition defined");
            }
            proActiveDescriptor.createMainDefinition(id);

            proActiveDescriptor.setMainDefined(true);
            proActiveDescriptor.mainDefinitionSetMainClass(className);

            // process args
            NodeList argNodes = (NodeList) xpath.evaluate(XMLNS_PREFIX +
                    ARG_TAG + "/@value", node, XPathConstants.NODESET);
            for (int j = 0; j < argNodes.getLength(); ++j) {
                Node argNode = argNodes.item(j);
                String argVal = getNodeExpandedValue(argNode);
                if (argVal == null) {
                    throw new org.xml.sax.SAXException(
                        "value Tag without any arg defined");
                }
                proActiveDescriptor.mainDefinitionAddParameter(argVal);
            }

            // process mapToVirtualNodes
            NodeList mapNodes = (NodeList) xpath.evaluate(XMLNS_PREFIX +
                    MAP_TO_VIRTUAL_NODE_TAG + "/@value", node,
                    XPathConstants.NODESET);
            for (int j = 0; j < mapNodes.getLength(); ++j) {
                Node mapNode = mapNodes.item(j);
                String virtualNode = getNodeExpandedValue(mapNode);
                if (virtualNode == null) {
                    throw new org.xml.sax.SAXException(
                        "value Tag without any mapToVirtualNode defined");
                }

                VirtualNodeInternal vn = proActiveDescriptor.createVirtualNode(virtualNode,
                        false, true);

                proActiveDescriptor.mainDefinitionAddVirtualNode(vn);
            }
        }
    }

    private void handleTechnicalServices()
        throws XPathExpressionException, SAXException {
        NodeList nodes = (NodeList) xpath.evaluate(TECHNICAL_SERVICES,
                document, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            String serviceId = getNodeExpandedValue(node.getAttributes()
                                                        .getNamedItem("id"));
            String serviceClass = getNodeExpandedValue(node.getAttributes()
                                                           .getNamedItem("class"));
            TechnicalServiceXmlType technicalService = new TechnicalServiceXmlType();
            technicalService.setId(serviceId);
            try {
                technicalService.setType(Class.forName(serviceClass));
            } catch (ClassNotFoundException e) {
                throw new SAXException("Technical Service not found", e);
            }

            // process args
            Map<String, String> argsMap = new Hashtable<String, String>();
            NodeList args = (NodeList) xpath.evaluate(XMLNS_PREFIX + ARG_TAG,
                    node, XPathConstants.NODESET);
            for (int j = 0; j < args.getLength(); ++j) {
                Node argNode = args.item(j);
                String argName = getNodeExpandedValue(argNode.getAttributes()
                                                             .getNamedItem("name"));
                String argValue = getNodeExpandedValue(argNode.getAttributes()
                                                              .getNamedItem("value"));
                argsMap.put(argName, argValue);
            }
            technicalService.setArgs(argsMap);

            try {
                proActiveDescriptor.addTechnicalService(technicalService);
            } catch (Exception e) {
                throw new SAXException("Technical service class not instanciable",
                    e);
            }
        }
    }

    private void handleVariables()
        throws XPathExpressionException, SAXException {
        // Variables
        //
        String[][] pairs = new String[][] {
                {
                    VARIABLES_JAVAPROPERTY_DESCRIPTOR,
                    VARIABLES_JAVAPROPERTY_DESCRIPTOR_TAG
                },
                {
                    VARIABLES_JAVAPROPERTY_PROGRAM,
                    VARIABLES_JAVAPROPERTY_PROGRAM_TAG
                },
                { VARIABLES_JAVAPROPERTY, VARIABLES_JAVAPROPERTY_TAG },
                { VARIABLES_DESCRIPTOR, VARIABLES_DESCRIPTOR_TAG },
                { VARIABLES_PROGRAM, VARIABLES_PROGRAM_TAG },
                { VARIABLES_PROGRAM_DEFAULT, VARIABLES_PROGRAM_DEFAULT_TAG },
                { VARIABLES_DESCRIPTOR_DEFAULT, VARIABLES_DESCRIPTOR_DEFAULT_TAG },
            };

        for (int i = 0; i < pairs.length; ++i) {
            VariableContractType varContractType = VariableContractType.getType(pairs[i][1]);
            processVariables(pairs[i][0], varContractType);
        }

        NodeList nodes = (NodeList) xpath.evaluate(VARIABLES_INCLUDE_XML_FILE,
                document, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            Node attr = node.getAttributes().getNamedItem("location");
            String s = getNodeExpandedValue(attr);
            if (s != null) {
                variableContract.loadXML(s);
            }
        }

        nodes = (NodeList) xpath.evaluate(VARIABLES_INCLUDE_PROPERTY_FILE,
                document, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            Node attr = node.getAttributes().getNamedItem("location");
            String s = getNodeExpandedValue(attr);
            if (s != null) {
                variableContract.load(s);
            }
        }
    }

    private void processVariables(String expr,
        VariableContractType varContractType)
        throws XPathExpressionException, SAXException {
        Object result = xpath.evaluate(expr, document, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);

            Node varNameItem = node.getAttributes().getNamedItem("name");
            if (!checkNonEmptyNode(varNameItem)) {
                throw new org.xml.sax.SAXException("Variable has no name");
            }

            String varName = varNameItem.getNodeValue();

            String varValue = getNodeExpandedValue(node.getAttributes()
                                                       .getNamedItem("value"));

            if (varValue == null) {
                varValue = "";
            }

            variableContract.setDescriptorVariable(varName, varValue,
                varContractType);
        }
    }

    private void handleFileTransfer()
        throws XPathExpressionException, SAXException {
        NodeList nodes = (NodeList) xpath.evaluate("//pa:fileTransferDefinitions/pa:fileTransfer",
                document, XPathConstants.NODESET);

        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);

            String fileTransferId = getNodeExpandedValue(node.getAttributes()
                                                             .getNamedItem("id"));

            FileTransferDefinition fileTransfer = proActiveDescriptor.getFileTransfer(fileTransferId);

            NodeList childNodes = node.getChildNodes();

            for (int j = 0; j < childNodes.getLength(); ++j) {
                Node transferNode = childNodes.item(j);
                if (transferNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                NamedNodeMap attributes = transferNode.getAttributes();
                String src = getNodeExpandedValue(attributes.getNamedItem("src"));
                String dest = getNodeExpandedValue(attributes.getNamedItem(
                            "dest"));

                if (transferNode.getNodeName().equals(FILE_TRANSFER_FILE_TAG)) {
                    fileTransfer.addFile(src, dest);
                } else if (transferNode.getNodeName()
                                           .equals(FILE_TRANSFER_DIR_TAG)) {
                    String include = getNodeExpandedValue(attributes.getNamedItem(
                                "include"));
                    String exclude = getNodeExpandedValue(attributes.getNamedItem(
                                "exclude"));
                    fileTransfer.addDir(src, dest, include, exclude);
                }
            }
        }
    }

    private void handleComponentDefinitions()
        throws XPathExpressionException, SAXException {
        NodeList nodes = (NodeList) xpath.evaluate(VIRTUAL_NODES_DEFINITIONS,
                document, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            NamedNodeMap attributes = node.getAttributes();
            Node nodeName = attributes.getNamedItem("name");
            Node nodeProperty = attributes.getNamedItem("property");

            //            System.out.println("Virtual node definition : "
            //                    + nodeName.getNodeValue() + " - "
            //                    + nodeProperty.getNodeValue());
            VirtualNodeImpl vn = (VirtualNodeImpl) proActiveDescriptor.createVirtualNode(getNodeExpandedValue(
                        nodeName), false);
            String s = getNodeExpandedValue(nodeProperty);
            if (s != null) {
                vn.setProperty(s);
            }

            Node timeout = attributes.getNamedItem("timeout");
            Node waitForTimeoutN = attributes.getNamedItem("waitForTimeout");
            boolean waitForTimeout = false;
            s = getNodeExpandedValue(waitForTimeoutN);
            if (s != null) {
                String nodeValue = s.toLowerCase();
                waitForTimeout = Boolean.parseBoolean(nodeValue); // nodeValue.equals("y")
                                                                  // ||
                                                                  // nodeValue.equals("true")
                                                                  // ||
                                                                  // nodeValue.equals("1");
            }

            s = getNodeExpandedValue(timeout);
            if (s != null) {
                vn.setTimeout(Long.parseLong(s), waitForTimeout);
            }

            Node minNodeNumber = attributes.getNamedItem("minNodeNumber");
            s = getNodeExpandedValue(minNodeNumber);
            if (s != null) {
                vn.setMinNumberOfNodes(Integer.parseInt(s));
            }

            Node ftServiceId = attributes.getNamedItem("ftServiceId");
            s = getNodeExpandedValue(ftServiceId);
            if (s != null) {
                proActiveDescriptor.registerService(vn, s);
            }

            Node fileTransferDeploy = attributes.getNamedItem(FILE_TRANSFER_DEPLOY_TAG);
            s = getNodeExpandedValue(fileTransferDeploy);
            if (s != null) {
                vn.addFileTransferDeploy(proActiveDescriptor.getFileTransfer(s));
            }

            Node fileTransferRetrieve = attributes.getNamedItem(FILE_TRANSFER_RETRIEVE_TAG);
            s = getNodeExpandedValue(fileTransferRetrieve);
            if (s != null) {
                vn.addFileTransferRetrieve(proActiveDescriptor.getFileTransfer(
                        s));
            }

            Node techServiceId = attributes.getNamedItem(TECHNICAL_SERVICE_ID);
            s = getNodeExpandedValue(techServiceId);
            if (s != null) {
                vn.addTechnicalService(proActiveDescriptor.getTechnicalService(
                        s));
            }
        }

        //
        // Node acquisitions
        //
        nodes = (NodeList) xpath.evaluate(VIRTUAL_NODES_ACQUISITIONS, document,
                XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            Node nodeName = node.getAttributes().getNamedItem("name");
            String s = getNodeExpandedValue(nodeName);
            if (s != null) {
                proActiveDescriptor.createVirtualNode(s, true);
            }
        }
    }

    private void handleDeployment()
        throws XPathExpressionException, SAXException, IOException {
        //
        // register
        //
        NodeList deploymentNodes = (NodeList) xpath.evaluate(DEPLOYMENT,
                document, XPathConstants.NODESET);

        if (deploymentNodes.getLength() == 0) {
            throw new ProActiveRuntimeException(
                "No 'deployment' node found in descriptor");
        }

        Node deploymentContextItem = deploymentNodes.item(0);
        NodeList nodes = (NodeList) xpath.evaluate(REGISTER,
                deploymentContextItem, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            Node virtualNodeName = node.getAttributes()
                                       .getNamedItem("virtualNode");

            Node protocol = node.getAttributes().getNamedItem("protocol");
            String p = getNodeExpandedValue(protocol);
            String protocolValue = (p != null) ? p
                                               : PAProperties.PA_COMMUNICATION_PROTOCOL.getValue();
            VirtualNodeImpl vnImpl = (VirtualNodeImpl) proActiveDescriptor.createVirtualNode(getNodeExpandedValue(
                        virtualNodeName), false);

            vnImpl.setRegistrationProtocol(protocolValue);
        }

        //
        // mapping
        //

        // collect the mappings in a hashmap
        //
        nodes = (NodeList) xpath.evaluate(VM_MAPPING, deploymentContextItem,
                XPathConstants.NODESET);

        HashMap<String, ArrayList<String>> vmMapping = new HashMap<String, ArrayList<String>>();

        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            Node mapParent = node.getParentNode().getParentNode();
            String virtualNodeName = getNodeExpandedValue(mapParent.getAttributes()
                                                                   .getNamedItem("virtualNode"));
            ArrayList<String> arrayList = vmMapping.get(virtualNodeName);
            if (arrayList == null) {
                arrayList = new ArrayList<String>();
                vmMapping.put(virtualNodeName, arrayList);
            }
            arrayList.add(getNodeExpandedValue(node.getAttributes()
                                                   .getNamedItem("value")));
        }

        // set the VM mappings to each virtual node
        //
        for (String s : vmMapping.keySet()) {
            VirtualNodeInternal vn = proActiveDescriptor.createVirtualNode(s,
                    false);
            for (String vmName : vmMapping.get(s)) {
                VirtualMachine vm = proActiveDescriptor.createVirtualMachine(vmName);
                vn.addVirtualMachine(vm);
            }
        }

        // current vm mappings
        //
        nodes = (NodeList) xpath.evaluate(CURRENT_VM_MAPPING,
                deploymentContextItem, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            Node mapParent = node.getParentNode().getParentNode();
            String virtualNodeName = getNodeExpandedValue(mapParent.getAttributes()
                                                                   .getNamedItem("virtualNode"));
            VirtualNodeInternal vn = proActiveDescriptor.createVirtualNode(virtualNodeName,
                    false);
            Node protocolAttr = node.getAttributes().getNamedItem("protocol");
            String protocol = getNodeExpandedValue(protocolAttr);
            if (protocol == null) {
                protocol = PAProperties.PA_COMMUNICATION_PROTOCOL.getValue();
            }

            vn.createNodeOnCurrentJvm(protocol);
        }

        // vm lookup
        //
        nodes = (NodeList) xpath.evaluate(LOOKUP, deploymentContextItem,
                XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);

            String vnLookup = getNodeExpandedValue(node.getAttributes()
                                                       .getNamedItem("virtualNode"));
            String host = getNodeExpandedValue(node.getAttributes()
                                                   .getNamedItem("host"));
            Node namedItem = node.getAttributes().getNamedItem("protocol");
            if (namedItem == null) {
                throw new org.xml.sax.SAXException(
                    "lookup Tag without any protocol defined");
            }
            String protocol = getNodeExpandedValue(namedItem);
            Node portItem = node.getAttributes().getNamedItem("port");

            String port = getNodeExpandedValue(portItem);
            if (port == null) {
                port = RMI_DEFAULT_PORT;
            }

            VirtualNodeLookup vn = (VirtualNodeLookup) proActiveDescriptor.createVirtualNode(vnLookup,
                    true);

            vn.setLookupInformations(host, protocol, port);
        }

        // vm creation and acquisition
        //
        nodes = (NodeList) xpath.evaluate(JVM_CREATION, deploymentContextItem,
                XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            Node jvmParent = node.getParentNode().getParentNode();
            String jvmName = getNodeExpandedValue(jvmParent.getAttributes()
                                                           .getNamedItem("name"));
            Node t = jvmParent.getAttributes().getNamedItem("askedNodes");
            VirtualMachine currentVM = proActiveDescriptor.createVirtualMachine(jvmName);
            String ts = getNodeExpandedValue(t);
            if (ts != null) {
                currentVM.setNbNodes(new Integer(ts));
            }
            proActiveDescriptor.registerProcess(currentVM,
                getNodeExpandedValue(node.getAttributes().getNamedItem("refid")));
        }

        nodes = (NodeList) xpath.evaluate(JVM_ACQUISITION,
                deploymentContextItem, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            Node jvmParent = node.getParentNode().getParentNode();
            String jvmName = getNodeExpandedValue(jvmParent.getAttributes()
                                                           .getNamedItem("name"));
            Node t = jvmParent.getAttributes().getNamedItem("askedNodes");
            VirtualMachine currentVM = proActiveDescriptor.createVirtualMachine(jvmName);
            String ts = getNodeExpandedValue(t);
            if (ts != null) {
                currentVM.setNbNodes(new Integer(ts));
            }
            proActiveDescriptor.registerService(currentVM,
                getNodeExpandedValue(node.getAttributes().getNamedItem("refid")));
        }
    }

    private void handleInfrastructure()
        throws XPathExpressionException, ProActiveException, SAXException {
        NodeList t = (NodeList) xpath.evaluate(INFRASTRUCTURE, document,
                XPathConstants.NODESET);
        Node infrastructureContext = t.item(0);

        NodeList nodes = (NodeList) xpath.evaluate(PROCESS_DEFINITIONS,
                infrastructureContext, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);

            String processType = node.getNodeName();

            if (processType.equals(JVM_PROCESS_TAG)) {
                new JVMProcessExtractor(node, infrastructureContext);
            } else if (processType.equals(RSH_PROCESS_TAG)) {
                new RshProcessExtractor(node, infrastructureContext);
            } else if (processType.equals(MAPRSH_PROCESS_TAG)) {
                new MapRshProcessExtractor(node, infrastructureContext);
            } else if (processType.equals(SSH_PROCESS_TAG)) {
                new SshProcessExtractor(node, infrastructureContext);
            } else if (processType.equals(RLOGIN_PROCESS_TAG)) {
                new RloginProcessExtractor(node, infrastructureContext);
            } else if (processType.equals(BSUB_PROCESS_TAG)) {
                new BSubProcessExtractor(node, infrastructureContext);
            } else if (processType.equals(LOADLEVELER_PROCESS_TAG)) {
                new LoadLevelerProcessExtractor(node, infrastructureContext);
            } else if (processType.equals(GLOBUS_PROCESS_TAG)) {
                new GlobusProcessExtractor(node, infrastructureContext);
            } else if (processType.equals(PRUN_PROCESS_TAG)) {
                new PrunProcessExtractor(node, infrastructureContext);
            } else if (processType.equals(PBS_PROCESS_TAG)) {
                new PbsProcessExtractor(node, infrastructureContext);
            } else if (processType.equals(GRID_ENGINE_PROCESS_TAG)) {
                new GridEngineProcessExtractor(node, infrastructureContext);
            } else if (processType.equals(OAR_PROCESS_TAG)) {
                new OARProcessExtractor(node, infrastructureContext);
            } else if (processType.equals(GLITE_PROCESS_TAG)) {
                new GliteProcessExtractor(node, infrastructureContext);
            } else if (processType.equals(OARGRID_PROCESS_TAG)) {
                new OARGridProcessExtractor(node, infrastructureContext);
            } else if (processType.equals(MPI_PROCESS_TAG)) {
                new MPIProcessExtractor(node, infrastructureContext);
            } else if (processType.equals(DEPENDENT_PROCESS_SEQUENCE_TAG)) {
                new DependentProcessSequenceExtractor(node,
                    infrastructureContext);
            } else if (processType.equals(SEQUENTIAL_PROCESS_TAG)) {
                new SequentialProcessExtractor(node, infrastructureContext);
            } else if (processType.equals(UNICORE_PROCESS_TAG)) {
                new UnicoreProcessExtractor(node, infrastructureContext);
            } else if (processType.equals(NG_PROCESS_TAG)) {
                new NGProcessExtractor(node, infrastructureContext);
            } else if (processType.equals(CLUSTERFORK_PROCESS_TAG)) {
                new ClusterForkProcessExtractor(node, infrastructureContext);
            } else if (processType.equals(PROCESS_LIST_TAG) ||
                    processType.equals(PROCESS_LIST_BYHOST_TAG)) {
                new ProcessListExtractor(node, infrastructureContext);
            }
        }

        nodes = (NodeList) xpath.evaluate(SERVICE_DEFINITIONS,
                infrastructureContext, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);

            String serviceID = getNodeExpandedValue(node.getParentNode()
                                                        .getAttributes()
                                                        .getNamedItem("id"));

            String serviceType = node.getNodeName();

            UniversalService service = null;

            if (serviceType.equals(RMI_LOOKUP_TAG)) {
                String lookupURL = getNodeExpandedValue(node.getAttributes()
                                                            .getNamedItem("url"));
                service = new RMIRegistryLookupService(lookupURL);
            } else if (serviceType.equals(P2P_SERVICE_TAG)) {
                P2PDescriptorService p2pDescriptorService = new P2PDescriptorService();

                service = p2pDescriptorService;

                String nodesAsked = getNodeExpandedValue(node.getAttributes()
                                                             .getNamedItem("nodesAsked"));
                if (nodesAsked != null) {
                    if (nodesAsked.equals("MAX")) {
                        p2pDescriptorService.setNodeNumberToMAX();
                    } else {
                        p2pDescriptorService.setNodeNumber(Integer.parseInt(
                                nodesAsked));
                    }
                }

                String acq = getNodeExpandedValue(node.getAttributes()
                                                      .getNamedItem("acq"));
                if (acq != null) {
                    p2pDescriptorService.setAcq(acq);
                }

                String port = getNodeExpandedValue(node.getAttributes()
                                                       .getNamedItem("port"));
                if (port != null) {
                    p2pDescriptorService.setPort(port);
                }

                String noa = getNodeExpandedValue(node.getAttributes()
                                                      .getNamedItem("NOA"));
                if (noa != null) {
                    p2pDescriptorService.setNoa(noa);
                }

                String ttu = getNodeExpandedValue(node.getAttributes()
                                                      .getNamedItem("TTU"));
                if (ttu != null) {
                    p2pDescriptorService.setTtu(ttu);
                }

                String ttl = getNodeExpandedValue(node.getAttributes()
                                                      .getNamedItem("TTL"));
                if (ttl != null) {
                    p2pDescriptorService.setTtl(ttl);
                }

                String multi_proc_nodes = getNodeExpandedValue(node.getAttributes()
                                                                   .getNamedItem("multi_proc_nodes"));
                if (multi_proc_nodes != null) {
                    p2pDescriptorService.setMultiProcNodes(multi_proc_nodes);
                }

                String xml_path = getNodeExpandedValue(node.getAttributes()
                                                           .getNamedItem("xml_path"));
                if (xml_path != null) {
                    p2pDescriptorService.setXmlPath(xml_path);
                }

                String node_family_regexp = getNodeExpandedValue(node.getAttributes()
                                                                     .getNamedItem("node_family_regexp"));
                if (node_family_regexp != null) {
                    p2pDescriptorService.setNodeFamilyRegexp(node_family_regexp);
                }

                NodeList peerNodes = (NodeList) xpath.evaluate("pa:peerSet/pa:peer",
                        node, XPathConstants.NODESET);

                String[] peerList = new String[peerNodes.getLength()];
                for (int pp = 0; pp < peerNodes.getLength(); ++pp) {
                    peerList[pp] = peerNodes.item(pp).getTextContent().trim();
                }
                p2pDescriptorService.setPeerList(peerList);
            } else if (serviceType.equals(FT_CONFIG_TAG)) {
                FaultToleranceService ftService = new FaultToleranceService();
                service = ftService;

                NodeList childNodes = node.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); ++j) {
                    Node childNode = childNodes.item(j);
                    if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }
                    String url = getNodeExpandedValue(childNode.getAttributes()
                                                               .getNamedItem("url"));

                    String nodeName = childNode.getNodeName();
                    if (nodeName.equals(FT_RECPROCESS_TAG)) {
                        ftService.setRecoveryProcessURL(url);
                    } else if (nodeName.equals(FT_LOCSERVER_TAG)) {
                        ftService.setLocationServerURL(url);
                    } else if (nodeName.equals(FT_CKPTSERVER_TAG)) {
                        ftService.setCheckpointServerURL(url);
                    } else if (nodeName.equals(FT_RESSERVER_TAG)) {
                        ftService.setAttachedResourceServer(url);
                    } else if (nodeName.equals(FT_TTCVALUE_TAG)) {
                        String value = getNodeExpandedValue(childNode.getAttributes()
                                                                     .getNamedItem("value"));
                        ftService.setTtcValue(value);
                    } else if (nodeName.equals(FT_GLOBALSERVER_TAG)) {
                        ftService.setGlobalServerURL(url);
                    } else if (nodeName.equals(FT_PROTO_TAG)) {
                        String type = getNodeExpandedValue(childNode.getAttributes()
                                                                    .getNamedItem("type"));
                        ftService.setProtocolType(type);
                    }
                }
            }

            proActiveDescriptor.addService(serviceID, service);
        }
    }

    protected class BasicProcessExtractor {
        protected ExternalProcess targetProcess;

        public BasicProcessExtractor(Node node, Node context)
            throws XPathExpressionException, SAXException, ProActiveException {
            // get parent id
            String id = getNodeExpandedValue(node.getParentNode().getAttributes()
                                                 .getNamedItem("id"));

            String processClassName = getNodeExpandedValue(node.getAttributes()
                                                               .getNamedItem("class"));
            targetProcess = proActiveDescriptor.createProcess(id,
                    processClassName);
        }
    }

    protected class ProcessExtractor extends BasicProcessExtractor {
        public ProcessExtractor(Node node, Node context)
            throws XPathExpressionException, SAXException, ProActiveException {
            super(node, context);

            Node namedItem = node.getAttributes().getNamedItem("closeStream");
            String t = getNodeExpandedValue(namedItem);
            if ((t != null) && t.equals("yes")) {
                targetProcess.closeStream();
            }

            namedItem = node.getAttributes().getNamedItem("hostname");
            t = getNodeExpandedValue(namedItem);
            if (t != null) {
                targetProcess.setHostname(t);
            }

            namedItem = node.getAttributes().getNamedItem("username");
            t = getNodeExpandedValue(namedItem);
            if (t != null) {
                targetProcess.setUsername(t);
            }

            // get all env. vars
            //
            XPathExpression varExpr = xpath.compile("//pa:variable");
            NodeList vars = (NodeList) varExpr.evaluate(context,
                    XPathConstants.NODESET);

            ArrayList<String> envVars = new ArrayList<String>();

            for (int i = 0; i < vars.getLength(); ++i) {
                Node varNode = vars.item(i);
                String name = getNodeExpandedValue(varNode.getAttributes()
                                                          .getNamedItem("name"));
                String value = getNodeExpandedValue(varNode.getAttributes()
                                                           .getNamedItem("value"));
                if (checkNonEmptyString(name)) {
                    envVars.add(name + "=" + value);
                }
            }

            if (envVars.size() > 0) {
                String[] env = new String[envVars.size()];
                envVars.toArray(env);
                targetProcess.setEnvironment(env);
            }

            NodeList childNodes = node.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); ++j) {
                Node child = childNodes.item(j);
                if (child.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeName = child.getNodeName();
                if (nodeName.equals(PROCESS_REFERENCE_TAG)) {
                    String refid = getNodeExpandedValue(child.getAttributes()
                                                             .getNamedItem("refid"));
                    if (!(targetProcess instanceof ExternalProcessDecorator)) {
                        throw new org.xml.sax.SAXException(
                            "found a Process defined inside a non composite process");
                    }

                    holdProcessRegistration(refid);
                } else if (nodeName.equals(COMMAND_PATH_TAG)) {
                    String value = getNodeExpandedValue(child.getAttributes()
                                                             .getNamedItem("value"));
                    targetProcess.setCommandPath(value);
                } else if (nodeName.equals(FILE_TRANSFER_DEPLOY_TAG)) {
                    getFileTransfer("deploy", targetProcess, child);
                } else if (nodeName.equals(FILE_TRANSFER_RETRIEVE_TAG)) {
                    getFileTransfer("retrieve", targetProcess, child);
                }
            }
        }

        protected void holdProcessRegistration(String refid) {
            ExternalProcessDecorator cep = (ExternalProcessDecorator) targetProcess;
            proActiveDescriptor.registerProcess(cep, refid);
        }

        private FileTransferWorkShop getFileTransfer(String fileTransferQueue,
            ExternalProcess targetProcess, Node node) throws SAXException {
            FileTransferWorkShop fileTransferStructure;
            if (fileTransferQueue.equalsIgnoreCase("deploy")) {
                fileTransferStructure = targetProcess.getFileTransferWorkShopDeploy();
            } else { // if(fileTransferQueue.equalsIgnoreCase("retrieve"))
                fileTransferStructure = targetProcess.getFileTransferWorkShopRetrieve();
            }

            Node namedItem = node.getAttributes().getNamedItem("refid");
            String ftRefId = getNodeExpandedValue(namedItem);
            if (ftRefId == null) {
                throw new org.xml.sax.SAXException(node.getNodeName() +
                    " defined without 'refid' attribute");
            }

            if (ftRefId.equalsIgnoreCase(FILE_TRANSFER_IMPLICT_KEYWORD)) {
                fileTransferStructure.setImplicit(true);
            } else {
                fileTransferStructure.setImplicit(false);
                fileTransferStructure.addFileTransfer(proActiveDescriptor.getFileTransfer(
                        ftRefId));
            }

            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); ++i) {
                Node childNode = childNodes.item(i);
                if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeName = childNode.getNodeName();
                if (nodeName.equals(FILE_TRANSFER_COPY_PROTOCOL_TAG)) {
                    fileTransferStructure.setFileTransferCopyProtocol(childNode.getTextContent());
                } else if (nodeName.equals(FILE_TRANSFER_SRC_INFO_TAG)) {
                    getTransferInfo(true, fileTransferStructure, childNode);
                } else if (nodeName.equals(FILE_TRANSFER_DST_INFO_TAG)) {
                    getTransferInfo(false, fileTransferStructure, childNode);
                }
            }

            return fileTransferStructure;
        }

        private void getTransferInfo(boolean src,
            FileTransferWorkShop fileTransferStructure, Node node)
            throws SAXException {
            String[] parameter = { "prefix", "hostname", "username", "password" };

            for (int i = 0; i < parameter.length; i++) {
                Node namedItem = node.getAttributes().getNamedItem(parameter[i]);
                String t = getNodeExpandedValue(namedItem);
                if (t != null) {
                    if (src) {
                        fileTransferStructure.setFileTransferStructureSrcInfo(parameter[i],
                            t);
                    } else {
                        fileTransferStructure.setFileTransferStructureDstInfo(parameter[i],
                            t);
                    }
                }
            }
        }
    }

    protected class JVMProcessExtractor extends ProcessExtractor {
        public JVMProcessExtractor(Node node, Node context)
            throws XPathExpressionException, SAXException, ProActiveException {
            super(node, context);
            JVMProcess jvmProcess = ((JVMProcess) targetProcess);

            Node namedItem = node.getAttributes().getNamedItem("priority");
            String priority = getNodeExpandedValue(namedItem);
            if (priority != null) {
                ((JVMProcess) targetProcess).setPriority(PriorityLevel.valueOf(
                        priority));
            }

            namedItem = node.getAttributes().getNamedItem("os");
            String os = getNodeExpandedValue(namedItem);
            if (os != null) {
                ((JVMProcess) targetProcess).setOperatingSystem(OperatingSystem.valueOf(
                        os));
            }

            NodeList childNodes = node.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); ++j) {
                Node child = childNodes.item(j);
                if (child.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeName = child.getNodeName();
                if (nodeName.equals(CLASSPATH_TAG)) {
                    String classpath = getPath(child);
                    jvmProcess.setClasspath(classpath);
                } else if (nodeName.equals(BOOT_CLASSPATH_TAG)) {
                    String classpath = getPath(child);
                    jvmProcess.setBootClasspath(classpath);
                } else if (nodeName.equals(JAVA_PATH_TAG)) {
                    String path = getPath(child);
                    jvmProcess.setJavaPath(path);
                } else if (nodeName.equals(POLICY_FILE_TAG)) {
                    String path = getPath(child);
                    jvmProcess.setPolicyFile(path);
                } else if (nodeName.equals(LOG4J_FILE_TAG)) {
                    String path = getPath(child);
                    jvmProcess.setLog4jFile(path);
                } else if (nodeName.equals(PROACTIVE_PROPS_FILE_TAG)) {
                    String path = getPath(child);
                    jvmProcess.setJvmOptions("-Dproactive.configuration=" +
                        path);
                } else if (nodeName.equals(JVMPARAMETERS_TAG)) {
                    String params = getParameters(child);
                    jvmProcess.setJvmOptions(params);
                } else if (nodeName.equals(EXTENDED_JVM_TAG)) {
                    Node overwriteParamsArg = child.getAttributes()
                                                   .getNamedItem("overwriteParameters");
                    if ((overwriteParamsArg != null) &&
                            "yes".equals(getNodeExpandedValue(
                                    overwriteParamsArg))) {
                        jvmProcess.setOverwrite(true);
                    }
                    try {
                        proActiveDescriptor.mapToExtendedJVM((JVMProcess) targetProcess,
                            getNodeExpandedValue(child.getAttributes()
                                                      .getNamedItem("refid")));
                    } catch (ProActiveException e) {
                        throw new SAXException(e);
                    }
                }
            }
        }
    }

    protected class RshProcessExtractor extends ProcessExtractor {
        public RshProcessExtractor(Node node, Node context)
            throws XPathExpressionException, SAXException, ProActiveException {
            super(node, context);
        }
    }

    protected class MapRshProcessExtractor extends RshProcessExtractor {
        public MapRshProcessExtractor(Node node, Node context)
            throws XPathExpressionException, SAXException, ProActiveException {
            super(node, context);

            Node namedItem = node.getAttributes().getNamedItem("parallelize");
            String t = getNodeExpandedValue(namedItem);
            if (t != null) {
                ((MapRshProcess) targetProcess).setParallelization(
                    "parallelize");
            }

            NodeList childNodes = node.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); ++j) {
                Node child = childNodes.item(j);
                if (child.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                if (child.getNodeName().equals(SCRIPT_PATH_TAG)) {
                    String path = getPath(child);
                    ((MapRshProcess) targetProcess).setScriptLocation(path);
                }
            }
        }
    }

    protected class SshProcessExtractor extends ProcessExtractor {
        public SshProcessExtractor(Node node, Node context)
            throws XPathExpressionException, SAXException, ProActiveException {
            super(node, context);
        }
    }

    protected class RloginProcessExtractor extends ProcessExtractor {
        public RloginProcessExtractor(Node node, Node context)
            throws XPathExpressionException, SAXException, ProActiveException {
            super(node, context);
        }
    }

    protected class LoadLevelerProcessExtractor extends ProcessExtractor {
        public LoadLevelerProcessExtractor(Node node, Node context)
            throws XPathExpressionException, SAXException, ProActiveException {
            super(node, context);

            Node taskRepartitionNode = (Node) xpath.evaluate(XMLNS_PREFIX +
                    LOADLEVELER_TASK_REPARTITION_TAG, node, XPathConstants.NODE);
            if (taskRepartitionNode != null) {
                new LoadLevelerTaskRepartitionExtractor(targetProcess,
                    taskRepartitionNode);
            }

            Node optionNode = (Node) xpath.evaluate(XMLNS_PREFIX +
                    LOADLEVELER_OPTIONS_TAG, node, XPathConstants.NODE);
            if (optionNode != null) {
                new LoadLevelerOptionExtractor(targetProcess, optionNode);
            }
        }
    }

    protected class LoadLevelerOptionExtractor {
        public LoadLevelerOptionExtractor(ExternalProcess targetProcess,
            Node node) throws SAXException {
            final NodeList childNodes = node.getChildNodes();
            final LoadLevelerProcess llProcess = (LoadLevelerProcess) targetProcess;

            for (int i = 0; i < childNodes.getLength(); ++i) {
                final Node childNode = childNodes.item(i);
                if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                final String nodeName = childNode.getNodeName();
                final String nodeValue = getNodeExpandedValue(childNode);
                if (nodeName.equals(LL_OPT_WALL_CLOCK_LIMIT)) {
                    llProcess.setWallClockLimit(nodeValue);
                } else if (nodeName.equals(LL_OPT_RESOURCES)) {
                    llProcess.setResources(nodeValue);
                } else if (nodeName.equals(LL_OPT_INITIAL_DIR)) {
                    final String path = getPath(childNode);
                    llProcess.setInitialDir(path);
                } else if (nodeName.equals(LL_OPT_JOB_SUBMISSION_SCRIPT)) {
                    final String path = getPath(childNode);
                    llProcess.setJobSubmissionScriptPath(path);
                } else if (nodeName.equals(LL_OPT_EXECUTABLE)) {
                    final String path = getPath(childNode);
                    llProcess.setExecutable(path);
                } else if (nodeName.equals(LL_OPT_ARGUMENTS)) {
                    llProcess.setArguments(nodeValue);
                } else if (nodeName.equals(LL_OPT_ERROR)) {
                    final String path = getPath(childNode);
                    llProcess.setErrorFile(path);
                } else if (nodeName.equals(LL_OPT_OUTPUT)) {
                    final String path = getPath(childNode);
                    llProcess.setOutputFile(path);
                } else if (nodeName.equals(LL_OPT_ENVIRONMENT)) {
                    llProcess.setTaskEnvironment(nodeValue);
                }
            }
        }
    }

    protected class LoadLevelerTaskRepartitionExtractor {
        public LoadLevelerTaskRepartitionExtractor(
            ExternalProcess targetProcess, Node node)
            throws SAXException, XPathExpressionException {
            final LoadLevelerProcess llProcess = (LoadLevelerProcess) targetProcess;

            final Node simple = (Node) xpath.evaluate(XMLNS_PREFIX +
                    LOADLEVELER_TASK_REPARTITION_TAG_SIMPLE, node,
                    XPathConstants.NODE);
            if (simple != null) {
                final NodeList childNodes = simple.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); ++i) {
                    final Node childNode = childNodes.item(i);
                    if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    final String nodeName = childNode.getNodeName();
                    final String nodeValue = getNodeExpandedValue(childNode);
                    if (nodeName.equals(LL_TASK_REP_NBTASKS)) {
                        llProcess.setNbTasks(nodeValue);
                    }
                    if (nodeName.equals(LL_TASK_REP_CPUS_PER_TASKS)) {
                        llProcess.setCpusPerTasks(nodeValue);
                    }
                    if (nodeName.equals(LL_TASK_REP_TASKS_PER_HOSTS)) {
                        llProcess.setTasksPerHosts(nodeValue);
                    }
                }
            }

            final Node advanced = (Node) xpath.evaluate(XMLNS_PREFIX +
                    LOADLEVELER_TASK_REPARTITION_TAG_ADVANCED, node,
                    XPathConstants.NODE);

            if (advanced != null) {
                final NodeList childNodes = advanced.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); ++i) {
                    final Node childNode = childNodes.item(i);
                    if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    final String nodeName = childNode.getNodeName();
                    final String nodeValue = getNodeExpandedValue(childNode);
                    if (nodeName.equals(LL_TASK_REP_BLOCKING)) {
                        llProcess.setBlocking(nodeValue);
                    } else if (nodeName.equals(LL_TASK_REP_NODE)) {
                        llProcess.setNode(nodeValue);
                    } else if (nodeName.equals(LL_TASK_REP_TASKS_PER_NODE)) {
                        llProcess.setTasksPerNode(nodeValue);
                    } else if (nodeName.equals(LL_TASK_REP_TASK_GEOMETRY)) {
                        llProcess.setTaskGeometry(nodeValue);
                    } else if (nodeName.equals(LL_TASK_REP_TOTAL_TASKS)) {
                        llProcess.setTotalTasks(nodeValue);
                    }
                }
            }
        }
    }

    protected class BSubProcessExtractor extends ProcessExtractor {
        public BSubProcessExtractor(Node node, Node context)
            throws XPathExpressionException, SAXException, ProActiveException {
            super(node, context);

            Node namedItem = node.getAttributes().getNamedItem("interactive");
            String t = getNodeExpandedValue(namedItem);
            if (t != null) {
                ((LSFBSubProcess) targetProcess).setInteractive(t);
            }

            namedItem = node.getAttributes().getNamedItem("queue");
            t = getNodeExpandedValue(namedItem);
            if (t != null) {
                ((LSFBSubProcess) targetProcess).setQueueName(t);
            }

            namedItem = node.getAttributes().getNamedItem("jobname");
            t = getNodeExpandedValue(namedItem);
            if (t != null) {
                ((LSFBSubProcess) targetProcess).setJobname(t);
            }

            Node optionNode = (Node) xpath.evaluate(XMLNS_PREFIX +
                    BSUB_OPTIONS_TAG, node, XPathConstants.NODE);
            if (optionNode != null) {
                new BSubOptionsExtractor(targetProcess, optionNode);
            }
        }

        protected class BSubOptionsExtractor {
            public BSubOptionsExtractor(ExternalProcess targetProcess, Node node)
                throws SAXException {
                NodeList childNodes = node.getChildNodes();
                LSFBSubProcess bSubProcess = (LSFBSubProcess) targetProcess;

                for (int i = 0; i < childNodes.getLength(); ++i) {
                    Node childNode = childNodes.item(i);
                    if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    String nodeName = childNode.getNodeName();
                    if (nodeName.equals(HOST_LIST_TAG)) {
                        String nodeValue = getNodeExpandedValue(childNode);
                        bSubProcess.setHostList(nodeValue);
                    } else if (nodeName.equals(PROCESSOR_TAG)) {
                        String nodeValue = getNodeExpandedValue(childNode);
                        bSubProcess.setProcessorNumber(nodeValue);
                    } else if (nodeName.equals(RES_REQ_TAG)) {
                        String nodeValue = getNodeExpandedValue(childNode.getAttributes()
                                                                         .getNamedItem("value"));
                        bSubProcess.setRes_requirement(nodeValue);
                    } else if (nodeName.equals(SCRIPT_PATH_TAG)) {
                        String path = getPath(childNode);
                        bSubProcess.setScriptLocation(path);
                    }
                }
            }
        }
    }

    protected class GlobusProcessExtractor extends ProcessExtractor {
        public GlobusProcessExtractor(Node node, Node context)
            throws XPathExpressionException, SAXException, ProActiveException {
            super(node, context);

            Node optionNode = (Node) xpath.evaluate(XMLNS_PREFIX +
                    GLOBUS_OPTIONS_TAG, node, XPathConstants.NODE);
            new GlobusOptionsExtractor(targetProcess, optionNode);
        }

        protected class GlobusOptionsExtractor {
            public GlobusOptionsExtractor(ExternalProcess targetProcess,
                Node node) throws SAXException {
                GlobusProcess globusProcess = (GlobusProcess) targetProcess;
                NodeList childNodes = node.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); ++j) {
                    Node child = childNodes.item(j);
                    if (child.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    String nodeValue = getNodeExpandedValue(child);
                    String nodeName = child.getNodeName();
                    if (nodeName.equals(COUNT_TAG)) {
                        globusProcess.setCount(nodeValue);
                    } else if (nodeName.equals(GLOBUS_MAXTIME_TAG)) {
                        globusProcess.setMaxTime(nodeValue);
                    } else if (nodeName.equals(OUTPUT_FILE)) {
                        globusProcess.setStdout(nodeValue);
                    } else if (nodeName.equals(ERROR_FILE)) {
                        globusProcess.setStderr(nodeValue);
                    }
                }
            }
        }
    }

    protected class GliteProcessExtractor extends ProcessExtractor {
        public GliteProcessExtractor(Node node, Node context)
            throws XPathExpressionException, SAXException, ProActiveException {
            super(node, context);

            GLiteProcess gliteProcess = ((GLiteProcess) targetProcess);

            Node namedItem = node.getAttributes().getNamedItem("Type");
            String t = getNodeExpandedValue(namedItem);
            if (t != null) {
                gliteProcess.setJobType(t);
            }
            namedItem = node.getAttributes().getNamedItem("jobType");
            t = getNodeExpandedValue(namedItem);
            if (t != null) {
                gliteProcess.setJobJobType(t);
            }
            namedItem = node.getAttributes().getNamedItem("JDLFileName");
            t = getNodeExpandedValue(namedItem);
            if (t != null) {
                gliteProcess.setFileName(t);
            }
            namedItem = node.getAttributes().getNamedItem("hostname");
            t = getNodeExpandedValue(namedItem);
            if (t != null) {
                gliteProcess.setNetServer(t);
            }
            namedItem = node.getAttributes().getNamedItem("executable");
            t = getNodeExpandedValue(namedItem);
            if (t != null) {
                gliteProcess.setJobExecutable(t);
                gliteProcess.setCommand_path(t);
            }
            namedItem = node.getAttributes().getNamedItem("stdOutput");
            t = getNodeExpandedValue(namedItem);
            if (t != null) {
                gliteProcess.setJobStdOutput(t);
            }
            namedItem = node.getAttributes().getNamedItem("stdInput");
            t = getNodeExpandedValue(namedItem);
            if (t != null) {
                gliteProcess.setJobStdInput(t);
            }
            namedItem = node.getAttributes().getNamedItem("stdError");
            t = getNodeExpandedValue(namedItem);
            if (t != null) {
                gliteProcess.setJobStdError(t);
            }
            namedItem = node.getAttributes().getNamedItem("outputse");
            t = getNodeExpandedValue(namedItem);
            if (t != null) {
                gliteProcess.setJobOutput_se(t);
            }
            namedItem = node.getAttributes().getNamedItem("virtualOrganisation");
            t = getNodeExpandedValue(namedItem);
            if (t != null) {
                gliteProcess.setJobVO(t);
            }
            namedItem = node.getAttributes().getNamedItem("retryCount");
            t = getNodeExpandedValue(namedItem);
            if (t != null) {
                gliteProcess.setJobRetryCount(t);
            }
            namedItem = node.getAttributes().getNamedItem("myProxyServer");
            t = getNodeExpandedValue(namedItem);
            if (t != null) {
                gliteProcess.setJobMyProxyServer(t);
            }
            namedItem = node.getAttributes().getNamedItem("nodeNumber");
            t = getNodeExpandedValue(namedItem);
            if (t != null) {
                gliteProcess.setJobNodeNumber(Integer.parseInt(t));
            }

            NodeList childNodes = node.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); ++j) {
                Node child = childNodes.item(j);
                if (child.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeName = child.getNodeName();
                String nodeValue = getNodeExpandedValue(child);
                if (nodeName.equals(GLITE_ENVIRONMENT_TAG)) {
                    gliteProcess.setJobEnvironment(nodeValue);
                } else if (nodeName.equals(GLITE_REQUIREMENTS_TAG)) {
                    gliteProcess.setJobRequirements(nodeValue);
                } else if (nodeName.equals(GLITE_RANK_TAG)) {
                    gliteProcess.setJobRank(nodeValue);
                } else if (nodeName.equals(GLITE_INPUTDATA_TAG)) {
                    new GliteInputExtractor(gliteProcess, child);
                } else if (nodeName.equals(GLITE_PROCESS_OPTIONS_TAG)) {
                    new GliteOptionsExtractor(gliteProcess, child);
                }
            }
        }

        protected class GliteInputExtractor {
            public GliteInputExtractor(GLiteProcess gliteProcess, Node node)
                throws SAXException {
                Node namedItem = node.getAttributes()
                                     .getNamedItem("dataAccessProtocol");
                String t = getNodeExpandedValue(namedItem);
                if (t != null) {
                    gliteProcess.setJobDataAccessProtocol(t);
                }
                namedItem = node.getAttributes().getNamedItem("storageIndex");
                t = getNodeExpandedValue(namedItem);
                if (t != null) {
                    gliteProcess.setJobStorageIndex(t);
                }
            }
        }

        protected class GliteOptionsExtractor {
            public GliteOptionsExtractor(GLiteProcess gliteProcess, Node node)
                throws SAXException {
                NodeList childNodes = node.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); ++i) {
                    Node childNode = childNodes.item(i);
                    if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    String nodeName = childNode.getNodeName();
                    if (nodeName.equals(GLITE_PATH_TAG)) {
                        String path = getPath(childNode);
                        gliteProcess.setFilePath(path);
                    } else if (nodeName.equals(GLITE_REMOTE_PATH_TAG)) {
                        String path = getPath(childNode);
                        gliteProcess.setRemoteFilePath(path);
                        gliteProcess.setJdlRemote(true);
                    } else if (nodeName.equals(GLITE_CONFIG_TAG)) {
                        String path = getPath(childNode);
                        gliteProcess.setConfigFile(path);
                        gliteProcess.setConfigFileOption(true);
                    } else {
                        String nodeValue = getNodeExpandedValue(childNode);
                        if (nodeName.equals(GLITE_INPUTSANDBOX_TAG)) {
                            String sandbox = nodeValue;
                            StringTokenizer st = new StringTokenizer(sandbox);
                            while (st.hasMoreTokens()) {
                                gliteProcess.addInputSBEntry(st.nextToken());
                            }
                        } else if (nodeName.equals(GLITE_OUTPUTSANDBOX_TAG)) {
                            String sandbox = nodeValue;
                            StringTokenizer st = new StringTokenizer(sandbox);
                            while (st.hasMoreTokens()) {
                                gliteProcess.addOutputSBEntry(st.nextToken());
                            }
                        } else if (nodeName.equals(GLITE_ARGUMENTS_TAG)) {
                            gliteProcess.setJobArgument(nodeValue);
                        }
                    }
                }
            }
        }
    }

    protected class UnicoreProcessExtractor extends ProcessExtractor {
        public UnicoreProcessExtractor(Node node, Node context)
            throws XPathExpressionException, SAXException, ProActiveException {
            super(node, context);
            UnicoreProcess unicoreProcess = ((UnicoreProcess) targetProcess);

            Node namedItem = node.getAttributes().getNamedItem("jobname");
            String t = getNodeExpandedValue(namedItem);
            if (t != null) {
                unicoreProcess.uParam.setUsiteName(t);
            }

            namedItem = node.getAttributes().getNamedItem("keypassword");
            t = getNodeExpandedValue(namedItem);
            if (t != null) {
                unicoreProcess.uParam.setKeyPassword(t);
            }

            namedItem = node.getAttributes().getNamedItem("submitjob");
            t = getNodeExpandedValue(namedItem);
            if (t != null) {
                unicoreProcess.uParam.setSubmitJob(t);
            }

            namedItem = node.getAttributes().getNamedItem("savejob");
            t = getNodeExpandedValue(namedItem);
            if (t != null) {
                unicoreProcess.uParam.setSaveJob(t);
            }

            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); ++i) {
                Node childNode = childNodes.item(i);
                if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeName = childNode.getNodeName();
                if (nodeName.equals(UNICORE_DIR_PATH_TAG)) {
                    String path = getPath(childNode);
                    unicoreProcess.uParam.setUnicoreDir(path);
                } else if (nodeName.equals(UNICORE_KEYFILE_PATH_TAG)) {
                    String path = getPath(childNode);
                    unicoreProcess.uParam.setKeyFilePath(path);
                } else if (nodeName.equals(UNICORE_OPTIONS_TAG)) {
                    NodeList grandChildren = childNode.getChildNodes();
                    for (int j = 0; j < grandChildren.getLength(); ++j) {
                        Node grandChildNode = grandChildren.item(j);
                        if (grandChildNode.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }

                        String grandChildNodeName = grandChildNode.getNodeName();
                        if (grandChildNodeName.equals(UNICORE_USITE_TAG)) {
                            new UnicoreUSiteExtractor(grandChildNode,
                                unicoreProcess);
                        } else if (grandChildNodeName.equals(UNICORE_VSITE_TAG)) {
                            new UnicoreVSiteExtractor(grandChildNode,
                                unicoreProcess);
                        }
                    }
                }
            }
        }

        protected class UnicoreUSiteExtractor {
            public UnicoreUSiteExtractor(Node grandChildNode,
                UnicoreProcess unicoreProcess) throws SAXException {
                Node namedItem = grandChildNode.getAttributes()
                                               .getNamedItem("name");
                String t = getNodeExpandedValue(namedItem);
                if (t != null) {
                    unicoreProcess.uParam.setUsiteName(t);
                }

                namedItem = grandChildNode.getAttributes().getNamedItem("type");
                t = getNodeExpandedValue(namedItem);
                if (t != null) {
                    unicoreProcess.uParam.setUsiteType(t);
                }

                namedItem = grandChildNode.getAttributes().getNamedItem("url");
                t = getNodeExpandedValue(namedItem);
                if (t != null) {
                    unicoreProcess.uParam.setUsiteUrl(t);
                }
            }
        }

        protected class UnicoreVSiteExtractor {
            public UnicoreVSiteExtractor(Node grandChildNode,
                UnicoreProcess unicoreProcess) throws SAXException {
                Node namedItem = grandChildNode.getAttributes()
                                               .getNamedItem("name");
                String t = getNodeExpandedValue(namedItem);
                if (t != null) {
                    unicoreProcess.uParam.setVsiteName(t);
                }

                namedItem = grandChildNode.getAttributes().getNamedItem("nodes");
                t = getNodeExpandedValue(namedItem);
                if (t != null) {
                    unicoreProcess.uParam.setVsiteNodes(Integer.parseInt(t));
                }

                namedItem = grandChildNode.getAttributes()
                                          .getNamedItem("processors");
                t = getNodeExpandedValue(namedItem);
                if (t != null) {
                    unicoreProcess.uParam.setVsiteProcessors(Integer.parseInt(t));
                }

                namedItem = grandChildNode.getAttributes().getNamedItem("memory");
                t = getNodeExpandedValue(namedItem);
                if (t != null) {
                    unicoreProcess.uParam.setVsiteMemory(Integer.parseInt(t));
                }

                namedItem = grandChildNode.getAttributes()
                                          .getNamedItem("runtime");
                t = getNodeExpandedValue(namedItem);
                if (t != null) {
                    unicoreProcess.uParam.setVsiteRuntime(Integer.parseInt(t));
                }

                namedItem = grandChildNode.getAttributes()
                                          .getNamedItem("priority");
                t = getNodeExpandedValue(namedItem);
                if (t != null) {
                    unicoreProcess.uParam.setVsitePriority(t);
                }
            }
        }
    }

    protected class PrunProcessExtractor extends ProcessExtractor {
        public PrunProcessExtractor(Node node, Node context)
            throws XPathExpressionException, SAXException, ProActiveException {
            super(node, context);

            String queueName = getNodeExpandedValue(node.getAttributes()
                                                        .getNamedItem("value"));
            if (queueName != null) {
                ((PrunSubProcess) targetProcess).setQueueName(queueName);
            }

            Node optionNode = (Node) xpath.evaluate(XMLNS_PREFIX +
                    PRUN_OPTIONS_TAG, node, XPathConstants.NODE);
            new PrunOptionsExtractor((PrunSubProcess) targetProcess, optionNode);
        }

        protected class PrunOptionsExtractor {
            public PrunOptionsExtractor(PrunSubProcess prunSubProcess, Node node)
                throws SAXException {
                NodeList childNodes = node.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); ++i) {
                    Node childNode = childNodes.item(i);
                    if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    String nodeName = childNode.getNodeName();
                    String nodeExpandedValue = getNodeExpandedValue(childNode);
                    if (nodeName.equals(HOST_LIST_TAG)) {
                        prunSubProcess.setHostList(nodeExpandedValue);
                    } else if (nodeName.equals(HOSTS_NUMBER_TAG)) {
                        prunSubProcess.setHostsNumber(nodeExpandedValue);
                    } else if (nodeName.equals(PROCESSOR_PER_NODE_TAG)) {
                        prunSubProcess.setProcessorPerNodeNumber(nodeExpandedValue);
                    } else if (nodeName.equals(BOOKING_DURATION_TAG)) {
                        prunSubProcess.setBookingDuration(nodeExpandedValue);
                    } else if (nodeName.equals(OUTPUT_FILE)) {
                        prunSubProcess.setOutputFile(nodeExpandedValue);
                    }
                }
            }
        }
    }

    protected class PbsProcessExtractor extends ProcessExtractor {
        public PbsProcessExtractor(Node node, Node context)
            throws XPathExpressionException, SAXException, ProActiveException {
            super(node, context);
            PBSSubProcess pbsSubProcess = (PBSSubProcess) targetProcess;

            String interactive = getNodeExpandedValue(node.getAttributes()
                                                          .getNamedItem("interactive"));
            if (interactive != null) {
                pbsSubProcess.setInteractive(interactive);
            }

            String queueName = getNodeExpandedValue(node.getAttributes()
                                                        .getNamedItem("queueName"));
            if (queueName != null) {
                pbsSubProcess.setQueueName(queueName);
            }

            Node optionNode = (Node) xpath.evaluate(XMLNS_PREFIX +
                    PBS_OPTIONS_TAG, node, XPathConstants.NODE);
            new PbsOptionsExtractor(pbsSubProcess, optionNode);
        }

        protected class PbsOptionsExtractor {
            public PbsOptionsExtractor(PBSSubProcess pbsSubProcess, Node node)
                throws SAXException {
                NodeList childNodes = node.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); ++i) {
                    Node childNode = childNodes.item(i);
                    if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    String nodeName = childNode.getNodeName();
                    String nodeExpandedValue = getNodeExpandedValue(childNode);
                    if (nodeName.equals(HOST_LIST_TAG)) {
                        pbsSubProcess.setHostList(nodeExpandedValue);
                    } else if (nodeName.equals(HOSTS_NUMBER_TAG)) {
                        pbsSubProcess.setHostsNumber(nodeExpandedValue);
                    } else if (nodeName.equals(PROCESSOR_PER_NODE_TAG)) {
                        pbsSubProcess.setProcessorPerNodeNumber(nodeExpandedValue);
                    } else if (nodeName.equals(BOOKING_DURATION_TAG)) {
                        pbsSubProcess.setBookingDuration(nodeExpandedValue);
                    } else if (nodeName.equals(OUTPUT_FILE)) {
                        pbsSubProcess.setOutputFile(nodeExpandedValue);
                    } else if (nodeName.equals(SCRIPT_PATH_TAG)) {
                        String path = getPath(childNode);
                        pbsSubProcess.setScriptLocation(path);
                    }
                }
            }
        }
    }

    protected class GridEngineProcessExtractor extends ProcessExtractor {
        public GridEngineProcessExtractor(Node node, Node context)
            throws XPathExpressionException, SAXException, ProActiveException {
            super(node, context);
            GridEngineSubProcess gridEngineSubProcess = (GridEngineSubProcess) targetProcess;
            String queueName = getNodeExpandedValue(node.getAttributes()
                                                        .getNamedItem("queue"));
            if (queueName != null) {
                gridEngineSubProcess.setQueueName(queueName);
            }

            Node optionNode = (Node) xpath.evaluate(XMLNS_PREFIX +
                    GRID_ENGINE_OPTIONS_TAG, node, XPathConstants.NODE);
            new GridEngineOptionsExtractor(gridEngineSubProcess, optionNode);
        }

        protected class GridEngineOptionsExtractor {
            public GridEngineOptionsExtractor(
                GridEngineSubProcess gridEngineSubProcess, Node node)
                throws SAXException {
                NodeList childNodes = node.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); ++i) {
                    Node childNode = childNodes.item(i);
                    if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    String nodeName = childNode.getNodeName();
                    String nodeExpandedValue = getNodeExpandedValue(childNode);
                    if (nodeName.equals(HOST_LIST_TAG)) {
                        gridEngineSubProcess.setHostList(nodeExpandedValue);
                    } else if (nodeName.equals(HOSTS_NUMBER_TAG)) {
                        gridEngineSubProcess.setHostsNumber(nodeExpandedValue);
                    } else if (nodeName.equals(PARALLEL_ENVIRONMENT_TAG)) {
                        gridEngineSubProcess.setParallelEnvironment(nodeExpandedValue);
                    } else if (nodeName.equals(BOOKING_DURATION_TAG)) {
                        gridEngineSubProcess.setBookingDuration(nodeExpandedValue);
                        //                    } else if (nodeName.equals(OUTPUT_FILE)) {
                        //                        gridEngineSubProcess.setOutputFile(nodeExpandedValue);
                    } else if (nodeName.equals(SCRIPT_PATH_TAG)) {
                        String path = getPath(childNode);
                        gridEngineSubProcess.setScriptLocation(path);
                    }
                }
            }
        }
    }

    protected class OARProcessExtractor extends ProcessExtractor {
        public OARProcessExtractor(Node node, Node context)
            throws XPathExpressionException, SAXException, ProActiveException {
            super(node, context);

            String interactive = getNodeExpandedValue(node.getAttributes()
                                                          .getNamedItem("interactive"));

            OARSubProcess oarSubProcess = ((OARSubProcess) targetProcess);
            if (interactive != null) {
                oarSubProcess.setInteractive(interactive);
            }

            String queueName = getNodeExpandedValue(node.getAttributes()
                                                        .getNamedItem("queue"));

            if (queueName != null) {
                oarSubProcess.setQueueName(queueName);
            }

            String accessProtocol = getNodeExpandedValue(node.getAttributes()
                                                             .getNamedItem("bookedNodesAccess"));

            if (accessProtocol != null) {
                oarSubProcess.setAccessProtocol(accessProtocol);
            }

            Node optionNode = (Node) xpath.evaluate(XMLNS_PREFIX +
                    OAR_OPTIONS_TAG, node, XPathConstants.NODE);
            new OAROptionsExtractor(oarSubProcess, optionNode);
        }

        protected class OAROptionsExtractor {
            public OAROptionsExtractor(OARSubProcess oarSubProcess, Node node)
                throws SAXException {
                NodeList childNodes = node.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); ++i) {
                    Node childNode = childNodes.item(i);
                    if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    String nodeName = childNode.getNodeName();
                    if (nodeName.equals(OAR_RESOURCE_TAG)) {
                        String nodeExpandedValue = getNodeExpandedValue(childNode);
                        oarSubProcess.setResources(nodeExpandedValue);
                    } else if (nodeName.equals(SCRIPT_PATH_TAG)) {
                        String path = getPath(childNode);
                        oarSubProcess.setScriptLocation(path);
                    }
                }
            }
        }
    }

    protected class OARGridProcessExtractor extends ProcessExtractor {
        public OARGridProcessExtractor(Node node, Node context)
            throws XPathExpressionException, SAXException, ProActiveException {
            super(node, context);
            String queueName = getNodeExpandedValue(node.getAttributes()
                                                        .getNamedItem("queue"));

            OARGRIDSubProcess oarGridSubProcess = ((OARGRIDSubProcess) targetProcess);

            if (queueName != null) {
                oarGridSubProcess.setQueueName(queueName);
            }

            String accessProtocol = getNodeExpandedValue(node.getAttributes()
                                                             .getNamedItem("bookedNodesAccess"));

            if (accessProtocol != null) {
                oarGridSubProcess.setAccessProtocol(accessProtocol);
            }

            Node optionNode = (Node) xpath.evaluate(XMLNS_PREFIX +
                    OARGRID_OPTIONS_TAG, node, XPathConstants.NODE);
            new OARGridOptionsExtractor(oarGridSubProcess, optionNode);
        }

        protected class OARGridOptionsExtractor {
            public OARGridOptionsExtractor(
                OARGRIDSubProcess oarGridSubProcess, Node node)
                throws SAXException {
                NodeList childNodes = node.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); ++i) {
                    Node childNode = childNodes.item(i);
                    if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    String nodeName = childNode.getNodeName();
                    String nodeExpandedValue = getNodeExpandedValue(childNode);
                    if (nodeName.equals(OAR_RESOURCE_TAG)) {
                        oarGridSubProcess.setResources(nodeExpandedValue);
                    } else if (nodeName.equals(OARGRID_WALLTIME_TAG)) {
                        oarGridSubProcess.setWallTime(nodeExpandedValue);
                    } else if (nodeName.equals(SCRIPT_PATH_TAG)) {
                        String path = getPath(childNode);
                        oarGridSubProcess.setScriptLocation(path);
                    }
                }
            }
        }
    }

    protected class MPIProcessExtractor extends ProcessExtractor {
        public MPIProcessExtractor(Node node, Node context)
            throws XPathExpressionException, SAXException, ProActiveException {
            super(node, context);
            String mpiFileName = getNodeExpandedValue(node.getAttributes()
                                                          .getNamedItem("mpiFileName"));
            MPIProcess mpiProcess = ((MPIProcess) targetProcess);
            if (mpiFileName != null) {
                mpiProcess.setMpiFileName(mpiFileName);
            }
            String hostsFileName = getNodeExpandedValue(node.getAttributes()
                                                            .getNamedItem("hostsFileName"));
            if (hostsFileName != null) {
                mpiProcess.setHostsFileName(hostsFileName);
            }
            String mpiCommandOptions = getNodeExpandedValue(node.getAttributes()
                                                                .getNamedItem("mpiCommandOptions"));
            if (mpiCommandOptions != null) {
                mpiProcess.setMpiCommandOptions(mpiCommandOptions);
            }

            Node optionNode = (Node) xpath.evaluate(XMLNS_PREFIX +
                    MPI_PROCESS_OPTIONS_TAG, node, XPathConstants.NODE);
            new MPIOptionsExtractor(mpiProcess, optionNode);
        }

        protected class MPIOptionsExtractor {
            public MPIOptionsExtractor(MPIProcess mpiProcess, Node node)
                throws SAXException {
                NodeList childNodes = node.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); ++i) {
                    Node childNode = childNodes.item(i);
                    if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    String nodeName = childNode.getNodeName();

                    if (nodeName.equals(MPI_LOCAL_PATH_TAG)) {
                        String path = getPath(childNode);
                        mpiProcess.setLocalPath(path);
                    } else if (nodeName.equals(MPI_REMOTE_PATH_TAG)) {
                        String path = getPath(childNode);
                        mpiProcess.setRemotePath(path);
                    } else if (nodeName.equals(PROCESS_NUMBER_TAG)) {
                        String nodeExpandedValue = getNodeExpandedValue(childNode);
                        mpiProcess.setHostsNumber(nodeExpandedValue);
                    }
                }
            }
        }
    }

    protected class DependentProcessSequenceExtractor
        extends BasicProcessExtractor {
        public DependentProcessSequenceExtractor(Node node, Node context)
            throws XPathExpressionException, SAXException, ProActiveException {
            super(node, context);

            NodeList processRefs = (NodeList) xpath.evaluate(XMLNS_PREFIX +
                    PROCESS_REFERENCE_TAG + "/@refid", node,
                    XPathConstants.NODESET);
            for (int i = 0; i < processRefs.getLength(); ++i) {
                Node item = processRefs.item(i);
                proActiveDescriptor.addProcessToSequenceList((DependentListProcess) targetProcess,
                    getNodeExpandedValue(item));
            }

            NodeList serviceRefs = (NodeList) xpath.evaluate(XMLNS_PREFIX +
                    SERVICE_REFERENCE_TAG + "/@refid", node,
                    XPathConstants.NODESET);
            for (int i = 0; i < serviceRefs.getLength(); ++i) {
                Node item = serviceRefs.item(i);
                proActiveDescriptor.addServiceToSequenceList((DependentListProcess) targetProcess,
                    getNodeExpandedValue(item));
            }
        }
    }

    protected class SequentialProcessExtractor extends BasicProcessExtractor {
        public SequentialProcessExtractor(Node node, Node context)
            throws XPathExpressionException, SAXException, ProActiveException {
            super(node, context);
            NodeList processRefs = (NodeList) xpath.evaluate(XMLNS_PREFIX +
                    PROCESS_REFERENCE_TAG + "/@refid", node,
                    XPathConstants.NODESET);
            for (int i = 0; i < processRefs.getLength(); ++i) {
                Node item = processRefs.item(i);
                proActiveDescriptor.addProcessToSequenceList((DependentListProcess) targetProcess,
                    getNodeExpandedValue(item));
            }
        }
    }

    protected class NGProcessExtractor extends ProcessExtractor {
        public NGProcessExtractor(Node node, Node infrastructureContext)
            throws XPathExpressionException, SAXException, ProActiveException {
            super(node, infrastructureContext);

            String jobname = getNodeExpandedValue(node.getAttributes()
                                                      .getNamedItem("jobname"));
            NGProcess ngProcess = ((NGProcess) targetProcess);
            if (jobname != null) {
                ngProcess.setJobname(jobname);
            }

            String queueName = getNodeExpandedValue(node.getAttributes()
                                                        .getNamedItem("queue"));
            if (queueName != null) {
                ngProcess.setQueue(queueName);
            }

            Node optionNode = (Node) xpath.evaluate(XMLNS_PREFIX +
                    NG_OPTIONS_TAG, node, XPathConstants.NODE);
            new NGOptionsExtractor(ngProcess, optionNode);
        }

        protected class NGOptionsExtractor {
            public NGOptionsExtractor(NGProcess ngProcess, Node node)
                throws SAXException {
                NodeList childNodes = node.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); ++i) {
                    Node childNode = childNodes.item(i);
                    if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    String nodeName = childNode.getNodeName();
                    String nodeExpandedValue = getNodeExpandedValue(childNode);

                    if (nodeName.equals(COUNT_TAG)) {
                        ngProcess.setCount(nodeExpandedValue);
                    } else if (nodeName.equals(OUTPUT_FILE)) {
                        ngProcess.setStdout(nodeExpandedValue);
                    } else if (nodeName.equals(ERROR_FILE)) {
                        ngProcess.setStderr(nodeExpandedValue);
                    } else if (nodeName.equals(EXECUTABLE_TAG)) {
                        String path = getPath(childNode);
                        ngProcess.setExecutable(path);
                    }
                }
            }
        }
    }

    protected class ClusterForkProcessExtractor extends ProcessExtractor {
        public ClusterForkProcessExtractor(Node node, Node context)
            throws XPathExpressionException, SAXException, ProActiveException {
            super(node, context);
        }
    }

    protected class ProcessListExtractor extends ProcessExtractor {
        private String heldProcessRegistrationRefId;

        public ProcessListExtractor(Node node, Node context)
            throws XPathExpressionException, SAXException, ProActiveException {
            super(node, context);

            String closeStream = getNodeExpandedValue(node.getAttributes()
                                                          .getNamedItem("closeStream"));

            String fixedName = getNodeExpandedValue(node.getAttributes()
                                                        .getNamedItem("fixedName"));
            String list = getNodeExpandedValue(node.getAttributes()
                                                   .getNamedItem("list"));
            String domain = getNodeExpandedValue(node.getAttributes()
                                                     .getNamedItem("domain"));
            String spadding = getNodeExpandedValue(node.getAttributes()
                                                       .getNamedItem("padding"));
            String hostlist = getNodeExpandedValue(node.getAttributes()
                                                       .getNamedItem("hostlist"));
            String srepeat = getNodeExpandedValue(node.getAttributes()
                                                      .getNamedItem("repeat"));

            int padding = 0;
            int repeat = 1;
            if (spadding != null) {
                padding = Integer.parseInt(spadding);
            }

            if (srepeat != null) {
                repeat = Integer.parseInt(srepeat);
            }

            AbstractListProcessDecorator listProcessDecorator = ((AbstractListProcessDecorator) targetProcess);
            if ((fixedName != null) && (list != null)) {
                listProcessDecorator.setHostConfig(fixedName, list, domain,
                    padding, repeat);
            }

            if (hostlist != null) {
                listProcessDecorator.setHostList(hostlist, domain);
            }
            if (heldProcessRegistrationRefId != null) {
                ExternalProcessDecorator cep = (ExternalProcessDecorator) targetProcess;
                proActiveDescriptor.registerProcess(cep,
                    heldProcessRegistrationRefId);
            }

            if ((closeStream != null) && closeStream.equals("yes")) {
                targetProcess.closeStream();
            }

            String username = getNodeExpandedValue(node.getAttributes()
                                                       .getNamedItem("username"));

            if (username != null) {
                targetProcess.setUsername(username);
            }
        }

        @Override
        protected void holdProcessRegistration(String refid) {
            this.heldProcessRegistrationRefId = refid;
        }
    }

    /////////////////////////////////////////////////////////////////////////
    // utility methods
    // //////////////////////////////
    private String getParameters(Node classpathNode) throws SAXException {
        NodeList childNodes = classpathNode.getChildNodes();

        StringBuffer sb = new StringBuffer();
        boolean firstParameter = true;

        for (int i = 0; i < childNodes.getLength(); ++i) {
            Node subNode = childNodes.item(i);
            if (subNode.getAttributes() == null) {
                continue;
            }
            Node namedItem = subNode.getAttributes().getNamedItem("value");
            String parameter = getNodeExpandedValue(namedItem);
            if (parameter != null) {
                if (!firstParameter) {
                    sb.append(' ');
                } else {
                    firstParameter = false;
                }
                sb.append(parameter);
            }
        }

        return sb.toString().trim();
    }

    private String getPath(Node node) throws SAXException {
        NodeList childNodes = node.getChildNodes();

        StringBuffer sb = new StringBuffer();
        String pathSeparator = File.pathSeparator;

        boolean firstPathComponent = true;
        for (int i = 0; i < childNodes.getLength(); ++i) {
            Node pathNode = childNodes.item(i);
            if (pathNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            String pathElement = expandPath(pathNode);

            if (!firstPathComponent) {
                sb.append(pathSeparator);
            } else {
                firstPathComponent = false;
            }
            sb.append(pathElement);
        }

        return sb.toString().trim();
    }

    private static final String ORIGIN_ATTRIBUTE = "origin";
    private static final String USER_HOME_ORIGIN = "user.home";
    private static final String WORKING_DIRECTORY_ORIGIN = "user.dir";
    private static final String FROM_CLASSPATH_ORIGIN = "user.classpath";

    // private static final String PROACTIVE_ORIGIN = "proactive.home";
    private static final String DEFAULT_ORIGIN = USER_HOME_ORIGIN;
    private static final String VALUE_ATTRIBUTE = "value";
    private static final String userDir = System.getProperty("user.dir");
    private static final String userHome = System.getProperty("user.home");
    private static final String javaHome = System.getProperty("java.home");
    protected Document document;
    private DocumentBuilderFactory domFactory;
    protected SchemaFactory schemaFactory;

    private String expandPath(Node pathNode) throws SAXException {
        String name = pathNode.getNodeName();
        Node valueAttr = pathNode.getAttributes().getNamedItem(VALUE_ATTRIBUTE);
        String value = getNodeExpandedValue(valueAttr);
        if (value == null) {
            return null;
        }

        String origin = getNodeExpandedValue(pathNode.getAttributes()
                                                     .getNamedItem(ORIGIN_ATTRIBUTE));

        if (origin == null) {
            origin = DEFAULT_ORIGIN;
        }

        String res = null;

        if (name.equals(ABS_PATH_TAG)) {
            res = value;
        } else if (name.equals(REL_PATH_TAG)) {
            if (origin.equals(USER_HOME_ORIGIN)) {
                res = resolvePath(userHome, value);
            } else if (origin.equals(WORKING_DIRECTORY_ORIGIN)) {
                res = resolvePath(userDir, value);
                // } else if (origin.equals(PROACTIVE_ORIGIN)) {
                // setResultObject(resolvePath(proActiveDir, value));
            } else if (origin.equals(FROM_CLASSPATH_ORIGIN)) {
                res = resolvePathFromClasspath(value);
            } else {
                throw new org.xml.sax.SAXException(
                    "Relative Path element defined with an unknown origin=" +
                    origin);
            }
        }

        return res;
    }

    private String resolvePath(String origin, String value) {
        java.io.File originDirectory = new java.io.File(origin);

        // in case of relative path, if the user put a / then remove it
        // transparently
        if (value.startsWith("/")) {
            value = value.substring(1);
        }
        java.io.File file = new java.io.File(originDirectory, value);
        return file.getAbsolutePath();
    }

    private String resolvePathFromClasspath(String value) {
        ClassLoader cl = this.getClass().getClassLoader();
        java.net.URL url = cl.getResource(value);
        return url.getPath();
    }

    protected boolean checkNonEmptyString(String s) {
        return (s != null) && (s.length() > 0);
    }

    protected boolean checkNonEmptyNode(Node n) {
        return (n != null) && checkNonEmptyString(n.getNodeValue());
    }

    protected String interpolateVariables(String value)
        throws SAXException {
        if (org.objectweb.proactive.core.xml.VariableContract.xmlproperties != null) {
            value = org.objectweb.proactive.core.xml.VariableContract.xmlproperties.transform(value.trim());
        }
        return value;
    }

    protected String getNodeExpandedValue(Node n) throws SAXException {
        if (n == null) {
            return null;
        }

        if ((n.getNodeType() == Node.ATTRIBUTE_NODE) && checkNonEmptyNode(n)) {
            return interpolateVariables(n.getNodeValue());
        }

        if ((n.getNodeType() == Node.ELEMENT_NODE) &&
                checkNonEmptyString(n.getTextContent())) {
            return interpolateVariables(n.getTextContent().trim());
        }

        return null;
    }

    class ProActiveNamespaceContext implements NamespaceContext {
        public String getNamespaceURI(String prefix) {
            if (prefix == null) {
                throw new NullPointerException("Null prefix");
            } else if ("pa".equals(prefix)) {
                return DESCRIPTOR_NAMESPACE;
            } else if ("pas".equals(prefix)) {
                return SECURITY_NAMESPACE;
            } else if ("xml".equals(prefix)) {
                return XMLConstants.XML_NS_URI;
            }
            return XMLConstants.NULL_NS_URI;
        }

        // This method isn't necessary for XPath processing.
        public String getPrefix(String uri) {
            throw new UnsupportedOperationException();
        }

        // This method isn't necessary for XPath processing either.
        public Iterator getPrefixes(String uri) {
            throw new UnsupportedOperationException();
        }
    }

    private void debugDump(Node topNode) {
        NodeList childNodes = topNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); ++i) {
            Node node = childNodes.item(i);
            System.out.println(node.getNodeName());
            debugDump(node);
        }
    }

    public ProActiveDescriptorImpl getProActiveDescriptor() {
        return proActiveDescriptor;
    }
}
