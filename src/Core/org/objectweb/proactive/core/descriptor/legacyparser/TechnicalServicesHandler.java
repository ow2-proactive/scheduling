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
package org.objectweb.proactive.core.descriptor.legacyparser;

import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorInternal;
import org.objectweb.proactive.core.descriptor.services.TechnicalServiceXmlType;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.xml.sax.SAXException;


public class TechnicalServicesHandler extends PassiveCompositeUnmarshaller
    implements ProActiveDescriptorConstants {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.DEPLOYMENT);
    protected ProActiveDescriptorInternal proActiveDescriptor;

    public TechnicalServicesHandler(
        ProActiveDescriptorInternal proActiveDescriptor) {
        super(false);
        this.proActiveDescriptor = proActiveDescriptor;
        addHandler(TECHNICAL_SERVICES_DEF_TAG,
            new TechnicalServiceDefinitionHandler());
    }

    /**
     * @see org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
     */
    @Override
    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws SAXException {
        try {
            proActiveDescriptor.addTechnicalService((TechnicalServiceXmlType) activeHandler.getResultObject());
        } catch (NullPointerException e) {
            // Technical service not used by any virtual node
            logger.warn("Technical service  not attached to virtual node");
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
        @Override
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
        @Override
        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws SAXException {
            this.technicalService.setArgs(this.argsMap);
        }

        /**
         * @see org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller#getResultObject()
         */
        @Override
        public Object getResultObject() throws SAXException {
            return this.technicalService;
        }

        // INNNER INNER
        public class TechnicalServiceArgHandler extends BasicUnmarshaller {
            @Override
            public void startContextElement(String name, Attributes attributes)
                throws SAXException {
                String argName = attributes.getValue("name");
                String argValue = attributes.getValue("value");
                argsMap.put(argName, argValue);
            }
        }
    }
}
