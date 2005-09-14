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
package org.objectweb.proactive.core.descriptor.data;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.services.ServiceUser;
import org.objectweb.proactive.core.descriptor.services.UniversalService;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.ExternalProcessDecorator;
import org.objectweb.proactive.core.process.HierarchicalProcess;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.ext.security.PolicyServer;


/**
 * <p>
 * A <code>ProactiveDescriptor</code> is an internal representation of XML
 * Descriptor. It offers a set of services to access/activate/desactivate
 * <code>VirtualNode</code>.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.3
 * @see VirtualNode
 * @see VirtualMachine
 */
public interface ProActiveDescriptor extends java.io.Serializable {

    /**
     * return the Url of the pad
     * @return String in fact it is an identifire for the pad that is returned.
     * This identifier is build from the pad url appended with the pad's jobId.
     */
    public String getUrl();

    public void setMainDefined(boolean mainDefined);

    /**
     * create a new MainDefinition object and add it to the map
     *
     */
    public void createMainDefinition(String id);

    /**
     * set the mainClass attribute of the last defined mainDefinition
     * @param mainClass fully qualified name of the mainclass
     */
    public void mainDefinitionSetMainClass(String mainClass);

    /**
     * add the parameter parameter to the parameters of the last
     * defined mainDefinition
     * @param parameter parameter to add
     */
    public void mainDefinitionAddParameter(String parameter);

    /**
     * add a VirtualNode virtualNode to the last defined mainDefinition
     * @param virtualNode VirtualNode to add
     */
    public void mainDefinitionAddVirtualNode(VirtualNode virtualNode);

    /**
     * return true if at least one mainDefinition is defined
     * @return true if at least one mainDefinition is defined
     */
    public boolean isMainDefined();

    /**
     * activates all mains of mainDefinitions defined
     *
     */
    public void activateMains();

    /**
     * activates the main of the id-th mainDefinition
     * @param mainDefinitionId key identifying a mainDefinition
     */
    public void activateMain(String mainDefinitionId);

    /**
     * return a table containing all the parameters of the last
     * defined mainDefinition
     * @param mainDefinitionId key identifying a mainDefinition
     * @return a table of String containing all the parameters of the mainDefinition
     */
    public String[] mainDefinitionGetParameters(String mainDefinitionId);

    /**
     * return the main definitions mapping
     * @return Map
     */
    public Map getMainDefinitionMapping();

    /**
     * return the virtual nodes mapping
     * @return Map
     */
    public Map getVirtualNodeMapping();

    public void setMainDefinitionMapping(HashMap newMapping);

    public void setVirtualNodeMapping(HashMap newMapping);

    /**
     *
     * @return a table containing all mainDefinitions conserving order
     */
    public MainDefinition[] getMainDefinitions();

    /**
     * Returns all VirtualNodes described in the XML Descriptor
     * @return VirtualNode[] all the VirtualNodes described in the XML Descriptor
     */
    public VirtualNode[] getVirtualNodes();

    /**
     * Returns the specified VirtualNode
     * @param name name of the VirtualNode
     * @return VirtualNode VirtualNode of the given name
     */
    public VirtualNode getVirtualNode(String name);

    /**
     * Returns the VitualMachine of the given name
     * @param name
     * @return VirtualMachine
     */
    public VirtualMachine getVirtualMachine(String name);

    /**
     * Returns the Process of the given name
     * @param name
     * @return ExternalProcess
     */
    public ExternalProcess getProcess(String name);

    /**
     * Returns the process to hierarchically deploy
     * @param vmname
     * @return
     */
    public ExternalProcess getHierarchicalProcess(String vmname);

    /**
     * Returns the Service of the given name
     * @param serviceID
     * @return an UniversalService
     */
    public UniversalService getService(String serviceID);

    /**
     * Creates a VirtualNode with the given name
     * If the VirtualNode with the given name has previously been created, this method returns it.
     * @param vnName
     * @param lookup if true, at creation time the VirtualNode will be a VirtualNodeLookup.
     * If false the created VirtualNode is a VirtualNodeImpl. Once the VirtualNode created this field
     * has no more influence when calling this method
     * @return VirtualNode
     */
    public VirtualNode createVirtualNode(String vnName, boolean lookup);

    /**
     * Creates a VirtualNode with the given name
     * If the VirtualNode with the given name has previously been created, this method returns it.
     * @param vnName
     * @param lookup if true, at creation time the VirtualNode will be a VirtualNodeLookup.
     * @param isMainVN true if the virtual node is linked to a main definition
     * @return VirtualNode
     */
    public VirtualNode createVirtualNode(String vnName, boolean lookup,
        boolean isMainVN);

    /**
     * Creates a VirtualMachine of the given name
     * @param vmName
     * @return VirtualMachine
     */
    public VirtualMachine createVirtualMachine(String vmName);

    /**
     * Creates an ExternalProcess of the given className with the specified ProcessID
     * @param processID
     * @param processClassName
     * @throws ProActiveException if a problem occurs during process creation
     */
    public ExternalProcess createProcess(String processID,
        String processClassName) throws ProActiveException;

    /**
     * Updates with the effective service, all objects that are mapped with the serviceID.
     * It updates the table where is stored the mapping serviceID/service and link the
     * VirtualMachine that references the serviceID with the effective service
     * @param serviceID
     * @param service
     */
    public void addService(String serviceID, UniversalService service);

    /**
     * Returns a new instance of ExternalProcess from processClassName
     * @param processClassName
     * @throws ProActiveException if a problem occurs during process creation
     */
    public ExternalProcess createProcess(String processClassName)
        throws ProActiveException;

    /**
     * Maps the process given by the specified processID with the specified virtualMachine.
     * @param virtualMachine
     * @param processID
     */
    public void registerProcess(VirtualMachine virtualMachine, String processID);

    /**
     * Registers the specified composite process with the specified processID.
     * @param compositeProcess
     * @param processID
     */
    public void registerProcess(ExternalProcessDecorator compositeProcess,
        String processID);

    /**
     * Registers the specified hierarchical process with the specified processID.
     * @param hp
     * @param processID
     */
    public void registerHierarchicalProcess(HierarchicalProcess hp,
        String processID);

    /**
     * Maps the given jvmProcess with the extended JVMProcess defined with processID.
     * @param jvmProcess the jvm defined in the descriptor that contains the extendedJvm clause
     * @param processID id of the extended jvm
     * @throws ProActiveException if the jvm with the given id does not exist.
     * In fact, it means that if the extended jvm is defined later on in the descriptor the exception
     * is thrown. The extended jvm must be defined before every other jvms that extend it.
     */
    public void mapToExtendedJVM(JVMProcess jvmProcess, String processID)
        throws ProActiveException;

    /**
     * Maps the service given by the specified serviceID with the specified virtualMachine.
     * @param serviceUser
     * @param serviceId
     */
    public void registerService(ServiceUser serviceUser, String serviceId);

    /**
     * Activates all VirtualNodes defined in the XML Descriptor.
     */
    public void activateMappings();

    /**
     * Activates the specified VirtualNode defined in the XML Descriptor
     * @param virtualNodeName name of the VirtulNode to be activated
     */
    public void activateMapping(String virtualNodeName);

    /**
     * Kills all Nodes and JVMs(local or remote) created when activating the descriptor
     * @param softly if false, all jvms created when activating the descriptor are killed abruptely
     * if true a jvm that originates the creation of  a rmi registry waits until registry is empty before
     * dying. To be more precise a thread is created to ask periodically the registry if objects are still
     * registered.
     * @throws ProActiveException if a problem occurs when terminating all jvms
     */
    public void killall(boolean softly) throws ProActiveException;

    //  /**
    //   * Kills all Nodes mapped to VirtualNodes in the XML Descriptor
    //   * This method kills also the jvm on which 
    //   */
    //  public void desactivateMapping();
    //  
    //  
    //  /**
    //   * Kills all Nodes mapped to the specified VirtualNode in the XML Descriptor
    //   * @param vitualNodeName name of the virtualNode to be desactivated
    //   */
    //  public void desactivateMapping(String virtualNodeName);

    /**
     * Returns the size of virualNodeMapping HashMap
     * @return int
     */
    public int getVirtualNodeMappingSize();

    // SECURITY

    /**
     * Creates the initial Security Manager associated to an application
     * @param file contains all related security information for the application :
     * certificate, policy rules, ...
     */
    public void createProActiveSecurityManager(String file);

    public PolicyServer getPolicyServer();

    public String getSecurityFilePath();
}
