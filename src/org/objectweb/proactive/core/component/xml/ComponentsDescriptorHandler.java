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
package org.objectweb.proactive.core.component.xml;

import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.objectweb.proactive.core.xml.io.StreamReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Matthieu Morel
 */
public class ComponentsDescriptorHandler
	extends AbstractUnmarshallerDecorator
	implements ComponentsDescriptorConstants {

	public static Logger logger = Logger.getLogger(ComponentsDescriptorHandler.class.getName());
	//private ComponentsDescriptor componentsDescriptor;
	private ComponentsCache componentsCache;
	private HashMap componentTypes;

	public ComponentsDescriptorHandler(ProActiveDescriptor deploymentDescriptor) {
		//super(true);
		componentsCache = new ComponentsCache();
		componentTypes = new HashMap();
		addHandler(TYPES_TAG, new TypesHandler(componentTypes));
		addHandler(COMPONENTS_TAG, new ComponentsHandler(deploymentDescriptor, componentsCache, componentTypes, null));
		addHandler(BINDINGS_TAG, new BindingsHandler(componentsCache));
	}

	public static ComponentsDescriptorHandler createComponentsDescriptorHandler(
	//ProActiveDescriptor deploymentDescriptor,
	String componentsDescriptorURL, String deploymentDescriptorURL)
		throws IOException, SAXException, ProActiveException {
		try {
			// 1. deployment descriptor

			logger.info("loading deployment description from file : " + deploymentDescriptorURL);
			// read the deployment descriptor
			ProActiveDescriptor deploymentDescriptor = ProActive.getProactiveDescriptor(deploymentDescriptorURL);
			deploymentDescriptor.activateMappings();

			// activate the virtual nodes (and underlying nodes)
			VirtualNode[] virtual_nodes = deploymentDescriptor.getVirtualNodes();
			for (int i = 0; i < virtual_nodes.length; i++) {
				VirtualNode vn = virtual_nodes[i];
				vn.activate();
			}
			logger.debug("virtual nodes activated");

			// 2. components descriptor

			InitialHandler initial_handler = new InitialHandler(deploymentDescriptor);
			String uri = componentsDescriptorURL;
			StreamReader stream_reader = new StreamReader(new InputSource(uri), initial_handler);
			stream_reader.read();
			return (ComponentsDescriptorHandler) initial_handler.getResultObject();
		} catch (SAXException se) {
			logger.fatal("a problem occured while parsing the components descriptor : " + se.getMessage());
			se.printStackTrace();
			throw se;
		} catch (ProActiveException pae) {
			logger.fatal("a problem occured while parsing the components descriptor");
			logger.fatal("exception from ProActive : " + pae.getMessage());
			pae.printStackTrace();
			throw pae;
		}
	}

	public static ComponentsDescriptorHandler createComponentsDescriptorHandler(
		String componentsDescriptorURL,
		ProActiveDescriptor deploymentDescriptor)
		throws IOException, SAXException, ProActiveException {
		try {
			InitialHandler initial_handler = new InitialHandler(deploymentDescriptor);
			String uri = componentsDescriptorURL;
			StreamReader stream_reader = new StreamReader(new InputSource(uri), initial_handler);
			stream_reader.read();
			return (ComponentsDescriptorHandler) initial_handler.getResultObject();
		} catch (SAXException se) {
			logger.fatal("a problem occured while parsing the components descriptor : " + se.getMessage());
			se.printStackTrace();
			throw se;
		}
	}

	/**
     * see {@link org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)}
	 */
	protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler) throws SAXException {
	}

	/**
     * see {@link org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.String, org.objectweb.proactive.core.xml.io.Attributes)}
	 */
	public void startContextElement(String name, Attributes attributes) throws SAXException {
	}

	//
	// -- INNER CLASSES ------------------------------------------------------
	//
	private static class InitialHandler extends AbstractUnmarshallerDecorator {
		private static Logger logger = Logger.getLogger(InitialHandler.class.getName());


		private ComponentsDescriptorHandler componentsDescriptorHandler;

		private InitialHandler(ProActiveDescriptor deploymentDescriptor) {
			componentsDescriptorHandler = new ComponentsDescriptorHandler(deploymentDescriptor);
			this.addHandler(COMPONENTS_DESCRIPTOR_TAG, componentsDescriptorHandler);
		}

		public Object getResultObject() throws org.xml.sax.SAXException {
			return componentsDescriptorHandler;
		}

		protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
			throws org.xml.sax.SAXException {
		}

		public void startContextElement(String name, Attributes attributes) throws SAXException {
		}

	}

	//-----------------------------------------------------------------------------------------------------------
	private class SingleValueUnmarshaller extends BasicUnmarshaller {
		public void readValue(String value) throws org.xml.sax.SAXException {
			setResultObject(value);
		}
	}
	
	
	// ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	public static void main(String[] args) {

		String deploymentDescriptorFileLocation = "/net/home/mmorel/ProActive/tmp/deploymentDescriptor.xml";
		String componentsDescriptorFileLocation = "/net/home/mmorel/ProActive/tmp/componentsDescriptor.xml";
		logger.info("loading deployment description from file : " + deploymentDescriptorFileLocation);
		ProActiveDescriptor deploymentDescriptor = null;
		// read the deployment descriptor
		try {
			deploymentDescriptor = ProActive.getProactiveDescriptor(deploymentDescriptorFileLocation);
			//            descriptor.activateMappings();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("could not read deployment descriptor file");
		}

		// activate the virtual nodes (and underlying nodes)
		VirtualNode[] virtual_nodes = deploymentDescriptor.getVirtualNodes();
		for (int i = 0; i < virtual_nodes.length; i++) {
			VirtualNode vn = virtual_nodes[i];
			vn.activate();
		}
		logger.debug("virtual nodes activated");

		logger.info("loading component description from file : " + componentsDescriptorFileLocation);
		try {
			createComponentsDescriptorHandler(componentsDescriptorFileLocation, deploymentDescriptor);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (SAXException e1) {
			e1.printStackTrace();
		} catch (ProActiveException pae) {
			pae.printStackTrace();
		}
	}

	/**
     * see {@link org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()}
	 */
	public Object getResultObject() throws SAXException {
		return componentsCache;
	}

}
