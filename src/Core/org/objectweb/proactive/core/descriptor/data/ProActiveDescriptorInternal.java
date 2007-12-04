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
package org.objectweb.proactive.core.descriptor.data;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.services.ServiceUser;
import org.objectweb.proactive.core.descriptor.services.TechnicalService;
import org.objectweb.proactive.core.descriptor.services.TechnicalServiceXmlType;
import org.objectweb.proactive.core.descriptor.services.UniversalService;
import org.objectweb.proactive.core.process.AbstractSequentialListProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.ExternalProcessDecorator;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.process.filetransfer.FileTransferDefinition;
import org.objectweb.proactive.core.security.PolicyServer;
import org.objectweb.proactive.core.xml.VariableContract;


/**
 *
 * <p> <code>ProActiveDescriptorInternal</code> is the internal interface
 * to be used by ProActive developper to manipulate a ProActiveDescriptor.
 * Methods provided by this interface allow the initialization and modification
 * of this <code>ProActiveDescriptor</code></p>
 *
 * <p>This interface is not public and not supported.</p>
 *
 * @author  ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.3
 * @see VirtualNodeInternal
 * @see VirtualMachine
 */
public interface ProActiveDescriptorInternal extends ProActiveDescriptor {
    public void setMainDefined(boolean mainDefined);

    /**
     * Creates a new MainDefinition object and add it to the map
     *
     */
    public void createMainDefinition(String id);

    /**
     * Sets the mainClass attribute of the last defined mainDefinition
     * @param mainClass fully qualified name of the mainclass
     */
    public void mainDefinitionSetMainClass(String mainClass);

    /**
     * Adds the parameter parameter to the parameters of the last
     * defined mainDefinition
     * @param parameter parameter to add
     */
    public void mainDefinitionAddParameter(String parameter);

    /**
     * Adds a VirtualNode virtualNode to the last defined mainDefinition
     * @param virtualNode VirtualNode to add
     */
    public void mainDefinitionAddVirtualNode(VirtualNodeInternal virtualNode);

    /**
     * return true if at least one mainDefinition is defined
     * @return true if at least one mainDefinition is defined
     */
    public boolean isMainDefined();

    /**
     * Activates all mains of mainDefinitions defined
     *
     */
    public void activateMains();

    /**
     * Activates the main of the id-th mainDefinition
     * @param mainDefinitionId key identifying a mainDefinition
     */
    public void activateMain(String mainDefinitionId);

    /**
     * Returns a table containing all the parameters of the last
     * defined mainDefinition
     * @param mainDefinitionId key identifying a mainDefinition
     * @return a table of String containing all the parameters of the mainDefinition
     */
    public String[] mainDefinitionGetParameters(String mainDefinitionId);

    /**
     * Returns the main definitions mapping
     * @return Map
     */
    public Map<String, MainDefinition> getMainDefinitionMapping();

    /**
     * Returns the virtual nodes mapping
     * @return Map
     */
    public Map<String, VirtualNodeInternal> getVirtualNodeMapping();

    public void setMainDefinitionMapping(
        HashMap<String, MainDefinition> newMapping);

    public void setVirtualNodeMapping(
        HashMap<String, VirtualNodeInternal> newMapping);

    /**
     * Returns a table containing all mainDefinitions conserving order
     * @return a table containing all mainDefinitions conserving order
     */
    public MainDefinition[] getMainDefinitions();

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
    public VirtualNodeInternal createVirtualNode(String vnName, boolean lookup);

    /**
     * Creates a VirtualNode with the given name
     * If the VirtualNode with the given name has previously been created, this method returns it.
     * @param vnName
     * @param lookup if true, at creation time the VirtualNode will be a VirtualNodeLookup.
     * @param isMainVN true if the virtual node is linked to a main definition
     * @return VirtualNode
     */
    public VirtualNodeInternal createVirtualNode(String vnName, boolean lookup,
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
     * Gets an instance of the FileTransfer description. If
     * an instance for this ID was already exists inside the pad
     * then this one is returned, else a new one is created.
     * @param fileTransferID The ID of the filetransfer
     * @return New or existing instance for the ID
     */
    public FileTransferDefinition getFileTransfer(String fileTransferID);

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

    /**
     * Keeps a reference to the Variable Contract passed as parameter
     * @param  properties The Variable Contract (ex XMLProperties)
     */
    public void setVariableContract(VariableContract properties);

    /**
     * Add the process given by the specified processID in the list of sequential processes.
     * @param sequentialListProcess
     * @param string a processID
     */
    public void addProcessToSequenceList(
        AbstractSequentialListProcessDecorator sequentialListProcess,
        String string);

    /**
     * Add the service given by the specified processID in the list of sequential services.
     * @param sequentialListProcess
     * @param string a processID
     */
    public void addServiceToSequenceList(
        AbstractSequentialListProcessDecorator sequentialListProcess,
        String string);

    /**
     * Add a technical service.
     * @param tsParsed id, class, and args.
     */
    public void addTechnicalService(TechnicalServiceXmlType tsParsed)
        throws Exception;

    public TechnicalService getTechnicalService(String technicalServiceId);
}
