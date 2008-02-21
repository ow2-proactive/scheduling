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

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorImpl;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorInternal;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.objectweb.proactive.core.xml.io.SAXParserErrorHandlerTerminating;
import org.xml.sax.SAXException;


/**
 * This class receives deployment events
 *
 * @author ProActive Team
 * @version 1.0, 2002/09/20
 * @since ProActive 0.9.3
 */
public class ProActiveDescriptorHandler extends AbstractUnmarshallerDecorator implements
        ProActiveDescriptorConstants {
    protected ProActiveDescriptorInternal proActiveDescriptor;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public ProActiveDescriptorHandler(String xmlDescriptorUrl, VariableContractImpl variableContract) {
        super(false);
        proActiveDescriptor = new ProActiveDescriptorImpl(xmlDescriptorUrl);
        // keep a reference of the variable contract for future use
        proActiveDescriptor.setVariableContract(variableContract);

        addHandler(MAIN_DEFINITION_TAG, new MainDefinitionHandler(proActiveDescriptor));
        addHandler(DEPLOYMENT_TAG, new DeploymentHandler(proActiveDescriptor));
        addHandler(INFRASTRUCTURE_TAG, new InfrastructureHandler(proActiveDescriptor));
        addHandler(FILE_TRANSFER_DEFINITIONS_TAG, new FileTransferDefinitionsHandler(proActiveDescriptor));
        addHandler(TECHNICAL_SERVICES_TAG, new TechnicalServicesHandler(proActiveDescriptor));
        addHandler(SECURITY_TAG, new SecurityHandler(proActiveDescriptor));

        {
            PassiveCompositeUnmarshaller compDefHandler = new PassiveCompositeUnmarshaller();
            PassiveCompositeUnmarshaller vNodesDefHandler = new PassiveCompositeUnmarshaller();
            PassiveCompositeUnmarshaller vNodesAcqHandler = new PassiveCompositeUnmarshaller();
            vNodesDefHandler.addHandler(VIRTUAL_NODE_TAG, new VirtualNodeHandler(proActiveDescriptor));
            vNodesAcqHandler.addHandler(VIRTUAL_NODE_TAG, new VirtualNodeLookupHandler());
            compDefHandler.addHandler(VIRTUAL_NODES_DEFINITION_TAG, vNodesDefHandler);
            compDefHandler.addHandler(VIRTUAL_NODES_ACQUISITION_TAG, vNodesAcqHandler);
            this.addHandler(COMPONENT_DEFINITION_TAG, compDefHandler);
        }

        this.addHandler(VARIABLES_TAG, new VariablesHandler(variableContract));
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //	public static void main(String[] args) throws java.io.IOException, org.xml.sax.SAXException {
    //		// String uri =
    //		// "Z:\\ProActive\\descriptors\\C3D_Dispatcher_Renderer.xml";
    //		String uri = "/user/cjarjouh/home/ProActive/descriptors/C3D_Dispatcher_Renderer.xml";
    //		InitialHandler h = new InitialHandler(uri, new VariableContract());
    //
    //		// String uri =
    //		// "file:/net/home/rquilici/ProActive/descriptors/C3D_Dispatcher_Renderer.xml";
    //		org.objectweb.proactive.core.xml.io.StreamReader sr;
    //
    //		if ("enable".equals(ProActiveConfiguration.getSchemaValidationState())) {
    //			sr = new org.objectweb.proactive.core.xml.io.StreamReader(
    //					new org.xml.sax.InputSource(uri), h, null,
    //					new SAXParserErrorHandlerTerminating());
    //		} else {
    //			sr = new org.objectweb.proactive.core.xml.io.StreamReader(
    //					new org.xml.sax.InputSource(uri), h);
    //		}
    //
    //		sr.read();
    //	}

    /**
     * Creates ProActiveDescriptor object from XML Descriptor
     *
     * @param xmlDescriptorUrl
     *            the URL of XML Descriptor
     */
    public static URL selectSchema(String schema) throws java.io.IOException {
        // we get the schema from the class location
        ClassLoader classLoader = ProActiveDescriptorHandler.class.getClassLoader();

        Enumeration<URL> schemaURLs = classLoader
                .getResources("org/objectweb/proactive/core/descriptor/xml/schemas/" + schema);

        // among the various descriptor schema that we may find, we will always
        // favor the one that is in the jar file
        URL schemaURLcandidate = null;

        while (schemaURLs.hasMoreElements()) {
            URL schemaURL = schemaURLs.nextElement();

            if (schemaURL.getProtocol().equals("jar")) {
                schemaURLcandidate = schemaURL;
            } else if (schemaURLcandidate == null) {
                schemaURLcandidate = schemaURL;
            }
        }

        if (schemaURLcandidate == null) {
            logger.error("The schema " + schema +
                " could not be located in your environment. Consider compiling ProActive using ant");
        }

        return schemaURLcandidate;
    }

    public static ProActiveDescriptorHandler createProActiveDescriptor(String xmlDescriptorUrl,
            VariableContractImpl variableContract) throws java.io.IOException, org.xml.sax.SAXException {
        // static method added to replace main method
        InitialHandler h = new InitialHandler(xmlDescriptorUrl, variableContract);
        String uri = xmlDescriptorUrl;

        String[] schemas = new String[] { "deployment/3.3/deployment.xsd", "security/1.0/security.xsd" };

        Vector<String> selectedSchemas = new Vector<String>();

        for (int i = 0; i < schemas.length; i++) {
            URL schemaURLcandidate = selectSchema(schemas[i]);

            if (schemaURLcandidate != null) {
                selectedSchemas.add(schemaURLcandidate.toString());
                logger.debug("Using XML schema: " + schemaURLcandidate.toString());
            } else {
                logger.error("No schema instance (file) found for " + schemas[i]);
            }
        }

        org.objectweb.proactive.core.xml.io.StreamReader sr = null;
        org.xml.sax.InputSource inputSource = new org.xml.sax.InputSource(uri);

        inputSource.setSystemId(uri.toString());

        if (PAProperties.SCHEMA_VALIDATION.isTrue()) {
            String[] selectedSchemasArray = selectedSchemas.toArray(new String[0]);
            if (selectedSchemasArray.length == 0) {
                selectedSchemasArray = null;
            }
            sr = new org.objectweb.proactive.core.xml.io.StreamReader(new org.xml.sax.InputSource(uri), h,
                selectedSchemasArray, new SAXParserErrorHandlerTerminating());
        } else {
            sr = new org.objectweb.proactive.core.xml.io.StreamReader(new org.xml.sax.InputSource(uri), h);
        }

        sr.read();

        return (ProActiveDescriptorHandler) h.getResultObject();
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

    public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
    }

    //
    // -- PROTECTED METHODS
    // ------------------------------------------------------
    //
    @Override
    protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
            throws org.xml.sax.SAXException {

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

        private InitialHandler(String xmlDescriptorUrl, VariableContractImpl variableContract) {
            super();
            proActiveDescriptorHandler = new ProActiveDescriptorHandler(xmlDescriptorUrl, variableContract);
            this.addHandler(PROACTIVE_DESCRIPTOR_TAG, proActiveDescriptorHandler);
        }

        public Object getResultObject() throws org.xml.sax.SAXException {
            return proActiveDescriptorHandler;
        }

        public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
        }

        @Override
        protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
                throws org.xml.sax.SAXException {
        }
    }

    /**
     * This class receives virtualNode events
     */
    private class VirtualNodeHandler extends BasicUnmarshaller {
        private ProActiveDescriptorInternal pad;

        private VirtualNodeHandler(ProActiveDescriptorInternal pad) {
            this.pad = pad;
        }

        @Override
        public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
            // create and register a VirtualNode
            String vnName = attributes.getValue("name");

            if (!checkNonEmpty(vnName)) {
                throw new org.xml.sax.SAXException("VirtualNode defined without name");
            }

            // underneath, we know that it is a VirtualNodeImpl, since the
            // bollean in the method is false
            VirtualNodeImpl vn = (VirtualNodeImpl) proActiveDescriptor.createVirtualNode(vnName, false);

            // property
            String property = attributes.getValue("property");

            if (checkNonEmpty(property)) {
                vn.setProperty(property);
            }

            String timeout = attributes.getValue("timeout");
            String waitForTimeoutAsString = attributes.getValue("waitForTimeout");
            boolean waitForTimeout = false;

            if (checkNonEmpty(waitForTimeoutAsString)) {
                waitForTimeout = new Boolean(waitForTimeoutAsString).booleanValue();
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

            String fileTransferDeployName = attributes.getValue(FILE_TRANSFER_DEPLOY_TAG);

            if (checkNonEmpty(fileTransferDeployName)) {
                vn.addFileTransferDeploy(pad.getFileTransfer(fileTransferDeployName));
            }

            String fileTransferRetrieveName = attributes.getValue(FILE_TRANSFER_RETRIEVE_TAG);

            if (checkNonEmpty(fileTransferRetrieveName)) {
                vn.addFileTransferRetrieve(pad.getFileTransfer(fileTransferRetrieveName));
            }

            String technicalServiceId = attributes.getValue(TECHNICAL_SERVICE_ID);

            if (checkNonEmpty(technicalServiceId)) {
                vn.addTechnicalService(pad.getTechnicalService(technicalServiceId));
            }
        }
    } // end inner class VirtualNodeHandler

    /**
     * This class receives virtualNode events
     */
    private class VirtualNodeLookupHandler extends BasicUnmarshaller {
        private VirtualNodeLookupHandler() {
        }

        @Override
        public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
            // create and register a VirtualNode
            String vnName = attributes.getValue("name");

            if (!checkNonEmpty(vnName)) {
                throw new org.xml.sax.SAXException("VirtualNode defined without name");
            }

            proActiveDescriptor.createVirtualNode(vnName, true);
        }
    } // end inner class VirtualNodeLookupHandler

    // SECURITY

    /**
     * This class receives Security events
     */
    private class SecurityHandler extends AbstractUnmarshallerDecorator {
        private ProActiveDescriptorInternal proActiveDescriptor;

        public SecurityHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            super();
            this.proActiveDescriptor = proActiveDescriptor;
            this.addHandler(SECURITY_FILE_TAG, new SecurityFileHandler(proActiveDescriptor));
        }

        @Override
        protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
                throws SAXException {
        }

        public Object getResultObject() throws SAXException {
            return proActiveDescriptor;
        }

        public void startContextElement(String name, Attributes attributes) throws SAXException {
        }
    }

    /**
     * This class receives Security events
     */
    private class SecurityFileHandler extends BasicUnmarshaller {
        private ProActiveDescriptorInternal proActiveDescriptor;

        public SecurityFileHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            super();
            this.proActiveDescriptor = proActiveDescriptor;
        }

        @Override
        public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
            // create and register a VirtualNode
            String path = attributes.getValue("uri");

            if (!checkNonEmpty(path)) {
                throw new org.xml.sax.SAXException("Empty security file");
            }

            File f = new File(path);
            if (!f.isAbsolute()) {
                File descriptorPath = new File(this.proActiveDescriptor.getUrl());
                String descriptorDir = descriptorPath.getParent();
                if (descriptorDir != null) {
                    path = descriptorDir + File.separator + path;
                }
            }

            logger.debug("creating ProActiveSecurityManager : " + path);
            proActiveDescriptor.createProActiveSecurityManager(path);
        }
    }

    // end inner class SecurityHandler
}
