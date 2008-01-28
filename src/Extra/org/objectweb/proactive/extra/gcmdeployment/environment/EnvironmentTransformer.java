package org.objectweb.proactive.extra.gcmdeployment.environment;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


class EnvironmentTransformer {
    Map<String, String> vmap;
    protected Document document;

    public EnvironmentTransformer(Map<String, String> vmap, Document document) {
        this.vmap = vmap;
        this.document = document;
    }

    public void transform(OutputStream output) throws XPathExpressionException, SAXException,
            TransformerException {
        String[] nameList = vmap.keySet().toArray(new String[0]);
        String[] valueList = new String[nameList.length];
        for (int i = 0; i < nameList.length; i++) {
            valueList[i] = vmap.get(nameList[i]);
        }

        // Escape \ and $
        for (int i = 0; i < valueList.length; i++) {
            valueList[i] = valueList[i].replaceAll("\\\\", "\\\\\\\\");
            valueList[i] = valueList[i].replaceAll("\\$", "\\\\\\$");
        }

        System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
        DOMSource domSource = new DOMSource(document);
        TransformerFactory tfactory = TransformerFactory.newInstance();

        InputStream variablesIS = this.getClass().getResourceAsStream("variables.xsl");
        Source stylesheetSource = new StreamSource(variablesIS);

        Transformer transformer = null;
        try {
            transformer = tfactory.newTransformer(stylesheetSource);
            transformer.setParameter("nameList", nameList);
            transformer.setParameter("valueList", valueList);
            StreamResult result = new StreamResult(output);
            transformer.transform(domSource, result);
        } catch (TransformerException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.fatal(e.getMessage());
            throw e;
        }
    }
}
