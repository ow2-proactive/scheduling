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
import org.objectweb.proactive.core.descriptor.services.TechnicalService;
import org.objectweb.proactive.core.filetransfer.FileTransferEngine;
import org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean;
import org.objectweb.proactive.core.jmx.notification.GCMRuntimeRegistrationNotificationData;
import org.objectweb.proactive.core.jmx.server.ServerConnector;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.Node;
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
public class ProActiveRuntimeRemoteObjectAdapter extends Adapter<ProActiveRuntime> implements
        ProActiveRuntime {

    /**
     * generated serial uid
     */

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
    public void addAcquaintance(String proActiveRuntimeName) throws ProActiveException {
        target.addAcquaintance(proActiveRuntimeName);
    }

    public UniversalBody createBody(String nodeName, ConstructorCall bodyConstructorCall, boolean isNodeLocal)
            throws ProActiveException, ConstructorCallExecutionFailedException, InvocationTargetException {
        return target.createBody(nodeName, bodyConstructorCall, isNodeLocal);
    }

    public String createLocalNode(String nodeName, boolean replacePreviousBinding,
            ProActiveSecurityManager nodeSecurityManager, String vnName, String jobId) throws NodeException,
            AlreadyBoundException {
        return target.createLocalNode(nodeName, replacePreviousBinding, nodeSecurityManager, vnName, jobId);
    }

    public void createVM(UniversalProcess remoteProcess) throws IOException, ProActiveException {
        target.createVM(remoteProcess);
    }

    public String[] getAcquaintances() throws ProActiveException {
        return target.getAcquaintances();
    }

    public List<UniversalBody> getActiveObjects(String nodeName) throws ProActiveException {
        return target.getActiveObjects(nodeName);
    }

    public List<UniversalBody> getActiveObjects(String nodeName, String className) throws ProActiveException {
        return target.getActiveObjects(nodeName, className);
    }

    public byte[] getClassDataFromParentRuntime(String className) throws ProActiveException {
        return target.getClassDataFromParentRuntime(className);
    }

    public byte[] getClassDataFromThisRuntime(String className) throws ProActiveException {
        return target.getClassDataFromThisRuntime(className);
    }

    public ProActiveDescriptorInternal getDescriptor(String url, boolean isHierarchicalSearch)
            throws IOException, ProActiveException {
        return target.getDescriptor(url, isHierarchicalSearch);
    }

    public FileTransferEngine getFileTransferEngine() {
        return target.getFileTransferEngine();
    }

    public ServerConnector getJMXServerConnector() {
        return target.getJMXServerConnector();
    }

    public String getJobID(String nodeUrl) throws ProActiveException {
        return target.getJobID(nodeUrl);
    }

    public String[] getLocalNodeNames() throws ProActiveException {
        return target.getLocalNodeNames();
    }

    public String getLocalNodeProperty(String nodeName, String key) throws ProActiveException {
        return target.getLocalNodeProperty(nodeName, key);
    }

    public ProActiveRuntimeWrapperMBean getMBean() {
        return target.getMBean();
    }

    public String getMBeanServerName() {
        return target.getMBeanServerName();
    }

    public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName) throws ProActiveException {
        return target.getProActiveRuntime(proActiveRuntimeName);
    }

    public ProActiveRuntime[] getProActiveRuntimes() throws ProActiveException {
        return target.getProActiveRuntimes();
    }

    public String getURL() {
        return target.getURL();
    }

    public VMInformation getVMInformation() {
        return this.vmInformation;
    }

    public String getVNName(String Nodename) throws ProActiveException {
        return target.getVNName(Nodename);
    }

    public VirtualNodeInternal getVirtualNode(String virtualNodeName) throws ProActiveException {
        return target.getVirtualNode(virtualNodeName);
    }

    public void killAllNodes() throws ProActiveException {
        target.killAllNodes();
    }

    public void killNode(String nodeName) throws ProActiveException {
        target.killNode(nodeName);
    }

    public void killRT(boolean softly) throws Exception {
        target.killRT(softly);
    }

    public void launchMain(String className, String[] parameters) throws ClassNotFoundException,
            NoSuchMethodException, ProActiveException {
        target.launchMain(className, parameters);
    }

    public void newRemote(String className) throws ClassNotFoundException, ProActiveException {
        target.newRemote(className);
    }

    public UniversalBody receiveBody(String nodeName, Body body) throws ProActiveException {
        return target.receiveBody(nodeName, body);
    }

    public UniversalBody receiveCheckpoint(String nodeName, Checkpoint ckpt, int inc)
            throws ProActiveException {
        return target.receiveCheckpoint(nodeName, ckpt, inc);
    }

    public void register(ProActiveRuntime proActiveRuntimeDist, String proActiveRuntimeUrl, String creatorID,
            String creationProtocol, String vmName) throws ProActiveException {
        target.register(proActiveRuntimeDist, proActiveRuntimeUrl, creatorID, creationProtocol, vmName);
    }

    public void register(GCMRuntimeRegistrationNotificationData event) {
        target.register(event);
    }

    public void registerVirtualNode(String virtualNodeName, boolean replacePreviousBinding)
            throws ProActiveException, AlreadyBoundException {
        target.registerVirtualNode(virtualNodeName, replacePreviousBinding);
    }

    public void rmAcquaintance(String proActiveRuntimeName) throws ProActiveException {
        target.rmAcquaintance(proActiveRuntimeName);
    }

    public Object setLocalNodeProperty(String nodeName, String key, String value) throws ProActiveException {
        return target.setLocalNodeProperty(nodeName, key, value);
    }

    public void startJMXServerConnector() {
        target.startJMXServerConnector();
    }

    public void unregister(ProActiveRuntime proActiveRuntimeDist, String proActiveRuntimeUrl,
            String creatorID, String creationProtocol, String vmName) throws ProActiveException {
        target.unregister(proActiveRuntimeDist, proActiveRuntimeUrl, creatorID, creationProtocol, vmName);
    }

    public void unregisterAllVirtualNodes() throws ProActiveException {
        target.unregisterAllVirtualNodes();
    }

    public void unregisterVirtualNode(String virtualNodeName) throws ProActiveException {
        target.unregisterVirtualNode(virtualNodeName);
    }

    public TypedCertificate getCertificate() throws SecurityNotAvailableException, IOException {
        return target.getCertificate();
    }

    public Entities getEntities() throws SecurityNotAvailableException, IOException {
        return target.getEntities();
    }

    public SecurityContext getPolicy(Entities local, Entities distant) throws SecurityNotAvailableException,
            IOException {
        return target.getPolicy(local, distant);
    }

    public ProActiveSecurityManager getProActiveSecurityManager(Entity user)
            throws SecurityNotAvailableException, AccessControlException, IOException {
        return target.getProActiveSecurityManager(user);
    }

    public PublicKey getPublicKey() throws SecurityNotAvailableException, IOException {
        return target.getPublicKey();
    }

    public byte[] publicKeyExchange(long sessionID, byte[] signature) throws SecurityNotAvailableException,
            RenegotiateSessionException, KeyExchangeException, IOException {
        return target.publicKeyExchange(sessionID, signature);
    }

    public byte[] randomValue(long sessionID, byte[] clientRandomValue) throws SecurityNotAvailableException,
            RenegotiateSessionException, IOException {
        return target.randomValue(sessionID, clientRandomValue);
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey, byte[] encodedIVParameters,
            byte[] encodedClientMacKey, byte[] encodedLockData, byte[] parametersSignature)
            throws SecurityNotAvailableException, RenegotiateSessionException, IOException {
        return target.secretKeyExchange(sessionID, encodedAESKey, encodedIVParameters, encodedClientMacKey,
                encodedLockData, parametersSignature);
    }

    public void setProActiveSecurityManager(Entity user, PolicyServer policyServer)
            throws SecurityNotAvailableException, AccessControlException, IOException {
        target.setProActiveSecurityManager(user, policyServer);
    }

    public long startNewSession(long distantSessionID, SecurityContext policy,
            TypedCertificate distantCertificate) throws SessionException, SecurityNotAvailableException,
            IOException {
        return target.startNewSession(distantSessionID, policy, distantCertificate);
    }

    public void terminateSession(long sessionID) throws SecurityNotAvailableException, IOException {
        target.terminateSession(sessionID);
    }

    public Node createGCMNode(ProActiveSecurityManager nodeSecurityManager, String vnName, String jobId,
            List<TechnicalService> tsList) throws NodeException, AlreadyBoundException {
        return target.createGCMNode(nodeSecurityManager, vnName, jobId, tsList);
    }
}
