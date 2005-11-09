/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.descriptor.xml;

import java.io.IOException;

import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class MasterPropertiesFileHandler extends AbstractUnmarshallerDecorator
    implements ProActiveDescriptorConstants {
    public MasterPropertiesFileHandler() {
        this.addHandler(PROPERTY_TAG, new PropertiesHandler());
    }

    /**
     * Create a SAX parser on the specified file
     * @param filename the full path to the file
     */
    public static void createMasterFileHandler(String filename) {
        InitialHandler h = new InitialHandler();
        org.objectweb.proactive.core.xml.io.StreamReader sr;

        try {
            String file = MasterPropertiesFileHandler.class.getResource(filename)
                                                           .getPath();
            InputSource source = new org.xml.sax.InputSource(file);

            sr = new org.objectweb.proactive.core.xml.io.StreamReader(source, h);
            sr.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws SAXException {
        //   System.out.println("End active handler");
    }

    public Object getResultObject() throws SAXException {
        //  System.out.println("get result object");
        return null;
    }

    public void startContextElement(String name, Attributes attributes)
        throws SAXException {
    }

    //
    // -- INNER CLASSES ------------------------------------------------------
    //
    private static class InitialHandler extends AbstractUnmarshallerDecorator {
        private MasterPropertiesFileHandler masterFileHandler;

        // private InitialHandler(AbstractManager manager) {
        private InitialHandler() {
            super();
            masterFileHandler = new MasterPropertiesFileHandler();

            this.addHandler(PROPERTY_INFILE_TAG, masterFileHandler);
        }

        public Object getResultObject() throws org.xml.sax.SAXException {
            //	  return managerDescriptorHandler;
            return null; //masterFileHandler;
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
        }

        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
        }
    }
}
