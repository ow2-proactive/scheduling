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

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.UnmarshalException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueRuntimeID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.ext.security.Communication;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;

/**
 * An adapter for a RemoteProActiveRuntimeForwarder. The Adpater is the generic entry point for remote calls
 * to a RemoteProActiveRuntimeForwarder using different protocols such as RMI, RMISSH, IBIS, HTTP, JINI.
 * This also allows to cache informations, and so to avoid crossing the network when calling some methods.
 *
 * All calls done on a ProActiveRuntimeAdapterForwarderImpl, method1(foo, bar) for example, are
 * translated into remotePA.method1(urid, foo, bar) where urid is an unique identifiant for runtimes.
 * The forwarder forwards the call to the right runtime by using this ID. 
 * 
 * @author ProActiveTeam
 *
 */
public class ProActiveRuntimeAdapterForwarderImpl
    extends ProActiveRuntimeAdapter implements Serializable, Cloneable {
    private UniqueRuntimeID urid; // Cached for speed issue
    protected RemoteProActiveRuntimeForwarder proActiveRuntime;
    protected String proActiveRuntimeURL;

    // this boolean is used when killing the runtime. 
    // Indeed in case of co-allocation, we avoid a second call to the runtime which is already dead
    protected boolean alreadykilled = false;

    //
    // -- Constructors -----------------------------------------------
    //
    public ProActiveRuntimeAdapterForwarderImpl() {
    }

    public ProActiveRuntimeAdapterForwarderImpl(
        RemoteProActiveRuntimeForwarder proActiveRuntime)
        throws ProActiveException {
        this.proActiveRuntime = proActiveRuntime;

        try {
            this.vmInformation = proActiveRuntime.getVMInformation();
            this.proActiveRuntimeURL = proActiveRuntime.getURL();
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    public ProActiveRuntimeAdapterForwarderImpl(
        ProActiveRuntimeAdapterForwarderImpl localAdapter,
        ProActiveRuntime remoteAdapter) throws ProActiveException {
        this.proActiveRuntime = (RemoteProActiveRuntimeForwarder) localAdapter.proActiveRuntime;
        this.vmInformation = remoteAdapter.getVMInformation();
        this.proActiveRuntimeURL = remoteAdapter.getURL();
        this.urid = new UniqueRuntimeID(remoteAdapter.getVMInformation()
                                                     .getName(),
                remoteAdapter.getVMInformation().getVMID());
    }

    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();

        String prop = System.getProperty("proactive.hierarchicalRuntime");
        if ((prop != null) && prop.equals("true")) {

        	// on a forwarder and during the deserialization of a ProActiveAdapterForwarderImpl.
            ProActiveRuntimeForwarderImpl partf = (ProActiveRuntimeForwarderImpl) ProActiveRuntimeImpl.getProActiveRuntime();


            if (!partf.registeredRuntimes.containsKey(urid)) {
                try {
                	// Add this unknown runtime to the table of forwarded runtimes
                    partf.registeredRuntimes.put(urid, this.clone());
                } catch (CloneNotSupportedException e) {
                    runtimeLogger.warn(e);
                }
            }

            try {
            	// Change the RMI reference to point on this forwarder. That's all, it's automagic !
                proActiveRuntime = ((ProActiveRuntimeAdapterForwarderImpl) RuntimeFactory.getDefaultRuntime()).proActiveRuntime;
            } catch (ProActiveException e) {
                runtimeLogger.warn(e);
            }
        }
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public boolean equals(Object o) {
        if (!(o instanceof ProActiveRuntimeAdapterForwarderImpl)) {
            return false;
        }

        ProActiveRuntimeAdapterForwarderImpl runtime = (ProActiveRuntimeAdapterForwarderImpl) o;

        return this.urid.equals(runtime.urid);
    }

    public int hashCode() {
        return urid.hashCode();
    }

    //
    // -- Implements ProActiveRuntime -----------------------------------------------
    //

    /**
     * @throws NodeException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#createLocalNode(java.lang.String, boolean, ProActiveSecurityManager, java.lang.String, java.lang.String)
     */
    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding, ProActiveSecurityManager psm,
        String vnName, String jobId) throws NodeException {
        try {
            return proActiveRuntime.createLocalNode(urid, nodeName,
                replacePreviousBinding, psm, vnName, jobId);
        } catch (Exception e) {
            throw new NodeException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#killAllNodes()
     */
    public void killAllNodes() throws ProActiveException {
        try {
            proActiveRuntime.killAllNodes(urid);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#killNode(java.lang.String)
     */
    public void killNode(String nodeName) throws ProActiveException {
        try {
            proActiveRuntime.killNode(urid, nodeName);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws IOException
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#createVM(org.objectweb.proactive.core.process.UniversalProcess)
     */
    public void createVM(UniversalProcess remoteProcess)
        throws IOException, ProActiveException {
        proActiveRuntime.createVM(urid, remoteProcess);
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getLocalNodeNames()
     */
    public String[] getLocalNodeNames() throws ProActiveException {
        try {
            return proActiveRuntime.getLocalNodeNames(urid);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getVMInformation()
     */
    public VMInformation getVMInformation() {
        return vmInformation;
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#register(org.objectweb.proactive.core.runtime.ProActiveRuntime, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void register(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeUrl, String creatorID, String creationProtocol,
        String vmName) throws ProActiveException {
        try {
            proActiveRuntime.register(urid, proActiveRuntimeDist,
                proActiveRuntimeUrl, creatorID, creationProtocol, vmName);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#unregister(org.objectweb.proactive.core.runtime.ProActiveRuntime, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void unregister(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeUrl, String creatorID, String creationProtocol,
        String vmName) throws ProActiveException {
        try {
            this.proActiveRuntime.unregister(urid, proActiveRuntimeDist,
                proActiveRuntimeUrl, creatorID, creationProtocol, vmName);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getProActiveRuntimes()
     */
    public ProActiveRuntime[] getProActiveRuntimes() throws ProActiveException {
        try {
            return proActiveRuntime.getProActiveRuntimes(urid);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getProActiveRuntime(java.lang.String)
     */
    public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName)
        throws ProActiveException {
        try {
            return proActiveRuntime.getProActiveRuntime(urid,
                proActiveRuntimeName);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#addAcquaintance(java.lang.String)
     */
    public void addAcquaintance(String proActiveRuntimeName)
        throws ProActiveException {
        try {
            proActiveRuntime.addAcquaintance(urid, proActiveRuntimeName);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getAcquaintances()
     */
    public String[] getAcquaintances() throws ProActiveException {
        try {
            return proActiveRuntime.getAcquaintances(urid);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#rmAcquaintance(java.lang.String)
     */
    public void rmAcquaintance(String proActiveRuntimeName)
        throws ProActiveException {
        try {
            proActiveRuntime.rmAcquaintance(urid, proActiveRuntimeName);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#killRT(boolean)
     */
    public void killRT(boolean softly) throws Exception {
        try {
            if (!alreadykilled) {
                proActiveRuntime.killRT(urid, softly);
            }

            alreadykilled = true;
        } catch (UnmarshalException e) {
            //here should be caught the exception from System.exit
            alreadykilled = true;
            throw e;
        }
    }

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getURL()
     */
    public String getURL() {
        return proActiveRuntimeURL;
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getActiveObjects(java.lang.String)
     */
    public ArrayList getActiveObjects(String nodeName)
        throws ProActiveException {
        try {
            return proActiveRuntime.getActiveObjects(urid, nodeName);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getActiveObjects(java.lang.String, java.lang.String)
     */
    public ArrayList getActiveObjects(String nodeName, String className)
        throws ProActiveException {
        try {
            return proActiveRuntime.getActiveObjects(urid, nodeName, className);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getVirtualNode(java.lang.String)
     */
    public VirtualNode getVirtualNode(String virtualNodeName)
        throws ProActiveException {
        try {
            return proActiveRuntime.getVirtualNode(urid, virtualNodeName);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#registerVirtualNode(java.lang.String, boolean)
     */
    public void registerVirtualNode(String virtualNodeName,
        boolean replacePreviousBinding) throws ProActiveException {
        try {
            proActiveRuntime.registerVirtualNode(urid, virtualNodeName,
                replacePreviousBinding);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#unregisterVirtualNode(java.lang.String)
     */
    public void unregisterVirtualNode(String virtualNodeName)
        throws ProActiveException {
        try {
            proActiveRuntime.unregisterVirtualNode(urid, virtualNodeName);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#unregisterAllVirtualNodes()
     */
    public void unregisterAllVirtualNodes() throws ProActiveException {
        try {
            proActiveRuntime.unregisterAllVirtualNodes(urid);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getJobID(java.lang.String)
     */
    public String getJobID(String nodeUrl) throws ProActiveException {
        try {
            return proActiveRuntime.getJobID(urid, nodeUrl);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#createBody(java.lang.String, org.objectweb.proactive.core.mop.ConstructorCall, boolean)
     */
    public UniversalBody createBody(String nodeName,
        ConstructorCall bodyConstructorCall, boolean isNodeLocal)
        throws ConstructorCallExecutionFailedException, 
            InvocationTargetException, ProActiveException {
        try {
            return proActiveRuntime.createBody(urid, nodeName,
                bodyConstructorCall, isNodeLocal);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#receiveBody(java.lang.String, org.objectweb.proactive.Body)
     */
    public UniversalBody receiveBody(String nodeName, Body body)
        throws ProActiveException {
        try {
            return proActiveRuntime.receiveBody(urid, nodeName, body);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#receiveCheckpoint(java.lang.String, org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint, int)
     */
    public UniversalBody receiveCheckpoint(String nodeName, Checkpoint ckpt,
        int inc) throws ProActiveException {
        try {
            return proActiveRuntime.receiveCheckpoint(urid, nodeName, ckpt, inc);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getVNName(java.lang.String)
     */
    public String getVNName(String Nodename) throws ProActiveException {
        try {
            return proActiveRuntime.getVNName(urid, Nodename);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getClassDataFromThisRuntime(java.lang.String)
     */
    public byte[] getClassDataFromThisRuntime(String className)
        throws ProActiveException {
        try {
            return proActiveRuntime.getClassDataFromThisRuntime(urid, className);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * @throws ProActiveException
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#getClassDataFromParentRuntime(java.lang.String)
     */
    public byte[] getClassDataFromParentRuntime(String className)
        throws ProActiveException {
        try {
            return proActiveRuntime.getClassDataFromParentRuntime(urid,
                className);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    public ExternalProcess getProcessToDeploy(
        ProActiveRuntime proActiveRuntimeDist, String creatorID, String vmName,
        String padURL) throws ProActiveException {
        try {
            return proActiveRuntime.getProcessToDeploy(urid,
                proActiveRuntimeDist, creatorID, vmName, padURL);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    //
    // Implements Job Interface
    //

    /**
     * @see org.objectweb.proactive.Job#getJobID()
     */
    public String getJobID() {
        return vmInformation.getJobID();
    }

    public ProActiveDescriptor getDescriptor(String url,
        boolean isHierarchicalSearch) throws IOException, ProActiveException {
        try {
            return proActiveRuntime.getDescriptor(urid, url,
                isHierarchicalSearch);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    public void launchMain(String className, String[] parameters)
        throws ClassNotFoundException, NoSuchMethodException, 
            ProActiveException {
        try {
            proActiveRuntime.launchMain(urid, className, parameters);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    public void newRemote(String className)
        throws ClassNotFoundException, ProActiveException {
        try {
            proActiveRuntime.newRemote(urid, className);
        } catch (IOException e) {
            throw new ProActiveException(e);
        }
    }

    public X509Certificate getCertificate()
        throws SecurityNotAvailableException, IOException {
        return proActiveRuntime.getCertificate(urid);
    }

    public byte[] getCertificateEncoded()
        throws SecurityNotAvailableException, IOException {
        return proActiveRuntime.getCertificateEncoded(urid);
    }

    public ArrayList getEntities()
        throws SecurityNotAvailableException, IOException {
        return proActiveRuntime.getEntities(urid);
    }

    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
        return proActiveRuntime.getPublicKey(urid);
    }

    public byte[][] publicKeyExchange(long sessionID, byte[] myPublicKey,
        byte[] myCertificate, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            KeyExchangeException, IOException {
        return proActiveRuntime.publicKeyExchange(urid, sessionID, myPublicKey,
            myCertificate, signature);
    }

    public byte[] randomValue(long sessionID, byte[] clientRandomValue)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            IOException {
        return proActiveRuntime.randomValue(urid, sessionID, clientRandomValue);
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey,
        byte[] encodedIVParameters, byte[] encodedClientMacKey,
        byte[] encodedLockData, byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            IOException {
        return proActiveRuntime.secretKeyExchange(urid, sessionID,
            encodedAESKey, encodedIVParameters, encodedClientMacKey,
            encodedLockData, parametersSignature);
    }

    public long startNewSession(Communication policy)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            IOException {
        return proActiveRuntime.startNewSession(urid, policy);
    }

    public void terminateSession(long sessionID)
        throws SecurityNotAvailableException, IOException {
        proActiveRuntime.terminateSession(urid, sessionID);
    }

    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException {
        return proActiveRuntime.getPolicy(urid, securityContext);
    }
}
