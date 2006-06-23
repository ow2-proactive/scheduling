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

import java.util.Properties;

import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;


/**
 * @author adicosta
 *
 */
public class PropertiesHandler extends AbstractUnmarshallerDecorator
    implements MasterFileConstants {
    //protected static Logger logger = Logger.getLogger(PropertiesHandler.class.getName());
    private Properties properties = null;

    public PropertiesHandler(ProActiveConfiguration config) {
        super();
        properties = new Properties();
        addHandler(PROP_TAG, new PropHandler(properties, config));
    }

    public Object getResultObject() throws org.xml.sax.SAXException {
        return properties;
    }

    public void startContextElement(String name, Attributes attributes)
        throws org.xml.sax.SAXException {
    }

    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
    }

    //-----------------------------------------------------------------------------------------------------------
    private static class PropHandler extends AbstractUnmarshallerDecorator {
        private Properties properties = null;
        private ProActiveConfiguration config;

        PropHandler(Properties properties, ProActiveConfiguration config) {
            super();
            this.properties = properties;
            this.config = config;
        }

        public Object getResultObject() throws org.xml.sax.SAXException {
            return properties;
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            //   System.out.println("**** PropHandler");
            String key = attributes.getValue("key");
            String value = attributes.getValue("value");

            //PropertiesHandler.logger.debug("Key " + key + " value  " + value);
            // System.out.println("Key " + key + " value  " + value);
            //  if (checkNonEmpty(key) && checkNonEmpty(value)) {
            //      properties.put(key, value);
            //  }
            //System.out.println("config is " + config);
            config.propertyFound(key, value);
        }

        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
        }
    }
}
