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
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.event.RuntimeRegistrationEvent;
import org.objectweb.proactive.core.event.RuntimeRegistrationEventProducerImpl;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.UniversalProcess;


import java.util.ArrayList;


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
    
    //
    // -- PRIVATE MEMBERS -----------------------------------------------------------
    //
    private VMInformation vmInformation;

    // map nodes and an ArrayList of Active Objects Id 
    private java.util.Hashtable nodeMap;

    //map VirtualNodes and their names
    private java.util.Hashtable virtualNodesMap;

    // map proActiveRuntime registered on this VM and their names
    private java.util.Hashtable proActiveRuntimeMap;

    //
    // -- CONSTRUCTORS -----------------------------------------------------------
    //
    // singleton
    private ProActiveRuntimeImpl() {
        try {
            this.nodeMap = new java.util.Hashtable();

            this.vmInformation = new VMInformationImpl();
            this.proActiveRuntimeMap = new java.util.Hashtable();
            this.virtualNodesMap = new java.util.Hashtable();

            //System.out.println(vmInformation.getVMID().toString());
        } catch (java.net.UnknownHostException e) {
            //System.out.println();
            logger.fatal(" !!! Cannot do a reverse lookup on that host");
            // System.out.println();
            e.printStackTrace();
            System.exit(1);
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
     * @param vnname the name of the VirtualNode to register
     */
    public void registerLocalVirtualNode(VirtualNode vn, String vnName) {
        //System.out.println("vn "+vnName+" registered");
        virtualNodesMap.put(vnName, vn);
    }

    //
    // -- Implements ProActiveRuntime  -----------------------------------------------
    //

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#createLocalNode(String, boolean)
     */
    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding) throws NodeException {
        //Node node = new NodeImpl(this,nodeName);
        //System.out.println("node created with name "+nodeName+"on proActiveruntime "+this);
        if (replacePreviousBinding) {
            if (nodeMap.get(nodeName) != null) {
                nodeMap.remove(nodeName);
            }
        }
        if (!replacePreviousBinding && (nodeMap.get(nodeName) != null)) {
            throw new NodeException("Node " + nodeName +
                " already created on this ProActiveRuntime. To overwrite this node, use true for replacePreviousBinding");
        }

        nodeMap.put(nodeName, new java.util.ArrayList());
        return nodeName;
    }

    /**

     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#DeleteAllNodes()
     */
    public void killAllNodes() {
        nodeMap.clear();
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#killNode(String)
     */
    public void killNode(String nodeName) {
        nodeMap.remove(nodeName);
    }

    //	/**
    //	 * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#createLocalVM(JVMProcess)
    //	 */
    //  public void createLocalVM(JVMProcess jvmProcess) throws java.io.IOException {
    //    jvmProcess.startProcess();
    //  }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#createVM(UniversalProcess)
     */
    public void createVM(UniversalProcess remoteProcess)
        throws java.io.IOException {
        remoteProcess.startProcess();
    }

    //  public Node[] getLocalNodes(){
    //    Node[] nodeArray = new Node[nodeMap.size()];
    //    nodeArray = (Node[])nodeMap.values().toArray();
    //    return nodeArray;
    //  }

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

    //	/**
    //	 * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getLocalNode(String)
    //	 */
    //  public String getLocalNode(String nodeName){
    //    return null;
    //  }
    //  
    //	
    //	/**
    //	 * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getNode(String)
    //	 */
    //  public String getNode(String nodeName) {
    //    return null;
    //  }
    //  public String getDefaultNodeName(){
    //  	return defaultNodeName;
    //  }

    /**
     *@see org.objectweb.proactive.core.runtime.ProActiveRuntime#getVMInformation()
     */
    public VMInformation getVMInformation() {
        return vmInformation;
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#register(ProActiveRuntime, String, String, String)
     */
    public void register(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeName, String creatorID, String creationProtocol) {
        //System.out.println("register in Impl");
        //System.out.println("thread"+Thread.currentThread().getName());
        //System.out.println(vmInformation.getVMID().toString());
        proActiveRuntimeMap.put(proActiveRuntimeName, proActiveRuntimeDist);
        notifyListeners(this, RuntimeRegistrationEvent.RUNTIME_REGISTERED,
            proActiveRuntimeName, creatorID, creationProtocol);
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
     *@see org.objectweb.proactive.core.runtime.ProActiveRuntime#killRT()
     */
    public void killRT() {
        System.exit(0);
    }

    /**
     *@see org.objectweb.proactive.core.runtime.ProActiveRuntime#getURL()
     */
    public String getURL() {
        return "//" + vmInformation.getInetAddress().getHostName() + "/" +
        vmInformation.getName();
        //		String protocol = System.getProperty("proactive.rmi")+":";
        //		System.out.println(protocol);
        //		String port = System.getProperty("proactive.rmi.port");
        //		if ( port != null){
        //			return UrlBuilder.buildUrl(vmInformation.getInetAddress().getHostName(), vmInformation.getName(),protocol, new Integer(port).intValue());
        //		}else{
        //			return UrlBuilder.buildUrl(vmInformation.getInetAddress().getHostName(), vmInformation.getName(),protocol);
        //        		
        //		} 
    }

    /**
     *@see org.objectweb.proactive.core.runtime.ProActiveRuntime#getURL()
     */
    public String getURL(int port) {
    	return "//" + vmInformation.getInetAddress().getHostName() + ":" + port + "/" +
    	vmInformation.getName();
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
        if (isLocal) {
            // if the body and proxy are on the same vm, returns the local view
            //System.out.println("body and proxy on the same vm");
            Body localBody = (Body) bodyConstructorCall.execute();

            //System.out.println(localBody.getReifiedObject().getClass().getName());
            //register the body in the nodemap
            registerBody(nodeName, localBody);
            return (UniversalBody) localBody;
        } else {
            //otherwise return the adapter
            //System.out.println ("RemoteProActiveImpl.createBody "+vmInformation.getInetAddress().getHostName() +" -> new "+bodyConstructorCall.getTargetClassName()+" on node "+nodeName);
            Body localBody = (Body) bodyConstructorCall.execute();
            registerBody(nodeName, localBody);
            //System.out.println ("RemoteProActiveRuntimeImpl.localBody created localBody="+localBody+" on node "+nodeName);
            return localBody.getRemoteAdapter();
        }
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#receiveBody(String, Body)
     */
    public UniversalBody receiveBody(String nodeName, Body body) {
        registerBody(nodeName, body);
        UniversalBody boa = body.getRemoteAdapter();
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

        public VMInformationImpl() throws java.net.UnknownHostException {
            this.uniqueVMID = UniqueID.getCurrentVMID();
            hostInetAddress = java.net.InetAddress.getLocalHost();
            String hostName = hostInetAddress.getHostName();
            this.processCreatorId = "jvm";
            //this.name = "PA_RT"+Integer.toString(new java.util.Random(System.currentTimeMillis()).nextInt())+"_"+hostName;
//                        this.name = "PA_RT" +
//                            Integer.toString(new java.security.SecureRandom().nextInt()) +
//                            "_" + hostName;
//            			this.name = "PA_RT" +
//            							Integer.toString(new java.util.Random().nextInt()) +
//            							"_" + hostName;
            this.name = "PA_RT" +
                Integer.toString(ProActiveRuntimeImpl.getNextInt()) + "_" +
                hostName;
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
    }
}
