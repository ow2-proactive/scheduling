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

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Matthieu Morel
 */
// ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//public class BindingsHandler extends CollectionUnmarshaller {
public class BindingsHandler extends AbstractUnmarshallerDecorator {
	ComponentsCache componentsCache;
	public BindingsHandler(ComponentsCache componentsCache) {
		this.componentsCache = componentsCache;
		addHandler(ComponentsDescriptorConstants.BINDING_TAG, new BindingHandler());

	}

	// ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	public class BindingHandler extends BasicUnmarshaller {

		private String clientComponent = null;
		private String clientInterface = null;
		private String serverComponent = null;
		private String serverInterface = null;

		public BindingHandler() {
		}

		private void setClient(String client) {
			clientComponent = client.substring(0, client.indexOf('.'));
			clientInterface = client.substring(client.indexOf('.') + 1, client.length());
		}

		private void setServer(String server) {
			serverComponent = server.substring(0, server.indexOf('.'));
			serverInterface = server.substring(server.indexOf('.') + 1, server.length());
		}

		/**
		 * {@link org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.String, org.objectweb.proactive.core.xml.io.Attributes)}
		 */
		public void startContextElement(String name, Attributes attributes) throws SAXException {
			logger.debug("*****inside binding handler");

			String client = attributes.getValue(ComponentsDescriptorConstants.BINDING_CLIENT_TAG);
			if (!checkNonEmpty(client)) {
				throw new SAXException("binding client interface unspecified");
			}
			String server = attributes.getValue(ComponentsDescriptorConstants.BINDING_SERVER_TAG);
			if (!checkNonEmpty(server)) {
				throw new SAXException("binding server interfaceunspecified");
			}
			setClient(client);
			setServer(server);
			// perform binding (binding is automatically stored within the component)
			try {
				if(!componentsCache.containsComponentNamed(serverComponent)) {
					throw new SAXException("tried to perform a binding from " + serverComponent +'.' + serverInterface + " , but " + serverComponent + " does not exist");
				}
				(
					(BindingController) componentsCache.getComponent(clientComponent).getFcInterface(
						Constants.BINDING_CONTROLLER)).bindFc(
					clientInterface,
					((Component) componentsCache.getComponent(serverComponent)).getFcInterface(serverInterface));
				logger.debug(
					"**** bound "
						+ clientComponent
						+ "."
						+ clientInterface
						+ " to "
						+ serverComponent
						+ "."
						+ serverInterface);
			} catch (NoSuchInterfaceException nsie) {
				nsie.printStackTrace();
				throw new SAXException("error while parsing", nsie);
			} catch (IllegalLifeCycleException ilce) {
				ilce.printStackTrace();
				throw new SAXException("error while parsing", ilce);
			} catch (IllegalBindingException ibe) {
				ibe.printStackTrace();
				throw new SAXException("error while parsing", ibe);
			}

		}

		/**
		 * see {@link org.objectweb.proactive.core.xml.io.XMLHandler#endElement(java.lang.String)}
		 */
		public void endElement(String name) throws SAXException {
		}

	}
	/**
	 * see {@link org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)}
	 */
	protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler) throws SAXException {
	}

	/**
	 * see {@link org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()}
	 */
	public Object getResultObject() throws SAXException {
		return null;
	}

	/**
	 * see {@link org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.String, org.objectweb.proactive.core.xml.io.Attributes)}
	 */
	public void startContextElement(String name, Attributes attributes) throws SAXException {
	}

}