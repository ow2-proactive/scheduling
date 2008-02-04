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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


public class GCMParserHelper implements GCMParserConstants {
    static public String getAttributeValue(Node node, String attributeName) {
        Node namedItem = node.getAttributes().getNamedItem(attributeName);
        return (namedItem != null) ? namedItem.getNodeValue() : null;
    }

    static public String getElementValue(Node node) {
        if ((node != null) && (node.getTextContent() != null)) {
            return node.getTextContent().trim();
        }

        return null;
    }

    static public class MyDefaultHandler extends DefaultHandler {
        @Override
        public void warning(SAXParseException e) throws SAXParseException {
            //            System.err.println("Warning Line " + e.getLineNumber() + ": " +
            //                e.getMessage() + "\n");
            throw e;
        }

        @Override
        public void error(SAXParseException e) throws SAXParseException {
            //            errMessage = new String("Error Line " + e.getLineNumber() + ": " +
            //                    e.getMessage() + "\n");
            //            System.err.println(errMessage);
            throw e;
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXParseException {
            //            errMessage = new String("Error Line " + e.getLineNumber() + ": " +
            //                    e.getMessage() + "\n");
            //            System.err.println(errMessage);
            throw e;
        }
    }

    /**
     * Although the descriptors do not use a namespace prefix, JAXP's xpath queries have to use one
     * (this is a limitation of jaxp).
     * 
     * For instance, given the following document part :
     * &lt;resource&gt;
     *   &lt;host&gt;
     *   ...
     *   &lt;/host&gt;
     * &lt;/resource&gt;
     * 
     * The query to fetch the 'host' node will be "dep:resource/dep:host".
     *   
     * @author glaurent
     *
     */
    static public class ProActiveNamespaceContext implements NamespaceContext {

        public ProActiveNamespaceContext() {
        }

        public String getNamespaceURI(String prefix) {
            if (prefix == null) {
                throw new NullPointerException("Null prefix");
            } else if (GCM_APPLICATION_NAMESPACE_PREFIX.equals(prefix)) {
                return GCM_APPLICATION_NAMESPACE;
            } else if (GCM_DEPLOYMENT_NAMESPACE_PREFIX.equals(prefix)) {
                return GCM_DEPLOYMENT_NAMESPACE;
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

    /**
     * Parse a &lt;xxxClasspath&gt; node.
     * We can hard-code the namespace because this is used only in application descriptor parsing.
     * @param xpath
     * @param classPathNode
     * @return
     * @throws XPathExpressionException
     */
    static public List<PathElement> parseClasspath(XPath xpath, Node classPathNode)
            throws XPathExpressionException {
        NodeList pathElementNodes = (NodeList) xpath.evaluate(GCM_APPLICATION_NAMESPACE_PREFIX +
            ":pathElement", classPathNode, XPathConstants.NODESET);

        ArrayList<PathElement> res = new ArrayList<PathElement>();

        for (int i = 0; i < pathElementNodes.getLength(); ++i) {
            Node pathElementNode = pathElementNodes.item(i);
            PathElement pathElement = parsePathElementNode(pathElementNode);
            res.add(pathElement);
        }

        return res;
    }

    static public PathElement parsePathElementNode(Node pathElementNode) {
        PathElement pathElement = new PathElement();
        String attr = GCMParserHelper.getAttributeValue(pathElementNode, "relpath");
        pathElement.setRelPath(attr);
        attr = GCMParserHelper.getAttributeValue(pathElementNode, "base");
        if (attr != null) {
            pathElement.setBase(attr);
        }

        return pathElement;
    }

    /**
     * Parse an argument list node :
     * &lt;xxx&gt;
     *   &lt;arg&gt;
     *   &lt;arg&gt;
     *   ...
     * &/lt;xxx&gt;
     * 
     * We can fix the namespace because it's only used in deployment descriptor parsing 
     * 
     * @param xpath
     * @param argumentListNode
     * @return
     * @throws XPathExpressionException
     */
    public static List<String> parseArgumentListNode(XPath xpath, Node argumentListNode)
            throws XPathExpressionException {
        ArrayList<String> args = new ArrayList<String>();

        NodeList argNodes = (NodeList) xpath.evaluate(GCM_DEPLOYMENT_NAMESPACE_PREFIX + ":arg",
                argumentListNode, XPathConstants.NODESET);

        for (int i = 0; i < argNodes.getLength(); ++i) {
            Node argNode = argNodes.item(i);
            args.add(getElementValue(argNode));
        }

        return args;
    }

    public static HashMap<String, HashMap<String, String>> parseTechnicalServicesNode(XPath xpath,
            Node techServicesNode) throws XPathExpressionException {

        HashMap<String, HashMap<String, String>> techServicesMap = new HashMap<String, HashMap<String, String>>();

        NodeList classList = (NodeList) xpath.evaluate("app:class", techServicesNode, XPathConstants.NODESET);

        for (int i = 0; i < classList.getLength(); ++i) {
            Node classNode = classList.item(i);

            String techServiceClassName = classNode.getAttributes().getNamedItem("name").getNodeValue();

            HashMap<String, String> propertyMap = new HashMap<String, String>();

            NodeList propertyList = (NodeList) xpath.evaluate("app:property", classNode,
                    XPathConstants.NODESET);

            for (int j = 0; j < propertyList.getLength(); j++) {
                Node propertyNode = propertyList.item(j);
                NamedNodeMap attributes = propertyNode.getAttributes();
                String propertyName = attributes.getNamedItem("name").getNodeValue();
                String propertyValue = attributes.getNamedItem("value").getNodeValue();

                propertyMap.put(propertyName, propertyValue);
            }

            techServicesMap.put(techServiceClassName, propertyMap);
        }

        return techServicesMap;
    }

    public static DocumentBuilder getNewDocumentBuilder(DocumentBuilderFactory domFactory) {
        return getNewDocumentBuilder(domFactory, null);
    }

    public static DocumentBuilder getNewDocumentBuilder(DocumentBuilderFactory domFactory,
            ErrorHandler errorHandler) {
        try {
            DocumentBuilder documentBuilder = domFactory.newDocumentBuilder();
            if (errorHandler == null) {
                errorHandler = new MyDefaultHandler();
            }
            documentBuilder.setErrorHandler(errorHandler);

            return documentBuilder;
        } catch (ParserConfigurationException e) {
            return null;
        }
    }

    public static String elementInNS(String prefixNS, String element) {
        return prefixNS + ":" + element;
    }

}
