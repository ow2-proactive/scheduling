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
package org.objectweb.proactive.core.runtime;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.log4j.MDC;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.ActiveBody;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.component.gen.MetaObjectInterfaceClassGenerator;
import org.objectweb.proactive.core.component.gen.RepresentativeInterfaceClassGenerator;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.descriptor.util.RefactorPAD;
import org.objectweb.proactive.core.event.RuntimeRegistrationEvent;
import org.objectweb.proactive.core.event.RuntimeRegistrationEventProducerImpl;
import org.objectweb.proactive.core.mop.ASMBytecodeStubBuilder;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.mop.JavassistByteCodeStubBuilder;
import org.objectweb.proactive.core.mop.MOPClassLoader;
import org.objectweb.proactive.core.mop.Utils;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.rmi.FileProcess;
import org.objectweb.proactive.core.util.ClassDataCache;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.ext.security.Communication;
import org.objectweb.proactive.ext.security.PolicyServer;
import org.objectweb.proactive.ext.security.ProActiveSecurity;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.SecurityEntity;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.domain.SecurityDomain;
import org.objectweb.proactive.ext.security.exceptions.InvalidPolicyFile;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.ext.security.securityentity.Entity;
import org.objectweb.proactive.ext.security.securityentity.EntityCertificate;
import org.objectweb.proactive.ext.security.securityentity.EntityVirtualNode;


/**
 * <p>
 * Implementation of  ProActiveRuntime
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.91
 *
 */
public class ProActiveRuntimeImpl extends RuntimeRegistrationEventProducerImpl
    implements ProActiveRuntime, LocalProActiveRuntime {
    //
    // -- STATIC MEMBERS -----------------------------------------------------------
    //
    //the Unique instance of ProActiveRuntime
    private static ProActiveRuntime proActiveRuntime;

    static {
        
        if (ProActiveConfiguration.isForwarder()) {
            proActiveRuntime = new ProActiveRuntimeForwarderImpl();
        } else {
            proActiveRuntime = new ProActiveRuntimeImpl();
        }
    }

    private static SecureRandom prng = null; // new Random();

    // runtime security manager 
    private static ProActiveSecurityManager runtimeSecurityManager;

    // map of local nodes, key is node name
    private java.util.Hashtable nodeMap;
    private String defaultNodeVirtualNode = null;

    //
    // -- PRIVATE MEMBERS -----------------------------------------------------------
    //
    private VMInformation vmInformation;

    //map VirtualNodes and their names
    private java.util.Hashtable virtualNodesMap;

    //map descriptor and their url
    private java.util.Hashtable descriptorMap;

    // map proActiveRuntime registered on this VM and their names
    private java.util.Hashtable proActiveRuntimeMap;
    private java.util.Hashtable proActiveRuntimeForwarderMap;

    // synchronized set of URL to runtimes in which we are registered
    private java.util.Set runtimeAcquaintancesURL;
    private ProActiveRuntime parentRuntime;

    //
    // -- CONSTRUCTORS -----------------------------------------------------------
    //
    // singleton
    protected ProActiveRuntimeImpl() {
        try {
            this.vmInformation = new VMInformationImpl();
            this.proActiveRuntimeMap = new java.util.Hashtable();
            this.proActiveRuntimeForwarderMap = new java.util.Hashtable();
            this.runtimeAcquaintancesURL = java.util.Collections.synchronizedSortedSet(new java.util.TreeSet());
            this.virtualNodesMap = new java.util.Hashtable();
            this.descriptorMap = new java.util.Hashtable();
            this.nodeMap = new java.util.Hashtable();

            try {
                String file = System.getProperties()
                                    .getProperty("proactive.runtime.security");
                ProActiveSecurity.loadProvider();

                if ((file != null) && new File(file).exists()) {
                    // loading security from a file
                    ProActiveRuntimeImpl.runtimeSecurityManager = new ProActiveSecurityManager(file);
                    logger.info(
                        "ProActive Security Policy (proactive.runtime.security) using " +
                        file);

                    // Is the runtime included within a Domain ?
                    String domainURL = System.getProperties()
                                             .getProperty("proactive.runtime.domain.url");

                    if (domainURL != null) {
                        SecurityEntity domain = (SecurityDomain) ProActive.lookupActive("org.objectweb.proactive.ext.security.domain.SecurityDomain",
                                domainURL);
                        ProActiveRuntimeImpl.runtimeSecurityManager.setParent(domain);
                    }
                } else {
                    logger.info(
                        "ProActive Security Policy (proactive.runtime.security) not set. Runtime Security disabled ");
                }
            } catch (InvalidPolicyFile e) {
                e.printStackTrace();
            } catch (ActiveObjectCreationException e) {
                e.printStackTrace();
            }

            //System.out.println(vmInformation.getVMID().toString());
        } catch (java.net.UnknownHostException e) {
            //System.out.println();
            logger.fatal(" !!! Cannot do a reverse lookup on that host");

            // System.out.println();
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // logging info
        MDC.remove("runtime");
        MDC.put("runtime", getURL());
    }

    public static synchronized int getNextInt() {
        if (prng == null) {
            prng = new SecureRandom();
        }

        return prng.nextInt();
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------------------
    //
    public static ProActiveRuntime getProActiveRuntime() {
        return proActiveRuntime;
    }

    //
    // -- Implements LocalProActiveRuntime  -----------------------------------------------
    //

    /**
     * @see org.objectweb.proactive.core.runtime.LocalProActiveRuntime#registerLocalVirtualNode(VirtualNode vn, String vnName)
     */
    public void registerLocalVirtualNode(VirtualNode vn, String vnName) {
        //System.out.println("vn "+vnName+" registered");
        virtualNodesMap.put(vnName, vn);
    }

    /**
     * @see org.objectweb.proactive.core.runtime.LocalProActiveRuntime#setParent(ProActiveRuntime parentPARuntime)
     */
    public void setParent(ProActiveRuntime parentPARuntime) {
        if (parentRuntime == null) {
            parentRuntime = parentPARuntime;
            runtimeAcquaintancesURL.add(parentPARuntime.getURL());
        } else {
            runtimeLogger.error("Parent runtime already set!");
        }
    }

    public void registerDescriptor(String url, ProActiveDescriptor pad) {
        descriptorMap.put(url, pad);
    }

    public ProActiveDescriptor getDescriptor(String url,
        boolean isHierarchicalSearch) throws IOException, ProActiveException {
        ProActiveDescriptor pad = (ProActiveDescriptor) descriptorMap.get(url);

        // hierarchical search or not, look if we know the pad
        if (pad != null) {
            // if pad found and hierarchy search return pad with no main
            if (isHierarchicalSearch) {
                return RefactorPAD.buildNoMainPAD(pad);
            } else {
                // if not hierarchy search, return the normal pad
                return pad;
            }
        } else if (!isHierarchicalSearch) {
            return null; // pad == null
        } else { // else search pad in parent runtime
            if (parentRuntime == null) {
                throw new IOException(
                    "Descriptor cannot be found hierarchically since this runtime has no parent");
            }

            return parentRuntime.getDescriptor(url, true);
        }
    }

    public void removeDescriptor(String url) {
        descriptorMap.remove(url);
    }

    //
    // -- Implements ProActiveRuntime  -----------------------------------------------
    //

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#createLocalNode(String, boolean, ProActiveSecurityManager, String, String)
     */
    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding,
        ProActiveSecurityManager nodeSecurityManager, String vnName,
        String jobId) throws NodeException {
        if (!replacePreviousBinding && (nodeMap.get(nodeName) != null)) {
            throw new NodeException("Node " + nodeName +
                " already created on this ProActiveRuntime. To overwrite this node, use true for replacePreviousBinding");
        }

        if (nodeSecurityManager != null) {
            // setting the current runtime as parent entity of the node 
            nodeSecurityManager.setParent(this);
        }

        LocalNode newNode = new LocalNode(nodeName, jobId, nodeSecurityManager,
                vnName);

        if (replacePreviousBinding && (nodeMap.get(nodeName) != null)) {
            newNode.setActiveObjects(((LocalNode) nodeMap.get(nodeName)).getActiveObjectsId());
            nodeMap.remove(nodeName);
        }

        nodeMap.put(nodeName, newNode);

        return nodeName;
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#killAllNodes()
     */
    public void killAllNodes() {
        for (Enumeration e = nodeMap.keys(); e.hasMoreElements();) {
            String nodeName = (String) e.nextElement();
            killNode(nodeName);
        }
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#killNode(String)
     */
    public void killNode(String nodeName) {
        LocalNode localNode = (LocalNode) nodeMap.get(nodeName);
        localNode.terminate();
        nodeMap.remove(nodeName);
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#createVM(UniversalProcess)
     */
    public void createVM(UniversalProcess remoteProcess)
        throws java.io.IOException {
        remoteProcess.startProcess();
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getLocalNodeNames()
     */
    public String[] getLocalNodeNames() {
        int i = 0;
        String[] nodeNames;

        synchronized (nodeMap) {
            nodeNames = new String[nodeMap.size()];

            for (java.util.Enumeration e = nodeMap.keys(); e.hasMoreElements();) {
                nodeNames[i] = (String) e.nextElement();
                i++;
            }
        }

        return nodeNames;
    }

    /**
     *@see org.objectweb.proactive.core.runtime.ProActiveRuntime#getVMInformation()
     */
    public VMInformation getVMInformation() {
        return vmInformation;
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#register(ProActiveRuntime, String, String, String, String)
     */
    public void register(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeName, String creatorID, String creationProtocol,
        String vmName) {
        //System.out.println("register in Impl");
        //System.out.println("thread"+Thread.currentThread().getName());
        //System.out.println(vmInformation.getVMID().toString());
        proActiveRuntimeMap.put(proActiveRuntimeName, proActiveRuntimeDist);
        notifyListeners(this, RuntimeRegistrationEvent.RUNTIME_REGISTERED,
            proActiveRuntimeDist, creatorID, creationProtocol, vmName);
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#unregister(org.objectweb.proactive.core.runtime.ProActiveRuntime, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void unregister(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeUrl, String creatorID, String creationProtocol,
        String vmName) {
        this.proActiveRuntimeMap.remove(proActiveRuntimeUrl);
        notifyListeners(this, RuntimeRegistrationEvent.RUNTIME_UNREGISTERED,
            proActiveRuntimeDist, creatorID, creationProtocol, vmName);
    }

    /**
     *@see org.objectweb.proactive.core.runtime.ProActiveRuntime#getProActiveRuntimes()
     */
    public ProActiveRuntime[] getProActiveRuntimes() {
        int i = 0;
        ProActiveRuntime[] runtimeArray;

        synchronized (proActiveRuntimeMap) {
            runtimeArray = new ProActiveRuntime[proActiveRuntimeMap.size()];

            for (java.util.Enumeration e = proActiveRuntimeMap.elements();
                    e.hasMoreElements();) {
                runtimeArray[i] = (ProActiveRuntime) e.nextElement();
                i++;
            }
        }

        return runtimeArray;
    }

    /**
     *@see org.objectweb.proactive.core.runtime.ProActiveRuntime#getProActiveRuntime(String)
     */
    public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName) {
        return (ProActiveRuntime) proActiveRuntimeMap.get(proActiveRuntimeName);
    }

    /**
     *@see org.objectweb.proactive.core.runtime.ProActiveRuntime#addAcquaintance(String)
     */
    public void addAcquaintance(String proActiveRuntimeName) {
        runtimeAcquaintancesURL.add(proActiveRuntimeName);
    }

    /**
     *@see org.objectweb.proactive.core.runtime.ProActiveRuntime#getAcquaintances()
     */
    public String[] getAcquaintances() {
        String[] urls;

        synchronized (runtimeAcquaintancesURL) {
            urls = new String[runtimeAcquaintancesURL.size()];

            java.util.Iterator iter = runtimeAcquaintancesURL.iterator();

            for (int i = 0; i < urls.length; i++)
                urls[i] = (String) iter.next();
        }

        return urls;
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#rmAcquaintance(java.lang.String)
     */
    public void rmAcquaintance(String proActiveRuntimeName) {
        runtimeAcquaintancesURL.remove(proActiveRuntimeName);
    }

    /**
     *@see org.objectweb.proactive.core.runtime.ProActiveRuntime#killRT(boolean)
     */
    public void killRT(boolean softly) {
        killAllNodes();
        System.exit(0);
    }

    /**
     *@see org.objectweb.proactive.core.runtime.ProActiveRuntime#getURL()
     */
    public String getURL() {
        return "//" +
        UrlBuilder.getHostNameorIP(vmInformation.getInetAddress()) + "/" +
        vmInformation.getName();
    }

    public ArrayList getActiveObjects(String nodeName) {
        //the array to return
        ArrayList localBodies = new ArrayList();
        LocalBodyStore localBodystore = LocalBodyStore.getInstance();
        ArrayList bodyList = ((LocalNode) nodeMap.get(nodeName)).getActiveObjectsId();

        if (bodyList == null) {
            // Probably the node is killed
            return localBodies;
        }

        synchronized (bodyList) {
            for (int i = 0; i < bodyList.size(); i++) {
                UniqueID bodyID = (UniqueID) bodyList.get(i);

                //check if the body is still on this vm
                Body body = localBodystore.getLocalBody(bodyID);

                if (body == null) {
                    //runtimeLogger.warn("body null");
                    // the body with the given ID is not any more on this ProActiveRuntime
                    // unregister it from this ProActiveRuntime
                    unregisterBody(nodeName, bodyID);
                } else {
                    //the body is on this runtime then return adapter and class name of the reified
                    //object to enable the construction of stub-proxy couple.
                    ArrayList bodyAndObjectClass = new ArrayList(2);

                    //adapter
                    bodyAndObjectClass.add(0, body.getRemoteAdapter());

                    //className
                    bodyAndObjectClass.add(1,
                        body.getReifiedObject().getClass().getName());
                    localBodies.add(bodyAndObjectClass);
                }
            }

            return localBodies;
        }
    }

    public VirtualNode getVirtualNode(String virtualNodeName) {
        //  	System.out.println("i am in get vn ");
        return (VirtualNode) virtualNodesMap.get(virtualNodeName);
    }

    public void registerVirtualNode(String virtualNodeName,
        boolean replacePreviousBinding) {
        //This method has no effect here since the virtualNode has been registered in some registry
        //like RMI or JINI
    }

    public void unregisterVirtualNode(String virtualNodeName) {
        removeRuntimeRegistrationEventListener((VirtualNodeImpl) virtualNodesMap.get(
                virtualNodeName));
        virtualNodesMap.remove(virtualNodeName);
    }

    public void unregisterAllVirtualNodes() {
        virtualNodesMap.clear();
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getJobID(java.lang.String)
     */
    public String getJobID(String nodeUrl) {
        String name = UrlBuilder.getNameFromUrl(nodeUrl);

        LocalNode localNode = (LocalNode) nodeMap.get(name);
        return localNode.getJobId();
    }

    public ArrayList getActiveObjects(String nodeName, String className) {
        //the array to return
        ArrayList localBodies = new ArrayList();
        LocalBodyStore localBodystore = LocalBodyStore.getInstance();
        ArrayList bodyList = ((LocalNode) nodeMap.get(nodeName)).getActiveObjectsId();

        if (bodyList == null) {
            // Probably the node is killed
            return localBodies;
        }

        synchronized (bodyList) {
            for (int i = 0; i < bodyList.size(); i++) {
                UniqueID bodyID = (UniqueID) bodyList.get(i);

                //check if the body is still on this vm
                Body body = localBodystore.getLocalBody(bodyID);

                if (body == null) {
                    //runtimeLogger.warn("body null");
                    // the body with the given ID is not any more on this ProActiveRuntime
                    // unregister it from this ProActiveRuntime
                    unregisterBody(nodeName, bodyID);
                } else {
                    String objectClass = body.getReifiedObject().getClass()
                                             .getName();

                    // if the reified object is of the specified type
                    // return the body adapter 
                    if (objectClass.equals((String) className)) {
                        localBodies.add(body.getRemoteAdapter());
                    }
                }
            }

            return localBodies;
        }
    }

    /**
     *@see org.objectweb.proactive.core.runtime.ProActiveRuntime#createBody(String, ConstructorCall, boolean)
     */
    public UniversalBody createBody(String nodeName,
        ConstructorCall bodyConstructorCall, boolean isLocal)
        throws ConstructorCallExecutionFailedException,
            java.lang.reflect.InvocationTargetException {
        Body localBody = (Body) bodyConstructorCall.execute();

        // SECURITY
        try {
            ProActiveSecurityManager objectSecurityManager = ((AbstractBody) localBody).getProActiveSecurityManager();

            if (objectSecurityManager != null) {
                ProActiveSecurityManager nodeSecurityManager = ((LocalNode) this.nodeMap.get(nodeName)).getSecurityManager();
                objectSecurityManager.setParent(nodeSecurityManager);
            }
        } catch (SecurityNotAvailableException e) {
            // well nothing to do
        } catch (IOException e) {
            // should never happen normally
            e.printStackTrace();
        }

        ProActiveLogger.getLogger(Loggers.RUNTIME).debug("nodeName " +
            nodeName);
        registerBody(nodeName, localBody);

        if (isLocal) {
            // if the body and proxy are on the same vm, returns the local view
            //System.out.println("body and proxy on the same vm");
            //System.out.println(localBody.getReifiedObject().getClass().getName());
            //register the body in the nodemap
            return (UniversalBody) localBody;
        } else {
            //otherwise return the adapter
            //System.out.println ("RemoteProActiveImpl.createBody "+vmInformation.getInetAddress().getHostName() +" -> new "+bodyConstructorCall.getTargetClassName()+" on node "+nodeName);
            //System.out.println ("RemoteProActiveRuntimeImpl.localBody created localBody="+localBody+" on node "+nodeName);
            return localBody.getRemoteAdapter();
        }
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#receiveBody(String, Body)
     */
    public UniversalBody receiveBody(String nodeName, Body body) {
        try {
            ((AbstractBody) body).getProActiveSecurityManager()
             .setParent(((LocalNode) this.nodeMap.get(nodeName)).getSecurityManager());
        } catch (SecurityNotAvailableException e) {
            // an exception here means that the body and its associated application
            // have not been started with a security policy
        } catch (IOException e) {
            e.printStackTrace();
        }

        registerBody(nodeName, body);

        return body.getRemoteAdapter();
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#receiveCheckpoint(String, Checkpoint, int)
     */
    public UniversalBody receiveCheckpoint(String nodeURL, Checkpoint ckpt,
        int inc) {
        runtimeLogger.debug("Receive a checkpoint for recovery");

        // the recovered body
        Body ret = ckpt.recover();

        // update node url
        ret.updateNodeURL(nodeURL);

        String nodeName = UrlBuilder.getNameFromUrl(nodeURL);

        // need to register as thread of the corresponding active object: this thread
        // may send looged requests or logged replies
        LocalBodyStore.getInstance().setCurrentThreadBody(ret);
        ((AbstractBody) ret).getFTManager()
         .beforeRestartAfterRecovery(ckpt.getCheckpointInfo(), inc);
        LocalBodyStore.getInstance().setCurrentThreadBody(null);

        // register the body
        this.registerBody(nodeName, ret);

        // restart actvity
        if (runtimeLogger.isDebugEnabled()) {
            runtimeLogger.debug(ret.getID() + " is restarting activity...");
        }

        ((ActiveBody) ret).startBody();

        // no more need to return the recovered body
        return null;
    }

    /**
     * Registers the specified body in the node with the nodeName key.
     * In fact it is the <code>UniqueID</code> of the body that is attached to the node.
     * @param nodeName. The name where to attached the body in the <code>nodeMap</code>
     * @param body. The body to register
     */
    private void registerBody(String nodeName, Body body) {
        UniqueID bodyID = body.getID();
        ArrayList bodyList = ((LocalNode) nodeMap.get(nodeName)).getActiveObjectsId();

        synchronized (bodyList) {
            if (!bodyList.contains(bodyID)) {
                //System.out.println("in registerbody id = "+ bodyID.toString());
                bodyList.add(bodyID);
            }
        }
    }

    /**
     * Unregisters the specified <code>UniqueID</code> from the node corresponding to the
     * nodeName key
     * @param nodeName. The key where to remove the <code>UniqueID</code>
     * @param bodyID. The <code>UniqueID</code> to remove
     */
    private void unregisterBody(String nodeName, UniqueID bodyID) {
        //System.out.println("in remove id= "+ bodyID.toString());
        //System.out.println("array size "+((ArrayList)nodeMap.get(nodeName)).size());
        ArrayList bodyList = ((LocalNode) nodeMap.get(nodeName)).getActiveObjectsId();

        synchronized (bodyList) {
            bodyList.remove(bodyID);

            //System.out.println("array size "+((ArrayList)nodeMap.get(nodeName)).size());
        }
    }

    //  SECURITY

    /**
     * set the runtime security manager
     */
    public static void setProActiveSecurityManager(
        ProActiveSecurityManager server) {
        if (runtimeSecurityManager != null) {
            return;
        }

        runtimeSecurityManager = server;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#setDefaultNodeVirtualNodeName(java.lang.String)
     */
    public void setDefaultNodeVirtualNodeName(String s) {
        ProActiveLogger.getLogger(Loggers.SECURITY)
                       .debug(" setting current node as currentJVM tag " + s);
        defaultNodeVirtualNode = s;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getEntities(java.lang.String)
     */
    public ArrayList getEntities(String nodeName) {
        ProActiveSecurityManager nodeSecurityManager = null;
        Entity nodeEntity = null;
        String nodeVirtualName = ((LocalNode) nodeMap.get(nodeName)).getVirtualNodeName();
        nodeSecurityManager = ((LocalNode) nodeMap.get(nodeName)).getSecurityManager();

        if (nodeSecurityManager != null) {
            nodeEntity = new EntityVirtualNode(nodeVirtualName,
                    nodeSecurityManager.getPolicyServer()
                                       .getApplicationCertificate(),
                    nodeSecurityManager.getCertificate());
        }

        ArrayList entities = null;

        //entities = getEntities();
        if (entities == null) {
            entities = new ArrayList();
        }

        entities.add(nodeEntity);

        return entities;
    }

    /**
     * the runtime looks for a matching security entity whithin its nodes and active objects
     * @param securityEntity the security entity looked for.
     * @return matching entities
     */
    public ArrayList getEntities(SecurityEntity securityEntity) {
        if (true) {
            throw new RuntimeException();
        }

        return null;

        //nodeMap.
        //    	try {
        //        System.out.println(" testing for securityentityID " + securityEntity);
        //        for (Enumeration e = nodeMap.keys(); e.hasMoreElements();) {
        //            String node = (String) e.nextElement();
        //
        //            System.out.println("testing for node " + node);
        //            ArrayList listAO = (ArrayList) nodeMap.get(node);
        //
        //            for (int i = 0; i < listAO.size(); i++) {
        //                UniqueID localBodyID = (UniqueID) listAO.get(i);
        //                System.out.println(" testing against localBbodyID " +
        //                    localBodyID);
        //                
        //					if (securityEntity.getCertificate().equals(localBodyID)) {
        //					    ArrayList a = new ArrayList();
        //
        //					    ProActiveSecurityManager nodeSecurityManager = (ProActiveSecurityManager) nodeSecurityManagerMap.get(node);
        //					    PolicyServer nodePolicyServer = nodeSecurityManager.getPolicyServer();
        //
        //					    if (nodePolicyServer != null) {
        //					        EntityVirtualNode entityVirtualNode = new EntityVirtualNode(nodeSecurityManager.getVNName(),
        //					                nodePolicyServer.getApplicationCertificate(),
        //					                nodeSecurityManager.getCertificate());
        //					        a.add(entityVirtualNode);
        //					        return a;
        //					    }
        //					}
        //				
        //            }
        //        }
        //    	} catch (SecurityNotAvailableException e1) {
        //			e1.printStackTrace();
        //		} catch (IOException e1) {
        //			e1.printStackTrace();
        //		}
        //        return new ArrayList();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getEntities()
     */
    public ArrayList getEntities() {
        PolicyServer policyServer = null;

        if ((runtimeSecurityManager != null) &&
                ((policyServer = runtimeSecurityManager.getPolicyServer()) != null)) {
            Entity e = new EntityCertificate(policyServer.getApplicationCertificate(),
                    runtimeSecurityManager.getCertificate());
            ArrayList array = new ArrayList();
            array.add(e);

            return array;
        }

        return null;
    }

    /**
     * @param sc
     */
    public SecurityContext getPolicy(SecurityContext sc)
        throws SecurityNotAvailableException {
        if (runtimeSecurityManager == null) {
            return sc;
        }

        PolicyServer policyServer = runtimeSecurityManager.getPolicyServer();

        return policyServer.getPolicy(sc);
    }

    /**
     * @see org.objectweb.proactive.Job#getJobID()
     */
    public String getJobID() {
        return vmInformation.getJobID();
    }

    public byte[] getClassDataFromParentRuntime(String className)
        throws ProActiveException {
        byte[] classData = null;

        if (parentRuntime != null) {
            classData = parentRuntime.getClassDataFromThisRuntime(className);

            if (classData == null) {
                // continue searching
                classData = parentRuntime.getClassDataFromParentRuntime(className);
            }

            if (classData != null) {
                // caching class
                ClassDataCache.instance().addClassData(className, classData);

                if (runtimeLogger.isDebugEnabled()) {
                    runtimeLogger.debug(getURL() + " -- > Returning class " +
                        className + " found in " + parentRuntime.getURL());
                }

                return classData;
            }
        }

        return null;
    }

    public synchronized byte[] getClassDataFromThisRuntime(String className)
        throws ProActiveException {
        byte[] classData = null;

        // 1. look in class cache
        // this can be redundant if not looking in a parent
        classData = ClassDataCache.instance().getClassData(className);

        // found something in classloader or cache...
        if (classData != null) {
            return classData;
        }

        try {
            // 2. look in classpath
            classData = FileProcess.getBytesFromResource(className);
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        if (classData != null) {
            ClassDataCache.instance().addClassData(className, classData);

            return classData;
        }

        if (parentRuntime == null) {
            // top of hierarchy of runtimes
            classData = generateStub(className);

            if (classData != null) {
                return classData;
            }
        }

        return null;
    }

    public void launchMain(String className, String[] parameters)
        throws ClassNotFoundException, NoSuchMethodException, ProActiveException {
        Class mainClass = Class.forName(className);
        Method mainMethod = mainClass.getMethod("main",
                new Class[] { String[].class });
        new LauncherThread(mainMethod, parameters).start();
    }

    public void newRemote(String className)
        throws ClassNotFoundException, ProActiveException {
        Class remoteClass = Class.forName(className);
        new LauncherThread(remoteClass).start();
    }

    // tries to generate a stub without using MOP methods
    public byte[] generateStub(String className) {
        byte[] classData = null;

        if (Utils.isStubClassName(className)) {
            try {
                // do not use directly MOP methods (avoid classloader cycles)
                //                /Logger.getLogger(Loggers.CLASSLOADING).debug("Generating class : " + className);
                //    e.printStackTrace();
                String classname = Utils.convertStubClassNameToClassName(className);

                //ASM is now the default bytecode manipulator
                if (MOPClassLoader.BYTE_CODE_MANIPULATOR.equals("ASM")) {
                    ASMBytecodeStubBuilder bsb = new ASMBytecodeStubBuilder(classname);
                    classData = bsb.create();
                } else if (MOPClassLoader.BYTE_CODE_MANIPULATOR.equals(
                            "javassist")) {
                    classData = JavassistByteCodeStubBuilder.create(classname);
                } else {
                    // that shouldn't happen, unless someone manually sets the BYTE_CODE_MANIPULATOR static variable
                    System.err.println(
                        "byteCodeManipulator argument is optionnal. If specified, it can only be set to ASM.");
                    System.err.println(
                        "Any other setting will result in the use of javassist, the default bytecode manipulator framework");
                }
            } catch (ClassNotFoundException ignored) {
            }
        }

        if (classData != null) {
            ClassDataCache.instance().addClassData(className, classData);

            return classData;
        }

        // try to get the class as a generated component interface reference
        classData = RepresentativeInterfaceClassGenerator.getClassData(className);

        if (classData != null) {
            ClassDataCache.instance().addClassData(className, classData);

            return classData;
        }

        // try to get the class as a generated component interface reference
        classData = MetaObjectInterfaceClassGenerator.getClassData(className);

        if (classData != null) {
            ClassDataCache.instance().addClassData(className, classData);

            return classData;
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#terminateSession(long)
     */
    public void terminateSession(long sessionID)
        throws SecurityNotAvailableException {
        runtimeSecurityManager.terminateSession(sessionID);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getCertificate()
     */
    public X509Certificate getCertificate()
        throws SecurityNotAvailableException {
        return runtimeSecurityManager.getCertificate();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getProActiveSecurityManager()
     */
    public ProActiveSecurityManager getProActiveSecurityManager() {
        return runtimeSecurityManager;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#startNewSession(org.objectweb.proactive.ext.security.Communication)
     */
    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, RenegotiateSessionException {
        return runtimeSecurityManager.startNewSession(policy);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getPublicKey()
     */
    public PublicKey getPublicKey() throws SecurityNotAvailableException {
        return runtimeSecurityManager.getPublicKey();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#randomValue(long, byte[])
     */
    public byte[] randomValue(long sessionID, byte[] clientRandomValue)
        throws SecurityNotAvailableException {
        try {
            return runtimeSecurityManager.randomValue(sessionID,
                clientRandomValue);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#publicKeyExchange(long, org.objectweb.proactive.core.body.UniversalBody, byte[], byte[], byte[])
     */
    public byte[][] publicKeyExchange(long sessionID, byte[] myPublicKey,
        byte[] myCertificate, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException {
        if (runtimeSecurityManager != null) {
            try {
                return runtimeSecurityManager.publicKeyExchange(sessionID,
                    myPublicKey, myCertificate, signature);
            } catch (KeyExchangeException e) {
                e.printStackTrace();
            }
        } else {
            throw new SecurityNotAvailableException();
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#secretKeyExchange(long, byte[], byte[], byte[], byte[], byte[])
     */
    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey,
        byte[] encodedIVParameters, byte[] encodedClientMacKey,
        byte[] encodedLockData, byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException {
        return runtimeSecurityManager.secretKeyExchange(sessionID,
            encodedAESKey, encodedIVParameters, encodedClientMacKey,
            encodedLockData, parametersSignature);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getCertificateEncoded()
     */
    public byte[] getCertificateEncoded() throws SecurityNotAvailableException {
        return runtimeSecurityManager.getCertificateEncoded();
    }

    public ExternalProcess getProcessToDeploy(
        ProActiveRuntime proActiveRuntimeDist, String creatorID, String vmName,
        String padURL) throws ProActiveException {
        ProActiveDescriptor pad = (ProActiveDescriptor) descriptorMap.get(padURL);

        if (pad == null) {
            logger.info("Cannot find descriptor, " + padURL);

            return null;
        }

        notifyListeners(this,
            RuntimeRegistrationEvent.FORWARDER_RUNTIME_REGISTERED,
            proActiveRuntimeDist, creatorID, null, vmName);

        return pad.getHierarchicalProcess(vmName);
    }

    public String getVNName(String nodename) throws ProActiveException {
        return ((LocalNode) nodeMap.get(nodename)).getVirtualNodeName();
    }

    //
    // -- INNER CLASSES  -----------------------------------------------
    //
    protected static class VMInformationImpl implements VMInformation,
        java.io.Serializable {
        private java.net.InetAddress hostInetAddress;

        //the Unique ID of the JVM
        private java.rmi.dgc.VMID uniqueVMID;
        private String name;
        private String processCreatorId;
        private String jobId;
        private String hostName;

        public VMInformationImpl() throws java.net.UnknownHostException {
            this.uniqueVMID = UniqueID.getCurrentVMID();
            hostInetAddress = java.net.InetAddress.getLocalHost();
            hostName = UrlBuilder.getHostNameorIP(hostInetAddress);
            this.processCreatorId = "jvm";

            //            this.name = "PA_JVM" +
            //                Integer.toString(new java.security.SecureRandom().nextInt()) +
            //                "_" + hostName;
            String random = Integer.toString(ProActiveRuntimeImpl.getNextInt());

            if (System.getProperty("proactive.runtime.name") != null) {
                this.name = System.getProperty("proactive.runtime.name");

                if (this.name.indexOf("PA_JVM") < 0) {
                    runtimeLogger.warn(
                        "WARNING !!! The name of a ProActiveRuntime MUST contain PA_JVM string \n" +
                        "WARNING !!! Property proactive.runtime.name does not contain PA_JVM. This name is not adapted to IC2D tool");
                }
            } else {
                this.name = "PA_JVM" + random + "_" + hostName;
            }

            if (System.getProperty("proactive.jobid") != null) {
                this.jobId = System.getProperty("proactive.jobid");
            } else {
                //if the property is null, no need to generate another random, take the one in name
                this.jobId = "JOB-" + random;
            }
        }

        //
        // -- PUBLIC METHODS  -----------------------------------------------
        //
        //
        // -- implements VMInformation  -----------------------------------------------
        //
        public java.rmi.dgc.VMID getVMID() {
            return uniqueVMID;
        }

        public String getName() {
            return name;
        }

        public java.net.InetAddress getInetAddress() {
            return hostInetAddress;
        }

        public String getCreationProtocolID() {
            return this.processCreatorId;
        }

        public void setCreationProtocolID(String protocolId) {
            this.processCreatorId = protocolId;
        }

        /**
         * @see org.objectweb.proactive.Job#getJobID()
         */
        public String getJobID() {
            return this.jobId;
        }

        /**
         * @see org.objectweb.proactive.core.runtime.VMInformation#getHostName()
         */
        public String getHostName() {
            return this.hostName;
        }

        /**
         * @see org.objectweb.proactive.core.runtime.VMInformation#getDescriptorVMName()
         */
        public String getDescriptorVMName() {
            return name;
        }
    }

    //
    // ----------------- INNER CLASSES --------------------------------
    //

    /**
     * inner class for method invocation
     */
    private class LauncherThread extends Thread {
        private boolean launchMain;
        private Method mainMethod;
        private Class remoteClass;
        private String[] parameters;

        public LauncherThread(Class remoteClass) {
            this.remoteClass = remoteClass;
            launchMain = false;
        }

        public LauncherThread(Method mainMethod, String[] parameters) {
            this.mainMethod = mainMethod;
            this.parameters = parameters;
            launchMain = true;
        }

        public void run() {
            if (launchMain) {
                try {
                    mainMethod.invoke(null, new Object[] { parameters });
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    remoteClass.newInstance();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#setLocalNodeProperty(java.lang.String, java.lang.String, java.lang.String)
     */
    public Object setLocalNodeProperty(String nodeName, String key, String value) {
        return ((LocalNode) this.nodeMap.get(nodeName)).setProperty(key, value);
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getLocalNodeProperty(java.lang.String, java.lang.String)
     */
    public String getLocalNodeProperty(String nodeName, String key) {
        return ((LocalNode) this.nodeMap.get(nodeName)).getProperty(key);
    }
}
