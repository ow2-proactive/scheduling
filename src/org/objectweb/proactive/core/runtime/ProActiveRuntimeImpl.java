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
package org.objectweb.proactive.core.runtime;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.event.RuntimeRegistrationEvent;
import org.objectweb.proactive.core.event.RuntimeRegistrationEventProducerImpl;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ext.security.Entity;
import org.objectweb.proactive.ext.security.EntityCertificate;
import org.objectweb.proactive.ext.security.EntityVirtualNode;
import org.objectweb.proactive.ext.security.PolicyServer;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;


//
import java.io.File;
import java.io.IOException;

import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;


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
    implements ProActiveRuntime {
    //
    // -- STATIC MEMBERS -----------------------------------------------------------
    //
    //the Unique instance of ProActiveRuntime
    private static ProActiveRuntime proActiveRuntime = new ProActiveRuntimeImpl();
    private static Random prng = null; // new Random();

    private static synchronized int getNextInt() {
        if (prng == null) {
            prng = new Random();
        }
        return prng.nextInt();
    }

    //	map nodes and an ArrayList of PolicyServer 
    private java.util.Hashtable policyServerMap;

    // creator certificate
    private X509Certificate creatorCertificate;
    private X509Certificate certificate;
    private PrivateKey privateKey;

    // link to domain policy server 
    // private PolicyServer policyServer;
    private ProActiveSecurityManager psm;
    private String defaultNodeVirtualNode = null;

    //
    // -- PRIVATE MEMBERS -----------------------------------------------------------
    //
    private VMInformation vmInformation;

    // map nodes and an ArrayList of Active Objects Id 
    private java.util.Hashtable nodeMap;

    // map nodes and their job id;
    private Hashtable nodeJobIdMap;

    //map VirtualNodes and their names
    private java.util.Hashtable virtualNodesMap;

    //map descriptor and their url
    private java.util.Hashtable descriptorMap;

    // map proActiveRuntime registered on this VM and their names
    private java.util.Hashtable proActiveRuntimeMap;

    // synchronized set of URL to runtimes in which we are registered
    private java.util.Set parentsURL;

    //
    // -- CONSTRUCTORS -----------------------------------------------------------
    //
    // singleton
    private ProActiveRuntimeImpl() {
        try {
            this.nodeMap = new java.util.Hashtable();

            this.vmInformation = new VMInformationImpl();
            this.proActiveRuntimeMap = new java.util.Hashtable();
            this.parentsURL = java.util.Collections.synchronizedSortedSet(new java.util.TreeSet());
            this.virtualNodesMap = new java.util.Hashtable();
            this.descriptorMap = new java.util.Hashtable();
            this.policyServerMap = new java.util.Hashtable();
            this.nodeJobIdMap = new java.util.Hashtable();
            String file = System.getProperties().getProperty("proactive.runtime.security");

            Provider myProvider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
            Security.addProvider(myProvider);

            if ((file != null) && new File(file).exists()) {
                // loading security from a file
                logger.info(
                    "ProActive Security Policy (proactive.runtime.security) using " +
                    file);

                // policyServer = ProActiveSecurityDescriptorHandler.createPolicyServer(file);
                psm = new ProActiveSecurityManager(file);
            } else {
                // creating a generic certificate
                logger.info(
                    "ProActive Security Policy (proactive.runtime.security) not set. Runtime Security disabled ");
                //Object[] tmp = ProActiveSecurity.generateGenericCertificate();
                //certificate = (X509Certificate) tmp[0];
                //privateKey = (PrivateKey) tmp[1];
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
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------------------
    //
    public static ProActiveRuntime getProActiveRuntime() {
        return proActiveRuntime;
    }

    /**
     * Register the given VirtualNode on this ProActiveRuntime. This method cannot be called remotely.
     * @param vn the virtualnode to register
     * @param vnName the name of the VirtualNode to register
     */
    public void registerLocalVirtualNode(VirtualNode vn, String vnName) {
        //System.out.println("vn "+vnName+" registered");
        virtualNodesMap.put(vnName, vn);
    }

    public void registerDescriptor(String url, ProActiveDescriptor pad) {
        descriptorMap.put(url, pad);
    }

    public ProActiveDescriptor getDescriptor(String url) {
        return (ProActiveDescriptor) descriptorMap.get(url);
    }

    public void removeDescriptor(String url) {
        descriptorMap.remove(url);
    }

    //
    // -- Implements ProActiveRuntime  -----------------------------------------------
    //

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#createLocalNode(String, boolean, PolicyServer, String, String)
     */
    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding, PolicyServer ps, String vnName,
        String jobId) throws NodeException {
        //Node node = new NodeImpl(this,nodeName);
        //System.out.println("node created with name "+nodeName+"on proActiveruntime "+this);
        if (replacePreviousBinding) {
            if (nodeMap.get(nodeName) != null) {
                nodeMap.remove(nodeName);
                nodeJobIdMap.remove(nodeName);
            }
        }
        if (!replacePreviousBinding && (nodeMap.get(nodeName) != null)) {
            throw new NodeException("Node " + nodeName +
                " already created on this ProActiveRuntime. To overwrite this node, use true for replacePreviousBinding");
        }

        nodeMap.put(nodeName, new java.util.ArrayList());
        nodeJobIdMap.put(nodeName, jobId);

        if (ps != null) {
            System.out.println("Node Certificate : " +
                ps.getCertificate().getPublicKey());
            System.out.println("Node Certificate : " +
                ps.getCertificate().getIssuerDN());
        }

        if ((vnName != null) && (vnName.equals("currentJVM"))) {
            // if Jvm has been started using the currentJVM tag
            // vnName = defaultNodeVirtualNode;
            logger.debug(
                "++++++++++++++++++++++++++++++++++++++++++++CurrentJVM Tag ! Local Node : " +
                nodeName + " VN name : " + vnName + " policyserver " + ps);
        } else {
            logger.debug(
                "----------------------------------------Local Node : " +
                nodeName + " VN name : " + vnName + " policyserver " + ps);
        }

        if (ps != null) {
            logger.debug("generating node certificate");
            // ps.generateEntityCertificate(vnName + " " + nodeName, vmInformation);
            policyServerMap.put(nodeName, ps);
        }

        if (vnName != null) {
            virtualNodesMap.put(nodeName, vnName);
        }

        return nodeName;
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#killAllNodes()
     */
    public void killAllNodes() {
        virtualNodesMap.clear();
        policyServerMap.clear();
        nodeMap.clear();
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#killNode(String)
     */
    public void killNode(String nodeName) {
        virtualNodesMap.remove(nodeName);
        policyServerMap.remove(nodeName);
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
            proActiveRuntimeName, creatorID, creationProtocol, vmName);
        
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
     *@see org.objectweb.proactive.core.runtime.ProActiveRuntime#addParent(String)
     */
    public void addParent(String proActiveRuntimeName) {
        parentsURL.add(proActiveRuntimeName);
    }

    /**
     *@see org.objectweb.proactive.core.runtime.ProActiveRuntime#getParents()
     */
    public String[] getParents() {
        String[] urls;

        synchronized (parentsURL) {
            urls = new String[parentsURL.size()];
            java.util.Iterator iter = parentsURL.iterator();
            for (int i = 0; i < urls.length; i++)
                urls[i] = (String) iter.next();
        }

        return urls;
    }

    /**
     *@see org.objectweb.proactive.core.runtime.ProActiveRuntime#killRT(boolean)
     */
    public void killRT(boolean softly) {
        System.exit(0);
    }

    /**
     *@see org.objectweb.proactive.core.runtime.ProActiveRuntime#getURL()
     */
    public String getURL() {
        return "//" + vmInformation.getInetAddress().getCanonicalHostName() +
        "/" + vmInformation.getName();
    }

    public ArrayList getActiveObjects(String nodeName) {
        // we have to clone the array otherwise modifications done on nodeMap
        // would be reflected on the temp variable bodyArray
        ArrayList bodyArray = (ArrayList) ((ArrayList) nodeMap.get(nodeName)).clone();

        //the array to return
        ArrayList localBodies = new ArrayList();
        LocalBodyStore localBodystore = LocalBodyStore.getInstance();
        for (int i = 0; i < bodyArray.size(); i++) {
            UniqueID bodyID = (UniqueID) bodyArray.get(i);

            //check if the body is still on this vm
            Body body = localBodystore.getLocalBody(bodyID);
            if (body == null) {
                runtimeLogger.warn("body null");
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
        return (String) nodeJobIdMap.get(name);
    }

    public ArrayList getActiveObjects(String nodeName, String objectName) {
        // we have to clone the array otherwise modifications done on nodeMap
        // would be reflected on the temp variable bodyArray
        ArrayList bodyArray = (ArrayList) ((ArrayList) nodeMap.get(nodeName)).clone();

        //the array to return
        ArrayList localBodies = new ArrayList();
        LocalBodyStore localBodystore = LocalBodyStore.getInstance();
        for (int i = 0; i < bodyArray.size(); i++) {
            UniqueID bodyID = (UniqueID) bodyArray.get(i);

            //check if the body is still on this vm
            Body body = localBodystore.getLocalBody(bodyID);
            if (body == null) {
                runtimeLogger.warn("body null");
                // the body with the given ID is not any more on this ProActiveRuntime
                // unregister it from this ProActiveRuntime
                unregisterBody(nodeName, bodyID);
            } else {
                String objectClass = body.getReifiedObject().getClass().getName();

                // if the reified object is of the specified type
                // return the body adapter 
                if (objectClass.equals((String) objectName)) {
                    localBodies.add(body.getRemoteAdapter());
                }
            }
        }
        return localBodies;
    }

    /**
     *@see org.objectweb.proactive.core.runtime.ProActiveRuntime#createBody(String, ConstructorCall, boolean)
     */
    public UniversalBody createBody(String nodeName,
        ConstructorCall bodyConstructorCall, boolean isLocal)
        throws ConstructorCallExecutionFailedException, 
            java.lang.reflect.InvocationTargetException {
        //  System.out.println("XXXXXX creating body on " + this.getURL());
        //   ProActiveConfiguration.getConfiguration().dumpAddedProperties();
        Body localBody = (Body) bodyConstructorCall.execute();

        PolicyServer objectPolicyServer = null;

        // SECURITY
        try {
            PolicyServer ps = (PolicyServer) policyServerMap.get(nodeName);
            if (ps != null) {
                objectPolicyServer = (PolicyServer) ps.clone();

                String objectName = localBody.toString();

                System.out.println("local Object Name " + objectName +
                    "On node " + nodeName);
                objectPolicyServer.generateEntityCertificate(objectName);

                localBody.setPolicyServer(objectPolicyServer);
                localBody.getProActiveSecurityManager().setVNName((String) virtualNodesMap.get(
                        nodeName));
            }

            /*} catch (IOException e) {
               e.printStackTrace();
               } catch (SecurityNotAvailableException e) {
                       // do nothing
                       // security not available
            
             */
        } catch (CloneNotSupportedException e) {
            // should never happen
            e.printStackTrace();
        } catch (SecurityNotAvailableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        UniversalBody boa = body.getRemoteAdapter();
        body.setPolicyServer((PolicyServer) policyServerMap.get(nodeName));

        /*
           try {
                   boa.getProActiveSecurityManager().setPolicyServer((PolicyServer) policyServerMap.get(
                                   nodeName));
           } catch (IOException e) {
                   e.printStackTrace();
           } catch (SecurityNotAvailableException e) {
                   // do nothing
           }
         */
        registerBody(nodeName, body);
        return boa;
    }

    //	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    //		System.out.println("I am in runtime serialization ");
    //		out.defaultWriteObject();
    //	}
    //  //
    //  // --------------------Implements AbstractDeploymentEventProducer---------------------
    //  //
    //  
    //  protected void notifyOneListener(ProActiveListener proActiveListener, ProActiveEvent event){
    //  	RuntimeRegistrationEvent runtimeRegistrationEvent = (RuntimeRegistrationEvent) event;
    //  	RuntimeRegistrationEventListener runtimeRegistrationEventListener = (RuntimeRegistrationEventListener)proActiveListener;
    //  	//notify the listener that a registration occurs
    //  	runtimeRegistrationEventListener.runtimeRegistered(runtimeRegistrationEvent);
    //  }
    //  
    //  
    //  
    //  //
    //  // -- PRIVATE METHODS  -----------------------------------------------
    //  //
    //  
    //  private void notifyListeners(ProActiveRuntime proActiveRuntime,int type,String registeredRuntimeName, String creatorID, String protocol){
    //  	if (hasListeners()){
    //      notifyAllListeners(new RuntimeRegistrationEvent(proActiveRuntime, type, registeredRuntimeName, creatorID, protocol));
    //  	}
    //  	else System.out.println("no listener");
    //  }

    /**
     * Registers the specified body at the nodeName key in the <code>nodeMap</code>.
     * In fact it is the <code>UniqueID</code> of the body that is attached to the nodeName
     * in the <code>nodeMap</code>
     * @param nodeName. The name where to attached the body in the <code>nodeMap</code>
     * @param body. The body to register
     */
    private void registerBody(String nodeName, Body body) {
        UniqueID bodyID = body.getID();
        ArrayList bodyList = (ArrayList) nodeMap.get(nodeName);
        synchronized (bodyList) {
            if (!bodyList.contains(bodyID)) {
                //System.out.println("in registerbody id = "+ bodyID.toString());
                bodyList.add(bodyID);
            }
        }

        //ArrayList test = ((ArrayList)nodeMap.get(nodeName));
        //UniqueID id = (UniqueID)test.get(0);
        //UniqueID id1 = (UniqueID)test.get(1);
        //System.out.println("id "+id.toString());
        //System.out.println("id "+id1.toString());
    }

    /**
     * Unregisters the specified <code>UniqueID</code> from the <code>nodeMap</code> at the
     * nodeName key
     * @param nodeName. The key where to remove the <code>UniqueID</code>
     * @param bodyID. The <code>UniqueID</code> to remove
     */
    private void unregisterBody(String nodeName, UniqueID bodyID) {
        //System.out.println("in remove id= "+ bodyID.toString());
        //System.out.println("array size "+((ArrayList)nodeMap.get(nodeName)).size());
        ArrayList bodyList = (ArrayList) nodeMap.get(nodeName);
        synchronized (bodyList) {
            bodyList.remove(bodyID);
            //System.out.println("array size "+((ArrayList)nodeMap.get(nodeName)).size());
        }
    }

    ///**
    //	 * Method BuildDefaultNodeName.
    //	 * @param nodeName
    //	 */
    //	private String BuildDefaultNodeName() throws java.net.UnknownHostException
    //	{
    //		return "//"+vmInformation.getInetAddress().getHostName()+"/Node"+removeRuntime(getVMInformation().getName());
    //	}
    //	
    //	private static String removeRuntime(String url){
    //  	String tmp = url;
    //  	tmp = url.substring(5);
    //  	return tmp;
    //  }
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

        public VMInformationImpl() throws java.net.UnknownHostException {
            this.uniqueVMID = UniqueID.getCurrentVMID();
            hostInetAddress = java.net.InetAddress.getLocalHost();
            String hostName = hostInetAddress.getCanonicalHostName();
            this.processCreatorId = "jvm";

            //            this.name = "PA_JVM" +
            //                Integer.toString(new java.security.SecureRandom().nextInt()) +
            //                "_" + hostName;
            String random = Integer.toString(ProActiveRuntimeImpl.getNextInt());
            this.name = "PA_JVM" + random + "_" + hostName;
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
    }

    // SECURITY

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getCreatorCertificate()
     */
    public X509Certificate getCreatorCertificate() {
        return creatorCertificate;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getPolicyServer()
     */
    public PolicyServer getPolicyServer() {
        // System.out.println("return my policy server " + policyServer);
        if (psm != null) {
            return psm.getPolicyServer();
        }
        return null;
    }

    public String getVNName(String nodeName) {
        return (String) virtualNodesMap.get(nodeName);
    }

    /**
     * set policy server to all virtual nodes
     */
    public void setProActiveSecurityManager(ProActiveSecurityManager server) {
        if (psm != null) {
            return;
        }
        psm = server;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#setDefaultNodeVirtualNodeName(java.lang.String)
     */
    public void setDefaultNodeVirtualNodeName(String s) {
        System.out.println(
            " ************************ **********************************setting current node as currentJVM tag " +
            s);
        defaultNodeVirtualNode = s;
    }

    public void listVirtualNodes() {

        /*
           if (defaultNodeVirtualNode != null) {
           String nodeName = null;
                                           try {
                                                                           nodeName = NodeFactory.getDefaultNode().getNodeInformation().getURL();
                                                           } catch (NodeException e) {
                                                                           e.printStackTrace();
                                                           }
           distantVNMap.put(nodeName,defaultNodeVirtualNode);
           }
         */
        System.out.println("Local node 1");

        for (Enumeration e = nodeMap.keys(); e.hasMoreElements();) {
            //VirtualNode vn =(VirtualNode) e.nextElement();
            String key = (String) e.nextElement();
            System.out.println("local element " + key);
        }

        System.out.println("Local node ");
        for (Enumeration e = virtualNodesMap.elements(); e.hasMoreElements();) {
            String key = (String) e.nextElement();

            //String s = (String) distantVNMap.get(key);
            System.out.println(" node " + key + " belongs to ");
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getNodePolicyServer(java.lang.String)
     */
    public PolicyServer getNodePolicyServer(String nodeName) {
        return (PolicyServer) policyServerMap.get(nodeName);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#enableSecurityIfNeeded()
     */
    public void enableSecurityIfNeeded() {

        /*        Enumeration e = virtualNodesMap.elements();
           for (; e.hasMoreElements(); ){
                   VirtualNode vn = (VirtualNode) e.nextElement();
                   logger.debug("Setting VN " + vn+"-");
                   logger.debug(vn.getName() );
                   logger.debug(" - policyserver " + policyServer);
                   vn.setPolicyServer(policyServer);
           }
         */
    }

    /** (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getNodeCertificate(java.lang.String)
     */
    public X509Certificate getNodeCertificate(String nodeName) {
        PolicyServer ps = null;

        ps = (PolicyServer) policyServerMap.get(nodeName);
        if (ps != null) {
            return ps.getCertificate();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getEntities(java.lang.String)
     */
    public ArrayList getEntities(String nodeName) {
        PolicyServer ps = null;
        Entity nodeEntity = null;
        String nodeVirtualName = (String) virtualNodesMap.get(nodeName);
        ps = (PolicyServer) policyServerMap.get(nodeName);

        if (ps != null) {
            nodeEntity = new EntityVirtualNode(nodeVirtualName,
                    ps.getApplicationCertificate(), ps.getCertificate());
        }

        ArrayList entities = null;

        //entities = getEntities();
        if (entities == null) {
            entities = new ArrayList();
        }

        entities.add(nodeEntity);
        return entities;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getEntities(org.objectweb.proactive.core.body.UniversalBody)
     */
    public ArrayList getEntities(UniversalBody uBody) {
        try {
            return uBody.getEntities();
        } catch (SecurityNotAvailableException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getEntities()
     */
    public ArrayList getEntities() {
        PolicyServer policyServer = psm.getPolicyServer();
        Entity e = new EntityCertificate(policyServer.getApplicationCertificate(),
                policyServer.getCertificate());
        ArrayList array = new ArrayList();
        array.add(e);

        return array;
    }

    /**
     * @param sc
     */
    public SecurityContext getPolicy(SecurityContext sc)
        throws SecurityNotAvailableException {
        if (psm == null) {
            throw new SecurityNotAvailableException();
        }
        PolicyServer policyServer = psm.getPolicyServer();
        return policyServer.getPolicy(sc);
    }

    /**
     * @see org.objectweb.proactive.Job#getJobID()
     */
    public String getJobID() {
        return vmInformation.getJobID();
    }
}
