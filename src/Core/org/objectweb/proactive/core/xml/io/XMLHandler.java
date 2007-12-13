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

/**
 *
 * Receives SAX event and pass them on
 *
 * @author       Lionel Mestre
 * @version      0.91
 *
 */
public interface XMLHandler {

    /**
     * Receives notification that an XML element of given name and attributes has been read in the
     * XML being deserialized
     * @param name the name of the element just opened
     * @param attributes the attributes of this element
     * @exception org.xml.sax.SAXException if an exception occur during processing
     */
    public void startElement(String name, Attributes attributes) throws org.xml.sax.SAXException;

    /**
     * Receives notification that an XML value has been read in the XML being deserialized
     * @param value the value just read
     * @exception org.xml.sax.SAXException if an exception occur during processing
     */
    public void readValue(String value) throws org.xml.sax.SAXException;

    /**
     * Receives notification that the end of an XML element of given name has been read
     * in the XML being deserialized
     * @param name the name of the element just ended
     * @exception org.xml.sax.SAXException if an exception occur during processing
     */
    public void endElement(String name) throws org.xml.sax.SAXException;

    /**
     * Receives notification that an XML prefix has just been defined
     * @param prefix the name of the prefix defined
     * @param uri the uri qualifying the prefix
     * @exception org.xml.sax.SAXException if an exception occur during processing
     */
    public void startPrefixMapping(String prefix, String uri) throws org.xml.sax.SAXException;

    /**
     * Receives notification that the scope of an XML prefix has just ended
     * @param prefix the name of the prefix ended
     * @exception org.xml.sax.SAXException if an exception occur during processing
     */
    public void endPrefixMapping(String prefix) throws org.xml.sax.SAXException;
}
