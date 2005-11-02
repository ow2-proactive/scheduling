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

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.apache.log4j.Logger;
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
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.ext.security.Communication;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;
import org.objectweb.proactive.ext.security.SecurityContext;
import org.objectweb.proactive.ext.security.crypto.KeyExchangeException;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.ext.security.exceptions.SecurityNotAvailableException;


/**
 * A ProActiveRuntimeForwarder is a ProActiveRuntime and a forwarder.
 *
 * All ProActiveRuntime methods are inherited, and for each inherited a new one
 * with the same name, the same return type, same arguments, but prefixed by an argument
 * of type UniqueRuntimeID is defined.
 *
 * When a method inherited from ProActiveRuntime is called a forwarder behave like a normal
 * runtime, it's a normal runtime.
 *
 * When a prefixed method is called, the prefix argument is used to forward the call to the
 * right runtime. To perform the dispatch a forwarder uses a HashTable
 * <UniqueRuntimeID, Adapter>, which is filled during deserialization of
 * ProActiveRuntimeAdapter objects.
 *
 * Check org.objectweb.proactive.core.runtime.ProActiveRuntimeAdapterForwarderImpl#readObject(java.io.ObjectInputStream)
 * @see org.objectweb.proactive.core.runtime.ProActiveRuntimeAdapterImpl#readResolve()
 *
 * @author  ProActive Team
 */
public interface ProActiveRuntimeForwarder extends ProActiveRuntime {
    static Logger runtimeLogger = ProActiveLogger.getLogger(Loggers.RUNTIME);

    /**
     * @see org.objectweb.proactive.core.runtime.ProActiveRuntime#createLocalNode(String, boolean, ProActiveSecurityManager, String, String)
     */
    public String createLocalNode(UniqueRuntimeID urid, String nodeName,
        boolean replacePreviousBinding, ProActiveSecurityManager psm,
        String vnName, String jobId) throws NodeException;

    /**
     * @see ProActiveRuntime#killAllNodes()
     */
    public void killAllNodes(UniqueRuntimeID urid) throws ProActiveException;

    /**
     * @see ProActiveRuntime#killNode(String)
     */
    public void killNode(UniqueRuntimeID urid, String nodeName)
        throws ProActiveException;

    /**
     * @see ProActiveRuntime#createVM(UniversalProcess)
     */
    public void createVM(UniqueRuntimeID urid, UniversalProcess remoteProcess)
        throws java.io.IOException, ProActiveException;

    /**
     * @see ProActiveRuntime#getLocalNodeNames()
     */
    public String[] getLocalNodeNames(UniqueRuntimeID urid)
        throws ProActiveException;

    /**
     * @see ProActiveRuntime#getVMInformation()
     */
    public VMInformation getVMInformation(UniqueRuntimeID urid);

    /**
     * @see ProActiveRuntime#register(ProActiveRuntime, String, String, String, String)
     */
    public void register(UniqueRuntimeID urid,
        ProActiveRuntime proActiveRuntimeDist, String proActiveRuntimeUrl,
        String creatorID, String creationProtocol, String vmName)
        throws ProActiveException;

    /**
     * @see ProActiveRuntime#unregister(ProActiveRuntime, String, String, String, String)
     */
    public void unregister(UniqueRuntimeID urid,
        ProActiveRuntime proActiveRuntimeDist, String proActiveRuntimeUrl,
        String creatorID, String creationProtocol, String vmName)
        throws ProActiveException;

    /**
     * @see ProActiveRuntime#getProActiveRuntimes()
     */
    public ProActiveRuntime[] getProActiveRuntimes(UniqueRuntimeID urid)
        throws ProActiveException;

    /**
     * @see ProActiveRuntime#getProActiveRuntime(String)
     */
    public ProActiveRuntime getProActiveRuntime(UniqueRuntimeID urid,
        String proActiveRuntimeName) throws ProActiveException;

    /**
     * @see ProActiveRuntime#addAcquaintance(String)
     */
    void addAcquaintance(UniqueRuntimeID urid, String proActiveRuntimeName)
        throws ProActiveException;

    /**
     * @see ProActiveRuntime#getAcquaintances()
     */
    public String[] getAcquaintances(UniqueRuntimeID urid)
        throws ProActiveException;

    /**
     * @see ProActiveRuntime#rmAcquaintance(String)
     */
    public void rmAcquaintance(UniqueRuntimeID urid, String proActiveRuntimeName)
        throws ProActiveException;

    /**
     * @see ProActiveRuntime#killRT(boolean)
     */
    public void killRT(UniqueRuntimeID urid, boolean softly)
        throws Exception;

    /**
     * @see ProActiveRuntime#getURL()
     */
    public String getURL(UniqueRuntimeID urid);

    /**
     * @see ProActiveRuntime#getActiveObjects(String)
     */
    public ArrayList getActiveObjects(UniqueRuntimeID urid, String nodeName)
        throws ProActiveException;

    /**
     * @see ProActiveRuntime#getActiveObjects(String, String)
     */
    public ArrayList getActiveObjects(UniqueRuntimeID urid, String nodeName,
        String className) throws ProActiveException;

    /**
     * @see ProActiveRuntime#getVirtualNode(String)
     */
    public VirtualNode getVirtualNode(UniqueRuntimeID urid,
        String virtualNodeName) throws ProActiveException;

    /**
     * @see ProActiveRuntime#registerVirtualNode(String, boolean)
     */
    public void registerVirtualNode(UniqueRuntimeID urid,
        String virtualNodeName, boolean replacePreviousBinding)
        throws ProActiveException;

    /**
     * @see ProActiveRuntime#unregisterVirtualNode(String)
     */
    public void unregisterVirtualNode(UniqueRuntimeID urid,
        String virtualNodeName) throws ProActiveException;

    /**
     * @see ProActiveRuntime#unregisterAllVirtualNodes()
     */
    public void unregisterAllVirtualNodes(UniqueRuntimeID urid)
        throws ProActiveException;

    /**
     * @see ProActiveRuntime#getJobID(String)
     */
    public String getJobID(UniqueRuntimeID urid, String nodeUrl)
        throws ProActiveException;

    /**
     * @see ProActiveRuntime#createBody(String, ConstructorCall, boolean)
     */
    public UniversalBody createBody(UniqueRuntimeID urid, String nodeName,
        ConstructorCall bodyConstructorCall, boolean isNodeLocal)
        throws ProActiveException, ConstructorCallExecutionFailedException, 
            java.lang.reflect.InvocationTargetException;

    /**
     * @see ProActiveRuntime#receiveBody(String, Body)
     */
    public UniversalBody receiveBody(UniqueRuntimeID urid, String nodeName,
        Body body) throws ProActiveException;

    /**
     * @see ProActiveRuntime#receiveCheckpoint(String, Checkpoint, int)
     */
    public UniversalBody receiveCheckpoint(UniqueRuntimeID urid,
        String nodeName, Checkpoint ckpt, int inc) throws ProActiveException;

    /**
     * @see ProActiveRuntime#getProcessToDeploy(ProActiveRuntime, String, String, String)
     */
    public ExternalProcess getProcessToDeploy(UniqueRuntimeID urid,
        ProActiveRuntime proActiveRuntimeDist, String creatorID, String vmName,
        String padURL) throws ProActiveException;

    /**
     * @see ProActiveRuntime#getClassDataFromThisRuntime(String)
     */
    public byte[] getClassDataFromThisRuntime(UniqueRuntimeID urid,
        String className) throws ProActiveException;

    /**
     * @see ProActiveRuntime#getClassDataFromParentRuntime(String)
     */
    public byte[] getClassDataFromParentRuntime(UniqueRuntimeID urid,
        String className) throws ProActiveException;

    /**
     * @see ProActiveRuntime#launchMain(String, String[])
     */
    public void launchMain(UniqueRuntimeID urid, String className,
        String[] parameters)
        throws ClassNotFoundException, NoSuchMethodException, 
            ProActiveException;

    /**
     * @see ProActiveRuntime#newRemote(String)
     */
    public void newRemote(UniqueRuntimeID urid, String className)
        throws ClassNotFoundException, ProActiveException;

    // 
    // -- SECURITY
    //    
    public X509Certificate getCertificate(UniqueRuntimeID urid)
        throws SecurityNotAvailableException, IOException;

    public long startNewSession(UniqueRuntimeID urid, Communication policy)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            IOException;

    public PublicKey getPublicKey(UniqueRuntimeID urid)
        throws SecurityNotAvailableException, IOException;

    public byte[] randomValue(UniqueRuntimeID urid, long sessionID,
        byte[] clientRandomValue)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            IOException;

    public byte[][] publicKeyExchange(UniqueRuntimeID urid, long sessionID,
        byte[] myPublicKey, byte[] myCertificate, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            KeyExchangeException, IOException;

    public byte[][] secretKeyExchange(UniqueRuntimeID urid, long sessionID,
        byte[] encodedAESKey, byte[] encodedIVParameters,
        byte[] encodedClientMacKey, byte[] encodedLockData,
        byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException, 
            IOException;

    public SecurityContext getPolicy(UniqueRuntimeID urid,
        SecurityContext securityContext)
        throws SecurityNotAvailableException, IOException;

    public byte[] getCertificateEncoded(UniqueRuntimeID urid)
        throws SecurityNotAvailableException, IOException;

    public ArrayList getEntities(UniqueRuntimeID urid)
        throws SecurityNotAvailableException, IOException;

    public void terminateSession(UniqueRuntimeID urid, long sessionID)
        throws SecurityNotAvailableException, IOException;

    public String getVNName(UniqueRuntimeID urid, String Nodename)
        throws ProActiveException;

    public ProActiveDescriptor getDescriptor(UniqueRuntimeID urid, String url,
        boolean isHierarchicalSearch) throws IOException, ProActiveException;

    // Interface: Job
    public String getJobID(UniqueRuntimeID urid);
}
