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
package testsuite.xml;

import java.util.Properties;

import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;


/**
 * @author Alexandre di Costanzo
 *
 */
public class PropertiesHandler extends AbstractUnmarshallerDecorator
    implements ManagerDescriptorConstants {
    private Properties properties = null;

    PropertiesHandler() {
        super();
        properties = new Properties();
        addHandler(PROP_TAG, new PropHandler(properties));
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

        PropHandler(Properties properties) {
            super();
            this.properties = properties;
        }

        public Object getResultObject() throws org.xml.sax.SAXException {
            return properties;
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            String key = attributes.getValue("key");
            String value = attributes.getValue("value");
            if (checkNonEmpty(key) && checkNonEmpty(value)) {
                properties.put(key, value);
            }
        }

        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
        }
    }
}
