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
package org.objectweb.proactive.core.runtime.rmi;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.rmi.RegistryHelper;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ext.security.Communication;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;


/**
 *   An adapter for a ProActiveRuntime to be able to receive remote calls. This helps isolate RMI-specific
 *   code into a small set of specific classes, thus enabling reuse if we one day decide to switch
 *   to anothe remote objects library.
 *   @see <a href="http://www.javaworld.com/javaworld/jw-05-1999/jw-05-networked_p.html">Adapter Pattern</a>
 */
public class RmiProActiveRuntimeImpl extends UnicastRemoteObject
    implements RmiProActiveRuntime {
    //  In few methods this field is cast into a ProActiveRuntimeImpl
    // First because we are sure that it is an instance of such object
    // and it avoids throwing an exception
    protected transient ProActiveRuntime proActiveRuntime;
    protected String proActiveRuntimeURL;

    //stores nodes urls to be able to unregister nodes
    private ArrayList nodesArray;

    //store vn urls to be able to unregister vns
    private ArrayList vnNodesArray;
    private boolean hasCreatedRegistry;

    //	
    // -- CONSTRUCTORS -----------------------------------------------
    //
    private void construct()
        throws java.rmi.RemoteException, java.rmi.AlreadyBoundException {
        //System.out.println("toto");
        this.hasCreatedRegistry = RegistryHelper.getRegistryCreator();
        try {
            this.proActiveRuntime = ProActiveRuntimeImpl.getProActiveRuntime();
        } catch (ExceptionInInitializerError e) {
            e.printStackTrace();
            throw e;
        }
        this.nodesArray = new java.util.ArrayList();
        this.vnNodesArray = new java.util.ArrayList();
        //this.urlBuilder = new UrlBuilder();
        this.proActiveRuntimeURL = buildRuntimeURL();
        //            java.rmi.Naming.bind(proActiveRuntimeURL, this);
        register(proActiveRuntimeURL, false);
        //System.out.println ("ProActiveRuntime successfully bound in registry at "+proActiveRuntimeURL);
    }

    public RmiProActiveRuntimeImpl()
        throws java.rmi.RemoteException, java.rmi.AlreadyBoundException {
        construct();
    }

    public RmiProActiveRuntimeImpl(RMIClientSocketFactory csf,
        RMIServerSocketFactory ssf)
        throws java.rmi.RemoteException, java.rmi.AlreadyBoundException {
        super(0, csf, ssf);
        construct();
    }

    public RmiProActiveRuntimeImpl(boolean isJini)
        throws java.rmi.RemoteException {
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding,
        ProActiveSecurityManager securityManager, String VNname, String jobId)
        throws NodeException, RemoteException {
        String nodeURL = null;

        //Node node;
        try {
            //first we build a well-formed url
            nodeURL = buildNodeURL(nodeName);
            //then take the name of the node
            String name = UrlBuilder.getNameFromUrl(nodeURL);

            //register the url in rmi registry
            register(nodeURL, replacePreviousBinding);

            proActiveRuntime.createLocalNode(name, replacePreviousBinding,
                securityManager, VNname, jobId);
        } catch (java.net.UnknownHostException e) {
            throw new java.rmi.RemoteException("Host unknown in " + nodeURL, e);
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
            throw new java.rmi.RemoteException("Host unknown in " + nodeUrl, e);
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
        //we can cast because for sure the runtime is a runtimeImpl
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
     * @see org.objectweb.proactive.core.runtime.rmi.RemoteProActiveRuntime#unregister(org.objectweb.proactive.core.runtime.ProActiveRuntime, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
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
        if (hasCreatedRegistry) {
            if (softly) {
                if (RegistryHelper.getRegistry().list().length > 0) {
                    new RMIKillerThread().start();
                    return;
                }
            }
        }
        proActiveRuntime.killRT(softly);
    }

    public String getURL() {
        return proActiveRuntimeURL;
    }

    //    public void setPortNumber(int p) {
    //    	this.portNumber = p;
    //    }
    //    
    //    public int getPortNumber() {
    //    	return this.portNumber;
    //    }
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
        boolean replacePreviousBinding) throws RemoteException {
        String virtualNodeURL = null;

        try {
            //first we build a well-formed url
            virtualNodeURL = buildNodeURL(virtualNodeName);
            //register it with the url
            register(virtualNodeURL, replacePreviousBinding);
        } catch (java.net.UnknownHostException e) {
            throw new java.rmi.RemoteException("Host unknown in " +
                virtualNodeURL, e);
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
            throw new java.rmi.RemoteException("Host unknown in " +
                virtualNodeURL, e);
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

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.rmi.RemoteProActiveRuntime#getJobID(java.lang.String)
     */
    public String getJobID(String nodeUrl)
        throws RemoteException, ProActiveException {
        return proActiveRuntime.getJobID(nodeUrl);
    }

    public byte[] getClassDataFromParentRuntime(String className)
        throws RemoteException, ProActiveException {
        try {
            return proActiveRuntime.getClassDataFromParentRuntime(className);
        } catch (ProActiveException e) {
            throw new ProActiveException("class not found : " + className, e);
        }
    }

    public byte[] getClassDataFromThisRuntime(String className)
        throws RemoteException, ProActiveException {
        return proActiveRuntime.getClassDataFromThisRuntime(className);
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
        throws java.rmi.RemoteException {
        try {
            if (replacePreviousBinding) {
                java.rmi.Naming.rebind(UrlBuilder.removeProtocol(url,
                        getProtocol()), this);
            } else {
                java.rmi.Naming.bind(UrlBuilder.removeProtocol(url,
                        getProtocol()), this);
            }
            if (url.indexOf("PA_JVM") < 0) {
                runtimeLogger.info(url + " successfully bound in registry at " +
                    url);
            }
        } catch (java.rmi.AlreadyBoundException e) {
            runtimeLogger.warn("WARNING " + url + " already bound in registry",
                e);
        } catch (java.net.MalformedURLException e) {
            throw new java.rmi.RemoteException("cannot bind in registry at " +
                url, e);
        }
    }

    private void unregister(String url) throws java.rmi.RemoteException {
        try {
            java.rmi.Naming.unbind(UrlBuilder.removeProtocol(url, getProtocol()));
            if (url.indexOf("PA_JVM") < 0) {
                runtimeLogger.info(url + " unbound in registry");
            }
        } catch (ConnectException e) {
            //if we get a connect exception, the rmi registry is unreachable. We cannot throw
            //an exception otherwise the killRT method cannot reach the end!
            if ((e.getCause().getClass().getName().equals("java.net.ConnectException") &&
                    e.getCause().getMessage().equals("Connection refused"))) {
                if (url.indexOf("PA_JVM") < 0) {
                    runtimeLogger.info("RMIRegistry unreachable on host " +
                        UrlBuilder.getHostNameorIP(getVMInformation()
                                                       .getInetAddress()) +
                        " to unregister " + url + ". Killed anyway !!!");
                }
            }
        } catch (java.net.MalformedURLException e) {
            throw new java.rmi.RemoteException("cannot unbind in registry at " +
                url, e);
        } catch (java.rmi.NotBoundException e) {
            //No need to throw an exception if an object is already unregistered
            runtimeLogger.info("WARNING " + url +
                " is not bound in the registry ");
        }
    }

    protected String getProtocol() {
        return Constants.RMI_PROTOCOL_IDENTIFIER;
    }

    private String buildRuntimeURL() {
        int port = RmiRuntimeFactory.getRegistryHelper().getRegistryPortNumber();
        String host = UrlBuilder.getHostNameorIP(getVMInformation()
                                                     .getInetAddress());
        String name = getVMInformation().getName();
        return UrlBuilder.buildUrl(host, name, getProtocol(), port);
    }

    private String buildNodeURL(String url)
        throws java.net.UnknownHostException {
        int i = url.indexOf('/');
        if (i == -1) {
            //it is an url given by a descriptor
            String host = UrlBuilder.getHostNameorIP(getVMInformation()
                                                         .getInetAddress());

            int port = RmiRuntimeFactory.getRegistryHelper()
                                        .getRegistryPortNumber();
            return UrlBuilder.buildUrl(host, url, getProtocol(), port);
        } else {
            return UrlBuilder.checkUrl(url);
        }
    }

    //
    // ----------------- INNER CLASSES --------------------------------
    //
    private class RMIKillerThread extends Thread {
        public RMIKillerThread() {
        }

        public void run() {
            try {
                while (RegistryHelper.getRegistry().list().length > 0) {
                    // the thread sleeps for 10 minutes
                    Thread.sleep(600000);
                }

                ((ProActiveRuntimeImpl) proActiveRuntime).killRT(false);
            } catch (InterruptedException e) {
                ((ProActiveRuntimeImpl) proActiveRuntime).killRT(false);
                e.printStackTrace();
            } catch (AccessException e) {
                runtimeLogger.error(e.getMessage());
                ((ProActiveRuntimeImpl) proActiveRuntime).killRT(false);
            } catch (Exception e) {
                runtimeLogger.error(e.getMessage());
                ((ProActiveRuntimeImpl) proActiveRuntime).killRT(false);
            }
        }
    }

    public String getVNName(String nodename)
        throws RemoteException, ProActiveException {
        return proActiveRuntime.getVNName(nodename);
    }

    // Security methods 
    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, IOException {
        return proActiveRuntime.getCertificate();
    }

    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            IOException {
        return proActiveRuntime.startNewSession(policy);
    }

    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
        return proActiveRuntime.getPublicKey();
    }

    public byte[] randomValue(long sessionID, byte[] clientRandomValue)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            IOException {
        return proActiveRuntime.randomValue(sessionID, clientRandomValue);
    }

    public byte[][] publicKeyExchange(long sessionID, byte[] myPublicKey,
        byte[] myCertificate, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            KeyExchangeException, IOException {
        return proActiveRuntime.publicKeyExchange(sessionID, myPublicKey,
            myCertificate, signature);
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey,
        byte[] encodedIVParameters, byte[] encodedClientMacKey,
        byte[] encodedLockData, byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            IOException {
        return proActiveRuntime.secretKeyExchange(sessionID, encodedAESKey,
            encodedIVParameters, encodedClientMacKey, encodedLockData,
            parametersSignature);
    }

    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException {
        return proActiveRuntime.getPolicy(securityContext);
    }

    public byte[] getCertificateEncoded()
        throws SecurityNotAvailableException, IOException {
        return proActiveRuntime.getCertificateEncoded();
    }

    public ArrayList getEntities()
        throws SecurityNotAvailableException, IOException {
        return proActiveRuntime.getEntities();
    }

    public void terminateSession(long sessionID)
        throws IOException, SecurityNotAvailableException {
        proActiveRuntime.terminateSession(sessionID);
    }
}
