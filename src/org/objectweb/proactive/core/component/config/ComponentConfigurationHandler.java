/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2004 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.component.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.CollectionUnmarshaller;
import org.objectweb.proactive.core.xml.handler.SingleValueUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.objectweb.proactive.core.xml.io.StreamReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * @author Matthieu Morel
 */
public class ComponentConfigurationHandler extends AbstractUnmarshallerDecorator
    implements ComponentConfigurationConstants {
    Map controllers = new HashMap();
    List inputInterceptors = new ArrayList();
    List outputInterceptors = new ArrayList();
    public static Logger logger = Logger.getLogger(ComponentConfigurationHandler.class.getName());

    //private ComponentsDescriptor componentsDescriptor;
    //private ComponentsCache componentsCache;
    //private HashMap componentTypes;
    public ComponentConfigurationHandler() {
        //super(true);
        addHandler(CONTROLLERS_ELEMENT, new ControllersHandler());
    }

    public List getInputInterceptors() {
        return inputInterceptors;
    }

    public List getOutputInterceptors() {
        return outputInterceptors;
    }

    public Map getControllers() {
        return controllers;
    }

    public static ComponentConfigurationHandler createComponentConfigurationHandler(
        String componentsConfigurationLocation)
        throws IOException, SAXException, ProActiveException {
        try {
            InitialHandler initial_handler = new InitialHandler();
            String url = null;
            if (ComponentConfigurationHandler.class.getResource(componentsConfigurationLocation) != null) {
                // it's in the classpath
                url = ComponentConfigurationHandler.class.getResource(componentsConfigurationLocation).toString();
            } else {
                // user-specified
                url = new File(componentsConfigurationLocation).getAbsolutePath();
            }
            StreamReader stream_reader = new StreamReader(new InputSource(url),
                    initial_handler);
            stream_reader.read();
            return (ComponentConfigurationHandler) initial_handler.getResultObject();
        } catch (SAXException se) {
            logger.fatal(
                "a problem occured while parsing the components descriptor : " +
                se.getMessage());
            se.printStackTrace();
            throw se;
        }
    }

    /**
     * see {@link org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)}
     */
    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws SAXException {
    }

    /**
     * see {@link org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.String, org.objectweb.proactive.core.xml.io.Attributes)}
     */
    public void startContextElement(String name, Attributes attributes)
        throws SAXException {
    }

    public Object getResultObject() throws SAXException {
        return null;
    }

    //
    // -- INNER CLASSES ------------------------------------------------------
    //
    private static class InitialHandler extends AbstractUnmarshallerDecorator {
        private ComponentConfigurationHandler componentConfigurationHandler;

        private InitialHandler() {
            componentConfigurationHandler = new ComponentConfigurationHandler();
            this.addHandler(COMPONENT_CONFIGURATION_ELEMENT,
                componentConfigurationHandler);
        }

        public Object getResultObject() throws org.xml.sax.SAXException {
            return componentConfigurationHandler;
        }

        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
        }

        public void startContextElement(String name, Attributes attributes)
            throws SAXException {
        }
    }

    /******************************************************************************************************************/
    private class ControllersHandler extends CollectionUnmarshaller {
        public ControllersHandler() {
            addHandler(ComponentConfigurationConstants.CONTROLLER_ELEMENT,
                new ControllerHandler());
        }

        public void startContextElement(String name, Attributes attributes)
            throws SAXException {
        }

        /* (non-Javadoc)
         * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
         */
        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws SAXException {
            if (name.equals(ComponentConfigurationConstants.CONTROLLER_ELEMENT)) {
                activeHandler.getResultObject();
            }
        }

        /* (non-Javadoc)
         * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
         */
        public Object getResultObject() throws SAXException {
            return null;
        }
    }

    /******************************************************************************************************************/
    public class ControllerHandler extends AbstractUnmarshallerDecorator {
        String interfaceSignature = null;
        String implementationSignature = null;
        boolean inputInterception = false;
        boolean outputInterception = false;

        public ControllerHandler() {
            UnmarshallerHandler singleValueHandler = new SingleValueUnmarshaller();
            addHandler(INTERFACE_ELEMENT, singleValueHandler);
            addHandler(IMPLEMENTATION_ELEMENT, singleValueHandler);
        }

        public void startContextElement(String name, Attributes attributes)
            throws SAXException {
            if ("true".equals(attributes.getValue(INPUT_INTERCEPTOR_ATTRIBUTE))) {
                inputInterception = true;
            }
            if ("true".equals(attributes.getValue(OUTPUT_INTERCEPTOR_ATTRIBUTE))) {
                outputInterception = true;
            }
        }

        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws SAXException {
            if (name.equals(INTERFACE_ELEMENT)) {
                interfaceSignature = (String) activeHandler.getResultObject();
            }
            if (name.equals(IMPLEMENTATION_ELEMENT)) {
                implementationSignature = (String) activeHandler.getResultObject();
            }
        }

        public Object getResultObject() throws SAXException {
            controllers.put(interfaceSignature, implementationSignature);
            if (inputInterception) {
                inputInterceptors.add(implementationSignature);
            }
            if (outputInterception) {
                outputInterceptors.add(implementationSignature);
            }
            interfaceSignature = null;
            implementationSignature = null;
            inputInterception = false;
            outputInterception = false;
            return null;
        }
    }
}
