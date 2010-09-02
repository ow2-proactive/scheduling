/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.nodesource.utils.NamesConvertor;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;
import org.ow2.proactive.resourcemanager.utils.VirtualInfrastructureNodeStarter;
import org.ow2.proactive.resourcemanager.utils.VirtualInfrastructureNodeStarterRegister;
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
    private static final long serialVersionUID = 21L;

    private static Logger logger = ProActiveLogger.getLogger(RMLoggers.NODESOURCE);

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

    /** To maintains the number of Nodes registered within the Resource Manager per Virtual Machine.
     * To be able to decide to power off/destroy the machine. */
    private Map<String, Integer> numberOfRegisteredNode = new HashMap<String, Integer>();
    /** To maintain the number of required nodes asked by the node source. */
    private int numberOfPendingNodes = 0;
    /** To be able to register the exact number of nodes the RM asked for.
     * Is used only in case of local Node registration. */
    private int numberOfRequiredNodes = 0;
    /** To be able to queue acquireNodeRequest because a newlly started vm is
     * supposed to acquire hostCapacity Nodes. */
    private long lastStartedVMTimeStamp = Long.MIN_VALUE;
    /** To keep track of available Nodes per virtual machines */
    private VMNodeCache availableNodes = new VMNodeCache();
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
    protected ArrayList<String> runningVM = new ArrayList<String>();
    /** The list of clone virtual machines */
    protected ArrayList<String> cloneVM = new ArrayList<String>();
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
    /** The resource manager's url */
    @Configurable(description = "Resource Manager's url")
    protected String RMUrl = PAActiveObject.getActiveObjectNodeUrl(PAActiveObject.getStubOnThis()).replace(
            PAResourceManagerProperties.RM_NODE_NAME.getValueAsString(), "");
    /** A path to a credentials file */
    @Configurable(credential = true, fileBrowser = true, description = "Absolute path of the rm.cred file")
    protected File RMCredentials;
    protected String cred;
    /** The properties used for the {@link VirtualInfrastructure} configuration.
     * See {@link VirtualInfrastructure.Prop} */
    protected Properties properties;
    /** The configuration of the managed virtual machine PART ( from paConf ). */
    protected Hashtable<String, String> confTable = new Hashtable<String, String>();

    private ReentrantLock availableNodesLock = new ReentrantLock();
    private ReentrantLock deploymentLock = new ReentrantLock();
    private ReentrantLock requiredNodesLock = new ReentrantLock();
    private ReentrantLock registeredNodesLock = new ReentrantLock();

    /** This constant is used as a circuit broker threshold */
    public static final int circuitBrokerThreshold = 5;

    public VirtualInfrastructure() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void acquireAllNodes() {
        logger.debug("Acquiring all nodes.");
        getAllAvailableNodes();
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
        if (getAnAvailableNode()) {
            return;
        } else {
            try {
                startNewVirtualMachineIfNecessary(1);
            } catch (Throwable e) {
                logger.error("An error occured while acquiring a node.", e);
            }
        }
    }

    /**
     * Caches the node for future reuse or destroy it if forever == true
     * @param node the node to remove
     * @param forever destroy the node if equal true, cache it otherwise
     * @throws RMException if a problem occurs.
     */
    private void removeNode(Node node, boolean forever) throws RMException {
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
            if (forever) {
                logger.debug("Destroying node " + nodeName + " within vm " + holdingVM);
                decNumberOfRegisteredNodes(holdingVM);
                try {
                    node.getProActiveRuntime().killNode(nodeName);
                    node.getProActiveRuntime().killRT(true);
                } catch (ProActiveException e) {
                    throw new RMException("An error occured while removing a node.", e);
                }
            } else {
                logger.debug("Node " + nodeName + " added in node cache for vm " + holdingVM + ".");
                availableNodes.addNode(holdingVM, node.getNodeInformation().getURL());
            }
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
     * 		  parameters[8] is the RM's url
     * 		  parameters[9] is the path to a credentials file
     */
    @Override
    public BooleanWrapper configure(Object... parameters) {
        logger.info("Configuration read from user input");
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
        if (parameters[index] != null) {
            definePARTConf((byte[]) parameters[index++]);
        }
        if (parameters[index] != null) {
            RMUrl = parameters[index++].toString();
        }
        if (parameters[index] != null) {
            defineCredentials((byte[]) parameters[index++]);
        } else {
            logger.info("Credentials not supplied, will register nodes locally.");
        }
        return new BooleanWrapper(true);
    }

    @Override
    public void registerAcquiredNode(Node node) throws RMException {
        try {
            requiredNodesLock.lock();
            String holdingVM = node.getProperty(Prop.HOLDING_VIRTUAL_MACHINE.getValue());
            if (node.getProperty(Prop.IS_ALREADY_REGISTERED.getValue()) == null) {
                incNumberOfRegisteredNodes(holdingVM);
                node.setProperty(Prop.IS_ALREADY_REGISTERED.getValue(), "true");
                logger.debug("A New node was added by " + this.getClass().getSimpleName() + ". " +
                    "Property isAlreadyRegistered is now true.");
            } else {
                logger.debug("A previously used node was added by " + this.getClass().getSimpleName() + ". " +
                    "Property isAlreadyRegistered was true.");
            }
        } catch (ProActiveException e) {
            logger.error("Unable to state about node registration " + node.getNodeInformation().getURL());
        } finally {
            //if not using local node registration, must decrease the number of required nodes.
            //otherwise, this is done by the node registration waiting thread
            if (!useLocalNodeRegistration()) {
                if (numberOfRequiredNodes > 0)
                    numberOfRequiredNodes--;
            }
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
            deploymentLock.unlock();
        }
    }

    @Override
    public void removeNode(Node node) throws RMException {
        this.removeNode(node, false);
    }

    /**
     * Check that the supplied properties are sufficient.
     * @throws RMException
     */
    private void checkInitialization() throws RMException {
        //Check that user property was supplied
        String user = properties.getProperty(Prop.HYPERVISOR_USER.getValue());
        if (user == null || user.equals(""))
            throw new RMException("Cannot find " + Prop.HYPERVISOR_USER.getValue() +
                " in configuration file.");

        //Check that user's password property was supplied
        String pwd = properties.getProperty(Prop.HYPERVISOR_PWD.getValue());
        if (pwd == null || pwd.equals(""))
            throw new RMException("Cannot find " + Prop.HYPERVISOR_PWD.getValue() + " in configuration file.");

        //Check that hypervisor's url was supplied
        String url = properties.getProperty(Prop.HYPERVISOR_URL.getValue());
        if (url == null || url.equals(""))
            throw new RMException("Cannot find " + Prop.HYPERVISOR_URL.getValue() + " in configuration file.");

        //Check that the template Virtual Machine's name was supplied
        VMTemplate = properties.getProperty(Prop.VIRTUAL_MACHINE.getValue());
        if (VMTemplate == null || VMTemplate.equals(""))
            throw new RMException("Cannot find " + Prop.VIRTUAL_MACHINE.getValue() +
                " in configuration file.");

        //Check the infrastructure type
        String infrastructureType = properties.getProperty(Prop.INFRASTRUCTURE.getValue());
        if (infrastructureType == null || infrastructureType.equals(""))
            throw new RMException("Cannot find " + Prop.INFRASTRUCTURE.getValue() + " in configuration file.");
        InfrastructureType it = InfrastructureType.getInfrastructureType(infrastructureType);
        if (it == null) {
            throw new RMException("A bad virtual infrastructure type was supplied");
        }
        virtualMachineManagerHolder = new VirtualMachineManagerHolder(it, url, user, pwd);

        //Initializes the max instance count
        String maxInstanceString = properties.getProperty(Prop.MAX_INSTANCE.getValue());
        if (maxInstanceString != null) {
            try {
                VMMax = Integer.parseInt(maxInstanceString);
            } catch (Throwable e) {
                logger.warn("The maximum number of instances is " + VMMax, e);
            }
        }

        //Initializes the host capacity for virtual machines
        String hostCapacityString = properties.getProperty(Prop.HOST_CAPACITY.getValue());
        if (hostCapacityString != null) {
            try {
                hostCapacity = Integer.parseInt(hostCapacityString);
            } catch (Throwable e) {
                logger.warn("The number of nodes per virtual machines is " + hostCapacity, e);
            }
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
    private void definePARTConf(String file) {
        InputStream is = null;
        try {
            is = new FileInputStream(new File(file));
            populateConfTable(is);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot read ProActive configuration from supplied file.");
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
     * Loads the PART configuration file for virtual machine.
     * @param bs a byte array containing a proactive configuration
     */
    private void definePARTConf(byte[] bs) {
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(bs);
            populateConfTable(is);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot read ProActive configuration from supplied file.");
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

    /** Defines the credentials value */
    private void defineCredentials(byte[] bs) {
        cred = new String(bs);
    }

    /** Defines the credentials value
     * @throws IOException */
    private void defineCredentials(String file) throws IOException {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new FileReader(new File(file)));
            int read;
            while ((read = br.read()) != -1) {
                sb.append((char) read);
            }
            cred = sb.toString();
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    /**
     * Parse parameters from {@link #addNodesAcquisitionInfo(Object...)} to
     * update configuration file path field.
     * @param config a byte array containing a set of key=value for configuration.
     * @throws RMException
     */
    private boolean defineConfigFile(byte[] config) throws RMException {
        InputStream is = null;
        Properties prop = new Properties();
        try {
            try {
                is = new ByteArrayInputStream(config);
                prop.load(is);
            } catch (Throwable e) {
                logger.info("Cannot read configuration from supplied config file");
                return false;
            }
            properties = prop;
            return true;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.warn(e);
                }
            }
        }
    }

    /**
     * Return an immutable (cloned) ProActive Configuration
     * backed by a {@link Hashtable}
     * @return The remote PART configuration that will be used for
     * a newly started virtual machine.
     */
    public Hashtable<String, String> getProActiveConfiguration() {
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
            //updating variable regarding timeout
            long currentTimeStamp = System.currentTimeMillis();
            if ((currentTimeStamp - lastStartedVMTimeStamp) > NODE_URL_ACQUISITION_TIMEOUT) {
                if (numberOfPendingNodes != 0) {
                    logger
                            .debug("The number of pending node acquisition is not zero and is reseted because of a timeout." +
                                "Previous value was " + numberOfPendingNodes);
                    numberOfPendingNodes = 0;
                }
                if (numberOfRequiredNodes != 0) {
                    logger
                            .debug("The number of required nodes is not zero and is reseted because of a timeout." +
                                "Previous value was " + numberOfRequiredNodes);
                    numberOfRequiredNodes = 0;
                }
            }
            //a previously deployed vm can handle node acquisition request
            if (numberOfPendingNodes >= required) {
                numberOfRequiredNodes += required;
                numberOfPendingNodes -= required;
                logger.debug("A node acquisition can be served by a recently started Virtual Machine.");
                logger.debug("Required & pending nodes numbers updated: req=" + numberOfRequiredNodes +
                    " pend=" + numberOfPendingNodes);
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
            if (!useLocalNodeRegistration()) { //will connect to rm from virtual machine
                logger.debug("Using remote node registration");
                setEnvironmentForStarterRegisterAndStart(toStart);
            } else {
                logger.debug("Using local node registration");
                setEnvironmentForStarterAndStart(toStart);
                WaitingForNewNodeThread myThread = new WaitingForNewNodeThread(nodeSource, toStartName,
                    hostCapacity);
                myThread.start();
            }
            numberOfPendingNodes += (hostCapacity - required);
            lastStartedVMTimeStamp = System.currentTimeMillis();
            logger.debug("Required & pending nodes numbers updated: req=" + numberOfRequiredNodes + " pend=" +
                numberOfPendingNodes);
            runningVM.add(toStartName);
            count++;
            logger.debug("A new Virtual Machine was started, current count: " + count);
        } finally {
            deploymentLock.unlock();
            requiredNodesLock.unlock();
        }
    }

    /**
     * Sets the environment of the virtual machine to be able to run
     * {@link VirtualInfrastructureNodeStarter} from within the virtual machine
     * and starts the virtual machine.
     * @param toStart the virtual machine to start
     * @return true if the virtual machine has been started, false otherwise.
     * @throws RMException
     * @throws VirtualServiceException
     */
    private boolean setEnvironmentForStarterAndStart(VirtualMachine2 toStart) throws RMException,
            VirtualServiceException {
        setEnvrionmentForStarter(toStart);
        return toStart.powerOn();
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
    private boolean setEnvironmentForStarterRegisterAndStart(VirtualMachine2 toStart) throws RMException,
            VirtualServiceException {
        setEnvrionmentForStarter(toStart);
        setEnvironmentForStarterRegister(toStart);
        return toStart.powerOn();
    }

    /**
     * Sets the environment of the virtual machine to be able to run
     * {@link VirtualInfrastructureNodeStarter} from within the virtual machine.
     * 	 * @param toStart the virtual machine to start
     * @return true if the virtual machine has been started, false otherwise.
     * @throws RMException
     * @throws VirtualServiceException
     */
    private void setEnvrionmentForStarter(VirtualMachine2 toDeploy) throws RMException {
        Hashtable<String, String> partConf = getProActiveConfiguration();
        Set<String> keys = partConf.keySet();
        try {
            toDeploy.pushData(VirtualInfrastructure.Prop.RM_URL.getValue(), null);
            toDeploy.pushData(VirtualInfrastructure.Prop.RM_CREDS.getValue(), null);
            for (int j = 0; j < hostCapacity; j++) {
                toDeploy.pushData(VirtualInfrastructure.Prop.NODE_URL.getValue() + "." + j, null);
            }
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
        } catch (VirtualServiceException e) {
            throw new RMException("Unnable to deploy virtual machine " + toDeploy, e);
        }
    }

    /**
     * Sets the environment of the virtual machine to be able to run
     * {@link VirtualInfrastructureNodeStarterRegister} from within the virtual machine.
     * 	 * @param toStart the virtual machine to start
     * @return true if the virtual machine has been started, false otherwise.
     * @throws RMException
     * @throws VirtualServiceException
     */
    private void setEnvironmentForStarterRegister(VirtualMachine2 toDeploy) throws RMException,
            VirtualServiceException {
        toDeploy.pushData(VirtualInfrastructure.Prop.RM_URL.getValue(), RMUrl);
        toDeploy.pushData(VirtualInfrastructure.Prop.RM_CREDS.getValue(), cred);
        //push the nodeSource's name
        toDeploy.pushData(VirtualInfrastructure.Prop.NODESOURCE.getValue(), nodeSource.getName());
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

    /**
     * Tries to add a node to the RMCore from a previously deployed VM.
     * @return true if such a node exists and has been added to the RMCore,
     * false otherwise
     */
    private boolean getAnAvailableNode() {
        try {
            availableNodesLock.lock();
            String nodeUrl = availableNodes.removeRandomNodeUrl();
            if (nodeUrl == null) {
                return false;
            } else {
                return nodeSource.acquireNode(nodeUrl, nodeSource.getProvider()).booleanValue();
            }
        } catch (Throwable t) {
            logger.error("Failled to add an available node to RMCore.", t);
            return false;
        } finally {
            availableNodesLock.unlock();
        }
    }

    /**
     * Add all previously released Nodes of a still
     * running VM to the RMCore
     */
    private void getAllAvailableNodes() {
        while (getAnAvailableNode()) {
            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }
    }

    /**
     * Decrease the number of registered nodes for a given virtual machine.
     * See {@link this#numberOfRegisteredNode}.
     * @param vm The virtual machine which needs update.
     */
    private void decNumberOfRegisteredNodes(String vm) {
        try {
            registeredNodesLock.lock();
            if (!numberOfRegisteredNode.containsKey(vm)) {
                numberOfRegisteredNode.put(vm, new Integer(0));
                return;
            }
            Integer current = numberOfRegisteredNode.get(vm);
            numberOfRegisteredNode.put(vm, current - 1);
            logger.debug("Number of registered nodes for " + vm + " decremented. Current value: " +
                (current - 1));
        } finally {
            registeredNodesLock.unlock();
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

    /**
     * To be able to know is the user want to use local or remote
     * node registration.
     * This is decided regarding the configuration supplied by the user.
     * If RM User & RM User's password are supplied, remote registration will
     * be used, otherwise, local registration will be used.
     * @return true if using local registration, false otherwise.
     */
    private boolean useLocalNodeRegistration() {
        return RMUrl == null || RMUrl.equals("");
    }

    /*----------------------------------------
     * Member classes
     *---------------------------------------*/

    /**
     * This private class is in charge of waiting for newly created nodes to be
     * registered in the VirtualMachine configuration data file thanks to a guest property.
     */
    private class WaitingForNewNodeThread extends Thread {
        private String vmName;
        private int capacity;
        private NodeSource nodeSource;

        WaitingForNewNodeThread(NodeSource ns, String vmName, int c) {
            this.nodeSource = ns;
            this.capacity = c;
            this.vmName = vmName;
        }

        @Override
        public void run() {
            logger.debug("A WaitingForNewNodeThread was started.");
            for (int i = 0; i < capacity; i++) {
                try {
                    long begin = System.currentTimeMillis();
                    int interruptedExceptionCB = 0, virtualServiceExceptionCB = 0;
                    String nodeUrl = null;
                    while ((System.currentTimeMillis() - NODE_URL_ACQUISITION_TIMEOUT) < begin) {
                        try {
                            VirtualMachine2 vm = virtualMachineManagerHolder.getVirtualMachine(vmName);
                            nodeUrl = vm.getData(Prop.NODE_URL.getValue() + "." + i);
                            logger.debug(vmName + "." + Prop.NODE_URL.getValue() + "." + i + " returned " +
                                nodeUrl);
                            if (nodeUrl != null && !nodeUrl.equals("")) {
                                break;
                            } else {
                                logger.debug("waiting...");
                                Thread.sleep(2000);
                            }
                        } catch (InterruptedException e) {
                            if (interruptedExceptionCB == circuitBrokerThreshold) {
                                Thread.currentThread().interrupt();
                                logger.error("Cannot retrieve node's information from virtual machine.", e);
                                break;
                            }
                            interruptedExceptionCB++;
                        } catch (VirtualServiceException e) {
                            if (virtualServiceExceptionCB == circuitBrokerThreshold) {
                                logger.error("Unable to retrieve node's url ( id " +
                                    Prop.NODE_URL.getValue() + "." + i + " ) from virtual machine " + vmName,
                                        e);
                                continue;
                            }
                            virtualServiceExceptionCB++;
                        }
                    }
                    if (nodeUrl != null && !nodeUrl.equals("")) {
                        try {
                            requiredNodesLock.lock();
                            availableNodesLock.lock();
                            if (numberOfRequiredNodes > 0) {
                                logger.debug("Retrieved a new node url: " + nodeUrl);
                                nodeSource.acquireNode(nodeUrl, nodeSource.getProvider());
                                numberOfRequiredNodes--;
                                logger.debug("Number of required nodes = " + numberOfRequiredNodes);
                            } else {
                                logger.debug("New node creation received but not required." +
                                    "New node added in cache. Number of required nodes = " +
                                    numberOfRequiredNodes);
                                availableNodes.addNode(this.vmName, nodeUrl);
                            }
                        } catch (Throwable t) {
                            logger.error("An Error occured while adding a node to RMCore.", t);
                        } finally {
                            requiredNodesLock.unlock();
                            availableNodesLock.unlock();
                        }
                    } else {
                        logger.warn("The node acquisition #" + (i + 1) + " failled due to timeout.");
                    }
                } catch (RMException e) {
                    logger.error("A RMException occured.", e);
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
        private static final long serialVersionUID = 21L;
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
        private static final long serialVersionUID = 21L;
        private HashMap<String, ArrayList<String>> vmNodeCache = new HashMap<String, ArrayList<String>>();

        void addNode(String holdingVM, String node) {
            getVMCache(holdingVM).add(node);
            logger.debug("Node added to cache. Current Node cache size: " + getVMCache(holdingVM).size());
        }

        boolean removeNode(String holdingVM, String node) {
            boolean res = getVMCache(holdingVM).remove(node);
            if (res) {
                logger.debug("Node removed from cache for " + holdingVM + ". Current node cache size is " +
                    getVMCache(holdingVM).size());
            } else {
                logger.warn("Removing Node from cache failled. Current Node cache size: " +
                    getVMCache(holdingVM).size());
            }
            return res;
        }

        boolean hasAvailableNode(String holdingVM) {
            return !getVMCache(holdingVM).isEmpty();
        }

        int numberOfAvailableNodes(String holdingVM) {
            return getVMCache(holdingVM).size();
        }

        String removeNodeUrl(String holdingVM) {
            ArrayList<String> vmCache = getVMCache(holdingVM);
            String res = vmCache.remove(vmCache.size() - 1);
            logger.debug("Node removed from cache for " + holdingVM + ". Current node cache size is " +
                vmCache.size());
            return res;
        }

        String removeRandomNodeUrl() {
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

        void removeCache(String holdingVM) {
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
        HOLDING_VIRTUAL_MACHINE("holdingVM"),
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
