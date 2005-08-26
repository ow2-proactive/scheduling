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
package org.objectweb.proactive.core.runtime.ibis;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ext.security.PolicyServer;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;

import ibis.rmi.AlreadyBoundException;
import ibis.rmi.Naming;
import ibis.rmi.NotBoundException;
import ibis.rmi.RemoteException;
import ibis.rmi.server.UnicastRemoteObject;


/**
 *   An adapter for a ProActiveRuntime to be able to receive remote calls. This helps isolate Ibis-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to anothe remote objects library.
 *          @see <a href="http://www.javaworld.com/javaworld/jw-05-1999/jw-05-networked_p.html">Adapter Pattern</a>
 */
public class IbisProActiveRuntimeImpl extends UnicastRemoteObject
    implements IbisProActiveRuntime {
    protected transient ProActiveRuntime proActiveRuntime;
    protected String proActiveRuntimeURL;

    //	stores nodes urls to be able to unregister nodes
    protected ArrayList nodesArray;

    //store vn urls to be able to unregister vns
    protected ArrayList vnNodesArray;

    //	
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public IbisProActiveRuntimeImpl()
        throws RemoteException, AlreadyBoundException {
        //System.out.println("toto");
        this.proActiveRuntime = ProActiveRuntimeImpl.getProActiveRuntime();
        this.nodesArray = new java.util.ArrayList();
        this.vnNodesArray = new java.util.ArrayList();
        this.proActiveRuntimeURL = buildRuntimeURL();
        register(proActiveRuntimeURL, false);
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding, PolicyServer ps, String vnname,
        String jobId) throws RemoteException, NodeException {
        String nodeURL = null;

        //Node node;
        try {
            //first we build a well-formed url
            nodeURL = buildNodeURL(nodeName);
            //then take the name of the node
            String name = UrlBuilder.getNameFromUrl(nodeURL);

            //register the url in rmi registry
            register(nodeURL, replacePreviousBinding);
            proActiveRuntime.createLocalNode(name, replacePreviousBinding, ps,
                vnname, jobId);
        } catch (java.net.UnknownHostException e) {
            throw new RemoteException("Host unknown in " + nodeURL, e);
        }
        nodesArray.add(nodeURL);
        return nodeURL;
    }

    public void killAllNodes() throws RemoteException, ProActiveException {
        for (int i = 0; i < nodesArray.size(); i++) {
            String url = (String) nodesArray.get(i);
            killNode(url);
        }
        proActiveRuntime.killAllNodes();
    }

    public void killNode(String nodeName)
        throws RemoteException, ProActiveException {
        String nodeUrl = null;
        String name = null;
        try {
            nodeUrl = buildNodeURL(nodeName);
            name = UrlBuilder.getNameFromUrl(nodeUrl);
            unregister(nodeUrl);
        } catch (UnknownHostException e) {
            throw new RemoteException("Host unknown in " + nodeUrl, e);
        }
        proActiveRuntime.killNode(name);
    }

    public void createVM(UniversalProcess remoteProcess)
        throws IOException, ProActiveException {
        proActiveRuntime.createVM(remoteProcess);
    }

    public String[] getLocalNodeNames()
        throws RemoteException, ProActiveException {
        return proActiveRuntime.getLocalNodeNames();
    }

    public VMInformation getVMInformation() {
        //      we can cast because for sure the runtime is a runtimeImpl
        // and we avoid throwing an exception
        return ((ProActiveRuntimeImpl) proActiveRuntime).getVMInformation();
    }

    public void register(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeName, String creatorID, String creationProtocol,
        String vmName) throws RemoteException, ProActiveException {
        proActiveRuntime.register(proActiveRuntimeDist, proActiveRuntimeName,
            creatorID, creationProtocol, vmName);
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ibis.RemoteProActiveRuntime#unregister(org.objectweb.proactive.core.runtime.ProActiveRuntime, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void unregister(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeName, String creatorID, String creationProtocol,
        String vmName) throws RemoteException, ProActiveException {
        this.proActiveRuntime.unregister(proActiveRuntimeDist,
            proActiveRuntimeURL, creatorID, creationProtocol, vmName);
    }

    public ProActiveRuntime[] getProActiveRuntimes()
        throws RemoteException, ProActiveException {
        return proActiveRuntime.getProActiveRuntimes();
    }

    public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName)
        throws RemoteException, ProActiveException {
        return proActiveRuntime.getProActiveRuntime(proActiveRuntimeName);
    }

    public void addAcquaintance(String proActiveRuntimeName)
        throws RemoteException, ProActiveException {
        proActiveRuntime.addAcquaintance(proActiveRuntimeName);
    }

    public String[] getAcquaintances()
        throws RemoteException, ProActiveException {
        return proActiveRuntime.getAcquaintances();
    }

    public void rmAcquaintance(String proActiveRuntimeName)
        throws RemoteException, ProActiveException {
        proActiveRuntime.rmAcquaintance(proActiveRuntimeName);
    }

    public void killRT(boolean softly) throws Exception {
        killAllNodes();
        unregisterAllVirtualNodes();
        unregister(proActiveRuntimeURL);
        proActiveRuntime.killRT(false);
    }

    public String getURL() {
        return proActiveRuntimeURL;
    }

    public ArrayList getActiveObjects(String nodeName)
        throws RemoteException, ProActiveException {
        return proActiveRuntime.getActiveObjects(nodeName);
    }

    public ArrayList getActiveObjects(String nodeName, String objectName)
        throws RemoteException, ProActiveException {
        return proActiveRuntime.getActiveObjects(nodeName, objectName);
    }

    public VirtualNode getVirtualNode(String virtualNodeName)
        throws RemoteException, ProActiveException {
        return proActiveRuntime.getVirtualNode(virtualNodeName);
    }

    public void registerVirtualNode(String virtualNodeName,
        boolean replacePreviousBinding) throws IOException {
        String virtualNodeURL = null;

        try {
            //first we build a well-formed url
            virtualNodeURL = buildNodeURL(virtualNodeName);
            //register it with the url
            register(virtualNodeURL, replacePreviousBinding);
        } catch (java.net.UnknownHostException e) {
            throw new RemoteException("Host unknown in " + virtualNodeURL, e);
        }
        vnNodesArray.add(virtualNodeURL);
    }

    public void unregisterVirtualNode(String virtualnodeName)
        throws RemoteException, ProActiveException {
        String virtualNodeURL = null;
        proActiveRuntime.unregisterVirtualNode(UrlBuilder.removeVnSuffix(
                virtualnodeName));
        try {
            //first we build a well-formed url
            virtualNodeURL = buildNodeURL(virtualnodeName);
            unregister(virtualNodeURL);
        } catch (java.net.UnknownHostException e) {
            throw new RemoteException("Host unknown in " + virtualNodeURL, e);
        }
        vnNodesArray.remove(virtualNodeURL);
    }

    public void unregisterAllVirtualNodes()
        throws RemoteException, ProActiveException {
        for (int i = 0; i < vnNodesArray.size(); i++) {
            String url = (String) vnNodesArray.get(i);
            unregisterVirtualNode(url);
        }
    }

    public UniversalBody createBody(String nodeName,
        ConstructorCall bodyConstructorCall, boolean isNodeLocal)
        throws RemoteException, ConstructorCallExecutionFailedException, 
            ProActiveException, InvocationTargetException {
        return proActiveRuntime.createBody(nodeName, bodyConstructorCall,
            isNodeLocal);
    }

    public UniversalBody receiveBody(String nodeName, Body body)
        throws RemoteException, ProActiveException {
        return proActiveRuntime.receiveBody(nodeName, body);
    }

    public UniversalBody receiveCheckpoint(String nodeName, Checkpoint ckpt,
        int inc) throws RemoteException, ProActiveException {
        return proActiveRuntime.receiveCheckpoint(nodeName, ckpt, inc);
    }

    // SECURITY

    /**
     * @return policy server
     * @throws ProActiveException
     */
    public PolicyServer getPolicyServer()
        throws RemoteException, ProActiveException {
        return proActiveRuntime.getPolicyServer();
    }

    public String getVNName(String nodename)
        throws RemoteException, ProActiveException {
        return proActiveRuntime.getVNName(nodename);
    }

    /**
     * @param nodeName
     * @return returns all entities associated to the node
     * @throws ProActiveException
     */
    public ArrayList getEntities(String nodeName)
        throws RemoteException, ProActiveException {
        return proActiveRuntime.getEntities(nodeName);
    }

    /**
     * @return returns all entities associated to this runtime
     * @throws SecurityNotAvailableException
     * @throws ProActiveException
     */
    public SecurityContext getPolicy(SecurityContext sc)
        throws RemoteException, ProActiveException, 
            SecurityNotAvailableException {
        return proActiveRuntime.getPolicy(sc);
    }

    //  -----------------------------------------
    //	Security: methods not used 
    //-----------------------------------------
    //    /* (non-Javadoc)
    //     * @see org.objectweb.proactive.core.runtime.rmi.RemoteProActiveRuntime#getCreatorCertificate()
    //     */
    //    public X509Certificate getCreatorCertificate() throws RemoteException {
    //        return proActiveRuntime.getCreatorCertificate();
    //    }
    //
    //    
    //
    //    public void setProActiveSecurityManager(ProActiveSecurityManager ps)
    //        throws RemoteException {
    //        proActiveRuntime.setProActiveSecurityManager(ps);
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.objectweb.proactive.core.runtime.rmi.RemoteProActiveRuntime#setDefaultNodeVirtualNodeNAme(java.lang.String)
    //     */
    //    public void setDefaultNodeVirtualNodeName(String s)
    //        throws RemoteException {
    //        proActiveRuntime.setDefaultNodeVirtualNodeName(s);
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.objectweb.proactive.core.runtime.rmi.RemoteProActiveRuntime#updateLocalNodeVirtualName()
    //     */
    //    public void updateLocalNodeVirtualName() throws RemoteException {
    //        //   proActiveRuntime.updateLocalNodeVirtualName();
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.objectweb.proactive.core.runtime.ibis.RemoteProActiveRuntime#getNodePolicyServer(java.lang.String)
    //     */
    //    public PolicyServer getNodePolicyServer(String nodeName)
    //        throws RemoteException {
    //        return null;
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.objectweb.proactive.core.runtime.ibis.RemoteProActiveRuntime#enableSecurityIfNeeded()
    //     */
    //    public void enableSecurityIfNeeded() throws RemoteException {
    //        // TODOremove this function
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.objectweb.proactive.core.runtime.ibis.RemoteProActiveRuntime#getNodeCertificate(java.lang.String)
    //     */
    //    public X509Certificate getNodeCertificate(String nodeName)
    //        throws RemoteException {
    //        return proActiveRuntime.getNodeCertificate(nodeName);
    //    }
    //
    //    
    //
    //    /**
    //     * @param uBody
    //     * @return returns all entities associated to the node
    //     */
    //    public ArrayList getEntities(UniversalBody uBody) throws RemoteException {
    //        return proActiveRuntime.getEntities(uBody);
    //    }
    //
    //    /**
    //     * @return returns all entities associated to this runtime
    //     */
    //    public ArrayList getEntities() throws RemoteException {
    //        return proActiveRuntime.getEntities();
    //    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ibis.RemoteProActiveRuntime#getJobID(java.lang.String)
     */
    public String getJobID(String nodeUrl)
        throws RemoteException, ProActiveException {
        return proActiveRuntime.getJobID(nodeUrl);
    }

    public byte[] getClassDataFromParentRuntime(String className)
        throws IOException, ProActiveException {
        try {
            return proActiveRuntime.getClassDataFromParentRuntime(className);
        } catch (ProActiveException e) {
            throw new ProActiveException("class not found : " + className, e);
        }
    }

    public byte[] getClassDataFromThisRuntime(String className)
        throws IOException, ProActiveException {
        return proActiveRuntime.getClassDataFromThisRuntime(className);
    }

    /**
     * @see org.objectweb.proactive.Job#getJobID()
     */
    public String getJobID() {
        return proActiveRuntime.getJobID();
    }

    public void launchMain(String className, String[] parameters)
        throws IOException, ClassNotFoundException, NoSuchMethodException, 
            ProActiveException {
        proActiveRuntime.launchMain(className, parameters);
    }

    public void newRemote(String className)
        throws IOException, ClassNotFoundException, ProActiveException {
        proActiveRuntime.newRemote(className);
    }

    public ProActiveDescriptor getDescriptor(String url,
        boolean isHierarchicalSearch) throws IOException, ProActiveException {
        return proActiveRuntime.getDescriptor(url, isHierarchicalSearch);
    }

    //
    // ---PRIVATE METHODS--------------------------------------
    //
    private void register(String url, boolean replacePreviousBinding)
        throws RemoteException {
        try {
            if (replacePreviousBinding) {
                Naming.rebind(UrlBuilder.removeProtocol(url, "ibis:"), this);
            } else {
                Naming.bind(UrlBuilder.removeProtocol(url, "ibis:"), this);
            }
            if (url.indexOf("PA_JVM") < 0) {
                runtimeLogger.info(url + " successfully bound in registry at " +
                    url);
            }
        } catch (AlreadyBoundException e) {
            runtimeLogger.warn("WARNING " + url + " already bound in registry",
                e);
        } catch (java.net.MalformedURLException e) {
            throw new RemoteException("cannot bind in registry at " + url, e);
        }
    }

    private void unregister(String url) throws RemoteException {
        try {
            Naming.unbind(UrlBuilder.removeProtocol(url, "ibis:"));
            if (url.indexOf("PA_JVM") < 0) {
                runtimeLogger.info(url + " unbound in registry");
            }
        } catch (java.net.MalformedURLException e) {
            throw new RemoteException("cannot unbind in registry at " + url, e);
        } catch (NotBoundException e) {
            //          No need to throw an exception if an object is already unregistered
            runtimeLogger.info("WARNING " + url +
                " is not bound in the registry ");
        }
    }

    private String buildRuntimeURL() {
        int port = IbisRuntimeFactory.getRegistryHelper().getRegistryPortNumber();
        String host = UrlBuilder.getHostNameorIP(getVMInformation()
                                                     .getInetAddress());
        String name = getVMInformation().getName();

        return UrlBuilder.buildUrl(host, name, "ibis:", port);
    }

    private String buildNodeURL(String url)
        throws java.net.UnknownHostException {
        int i = url.indexOf('/');

        if (i == -1) {
            //it is an url given by a descriptor
            String host = UrlBuilder.getHostNameorIP(getVMInformation()
                                                         .getInetAddress());
            int port = IbisRuntimeFactory.getRegistryHelper()
                                         .getRegistryPortNumber();
            return UrlBuilder.buildUrl(host, url, "ibis:", port);
        } else {
            return UrlBuilder.checkUrl(url);
        }
    }
}
