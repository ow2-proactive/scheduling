/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.descriptor.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.core.descriptor.services.ServiceUser;
import org.objectweb.proactive.core.descriptor.services.UniversalService;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.ExternalProcessDecorator;
import org.objectweb.proactive.core.process.HierarchicalProcess;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.process.filetransfer.FileTransfer;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.ext.security.PolicyServer;
import org.objectweb.proactive.ext.security.ProActiveSecurityDescriptorHandler;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.exceptions.InvalidPolicyFile;


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
public class ProActiveDescriptorImpl implements ProActiveDescriptor {
    //
    //  ----- PRIVATE MEMBERS -----------------------------------------------------------------------------------
    //
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.DEPLOYMENT);
    private String lastMainDefinitionID;

    /** map keys with mainDefinitions */
    private Map mainDefinitionMapping;

    /** map virtualNode name and objects */
    private java.util.HashMap virtualNodeMapping;

    /** map jvm name and object */
    private java.util.HashMap virtualMachineMapping;

    /** map process id and process */
    private java.util.HashMap processMapping;

    /** map process id and process updater for later update of the process */
    private java.util.HashMap pendingProcessMapping;

    /** map process id and service */
    private java.util.HashMap serviceMapping;

    /** map process id and service updater for later update of the service */
    private java.util.HashMap pendingServiceMapping;

    /** map filetransfer-id and filetransfer */
    private java.util.HashMap fileTransferMapping;

    /** map of the variable contract (ex XMLProperties) */
    private VariableContract variableContract;
    
    /** Location of the xml file */
    private String url;
    private String jobID;
    private boolean mainDefined;

    /** security rules */
    protected ProActiveSecurityManager proactiveSecurityManager;
    public PolicyServer policyServer;

    //  public X509Certificate creatorCertificate;
    public String securityFile;

    //
    //  ----- CONSTRUCTORS -----------------------------------------------------------------------------------
    //

    /**
     * Contructs a new intance of ProActiveDescriptor
     */
    public ProActiveDescriptorImpl(String url) {
        mainDefinitionMapping = new HashMap();
        virtualNodeMapping = new java.util.HashMap();
        virtualMachineMapping = new java.util.HashMap();
        processMapping = new java.util.HashMap();
        pendingProcessMapping = new java.util.HashMap();
        serviceMapping = new java.util.HashMap();
        pendingServiceMapping = new java.util.HashMap();
        fileTransferMapping = new java.util.HashMap();
        variableContract = new VariableContract();
        this.url = url;
        mainDefined = false;
    }

    //
    //  ----- PUBLIC METHODS -----------------------------------------------------------------------------------
    //
    public String getUrl() {
        return this.url;
    }

    /**
     * create a new mainDefintion with a unique id defined by the append of
     * "mainDefinition:" + lastMainDefinitionID
     */
    public void createMainDefinition(String id) {
        lastMainDefinitionID = id;
        mainDefinitionMapping.put(id, new MainDefinition());
    }

    /**
     * set the mainClass attribute of the last defined mainDefinition
     * @param mainClass fully qualified name of the mainclass
     */
    public void mainDefinitionSetMainClass(String mainClass) {
        getMainDefinition().setMainClass(mainClass);
    }

    /**
     * add the parameter parameter to the parameters of the last
     * defined mainDefinition
     * @param parameter parameter to add
     */
    public void mainDefinitionAddParameter(String parameter) {
        getMainDefinition().addParameter(parameter);
    }

    /**
     * return an array that contains all the parameters of the last
     * defined mainDefinition
     * @param mainDefinitionId key identifying a mainDefinition
     * @return a table of String containing all the parameters of the mainDefinition
     */
    public String[] mainDefinitionGetParameters(String mainDefinitionId) {
        return getMainDefinition(mainDefinitionId).getParameters();
    }

    /**
     * add a VirtualNode virtualNode to the last defined mainDefinition
     * @param virtualNode VirtualNode to add
     */
    public void mainDefinitionAddVirtualNode(VirtualNode virtualNode) {
        getMainDefinition().addVirtualNode(virtualNode);
    }

    /**
     * return true if at least one mainDefinition is defined
     * @return true if at least one mainDefinition is defined
     */
    public boolean isMainDefined() {
        return mainDefined;
    }

    public void setMainDefined(boolean mainDefined) {
        this.mainDefined = mainDefined;
    }

    /**
     * activates all mains of mainDefinitions defined
     *
     */
    public void activateMains() {
        if (!isMainDefined()) {
            return;
        }

        Set mainsId = mainDefinitionMapping.keySet();
        Iterator it = mainsId.iterator();

        while (it.hasNext()) {
            String id = (String) it.next();
            activateMain(id);
        }
    }

    /**
     * activates the main of the id-th mainDefinition
     * @param mainDefinitionId key identifying a mainDefinition
     */
    public void activateMain(String mainDefinitionId) {
        MainDefinition mainDefinition = getMainDefinition(mainDefinitionId);

        if (mainDefinition != null) {
            mainDefinition.activateMain();
        }
    }

    /**
     * return the main definitions mapping
     * @return Map
     */
    public Map getMainDefinitionMapping() {
        return mainDefinitionMapping;
    }

    public void setMainDefinitionMapping(HashMap newMapping) {
        mainDefinitionMapping = newMapping;
    }

    /**
     * return the virtual nodes mapping
     * @return Map
     */
    public Map getVirtualNodeMapping() {
        return virtualNodeMapping;
    }

    public void setVirtualNodeMapping(HashMap newMapping) {
        virtualNodeMapping = newMapping;
    }

    /**
     *
     * @return an array containing all mainDefinitions conserving order
     */
    public MainDefinition[] getMainDefinitions() {
        MainDefinition[] mainDefinitions = new MainDefinition[mainDefinitionMapping.size()];
        Set mainsId = mainDefinitionMapping.keySet();
        Iterator it = mainsId.iterator();
        int i = 0;

        while (it.hasNext()) {
            String id = (String) it.next();
            mainDefinitions[i] = getMainDefinition(id);
            i++;
        }

        return mainDefinitions;
    }

    public VirtualNode[] getVirtualNodes() {
        int i = 0;
        VirtualNode[] virtualNodeArray = new VirtualNode[virtualNodeMapping.size()];
        Collection collection = virtualNodeMapping.values();

        for (Iterator iter = collection.iterator(); iter.hasNext();) {
            virtualNodeArray[i] = (VirtualNode) iter.next();
            i++;
        }

        return virtualNodeArray;
    }

    public VirtualNode getVirtualNode(String name) {
        return (VirtualNode) virtualNodeMapping.get(name);
    }

    public VirtualMachine getVirtualMachine(String name) {
        return (VirtualMachine) virtualMachineMapping.get(name);
    }

    public ExternalProcess getProcess(String name) {
        return (ExternalProcess) processMapping.get(name);
    }

    public ExternalProcess getHierarchicalProcess(String vmname) {
        VirtualMachine vm = getVirtualMachine(vmname);

        if (vm == null) {
            logger.warn("" + vmname + "cannot be found");

            return null;
        }

        if (vm.getProcess() instanceof HierarchicalProcess) {
            return ((HierarchicalProcess) vm.getProcess()).getHierarchicalProcess();
        } else {
            logger.warn("" + vmname +
                " does not contain a hierarchical process !");

            return null;
        }
    }

    public UniversalService getService(String serviceID) {
        return (UniversalService) serviceMapping.get(serviceID);
    }

    /**
     * Creates a new FileTransfer definition, and maps this definition
     * in the PAD internal mapping. Direct usage of this function is
     * discouraged, since no pre-existance checking of the definition
     * is made. Instead use the public method getFileTransfer(String)
     * factory.
     * @param fileTransferID
     * @return a new FileTransfer definition
     */
    protected FileTransfer createFileTransferDefinition(String fileTransferID) {
        FileTransfer ft = new FileTransfer(fileTransferID);
        fileTransferMapping.put(fileTransferID, ft);

        if (logger.isDebugEnabled()) {
            logger.debug("created FileTransfer id=" + fileTransferID);
        }

        return ft;
    }

    public VirtualNode createVirtualNode(String vnName, boolean lookup) {
        return createVirtualNode(vnName, lookup, false);
    }

    public VirtualNode createVirtualNode(String vnName, boolean lookup,
        boolean isMainNode) {
        VirtualNode vn = getVirtualNode(vnName);

        if (jobID == null) {
            if (isMainDefined()) {
                this.jobID = generateNewJobID();

                //System.out.println("new id generated : " + jobID);
            } else {
                this.jobID = ProActive.getJobId();

                //System.out.println("using runtime id : " + jobID);
            }

            this.url = url + this.jobID;
        }

        if (vn == null) {
            if (lookup) {
                vn = new VirtualNodeLookup(vnName);
            } else {
                vn = new VirtualNodeImpl(vnName, proactiveSecurityManager,
                        this.url, isMainNode);
                ((VirtualNodeImpl) vn).jobID = this.jobID;

                //System.out.println("vn created with url: " + padURL + " and jobid : " + ((VirtualNodeImpl) vn).jobID);
            }

            virtualNodeMapping.put(vnName, vn);

            if (logger.isInfoEnabled()) {
                logger.info("created VirtualNode name=" + vnName);
            }
        }

        return vn;
    }

    public VirtualMachine createVirtualMachine(String vmName) {
        VirtualMachine vm = getVirtualMachine(vmName);

        if (vm == null) {
            vm = new VirtualMachineImpl();
            vm.setName(vmName);
            virtualMachineMapping.put(vmName, vm);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("created virtualMachine name=" + vmName);
        }

        return vm;
    }

    public ExternalProcess createProcess(String processID,
        String processClassName) throws ProActiveException {
        ExternalProcess process = getProcess(processID);

        if (process == null) {
            process = createProcess(processClassName);
            addExternalProcess(processID, process);
        }

        return process;
    }

    public ExternalProcess createProcess(String processClassName)
        throws ProActiveException {
        try {
            Class processClass = Class.forName(processClassName);
            ExternalProcess process = (ExternalProcess) processClass.newInstance();

            return process;
        } catch (ClassNotFoundException e) {
            throw new ProActiveException(e);
        } catch (InstantiationException e) {
            throw new ProActiveException(e);
        } catch (IllegalAccessException e) {
            throw new ProActiveException(e);
        }
    }

    public void registerProcess(VirtualMachine virtualMachine, String processID) {
        ExternalProcess process = getProcess(processID);

        if (process == null) {
            addPendingProcess(processID, virtualMachine);
        } else {
            virtualMachine.setProcess(process);
        }
    }

    public synchronized FileTransfer getFileTransfer(String fileTransferID) {
        //TODO throw new ProActiveException 
        //if(fileTransferID.equalsIgnoreCase("implicit")) throw new ProActiveException();
        FileTransfer ft = (FileTransfer) fileTransferMapping.get(fileTransferID);

        if (ft == null) {
            ft = createFileTransferDefinition(fileTransferID);
            fileTransferMapping.put(fileTransferID, ft);

            if (logger.isDebugEnabled()) {
                logger.debug("created FileTransfer id=" + fileTransferID);
            }
        }

        return ft;
    }

    public void registerProcess(ExternalProcessDecorator compositeProcess,
        String processID) {
        ExternalProcess process = getProcess(processID);

        if (process == null) {
            addPendingProcess(processID, compositeProcess);
        } else {
            compositeProcess.setTargetProcess(process);
        }
    }

    public void mapToExtendedJVM(JVMProcess jvmProcess, String processID)
        throws ProActiveException {
        try {
            JVMProcessImpl process = (JVMProcessImpl) getProcess(processID);

            if (process == null) {
                throw new ProActiveException("The jvmProcess with id " +
                    processID +
                    " is not yet defined in the descriptor. The extended jvmProcess must be defined before all jvmProcesses that extend it");

                //addPendingJVMProcess(processID, jvmProcess);
            } else {
                jvmProcess.setExtendedJVM(process);
            }
        } catch (ClassCastException e) {
            logger.fatal(
                "ERROR: a jvmProcess can only extend another jvmProcess. Correct the Descriptor");
            e.printStackTrace();
        }
    }

    public void registerHierarchicalProcess(HierarchicalProcess hp,
        String processID) {
        ExternalProcess process = getProcess(processID);

        if (process == null) {
            addHierarchicalPendingProcess(processID, hp);
        } else {
            hp.setHierarchicalProcess(process);
        }
    }

    public void addService(String serviceID, UniversalService service) {
        ServiceUpdater serviceUpdater = (ServiceUpdater) pendingServiceMapping.remove(serviceID);

        if (serviceUpdater != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Updating Service name=" + serviceID);
            }

            serviceUpdater.updateService(service);
        }

        processMapping.put(serviceID, service);
    }

    public void registerService(ServiceUser serviceUser, String serviceID) {
        UniversalService service = getService(serviceID);

        if (service == null) {
            addPendingService(serviceID, serviceUser);
        } else {
            try {
                serviceUser.setService(service);
            } catch (ProActiveException e) {
                e.printStackTrace();
                logger.error("the given service " + service.getServiceName() +
                    " cannot be set for class " + serviceUser.getUserClass());
            }
        }
    }

    public void activateMappings() {
        VirtualNode[] virtualNodeArray = getVirtualNodes();

        for (int i = 0; i < virtualNodeArray.length; i++) {
            virtualNodeArray[i].activate();
        }
    }

    public void activateMapping(String virtualNodeName) {
        VirtualNode virtualNode = getVirtualNode(virtualNodeName);
        virtualNode.activate();
    }

    public void killall(boolean softly) throws ProActiveException {
        ProActiveRuntimeImpl part = (ProActiveRuntimeImpl) ProActiveRuntimeImpl.getProActiveRuntime();

        part.removeDescriptor(this.url);

        VirtualNode[] vnArray = getVirtualNodes();

        for (int i = 0; i < vnArray.length; i++) {
            vnArray[i].killAll(softly);
            virtualNodeMapping.remove(vnArray[i].getName());

            //vnArray[i] = null;		
        }
    }

    /**
     * Returns the size of virualNodeMapping HashMap
     * @return int
     */
    public int getVirtualNodeMappingSize() {
        return virtualNodeMapping.size();
    }

    // SECURITY

    /**
     * Initialize application security policy
     * @param file link to the XML security policy file
     */
    public void createProActiveSecurityManager(String file) {
        securityFile = file;

        try {
            policyServer = ProActiveSecurityDescriptorHandler.createPolicyServer(file);
            proactiveSecurityManager = new ProActiveSecurityManager(policyServer);

            // set the security policyserver to the default proactive meta object
            // by the way, the HalfBody will be associated to a security manager
            // derivated from this one.
            ProActiveSecurityManager psm = proactiveSecurityManager.generateSiblingCertificate(
                    "HalfBody");
            ProActiveMetaObjectFactory.newInstance()
                                      .setProActiveSecurityManager(psm);
        } catch (InvalidPolicyFile e) {
            e.printStackTrace();
        }
    }

    public PolicyServer getPolicyServer() {
        return policyServer;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor#getSecurityFilePath()
     */
    public String getSecurityFilePath() {
        return securityFile;
    }

    //
    //  ----- PROTECTED METHODS -----------------------------------------------------------------------------------
    //
    //
    //  ----- PRIVATE METHODS -----------------------------------------------------------------------------------
    //

    /**
     * return the main definition matching with the id mainDefinitionID
     * @param mainDefinitionID Id of the mainDefinition
     * @return MainDefinition
     */
    private MainDefinition getMainDefinition(String mainDefinitionID) {
        return (MainDefinition) mainDefinitionMapping.get(mainDefinitionID);
    }

    /**
     * return the last main definition added
     * @return MainDefinition
     */
    private MainDefinition getMainDefinition() {
        return getMainDefinition(lastMainDefinitionID);
    }

    private void addExternalProcess(String processID, ExternalProcess process) {
        ProcessUpdater processUpdater = (ProcessUpdater) pendingProcessMapping.remove(processID);

        if (processUpdater != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Updating Process name=" + processID);
            }

            processUpdater.updateProcess(process);
        }

        processMapping.put(processID, process);
    }

    private void addPendingProcess(String processID,
        VirtualMachine virtualMachine) {
        ProcessUpdater updater = new VirtualMachineUpdater(virtualMachine);

        //pendingProcessMapping.put(processID, updater);
        addProcessUpdater(processID, updater);
    }

    private void addPendingProcess(String processID,
        ExternalProcessDecorator compositeProcess) {
        ProcessUpdater updater = new CompositeExternalProcessUpdater(compositeProcess);

        //pendingProcessMapping.put(processID, updater);
        addProcessUpdater(processID, updater);
    }

    private void addHierarchicalPendingProcess(String processID,
        HierarchicalProcess hp) {
        ProcessUpdater updater = new HierarchicalProcessUpdater(hp);
        addProcessUpdater(processID, updater);
    }

    //Commented in the fist version of jvm extension
    //    private void addPendingJVMProcess(String processID, JVMProcess jvmProcess) {
    //        ProcessUpdater updater = new ExtendedJVMProcessUpdater(jvmProcess);
    //
    //        //pendingProcessMapping.put(processID, updater);
    //        addProcessUpdater(processID, updater);
    //    }
    private void addProcessUpdater(String processID,
        ProcessUpdater processUpdater) {
        CompositeProcessUpdater compositeProcessUpdater = (CompositeProcessUpdater) pendingProcessMapping.get(processID);

        if (compositeProcessUpdater == null) {
            compositeProcessUpdater = new CompositeProcessUpdater();

            //pendingProcessMapping.put(processID, processUpdater);
            pendingProcessMapping.put(processID, compositeProcessUpdater);
        }

        compositeProcessUpdater.addProcessUpdater(processUpdater);
    }

    private void addPendingService(String serviceID, ServiceUser serviceUser) {
        ServiceUpdater updater = null;

        if (serviceUser instanceof VirtualMachine) {
            updater = new VirtualMachineUpdater((VirtualMachine) serviceUser);
        } else {
            updater = new ServiceUpdaterImpl(serviceUser);
        }

        addServiceUpdater(serviceID, updater);
    }

    private void addServiceUpdater(String serviceID,
        ServiceUpdater serviceUpdater) {
        CompositeServiceUpdater compositeServiceUpdater = (CompositeServiceUpdater) pendingServiceMapping.get(serviceID);

        if (compositeServiceUpdater == null) {
            compositeServiceUpdater = new CompositeServiceUpdater();

            //pendingProcessMapping.put(processID, processUpdater);
            pendingServiceMapping.put(serviceID, compositeServiceUpdater);
        }

        compositeServiceUpdater.addServiceUpdater(serviceUpdater);
    }

    /**
     * generate a new jobId based on the current time.
     *
     * @return a new jobId
     */
    private String generateNewJobID() {
        return "JOB-" + System.currentTimeMillis();
    }

    //
    //  ----- INNER CLASSES -----------------------------------------------------------------------------------
    //
    private interface ServiceUpdater {
        public void updateService(UniversalService service);
    }

    private interface ProcessUpdater {
        public void updateProcess(ExternalProcess p);
    }

    private class CompositeServiceUpdater implements ServiceUpdater {
        private java.util.ArrayList updaterList;

        public CompositeServiceUpdater() {
            updaterList = new java.util.ArrayList();
        }

        public void addServiceUpdater(ServiceUpdater s) {
            updaterList.add(s);
        }

        public void updateService(UniversalService s) {
            java.util.Iterator it = updaterList.iterator();

            while (it.hasNext()) {
                ServiceUpdater serviceUpdater = (ServiceUpdater) it.next();
                serviceUpdater.updateService(s);
            }

            updaterList.clear();
        }
    }

    private class CompositeProcessUpdater implements ProcessUpdater {
        private java.util.ArrayList updaterList;

        public CompositeProcessUpdater() {
            updaterList = new java.util.ArrayList();
        }

        public void addProcessUpdater(ProcessUpdater p) {
            updaterList.add(p);
        }

        public void updateProcess(ExternalProcess p) {
            java.util.Iterator it = updaterList.iterator();

            while (it.hasNext()) {
                ProcessUpdater processUpdater = (ProcessUpdater) it.next();
                processUpdater.updateProcess(p);
            }

            updaterList.clear();
        }
    }

    private class CompositeExternalProcessUpdater implements ProcessUpdater {
        private ExternalProcessDecorator compositeExternalProcess;

        public CompositeExternalProcessUpdater(
            ExternalProcessDecorator compositeExternalProcess) {
            this.compositeExternalProcess = compositeExternalProcess;
        }

        public void updateProcess(ExternalProcess p) {
            if (logger.isDebugEnabled()) {
                logger.debug("Updating CompositeExternal Process");
            }

            compositeExternalProcess.setTargetProcess(p);
        }
    }

    private class HierarchicalProcessUpdater implements ProcessUpdater {
        private HierarchicalProcess hp;

        public HierarchicalProcessUpdater(HierarchicalProcess hp) {
            this.hp = hp;
        }

        public void updateProcess(ExternalProcess p) {
            if (logger.isDebugEnabled()) {
                logger.debug("Updating Hierarchical Process");
            }

            hp.setHierarchicalProcess(p);
        }
    }

    //  Commented in the fist version of jvm extension
    //    private class ExtendedJVMProcessUpdater implements ProcessUpdater {
    //        private JVMProcess jvmProcess;
    //
    //        public ExtendedJVMProcessUpdater(JVMProcess jvmProcess) {
    //            this.jvmProcess = jvmProcess;
    //        }
    //
    //        public void updateProcess(ExternalProcess p) {
    //            jvmProcess.setExtendedJVM((JVMProcessImpl) p);
    //        }
    //    }
    private class VirtualMachineUpdater implements ProcessUpdater,
        ServiceUpdater {
        private VirtualMachine virtualMachine;

        public VirtualMachineUpdater(VirtualMachine virtualMachine) {
            this.virtualMachine = virtualMachine;
        }

        public void updateProcess(ExternalProcess p) {
            if (logger.isDebugEnabled()) {
                logger.debug("Updating VirtualMachine Process");
            }

            virtualMachine.setProcess(p);
        }

        public void updateService(UniversalService s) {
            if (logger.isDebugEnabled()) {
                logger.debug("Updating VirtualMachine Service");
            }

            virtualMachine.setService(s);
        }
    }

    private class ServiceUpdaterImpl implements ServiceUpdater {
        private ServiceUser serviceUser;

        public ServiceUpdaterImpl(ServiceUser serviceUser) {
            this.serviceUser = serviceUser;
        }

        public void updateService(UniversalService s) {
            if (logger.isDebugEnabled()) {
                logger.debug("Updating VirtualMachine Service");
            }

            try {
                serviceUser.setService(s);
            } catch (ProActiveException e) {
                e.printStackTrace();
                logger.error("the given service " + s.getServiceName() +
                    " cannot be set for class " + serviceUser.getUserClass());
            }
        }
    }

	/**
	 * @see org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor#setVariableContract(org.objectweb.proactive.core.xml.XMLProperties)
	 */
	public void setVariableContract(VariableContract variableContract) {
		this.variableContract=variableContract;
	}

	/**
	 * @see org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor#getVariableContract()
	 */
	public VariableContract getVariableContract() {
		return this.variableContract;
	}
}
