package org.objectweb.proactive.extensions.gcmdeployment.environment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.core.descriptor.legacyparser.ProActiveDescriptorConstants;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extensions.gcmdeployment.GCMParserHelper;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


class EnvironmentParser {
    private static final String INCLUDE_PROPERTY_FILE = "includePropertyFile";
    private static final String INCLUDE_XML_FILE = "includeXMLFile";
    private Document document;
    private XPath xpath;

    protected List<String> schemas = null;
    protected VariableContractImpl variableContract;
    protected String namespace;
    protected boolean alreadyParsed;

    protected EnvironmentParser(URL descriptor, VariableContractImpl vContract,
            DocumentBuilderFactory domFactory, XPath xpath, String namespace) throws IOException,
            SAXException {
        this(descriptor, vContract, domFactory, xpath, namespace, null);
    }

    protected EnvironmentParser(URL descriptor, VariableContractImpl vContract,
            DocumentBuilderFactory domFactory, XPath xpath, String namespace, List<String> userSchemas)
            throws IOException, SAXException {
        this.xpath = xpath;
        this.namespace = namespace;
        if (vContract == null) {
            vContract = new VariableContractImpl();
        }
        this.variableContract = vContract;

        InputSource inputSource = new InputSource(descriptor.openStream());
        try {
            DocumentBuilder documentBuilder = GCMParserHelper.getNewDocumentBuilder(domFactory);
            document = documentBuilder.parse(inputSource);
        } catch (SAXException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.fatal(e.getMessage());
            throw e;
        }
    }

    protected VariableContractImpl getVariableContract() throws XPathExpressionException, SAXException,
            DOMException, IOException {
        if (!alreadyParsed) {
            parseEnvironment();
        }

        return variableContract;
    }

    protected Map<String, String> getVariableMap() throws XPathExpressionException, SAXException,
            DOMException, IOException {
        return getVariableContract().toMap();
    }

    private void parseEnvironment() throws XPathExpressionException, SAXException, DOMException, IOException {
        alreadyParsed = true;

        NodeList environmentNodes = (NodeList) xpath.evaluate("/*/" +
            GCMParserHelper.elementInNS(namespace, "environment"), document, XPathConstants.NODESET);

        if (environmentNodes.getLength() == 1) {

            Node envNode = environmentNodes.item(0);

            NodeList variableDeclarationNodes = envNode.getChildNodes();

            for (int i = 0; i < variableDeclarationNodes.getLength(); ++i) {
                Node varDeclNode = variableDeclarationNodes.item(i);

                if (varDeclNode.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                String varContractTypeName = varDeclNode.getNodeName();

                if (varContractTypeName.equals(INCLUDE_PROPERTY_FILE)) {
                    String fileLocation = varDeclNode.getAttributes().getNamedItem("location").getNodeValue();
                    fileLocation = variableContract.transform(fileLocation);
                    InputStream is = new FileInputStream(fileLocation);
                    Properties properties = new Properties();
                    properties.load(is);
                    VariableContractType varContractType = VariableContractType
                            .getType(ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_TAG);

                    Enumeration<String> propertiesNames = (Enumeration<String>) properties.propertyNames();
                    while (propertiesNames.hasMoreElements()) {
                        String propertyName = propertiesNames.nextElement();

                        String propertyValue = variableContract.transform(properties
                                .getProperty(propertyName));
                        if (propertyValue == null) {
                            propertyValue = "";
                        }
                        variableContract.setDescriptorVariable(propertyName.toString(), propertyValue,
                                varContractType);
                    }

                } else { // normal variable declaration

                    VariableContractType varContractType = VariableContractType.getType(varContractTypeName);
                    if (varContractType == null) {
                        GCMDeploymentLoggers.GCMD_LOGGER.warn("unknown variable declaration type : " +
                            varContractTypeName);
                        continue;
                    }

                    String varName = GCMParserHelper.getAttributeValue(varDeclNode, "name");

                    String varValue = variableContract.transform(GCMParserHelper.getAttributeValue(
                            varDeclNode, "value"));
                    if (varValue == null) {
                        varValue = "";
                    }
                    variableContract.setDescriptorVariable(varName, varValue, varContractType);
                }

            }
        }
    }
}
