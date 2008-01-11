package org.objectweb.proactive.extra.gcmdeployment.environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class Environment {

    public static InputSource replaceVariables(File descriptor, DocumentBuilder documentBuilder, XPath xpath,
            String namespace) throws IOException, SAXException, XPathExpressionException,
            TransformerException {
        // Get the variable map
        EnvironmentParser environmentParser;
        Map<String, String> variableMap;
        environmentParser = new EnvironmentParser(descriptor, documentBuilder, xpath, namespace);
        variableMap = environmentParser.getVariableMap();

        Document baseDocument = documentBuilder.parse(descriptor);
        EnvironmentTransformer environmentTransformer;
        environmentTransformer = new EnvironmentTransformer(variableMap, baseDocument);

        File tempFile = File.createTempFile(descriptor.getName(), null);
        OutputStream outputStream = new FileOutputStream(tempFile);
        environmentTransformer.transform(outputStream);
        outputStream.close();

        InputSource inputSource = new InputSource(new FileInputStream(tempFile));
        return inputSource;
    }

}
