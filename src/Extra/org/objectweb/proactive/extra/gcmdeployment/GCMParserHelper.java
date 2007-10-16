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
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.FileTransferBlock;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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

    static public FileTransferBlock parseFileTransferNode(Node fileTransferNode) {
        FileTransferBlock fileTransferBlock = new FileTransferBlock();
        String source = GCMParserHelper.getAttributeValue(fileTransferNode,
                "source");
        fileTransferBlock.setSource(source);
        String destination = GCMParserHelper.getAttributeValue(fileTransferNode,
                "destination");
        fileTransferBlock.setDestination(destination);

        return fileTransferBlock;
    }

    static public class MyDefaultHandler extends DefaultHandler {
        private String errMessage = "";

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

    static public class ProActiveNamespaceContext implements NamespaceContext {
        protected String namespace;

        public ProActiveNamespaceContext(String namespace) {
            this.namespace = namespace;
        }

        public String getNamespaceURI(String prefix) {
            if (prefix == null) {
                throw new NullPointerException("Null prefix");
            } else if ("pa".equals(prefix)) {
                return namespace;
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

    static public List<PathElement> parseClasspath(XPath xpath,
        Node classPathNode) throws XPathExpressionException {
        NodeList pathElementNodes = (NodeList) xpath.evaluate("pa:pathElement",
                classPathNode, XPathConstants.NODESET);

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
        String attr = GCMParserHelper.getAttributeValue(pathElementNode,
                "relpath");
        pathElement.setRelPath(attr);
        attr = GCMParserHelper.getAttributeValue(pathElementNode, "base");
        if (attr != null) {
            pathElement.setBase(attr);
        }

        return pathElement;
    }
}
