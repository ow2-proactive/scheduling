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
package org.objectweb.proactive.core.descriptor.xml;

import java.util.Hashtable;
import java.util.Map;

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.services.TechnicalServiceXmlType;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.xml.sax.SAXException;


public class TechnicalServicesHandler extends PassiveCompositeUnmarshaller
    implements ProActiveDescriptorConstants {
    protected ProActiveDescriptor proActiveDescriptor;

    public TechnicalServicesHandler(ProActiveDescriptor proActiveDescriptor) {
        super(false);
        this.proActiveDescriptor = proActiveDescriptor;
        addHandler(TECHNICAL_SERVICES_DEF_TAG,
            new TechnicalServiceDefinitionHandler());
    }

    /**
     * @see org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
     */
    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws SAXException {
        try {
            proActiveDescriptor.addTechnicalService((TechnicalServiceXmlType) activeHandler.getResultObject());
        } catch (Exception e) {
            throw new SAXException("Technical service class not instanciable", e);
        }
    }

    // INNER
    public class TechnicalServiceDefinitionHandler
        extends PassiveCompositeUnmarshaller {
        private Map<String, String> argsMap = new Hashtable<String, String>();
        private TechnicalServiceXmlType technicalService = new TechnicalServiceXmlType();

        public TechnicalServiceDefinitionHandler() {
            addHandler(TECHNICAL_SERVICE_ARG_TAG,
                new TechnicalServiceArgHandler());
        }

        /**
         * @see org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller#startContextElement(java.lang.String, org.objectweb.proactive.core.xml.io.Attributes)
         */
        public void startContextElement(String name, Attributes attributes)
            throws SAXException {
            this.technicalService.setId(attributes.getValue("id"));
            try {
                this.technicalService.setType(Class.forName(attributes.getValue(
                            "class")));
            } catch (ClassNotFoundException e) {
                throw new SAXException("Technical Service not found", e);
            }
        }

        /**
         * @see org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
         */
        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws SAXException {
            this.technicalService.setArgs(this.argsMap);
        }

        /**
         * @see org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller#getResultObject()
         */
        public Object getResultObject() throws SAXException {
            return this.technicalService;
        }

        // INNNER INNER
        public class TechnicalServiceArgHandler extends BasicUnmarshaller {
            public void startContextElement(String name, Attributes attributes)
                throws SAXException {
                String argName = attributes.getValue("name");
                String argValue = attributes.getValue("value");
                argsMap.put(argName, argValue);
            }
        }
    }
}
