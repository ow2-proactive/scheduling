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
package org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder;

import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extensions.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extensions.gcmdeployment.PathElement;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationParser;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.TechnicalServicesProperties;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ApplicationParserProactive extends AbstractApplicationParser {
    private static final String XPATH_JAVA = "app:java";
    private static final String XPATH_JVMARG = "app:jvmarg";
    private static final String XPATH_CONFIGURATION = "app:configuration";
    private static final String XPATH_PROACTIVE_CLASSPATH = "app:proactiveClasspath";
    private static final String XPATH_APPLICATION_CLASSPATH = "app:applicationClasspath";
    private static final String XPATH_SECURITY_POLICY = "app:securityPolicy";
    private static final String XPATH_PROACTIVE_SECURITY = "app:proactiveSecurity";
    private static final String XPATH_APPLICATION_POLICY = "app:applicationPolicy";
    private static final String XPATH_RUNTIME_POLICY = "app:runtimePolicy";
    private static final String XPATH_LOG4J_PROPERTIES = "app:log4jProperties";
    private static final String XPATH_USER_PROPERTIES = "app:userProperties";
    protected static final String NODE_NAME = "proactive";
    protected TechnicalServicesProperties appTechnicalServicesProperties;

    @Override
    protected CommandBuilder createCommandBuilder() {
        return new CommandBuilderProActive();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public void parseApplicationNode(Node paNode, GCMApplicationParser applicationParser, XPath xpath)
            throws Exception {
        super.parseApplicationNode(paNode, applicationParser, xpath);

        CommandBuilderProActive commandBuilderProActive = (CommandBuilderProActive) commandBuilder;

        String relPath = GCMParserHelper.getAttributeValue(paNode, "relpath");
        String base = GCMParserHelper.getAttributeValue(paNode, "base");
        commandBuilderProActive.setProActivePath(relPath, base);

        try {

            Node techServicesNode = (Node) xpath.evaluate(XPATH_TECHNICAL_SERVICES, paNode,
                    XPathConstants.NODE);
            if (techServicesNode != null) {
                appTechnicalServicesProperties = GCMParserHelper.parseTechnicalServicesNode(xpath,
                        techServicesNode);
            } else {
                appTechnicalServicesProperties = new TechnicalServicesProperties();
            }

            // parse configuration
            //
            Node configNode = (Node) xpath.evaluate(XPATH_CONFIGURATION, paNode, XPathConstants.NODE);

            if (configNode != null) {
                parseProActiveConfiguration(xpath, commandBuilderProActive, configNode);
            }

            commandBuilderProActive.setVirtualNodes(applicationParser.getVirtualNodes());
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMA_LOGGER.fatal(e.getMessage(), e);
        }
    }

    protected void parseProActiveConfiguration(XPath xpath, CommandBuilderProActive commandBuilderProActive,
            Node configNode) throws XPathExpressionException {
        // Optional: java
        Node javaNode = (Node) xpath.evaluate(XPATH_JAVA, configNode, XPathConstants.NODE);
        if (javaNode != null) {
            PathElement pe = GCMParserHelper.parsePathElementNode(javaNode);
            commandBuilderProActive.setJavaPath(pe);
        }

        Node classPathNode;
        // Optional: proactiveClasspath
        classPathNode = (Node) xpath.evaluate(XPATH_PROACTIVE_CLASSPATH, configNode, XPathConstants.NODE);
        if (classPathNode != null) {
            String type = GCMParserHelper.getAttributeValue(classPathNode, "type");
            List<PathElement> proactiveClassPath = GCMParserHelper.parseClasspath(xpath, classPathNode);
            commandBuilderProActive.setProActiveClasspath(proactiveClassPath);
            if ("overwrite".equals(type)) {
                commandBuilderProActive.setOverwriteClasspath(true);
            } else {
                commandBuilderProActive.setOverwriteClasspath(false);
            }
        }

        // Optional: applicationClasspath
        classPathNode = (Node) xpath.evaluate(XPATH_APPLICATION_CLASSPATH, configNode, XPathConstants.NODE);
        if (classPathNode != null) {
            List<PathElement> applicationClassPath = GCMParserHelper.parseClasspath(xpath, classPathNode);
            commandBuilderProActive.setApplicationClasspath(applicationClassPath);
        }

        // Optional: security policy
        Node securityPolicyNode = (Node) xpath.evaluate(XPATH_SECURITY_POLICY, configNode,
                XPathConstants.NODE);
        if (securityPolicyNode != null) {
            PathElement pathElement = GCMParserHelper.parsePathElementNode(securityPolicyNode);
            commandBuilderProActive.setSecurityPolicy(pathElement);
        }

        Node applicationSecurityPolicyNode = (Node) xpath.evaluate(XPATH_PROACTIVE_SECURITY + "/" +
            XPATH_APPLICATION_POLICY, configNode, XPathConstants.NODE);
        if (applicationSecurityPolicyNode != null) {
            PathElement pathElement = GCMParserHelper.parsePathElementNode(applicationSecurityPolicyNode);
            commandBuilderProActive.setApplicationPolicy(pathElement);
        }

        Node runtimeSecurityPolicyNode = (Node) xpath.evaluate(XPATH_PROACTIVE_SECURITY + "/" +
            XPATH_RUNTIME_POLICY, configNode, XPathConstants.NODE);
        if (runtimeSecurityPolicyNode != null) {
            PathElement pathElement = GCMParserHelper.parsePathElementNode(runtimeSecurityPolicyNode);
            commandBuilderProActive.setRuntimePolicy(pathElement);
        }

        // Optional: log4j properties
        Node log4jPropertiesNode = (Node) xpath.evaluate(XPATH_LOG4J_PROPERTIES, configNode,
                XPathConstants.NODE);
        if (log4jPropertiesNode != null) {
            PathElement pathElement = GCMParserHelper.parsePathElementNode(log4jPropertiesNode);
            commandBuilderProActive.setLog4jProperties(pathElement);
        }

        // Optional: user properties
        Node userPropertiesNode = (Node) xpath.evaluate(XPATH_USER_PROPERTIES, configNode,
                XPathConstants.NODE);
        if (userPropertiesNode != null) {
            PathElement pathElement = GCMParserHelper.parsePathElementNode(userPropertiesNode);
            commandBuilderProActive.setUserProperties(pathElement);
        }

        // Optional: jvmarg
        NodeList jvmargNodes = (NodeList) xpath.evaluate(XPATH_JVMARG, configNode, XPathConstants.NODESET);
        for (int i = 0; i < jvmargNodes.getLength(); i++) {
            String jvmarg = GCMParserHelper.getAttributeValue(jvmargNodes.item(i), "value");
            commandBuilderProActive.addJVMArg(jvmarg);
        }
    }

    public TechnicalServicesProperties getTechnicalServicesProperties() {
        return appTechnicalServicesProperties;
    }

}
