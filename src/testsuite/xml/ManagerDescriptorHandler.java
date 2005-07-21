/*
* ################################################################
*
* ProActive: The Java(TM) library for Parallel, Distributed,
*            Concurrent computing with Security and Mobility
*
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
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

import org.apache.log4j.Logger;

import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.SingleValueUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;

import org.xml.sax.SAXException;

import testsuite.manager.AbstractManager;
import testsuite.manager.FunctionalTestManager;

import java.util.Properties;


/**
 * @author Alexandre di Costanzo
 *
 */
public class ManagerDescriptorHandler extends AbstractUnmarshallerDecorator
    implements ManagerDescriptorConstants {
    private AbstractManager manager = null;

    /**
     * @param lenient
     */
    public ManagerDescriptorHandler(AbstractManager manager) {
        super(true);
        this.manager = manager;
        addHandler(MANAGER_NAME_TAG, new SingleValueUnmarshaller());
        addHandler(MANAGER_DESCRIPTION_TAG, new SingleValueUnmarshaller());
        addHandler(MANAGER_NB_RUNS_TAG, new SingleValueUnmarshaller());
        addHandler(SIMPLE_GROUP_TAG,
            new GroupHandler.SimpleGroupHandler(manager));
        addHandler(PACKAGE_GROUP_TAG,
            new GroupHandler.PackageGroupHandler(manager));
        if (manager instanceof FunctionalTestManager) {
            addHandler(INTERLINKED_TESTS_TAG,
                new InterLinkedHandler((FunctionalTestManager) manager));
        }
        addHandler(PROPERTIES_TAG, new PropertiesHandler());
        addHandler(LOG4J_TAG, new SingleValueUnmarshaller());
        addHandler(RESULT_TAG, new ResultHandler(this.manager));
    }

    public static void createManagerDescriptor(String xmlDescriptorUrl,
        AbstractManager manager)
        throws java.io.IOException, org.xml.sax.SAXException {
        Logger logger = manager.getLogger();
        if (logger.isDebugEnabled()) {
            logger.debug("Reading file " + xmlDescriptorUrl +
                " to configure the manager ...");
        }
        InitialHandler h = new InitialHandler(manager);
        String uri = xmlDescriptorUrl;
        org.objectweb.proactive.core.xml.io.StreamReader sr = new org.objectweb.proactive.core.xml.io.StreamReader(new org.xml.sax.InputSource(
                    uri), h);
        sr.read();
        if (logger.isDebugEnabled()) {
            logger.debug("... Finish to configure the manager");
        }
    }

    /**
     * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
     */
    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws SAXException {
        if (name.equalsIgnoreCase(MANAGER_NAME_TAG)) {
            this.manager.setName((String) activeHandler.getResultObject());
        } else if (name.equalsIgnoreCase(MANAGER_DESCRIPTION_TAG)) {
            this.manager.setDescription((String) activeHandler.getResultObject());
        } else if (name.equalsIgnoreCase(MANAGER_NB_RUNS_TAG)) {
            this.manager.setNbRuns(Integer.parseInt(
                    (String) activeHandler.getResultObject()));
        } else if (name.equalsIgnoreCase(PROPERTIES_TAG)) {
            this.manager.setProperties((Properties) activeHandler.getResultObject());
        } else if (name.equalsIgnoreCase(LOG4J_TAG)) {
            this.manager.loggerConfigure((String) activeHandler.getResultObject());
        }
    }

    /**
     * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
     */
    public Object getResultObject() throws SAXException {
        return this.manager;
    }

    /**
     * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.String, org.objectweb.proactive.core.xml.io.Attributes)
     */
    public void startContextElement(String name, Attributes attributes)
        throws SAXException {
    }

    //
    // -- INNER CLASSES ------------------------------------------------------
    //
    private static class InitialHandler extends AbstractUnmarshallerDecorator {
        private ManagerDescriptorHandler managerDescriptorHandler;

        private InitialHandler(AbstractManager manager) {
            super();
            managerDescriptorHandler = new ManagerDescriptorHandler(manager);
            this.addHandler(MANAGER_TAG, managerDescriptorHandler);
        }

        public Object getResultObject() throws org.xml.sax.SAXException {
            return managerDescriptorHandler;
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
        }

        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
        }
    }

}
