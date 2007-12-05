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
package org.objectweb.proactive.core.config.xml;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


public class ProActiveConfigurationParser {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.CONFIGURATION);
    static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    static final String XPATH_PAPROPS = "//properties/prop";
    static final String XPATH_JAVAPROPS = "//javaProperties/prop";
    static final String ATTR_KEY = "key";
    static final String ATTR_VALUE = "value";

    public static Properties parse(String filename, Properties properties) {
        if (properties == null) {
            properties = new Properties();
        }

        InputSource source = null;
        try {
            //System.out.println("FILENAME = " + filename);
            if (filename.startsWith("bundle://")) {
                /* osgi mode, get the ProActiveConfiguration in the jar root */
                filename = "/ProActiveConfiguration.xml";
                //filename = "/org/objectweb/proactive/core/config/ProActiveConfiguration.xml";
                source = new InputSource(ProActiveConfigurationParser.class.getResourceAsStream(
                            filename));
            } else {
                source = new org.xml.sax.InputSource(filename);
            }

            DocumentBuilderFactory domFactory;
            DocumentBuilder builder;
            Document document;
            XPath xpath;
            domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(false);
            domFactory.setValidating(true);
            domFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
            String schema = ProActiveConfigurationParser.class.getClass()
                                                              .getResource("/org/objectweb/proactive/core/config/xml/ProActiveConfiguration.xsd")
                                                              .toString();
            domFactory.setAttribute(JAXP_SCHEMA_SOURCE, new Object[] { schema });

            XPathFactory factory = XPathFactory.newInstance();

            xpath = factory.newXPath();
            xpath.setNamespaceContext(new PAConfNamespaceContext());
            builder = domFactory.newDocumentBuilder();
            builder.setErrorHandler(new MyDefaultHandler());

            document = builder.parse(source);

            NodeList nodes;

            nodes = (NodeList) xpath.evaluate(XPATH_PAPROPS, document,
                    XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                String key = getAttributeValue(node, ATTR_KEY);
                String value = getAttributeValue(node, ATTR_VALUE);

                PAProperties prop = PAProperties.getProperty(key);
                if (prop != null) {
                    if (prop.isValid(value)) {
                        properties.setProperty(key, value);
                    } else {
                        logger.warn("Invalid value, " + value + " for key " +
                            key + ". Must be a " + prop.getType().toString());
                    }
                } else {
                    logger.warn("Property " + key + " is not declared inside " +
                        PAProperties.class.getSimpleName() + " , ignoring");
                }
            }

            nodes = (NodeList) xpath.evaluate(XPATH_JAVAPROPS, document,
                    XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                String key = getAttributeValue(node, ATTR_KEY);
                String value = getAttributeValue(node, ATTR_VALUE);
                properties.put(key, value);
            }
        } catch (SAXException e) {
            logger.warn("Invalid ProActive Configuration file: " + source, e);
            properties = new Properties();
        } catch (ParserConfigurationException e) {
            logger.warn("Invalid ProActive Configuration file: " + source, e);
            properties = new Properties();
        } catch (XPathExpressionException e) {
            logger.warn("Invalid ProActive Configuration file: " + source, e);
            logger.warn(e);
            properties = new Properties();
        } catch (IOException e) {
            logger.warn(e);
            properties = new Properties();
        }

        return properties;
    }

    static private String getAttributeValue(Node node, String attributeName) {
        Node namedItem = node.getAttributes().getNamedItem(attributeName);
        return (namedItem != null) ? namedItem.getNodeValue() : null;
    }

    private static class PAConfNamespaceContext implements NamespaceContext {
        public String getNamespaceURI(String prefix) {
            if (prefix == null) {
                throw new NullPointerException("Null prefix");
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

    private static class MyDefaultHandler extends DefaultHandler {
        @Override
        public void warning(SAXParseException e) {
            logger.warn("Warning Line " + e.getLineNumber() + ": " +
                e.getMessage() + "\n");
        }

        @Override
        public void error(SAXParseException e) throws SAXParseException {
            String errMessage = new String("Error Line " + e.getLineNumber() +
                    ": " + e.getMessage() + "\n");
            logger.error(errMessage);
            throw e;
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXParseException {
            String errMessage = new String("Error Line " + e.getLineNumber() +
                    ": " + e.getMessage() + "\n");
            logger.fatal(errMessage);
            throw e;
        }
    }
}
