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
package org.objectweb.proactive.core.runtime.rmi;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
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
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeForwarder;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.ext.security.Communication;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;


public class RmiProActiveRuntimeForwarderImpl extends RmiProActiveRuntimeImpl
    implements RmiProActiveRuntimeForwarder {
    protected transient ProActiveRuntimeForwarder proActiveRuntimeF;

    //
    // --- CONSTRUCTORS
    //
    public RmiProActiveRuntimeForwarderImpl()
        throws RemoteException, AlreadyBoundException {
        super();
        proActiveRuntimeF = (ProActiveRuntimeForwarder) super.proActiveRuntime;
    }

    public RmiProActiveRuntimeForwarderImpl(boolean isJini)
        throws RemoteException {
        super(isJini);
        proActiveRuntimeF = (ProActiveRuntimeForwarder) super.proActiveRuntime;
    }

    public RmiProActiveRuntimeForwarderImpl(RMIClientSocketFactory csf,
        RMIServerSocketFactory ssf)
        throws RemoteException, AlreadyBoundException {
        super(csf, ssf);
        proActiveRuntimeF = (ProActiveRuntimeForwarder) super.proActiveRuntime;
    }

    //
    // --- Remote Interface
    //
    public void addAcquaintance(UniqueRuntimeID urid,
        String proActiveRuntimeName) throws IOException, ProActiveException {
        proActiveRuntimeF.addAcquaintance(urid, proActiveRuntimeName);
    }

    public UniversalBody createBody(UniqueRuntimeID urid, String nodeName,
        ConstructorCall bodyConstructorCall, boolean isNodeLocal)
        throws IOException, ConstructorCallExecutionFailedException,
            InvocationTargetException, ProActiveException {
        return proActiveRuntimeF.createBody(urid, nodeName,
            bodyConstructorCall, isNodeLocal);
    }

    public String createLocalNode(UniqueRuntimeID urid, String nodeName,
        boolean replacePreviousBinding, ProActiveSecurityManager ps,
        String VNname, String jobId)
        throws IOException, NodeException, AlreadyBoundException {
        return proActiveRuntimeF.createLocalNode(urid, nodeName,
            replacePreviousBinding, ps, VNname, jobId);
    }

    public void createVM(UniqueRuntimeID urid, UniversalProcess remoteProcess)
        throws IOException, ProActiveException {
        proActiveRuntimeF.createVM(urid, remoteProcess);
    }

    public String[] getAcquaintances(UniqueRuntimeID urid)
        throws IOException, ProActiveException {
        return proActiveRuntimeF.getAcquaintances(urid);
    }

    public ArrayList getActiveObjects(UniqueRuntimeID urid, String nodeName,
        String objectName) throws IOException, ProActiveException {
        return proActiveRuntimeF.getActiveObjects(urid, nodeName, objectName);
    }

    public ArrayList getActiveObjects(UniqueRuntimeID urid, String nodeName)
        throws IOException, ProActiveException {
        return proActiveRuntimeF.getActiveObjects(urid, nodeName);
    }

    public byte[] getClassDataFromParentRuntime(UniqueRuntimeID urid,
        String className) throws IOException, ProActiveException {
        return proActiveRuntimeF.getClassDataFromParentRuntime(urid, className);
    }

    public byte[] getClassDataFromThisRuntime(UniqueRuntimeID urid,
        String className) throws IOException, ProActiveException {
        return proActiveRuntimeF.getClassDataFromThisRuntime(urid, className);
    }

    public ArrayList getEntities(UniqueRuntimeID urid)
        throws IOException, SecurityNotAvailableException {
        return proActiveRuntimeF.getEntities(urid);
    }

    public String getJobID(UniqueRuntimeID urid, String nodeUrl)
        throws IOException, ProActiveException {
        return proActiveRuntimeF.getJobID(urid, nodeUrl);
    }

    public String[] getLocalNodeNames(UniqueRuntimeID urid)
        throws IOException, ProActiveException {
        return proActiveRuntimeF.getLocalNodeNames(urid);
    }

    public SecurityContext getPolicy(UniqueRuntimeID urid, SecurityContext sc)
        throws IOException, SecurityNotAvailableException {
        return proActiveRuntimeF.getPolicy(urid, sc);
    }

    public ProActiveRuntime getProActiveRuntime(UniqueRuntimeID urid,
        String proActiveRuntimeName) throws IOException, ProActiveException {
        return proActiveRuntimeF.getProActiveRuntime(urid, proActiveRuntimeName);
    }

    public ProActiveRuntime[] getProActiveRuntimes(UniqueRuntimeID urid)
        throws IOException, ProActiveException {
        return proActiveRuntimeF.getProActiveRuntimes(urid);
    }

    public ExternalProcess getProcessToDeploy(UniqueRuntimeID urid,
        ProActiveRuntime proActiveRuntimeDist, String creatorID, String vmName,
        String padURL) throws ProActiveException, IOException {
        return proActiveRuntimeF.getProcessToDeploy(urid, proActiveRuntimeDist,
            creatorID, vmName, padURL);
    }

    public String getURL(UniqueRuntimeID urid)
        throws IOException, ProActiveException {
        return proActiveRuntimeF.getURL(urid);
    }

    public VirtualNode getVirtualNode(UniqueRuntimeID urid,
        String virtualNodeName) throws IOException, ProActiveException {
        return proActiveRuntimeF.getVirtualNode(urid, virtualNodeName);
    }

    public VMInformation getVMInformation(UniqueRuntimeID urid)
        throws IOException {
        return proActiveRuntimeF.getVMInformation();
    }

    public String getVNName(UniqueRuntimeID urid, String Nodename)
        throws IOException, ProActiveException {
        return proActiveRuntimeF.getVNName(urid, Nodename);
    }

    public void killAllNodes(UniqueRuntimeID urid)
        throws IOException, ProActiveException {
        proActiveRuntimeF.killAllNodes(urid);
    }

    public void killNode(UniqueRuntimeID urid, String nodeName)
        throws IOException, ProActiveException {
        proActiveRuntimeF.killNode(urid, nodeName);
    }

    public void killRT(UniqueRuntimeID urid, boolean softly)
        throws Exception {
        proActiveRuntimeF.killRT(urid, softly);
    }

    public UniversalBody receiveBody(UniqueRuntimeID urid, String nodeName,
        Body body) throws IOException, ProActiveException {
        return proActiveRuntimeF.receiveBody(urid, nodeName, body);
    }

    public UniversalBody receiveCheckpoint(UniqueRuntimeID urid,
        String nodeName, Checkpoint ckpt, int inc)
        throws IOException, ProActiveException {
        return proActiveRuntimeF.receiveCheckpoint(urid, nodeName, ckpt, inc);
    }

    public void register(UniqueRuntimeID urid,
        ProActiveRuntime proActiveRuntimeDist, String proActiveRuntimeName,
        String creatorID, String creationProtocol, String vmName)
        throws IOException, ProActiveException {
        proActiveRuntimeF.register(urid, proActiveRuntimeDist,
            proActiveRuntimeName, creatorID, creationProtocol, vmName);
    }

    public void registerVirtualNode(UniqueRuntimeID urid,
        String virtualNodeName, boolean replacePreviousBinding)
        throws IOException, ProActiveException, java.rmi.AlreadyBoundException {
        proActiveRuntimeF.registerVirtualNode(urid, virtualNodeName,
            replacePreviousBinding);
    }

    public void rmAcquaintance(UniqueRuntimeID urid, String proActiveRuntimeName)
        throws IOException, ProActiveException {
        proActiveRuntimeF.rmAcquaintance(urid, proActiveRuntimeName);
    }

    public void unregister(UniqueRuntimeID urid,
        ProActiveRuntime proActiveRuntimeDist, String proActiveRuntimeName,
        String creatorID, String creationProtocol, String vmName)
        throws IOException, ProActiveException {
        proActiveRuntimeF.unregister(urid, proActiveRuntimeDist,
            proActiveRuntimeURL, creatorID, creationProtocol, vmName);
    }

    public void unregisterAllVirtualNodes(UniqueRuntimeID urid)
        throws IOException, ProActiveException {
        proActiveRuntimeF.unregisterAllVirtualNodes(urid);
    }

    public void unregisterVirtualNode(UniqueRuntimeID urid,
        String virtualNodeName) throws IOException, ProActiveException {
        proActiveRuntimeF.unregisterVirtualNode(urid, virtualNodeName);
    }

    public X509Certificate getCertificate(UniqueRuntimeID urid)
        throws SecurityNotAvailableException, IOException {
        return proActiveRuntimeF.getCertificate(urid);
    }

    public byte[] getCertificateEncoded(UniqueRuntimeID urid)
        throws SecurityNotAvailableException, IOException {
        return proActiveRuntimeF.getCertificateEncoded(urid);
    }

    public ProActiveDescriptor getDescriptor(UniqueRuntimeID urid, String url,
        boolean isHierarchicalSearch) throws IOException, ProActiveException {
        return proActiveRuntimeF.getDescriptor(urid, url, isHierarchicalSearch);
    }

    public PublicKey getPublicKey(UniqueRuntimeID urid)
        throws SecurityNotAvailableException, IOException {
        return proActiveRuntimeF.getPublicKey(urid);
    }

    public void launchMain(UniqueRuntimeID urid, String className,
        String[] parameters)
        throws IOException, ClassNotFoundException, NoSuchMethodException,
            ProActiveException {
        proActiveRuntimeF.launchMain(className, parameters);
    }

    public void newRemote(UniqueRuntimeID urid, String className)
        throws IOException, ClassNotFoundException, ProActiveException {
        proActiveRuntimeF.newRemote(className);
    }

    public byte[][] publicKeyExchange(UniqueRuntimeID urid, long sessionID,
        byte[] myPublicKey, byte[] myCertificate, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            KeyExchangeException, IOException {
        return proActiveRuntimeF.publicKeyExchange(urid, sessionID,
            myPublicKey, myCertificate, signature);
    }

    public byte[] randomValue(UniqueRuntimeID urid, long sessionID,
        byte[] clientRandomValue)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        return proActiveRuntimeF.randomValue(urid, sessionID, clientRandomValue);
    }

    public byte[][] secretKeyExchange(UniqueRuntimeID urid, long sessionID,
        byte[] encodedAESKey, byte[] encodedIVParameters,
        byte[] encodedClientMacKey, byte[] encodedLockData,
        byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        return proActiveRuntimeF.secretKeyExchange(urid, sessionID,
            encodedAESKey, encodedIVParameters, encodedClientMacKey,
            encodedLockData, parametersSignature);
    }

    public long startNewSession(UniqueRuntimeID urid, Communication policy)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        return proActiveRuntimeF.startNewSession(policy);
    }

    public void terminateSession(UniqueRuntimeID urid, long sessionID)
        throws IOException, SecurityNotAvailableException {
        // TODO Auto-generated method stub
    }

    public Object setLocalNodeProperty(UniqueRuntimeID runtimeID,
        String nodeName, String key, String value)
        throws IOException, ProActiveException {
        return this.proActiveRuntimeF.setLocalNodeProperty(runtimeID, nodeName,
            key, value);
    }

    public String getLocalNodeProperty(UniqueRuntimeID runtimeID,
        String nodeName, String key) throws IOException, ProActiveException {
        return this.proActiveRuntimeF.getLocalNodeProperty(runtimeID, nodeName,
            key);
    }
}
