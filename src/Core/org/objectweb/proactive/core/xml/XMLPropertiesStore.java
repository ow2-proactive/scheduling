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
package org.objectweb.proactive.core.xml;

import java.util.List;

/**
 * This class implements a XML based preferences store. Constructor expect a URI pointing to an XML file containing
 * the values.
 * The URI can point to a file using the file protocol :
 *     file:///D:/doc/xml/properties.xml
 *
 * The data are loaded and parsed in memory.
 *
 * In order to query the values, the class uses a stripped down version of XPath expressions.
 * The only supported expressions are combination of /, . and @ for the attributes.
 *
 * Here some examples of supported expressions :
 *
 *    /a/b/c     : returns the value of the node or the node c
 *    /a/b/c/    : returns the value of the node or the node c
 *    /a/b/c/.   : returns the value of the node or the node c
 *    e/.        : returns the value of the node e which should be a child of the given current node
 *    /a/b/c/@t  : returns the value of the attribute node or the attribute node t
 *    &amp;t         : returns the value of the attribute node or the attribute node t which should be a child of the given current node
 *
 *
 * @author       Lionel Mestre
 * @version      1.0
 */
public class XMLPropertiesStore {
    //
    //  ----- STATIC MEMBERS -----------------------------------------------------------------------------------
    //
    //
    //  ----- PRIVATE MEMBERS -----------------------------------------------------------------------------------
    //

    /** the URI pointing to the XML data to read the properties from */
    private String targetURI;

    /** the DOM Document resulting of the parsing of the XML Data */
    private org.w3c.dom.Document targetDocument;

    /** the DOM Element root of the Document resulting from the parsing */
    private org.w3c.dom.Element rootElement;

    //
    //  ----- CONSTRUCTORS -----------------------------------------------------------------------------------
    //

    /**
     * Contructs a new intance of XMLPropertiesStore based on th given URI pointing to
     * XML data. The data are loaded and parsed into a DOM.
     * @param uri the URI of the XML data containing the properties to read
     * @exception java.io.IOException if the XML data cannot be loaded
     */
    public XMLPropertiesStore(String uri) throws java.io.IOException {
        this.targetURI = uri;
        this.targetDocument = parseFromURI(uri);
        this.rootElement = targetDocument.getDocumentElement();
    }

    //
    //  ----- STATIC METHODS -----------------------------------------------------------------------------------
    //

    /**
     * Parses the XML data the given URI points to and returns the DOM Document
     * representing that XML.
     * @param uri the URI of the XML data containing the data to read
     * @exception java.io.IOException if the XML data cannot be read or parsed
     */
    public static org.w3c.dom.Document parseFromURI(String uri) throws java.io.IOException {
        try {
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory
                    .newInstance();
            javax.xml.parsers.DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            return documentBuilder.parse(uri);
        } catch (org.xml.sax.SAXException e) {
            throw new java.io.IOException(e.toString());
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            throw new java.io.IOException(e.toString());
        }
    }

    //
    //  ----- PUBLIC METHODS -----------------------------------------------------------------------------------
    //

    /**
     * Returns the value defined by the given path as a string. If the XPath
     * is not found null is returned
     * @param path represents the XPath expression leading to the value.
     *             (see restrictions in the class description)
     * @return the value identified by path or null
     */
    public String getValueAsString(String path) {
        return getValueAsString(path, rootElement);
    }

    /**
     * Returns the value defined by the given path as an int. If the XPath
     * is not found null is returned
     * @param path represents the XPath expression leading to the value.
     *             (see restrictions in the class description)
     * @param defaultValue the defaultValue to return if the value cannot be found
     * @return the value identified by path or the defaultValue
     */
    public int getValueAsInt(String path, int defaultValue) {
        return getValueAsInt(path, rootElement, defaultValue);
    }

    /**
     * Returns the value defined by the given path as an int. If the XPath
     * is not found null is returned
     * @param path represents the XPath expression leading to the value.
     *             (see restrictions in the class description)
     * @return the node identified by path or null
     */
    public org.w3c.dom.Node getValueAsNode(String path) {
        return getValueAsNode(path, rootElement);
    }

    /**
     * Returns all child nodes defined by the given path. If the XPath
     * is not found null is returned.
     * @param path represents the XPath expression leading to the nodes.
     *             (see restrictions in the class description)
     * @return all nodes identified by path or null
     */
    public org.w3c.dom.Node[] getAllNodes(String path) {
        return getAllNodes(path, rootElement);
    }

    /**
     * Returns the value defined by the given path as a string. If the XPath
     * is not found null is returned
     * @param path represents the XPath expression leading to the value.
     *        It is relative to the given context.
     * @param context the node from which to interprete the XPath expression.
     * @return the value identified by path or null
     */
    public String getValueAsString(String path, org.w3c.dom.Node context) {
        if (context.getOwnerDocument() != targetDocument) {
            return null;
        }
        org.w3c.dom.Node node = findNodeFromXPath(path, context);
        if (node == null) {
            return null;
        }
        int type = node.getNodeType();
        if (type == org.w3c.dom.Node.ELEMENT_NODE) {
            org.w3c.dom.Node child = node.getFirstChild();
            if (child == null) {
                return null;
            }
            return child.getNodeValue();
        } else {
            return node.getNodeValue();
        }
    }

    /**
     * Returns the value defined by the given path as a string. If the XPath
     * is not found null is returned
     * @param path represents the XPath expression leading to the value.
     *        It is relative to the given context.
     * @param context the node from which to interprete the XPath expression.
     * @param defaultValue the defaultValue to return if the value cannot be found
     * @return the value identified by path or the defaultValue
     */
    public int getValueAsInt(String path, org.w3c.dom.Node context, int defaultValue) {
        String s = getValueAsString(path, context);
        if (s == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Returns the node defined by the given path. If the XPath
     * is not found null is returned. The XPath is interpreted from the given node.
     * @param path represents the XPath expression leading to the property.
     *             (see restrictions in the class description)
     * @param context the node from which to interprete the XPath expression.
     * @return the node of the property identified by path or null
     */
    public org.w3c.dom.Node getValueAsNode(String path, org.w3c.dom.Node context) {
        if (context.getOwnerDocument() != targetDocument) {
            return null;
        }
        return findNodeFromXPath(path, context);
    }

    /**
     * Returns all child nodes defined by the given path. If the XPath
     * is not found null is returned. The XPath is interpreted from the given node.
     * @param path represents the XPath expression leading to the nodes.
     *             (see restrictions in the class description)
     * @param context the node from which to interprete the XPath expression.
     * @return all nodes identified by path or null
     */
    public org.w3c.dom.Node[] getAllNodes(String path, org.w3c.dom.Node context) {
        if (context.getOwnerDocument() != targetDocument) {
            return null;
        }
        return findNodesFromXPath(path, context);
    }

    /*
       // for testing purpose
       public static void main(String[] args) {
         String uri = null;
         if (args.length > 0) {
           uri = args[0];
         } else {
           uri = "file:///D:/cygwin/home/lmestre/ProActive/ProActiveDescriptor2.xml";
           System.out.println("uri="+uri);
         }
         try {
           XMLPropertiesStore p = new XMLPropertiesStore(uri);
           System.out.println("p="+p.toString());
           System.out.println("  ====  ");
         } catch (java.io.IOException e) {
           e.printStackTrace();
         }
       }
       // for testing purpose
       public String toString() {
         try {
           return serializeDocumentToString(targetDocument);
         } catch (java.io.IOException e) {
           e.printStackTrace();
           return null;
         }
       }
       public static String serializeDocumentToString(org.w3c.dom.Document doc) throws java.io.IOException {
         org.apache.xml.serialize.OutputFormat of = new org.apache.xml.serialize.OutputFormat(
             org.apache.xml.serialize.Method.XML,
             org.apache.xml.serialize.OutputFormat.Defaults.Encoding,true);
         of.setIndent(2);
         org.apache.xml.serialize.XMLSerializer ts = new org.apache.xml.serialize.XMLSerializer(of);
         java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
         java.io.Writer writer = new java.io.PrintWriter(baos,false);
         ts.setOutputCharStream(writer);
         ts.serialize(doc);
         writer.flush();
         writer.close();
         return baos.toString();
       }
     */

    //
    //  ----- PRIVATE METHODS -----------------------------------------------------------------------------------
    //
    /**
     * Returns the node of the current DOM defined by the given xpath and starting from
     * the given context node. If the xpath expression does not lead to a node null is returned.
     * @param path is the XPath expression leading to the node.
     * @param node is the context node from where to start processing the xpath
     * @return the node targeted by the xpath expression or null
     */
    private org.w3c.dom.Node findNodeFromXPath(String path, org.w3c.dom.Node node) {
        // deals with special cases if path is empty or point to the current node
        if ((path == null) || (path.length() == 0) || path.equals(".")) {
            return node;
        }

        // find the possible attribute in the XPath expression
        int n = path.indexOf('@');
        String attributeName = null;
        if ((n > -1) && (n < (path.length() - 1))) {
            attributeName = path.substring(n + 1);
            path = path.substring(0, n);
        }

        // deal with case where the XPath was just an attribute
        if (path.length() == 0) {
            return findNamedAttribute(attributeName, node);
        }

        java.util.StringTokenizer st = new java.util.StringTokenizer(path, "/");
        if (path.charAt(0) == '/') {
            // case of an absolute path (starting from the root node)
            // in this case we process the current node
            node = rootElement;
            if (st.hasMoreTokens()) {
                String t = st.nextToken();
                if (!t.equals(node.getNodeName())) {
                    return null;
                }
            }
        }

        // iterates through the /
        while (st.hasMoreTokens() && (node != null)) {
            String t = st.nextToken();
            if (t.equals(".")) {
                break;
            }
            node = findNamedChild(t, node);
        }
        return findNamedAttribute(attributeName, node);
    }

    /**
     * Returns the nodes of the current DOM defined by the given xpath and starting from
     * the given context node. If the xpath expression does not lead to a node null is returned.
     * @param path is the XPath expression leading to the node.
     * @param node is the context node from where to start processing the xpath
     * @return the node targeted by the xpath expression or null
     */
    private org.w3c.dom.Node[] findNodesFromXPath(String path, org.w3c.dom.Node node) {
        // deals with special cases if path is empty or point to the current node
        if ((path == null) || (path.length() == 0) || path.equals(".")) {
            return null;
        }

        // remove possible attribute in the XPath expression
        int n = path.indexOf('@');
        if ((n > -1) && (n < (path.length() - 1))) {
            path = path.substring(0, n);
        }

        // deal with case where the XPath was just an attribute
        if (path.length() == 0) {
            return null;
        }

        java.util.StringTokenizer st = new java.util.StringTokenizer(path, "/");
        if (path.charAt(0) == '/') {
            // case of an absolute path (starting from the root node)
            // in this case we process the current node
            node = rootElement;
            if (st.hasMoreTokens()) {
                String t = st.nextToken();
                if (!t.equals(node.getNodeName())) {
                    return null;
                }
            }
        }

        // iterates through the /
        if (!st.hasMoreTokens()) {
            return null;
        }
        while (node != null) {
            String t = st.nextToken();
            if (t.equals(".")) {
                return null;
            }
            if (st.hasMoreTokens()) {
                node = findNamedChild(t, node);
            } else {
                return findNamedChilds(t, node);
            }
        }
        return null;
    }

    /**
     * Returns the attribute node of the given node and of name attributeName. If there is
     * no attribute of such a name null is returned.
     * @param attributeName the name of the attribute to look for
     * @param node the node where to look for the attribute (the node can be null
     * and in such a case null is returned)
     * @return the attribute node of name attributeName or null
     */
    private org.w3c.dom.Node findNamedAttribute(String attributeName, org.w3c.dom.Node node) {
        if ((attributeName == null) || (attributeName.length() == 0)) {
            return node;
        }
        org.w3c.dom.NamedNodeMap attributes = node.getAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getNamedItem(attributeName);
    }

    /**
     * Returns the child node of the given node and of given name. If there is
     * no child of such a name null is returned. If there is more than one child
     * only the first one is returned
     * @param name the name of the child to look for
     * @param node the non null node where to look for the childs
     * @return the matching child node or null
     */
    private org.w3c.dom.Node findNamedChild(String name, org.w3c.dom.Node node) {
        org.w3c.dom.Node child = node.getFirstChild();
        while (child != null) {
            if (name.equals(child.getNodeName())) {
                return child;
            }
            child = child.getNextSibling();
        }
        return child;
    }

    /**
     * Returns the child nodes of the given node and of given name. If there is
     * no child of such a name null is returned. All childs of that name are returned.
     * @param name the name of the child to look for
     * @param node the non null node where to look for the childs
     * @return the matching child nodes or null
     */
    private org.w3c.dom.Node[] findNamedChilds(String name, org.w3c.dom.Node node) {
        org.w3c.dom.Node child = node.getFirstChild();
        List<org.w3c.dom.Node> result = new java.util.ArrayList<org.w3c.dom.Node>();
        while (child != null) {
            if (name.equals(child.getNodeName())) {
                result.add(child);
            }
            child = child.getNextSibling();
        }
        int n = result.size();
        if (n == 0) {
            return null;
        }
        org.w3c.dom.Node[] resultArray = new org.w3c.dom.Node[n];
        return (org.w3c.dom.Node[]) result.toArray(resultArray);
    }
}
