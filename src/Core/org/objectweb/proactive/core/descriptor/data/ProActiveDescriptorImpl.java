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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.core.descriptor.services.ServiceUser;
import org.objectweb.proactive.core.descriptor.services.TechnicalService;
import org.objectweb.proactive.core.descriptor.services.TechnicalServiceWrapper;
import org.objectweb.proactive.core.descriptor.services.TechnicalServiceXmlType;
import org.objectweb.proactive.core.descriptor.services.UniversalService;
import org.objectweb.proactive.core.process.AbstractSequentialListProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.ExternalProcessDecorator;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.process.filetransfer.FileTransferDefinition;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.security.PolicyServer;
import org.objectweb.proactive.core.security.ProActiveSecurityDescriptorHandler;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.SecurityConstants.EntityType;
import org.objectweb.proactive.core.security.exceptions.InvalidPolicyFile;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.xml.VariableContractImpl;


/**
 * <p>
 * A <code>ProactiveDescriptor</code> is an internal representation of XML
 * Descriptor. It offers a set of services to access/activate/desactivate
 * <code>VirtualNode</code>.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.3
 * @see VirtualNodeInternal
 * @see VirtualMachine
 */
public class ProActiveDescriptorImpl implements ProActiveDescriptorInternal {

    /**
     *
     */

    //
    //  ----- PRIVATE MEMBERS -----------------------------------------------------------------------------------
    //
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.DEPLOYMENT);
    private String lastMainDefinitionID;

    /** map keys with mainDefinitions */
    private Map<String, MainDefinition> mainDefinitionMapping;

    /** map virtualNode name and objects */
    private java.util.HashMap<String, VirtualNodeInternal> virtualNodeMapping;

    /** map jvm name and object */
    private java.util.HashMap<String, VirtualMachine> virtualMachineMapping;

    /** map process id and process */
    @SuppressWarnings("unchecked")
    // don't know how to describe that that map can contain either process or service
    // using the generics
    private java.util.HashMap processMapping;

    /** map process id and process updater for later update of the process */
    private java.util.HashMap<String, ProcessUpdater> pendingProcessMapping;

    /** map process id and service */
    private java.util.HashMap<String, UniversalService> serviceMapping;

    /** map process id and service updater for later update of the service */
    private java.util.HashMap pendingServiceMapping;

    /** map filetransfer-id and filetransfer */
    private java.util.HashMap<String, FileTransferDefinition> fileTransferMapping;

    /** map of the variable contract (ex XMLProperties) */
    private VariableContractImpl variableContract;

    /** Location of the xml file */
    private String url;
    private String jobID;
    private String descriptorURL;
    private boolean mainDefined;

    /** security rules */
    protected ProActiveSecurityManager proactiveSecurityManager;
    public PolicyServer policyServer;

    //  public X509Certificate creatorCertificate;
    public String securityFile;
    private HashMap<String, TechnicalService> technicalServiceMapping;

    //
    //  ----- CONSTRUCTORS -----------------------------------------------------------------------------------
    //
    public ProActiveDescriptorImpl(String url) {
        mainDefinitionMapping = new HashMap<String, MainDefinition>();
        virtualNodeMapping = new java.util.HashMap<String, VirtualNodeInternal>();
        virtualMachineMapping = new java.util.HashMap<String, VirtualMachine>();
        processMapping = new java.util.HashMap();
        pendingProcessMapping = new java.util.HashMap<String, ProcessUpdater>();
        serviceMapping = new java.util.HashMap<String, UniversalService>();
        pendingServiceMapping = new java.util.HashMap();
        fileTransferMapping = new java.util.HashMap<String, FileTransferDefinition>();
        variableContract = new VariableContractImpl();
        this.url = url;
        this.descriptorURL = url;
        mainDefined = false;
        this.technicalServiceMapping = new HashMap<String, TechnicalService>();
    }

    //
    //  ----- PUBLIC METHODS -----------------------------------------------------------------------------------
    //
    public String getUrl() {
        return this.url;
    }

    public String getProActiveDescriptorURL() {
        return descriptorURL;
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
    public void mainDefinitionAddVirtualNode(VirtualNodeInternal virtualNode) {
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

        for (String id : mainDefinitionMapping.keySet()) {
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
    public Map<String, MainDefinition> getMainDefinitionMapping() {
        return mainDefinitionMapping;
    }

    public void setMainDefinitionMapping(HashMap<String, MainDefinition> newMapping) {
        mainDefinitionMapping = newMapping;
    }

    /**
     * return the virtual nodes mapping
     * @return Map
     */
    public Map<String, VirtualNodeInternal> getVirtualNodeMapping() {
        return virtualNodeMapping;
    }

    public void setVirtualNodeMapping(HashMap<String, VirtualNodeInternal> newMapping) {
        virtualNodeMapping = newMapping;
    }

    /**
     *
     * @return an array containing all mainDefinitions conserving order
     */
    public MainDefinition[] getMainDefinitions() {
        MainDefinition[] mainDefinitions = new MainDefinition[mainDefinitionMapping.size()];
        Set<String> mainsId = mainDefinitionMapping.keySet();
        Iterator<String> it = mainsId.iterator();
        int i = 0;

        while (it.hasNext()) {
            String id = it.next();
            mainDefinitions[i] = getMainDefinition(id);
            i++;
        }

        return mainDefinitions;
    }

    public VirtualNodeInternal[] getVirtualNodes() {
        int i = 0;
        VirtualNodeInternal[] virtualNodeArray = new VirtualNodeInternal[virtualNodeMapping.size()];
        Collection<VirtualNodeInternal> collection = virtualNodeMapping.values();

        for (Iterator<VirtualNodeInternal> iter = collection.iterator(); iter.hasNext();) {
            virtualNodeArray[i] = iter.next();
            i++;
        }

        return virtualNodeArray;
    }

    public VirtualNodeInternal getVirtualNode(String name) {
        return virtualNodeMapping.get(name);
    }

    public VirtualMachine getVirtualMachine(String name) {
        return virtualMachineMapping.get(name);
    }

    public ExternalProcess getProcess(String name) {
        return (ExternalProcess) processMapping.get(name);
    }

    public UniversalService getService(String serviceID) {
        return serviceMapping.get(serviceID);
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
    protected FileTransferDefinition createFileTransferDefinition(String fileTransferID) {
        FileTransferDefinition ft = new FileTransferDefinition(fileTransferID);
        fileTransferMapping.put(fileTransferID, ft);

        if (logger.isDebugEnabled()) {
            logger.debug("created FileTransfer id=" + fileTransferID);
        }

        return ft;
    }

    public VirtualNodeInternal createVirtualNode(String vnName, boolean lookup) {
        return createVirtualNode(vnName, lookup, false);
    }

    public VirtualNodeInternal createVirtualNode(String vnName, boolean lookup, boolean isMainNode) {
        VirtualNodeInternal vn = getVirtualNode(vnName);

        if (jobID == null) {
            if (isMainDefined()) {
                this.jobID = generateNewJobID();

                //System.out.println("new id generated : " + jobID);
            } else {
                this.jobID = PAActiveObject.getJobId();

                //System.out.println("using runtime id : " + jobID);
            }

            this.url = url + this.jobID;
        }

        if (vn == null) {
            if (lookup) {
                vn = new VirtualNodeLookup(vnName);
            } else {
                vn = new VirtualNodeImpl(vnName, proactiveSecurityManager, this.url, isMainNode, this);
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

    public ExternalProcess createProcess(String processID, String processClassName) throws ProActiveException {
        ExternalProcess process = getProcess(processID);

        if (process == null) {
            process = createProcess(processClassName);
            addExternalProcess(processID, process);
        }

        return process;
    }

    public ExternalProcess createProcess(String processClassName) throws ProActiveException {
        try {
            Class<?> processClass = Class.forName(processClassName);
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

    public synchronized FileTransferDefinition getFileTransfer(String fileTransferID) {
        //TODO throw new ProActiveException 
        //if(fileTransferID.equalsIgnoreCase("implicit")) throw new ProActiveException();
        FileTransferDefinition ft = fileTransferMapping.get(fileTransferID);

        if (ft == null) {
            ft = createFileTransferDefinition(fileTransferID);
            fileTransferMapping.put(fileTransferID, ft);

            if (logger.isDebugEnabled()) {
                logger.debug("created FileTransfer id=" + fileTransferID);
            }
        }

        return ft;
    }

    public void registerProcess(ExternalProcessDecorator compositeProcess, String processID) {
        ExternalProcess process = getProcess(processID);

        if (process == null) {
            addPendingProcess(processID, compositeProcess);
        } else {
            compositeProcess.setTargetProcess(process);
        }
    }

    public void addProcessToSequenceList(AbstractSequentialListProcessDecorator sequentialListProcess,
            String processID) {
        ExternalProcess process = getProcess(processID);
        if (process == null) {
            addSequentialPendingProcess(processID, sequentialListProcess);
        } else {
            sequentialListProcess.addProcessToList(process);
        }
    }

    public void addServiceToSequenceList(AbstractSequentialListProcessDecorator sequentialListProcess,
            String processID) {
        UniversalService service = (UniversalService) processMapping.get(processID);
        if (service == null) {
            addSequentialPendingService(processID, sequentialListProcess);
            sequentialListProcess.setFirstElementIsService(true);
        } else {
            sequentialListProcess.addServiceToList(service);
            sequentialListProcess.setFirstElementIsService(true);
        }
    }

    public void mapToExtendedJVM(JVMProcess jvmProcess, String processID) throws ProActiveException {
        try {
            JVMProcessImpl process = (JVMProcessImpl) getProcess(processID);

            if (process == null) {
                throw new ProActiveException(
                    "The jvmProcess with id " +
                        processID +
                        " is not yet defined in the descriptor. The extended jvmProcess must be defined before all jvmProcesses that extend it");

                //addPendingJVMProcess(processID, jvmProcess);
            } else {
                jvmProcess.setExtendedJVM(process);
            }
        } catch (ClassCastException e) {
            logger.fatal("ERROR: a jvmProcess can only extend another jvmProcess. Correct the Descriptor");
            e.printStackTrace();
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
                logger.error("the given service " + service.getServiceName() + " cannot be set for class " +
                    serviceUser.getUserClass());
            }
        }
    }

    public void activateMappings() {
        VirtualNodeInternal[] virtualNodeArray = getVirtualNodes();

        for (int i = 0; i < virtualNodeArray.length; i++) {
            virtualNodeArray[i].activate();
        }
    }

    public void activateMapping(String virtualNodeName) {
        VirtualNodeInternal virtualNode = getVirtualNode(virtualNodeName);
        virtualNode.activate();
    }

    public void killall(boolean softly) throws ProActiveException {
        ProActiveRuntimeImpl part = ProActiveRuntimeImpl.getProActiveRuntime();

        part.removeDescriptor(this.url);

        VirtualNodeInternal[] vnArray = getVirtualNodes();

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
            proactiveSecurityManager = new ProActiveSecurityManager(EntityType.APPLICATION, policyServer);

            // set the security policyserver to the default proactive meta object
            // by the way, the HalfBody will be associated to a security manager
            // derivated from this one.
            ProActiveSecurityManager psm = proactiveSecurityManager.generateSiblingCertificate(
                    EntityType.OBJECT, "HalfBody");
            ProActiveMetaObjectFactory.newInstance().setProActiveSecurityManager(psm);
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
    private MainDefinition getMainDefinition(String mainDefinitionID) {
        return mainDefinitionMapping.get(mainDefinitionID);
    }

    /**
     * return the last main definition added
     * @return MainDefinition
     */
    private MainDefinition getMainDefinition() {
        return getMainDefinition(lastMainDefinitionID);
    }

    private void addExternalProcess(String processID, ExternalProcess process) {
        ProcessUpdater processUpdater = pendingProcessMapping.remove(processID);

        if (processUpdater != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Updating Process name=" + processID);
            }

            processUpdater.updateProcess(process);
        }

        processMapping.put(processID, process);
    }

    private void addPendingProcess(String processID, VirtualMachine virtualMachine) {
        ProcessUpdater updater = new VirtualMachineUpdater(virtualMachine);

        //pendingProcessMapping.put(processID, updater);
        addProcessUpdater(processID, updater);
    }

    private void addPendingProcess(String processID, ExternalProcessDecorator compositeProcess) {
        ProcessUpdater updater = new CompositeExternalProcessUpdater(compositeProcess);

        //pendingProcessMapping.put(processID, updater);
        addProcessUpdater(processID, updater);
    }

    private void addSequentialPendingProcess(String processID, AbstractSequentialListProcessDecorator sp) {
        ProcessUpdater updater = new SequentialProcessUpdater(sp);
        addProcessUpdater(processID, updater);
    }

    private void addSequentialPendingService(String processID, AbstractSequentialListProcessDecorator sp) {
        ServiceUpdater updater = new SequentialServiceUpdater(sp);
        addServiceUpdater(processID, updater);
    }

    //Commented in the fist version of jvm extension
    //    private void addPendingJVMProcess(String processID, JVMProcess jvmProcess) {
    //        ProcessUpdater updater = new ExtendedJVMProcessUpdater(jvmProcess);
    //
    //        //pendingProcessMapping.put(processID, updater);
    //        addProcessUpdater(processID, updater);
    //    }
    private void addProcessUpdater(String processID, ProcessUpdater processUpdater) {
        CompositeProcessUpdater compositeProcessUpdater = (CompositeProcessUpdater) pendingProcessMapping
                .get(processID);

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

    private void addServiceUpdater(String serviceID, ServiceUpdater serviceUpdater) {
        CompositeServiceUpdater compositeServiceUpdater = (CompositeServiceUpdater) pendingServiceMapping
                .get(serviceID);

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

    private static class CompositeServiceUpdater implements ServiceUpdater {
        private java.util.ArrayList<ServiceUpdater> updaterList;

        public CompositeServiceUpdater() {
            updaterList = new java.util.ArrayList<ServiceUpdater>();
        }

        public void addServiceUpdater(ServiceUpdater s) {
            updaterList.add(s);
        }

        public void updateService(UniversalService s) {
            for (ServiceUpdater serviceUpdater : updaterList) {
                serviceUpdater.updateService(s);
            }

            updaterList.clear();
        }
    }

    private static class CompositeProcessUpdater implements ProcessUpdater {
        private java.util.ArrayList<ProcessUpdater> updaterList;

        public CompositeProcessUpdater() {
            updaterList = new java.util.ArrayList<ProcessUpdater>();
        }

        public void addProcessUpdater(ProcessUpdater p) {
            updaterList.add(p);
        }

        public void updateProcess(ExternalProcess p) {
            for (ProcessUpdater processUpdater : updaterList) {
                processUpdater.updateProcess(p);
            }

            updaterList.clear();
        }
    }

    private static class CompositeExternalProcessUpdater implements ProcessUpdater {
        private ExternalProcessDecorator compositeExternalProcess;

        public CompositeExternalProcessUpdater(ExternalProcessDecorator compositeExternalProcess) {
            this.compositeExternalProcess = compositeExternalProcess;
        }

        public void updateProcess(ExternalProcess p) {
            if (logger.isDebugEnabled()) {
                logger.debug("Updating CompositeExternal Process");
            }

            compositeExternalProcess.setTargetProcess(p);
        }
    }

    private static class SequentialProcessUpdater implements ProcessUpdater {
        private AbstractSequentialListProcessDecorator spd;

        public SequentialProcessUpdater(AbstractSequentialListProcessDecorator spd) {
            this.spd = spd;
        }

        public void updateProcess(ExternalProcess p) {
            if (logger.isDebugEnabled()) {
                logger.debug("Updating Sequential Process");
            }

            // add the process in head of list
            spd.addProcessToList(0, p);
        }
    }

    private static class SequentialServiceUpdater implements ServiceUpdater {
        private AbstractSequentialListProcessDecorator spd;

        public SequentialServiceUpdater(AbstractSequentialListProcessDecorator spd) {
            this.spd = spd;
        }

        public void updateService(UniversalService s) {
            if (logger.isDebugEnabled()) {
                logger.debug("Updating Sequential Service");
            }

            // add the process in head of list
            spd.addServiceToList(0, s);
        }
    }

    private static class VirtualMachineUpdater implements ProcessUpdater, ServiceUpdater {
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

    private static class ServiceUpdaterImpl implements ServiceUpdater {
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
                logger.error("the given service " + s.getServiceName() + " cannot be set for class " +
                    serviceUser.getUserClass());
            }
        }
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorInternal#setVariableContract(org.objectweb.proactive.core.xml.VariableContractImpl)
     */
    public void setVariableContract(VariableContractImpl variableContract) {
        this.variableContract = variableContract;
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorInternal#getVariableContract()
     */
    public VariableContractImpl getVariableContract() {
        return this.variableContract;
    }

    public void addTechnicalService(TechnicalServiceXmlType tsParsed) throws Exception {
        TechnicalService ts = (TechnicalService) tsParsed.getType().newInstance();
        (ts).init(tsParsed.getArgs());
        if (technicalServiceMapping.containsKey(tsParsed.getId())) {
            ((TechnicalServiceWrapper) this.technicalServiceMapping.get(tsParsed.getId())).setTs(ts);
        } else {
            logger.warn("Unregistered technical service id : " + tsParsed.getId());
        }
    }

    public TechnicalService getTechnicalService(String technicalServiceId) {
        TechnicalService ts = this.technicalServiceMapping.get(technicalServiceId);

        if (ts == null) {
            ts = new TechnicalServiceWrapper();
            this.technicalServiceMapping.put(technicalServiceId, ts);
        }

        return ts;
    }

    public ProActiveDescriptorInternal getProActiveDescriptorInternal() {
        return this;
    }
}
