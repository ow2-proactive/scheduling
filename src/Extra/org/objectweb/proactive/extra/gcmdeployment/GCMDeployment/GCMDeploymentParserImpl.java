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
package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import static org.objectweb.proactive.core.mop.Utils.makeDeepCopy;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.BridgeParsers.BridgeOARSHParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.BridgeParsers.BridgeParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.BridgeParsers.BridgeRSHParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.BridgeParsers.BridgeSSHParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers.GroupARCParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers.GroupCGSPParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers.GroupFuraParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers.GroupGLiteParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers.GroupGlobusParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers.GroupGridBusParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers.GroupGridEngineParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers.GroupLSFParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers.GroupLoadLevelerParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers.GroupOARParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers.GroupOARSHParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers.GroupPBSParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers.GroupParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers.GroupPrunParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers.GroupRSHParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers.GroupSSHParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.GCMDescriptorProcessor;
import org.objectweb.proactive.extra.gcmdeployment.GCMEnvironmentParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extra.gcmdeployment.process.Bridge;
import org.objectweb.proactive.extra.gcmdeployment.process.Group;
import org.objectweb.proactive.extra.gcmdeployment.process.HostInfo;
import org.objectweb.proactive.extra.gcmdeployment.process.bridge.AbstractBridge;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.objectweb.proactive.extra.gcmdeployment.process.hostinfo.HostInfoImpl;
import org.objectweb.proactive.extra.gcmdeployment.process.hostinfo.Tool;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class GCMDeploymentParserImpl implements GCMDeploymentParser {
    private static final String PA_HOST = GCM_DESCRIPTOR_NAMESPACE_PREFIX +
        "host";
    private static final String PA_GROUP = GCM_DESCRIPTOR_NAMESPACE_PREFIX +
        "group";
    private static final String PA_BRIDGE = GCM_DESCRIPTOR_NAMESPACE_PREFIX +
        "bridge";
    private static final String XPATH_GCMDEPLOYMENT = "/pa:GCMDeployment/";
    private static final String XPATH_INFRASTRUCTURE = XPATH_GCMDEPLOYMENT +
        "pa:infrastructure";
    private static final String XPATH_RESOURCES = XPATH_GCMDEPLOYMENT +
        "pa:resources";
    private static final String XPATH_ENVIRONMENT = XPATH_GCMDEPLOYMENT +
        "pa:environment";
    private static final String XPATH_TOOL = "pa:tool";
    private static final String XPATH_HOME_DIRECTORY = "pa:homeDirectory";
    private static final String XPATH_BRIDGES = "pa:bridges/*";
    private static final String XPATH_GROUPS = "pa:groups/*";
    private static final String XPATH_HOSTS = "pa:hosts/pa:host";
    private static final String XPATH_HOST = PA_HOST;
    private static final String XPATH_DESCRIPTOR_VARIABLE = "pa:descriptorVariable";
    protected Document document;
    protected DocumentBuilderFactory domFactory;
    protected XPath xpath;
    protected DocumentBuilder documentBuilder;
    protected List<String> schemas;
    protected Map<String, GroupParser> groupParserMap;
    protected Map<String, BridgeParser> bridgeParserMap;
    protected GCMDeploymentInfrastructure infrastructure;

    //    protected GCMDeploymentEnvironment environment;
    protected GCMDeploymentResources resources;
    private VariableContract variableContract;
    private boolean parsedResource = false;
    private boolean parsedInfrastructure = false;
    private File descriptor;

    public GCMDeploymentParserImpl(File descriptor)
        throws IOException, SAXException {
        this(descriptor, null);
    }

    public GCMDeploymentParserImpl(File descriptor, List<String> userSchemas)
        throws RuntimeException, SAXException, IOException {
        this.descriptor = descriptor;
        infrastructure = new GCMDeploymentInfrastructure();
        resources = new GCMDeploymentResources();
        groupParserMap = new HashMap<String, GroupParser>();
        bridgeParserMap = new HashMap<String, BridgeParser>();
        variableContract = new VariableContract();
        schemas = (userSchemas != null) ? new ArrayList<String>(userSchemas)
                                        : new ArrayList<String>();

        setup();
        registerDefaultGroupParsers();
        registerUserGroupParsers();
        registerDefaultBridgeParsers();
        registerUserBridgeParsers();
        try {
            // process variables first
            GCMEnvironmentParser environmentParser = new GCMEnvironmentParser(descriptor);

            Map<String, String> variableMap = environmentParser.getVariableMap();

            InputSource inputSource = new InputSource(new FileInputStream(
                        descriptor));

            Document baseDocument = documentBuilder.parse(inputSource);

            GCMDescriptorProcessor descriptorProcessor = new GCMDescriptorProcessor(variableMap,
                    baseDocument);

            // this blocks - use temp file until I figure it out
            //
            //            PipedOutputStream pipedOutputStream = new PipedOutputStream();
            //
            //            InputSource inputSource = new InputSource(new PipedInputStream(
            //            		pipedOutputStream));
            //
            //            descriptorProcessor.transform(pipedOutputStream);
            //            
            File tempFile = File.createTempFile(descriptor.getName(), "tmp");

            FileOutputStream outputStream = new FileOutputStream(tempFile);
            descriptorProcessor.transform(outputStream);
            outputStream.close();

            InputSource processedInputSource = new InputSource(new FileInputStream(
                        tempFile));

            documentBuilder = domFactory.newDocumentBuilder();
            documentBuilder.setErrorHandler(new GCMParserHelper.MyDefaultHandler());

            document = documentBuilder.parse(processedInputSource);
        } catch (Exception e) {
            String msg = "parsing problem with document " +
                descriptor.getCanonicalPath();
            GCMDeploymentLoggers.GCMD_LOGGER.fatal(msg + " - " +
                e.getMessage());
            throw new SAXException(msg, e);
        }
    }

    protected void registerDefaultGroupParsers() {
        registerGroupParser(new GroupARCParser());
        registerGroupParser(new GroupCGSPParser());
        registerGroupParser(new GroupFuraParser());
        registerGroupParser(new GroupGLiteParser());
        registerGroupParser(new GroupGlobusParser());
        registerGroupParser(new GroupGridBusParser());
        registerGroupParser(new GroupGridEngineParser());
        registerGroupParser(new GroupLSFParser());
        registerGroupParser(new GroupLoadLevelerParser());
        registerGroupParser(new GroupOARParser());
        registerGroupParser(new GroupOARSHParser());
        registerGroupParser(new GroupPBSParser());
        registerGroupParser(new GroupPrunParser());
        registerGroupParser(new GroupRSHParser());
        registerGroupParser(new GroupSSHParser());
        // TODO add other group parsers here
    }

    protected void registerDefaultBridgeParsers() {
        registerBridgeParser(new BridgeSSHParser());
        registerBridgeParser(new BridgeRSHParser());
        registerBridgeParser(new BridgeOARSHParser());
        // TODO add other bridge parsers here
    }

    /**
     * Override this
     */
    protected void registerUserGroupParsers() {
    }

    /**
     * Override this
     */
    protected void registerUserBridgeParsers() {
    }

    public void setup() throws IOException {
        //    	System.setProperty("jaxp.debug", "1");
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
            "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");

        domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        domFactory.setValidating(true);
        domFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);

        String deploymentSchema = GCMDeploymentParserImpl.class.getClass()
                                                               .getResource(DEPLOYMENT_DESC_LOCATION)
                                                               .getFile();

        String commonTypesSchema = GCMDeploymentParserImpl.class.getClass()
                                                                .getResource(COMMON_TYPES_LOCATION)
                                                                .getFile();

        String extensionSchemas = GCMDeploymentParserImpl.class.getClass()
                                                               .getResource(EXTENSION_SCHEMAS_LOCATION)
                                                               .getFile();

        // DO NOT change the order here, it would break validation
        //
        schemas.add(deploymentSchema);
        schemas.add(extensionSchemas);
        //        schemas.add(commonTypesSchema); // not needed - it is included by the deployment schema
        domFactory.setAttribute(JAXP_SCHEMA_SOURCE, schemas.toArray());

        try {
            documentBuilder = domFactory.newDocumentBuilder();
            documentBuilder.setErrorHandler(new GCMParserHelper.MyDefaultHandler());

            XPathFactory factory = XPathFactory.newInstance();
            xpath = factory.newXPath();
            xpath.setNamespaceContext(new GCMParserHelper.ProActiveNamespaceContext(
                    GCM_DESCRIPTOR_NAMESPACE));
        } catch (ParserConfigurationException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.fatal(e.getMessage());
        }
    }

    public void parseEnvironment() throws XPathExpressionException {
        Node environmentNode = (Node) xpath.evaluate(XPATH_ENVIRONMENT,
                document, XPathConstants.NODE);

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
            processVariables(environmentNode, pairs[i][0], varContractType);
        }
    }

    private void processVariables(Node environmentNode, String expr,
        VariableContractType varContractType) throws XPathExpressionException {
        Object result = xpath.evaluate(expr, environmentNode,
                XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);

            String varName = GCMParserHelper.getAttributeValue(node, "name");

            String varValue = GCMParserHelper.getAttributeValue(node, "value");

            variableContract.setDescriptorVariable(varName, varValue,
                varContractType);
        }
    }

    public void parseResources() throws XPathExpressionException, IOException {
        if (parsedResource) {
            throw new IllegalStateException(
                "parseResources can only be called once");
        }

        if (!parsedInfrastructure) {
            parseInfrastructure();
        }

        Node resourcesNode = (Node) xpath.evaluate(XPATH_RESOURCES, document,
                XPathConstants.NODE);

        NodeList childNodes = resourcesNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); ++i) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                parseResourceNode(childNode);
            }
        }

        parsedResource = true;
    }

    protected void parseResourceNode(Node resourceNode)
        throws XPathExpressionException, IOException {
        String refid = GCMParserHelper.getAttributeValue(resourceNode, "refid");

        String nodeName = resourceNode.getNodeName();

        if (nodeName.equals(PA_BRIDGE)) {
            Bridge bridge = getBridge(refid);
            if (bridge == null) {
                throw new RuntimeException("no bridge with refid " + refid +
                    " has been defined");
            }
            parseBridgeResource(resourceNode, bridge);
            resources.addBridge(bridge);
        } else if (nodeName.equals(PA_GROUP)) {
            Group group = getGroup(refid);
            if (group == null) {
                throw new RuntimeException("no group with refid " + refid +
                    " has been defined");
            }
            parseGroupResource(resourceNode, group);
            resources.addGroup(group);
        } else if (nodeName.equals(PA_HOST)) {
            HostInfo hostInfo = getHostInfo(refid);
            if (hostInfo == null) {
                throw new RuntimeException("no host with refid " + refid +
                    " has been defined");
            }

            resources.setHostInfo(hostInfo);
        }
    }

    protected HostInfo getHostInfo(String refid) throws IOException {
        HostInfo hostInfo = infrastructure.getHosts().get(refid);
        return (HostInfo) makeDeepCopy(hostInfo);
    }

    protected Group getGroup(String refid) throws IOException {
        Group group = infrastructure.getGroups().get(refid);
        return (Group) makeDeepCopy(group);
    }

    protected Bridge getBridge(String refid) throws IOException {
        Bridge bridge = infrastructure.getBridges().get(refid);
        return (Bridge) makeDeepCopy(bridge);
    }

    protected void parseGroupResource(Node resourceNode, Group group)
        throws XPathExpressionException, IOException {
        Node hostNode = (Node) xpath.evaluate(XPATH_HOST, resourceNode,
                XPathConstants.NODE);

        String refid = GCMParserHelper.getAttributeValue(hostNode, "refid");

        // FIXME glaurent XSD does not enforce keyref integrity so a check is needed to see if refid exist or not
        HostInfo hostInfo = getHostInfo(refid);
        group.setHostInfo(hostInfo);
    }

    protected void parseBridgeResource(Node resourceNode, Bridge bridge)
        throws IOException, XPathExpressionException {
        NodeList childNodes = resourceNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); ++i) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            String childNodeName = childNode.getNodeName();
            String childRefId = GCMParserHelper.getAttributeValue(childNode,
                    "refid");

            if (childNodeName.equals("pa:group")) {
                Group group = getGroup(childRefId);
                parseGroupResource(childNode, group);
                bridge.addGroup(group);
            } else if (childNodeName.equals("pa:host")) {
                HostInfo hostInfo = getHostInfo(childRefId);
                bridge.setHostInfo(hostInfo);
            } else if (childNodeName.equals("pa:bridge")) {
                Bridge childBridge = getBridge(childRefId);
                parseBridgeResource(childNode, childBridge);
                bridge.addBridge(childBridge);
            }
        }
    }

    public void parseInfrastructure() throws XPathExpressionException {
        if (parsedInfrastructure) {
            throw new IllegalStateException(
                "parseInfrastructure can only be called once");
        }

        Node infrastructureNode = (Node) xpath.evaluate(XPATH_INFRASTRUCTURE,
                document, XPathConstants.NODE);

        //
        // Hosts
        //
        NodeList hosts = (NodeList) xpath.evaluate(XPATH_HOSTS,
                infrastructureNode, XPathConstants.NODESET);

        for (int i = 0; i < hosts.getLength(); ++i) {
            HostInfo hostInfo = parseHostNode(hosts.item(i));
            infrastructure.addHost(hostInfo);
        }

        //
        // Groups
        //
        NodeList groups = (NodeList) xpath.evaluate(XPATH_GROUPS,
                infrastructureNode, XPathConstants.NODESET);

        for (int i = 0; i < groups.getLength(); ++i) {
            Node groupNode = groups.item(i);
            GroupParser groupParser = groupParserMap.get(groupNode.getNodeName());
            if (groupParser == null) {
                GCMDeploymentLoggers.GCMD_LOGGER.warn(
                    "No group parser registered for node <" +
                    groupNode.getNodeName() + ">");
            } else {
                AbstractGroup group = groupParser.parseGroupNode(groupNode,
                        xpath);
                infrastructure.addGroup(group);
            }
        }

        //
        // Bridges
        //
        NodeList bridges = (NodeList) xpath.evaluate(XPATH_BRIDGES,
                infrastructureNode, XPathConstants.NODESET);

        for (int i = 0; i < bridges.getLength(); ++i) {
            Node bridgeNode = bridges.item(i);
            BridgeParser bridgeParser = bridgeParserMap.get(bridgeNode.getNodeName());
            if (bridgeParser == null) {
                GCMDeploymentLoggers.GCMD_LOGGER.warn(
                    "No bridge parser registered for node <" +
                    bridgeNode.getNodeName() + ">");
            } else {
                AbstractBridge bridge = bridgeParser.parseBridgeNode(bridgeNode,
                        xpath);
                infrastructure.addBrige(bridge);
            }
        }

        parsedInfrastructure = true;
    }

    public void registerGroupParser(GroupParser groupParser) {
        if (groupParserMap.containsKey(groupParser.getNodeName())) {
            GCMDeploymentLoggers.GCMD_LOGGER.error("Group parser for '" +
                groupParser.getNodeName() + "' already registered");
        }
        groupParserMap.put(groupParser.getNodeName(), groupParser);
    }

    public void registerBridgeParser(BridgeParser bridgeParser) {
        if (bridgeParserMap.containsKey(bridgeParser.getNodeName())) {
            GCMDeploymentLoggers.GCMD_LOGGER.error("Bridge parser for '" +
                bridgeParser.getNodeName() + "' already registered");
        }
        bridgeParserMap.put(bridgeParser.getNodeName(), bridgeParser);
    }

    protected HostInfo parseHostNode(Node hostNode)
        throws XPathExpressionException {
        HostInfoImpl hostInfo = new HostInfoImpl();

        String id = GCMParserHelper.getAttributeValue(hostNode, "id");
        hostInfo.setId(id);

        String os = GCMParserHelper.getAttributeValue(hostNode, "os");
        if (os.equals("unix")) {
            hostInfo.setOs(OperatingSystem.unix);
        } else if (os.equals("windows")) {
            hostInfo.setOs(OperatingSystem.windows);
        }

        String hostCapacityStr = GCMParserHelper.getAttributeValue(hostNode,
                "hostCapacity");
        if (hostCapacityStr != null) {
            hostInfo.setHostCapacity(Integer.parseInt(hostCapacityStr));
        }

        String vmCapacityStr = GCMParserHelper.getAttributeValue(hostNode,
                "vmCapacity");
        if (vmCapacityStr != null) {
            hostInfo.setVmCapacity(Integer.parseInt(vmCapacityStr));
        }

        String username = GCMParserHelper.getAttributeValue(hostNode, "username");
        if (username != null) {
            hostInfo.setUsername(username);
        }

        Node homeDirectoryNode = (Node) xpath.evaluate(XPATH_HOME_DIRECTORY,
                hostNode, XPathConstants.NODE);

        if (homeDirectoryNode != null) {
            hostInfo.setHomeDirectory(GCMParserHelper.getAttributeValue(
                    homeDirectoryNode, "relpath"));
        }

        NodeList toolNodes = (NodeList) xpath.evaluate(XPATH_TOOL, hostNode,
                XPathConstants.NODESET);

        for (int i = 0; i < toolNodes.getLength(); ++i) {
            Node toolNode = toolNodes.item(i);
            Tool tool = new Tool(GCMParserHelper.getAttributeValue(toolNode,
                        "id"),
                    GCMParserHelper.getAttributeValue(toolNode, "path"));
            hostInfo.addTool(tool);
        }

        return hostInfo;
    }

    public VariableContract getEnvironment() {
        return variableContract;
    }

    public GCMDeploymentInfrastructure getInfrastructure() {
        if (!parsedInfrastructure) {
            try {
                parseInfrastructure();
            } catch (XPathExpressionException e) {
                GCMDeploymentLoggers.GCMD_LOGGER.fatal(e.getMessage());
            }
        }
        return infrastructure;
    }

    public GCMDeploymentResources getResources() {
        if (!parsedResource) {
            try {
                parseResources();
            } catch (XPathExpressionException e) {
                GCMDeploymentLoggers.GCMD_LOGGER.fatal(e.getMessage());
            } catch (IOException e) {
                GCMDeploymentLoggers.GCMD_LOGGER.fatal(e.getMessage());
            }
        }

        return resources;
    }

    public String getDescriptorFilePath() {
        try {
            return descriptor.getCanonicalPath();
        } catch (IOException e) {
            return "";
        }
    }
}
