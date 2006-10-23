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

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorImpl;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.objectweb.proactive.core.xml.io.SAXParserErrorHandlerTerminating;
import org.objectweb.proactive.scheduler.Scheduler;

/**
 * This class receives deployment events
 * 
 * @author ProActive Team
 * @version 1.0, 2002/09/20
 * @since ProActive 0.9.3
 */
public class ProActiveDescriptorHandler extends AbstractUnmarshallerDecorator
		implements ProActiveDescriptorConstants {
	protected ProActiveDescriptor proActiveDescriptor;

	private Scheduler scheduler;

	private String jobID;

	//
	// -- CONSTRUCTORS -----------------------------------------------
	//
	public ProActiveDescriptorHandler(String xmlDescriptorUrl,
			VariableContract variableContract) {
		super(false);
		proActiveDescriptor = new ProActiveDescriptorImpl(xmlDescriptorUrl);
		// keep a reference of the variable contract for future use
		proActiveDescriptor.setVariableContract(variableContract);

		addHandler(MAIN_DEFINITION_TAG, new MainDefinitionHandler(
				proActiveDescriptor));
		addHandler(DEPLOYMENT_TAG, new DeploymentHandler(proActiveDescriptor));
		addHandler(INFRASTRUCTURE_TAG, new InfrastructureHandler(
				proActiveDescriptor));
		addHandler(FILE_TRANSFER_DEFINITIONS_TAG,
				new FileTransferDefinitionsHandler(proActiveDescriptor));
		addHandler(TECHNICAL_SERVICES_TAG, new TechnicalServicesHandler(
				proActiveDescriptor));
		addHandler(SECURITY_TAG, new SecurityHandler(proActiveDescriptor));
		{
			PassiveCompositeUnmarshaller compDefHandler = new PassiveCompositeUnmarshaller();
			PassiveCompositeUnmarshaller vNodesDefHandler = new PassiveCompositeUnmarshaller();
			PassiveCompositeUnmarshaller vNodesAcqHandler = new PassiveCompositeUnmarshaller();
			vNodesDefHandler.addHandler(VIRTUAL_NODE_TAG,
					new VirtualNodeHandler(proActiveDescriptor));
			vNodesAcqHandler.addHandler(VIRTUAL_NODE_TAG,
					new VirtualNodeLookupHandler());
			compDefHandler.addHandler(VIRTUAL_NODES_DEFINITION_TAG,
					vNodesDefHandler);
			compDefHandler.addHandler(VIRTUAL_NODES_ACQUISITION_TAG,
					vNodesAcqHandler);
			this.addHandler(COMPONENT_DEFINITION_TAG, compDefHandler);
		}

		this.addHandler(VARIABLES_TAG, new VariablesHandler(variableContract));
	}

	public ProActiveDescriptorHandler(Scheduler scheduler, String jobId,
			String xmlDescriptorUrl) {
		super(false);
		this.proActiveDescriptor = new ProActiveDescriptorImpl(xmlDescriptorUrl);
		this.scheduler = scheduler;
		this.jobID = jobId;
		addHandler(MAIN_DEFINITION_TAG, new MainDefinitionHandler(scheduler,
				jobId, this.proActiveDescriptor));
		addHandler(INFRASTRUCTURE_TAG, new InfrastructureHandler(scheduler,
				jobId, this.proActiveDescriptor));
		addHandler(DEPLOYMENT_TAG, new DeploymentHandler(proActiveDescriptor,
				false));
		addHandler(FILE_TRANSFER_DEFINITIONS_TAG,
				new FileTransferDefinitionsHandler(proActiveDescriptor));
		addHandler(TECHNICAL_SERVICES_TAG, new TechnicalServicesHandler(
				proActiveDescriptor));
		addHandler(SECURITY_TAG, new SecurityHandler(proActiveDescriptor));
		{
			PassiveCompositeUnmarshaller compDefHandler = new PassiveCompositeUnmarshaller();
			PassiveCompositeUnmarshaller vNodesDefHandler = new PassiveCompositeUnmarshaller();
			PassiveCompositeUnmarshaller vNodesAcqHandler = new PassiveCompositeUnmarshaller();
			vNodesDefHandler.addHandler(VIRTUAL_NODE_TAG,
					new VirtualNodeHandler(proActiveDescriptor));
			vNodesAcqHandler.addHandler(VIRTUAL_NODE_TAG,
					new VirtualNodeLookupHandler());
			compDefHandler.addHandler(VIRTUAL_NODES_DEFINITION_TAG,
					vNodesDefHandler);
			compDefHandler.addHandler(VIRTUAL_NODES_ACQUISITION_TAG,
					vNodesAcqHandler);
			this.addHandler(COMPONENT_DEFINITION_TAG, compDefHandler);
		}
	}

	//
	// -- PUBLIC METHODS -----------------------------------------------
	//
	public static void main(String[] args) throws java.io.IOException {
		// String uri =
		// "Z:\\ProActive\\descriptors\\C3D_Dispatcher_Renderer.xml";
		String uri = "/user/cjarjouh/home/ProActive/descriptors/C3D_Dispatcher_Renderer.xml";
		InitialHandler h = new InitialHandler(uri, new VariableContract());

		// String uri =
		// "file:/net/home/rquilici/ProActive/descriptors/C3D_Dispatcher_Renderer.xml";
		org.objectweb.proactive.core.xml.io.StreamReader sr = new org.objectweb.proactive.core.xml.io.StreamReader(
				new org.xml.sax.InputSource(uri), h, null,
				new SAXParserErrorHandlerTerminating());
		sr.read();
	}

	/**
	 * Creates ProActiveDescriptor object from XML Descriptor
	 * 
	 * @param xmlDescriptorUrl
	 *            the URL of XML Descriptor
	 */
	public static ProActiveDescriptorHandler createProActiveDescriptor(
			String xmlDescriptorUrl, VariableContract variableContract)
			throws java.io.IOException, org.xml.sax.SAXException {
		// static method added to replace main method
		try {
			InitialHandler h = new InitialHandler(xmlDescriptorUrl,
					variableContract);
			String uri = xmlDescriptorUrl;
			// we get the schema from the class location
			java.net.URL urlSchema = ProActiveDescriptorHandler.class
					.getResource("/DescriptorSchema.xsd");
			String pathSchema;
			if (urlSchema == null) {
				// In case the application is executed neither via the ant
				// script, nor via the jar file, we need to find the schema
				// manually
				urlSchema = ProActiveDescriptorHandler.class.getResource("/");
				pathSchema = urlSchema.getPath();
				pathSchema = pathSchema
						.concat(".." + java.io.File.separator + "descriptors" + java.io.File.separator + "DescriptorSchema.xsd");

			} else {
				pathSchema = urlSchema.getPath();
			}

			java.io.File fileSchema = new java.io.File(pathSchema);
			org.objectweb.proactive.core.xml.io.StreamReader sr = null;
			if (fileSchema.exists()) {
				sr = new org.objectweb.proactive.core.xml.io.StreamReader(
						new org.xml.sax.InputSource(uri), h, fileSchema,
						new SAXParserErrorHandlerTerminating());
			} else {
				sr = new org.objectweb.proactive.core.xml.io.StreamReader(
						new org.xml.sax.InputSource(uri), h, null,
						new SAXParserErrorHandlerTerminating());
			}
			sr.read();
			return (ProActiveDescriptorHandler) h.getResultObject();
		} catch (org.xml.sax.SAXException e) {
			// e.printStackTrace();
			// logger.fatal(
			// "a problem occurs when getting the ProActiveDescriptorHandler");
			throw e;
		}
	}

	//
	// -- implements XMLUnmarshaller
	// ------------------------------------------------------
	//
	public Object getResultObject() throws org.xml.sax.SAXException {
		// copy xmlproperties into the pad
		// proActiveDescriptor.setVariableContract(XMLProperties.xmlproperties.duplicate());

		// Release lock on static global variable XMLProperties
		// XMLProperties.xmlproperties.clear();
		// XMLProperties.xmlproperties.releaseLock();
		return proActiveDescriptor;
	}

	public void startContextElement(String name, Attributes attributes)
			throws org.xml.sax.SAXException {
	}

	//
	// -- PROTECTED METHODS
	// ------------------------------------------------------
	//
	protected void notifyEndActiveHandler(String name,
			UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {

		/*
		 * if(name.equals(VARIABLES_TAG)){ //Check XMLProperties Runtime
		 * if(!org.objectweb.proactive.core.xml.XMLProperties.xmlproperties.checkContract())
		 * throw new SAXException("Variable contract breached"); }
		 */
	}

	//
	// -- PRIVATE METHODS ------------------------------------------------------
	//
	//
	// -- INNER CLASSES ------------------------------------------------------
	//

	/**
	 * Receives deployment events
	 */
	private static class InitialHandler extends AbstractUnmarshallerDecorator {
		// line added to return a ProactiveDescriptorHandler object
		private ProActiveDescriptorHandler proActiveDescriptorHandler;

		private InitialHandler(String xmlDescriptorUrl,
				VariableContract variableContract) {
			super();
			proActiveDescriptorHandler = new ProActiveDescriptorHandler(
					xmlDescriptorUrl, variableContract);
			this.addHandler(PROACTIVE_DESCRIPTOR_TAG,
					proActiveDescriptorHandler);
		}

		public Object getResultObject() throws org.xml.sax.SAXException {
			return proActiveDescriptorHandler;
		}

		public void startContextElement(String name, Attributes attributes)
				throws org.xml.sax.SAXException {
		}

		protected void notifyEndActiveHandler(String name,
				UnmarshallerHandler activeHandler)
				throws org.xml.sax.SAXException {
		}
	}

	/**
	 * This class receives virtualNode events
	 */
	private class VirtualNodeHandler extends BasicUnmarshaller {
		private ProActiveDescriptor pad;

		private VirtualNodeHandler(ProActiveDescriptor pad) {
			this.pad = pad;
		}

		public void startContextElement(String name, Attributes attributes)
				throws org.xml.sax.SAXException {
			// create and register a VirtualNode
			String vnName = attributes.getValue("name");
			if (!checkNonEmpty(vnName)) {
				throw new org.xml.sax.SAXException(
						"VirtualNode defined without name");
			}

			// underneath, we know that it is a VirtualNodeImpl, since the
			// bollean in the method is false
			VirtualNodeImpl vn = (VirtualNodeImpl) proActiveDescriptor
					.createVirtualNode(vnName, false);

			// property
			String property = attributes.getValue("property");
			if (checkNonEmpty(property)) {
				vn.setProperty(property);
			}
			String timeout = attributes.getValue("timeout");
			String waitForTimeoutAsString = attributes
					.getValue("waitForTimeout");
			boolean waitForTimeout = false;
			if (checkNonEmpty(waitForTimeoutAsString)) {
				waitForTimeout = new Boolean(waitForTimeoutAsString)
						.booleanValue();
			}
			if (checkNonEmpty(timeout)) {
				vn.setTimeout(new Integer(timeout).longValue(), waitForTimeout);
			}
			String minNodeNumber = attributes.getValue("minNodeNumber");
			if (checkNonEmpty(minNodeNumber)) {
				vn.setMinNumberOfNodes((new Integer(minNodeNumber).intValue()));
			}
			String serviceId = attributes.getValue("ftServiceId");
			if (checkNonEmpty(serviceId)) {
				pad.registerService(vn, serviceId);
			}
			String fileTransferDeployName = attributes
					.getValue(FILE_TRANSFER_DEPLOY_TAG);
			if (checkNonEmpty(fileTransferDeployName)) {
				vn.addFileTransferDeploy(pad
						.getFileTransfer(fileTransferDeployName));
			}
			String fileTransferRetrieveName = attributes
					.getValue(FILE_TRANSFER_RETRIEVE_TAG);
			if (checkNonEmpty(fileTransferRetrieveName)) {
				vn.addFileTransferRetrieve(pad
						.getFileTransfer(fileTransferRetrieveName));
			}
			String technicalServiceId = attributes
					.getValue(TECHNICAL_SERVICE_ID);
			if (checkNonEmpty(technicalServiceId)) {
				vn.addTechnicalService(pad
						.getTechnicalService(technicalServiceId));
			}
		}
	} // end inner class VirtualNodeHandler

	/**
	 * This class receives virtualNode events
	 */
	private class VirtualNodeLookupHandler extends BasicUnmarshaller {
		private VirtualNodeLookupHandler() {
		}

		public void startContextElement(String name, Attributes attributes)
				throws org.xml.sax.SAXException {
			// create and register a VirtualNode
			String vnName = attributes.getValue("name");
			if (!checkNonEmpty(vnName)) {
				throw new org.xml.sax.SAXException(
						"VirtualNode defined without name");
			}
			proActiveDescriptor.createVirtualNode(vnName, true);
		}
	} // end inner class VirtualNodeLookupHandler

	// SECURITY

	/**
	 * This class receives Security events
	 */
	private class SecurityHandler extends BasicUnmarshaller {
		private ProActiveDescriptor proActiveDescriptor;

		public SecurityHandler(ProActiveDescriptor proActiveDescriptor) {
			super();
			this.proActiveDescriptor = proActiveDescriptor;
		}

		public void startContextElement(String name, Attributes attributes)
				throws org.xml.sax.SAXException {
			// create and register a VirtualNode
			String file = attributes.getValue("file");

			if (!checkNonEmpty(file)) {
				throw new org.xml.sax.SAXException("Empty security file");
			}
			logger.debug("creating ProActiveSecurityManager : " + file);
			proActiveDescriptor.createProActiveSecurityManager(file);
		}
	}

	// end inner class SecurityHandler
}
