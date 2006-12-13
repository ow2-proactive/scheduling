/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.config.xml;

import java.io.IOException;

import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class MasterFileHandler extends AbstractUnmarshallerDecorator
    implements MasterFileConstants {
    //    static {
    //        BasicConfigurator.configure();
    //    }
    protected static ProActiveConfiguration config;

    public MasterFileHandler() {
        addHandler(PROPERTIES_TAG,
            new PropertiesHandler(MasterFileHandler.config));

        //addHandler(LOG4J_FILE_TAG, new Log4jConfigurationHandler());
    }

    /**
     * Create a SAX parser on the specified file
     * @param filename the full path to the file
     */
    public static void createMasterFileHandler(String filename,
        ProActiveConfiguration config) {
        MasterFileHandler.config = config;

        InitialHandler h = new InitialHandler();
        org.objectweb.proactive.core.xml.io.StreamReader sr;

        try {
            InputSource source = null;

            //System.out.println("FILENAME = " + filename);
            if (filename.startsWith("bundle://")) {

                /* osgi mode, get the ProActiveConfiguration in the jar root */
                filename = "/ProActiveConfiguration.xml";
                //filename = "/org/objectweb/proactive/core/config/ProActiveConfiguration.xml";
                source = new InputSource(MasterFileHandler.class.getResourceAsStream(
                            filename));
            } else {
                source = new org.xml.sax.InputSource(filename);
            }
            sr = new org.objectweb.proactive.core.xml.io.StreamReader(source, h);
            sr.read();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
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
        private MasterFileHandler masterFileHandler;

        // private InitialHandler(AbstractManager manager) {
        private InitialHandler() {
            super();
            masterFileHandler = new MasterFileHandler();

            //			  managerDescriptorHandler = new ManagerDescriptorHandler(manager);
            this.addHandler(MASTER_TAG, masterFileHandler);
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

    //    private class SingleValueUnmarshaller extends AbstractUnmarshallerDecorator {
    //        public void readValue(String value) throws org.xml.sax.SAXException {
    //            //  setResultObject(value);
    //        }
    //
    //        public void startContextElement(String name, Attributes attributes)
    //            throws org.xml.sax.SAXException {
    //            //	String key = attributes.getValue("key");
    //            String value = attributes.getValue("value");
    //            System.out.println("name = " + name + " value = " + value);
    //            //			if (checkNonEmpty(key) && checkNonEmpty(value)) {
    //            //				properties.put(key, value);
    //            //			}
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
    //         */
    //        protected void notifyEndActiveHandler(String name,
    //            UnmarshallerHandler activeHandler) throws SAXException {
    //            // TO DO : Auto-generated method stub
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
    //         */
    //        public Object getResultObject() throws SAXException {
    //            // TO DO : Auto-generated method stub
    //            return null;
    //        }
    //    }
}
