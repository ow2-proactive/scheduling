package org.objectweb.proactive.extra.gcmdeployment.environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserConstants;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


class EnvironmentParser {
    private Document document;
    private XPath xpath;

    protected List<String> schemas = null;
    protected VariableContract variableContract;
    protected String namespace;
    protected boolean alreadyParsed;

    protected EnvironmentParser(File descriptor, VariableContract vContract,
            DocumentBuilderFactory domFactory, XPath xpath, String namespace) throws IOException,
            SAXException {
        this(descriptor, vContract, domFactory, xpath, namespace, null);
    }

    protected EnvironmentParser(File descriptor, VariableContract vContract,
            DocumentBuilderFactory domFactory, XPath xpath, String namespace, List<String> userSchemas)
            throws IOException, SAXException {
        this.xpath = xpath;
        this.namespace = namespace;
        if (vContract == null) {
            vContract = new VariableContract();
        }
        this.variableContract = vContract;

        InputSource inputSource = new InputSource(new FileInputStream(descriptor));
        try {
            DocumentBuilder documentBuilder = GCMParserHelper.getNewDocumentBuilder(domFactory);
            document = documentBuilder.parse(inputSource);
        } catch (SAXException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.fatal(e.getMessage());
            throw e;
        }
    }

    protected VariableContract getVariableContract() throws XPathExpressionException, SAXException {
        if (!alreadyParsed) {
            parseEnvironment();
        }

        return variableContract;
    }

    protected Map<String, String> getVariableMap() throws XPathExpressionException, SAXException {
        return getVariableContract().toMap();
    }

    private void parseEnvironment() throws XPathExpressionException, SAXException {
        alreadyParsed = true;

        NodeList environmentNodes = (NodeList) xpath.evaluate("/*/" +
            GCMParserHelper.elementInNS(namespace, "environment"), document, XPathConstants.NODESET);

        if (environmentNodes.getLength() == 1) {
            for (int i = 0; i < GCMParserConstants.VARIABLES_TAGS.length; i++) {
                VariableContractType varContractType = VariableContractType
                        .getType(GCMParserConstants.VARIABLES_TAGS[i]);
                String expr = GCMParserHelper.elementInNS(namespace, GCMParserConstants.VARIABLES_TAGS[i]);
                processVariables(environmentNodes.item(0), expr, varContractType);
            }
        }
    }

    private void processVariables(Node environmentNode, String expr, VariableContractType varContractType)
            throws XPathExpressionException, SAXException {
        Object result = xpath.evaluate(expr, environmentNode, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);

            String varName = GCMParserHelper.getAttributeValue(node, "name");

            String varValue = variableContract.transform(GCMParserHelper.getAttributeValue(node, "value"));
            if (varValue == null) {
                varValue = "";
            }
            variableContract.setDescriptorVariable(varName, varValue, varContractType);
        }
    }

}
