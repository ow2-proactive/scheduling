/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.KeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.nodesource.utils.NamesConvertor;
import org.ow2.proactive.resourcemanager.rmnode.RMDeployingNode;
import org.ow2.proactive.resourcemanager.utils.VIRMNodeStarter;
import org.ow2.proactive.resourcemanager.utils.VirtualInfrastructureNodeStarter;
import org.ow2.proactive.resourcemanager.utils.VirtualInfrastructureNodeStarterRegister;
import org.ow2.proactive.virtualizing.core.VMGuestStatus;
import org.ow2.proactive.virtualizing.core.VirtualMachine2;
import org.ow2.proactive.virtualizing.core.VirtualMachineManager2;
import org.ow2.proactive.virtualizing.core.error.VirtualServiceException;
import org.ow2.proactive.virtualizing.virtualbox.VirtualboxVMM;
import org.ow2.proactive.virtualizing.vmwarevi.VMwareVMM;
import org.ow2.proactive.virtualizing.xenserver.XenServerVMM;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/** This class provides a way to manager Virtualized Infrastructure. */
public class VirtualInfrastructure extends InfrastructureManager {

    /**  */
    private static final long serialVersionUID = 31L;
    @Configurable(description = "Virtual Infrastructure Type:\nxenserver, virtualbox, vmware, hyperv-winrm or hyperv-wmi")
    protected String infrastructure;
    /** The hypervisor's url */
    @Configurable(description = "Hypervisor's url")
    protected String VMMUrl;
    /** A user with sufficient permissions to manage the hypervisor */
    @Configurable(description = "Hypervisor's user")
    protected String VMMUser;
    /** The hypervisor's user's password */
    @Configurable(password = true, description = "Hypervisor's user's password")
    protected String VMMPwd;

    /** To maintain the number of Nodes registered within the Resource Manager per Virtual Machine.
     * To be able to decide to power off/destroy the machine. */
    private Map<String, Integer> numberOfRegisteredNode = null;
    /** To be able to register the exact number of nodes the RM asked for.
     * Is used only in case of local Node registration. */
    private int numberOfRequiredNodes = 0;
    /** To keep track of available Nodes per virtual machines */
    private VMNodeCache availableNodes = null;
    /** When not using rm authentication for remote registration of newly
     * created nodes, shared variables are set to allow communication between
     * hypervisor provider and infrastructure manager. Virtual infrastructure
     * manager gets the nodes' url and registers them itself. Here is the timeout
     * the infrastructure manager will wait for shared variable to be set. */
    public static final long NODE_URL_ACQUISITION_TIMEOUT = 300000;

    /** The name of the template virtual machine */
    @Configurable(description = "Template virtual machine's name")
    protected String VMTemplate;
    /** The list of started virtual machines */
    protected ArrayList<String> runningVM = null;
    /** The list of clone virtual machines */
    protected ArrayList<String> cloneVM = null;
    /** The maximum number of runnable instance of the template virtual machine */
    @Configurable(description = "The maximum number of vm")
    protected int VMMax;
    /** The current number of running virtual machine's clones */
    protected int count = 0;
    /** The virtual machines accessor. Required because virtual machines don't implement
     * {@link Serializable} */
    protected VirtualMachineManagerHolder virtualMachineManagerHolder;

    /** The number of node to be launched within every virtual machine */
    @Configurable(description = "The number of node per virtual machine")
    protected int hostCapacity;
    /** A path to a ProActive configuration file */
    @Configurable(fileBrowser = true, description = "ProActive Configuration file path")
    protected File PAConfig;
    /** A path to a credentials file */
    @Configurable(credential = true, fileBrowser = true, description = "Absolute path of the rm.cred file")
    protected File RMCredentials;
    protected String credentials;
    /** The properties used for the {@link VirtualInfrastructure} configuration.
     * See {@link VirtualInfrastructure.Prop} */
    protected Properties properties;
    /** The configuration of the managed virtual machine PART ( from paConf ). */
    protected Hashtable<String, String> confTable = null;

    private ReentrantLock availableNodesLock = null;
    private ReentrantLock deploymentLock = null;
    private ReentrantLock requiredNodesLock = null;
    private ReentrantLock registeredNodesLock = null;

    /** This constant is used as a circuit broker threshold */
    public static final int circuitBrokerThreshold = 5;

    /** This thread update the deployed virtual machines' guest status */
    private VMDeploymentMonitor vmGuestStatusMonitor = null;
    /** The monitored guest OSes refresh frequency in ms */
    public static final int GUEST_STATUS_UPDATE_FREQUENCY = 3000;

    public VirtualInfrastructure() {
    }

    @Override
    public void setNodeSource(NodeSource nodeSource) {
        super.setNodeSource(nodeSource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void acquireAllNodes() {
        logger.debug("Acquiring all nodes.");
        addAllCachedNodesToCore();
        try {
            deploymentLock.lock();
            while (count < VMMax) {
                startNewVirtualMachineIfNecessary(hostCapacity);
            }
        } catch (Throwable e) {
            logger.error("Unable to set environment and start the virtual machine.", e);
        } finally {
            deploymentLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void acquireNode() {
        logger.debug("Acquiring one node.");
        if (addACachedNodeToCore()) {
            return;
        } else {
            try {
                startNewVirtualMachineIfNecessary(1);
            } catch (Throwable e) {
                logger.error("An error occured while acquiring a node.", e);
            }
        }
    }

    private void initFields() {
        this.numberOfRegisteredNode = new HashMap<String, Integer>();
        this.availableNodes = new VMNodeCache();
        this.runningVM = new ArrayList<String>();
        this.cloneVM = new ArrayList<String>();
        this.confTable = new Hashtable<String, String>();
        this.availableNodesLock = new ReentrantLock();
        this.deploymentLock = new ReentrantLock();
        this.requiredNodesLock = new ReentrantLock();
        this.registeredNodesLock = new ReentrantLock();
        this.vmGuestStatusMonitor = new VMDeploymentMonitor();
    }

    /**
     * To be able to handler virtual infrastructure node acquisition.
     * @param parameters contains compulsory information:
     * 		  parameters[0] is the infrastructure type
     * 		  parameters[1] is the hypervisor's url
     * 		  parameters[2] is the hypervisor's user
     * 		  parameters[3] is the hypervisor's password
     * 		  parameters[4] is the virtual machine template's name
     * 		  parameters[5] is the maximum number of template virtual machine
     * 		  parameters[6] is the host capacity
     * 		  parameters[7] is the path to a ProActive Configuration file
     *        parameters[8] is the rm credentials used to connect to the RM from within the Virtual machines
     */
    @Override
    public void configure(Object... parameters) {
        logger.info("Configuration read from user input");
        initFields();
        int index = 0;
        infrastructure = parameters[index++].toString();
        InfrastructureType it = InfrastructureType.getInfrastructureType(infrastructure);
        if (it == null) {
            throw new IllegalArgumentException("A bad virtual infrastructure type was supplied");
        }
        VMMUrl = parameters[index++].toString();
        VMMUser = parameters[index++].toString();
        VMMPwd = parameters[index++].toString();
        VMTemplate = parameters[index++].toString();
        VMMax = Integer.parseInt(parameters[index++].toString());
        hostCapacity = Integer.parseInt(parameters[index++].toString());
        virtualMachineManagerHolder = new VirtualMachineManagerHolder(it, VMMUrl, VMMUser, VMMPwd);
        //fail fast... if the vm doesn't exist, raises an exception
        try {
            VirtualMachine2 tmp = virtualMachineManagerHolder.getVirtualMachine(VMTemplate);
            if (tmp == null) {
                throw new RMException("Cannot get virtual machine: " + VMTemplate);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot get virtual machine: " + VMTemplate, e);
        }
        if (parameters[index] != null) {
            definePARTConf((byte[]) parameters[index++]);
        }
        if (parameters[index] != null) {
            try {
                Credentials tmpCred = Credentials.getCredentialsBase64((byte[]) parameters[index++]);
                credentials = new String(tmpCred.getBase64());
            } catch (KeyException e) {
                throw new IllegalArgumentException("Could not retrieve base64 credentials", e);
            }
        } else {
            throw new IllegalArgumentException("Credential file is mandatory.");
        }
    }

    @Override
    public void notifyAcquiredNode(Node node) throws RMException {
        try {
            requiredNodesLock.lock();
            String holdingVM = node.getProperty(Prop.HOLDING_VIRTUAL_MACHINE.getValue());
            if (node.getProperty(Prop.IS_ALREADY_REGISTERED.getValue()) == null) {
                if (holdingVM == null) {
                    logger.error("Cannot determine holding virtual machine");
                    throw new RMException("Cannot determine holding virtual machine");
                }
                incNumberOfRegisteredNodes(holdingVM);
                node.setProperty(Prop.IS_ALREADY_REGISTERED.getValue(), "true");
                logger.debug("A New node was added by " + this.getClass().getSimpleName() + ". " +
                    "Property isAlreadyRegistered is now true.");
                //this is the first time one deploys this node, it is deploying...
                //even if the policy didn't request it we deployed it as deamon
                //now it should be added to the core... it will be cached later
                if (numberOfRequiredNodes <= 0) {
                    logger.debug("First time not required node acquisition, not discarded: " +
                        node.getNodeInformation().getURL());
                    return;
                }
            } else {
                logger.debug("A previously used node was added by " + this.getClass().getSimpleName() + ". " +
                    "Property isAlreadyRegistered was true.");
            }
            if (numberOfRequiredNodes > 0) {
                numberOfRequiredNodes--;
            } else {
                logger.debug("A new node was added to RM but not required. Caching it for futur use");
                availableNodes.addNode(holdingVM, node.getNodeInformation().getURL());
                throw new RMException("Not expected node registered. Caching it for futur use.");
            }
        } catch (ProActiveException e) {
            logger.error("Unable to state about node registration " + node.getNodeInformation().getURL());
            throw new RMException("Unable to state about node registration " +
                node.getNodeInformation().getURL());
        } finally {
            requiredNodesLock.unlock();
        }
    }

    @Override
    public void shutDown() {
        try {
            deploymentLock.lock();
            for (String vmName : runningVM) {
                VirtualMachine2 vm = null;
                try {
                    vm = virtualMachineManagerHolder.getVirtualMachine(vmName);
                    vm.powerOff();
                } catch (Exception e) {
                    logger.warn("Virtual Infrastructure Manager wasn't able to power " + vmName +
                        " off. Do it manually.");
                }
            }
            runningVM.clear();
            for (String vmName : cloneVM) {
                VirtualMachine2 vm = null;
                try {
                    vm = virtualMachineManagerHolder.getVirtualMachine(vmName);
                    vm.destroy();
                } catch (Exception e) {
                    logger.warn("Virtual Infrastructure Manager wasn't able to destroy " + vmName +
                        ". Do it manually.");
                }
            }
        } finally {
            this.vmGuestStatusMonitor.exit();
            deploymentLock.unlock();
        }
    }

    @Override
    public void removeNode(Node node) throws RMException {
        String holdingVM = null;
        try {
            availableNodesLock.lock();
            registeredNodesLock.lock();
            try {
                holdingVM = node.getProperty(Prop.HOLDING_VIRTUAL_MACHINE.getValue());
                if (holdingVM == null) {
                    throw new RMException("Trying to remove a node without holding virtual machine.");
                }
            } catch (ProActiveException e) {
                throw new RMException("An error occured while removing a node.", e);
            }
            String nodeName = node.getNodeInformation().getName();
            logger.debug("Node " + nodeName + " added in node cache for vm " + holdingVM + ".");
            availableNodes.addNode(holdingVM, node.getNodeInformation().getURL());
        } finally {
            if (availableNodes.numberOfAvailableNodes(holdingVM) >= numberOfRegisteredNode.get(holdingVM)) {
                logger.debug("The size of node cache has reached " + holdingVM + "'s capacity.");
                if (holdingVM != null) {
                    try {
                        destroyVM(holdingVM);
                    } catch (VirtualServiceException e) {
                        throw new RMException("Cannot destroy virtual machine " + holdingVM, e);
                    }
                }
            }
            registeredNodesLock.unlock();
            availableNodesLock.unlock();
        }
    }

    /** @return a string describing the object */
    @Override
    public String toString() {
        return NamesConvertor.beautifyName(this.getClass().getSimpleName());
    }

    /** @return a string for GUI infrastructure manager description. */
    public String getDescription() {
        return "Virtualized Infrastructure node acquisition";
    }

    /**
     * Loads the PART configuration file for virtual machine.
     * @param bs a byte array containing a proactive configuration
     */
    private void definePARTConf(byte[] bs) {
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(bs);
            populateConfTable(is);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot read ProActive configuration from supplied file.", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                logger.warn(e);
            }
        }
    }

    /**
     * Helper method used to populate PA Configuration table
     * @param is the InputStream containing the ProActive configuration
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private void populateConfTable(InputStream is) throws ParserConfigurationException, SAXException,
            IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(is);
        NodeList props = doc.getElementsByTagName("prop");
        for (int i = 0; i < props.getLength(); i++) {
            Element prop = (Element) props.item(i);
            String key = prop.getAttribute("key");
            String value = prop.getAttribute("value");
            confTable.put(key, value);
        }
    }

    //	/** Defines the credentials value */
    //	private void defineCredentials(byte[] bs) {
    //		cred = new String(bs);
    //	}

    /**
     * Return an immutable (cloned) ProActive Configuration
     * backed by a {@link Hashtable}
     * @return The remote PART configuration that will be used for
     * a newly started virtual machine.
     */
    private Hashtable<String, String> getProActiveConfiguration() {
        return (Hashtable<String, String>) confTable.clone();
    }

    /**
     * Tries to get an already available node. If no node is available, tries to start
     * a new virtual machine to handle the node acquisition request.
     * @param required the number of needed nodes.
     * @throws VirtualServiceException
     * @throws RMException
     */
    private void startNewVirtualMachineIfNecessary(int required) throws VirtualServiceException, RMException {
        try {
            deploymentLock.lock();
            requiredNodesLock.lock();
            //a previously deployed vm can handle node acquisition request
            if (vmGuestStatusMonitor.getNumberOfNonUsedNode() >= required) {
                numberOfRequiredNodes += required;
                logger.debug("A node acquisition can be served by a recently started Virtual Machine.");
                //                vmGuestStatusMonitor.internalNewPendingNode(required);
                logger.debug("Required & non used nodes numbers updated: req=" + numberOfRequiredNodes +
                    " non used=" + vmGuestStatusMonitor.getNumberOfNonUsedNode());
                return;
            }
            //VIM has reached max vm capacity
            if (count >= VMMax) {
                logger.debug("A node acquisition required to start a new Virtual Machine "
                    + "but the max instances number has been reached.");
                return;
            }
            //starting a new vm
            logger.debug("A node acquisition requires a new Virtual Machine to be started.");
            numberOfRequiredNodes += required;
            String toStartName = null;
            VirtualMachine2 toStart, templateVM = virtualMachineManagerHolder.getVirtualMachine(VMTemplate);
            if (VMMax == 1) {//one starts the template virtual machine
                toStartName = VMTemplate;
                toStart = templateVM;
            } else {//one starts a new clone
                toStartName = VMTemplate + "_PAClone_" + new Random(System.currentTimeMillis()).nextInt();
                //some providers raise Exception even if request succeeds
                //vm always added, better receive an exception at exit time
                //than at deployment time
                cloneVM.add(toStartName);
                toStart = templateVM.clone(toStartName);
            }
            logger.info("Powering " + toStartName + " on");
            setEnvironmentForStarterRegisterAndStart(toStart);
            initializeNumberOfRegisteredNode(toStartName);
            runningVM.add(toStartName);
            vmGuestStatusMonitor.newDeployingNodes(toStartName, this.hostCapacity);
            count++;
            logger.debug("Required & non used nodes numbers updated: req=" + numberOfRequiredNodes +
                " non used=" + vmGuestStatusMonitor.getNumberOfNonUsedNode());
            logger.debug("A new Virtual Machine was started, current count: " + count);
        } finally {
            deploymentLock.unlock();
            requiredNodesLock.unlock();
        }
    }

    /**
     * Sets the environment of the virtual machine to be able to run
     * {@link VirtualInfrastructureNodeStarterRegister} from within the virtual machine
     * and starts the virtual machine.
     * @param toStart the virtual machine to start
     * @return true if the virtual machine has been started, false otherwise.
     * @throws RMException
     * @throws VirtualServiceException
     */
    private boolean setEnvironmentForStarterRegisterAndStart(VirtualMachine2 toDeploy) throws RMException,
            VirtualServiceException {
        //TODO : also push classpath & executable's name... requires to modify python scripts
        Hashtable<String, String> partConf = getProActiveConfiguration();
        Set<String> keys = partConf.keySet();
        try {
            int i = 0;
            for (String key : keys) {
                String value = partConf.get(key);
                toDeploy.pushData("dynamic." + i, "-D" + key + "=" + value);
                i++;
            }
            String toDeployName = toDeploy.getName();
            //push a watchdog
            toDeploy.pushData("dynamic." + i, "null");

            //push the holding virtual machine's name
            toDeploy.pushData(VirtualInfrastructure.Prop.HOLDING_VIRTUAL_MACHINE.getValue(), toDeployName);

            //push capacity information
            toDeploy.pushData(VirtualInfrastructure.Prop.HOST_CAPACITY.getValue(), new Integer(hostCapacity)
                    .toString());

            toDeploy.pushData(VirtualInfrastructure.Prop.RM_URL.getValue(), this.rmUrl);
            toDeploy.pushData(VirtualInfrastructure.Prop.RM_CREDS.getValue(), this.credentials);
            //push the nodeSource's name
            toDeploy.pushData(VirtualInfrastructure.Prop.NODESOURCE.getValue(), nodeSource.getName());
        } catch (VirtualServiceException e) {
            throw new RMException("Unnable to deploy virtual machine " + toDeploy, e);
        }
        return toDeploy.powerOn();
    }

    /**
     * Kills the nodes hold by the virtual machine, power the virtual machine off, and if
     * the virtual machine is a clone, destroys it.
     * @param holdingVM the virtual machine to destroy
     * @throws VirtualServiceException
     * @throws RMException
     */
    private void destroyVM(String holdingVM) throws VirtualServiceException, RMException {
        logger.info("Destroying vm " + holdingVM);
        try {
            availableNodesLock.lock();
            requiredNodesLock.lock();
            while (availableNodes.hasAvailableNode(holdingVM)) {
                String nodeUrl = availableNodes.removeNodeUrl(holdingVM);
                try {
                    Node node = NodeFactory.getNode(nodeUrl);
                    node.getProActiveRuntime().killNode(node.getNodeInformation().getName());
                    node.getProActiveRuntime().killRT(false);
                } catch (Throwable e) {
                    logger.warn("Throwable caught while destroying a vm: " + e.getLocalizedMessage());
                }
            }
            availableNodes.removeCache(holdingVM);
            clearNumberOfRegisteredNodes(holdingVM);
        } finally {
            availableNodesLock.unlock();
        }
        try {
            deploymentLock.lock();
            VirtualMachine2 vm = virtualMachineManagerHolder.getVirtualMachine(holdingVM);
            vm.powerOff();
            runningVM.remove(holdingVM);
            if (VMMax != 1) {
                if (cloneVM.contains(holdingVM)) {
                    vm.destroy();
                }
                cloneVM.remove(holdingVM);
            }
        } finally {
            count--;
            requiredNodesLock.unlock();
            deploymentLock.unlock();
        }
    }

    private void initializeNumberOfRegisteredNode(String vmName) {
        try {
            registeredNodesLock.lock();
            if (!numberOfRegisteredNode.containsKey(vmName) || numberOfRegisteredNode.get(vmName) == null) {
                numberOfRegisteredNode.put(vmName, new Integer(0));
            }
        } finally {
            registeredNodesLock.unlock();
        }
    }

    /**
     * Tries to add a node to the RMCore from a previously deployed VM.
     * @return true if such a node exists and has been added to the RMCore,
     * false otherwise
     */
    private boolean addACachedNodeToCore() {
        try {
            availableNodesLock.lock();
            requiredNodesLock.lock();
            numberOfRequiredNodes++;
            String nodeUrl = availableNodes.removeRandomNodeUrl();
            if (nodeUrl == null) {
                return false;
            } else {
                Node toAdd = NodeFactory.getNode(nodeUrl);
                String nodeName = toAdd.getNodeInformation().getName();
                super.addDeployingNode(nodeName, "Launched as daemon", RMDeployingNode.class.getSimpleName() +
                    " cached by Virtual Infrastructure Manager",
                        VirtualInfrastructure.NODE_URL_ACQUISITION_TIMEOUT);
                this.nodeSource.acquireNode(nodeUrl, this.nodeSource.getAdministrator());
                return true;
            }
        } catch (Throwable t) {
            logger.error("Failled to add an available node to RMCore.", t);
            return false;
        } finally {
            requiredNodesLock.unlock();
            availableNodesLock.unlock();
        }
    }

    /**
     * Add all previously released Nodes of a still
     * running VM to the RMCore
     */
    private void addAllCachedNodesToCore() {
        while (addACachedNodeToCore()) {
            Thread.yield();
        }
    }

    /**
     * Increase the number of registered nodes for a given virtual machine.
     * See {@link this#numberOfRegisteredNode}.
     * @param vm The virtual machine which needs update.
     */
    private void incNumberOfRegisteredNodes(String vm) {
        try {
            registeredNodesLock.lock();
            if (!numberOfRegisteredNode.containsKey(vm)) {
                numberOfRegisteredNode.put(vm, new Integer(0));
            }
            Integer current = numberOfRegisteredNode.get(vm);
            numberOfRegisteredNode.put(vm, current + 1);
            logger.debug("Number of registered nodes for " + vm + " incremented. Current value: " +
                (current + 1));
        } finally {
            registeredNodesLock.unlock();
        }
    }

    /**
     * Clear the number of registered nodes for a given virtual machine.
     * See {@link this#numberOfRegisteredNode}.
     * This method is called when a virtual machine is removed.
     * @param vm The virtual machine which needs update.
     */
    private void clearNumberOfRegisteredNodes(String vm) {
        try {
            registeredNodesLock.lock();
            numberOfRegisteredNode.remove(vm);
            logger.debug("Number of registered nodes for " + vm + " cleared.");
        } finally {
            registeredNodesLock.unlock();
        }
    }

    /*----------------------------------------
     * Member classes
     *---------------------------------------*/

    private class VMDeploymentMonitor implements Serializable, Runnable {
        /**  */
        private static final long serialVersionUID = 31L;
        private volatile boolean run = false;
        private static final String DEPLOYING_NODE_NAME_FRAGMENT = "_node_";
        private Hashtable<String, VMGuestStatus> status = new Hashtable<String, VMGuestStatus>();
        private Hashtable<String, String[]> deployingNodes = new Hashtable<String, String[]>();

        private VMDeploymentMonitor() {
        }

        public void run() {
            int circuitBroker = 0;
            while (this.run) {
                try {
                    for (Entry<String, VMGuestStatus> entry : status.entrySet()) {
                        try {
                            VirtualMachine2 vm = VirtualInfrastructure.this.virtualMachineManagerHolder
                                    .getVirtualMachine(entry.getKey());
                            VMGuestStatus previous = entry.getValue(), current = vm.getVMGuestStatus();
                            if (previous != null) {
                                if (!previous.equals(current)) {
                                    org.ow2.proactive.virtualizing.core.State vmState = vm.getState();
                                    VMDeploymentMonitor.this._fireDeployingNodeUpdate(entry.getKey(),
                                            _buildDescription(entry.getKey(), current, vmState));

                                }
                            }
                            entry.setValue(current);
                        } catch (Exception e) {
                            logger
                                    .warn(
                                            "An exception occured while monitoring VMGuestStatus for virtual machine: " +
                                                entry.getKey(), e);
                        }
                    }
                    Thread.sleep(VirtualInfrastructure.GUEST_STATUS_UPDATE_FREQUENCY);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted while updating guest status", e);
                    circuitBroker++;
                    if (circuitBroker >= VirtualInfrastructure.circuitBrokerThreshold) {
                        logger.warn("Exited monitoring thread because circuit broker threshold was reached.");
                        exit();
                    }
                }
            }
            logger.debug("Guest status monitoring thread exiting");
        }

        private int getNumberOfNonUsedNode() {
            int result = 0;
            Collection<String[]> pendings = deployingNodes.values();
            for (String[] tmpURL : pendings) {
                if (tmpURL != null) {
                    for (int i = 0; i < tmpURL.length; i++) {
                        if (tmpURL[i] == null) {
                            result++;
                        }
                    }
                }
            }
            return result;
        }

        /**
         * This method must be called when notifying deploying node for a given virtual machine for the first time.
         * It creates and store the VMGuestStatus associated to the holding virtual machine. It also creates and store
         * howMany RMDeployingNode in a String Array initialized with a length of VirtualMachine.this.hostCapacity.
         * The length of the array will be used to compute deploying nodes that have not been added to the monitoring thread yet.
         * @param vmName The holding virtual machine's name.
         * @param howMany The number of deploying node whose state must be monitored.
         */
        private void newDeployingNodes(String vmName, int howMany) {
            synchronized (deployingNodes) {
                String description = "Cannot determine guest status for this " +
                    RMDeployingNode.class.getSimpleName();
                String[] pendings = deployingNodes.get(vmName);
                if (pendings == null) {
                    pendings = new String[VirtualInfrastructure.this.hostCapacity];
                }
                VMGuestStatus guestStatus = null;
                org.ow2.proactive.virtualizing.core.State vmState = null;
                try {
                    VirtualMachine2 vm = VirtualInfrastructure.this.virtualMachineManagerHolder
                            .getVirtualMachine(vmName);
                    guestStatus = vm.getVMGuestStatus();
                    vmState = vm.getState();
                } catch (Exception e) {
                    logger.warn("An exception occured while declaring new " +
                        RMDeployingNode.class.getSimpleName(), e);
                }
                if (guestStatus != null && vmState != null) {
                    description = _buildDescription(vmName, guestStatus, vmState);
                }
                howMany = (howMany <= VirtualInfrastructure.this.hostCapacity ? howMany
                        : VirtualInfrastructure.this.hostCapacity);
                for (int i = 0; i < howMany; i++) {
                    String pendingNodeName = "VIRT-" + vmName + DEPLOYING_NODE_NAME_FRAGMENT + (i + 1);
                    String tmpURL = addDeployingNode(pendingNodeName, "daemon command", description,
                            VirtualInfrastructure.NODE_URL_ACQUISITION_TIMEOUT);
                    pendings[i] = tmpURL;
                }
                if (guestStatus != null) {
                    status.put(vmName, guestStatus);
                }
                deployingNodes.put(vmName, pendings);
                if (this.run == false) {
                    this.run = true;
                    VirtualInfrastructure.this.nodeSource.executeInParallel(this);
                    logger
                            .debug("Thread watching deploying node status update for VirtualInfrastructure started.");
                }
            }
        }

        /**
         * Stop monitoring vm's guest status.
         */
        private void exit() {
            this.run = false;
        }

        private String _buildDescription(String vmName, VMGuestStatus guestStatus,
                org.ow2.proactive.virtualizing.core.State vmState) {
            String lf = System.getProperty("line.separator");
            StringBuilder sb = new StringBuilder(RMDeployingNode.class.getSimpleName() +
                " on virtual machine ");
            sb.append(vmName);
            sb.append(lf);
            sb.append("\tVM's state: ");
            sb.append(vmState);
            sb.append(lf);
            sb.append("\tVM's Heart Beat: ");
            sb.append(guestStatus.getHeartBeat());
            sb.append(lf);
            sb.append("\tVM's Mac addresses: ");
            String[] tmp = guestStatus.getMacAddresses();
            if (tmp == null || tmp.length == 0) {
                sb.append("Unknown");
                sb.append(lf);
            } else {
                for (int i = 0; i < tmp.length; i++) {
                    sb.append(tmp[i]);
                    if (i != (tmp.length - 1)) {
                        sb.append(" - ");
                    }
                }
                sb.append(lf);
            }
            sb.append("\tVM's IP addresses: ");
            tmp = guestStatus.getIPAddresses();
            if (tmp == null || tmp.length == 0) {
                sb.append("Unknown");
            } else {
                for (int i = 0; i < tmp.length; i++) {
                    sb.append(tmp[i]);
                    if (i != (tmp.length - 1)) {
                        sb.append(" - ");
                    }
                }
            }
            return sb.toString();
        }

        private void _fireDeployingNodeUpdate(String vmName, String description) {
            synchronized (deployingNodes) {
                String[] pendings = deployingNodes.get(vmName);
                if (pendings != null) {
                    for (String pnURL : pendings) {
                        if (pnURL != null) {
                            updateDeployingNodeDescription(pnURL, description);
                        }
                    }
                } else {
                    deployingNodes.remove(vmName);
                }
            }
        }
    }

    /**
     * This class holds {@link VirtualMachineManager2} instance and
     * managed {@link VirtualMachine2}. This class was built because
     * {@link VirtualMachineManager2} & {@link VirtualMachine2} are not
     * implementing {@link Serializable}
     */
    private class VirtualMachineManagerHolder implements Serializable {
        /**  */
        private static final long serialVersionUID = 31L;
        /** The kind of infrastructure to manage */
        protected final InfrastructureType infrastructureType;
        /** The hypervisor's url and authentication credentials */
        private final String url, user, pwd;
        private transient VirtualMachineManager2 vmm;
        private transient Map<String, VirtualMachine2> virtualMachines;

        VirtualMachineManagerHolder(InfrastructureType infrastructure, String url, String user, String pwd) {
            this.infrastructureType = infrastructure;
            this.url = url;
            this.user = user;
            this.pwd = pwd;
        }

        VirtualMachine2 getVirtualMachine(String name) throws RMException, VirtualServiceException {
            if (vmm == null) {
                try {
                    vmm = infrastructureType.getVMM(url, user, pwd);
                } catch (Exception e) {
                    throw new RMException(e);
                }
            }
            if (virtualMachines == null) {
                virtualMachines = Collections.synchronizedMap(new HashMap<String, VirtualMachine2>());
            }
            VirtualMachine2 res = virtualMachines.get(name);
            if (res != null) {
                return res;
            } else {
                res = vmm.getNewVM(name);
                virtualMachines.put(name, res);
                return res;
            }
        }
    }

    /** This class allows to cache registered nodes. */
    private class VMNodeCache implements Serializable {
        /**  */
        private static final long serialVersionUID = 31L;
        private final HashMap<String, ArrayList<String>> vmNodeCache = new HashMap<String, ArrayList<String>>();

        private void addNode(String holdingVM, String node) {
            getVMCache(holdingVM).add(node);
            logger.debug("Node added to cache. Current Node cache size: " + getVMCache(holdingVM).size());
        }

        private boolean hasAvailableNode(String holdingVM) {
            return !getVMCache(holdingVM).isEmpty();
        }

        private int numberOfAvailableNodes(String holdingVM) {
            return getVMCache(holdingVM).size();
        }

        private String removeNodeUrl(String holdingVM) {
            ArrayList<String> vmCache = getVMCache(holdingVM);
            String res = vmCache.remove(vmCache.size() - 1);
            logger.debug("Node removed from cache for " + holdingVM + ". Current node cache size is " +
                vmCache.size());
            return res;
        }

        private String removeRandomNodeUrl() {
            while (!vmNodeCache.isEmpty()) {
                String holdingVM = (String) this.vmNodeCache.keySet().toArray()[0];
                ArrayList<String> vmCache = this.vmNodeCache.get(holdingVM);
                if (vmCache.isEmpty()) {
                    this.vmNodeCache.remove(holdingVM);
                } else {
                    String res = vmCache.remove(vmCache.size() - 1);
                    logger.debug("Node removed from cache for " + holdingVM +
                        ". Current node cache size is " + vmCache.size());
                    return res;
                }
            }
            return null;
        }

        private ArrayList<String> getVMCache(String holdingVM) {
            ArrayList<String> res = this.vmNodeCache.get(holdingVM);
            if (res == null) {
                res = new ArrayList<String>();
                vmNodeCache.put(holdingVM, res);
            }
            return res;
        }

        private void removeCache(String holdingVM) {
            this.vmNodeCache.remove(holdingVM);
        }
    }

    /**
     * The different infrastructure types supported
     * by this infrastructure manager
     */
    private enum InfrastructureType implements Serializable {
        XEN_SERVER("xenserver") {
            @Override
            protected VirtualMachineManager2 getVMM(String url, String user, String pwd)
                    throws VirtualServiceException {
                return new XenServerVMM(url, user, pwd);
            }
        },
        VMWARE("vmware") {
            @Override
            protected VirtualMachineManager2 getVMM(String url, String user, String pwd)
                    throws VirtualServiceException {
                return new VMwareVMM(url, user, pwd);
            }
        },
        VIRTUALBOX("virtualbox") {
            @Override
            protected VirtualMachineManager2 getVMM(String url, String user, String pwd)
                    throws VirtualServiceException {
                return new VirtualboxVMM(url, user, pwd);
            }
        },
        HYPERV_WMI("hyperv-wmi") {
            @Override
            protected VirtualMachineManager2 getVMM(String url, String user, String pwd)
                    throws VirtualServiceException {
                return new org.ow2.proactive.virtualizing.hypervwmi.HyperVVMM(url, user, pwd);
            }
        },
        HYPERV_WINRM("hyperv-winrm") {
            @Override
            protected VirtualMachineManager2 getVMM(String url, String user, String pwd)
                    throws VirtualServiceException {
                return new org.ow2.proactive.virtualizing.hypervwinrm.HyperVVMM(url, user, pwd);
            }
        };

        private final String id;

        private InfrastructureType(String id) {
            this.id = id;
        }

        /** to implement in each enum */
        protected abstract VirtualMachineManager2 getVMM(String url, String user, String pwd)
                throws VirtualServiceException;

        private String getValue() {
            return this.id;
        }

        /**
         * Returns the {@link InfrastructureType} matching the String argument
         * @param type the string representing the {@link InfrastructureType} associated value.
         * @return the matching {@link InfrastructureType} or null if the argument doesn't match.
         */
        public static InfrastructureType getInfrastructureType(String type) {
            if (XEN_SERVER.getValue().compareToIgnoreCase(type) == 0) {
                return XEN_SERVER;
            } else if (VMWARE.getValue().compareToIgnoreCase(type) == 0) {
                return VMWARE;
            } else if (VIRTUALBOX.getValue().compareToIgnoreCase(type) == 0) {
                return VIRTUALBOX;
            } else if (HYPERV_WINRM.getValue().compareToIgnoreCase(type) == 0) {
                return HYPERV_WINRM;
            } else if (HYPERV_WMI.getValue().compareToIgnoreCase(type) == 0) {
                return HYPERV_WMI;
            } else {
                return null;
            }
        }
    }

    /**
     * The properties available for {@link VirtualInfrastructure} and ProActive
     * Runtime configurations.
     */
    public enum Prop implements Serializable {
        /** The following set of key is common to every possible implementation, maybe
         * the different Providers will need more information. See the associated
         * VirtualInfrastructureProvider implementation for more details.
         * COMPULSORY - must be set by user
         * DEFAULT - required but with a default value
         * OPTIONAL - can be omitted ( the node acquisition semantic can change )
         * NOT REQUIRED - set by the program itself */

        /** this key will be used (set) by the virtual infrastructure starter
         * to fix the holding virtual machine's name. It will also be used when removing a node to kill this holding
         * virtual machine - NOT REQUIRED*/
        HOLDING_VIRTUAL_MACHINE(VIRMNodeStarter.HOLDING_VM_KEY),
        /** The manager user of your virtualized infrastructure - COMPULSORY*/
        HYPERVISOR_USER("vmmuser"),
        /** The manager's password - COMPULSORY */
        HYPERVISOR_PWD("vmmpwd"),
        /** The hypervisor's url allowing remote management - COMPULSORY */
        HYPERVISOR_URL("vmmurl"),
        /** The virtual machine you want to manage - COMPULSORY */
        VIRTUAL_MACHINE("vmtemplate"),
        /** The maximum number of managed instances of the virtual machine
         * ( implementation dependent ) - DEFAULT 1 */
        MAX_INSTANCE("vmmax"),
        /** hostCapacity ( number of runtime to boot ) - DEFAULT 1 */
        HOST_CAPACITY("hostCapacity"),
        /** The VirtualInfrastructureProvider implementation to use - COMPULSORY */
        INFRASTRUCTURE("infrastructure"),
        /** This key must be used only to push information into the virtual machine.
         * The resource manager's url is gotten from RMCore interface and hasn't to be
         * supplied by users... - NOT REQUIRED*/
        RM_URL("rmUrl"),
        /** If this key is present in a configuration file, providers must start nodes using
         * {@link VirtualInfrastructureNodeStarterRegister}, otherwise, must use
         * {@link VirtualInfrastructureNodeStarter} - OPTIONAL*/
        RM_CREDS("credentials"),
        /** the nodesource's name key - NOT REQUIRED */
        NODESOURCE("nodesource"),
        /** This property is used to get node's url when the NodeStarter
         * registers the nodes itself */
        NODE_URL("nodeUrl"),
        /** To be able to count the real number of
         * deployed nodes per virtual machines */
        IS_ALREADY_REGISTERED("isAlreadyRegistered"), PROACTIVE_CONFIGURATION("paconfig");
        Prop(String value) {
            this.value = value;
        }

        private final String value;

        public String getValue() {
            return this.value;
        }
    }

}
