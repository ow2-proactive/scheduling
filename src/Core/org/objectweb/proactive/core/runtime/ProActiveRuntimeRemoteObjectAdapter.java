/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.runtime;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.AlreadyBoundException;
import java.security.AccessControlException;
import java.security.PublicKey;
import java.util.List;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorInternal;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeInternal;
import org.objectweb.proactive.core.filetransfer.FileTransferEngine;
import org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean;
import org.objectweb.proactive.core.jmx.notification.GCMRuntimeRegistrationNotificationData;
import org.objectweb.proactive.core.jmx.server.ServerConnector;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.remoteobject.adapter.Adapter;
import org.objectweb.proactive.core.security.PolicyServer;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.TypedCertificate;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.crypto.SessionException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entities;
import org.objectweb.proactive.core.security.securityentity.Entity;

/**
 * @author acontes
 * this class provides some additional behaviours expected when talking with a
 * runtime.
 *  - cache the vmInformation field
 */
public class ProActiveRuntimeRemoteObjectAdapter extends Adapter<ProActiveRuntime>
    implements ProActiveRuntime {

    /**
	 * generated serial uid
	 */
	private static final long serialVersionUID = -8238213803072932083L;


	/**
     * Cache the vmInformation field
     */
    protected VMInformation vmInformation;

    public ProActiveRuntimeRemoteObjectAdapter() {
    	// empty non arg constructor
    }

    public ProActiveRuntimeRemoteObjectAdapter(ProActiveRuntime u) {
        super(u);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.adapter.Adapter#construct()
     */
    @Override
    protected void construct() {
        this.vmInformation = target.getVMInformation();
    }


    // =========   Implements ProActiveRuntime ==================
    @Override
    public void addAcquaintance(String proActiveRuntimeName)
        throws ProActiveException {
        target.addAcquaintance(proActiveRuntimeName);
    }

    @Override
    public UniversalBody createBody(String nodeName,
        ConstructorCall bodyConstructorCall, boolean isNodeLocal)
        throws ProActiveException, ConstructorCallExecutionFailedException,
            InvocationTargetException {
        return target.createBody(nodeName, bodyConstructorCall, isNodeLocal);
    }

    @Override
    public String createLocalNode(String nodeName,
        boolean replacePreviousBinding,
        ProActiveSecurityManager nodeSecurityManager, String vnName,
        String jobId) throws NodeException, AlreadyBoundException {
        return target.createLocalNode(nodeName, replacePreviousBinding,
            nodeSecurityManager, vnName, jobId);
    }

    @Override
    public void createVM(UniversalProcess remoteProcess)
        throws IOException, ProActiveException {
        target.createVM(remoteProcess);
    }

    @Override
    public String[] getAcquaintances() throws ProActiveException {
        return target.getAcquaintances();
    }

    @Override
    public List<UniversalBody> getActiveObjects(String nodeName)
        throws ProActiveException {
        return target.getActiveObjects(nodeName);
    }

    @Override
    public List<UniversalBody> getActiveObjects(String nodeName,
        String className) throws ProActiveException {
        return target.getActiveObjects(nodeName, className);
    }

    @Override
    public byte[] getClassDataFromParentRuntime(String className)
        throws ProActiveException {
        return target.getClassDataFromParentRuntime(className);
    }

    @Override
    public byte[] getClassDataFromThisRuntime(String className)
        throws ProActiveException {
        return target.getClassDataFromThisRuntime(className);
    }

    @Override
    public ProActiveDescriptorInternal getDescriptor(String url,
        boolean isHierarchicalSearch) throws IOException, ProActiveException {
        return target.getDescriptor(url, isHierarchicalSearch);
    }

    @Override
    public FileTransferEngine getFileTransferEngine() {
        return target.getFileTransferEngine();
    }

    @Override
    public ServerConnector getJMXServerConnector() {
        return target.getJMXServerConnector();
    }

    @Override
    public String getJobID(String nodeUrl) throws ProActiveException {
        return target.getJobID(nodeUrl);
    }

    @Override
    public String[] getLocalNodeNames() throws ProActiveException {
        return target.getLocalNodeNames();
    }

    @Override
    public String getLocalNodeProperty(String nodeName, String key)
        throws ProActiveException {
        return target.getLocalNodeProperty(nodeName, key);
    }

    @Override
    public ProActiveRuntimeWrapperMBean getMBean() {
        return target.getMBean();
    }

    @Override
    public String getMBeanServerName() {
        return target.getMBeanServerName();
    }

    @Override
    public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName)
        throws ProActiveException {
        return target.getProActiveRuntime(proActiveRuntimeName);
    }

    @Override
    public ProActiveRuntime[] getProActiveRuntimes() throws ProActiveException {
        return target.getProActiveRuntimes();
    }

    @Override
    public String getURL() {
        return target.getURL();
    }

    @Override
    public VMInformation getVMInformation() {
        return this.vmInformation;
    }

    @Override
    public String getVNName(String Nodename) throws ProActiveException {
        return target.getVNName(Nodename);
    }

    @Override
    public VirtualNodeInternal getVirtualNode(String virtualNodeName)
        throws ProActiveException {
        return target.getVirtualNode(virtualNodeName);
    }

    @Override
    public void killAllNodes() throws ProActiveException {
        target.killAllNodes();
    }

    @Override
    public void killNode(String nodeName) throws ProActiveException {
        target.killNode(nodeName);
    }

    @Override
    public void killRT(boolean softly) throws Exception {
        target.killRT(softly);
    }

    @Override
    public void launchMain(String className, String[] parameters)
        throws ClassNotFoundException, NoSuchMethodException, ProActiveException {
        target.launchMain(className, parameters);
    }

    @Override
    public void newRemote(String className)
        throws ClassNotFoundException, ProActiveException {
        target.newRemote(className);
    }

    @Override
    public UniversalBody receiveBody(String nodeName, Body body)
        throws ProActiveException {
        return target.receiveBody(nodeName, body);
    }

    @Override
    public UniversalBody receiveCheckpoint(String nodeName, Checkpoint ckpt,
        int inc) throws ProActiveException {
        return target.receiveCheckpoint(nodeName, ckpt, inc);
    }

    @Override
    public void register(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeUrl, String creatorID, String creationProtocol,
        String vmName) throws ProActiveException {
        target.register(proActiveRuntimeDist, proActiveRuntimeUrl, creatorID,
            creationProtocol, vmName);
    }

    @Override
    public void register(GCMRuntimeRegistrationNotificationData event) {
        target.register(event);
    }

    @Override
    public void registerVirtualNode(String virtualNodeName,
        boolean replacePreviousBinding)
        throws ProActiveException, AlreadyBoundException {
        target.registerVirtualNode(virtualNodeName, replacePreviousBinding);
    }

    @Override
    public void rmAcquaintance(String proActiveRuntimeName)
        throws ProActiveException {
        target.rmAcquaintance(proActiveRuntimeName);
    }

    @Override
    public Object setLocalNodeProperty(String nodeName, String key, String value)
        throws ProActiveException {
        return target.setLocalNodeProperty(nodeName, key, value);
    }

    @Override
    public void startJMXServerConnector() {
        target.startJMXServerConnector();
    }

    @Override
    public void unregister(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeUrl, String creatorID, String creationProtocol,
        String vmName) throws ProActiveException {
        target.unregister(proActiveRuntimeDist, proActiveRuntimeUrl, creatorID,
            creationProtocol, vmName);
    }

    @Override
    public void unregisterAllVirtualNodes() throws ProActiveException {
        target.unregisterAllVirtualNodes();
    }

    @Override
    public void unregisterVirtualNode(String virtualNodeName)
        throws ProActiveException {
        target.unregisterVirtualNode(virtualNodeName);
    }

    @Override
    public TypedCertificate getCertificate()
        throws SecurityNotAvailableException, IOException {
        return target.getCertificate();
    }

    @Override
    public Entities getEntities()
        throws SecurityNotAvailableException, IOException {
        return target.getEntities();
    }

    @Override
    public SecurityContext getPolicy(Entities local, Entities distant)
        throws SecurityNotAvailableException, IOException {
        return target.getPolicy(local, distant);
    }

    @Override
    public ProActiveSecurityManager getProActiveSecurityManager(Entity user)
        throws SecurityNotAvailableException, AccessControlException,
            IOException {
        return target.getProActiveSecurityManager(user);
    }

    @Override
    public PublicKey getPublicKey()
        throws SecurityNotAvailableException, IOException {
        return target.getPublicKey();
    }

    @Override
    public byte[] publicKeyExchange(long sessionID, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            KeyExchangeException, IOException {
        return target.publicKeyExchange(sessionID, signature);
    }

    @Override
    public byte[] randomValue(long sessionID, byte[] clientRandomValue)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        return target.randomValue(sessionID, clientRandomValue);
    }

    @Override
    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey,
        byte[] encodedIVParameters, byte[] encodedClientMacKey,
        byte[] encodedLockData, byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        return target.secretKeyExchange(sessionID, encodedAESKey,
            encodedIVParameters, encodedClientMacKey, encodedLockData,
            parametersSignature);
    }

    @Override
    public void setProActiveSecurityManager(Entity user,
        PolicyServer policyServer)
        throws SecurityNotAvailableException, AccessControlException,
            IOException {
        target.setProActiveSecurityManager(user, policyServer);
    }

    @Override
    public long startNewSession(long distantSessionID, SecurityContext policy,
        TypedCertificate distantCertificate)
        throws SessionException, SecurityNotAvailableException, IOException {
        return target.startNewSession(distantSessionID, policy,
            distantCertificate);
    }

    @Override
    public void terminateSession(long sessionID)
        throws SecurityNotAvailableException, IOException {
        target.terminateSession(sessionID);
    }
}
