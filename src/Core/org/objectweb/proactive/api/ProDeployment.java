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
package org.objectweb.proactive.api;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorInternal;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeInternal;
import org.objectweb.proactive.core.descriptor.legacyparser.ProActiveDescriptorHandler;
import org.objectweb.proactive.core.descriptor.parser.JaxpDescriptorParser;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.xml.VariableContract;


@PublicAPI
public class ProDeployment {
    protected final static Logger logger = ProActiveLogger.getLogger(Loggers.CORE);

    /**
     * Returns a <code>ProActiveDescriptor</code> that gives an object representation
     * of the XML document located at the url given by proactive.pad system's property.
     * @return the pad located at the url given by proactive.pad system's property
     * @throws ProActiveException
     * @throws RemoteException
     */
    public static ProActiveDescriptor getProactiveDescriptor()
        throws ProActiveException, IOException {
        String padURL = PAProperties.PA_PAD.getValue();

        //System.out.println("pad propertie : " + padURL) ;
        if (padURL == null) {
            //System.out.println("pad null");
            return null;
        } else {
            return getProActiveDescriptor(padURL, new VariableContract(), true);
        }
    }

    /**
     * Returns a <code>ProActiveDescriptor</code> that gives an object representation
     * of the XML document located at the given url.
     * @param xmlDescriptorUrl The url of the XML document
     * @return ProActiveDescriptor. The object representation of the XML document
     * @throws ProActiveException if a problem occurs during the creation of the object
     * @see org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorInternal
     * @see org.objectweb.proactive.core.descriptor.data.VirtualNodeInternal
     * @see org.objectweb.proactive.core.descriptor.data.VirtualMachine
     */
    public static ProActiveDescriptor getProactiveDescriptor(
        String xmlDescriptorUrl) throws ProActiveException {
        return getProActiveDescriptor(xmlDescriptorUrl, new VariableContract(),
            false);
    }

    /**
     * Returns a <code>ProActiveDescriptor</code> that gives an object representation
     * of the XML document located at the given url, and uses the given Variable Contract.
     * @param xmlDescriptorUrl The url of the XML document
     * @return ProActiveDescriptor. The object representation of the XML document
     * @throws ProActiveException if a problem occurs during the creation of the object
     * @see org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorInternal
     * @see org.objectweb.proactive.core.descriptor.data.VirtualNodeInternal
     * @see org.objectweb.proactive.core.descriptor.data.VirtualMachine
     */
    public static ProActiveDescriptor getProactiveDescriptor(
        String xmlDescriptorUrl, VariableContract variableContract)
        throws ProActiveException {
        if (variableContract == null) {
            throw new NullPointerException(
                "Argument variableContract can not be null");
        }

        return getProActiveDescriptor(xmlDescriptorUrl, variableContract, false);
    }

    public static ProActiveDescriptor getProActiveDescriptor(
        String xmlDescriptorUrl, VariableContract variableContract,
        boolean hierarchicalSearch) throws ProActiveException {
        //Get lock on XMLProperties global static variable
        org.objectweb.proactive.core.xml.VariableContract.lock.aquire();
        org.objectweb.proactive.core.xml.VariableContract.xmlproperties = variableContract;

        //Get the pad
        ProActiveDescriptorInternal pad;
        try {
            pad = internalGetProActiveDescriptor(xmlDescriptorUrl,
                    variableContract, hierarchicalSearch);
        } catch (ProActiveException e) {
            org.objectweb.proactive.core.xml.VariableContract.lock.release();
            throw e;
        }

        //No further modifications can be donde on the xmlproperties, thus we close the contract
        variableContract.close();

        //Check the contract (proposed optimization: Do this when parsing </variable> tag instead of here!)
        if (!variableContract.checkContract()) {
            ProDeployment.logger.error(variableContract.toString());
            org.objectweb.proactive.core.xml.VariableContract.lock.release();
            throw new ProActiveException("Variable Contract has not been met!");
        }

        //Release lock on static global variable XMLProperties
        VariableContract.xmlproperties = new VariableContract();
        org.objectweb.proactive.core.xml.VariableContract.lock.release();

        return pad;
        //return getProactiveDescriptor(xmlDescriptorUrl, false);
    }

    private static ProActiveDescriptorInternal internalGetProActiveDescriptor(
        String xmlDescriptorUrl, VariableContract variableContract,
        boolean hierarchicalSearch) throws ProActiveException {
        ProActiveDescriptorInternal descriptor;
        if (System.getProperty("proactive.old.parser") != null) {
            descriptor = internalGetProActiveDescriptor_old(xmlDescriptorUrl,
                    variableContract, hierarchicalSearch);
        } else {
            descriptor = internalGetProActiveDescriptor_new(xmlDescriptorUrl,
                    variableContract, hierarchicalSearch);
        }
        return descriptor;
    }

    /**
     * return the pad matching with the given url or parse it from the file system
     * @param xmlDescriptorUrl url of the pad
     * @param hierarchicalSearch must search in hierarchy ?
     * @return the pad found or a new pad parsed from xmlDescriptorUrl
     * @throws ProActiveException
     * @throws RemoteException
     */
    private static ProActiveDescriptorInternal internalGetProActiveDescriptor_new(
        String xmlDescriptorUrl, VariableContract variableContract,
        boolean hierarchicalSearch) throws ProActiveException {
        RuntimeFactory.getDefaultRuntime();
        if (xmlDescriptorUrl.indexOf(':') == -1) {
            xmlDescriptorUrl = "file:" + xmlDescriptorUrl;
        }
        ProActiveRuntimeImpl part = ProActiveRuntimeImpl.getProActiveRuntime();
        ProActiveDescriptorInternal pad;
        try {
            if (!hierarchicalSearch) {
                //if not hierarchical search, we assume that the descriptor might has been
                //register with the default jobID
                pad = part.getDescriptor(xmlDescriptorUrl +
                        ProActiveObject.getJobId(), hierarchicalSearch);
            } else {
                pad = part.getDescriptor(xmlDescriptorUrl, hierarchicalSearch);
            }
        } catch (Exception e) {
            throw new ProActiveException(e);
        }

        // if pad found, returns it
        if (pad != null) {
            return pad;
        }

        // else parses it
        try {
            if (ProDeployment.logger.isInfoEnabled()) {
                ProDeployment.logger.info(
                    "************* Reading deployment descriptor: " +
                    xmlDescriptorUrl + " ********************");
            }
            JaxpDescriptorParser parser = new JaxpDescriptorParser(xmlDescriptorUrl,
                    variableContract);
            parser.parse();
            pad = parser.getProActiveDescriptor();
            part.registerDescriptor(pad.getUrl(), pad);
            return pad;
        } catch (org.xml.sax.SAXException e) {
            //e.printStackTrace(); hides errors when testing parameters in xml descriptors
            ProDeployment.logger.fatal(
                "A problem occured when getting the proActiveDescriptor at location \"" +
                xmlDescriptorUrl + "\"." + e.getMessage());
            throw new ProActiveException(
                "A problem occured when getting the proActiveDescriptor at location \"" +
                xmlDescriptorUrl + "\"." + e.getMessage(), e);
        } catch (java.io.IOException e) {
            //e.printStackTrace(); hides errors when testing parameters in xml descriptors
            ProDeployment.logger.fatal(
                "An IO problem occured when getting the proActiveDescriptor at location \"" +
                xmlDescriptorUrl + "\".");
            throw new ProActiveException(e);
        }
    }

    /**
     * return the pad matching with the given url or parse it from the file system
     * @param xmlDescriptorUrl url of the pad
     * @param hierarchicalSearch must search in hierarchy ?
     * @return the pad found or a new pad parsed from xmlDescriptorUrl
     * @throws ProActiveException
     * @throws RemoteException
     */
    private static ProActiveDescriptorInternal internalGetProActiveDescriptor_old(
        String xmlDescriptorUrl, VariableContract variableContract,
        boolean hierarchicalSearch) throws ProActiveException {
        RuntimeFactory.getDefaultRuntime();
        if (xmlDescriptorUrl.indexOf(':') == -1) {
            xmlDescriptorUrl = "file:" + xmlDescriptorUrl;
        }
        ProActiveRuntimeImpl part = ProActiveRuntimeImpl.getProActiveRuntime();
        ProActiveDescriptorInternal pad;
        try {
            if (!hierarchicalSearch) {
                //if not hierarchical search, we assume that the descriptor might has been
                //register with the default jobID
                pad = part.getDescriptor(xmlDescriptorUrl +
                        ProActiveObject.getJobId(), hierarchicalSearch);
            } else {
                pad = part.getDescriptor(xmlDescriptorUrl, hierarchicalSearch);
            }
        } catch (Exception e) {
            throw new ProActiveException(e);
        }

        // if pad found, returns it
        if (pad != null) {
            return pad;
        }

        // else parses it
        try {
            if (ProDeployment.logger.isInfoEnabled()) {
                ProDeployment.logger.info(
                    "************* Reading deployment descriptor: " +
                    xmlDescriptorUrl + " ********************");
            }
            ProActiveDescriptorHandler proActiveDescriptorHandler = ProActiveDescriptorHandler.createProActiveDescriptor(xmlDescriptorUrl,
                    variableContract);
            pad = (ProActiveDescriptorInternal) proActiveDescriptorHandler.getResultObject();
            part.registerDescriptor(pad.getUrl(), pad);
            return pad;
        } catch (org.xml.sax.SAXException e) {
            //e.printStackTrace(); hides errors when testing parameters in xml descriptors
            ProDeployment.logger.fatal(
                "A problem occured when getting the proActiveDescriptor at location \"" +
                xmlDescriptorUrl + "\".");
            throw new ProActiveException(
                "A problem occured when getting the proActiveDescriptor at location \"" +
                xmlDescriptorUrl + "\".", e);
        } catch (java.io.IOException e) {
            //e.printStackTrace(); hides errors when testing parameters in xml descriptors
            ProDeployment.logger.fatal(
                "A problem occured when getting the proActiveDescriptor at location \"" +
                xmlDescriptorUrl + "\".");
            throw new ProActiveException(e);
        }
    }

    /**
     * Looks-up a VirtualNode previously registered in a registry(RMI or HTTP or IBIS)
     * The registry where to look for is fully determined with the protocol included in the url
     * @param url The url where to perform the lookup. The url takes the following form:
     * protocol://machine_name:port/name. Protocol and port can be ommited if respectively RMI and 1099:
     * //machine_name/name
     * @return VirtualNode The virtualNode returned by the lookup
     * @throws ProActiveException If no objects are bound with the given url
     */
    public static VirtualNode lookupVirtualNode(String url)
        throws ProActiveException {
        ProActiveRuntime remoteProActiveRuntime = null;
        remoteProActiveRuntime = RuntimeFactory.getRuntime(URIBuilder.buildVirtualNodeUrl(
                    url).toString());
        return remoteProActiveRuntime.getVirtualNode(URIBuilder.getNameFromURI(
                url));
    }

    /**
     * Registers locally the given VirtualNode in a registry such RMIRegistry or HTTP registry.
     * The VirtualNode to register must exist on the local runtime. This is done when using XML Deployment Descriptors
     * @param virtualNode the VirtualNode to register.
     * @param registrationProtocol The protocol used for registration or null in order to use the protocol used to start the jvm.
     * At this time RMI, HTTP, IBIS are supported. If set to null, the registration protocol will be set to the system property:
     * proactive.communication.protocol
     * @param replacePreviousBinding
     * @throws ProActiveException If the VirtualNode with the given name has not been yet activated or does not exist on the local runtime
     */
    public static void registerVirtualNode(VirtualNode virtualNode,
        String registrationProtocol, boolean replacePreviousBinding)
        throws ProActiveException, AlreadyBoundException {
        if (!(virtualNode instanceof VirtualNodeImpl)) {
            throw new ProActiveException(
                "Cannot register such virtualNode since it results from a lookup!");
        }
        if (registrationProtocol == null) {
            registrationProtocol = PAProperties.PA_COMMUNICATION_PROTOCOL.getValue();
        }
        String virtualnodeName = virtualNode.getName();
        ProActiveRuntime part = RuntimeFactory.getProtocolSpecificRuntime(registrationProtocol);
        VirtualNodeInternal vn = part.getVirtualNode(virtualnodeName);
        if (vn == null) {
            throw new ProActiveException("VirtualNode " + virtualnodeName +
                " has not been yet activated or does not exist! Try to activate it first !");
        }
        part.registerVirtualNode(URIBuilder.appendVnSuffix(virtualnodeName),
            replacePreviousBinding);
    }

    /**
     * Unregisters the virtualNode previoulsy registered in a registry such RMI.
     * Calling this method removes the VirtualNode from the local runtime.
     * @param virtualNode The VirtualNode to unregister
     * @throws ProActiveException if a problem occurs whle unregistering the VirtualNode
     */
    public static void unregisterVirtualNode(VirtualNode virtualNode)
        throws ProActiveException {
        //VirtualNode vn = ((VirtualNodeStrategy)virtualNode).getVirtualNode();
        if (!(virtualNode instanceof VirtualNodeImpl)) {
            throw new ProActiveException(
                "Cannot unregister such virtualNode since it results from a lookup!");
        }
        String virtualNodeName = virtualNode.getName();
        ProActiveRuntime part = RuntimeFactory.getProtocolSpecificRuntime(((VirtualNodeImpl) virtualNode).getRegistrationProtocol());
        part.unregisterVirtualNode(URIBuilder.appendVnSuffix(
                virtualNode.getName()));
        if (ProDeployment.logger.isInfoEnabled()) {
            ProDeployment.logger.info("Success at unbinding " +
                virtualNodeName);
        }
    }
}
