package org.objectweb.proactive.extensions.gcmdeployment.environment;

import java.io.*;
import java.net.URLConnection;
import java.net.URL;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.extensions.gcmdeployment.GCMParserConstants;
import org.objectweb.proactive.extensions.gcmdeployment.GCMParserHelper;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class Environment {

    public static InputSource replaceVariables(URL descriptor, VariableContractImpl vContract, XPath xpath,
            String namespace) throws IOException, SAXException, XPathExpressionException,
            TransformerException {

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        domFactory.setIgnoringComments(true);

        // Get the variable map
        EnvironmentParser environmentParser;
        Map<String, String> variableMap;
        environmentParser = new EnvironmentParser(descriptor, vContract, domFactory, xpath, namespace);
        variableMap = environmentParser.getVariableMap();

        DocumentBuilder newDocumentBuilder = GCMParserHelper.getNewDocumentBuilder(domFactory);

        Document baseDocument = newDocumentBuilder.parse(descriptor.openStream());

        // sanity check on the document's namespace
        // we have to do this because we have no schema validation at this stage 
        //
        String expectedNamespace = namespace.equals(GCMParserConstants.GCM_APPLICATION_NAMESPACE_PREFIX) ? GCMParserConstants.GCM_APPLICATION_NAMESPACE
                : GCMParserConstants.GCM_DEPLOYMENT_NAMESPACE;
        NamedNodeMap rootNodeAttributes = baseDocument.getFirstChild().getAttributes();
        if (rootNodeAttributes != null) {
            Node attr = rootNodeAttributes.getNamedItem("xmlns");
            if (attr == null || !attr.getNodeValue().equals(expectedNamespace)) {
                if (attr != null && attr.getNodeValue().equals("urn:proactive:deployment:3.3")) {
                    throw new SAXException("descriptor is using old format - expected namespace is " +
                        expectedNamespace);
                } else {
                    throw new SAXException("document has wrong namespace or no namespace - must be in " +
                        expectedNamespace);
                }
            }
        } else {
            throw new SAXException("couldn't check document's namespace");
        }

        EnvironmentTransformer environmentTransformer;
        environmentTransformer = new EnvironmentTransformer(variableMap, baseDocument);

        // We get the file name from the url
        // TODO test it on linux / windows
        File abstractFile = new File(descriptor.getFile());
        File tempFile = File.createTempFile(abstractFile.getName(), null);
        OutputStream outputStream = new FileOutputStream(tempFile);
        environmentTransformer.transform(outputStream);
        outputStream.close();

        InputSource inputSource = new InputSource(new FileInputStream(tempFile));
        return inputSource;
    }

}
