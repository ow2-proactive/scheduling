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

import org.apache.log4j.Logger;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.ExternalProcessDecorator;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.ext.security.PolicyServer;
import org.objectweb.proactive.ext.security.ProActiveSecurityDescriptorHandler;

import org.xml.sax.SAXException;

import java.io.IOException;

import java.security.cert.X509Certificate;

import java.util.Collection;
import java.util.Iterator;


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
    protected static Logger logger = Logger.getLogger(ProActiveDescriptorImpl.class.getName());

    /** map virtualNode name and objects */
    private java.util.HashMap virtualNodeMapping;

    /** map jvm name and object */
    private java.util.HashMap virtualMachineMapping;

    /** map process id and process */
    private java.util.HashMap processMapping;

    /** map process id and process updater for later update of the process */
    private java.util.HashMap pendingProcessMapping;

    /** Location of the xml file */
    private String url;

    /** security rules */
    public PolicyServer policyServer;
    public X509Certificate creatorCertificate;
    public String securityFile;

    //
    //  ----- CONSTRUCTORS -----------------------------------------------------------------------------------
    //

    /**
     * Contructs a new intance of ProActiveDescriptor
     */
    public ProActiveDescriptorImpl(String url) {
        virtualNodeMapping = new java.util.HashMap();
        virtualMachineMapping = new java.util.HashMap();
        processMapping = new java.util.HashMap();
        pendingProcessMapping = new java.util.HashMap();
        this.url = url;
    }

    //
    //  ----- PUBLIC METHODS -----------------------------------------------------------------------------------
    //
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

    public VirtualNode createVirtualNode(String vnName, boolean lookup) {
        VirtualNode vn = getVirtualNode(vnName);
        if (vn == null) {
            if (lookup) {
                vn = new VirtualNodeLookup(vnName);
            } else {
                vn = new VirtualNodeImpl(vnName, creatorCertificate,
                        policyServer);
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
            return (ExternalProcess) processClass.newInstance();
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

    public void registerProcess(ExternalProcessDecorator compositeProcess,
        String processID) {
        ExternalProcess process = getProcess(processID);
        if (process == null) {
            addPendingProcess(processID, compositeProcess);
        } else {
            compositeProcess.setTargetProcess(process);
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
     * Intialize application security policy
     * @param file
     */
    public void createPolicyServer(String file) {
        securityFile = file;
        try {
            policyServer = ProActiveSecurityDescriptorHandler.createPolicyServer(file);
            //      policyServer = ps;
            //         ProActiveRuntime prR = RuntimeFactory.getDefaultRuntime();
            //        ProActiveSecurityManager psm = new ProActiveSecurityManager(file);
            //       prR.setProActiveSecurityManager(psm);
            //   } catch (ProActiveException e) {
            //       e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
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
        ProcessUpdater updater = new VirtualMachineProcessUpdater(virtualMachine);

        //pendingProcessMapping.put(processID, updater);
        addUpdater(processID, updater);
    }

    private void addPendingProcess(String processID,
        ExternalProcessDecorator compositeProcess) {
        ProcessUpdater updater = new CompositeExternalProcessUpdater(compositeProcess);

        //pendingProcessMapping.put(processID, updater);
        addUpdater(processID, updater);
    }

    private void addUpdater(String processID, ProcessUpdater processUpdater) {
        CompositeProcessUpdater compositeProcessUpdater = (CompositeProcessUpdater) pendingProcessMapping.get(processID);
        if (compositeProcessUpdater == null) {
            compositeProcessUpdater = new CompositeProcessUpdater();
            //pendingProcessMapping.put(processID, processUpdater);
            pendingProcessMapping.put(processID, compositeProcessUpdater);
        }
        compositeProcessUpdater.addProcessUpdater(processUpdater);
    }

    //
    //  ----- INNER CLASSES -----------------------------------------------------------------------------------
    //
    private interface ProcessUpdater {
        public void updateProcess(ExternalProcess p);
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

    private class VirtualMachineProcessUpdater implements ProcessUpdater {
        private VirtualMachine virtualMachine;

        public VirtualMachineProcessUpdater(VirtualMachine virtualMachine) {
            this.virtualMachine = virtualMachine;
        }

        public void updateProcess(ExternalProcess p) {
            if (logger.isDebugEnabled()) {
                logger.debug("Updating VirtualMachine Process");
            }
            virtualMachine.setProcess(p);
        }
    }
}
