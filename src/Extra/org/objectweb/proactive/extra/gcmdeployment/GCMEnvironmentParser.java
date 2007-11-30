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
package org.objectweb.proactive.extra.gcmdeployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentParserImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class GCMEnvironmentParser implements GCMParserConstants {
    protected Document document;
    protected DocumentBuilderFactory domFactory;
    protected XPath xpath;
    protected DocumentBuilder documentBuilder;
    protected List<String> schemas;
    protected VariableContract variableContract;
    private static final String XPATH_ENVIRONMENT = "pa:/GCM*/pa:environment";

    public GCMEnvironmentParser(File descriptor)
        throws IOException, SAXException {
        variableContract = null;

        setup();

        InputSource inputSource = new InputSource(new FileInputStream(
                    descriptor));
        try {
            document = documentBuilder.parse(inputSource);
        } catch (SAXException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.fatal(e.getMessage());
            throw e;
        }
    }

    public VariableContract getVariableContract()
        throws XPathExpressionException, SAXException {
        if (variableContract == null) {
            variableContract = new VariableContract();
            parseEnvironment();
        }

        return variableContract;
    }

    public Map<String, String> getVariableMap()
        throws XPathExpressionException, SAXException {
        return getVariableContract().toMap();
    }

    protected void setup() throws IOException {
        domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        domFactory.setValidating(true);
        domFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);

        String deploymentSchema = GCMDeploymentParserImpl.class.getClass()
                                                               .getResource(DEPLOYMENT_DESC_LOCATION)
                                                               .toString();

        String commonTypesSchema = GCMDeploymentParserImpl.class.getClass()
                                                                .getResource(COMMON_TYPES_LOCATION)
                                                                .toString();

        String extensionSchemas = GCMDeploymentParserImpl.class.getClass()
                                                               .getResource(EXTENSION_SCHEMAS_LOCATION)
                                                               .toString();

        schemas.add(0, extensionSchemas);
        schemas.add(0, deploymentSchema);
        //        schemas.add(0, commonTypesSchema); // not needed - it is included by the deployment schema
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

    protected void parseEnvironment()
        throws XPathExpressionException, SAXException {
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
        VariableContractType varContractType)
        throws XPathExpressionException, SAXException {
        Object result = xpath.evaluate(expr, environmentNode,
                XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);

            String varName = GCMParserHelper.getAttributeValue(node, "name");

            String varValue = variableContract.transform(GCMParserHelper.getAttributeValue(
                        node, "value"));

            variableContract.setDescriptorVariable(varName, varValue,
                varContractType);
        }
    }
}
