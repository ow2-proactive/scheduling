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
package org.objectweb.proactive.core.xml.io;

import org.xml.sax.SAXException;


/**
 *
 * Adaptor between the DefaultHandler from SAX API and the XMLHandler
 *
 * @author       Lionel Mestre
 * @version      0.91
 *
 */
public class DefaultHandlerAdapter extends org.xml.sax.helpers.DefaultHandler {
    protected XMLHandler targetHandler;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //  
    public DefaultHandlerAdapter(XMLHandler targetHandler) {
        this.targetHandler = targetHandler;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- ContentHandler methods ------------------------------------------------------
    //

    /**
     * startPrefixMapping.
     * Receives notification of the start of a Namespace mapping.
     */

    //  public void startPrefixMapping(String prefix, String uri) throws org.xml.sax.SAXException {
    //    //System.out.println("startPrefixMapping prefix="+prefix+"  uri="+uri);
    //    targetHandler.startPrefixMapping(prefix, uri);
    //  }
    //
    //
    // /**
    //  * endPrefixMapping.
    //  * Receive notification of the start of a Namespace mapping. 
    //  */
    //  public void endPrefixMapping(String prefix) throws org.xml.sax.SAXException {
    //    //System.out.println("endPrefixMapping prefix="+prefix);
    //    targetHandler.endPrefixMapping(prefix);
    //  }
    /**
     * Start element.
     */
    @Override
    public void startElement(String namespaceURI, String localName, String qName, org.xml.sax.Attributes atts)
            throws org.xml.sax.SAXException {
        //System.out.println("DefaultHandlerAdaptor startElement localName="+localName+" qName="+qName);
        targetHandler.startElement(qName, new AttributesImpl(atts));
    }

    /**
     * end element.
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName)
            throws org.xml.sax.SAXException {
        //System.out.println("DefaultHandlerAdaptor endElement localName="+localName+" qName="+qName);
        targetHandler.endElement(qName);
    }

    //  public void skippedEntity(java.lang.String name) {
    //    // ignore
    //    // System.out.println("skippedEntity "+name);
    //  }
    //  
    //  
    //  public void processingInstruction(java.lang.String target, java.lang.String data) {
    //    // ignore
    //    //System.out.println("processingInstruction target="+target+"  data="+data);
    //  }

    /**
     * Characters.
     */
    @Override
    public void characters(char[] ch, int start, int length) throws org.xml.sax.SAXException {
        if (length > 0) {
            char[] c = new char[length];
            System.arraycopy(ch, start, c, 0, length);
            targetHandler.readValue(parseCharacterEntities(new String(c)));
        }
    }

    // /**
    //  * ignorableWhitespace.
    //  */
    //  public void ignorableWhitespace(char[] ch, int start, int length) throws org.xml.sax.SAXException {
    //    // ignore
    //  }
    //
    // -- ErrorHandler methods ------------------------------------------------------
    //
    // /**
    //  * Warning.
    //  */
    //  public void warning(org.xml.sax.SAXParseException ex) {
    //    System.err.println("[Warning] : "+ex.getMessage());
    //  }
    //
    //  /** Error. */
    //  public void error(org.xml.sax.SAXParseException ex) {
    //    System.err.println("[Error] : "+ex.getMessage());
    //  }
    //
    //  /** Fatal error. */
    //  public void fatalError(org.xml.sax.SAXParseException ex) throws org.xml.sax.SAXException {
    //    System.err.println("[Fatal Error] : "+ex.getMessage());
    //    throw ex;
    //  }
    //
    // -- PROTECTED METHODS ------------------------------------------------------
    //
    protected class AttributesImpl implements Attributes {
        private org.xml.sax.Attributes attributes;

        AttributesImpl(org.xml.sax.Attributes attributes) {
            this.attributes = attributes;
        }

        public String getValue(int index) {
            return attributes.getValue(index);
        }

        public String getValue(String qName) throws SAXException {
            String attribute = attributes.getValue(qName);
            if ((attribute != null) && (attribute.indexOf("${") >= 0)) {
                return org.objectweb.proactive.core.xml.VariableContract.xmlproperties.transform(attribute);
            }

            return attribute;
        }

        public String getValue(String uri, String localPart) throws SAXException {
            String attribut = attributes.getValue(uri, localPart);
            if ((attribut != null) && (attribut.indexOf("${") >= 0)) {
                return org.objectweb.proactive.core.xml.VariableContract.xmlproperties.transform(attribut);
            }

            return attribut;
            //            return attributes.getValue(uri, localPart);
        }

        public int getLength() {
            return attributes.getLength();
        }
    }

    protected class EmptyAttributesImpl implements Attributes {
        EmptyAttributesImpl() {
        }

        public String getValue(int index) {
            return null;
        }

        public String getValue(String qName) {
            return null;
        }

        public String getValue(String uri, String localPart) {
            return null;
        }

        public int getLength() {
            return 0;
        }
    }

    //
    // -- PRIVATE METHODS ------------------------------------------------------
    //
    private String parseCharacterEntities(String in) {
        return in;
    }
}
