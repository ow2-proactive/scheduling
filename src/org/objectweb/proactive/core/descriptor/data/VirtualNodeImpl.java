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

import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.services.FaultToleranceService;
import org.objectweb.proactive.core.descriptor.services.P2PDescriptorService;
import org.objectweb.proactive.core.descriptor.services.ServiceThread;
import org.objectweb.proactive.core.descriptor.services.ServiceUser;
import org.objectweb.proactive.core.descriptor.services.UniversalService;
import org.objectweb.proactive.core.event.NodeCreationEvent;
import org.objectweb.proactive.core.event.NodeCreationEventListener;
import org.objectweb.proactive.core.event.NodeCreationEventProducerImpl;
import org.objectweb.proactive.core.event.RuntimeRegistrationEvent;
import org.objectweb.proactive.core.event.RuntimeRegistrationEventListener;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.node.NodeImpl;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.process.filetransfer.FileTransfer;
import org.objectweb.proactive.core.process.filetransfer.FileTransferWorkShop;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.ext.security.PolicyServer;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.p2p.service.node.P2PNodeLookup;
import org.objectweb.proactive.p2p.service.util.P2PConstants;


/**
 * A <code>VirtualNode</code> is a conceptual entity that represents one or several nodes. After activation
 * a <code>VirtualNode</code> represents one or several nodes.
 *
 * @author  ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.3
 * @see ProActiveDescriptor
 * @see VirtualMachine
 */
public class VirtualNodeImpl extends NodeCreationEventProducerImpl
    implements VirtualNode, Serializable, RuntimeRegistrationEventListener,
        NodeCreationEventListener, ServiceUser {

    /** Logger */
    private final static Logger P2P_LOGGER = Logger.getLogger(Loggers.P2P_VN);
    public static int counter = 0;

    //
    //  ----- PRIVATE MEMBERS -----------------------------------------------------------------------------------
    //

    /** Reference on the local runtime*/
    protected transient ProActiveRuntimeImpl proActiveRuntimeImpl;

    /** the name of this VirtualNode */
    private String name;

    /** the property of this virtualNode, property field can take five value: null,unique, unique_singleAO, multiple, multiple_cyclic */
    private String property;

    /** the list of remote virtual machines associated with this VirtualNode */
    private java.util.ArrayList virtualMachines;

    /** the list of local virtual machine (normally one) associated with this VirtualNode */
    private java.util.ArrayList localVirtualMachines;

    /** index of the last associated jvm used */
    private int lastVirtualMachineIndex;

    /** the list of RuntimeForwarder that have been created */
    private ArrayList createdRuntimeF;

    /** the list of nodes linked to this VirtualNode that have been created*/
    private java.util.ArrayList createdNodes;

    /** the list of file transfers to deploy*/

    //TODO check if this is the best place to init
    private java.util.ArrayList fileTransferDeploy = new ArrayList();

    /** the list of file transfers to retrieve*/
    private java.util.ArrayList fileTransferRetrieve = new ArrayList();

    /** index of the last node used */
    private int lastNodeIndex;

    /** Number of Nodes mapped to this VirtualNode in the XML Descriptor */
    private int nbMappedNodes;

    /** Minimum number of nodes needed for this virtualnode while waiting on the nodes
     * creation.
     */
    private int minNumberOfNodes = 0;

    /** Number of Nodes mapped to this VitualNode in the XML Descriptor that are actually created */
    private int nbCreatedNodes;

    /** true if the node has been created*/
    private boolean nodeCreated = false;
    private boolean isActivated = false;

    /** the list of VitualNodes Id that this VirualNode is waiting for in order to create Nodes on a JVM
     * already assigned in the XML descriptor */
    private Hashtable awaitedVirtualNodes;
    private String registrationProtocol;
    private boolean registration = false;
    private boolean waitForTimeout = false;

    //boolean used to know if the vn is mapped only with a P2P service that request MAX nodes
    // indeed the behavior is different when returning nodes
    private boolean MAX_P2P = false;

    //protected int MAX_RETRY = 70;

    /** represents the timeout in ms*/
    protected long timeout = 70000;

    /** represents the sum of the timeout + current time in ms*/
    protected long globalTimeOut;
    private Object uniqueActiveObject = null;

    // Security 
    private ProActiveSecurityManager proactiveSecurityManager;
    private String policyServerFile;
    protected String jobID = ProActive.getJobId();

    // FAULT TOLERANCE
    private FaultToleranceService ftService;
    private Vector p2pNodes = new Vector();

    // PAD infos
    private boolean mainVirtualNode;
    private String padURL;
    private Vector p2pNodeslookupList = new Vector();

    //
    //  ----- CONSTRUCTORS -----------------------------------------------------------------------------------
    //

    /**
     * Contructs a new intance of VirtualNode
     */
    VirtualNodeImpl() {
    }

    /**
     * Contructs a new intance of VirtualNode
     */
    VirtualNodeImpl(String name,
        ProActiveSecurityManager proactiveSecurityManager, String padURL,
        boolean isMainVN) {
        // if we launch several times the same application 
        // we have to change the name of the main VNs because of
        // the register, otherwise we will monitor each time all the last
        // main VNs with the same name.
        if (isMainVN) {
            this.name = name + (counter++);
        } else {
            this.name = name;
        }

        virtualMachines = new java.util.ArrayList(5);
        localVirtualMachines = new java.util.ArrayList();
        createdNodes = new java.util.ArrayList();
        createdRuntimeF = new ArrayList();
        awaitedVirtualNodes = new Hashtable();
        proActiveRuntimeImpl = (ProActiveRuntimeImpl) ProActiveRuntimeImpl.getProActiveRuntime();

        if (logger.isDebugEnabled()) {
            logger.debug("vn " + this.name + " registered on " +
                proActiveRuntimeImpl.getVMInformation().getVMID().toString());
        }

        // SECURITY
        this.proactiveSecurityManager = proactiveSecurityManager;

        // added for main infos
        this.mainVirtualNode = isMainVN;
        this.padURL = padURL;
    }

    //
    //  ----- PUBLIC METHODS -----------------------------------------------------------------------------------
    //

    /**
     * Sets the property attribute to the given value
     * @param value property the value of property attribute, this value can be "unique", "unique_singleAO", "multiple", "multiple_cyclic" or nothing
     */
    public void setProperty(String value) {
        this.property = value;
    }

    public String getProperty() {
        return property;
    }

    public long getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout variable to the given value.
     * Using waitForTimeout = true will force this VirtualNode to wait until the timeout expires
     * before giving access to its nodes, waitForTimeout = false allows this VirtualNode to give
     * access to its node when number of nodes expected are really created or the timeout
     * has expired
     * @param timeout the timeout to set in ms
     * @param waitForTimeout to force or not this VirtualNode to wait untile timeout's expiration
     */
    public void setTimeout(long timeout, boolean waitForTimeout) {
        this.timeout = timeout;
        this.waitForTimeout = waitForTimeout;
    }

    /**
     * Sets the name of this VirtualNode
     * @param s the name of this Virtual Node
     */
    public void setName(String s) {
        this.name = s;
    }

    public String getName() {
        return name;
    }

    public void addVirtualMachine(VirtualMachine virtualMachine) {
        virtualMachines.add(virtualMachine);

        if (!((virtualMachine.getCreatorId()).equals(this.name))) {
            // add in the hashtable the vm's creator id, and the number of nodes that should be created
            awaitedVirtualNodes.put(virtualMachine.getCreatorId(),
                virtualMachine);

            //we need to do it here otherwise event could occurs, whereas vm 's creator id is not in the hash map
            //just synchro pb, this workaround solves the pb
        }

        if (logger.isDebugEnabled()) {
            logger.debug("mapped VirtualNode=" + name +
                " with VirtualMachine=" + virtualMachine.getName());
        }
    }

    public void addFileTransferDeploy(FileTransfer ft) {
        if (ft == null) {
            return;
        }

        fileTransferDeploy.add(ft);

        if (logger.isDebugEnabled()) {
            logger.debug("mapped VirtualNode=" + name +
                " with FileTransferDeploy id=" + ft.getId());
        }
    }

    public void addFileTransferRetrieve(FileTransfer ft) {
        if (ft == null) {
            return;
        }

        fileTransferRetrieve.add(ft);

        if (logger.isDebugEnabled()) {
            logger.debug("mapped VirtualNode=" + name +
                " with FileTransferRetrieve id=" + ft.getId());
        }
    }

    public VirtualMachine getVirtualMachine() {
        if (virtualMachines.isEmpty()) {
            return null;
        }

        VirtualMachine vm = (VirtualMachine) virtualMachines.get(lastVirtualMachineIndex);

        return vm;
    }

    /**
     * Activates all the Nodes mapped to this VirtualNode in the XML Descriptor
     */
    public void activate() {
        if (!isActivated) {
            proActiveRuntimeImpl.addRuntimeRegistrationEventListener(this);
            proActiveRuntimeImpl.registerLocalVirtualNode(this, this.name);

            for (int i = 0; i < virtualMachines.size(); i++) {
                VirtualMachine vm = getVirtualMachine();

                // first check if it is a process that is attached to the vm
                if (vm.hasProcess()) {
                    boolean vmAlreadyAssigned = !((vm.getCreatorId()).equals(this.name));
                    ExternalProcess process = getProcess(vm, vmAlreadyAssigned);

                    // Test if that is this virtual Node that originates the creation of the vm
                    // else the vm was already created by another virtualNode, in that case, nothing is
                    // done at this point, nodes creation will occur when the runtime associated with the jvm
                    // will register.
                    if (!vmAlreadyAssigned) {
                        setParameters(process, vm);

                        // It is this virtual Node that originates the creation of the vm
                        try {
                            proActiveRuntimeImpl.createVM(process);
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                            logger.error("cannot activate virtualNode " +
                                this.name + " with the process " +
                                process.getCommand());
                        }
                    }
                } else {
                    // It is a service that is mapped to the vm.
                    startService(vm);
                }

                increaseIndex();
            }

            // local nodes creation 
            for (int i = 0; i < localVirtualMachines.size(); i++) {
                String protocol = (String) localVirtualMachines.get(i);
                internalCreateNodeOnCurrentJvm(protocol);
            }

            //initialization of the global timeout
            globalTimeOut = System.currentTimeMillis() + timeout;
            isActivated = true;

            if (registration) {
                register();
            }

            // FAULT TOLERANCE
            try {
                if (this.ftService != null) {
                    // register nodes only if ressource is not null
                    this.ftService.registerRessources(this.getNodes());
                }
            } catch (NodeException e) {
                logger.error(e.getMessage());
            }
        } else {
            logger.info("VirtualNode " + this.name + " already activated !!!");
        }
    }

    /**
     * return true if the virtual node represents a node with a main method
     * @return boolean
     */
    public boolean isMainVirtualNode() {
        return mainVirtualNode;
    }

    /**
     * return the url of the proactive descriptor which created this VN
     * @return String
     */
    public String getPadURL() {
        return padURL;
    }

    /**
     * set tha main property of this VN
     * @param isMainVirtualNode
     */
    public void setIsMainVirtualNode(boolean isMainVirtualNode) {
        mainVirtualNode = isMainVirtualNode;
    }

    /**
     * set the url of the pad
     * @param XMLpadURL url of the pad
     */
    public void setPadURL(String XMLpadURL) {
        this.padURL = XMLpadURL;
    }

    /**
     * Returns the number of Nodes mapped to this VirtualNode in the XML Descriptor
     * @return int
     */
    public int getNodeCount() {
        return nbMappedNodes;
    }

    /**
     * @deprecated use {@link #getNumberOfCurrentlyCreatedNodes()} or {@link #getNumberOfCreatedNodesAfterDeployment()} instead
     */
    public int createdNodeCount() {
        throw new RuntimeException(
            "This method is deprecated, use getNumberOfCurrentlyCreatedNodes() or getNumberOfCreatedNodesAfterDeployment()");
    }

    /*
     *  (non-Javadoc)
     * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#getNumberOfCurrentlyCreatedNodes()
     */
    public int getNumberOfCurrentlyCreatedNodes() {
        return nbCreatedNodes;
    }

    /*
     *  (non-Javadoc)
     * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#getNumberOfCreatedNodesAfterDeployment()
     */
    public int getNumberOfCreatedNodesAfterDeployment() {
        try {
            waitForAllNodesCreation();
        } catch (NodeException e) {
            logger.error("Problem occured while waiting for nodes creation");
        }

        return nbCreatedNodes;
    }

    /**
     * Returns the first Node created among Nodes mapped to this VirtualNode in the XML Descriptor
     * Another call to this method will return the following created node if any.
     * @return Node
     */
    public Node getNode() throws NodeException {
        //try first to get the Node from the createdNodes array to be continued
        Node node;
        waitForNodeCreation();

        if (!createdNodes.isEmpty()) {
            node = (Node) createdNodes.get(lastNodeIndex);
            increaseNodeIndex();

            return node;
        } else {
            throw new NodeException("Cannot get the node " + this.name);
        }
    }

    public Node getNode(int index) throws NodeException {
        Node node = (Node) createdNodes.get(index);

        if (node == null) {
            throw new NodeException(
                "Cannot return the first node, no nodes hava been created");
        }

        return node;
    }

    public String[] getNodesURL() throws NodeException {
        String[] nodeNames;

        try {
            waitForAllNodesCreation();
        } catch (NodeException e) {
            logger.error(e.getMessage());
        }

        if (!createdNodes.isEmpty()) {
            synchronized (createdNodes) {
                nodeNames = new String[createdNodes.size()];

                for (int i = 0; i < createdNodes.size(); i++) {
                    nodeNames[i] = ((Node) createdNodes.get(i)).getNodeInformation()
                                    .getURL();
                }
            }
        } else {
            if (!MAX_P2P) {
                throw new NodeException(
                    "Cannot return nodes, no nodes have been created");
            } else {
                logger.warn("WARN: No nodes have yet been created.");
                logger.warn(
                    "WARN: This behavior might be normal, since P2P service is used with MAX number of nodes requested");
                logger.warn("WARN: Returning empty array");

                return new String[0];
            }
        }

        return nodeNames;
    }

    public Node[] getNodes() throws NodeException {
        Node[] nodeTab;

        try {
            waitForAllNodesCreation();
        } catch (NodeException e) {
            logger.error(e.getMessage());
        }

        if (!createdNodes.isEmpty()) {
            synchronized (createdNodes) {
                nodeTab = new Node[createdNodes.size()];

                for (int i = 0; i < createdNodes.size(); i++) {
                    nodeTab[i] = ((Node) createdNodes.get(i));
                }
            }
        } else {
            if (!MAX_P2P) {
                throw new NodeException(
                    "Cannot return nodes, no nodes have been created");
            } else {
                logger.warn("WARN: No nodes have yet been created.");
                logger.warn(
                    "WARN: This behavior might be normal, since P2P service is used with MAX number of nodes requested");
                logger.warn("WARN: Returning empty array");

                return new Node[0];
            }
        }

        return nodeTab;
    }

    public Node getNode(String url) throws NodeException {
        Node node = null;

        try {
            waitForAllNodesCreation();
        } catch (NodeException e) {
            logger.error(e.getMessage());
        }

        if (!createdNodes.isEmpty()) {
            synchronized (createdNodes) {
                for (int i = 0; i < createdNodes.size(); i++) {
                    if (((Node) createdNodes.get(i)).getNodeInformation()
                             .getURL().equals(url)) {
                        node = (Node) createdNodes.get(i);

                        break;
                    }
                }

                return node;
            }
        } else {
            throw new NodeException(
                "Cannot return nodes, no nodes hava been created");
        }
    }

    public void killAll(boolean softly) {
        Node node;
        ProActiveRuntime part = null;

        if (isActivated) {
            // Killing p2p nodes
            if (this.p2pNodeslookupList.size() > 0) {
                for (int index = 0; index < this.p2pNodeslookupList.size();
                        index++) {
                    P2PNodeLookup currentNodesLookup = (P2PNodeLookup) this.p2pNodeslookupList.get(index);
                    currentNodesLookup.killAllNodes();
                }
            }

            // Killing other nodes
            for (int i = 0; i < createdNodes.size(); i++) {
                node = (Node) createdNodes.get(i);
                part = node.getProActiveRuntime();

                if (this.p2pNodes.contains(node)) {
                    continue;
                }

                //we have to be carefull. Indeed if the node is local, we do not
                // want to kill the runtime, otherwise the application is over
                // so if the node is local, we just unregister this node from any registry
                if (!NodeFactory.isNodeLocal(node)) {
                    try {
                        part.killRT(softly);
                    } catch (ProActiveException e1) {
                        e1.printStackTrace();
                    } catch (Exception e) {
                        logger.info(" Virtual Machine " +
                            part.getVMInformation().getVMID() + " on host " +
                            UrlBuilder.getHostNameorIP(
                                part.getVMInformation().getInetAddress()) +
                            " terminated!!!");
                    }
                } else {
                    try {
                        //					the node is local, unregister it.
                        part.killNode(node.getNodeInformation().getURL());
                    } catch (ProActiveException e) {
                        e.printStackTrace();
                    }
                }
            }

            isActivated = false;

            try {
                //if registered in any regigistry, unregister everything
                if (registration) {
                    ProActive.unregisterVirtualNode(this);
                }
                //else unregister just in the local runtime
                else {
                    proActiveRuntimeImpl.unregisterVirtualNode(this.name);
                }
            } catch (ProActiveException e) {
                e.printStackTrace();
            }

            // if not activated unregister it from the local runtime
        } else {
            proActiveRuntimeImpl.unregisterVirtualNode(this.name);
        }

        for (int i = 0; i < createdRuntimeF.size(); i++) {
            part = (ProActiveRuntime) createdRuntimeF.get(i);
            try {
                part.killRT(true);
            } catch (Exception e) {
                logger.info(" Forwarder " + part.getVMInformation().getVMID() +
                    " on host " +
                    UrlBuilder.getHostNameorIP(part.getVMInformation()
                                                   .getInetAddress()) +
                    " terminated!!!");
            }
        }
    }

    public void createNodeOnCurrentJvm(String protocol) {
        if (protocol == null) {
            protocol = System.getProperty("proactive.communication.protocol");
        }

        localVirtualMachines.add(protocol);
    }

    public Object getUniqueAO() throws ProActiveException {
        if (!property.equals("unique_singleAO")) {
            logger.warn(
                "!!!!!!!!!!WARNING. This VirtualNode is not defined with unique_single_AO property in the XML descriptor. Calling getUniqueAO() on this VirtualNode can lead to unexpected behaviour");
        }

        if (uniqueActiveObject == null) {
            try {
                Node node = getNode();

                if (node.getActiveObjects().length > 1) {
                    logger.warn(
                        "!!!!!!!!!!WARNING. More than one active object is created on this VirtualNode.");
                }

                uniqueActiveObject = node.getActiveObjects()[0];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (uniqueActiveObject == null) {
            throw new ProActiveException(
                "No active object are registered on this VirtualNode");
        }

        return uniqueActiveObject;
    }

    public boolean isActivated() {
        return isActivated;
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#isLookup()
     */
    public boolean isLookup() {
        return false;
    }

    //
    //-------------------IMPLEMENTS Job-----------------------------------
    //

    /**
     * @see org.objectweb.proactive.Job#getJobID()
     */
    public String getJobID() {
        return this.jobID;
    }

    //
    //-------------------IMPLEMENTS RuntimeRegistrationEventListener------------
    //
    public void runtimeRegistered(RuntimeRegistrationEvent event) {
        if (event.getType() == RuntimeRegistrationEvent.FORWARDER_RUNTIME_REGISTERED) {
            forwarderRuntimeRegisteredPerform(event);
        } else {
            runtimeRegisteredPerform(event);
        }
    }

    private synchronized void forwarderRuntimeRegisteredPerform(
        RuntimeRegistrationEvent event) {
        VirtualMachine virtualMachine = null;
        for (int i = 0; i < virtualMachines.size(); i++) {
            if (((VirtualMachine) virtualMachines.get(i)).getName().equals(event.getVmName())) {
                virtualMachine = (VirtualMachine) virtualMachines.get(i);
            }
        }

        //Check if it this virtualNode that originates the process
        if ((event.getCreatorID().equals(this.name)) &&
                (virtualMachine != null)) {
            if (logger.isDebugEnabled()) {
                logger.debug("forwarder " + event.getCreatorID() +
                    " registered on virtualnode " + this.name);
            }
        }
        createdRuntimeF.add(event.getRegisteredRuntime());
    }

    private synchronized void runtimeRegisteredPerform(
        RuntimeRegistrationEvent event) {
        String nodeName;
        ProActiveRuntime proActiveRuntimeRegistered;
        String nodeHost;
        String protocol;
        String url;
        int port = 0;
        VirtualMachine virtualMachine = null;

        for (int i = 0; i < virtualMachines.size(); i++) {
            if (((VirtualMachine) virtualMachines.get(i)).getName().equals(event.getVmName())) {
                virtualMachine = (VirtualMachine) virtualMachines.get(i);
            }
        }

        //Check if it this virtualNode that originates the process
        if ((event.getCreatorID().equals(this.name)) &&
                (virtualMachine != null)) {
            if (logger.isDebugEnabled()) {
                logger.debug("runtime " + event.getCreatorID() +
                    " registered on virtualnode " + this.name);
            }

            protocol = event.getProtocol();

            //gets the registered runtime
            proActiveRuntimeRegistered = event.getRegisteredRuntime();

            // get the host of nodes
            nodeHost = proActiveRuntimeRegistered.getVMInformation()
                                                 .getHostName();

            port = UrlBuilder.getPortFromUrl(proActiveRuntimeRegistered.getURL());

            try {
                //get the node on the registered runtime
                // nodeNames = proActiveRuntimeRegistered.getLocalNodeNames();
                int nodeNumber = (new Integer((String) virtualMachine.getNodeNumber())).intValue();

                for (int i = 1; i <= nodeNumber; i++) {
                    nodeName = this.name +
                        Integer.toString(new java.util.Random(
                                System.currentTimeMillis()).nextInt());
                    url = buildURL(nodeHost, nodeName, protocol, port);

                    // nodes are created from the registered runtime, since this virtualNode is
                    // waiting for runtime registration to perform co-allocation in the jvm.
                    ProActiveSecurityManager siblingPSM = null;

                    if (proactiveSecurityManager != null) {
                        siblingPSM = proactiveSecurityManager.generateSiblingCertificate(this.name);
                    }

                    proActiveRuntimeRegistered.createLocalNode(url, false,
                        siblingPSM, this.getName(), this.jobID);
                    performOperations(proActiveRuntimeRegistered, url, protocol);
                }
            } catch (ProActiveException e) {
                e.printStackTrace();
            }
        }

        //Check if the virtualNode that originates the process is among awaited VirtualNodes
        if (awaitedVirtualNodes.containsKey(event.getCreatorID())) {
            //gets the registered runtime
            proActiveRuntimeRegistered = event.getRegisteredRuntime();

            // get the host for the node to be created
            nodeHost = proActiveRuntimeRegistered.getVMInformation()
                                                 .getHostName();
            protocol = event.getProtocol();

            port = UrlBuilder.getPortFromUrl(proActiveRuntimeRegistered.getURL());

            // it is the only way to get accurate value of askedNodes
            VirtualMachine vm = (VirtualMachine) awaitedVirtualNodes.get(event.getCreatorID());
            int nodeNumber = (new Integer((String) vm.getNodeNumber())).intValue();

            for (int i = 1; i <= nodeNumber; i++) {
                try {
                    nodeName = this.name +
                        Integer.toString(new java.util.Random(
                                System.currentTimeMillis()).nextInt());
                    url = buildURL(nodeHost, nodeName, protocol, port);

                    // nodes are created from the registered runtime, since this virtualNode is
                    // waiting for runtime registration to perform co-allocation in the jvm.
                    ProActiveSecurityManager siblingPSM = null;

                    if (proactiveSecurityManager != null) {
                        siblingPSM = proactiveSecurityManager.generateSiblingCertificate(this.name);
                    }

                    proActiveRuntimeRegistered.createLocalNode(url, false,
                        siblingPSM, this.getName(), this.jobID);
                    performOperations(proActiveRuntimeRegistered, url, protocol);
                } catch (ProActiveException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#setRuntimeInformations(String,String)
     * At the moment no property can be set at runtime on a VirtualNodeImpl.
     */
    public void setRuntimeInformations(String information, String value)
        throws ProActiveException {
        //        try {
        //            checkProperty(information);
        //        } catch (ProActiveException e) {
        //            throw new ProActiveException("No property can be set at runtime on this VirtualNode",
        //                e);
        //        }
        //No need to check if the property exist since no property can be set 
        // at runtime on a VNImpl. This might change in the future.
        throw new ProActiveException(
            "No property can be set at runtime on this VirtualNode");
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.services.ServiceUser#setService(org.objectweb.proactive.core.descriptor.services.UniversalService)
     */
    public void setService(UniversalService service) throws ProActiveException {
        if (FaultToleranceService.FT_SERVICE_NAME.equals(
                    service.getServiceName())) {
            this.ftService = (FaultToleranceService) service;
        } else {
            throw new ProActiveException(
                " Unable to bind the given service to a virtual node");
        }
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.services.ServiceUser#getUserClass()
     */
    public String getUserClass() {
        return this.getClass().getName();
    }

    public void setRegistrationProtocol(String protocol) {
        setRegistrationValue(true);
        this.registrationProtocol = protocol;
    }

    public String getRegistrationProtocol() {
        return this.registrationProtocol;
    }

    /**
     * Sets the minimal number of nodes this VirtualNode needs to be suitable for the application.
     * It means that if in the Deployment file, this VirtualNode is mapped onto n nodes, and the
     * minimum number of nodes is set to m, with of course m &lt; n, calling method getNodes will return
     * when at least m nodes are created
     * @param min the minimum number of nodes
     */
    public void setMinNumberOfNodes(int min) {
        this.minNumberOfNodes = min;
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#getMinNumberOfNodes()
     */
    public int getMinNumberOfNodes() {
        return minNumberOfNodes;
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.data.VirtualNode#isMultiple()
     */
    public boolean isMultiple() {
        return ((virtualMachines.size() + localVirtualMachines.size()) > 1);
    }

    //
    //-------------------PRIVATE METHODS--------------------------------------
    //
    private void internalCreateNodeOnCurrentJvm(String protocol) {
        try {
            // this method should be called when in the xml document the tag currenJVM is encountered. It means that one node must be created
            // on the jvm that originates the creation of this virtualNode(the current jvm) and mapped on this virtualNode
            // we must increase the node count
            String url;
            increaseNumberOfNodes(1);

            String nodeName = this.name +
                Integer.toString(new java.util.Random(
                        System.currentTimeMillis()).nextInt());

            // get the Runtime for the given protocol
            ProActiveRuntime defaultRuntime = RuntimeFactory.getProtocolSpecificRuntime(checkProtocol(
                        protocol));

            //create the node
            ProActiveSecurityManager siblingPSM = null;

            if (proactiveSecurityManager != null) {
                siblingPSM = proactiveSecurityManager.generateSiblingCertificate(this.name);
            }

            url = defaultRuntime.createLocalNode(nodeName, false, siblingPSM,
                    this.getName(), ProActive.getJobId());

            //add this node to this virtualNode
            performOperations(defaultRuntime, url, protocol);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Waits until at least one Node mapped to this VirtualNode in the XML Descriptor is created
     */
    private synchronized void waitForNodeCreation() throws NodeException {
        while (!nodeCreated) {
            if (!timeoutExpired()) {
                try {
                    wait(getTimeToSleep());
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                } catch (IllegalStateException e) {
                    // it may happen that we entered in the loop and just after
                    // the timeToSleep is < 0. It means that the timeout expired
                    // that is why we catch the runtime exception
                    throw new NodeException(
                        "After many retries, not even one node can be found");
                }
            } else {
                throw new NodeException(
                    "After many retries, not even one node can be found");
            }
        }

        return;
    }

    /**
     * Waits until all Nodes mapped to this VirtualNode in the XML Descriptor are created
     */
    private void waitForAllNodesCreation() throws NodeException {
        int tempNodeCount = nbMappedNodes;

        if (tempNodeCount != P2PConstants.MAX_NODE) {
            //nodeCount equal 0 means there is only a P2P service with MAX number of nodes requested
            // so if different of 0, we can set to false the boolean
            MAX_P2P = false;
        }

        if (waitForTimeout) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
        } else {
            //behavior has to be moved in a synchronized method to avoid useless lock
            // when sleeping
            internalWait(tempNodeCount);
        }

        return;
    }

    private synchronized void internalWait(int tempNodeCount)
        throws NodeException {
        // check if we can release the vn before all nodes expected, are created
        // i.e the minNumber of nodes is set
        if (minNumberOfNodes != 0) {
            tempNodeCount = minNumberOfNodes;
        }

        while (nbCreatedNodes < tempNodeCount) {
            if (!timeoutExpired()) {
                try {
                    wait(getTimeToSleep());
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                } catch (IllegalStateException e) {
                    // it may happen that we entered in the loop and just after
                    // the timeToSleep is < 0. It means that the timeout expired
                    // that is why we catch the runtime exception
                    throw new NodeException("After many retries, only " +
                        nbCreatedNodes + " nodes are created on " +
                        tempNodeCount + " expected ");
                }
            } else {
                throw new NodeException("After many retries, only " +
                    nbCreatedNodes + " nodes are created on " + tempNodeCount +
                    " expected");
            }
        }
    }

    private boolean timeoutExpired() {
        long currentTime = System.currentTimeMillis();

        return (globalTimeOut < currentTime);
    }

    private long getTimeToSleep() {
        // if timeToSleep is < 0 we throw an exception
        long timeToSleep = globalTimeOut - System.currentTimeMillis();

        if (timeToSleep > 0) {
            return timeToSleep;
        } else {
            throw new IllegalStateException("Timeout expired");
        }
    }

    /**
     * Returns the process mapped to the given virtual machine mapped to this virtual node
     * @param VirtualMachine
     * @return ExternalProcess
     */
    private ExternalProcess getProcess(VirtualMachine vm,
        boolean vmAlreadyAssigned) {
        ExternalProcess copyProcess;

        //VirtualMachine vm = getVirtualMachine();
        ExternalProcess process = vm.getProcess();

        // we need to do a deep copy of the process otherwise,
        //modifications will be applied on one object that might 
        // be referenced by other virtualNodes .i.e check started
        if (!vmAlreadyAssigned) {
            copyProcess = (ExternalProcess) makeDeepCopy(process);
            vm.setProcess(copyProcess);

            return copyProcess;
        } else {
            //increment the node count by askedNodes
            increaseNumberOfNodes(process.getNodeNumber() * new Integer(
                    vm.getNodeNumber()).intValue());

            return process;
        }
    }

    /**
     * Sets parameters to the JVMProcess linked to the ExternalProcess
     * @param process
     */
    private void setParameters(ExternalProcess process, VirtualMachine vm) {
        JVMProcess jvmProcess;

        //jobID = ProActive.getJobId();
        String protocolId = "";
        int nodeNumber = new Integer(vm.getNodeNumber()).intValue();

        if (logger.isDebugEnabled()) {
            logger.debug("askedNodes " + nodeNumber);
        }

        protocolId = process.getProcessId();

        int cnt = process.getNodeNumber();
        if (cnt == UniversalProcess.UNKNOWN_NODE_NUMBER) {
            waitForTimeout = true;
        } else {
            increaseNumberOfNodes(cnt * nodeNumber);
        }

        //When the virtualNode will be activated, it has to launch the process
        //with such parameter.See StartRuntime
        jvmProcess = (JVMProcess) process.getFinalProcess();

        //if the target class is StartRuntime, then give parameters otherwise keep parameters
        if (jvmProcess.getClassname().equals("org.objectweb.proactive.core.runtime.StartRuntime")) {
            String vnName = this.name;

            String localruntimeURL = null;

            try {
                localruntimeURL = RuntimeFactory.getDefaultRuntime().getURL();

                if (process.getUsername() != null) {
                    localruntimeURL = System.getProperty("user.name") + "@" +
                        localruntimeURL;
                }
            } catch (ProActiveException e) {
                e.printStackTrace();
            }

            if (logger.isDebugEnabled()) {
                logger.debug(localruntimeURL);
            }

            // if it is a main node we set the property for retrieving the pad
            if (mainVirtualNode || process.isHierarchical()) {
                jvmProcess.setJvmOptions(" -Dproactive.pad=" + padURL);
            }

            jvmProcess.setJvmOptions("-Dproactive.jobid=" + jobID);
            jvmProcess.setParameters(vnName + " " + localruntimeURL + " " +
                nodeNumber + " " + protocolId + " " + vm.getName());

            // FAULT TOLERANCE settings
            if (this.ftService != null) {
                jvmProcess.setJvmOptions(this.ftService.buildParamsLine());
            }
        }

        /* Setting the file transfer definitions to be copied */
        FileTransferWorkShop ftsDeploy = process.getFileTransferWorkShopDeploy();
        FileTransferWorkShop ftsRetrieve = process.getFileTransferWorkShopRetrieve();

        if ((ftsDeploy != null) && ftsDeploy.isImplicit()) {
            for (int i = 0; i < fileTransferDeploy.size(); i++)
                ftsDeploy.addFileTransfer((FileTransfer) fileTransferDeploy.get(
                        i));
        }

        if ((ftsRetrieve != null) && ftsRetrieve.isImplicit()) {
            for (int i = 0; i < fileTransferRetrieve.size(); i++)
                ftsRetrieve.addFileTransfer((FileTransfer) fileTransferRetrieve.get(
                        i));
        }
    }

    /**
     * Returns a deepcopy of the process
     * @param process the process to copy
     * @return ExternalProcess, the copy version of the process
     */
    private Object makeDeepCopy(Object process) {
        //deepCopyTag = true;
        Object result = null;

        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
            oos.writeObject(process);
            oos.flush();
            oos.close();

            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
            result = ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //deepCopyTag = false;
        return result;
    }

    private String buildURL(String host, String name, String protocol, int port) {
        if (port != 0) {
            return UrlBuilder.buildUrl(host, name, protocol, port);
        } else {
            return UrlBuilder.buildUrl(host, name, protocol);
        }
    }

    private void increaseIndex() {
        if (virtualMachines.size() > 1) {
            lastVirtualMachineIndex = (lastVirtualMachineIndex + 1) % virtualMachines.size();
        }
    }

    private void increaseNumberOfNodes(int n) {
        nbMappedNodes = nbMappedNodes + n;

        if (logger.isDebugEnabled()) {
            logger.debug("Number of nodes = " + nbMappedNodes);
        }
    }

    private void increaseNodeIndex() {
        if (createdNodes.size() > 1) {
            lastNodeIndex = (lastNodeIndex + 1) % createdNodes.size();
        }
    }

    private String checkProtocol(String protocol) {
        if (protocol.indexOf(":") == -1) {
            return protocol.concat(":");
        }

        return protocol;
    }

    private synchronized void performOperations(ProActiveRuntime part,
        String url, String protocol) {
        Node node = new NodeImpl(part, url, checkProtocol(protocol), this.jobID);
        createdNodes.add(node);
        logger.info("**** Mapping VirtualNode " + this.name + " with Node: " +
            url + " done");
        nodeCreated = true;
        nbCreatedNodes++;

        // wakes up Thread that are waiting for the node creation 
        notifyAll();

        //notify all listeners that a node has been created
        notifyListeners(this, NodeCreationEvent.NODE_CREATED, node,
            nbCreatedNodes);
    }

    private void register() {
        try {
            waitForAllNodesCreation();

            //  	ProActiveRuntime part = RuntimeFactory.getProtocolSpecificRuntime(registrationProtocol);
            //  	part.registerVirtualnode(this.name,false);
            ProActive.registerVirtualNode(this, registrationProtocol, false);
        } catch (NodeException e) {
            logger.error(e.getMessage());
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }

    private void setRegistrationValue(boolean value) {
        this.registration = value;
    }

    private void startService(VirtualMachine vm) {
        UniversalService service = vm.getService();

        //we need to perform a deep copy. Indeed if several vm reference
        // the same service this might lead to unexpected behaviour
        UniversalService copyService = (UniversalService) makeDeepCopy(service);
        vm.setService(copyService);

        if (service.getServiceName().equals(P2PConstants.P2P_NODE_NAME)) {
            int nodeRequested = ((P2PDescriptorService) service).getNodeNumber();

            // if it is a P2Pservice we must increase the node count with the number
            // of nodes requested
            if (nodeRequested != ((P2PDescriptorService) service).getMAX()) {
                increaseNumberOfNodes(nodeRequested);

                //nodeRequested = MAX means that the service will try to get every nodes 
                // it can. So we can't predict how many nodes will return.
            } else {
                MAX_P2P = true;
            }
        } else {
            //increase with 1 node
            increaseNumberOfNodes(1);
        }

        new ServiceThread(this, vm).start();
    }

    private void writeObject(java.io.ObjectOutputStream out)
        throws java.io.IOException {
        if (isActivated) {
            try {
                waitForAllNodesCreation();
            } catch (NodeException e) {
                out.defaultWriteObject();

                return;
            }
        }

        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.proActiveRuntimeImpl = (ProActiveRuntimeImpl) ProActiveRuntimeImpl.getProActiveRuntime();
    }

    // -------------------------------------------------------------------------
    // For P2P nodes acquisition
    // -------------------------------------------------------------------------

    /**
     * Use for p2p infrastructure to get nodes.
     * @see org.objectweb.proactive.core.event.NodeCreationEventListener#nodeCreated(org.objectweb.proactive.core.event.NodeCreationEvent)
     */
    public synchronized void nodeCreated(NodeCreationEvent event) {
        Node newNode = event.getNode();
        this.createdNodes.add(newNode);
        this.p2pNodes.add(newNode);
        nbCreatedNodes++;
        nodeCreated = true;

        //notify all listeners that a node has been created
        notifyAll();
        notifyListeners(this, NodeCreationEvent.NODE_CREATED, newNode,
            nbCreatedNodes);
    }

    /**
     * @param nodesLookup
     */
    public void addP2PNodesLookup(P2PNodeLookup nodesLookup) {
        this.p2pNodeslookupList.add(nodesLookup);
        P2P_LOGGER.debug("A P2P nodes lookup added to the vn: " + this.name);
    }
}
