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
import java.lang.reflect.InvocationTargetException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueRuntimeID;
import org.objectweb.proactive.core.body.BodyAdapterForwarder;
import org.objectweb.proactive.core.body.BodyForwarderImpl;
import org.objectweb.proactive.core.body.RemoteBodyForwarder;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.body.rmi.RmiRemoteBodyForwarderImpl;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.HierarchicalProcess;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.security.Communication;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entity;
import org.objectweb.proactive.core.ssh.rmissh.SshRMIClientSocketFactory;
import org.objectweb.proactive.core.ssh.rmissh.SshRMIServerSocketFactory;


/**
 * Implementation of ProActiveRuntimeForwarder
 *
 * @author ProActive Team
 * @see ProActiveRuntimeForwarder
 */
public class ProActiveRuntimeForwarderImpl extends ProActiveRuntimeImpl
    implements ProActiveRuntimeForwarder, LocalProActiveRuntime {

    /** All runtimes known by this forwarder
     * <UniqueRuntimeID, ProActiveRuntimeAdapter> */
    protected ConcurrentHashMap<UniqueRuntimeID, ProActiveRuntime> registeredRuntimes;

    /** Processes to deploy
     * <String, ExternalProcess> */
    private HashMap<Object, ExternalProcess> hierarchicalProcesses;

    /** The parent of this runtime */
    private ProActiveRuntime parentRuntime = null;

    /** The BodyForwarder associated to this ProActiveRuntimeForwarder
     * There is one and only one BodyForwarder per RuntimeForwarder
     * (remember a RuntimeForwarder IS a Runtime, a BodyForwarder IS NOT a Body)
     */
    private BodyForwarderImpl bodyForwarder = null;
    private BodyAdapterForwarder bodyAdapterForwarder = null;
    private RemoteBodyForwarder remoteBodyForwarder = null;

    protected ProActiveRuntimeForwarderImpl() {
        super();
        registeredRuntimes = new ConcurrentHashMap<UniqueRuntimeID, ProActiveRuntime>();
        hierarchicalProcesses = new HashMap<Object, ExternalProcess>();
        bodyForwarder = new BodyForwarderImpl();

        // Create the BodyForwarder, protocol specific
        if (Constants.IBIS_PROTOCOL_IDENTIFIER.equals(
                    ProActiveConfiguration.getInstance()
                                              .getProperty(Constants.PROPERTY_PA_COMMUNICATION_PROTOCOL))) {
            if (logger.isDebugEnabled()) {
                logger.debug("Factory is " +
                    Constants.IBIS_PROTOCOL_IDENTIFIER);
            }

            logger.info("Ibis forwarding not yet implemented");

            // TODO support Ibis forwarding
        } else if (Constants.XMLHTTP_PROTOCOL_IDENTIFIER.equals(
                    ProActiveConfiguration.getInstance()
                                              .getProperty(Constants.PROPERTY_PA_COMMUNICATION_PROTOCOL))) {
            if (logger.isDebugEnabled()) {
                logger.debug("Factory is " +
                    Constants.XMLHTTP_PROTOCOL_IDENTIFIER);
            }

            logger.info("Http forwarding not yet implemented");

            // TODO support Http forwarding
        } else if (Constants.RMISSH_PROTOCOL_IDENTIFIER.equals(
                    ProActiveConfiguration.getInstance()
                                              .getProperty(Constants.PROPERTY_PA_COMMUNICATION_PROTOCOL))) {
            if (logger.isDebugEnabled()) {
                logger.debug("Factory is rmissh");
            }

            try {
                remoteBodyForwarder = new RmiRemoteBodyForwarderImpl(bodyForwarder,
                        new SshRMIServerSocketFactory(),
                        new SshRMIClientSocketFactory());
            } catch (RemoteException e) {
                logger.info("Local forwarder cannot be created.");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Factory is " + Constants.RMI_PROTOCOL_IDENTIFIER);
            }

            try {
                remoteBodyForwarder = new RmiRemoteBodyForwarderImpl(bodyForwarder);
            } catch (RemoteException e) {
                logger.info("Local forwarder cannot be created.");
            }
        }

        bodyAdapterForwarder = new BodyAdapterForwarder(remoteBodyForwarder);
    }

    /* Some methods in proactive.core.body package need to get acces to the BodyForwarder
     * currently running, so the three following methods are public
     */
    public BodyAdapterForwarder getBodyAdapterForwarder() {
        return bodyAdapterForwarder;
    }

    public BodyForwarderImpl getBodyForwarder() {
        return bodyForwarder;
    }

    public RemoteBodyForwarder getRemoteBodyForwarder() {
        return remoteBodyForwarder;
    }

    public boolean isRoot() {
        String val = ProActiveConfiguration.getInstance()
                                           .getProperty("proactive.hierarchicalRuntime");

        return ((val != null) && val.equals("root"));
    }

    //
    // --- OVERIDING SOME METHODS
    //
    @Override
    public void setParent(ProActiveRuntime parentPARuntime) {
        this.parentRuntime = parentPARuntime;
        super.setParent(parentPARuntime);
    }

    @Override
    public void register(ProActiveRuntime proActiveRuntimeDist,
        String proActiveRuntimeName, String creatorID, String creationProtocol,
        String vmName) {

        /* Act like a forwarder even if it's not a prefixed method.
         * A forwarder is fully transparent, so a bootstrap is needed. A forwarder insert itself
         * in the chain by intercepting register calls of deployed process
         */
        try {
            // erk ! 
            ProActiveRuntimeAdapterForwarderImpl adapter = new ProActiveRuntimeAdapterForwarderImpl((ProActiveRuntimeAdapterForwarderImpl) RuntimeFactory.getDefaultRuntime(),
                    proActiveRuntimeDist);

            if (parentRuntime != null) {
                parentRuntime.register(adapter, proActiveRuntimeName,
                    creatorID, creationProtocol, vmName);
            } else {
                if (isRoot()) {
                    super.register(proActiveRuntimeDist, proActiveRuntimeName,
                        creatorID, creationProtocol, vmName);
                } else {
                    logger.warn(
                        "setParent() as not yet be called. Cannot forward the registration");
                }
            }
        } catch (ProActiveException e) {
            e.printStackTrace();
            logger.warn("Cannot register this runtime: " +
                proActiveRuntimeName);
        }
    }

    static private Object buildKey(String padURL, String vmName) {
        return padURL + "??" + vmName;
    }

    @Override
    public ExternalProcess getProcessToDeploy(
        ProActiveRuntime proActiveRuntimeDist, String creatorID, String vmName,
        String padURL) throws ProActiveException {
        if (this.isRoot()) {
            return super.getProcessToDeploy(proActiveRuntimeDist, creatorID,
                vmName, padURL);
        } else {

            /* Used only in multi-heriarchical deployment. */
            HierarchicalProcess hp = (HierarchicalProcess) hierarchicalProcesses.get(buildKey(
                        padURL, vmName));

            if (hp != null) {
                return hp.getHierarchicalProcess();
            } else {
                return null;
            }
        }
    }

    /**
     * Add process to process list to be hierarchically deployed,
     * if we launched a forwarder it will ask for it using register().
     * @param padURL  URL of the ProActive Descriptor
     * @param vmName  Virtual Machine associated to process
     * @param process The process
     */
    protected void setProcessesToDeploy(String padURL, String vmName,
        ExternalProcess process) {
        JVMProcess jvmProcess = (JVMProcess) process.getFinalProcess();
        jvmProcess.resetParameters();
        hierarchicalProcesses.put(buildKey(padURL, vmName), process);
    }

    public UniversalBody createBody(UniqueRuntimeID urid, String nodeName,
        ConstructorCall bodyConstructorCall, boolean isNodeLocal)
        throws ProActiveException, ConstructorCallExecutionFailedException,
            InvocationTargetException {
        if (urid == null) {
            return this.createBody(nodeName, bodyConstructorCall, isNodeLocal);
        }

        ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

        if (part == null) {
            logErrorAndDumpTable(urid);

            return null;
        }

        UniversalBody rBody = part.createBody(nodeName, bodyConstructorCall,
                isNodeLocal);

        bodyForwarder.addcreatedBody(rBody.getID());

        return rBody;
    }

    //
    // --- MULTIPLEXER ---
    //
    public String createLocalNode(UniqueRuntimeID urid, String nodeName,
        boolean replacePreviousBinding, ProActiveSecurityManager psm,
        String vnName, String jobId)
        throws NodeException, AlreadyBoundException {
        if (urid == null) {
            return this.createLocalNode(nodeName, replacePreviousBinding, psm,
                vnName, jobId);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.createLocalNode(nodeName, replacePreviousBinding,
                    psm, vnName, jobId);
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public void killAllNodes(UniqueRuntimeID urid) throws ProActiveException {
        if (urid == null) {
            this.killAllNodes();
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                part.killAllNodes();
            } else {
                logErrorAndDumpTable(urid);
            }
        }
    }

    public void killNode(UniqueRuntimeID urid, String nodeName)
        throws ProActiveException {
        if (urid == null) {
            this.killNode(nodeName);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                part.killNode(nodeName);
            } else {
                logErrorAndDumpTable(urid);
            }
        }
    }

    public void createVM(UniqueRuntimeID urid, UniversalProcess remoteProcess)
        throws IOException, ProActiveException {
        if (urid == null) {
            this.createVM(remoteProcess);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                part.createVM(remoteProcess);
            } else {
                logErrorAndDumpTable(urid);
            }
        }
    }

    public String[] getLocalNodeNames(UniqueRuntimeID urid)
        throws ProActiveException {
        if (urid == null) {
            return this.getLocalNodeNames();
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.getLocalNodeNames();
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public VMInformation getVMInformation(UniqueRuntimeID urid) {
        if (urid == null) {
            return this.getVMInformation();
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.getVMInformation();
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public void register(UniqueRuntimeID urid,
        ProActiveRuntime proActiveRuntimeDist, String proActiveRuntimeUrl,
        String creatorID, String creationProtocol, String rurid)
        throws ProActiveException {
        if (urid == null) {
            this.register(proActiveRuntimeDist, proActiveRuntimeUrl, creatorID,
                creationProtocol, rurid);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                part.register(proActiveRuntimeDist, proActiveRuntimeUrl,
                    creatorID, creationProtocol, rurid);
            } else {
                logErrorAndDumpTable(urid);
            }
        }
    }

    public void unregister(UniqueRuntimeID urid,
        ProActiveRuntime proActiveRuntimeDist, String proActiveRuntimeUrl,
        String creatorID, String creationProtocol, String rurid)
        throws ProActiveException {
        if (urid == null) {
            this.unregister(proActiveRuntimeDist, proActiveRuntimeUrl,
                creatorID, creationProtocol, rurid);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                part.unregister(proActiveRuntimeDist, proActiveRuntimeUrl,
                    creatorID, creationProtocol, rurid);
            } else {
                logErrorAndDumpTable(urid);
            }
        }
    }

    public ProActiveRuntime[] getProActiveRuntimes(UniqueRuntimeID urid)
        throws ProActiveException {
        if (urid == null) {
            return this.getProActiveRuntimes();
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.getProActiveRuntimes();
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public ProActiveRuntime getProActiveRuntime(UniqueRuntimeID urid,
        String proActiveRuntimeName) throws ProActiveException {
        if (urid == null) {
            return this.getProActiveRuntime(proActiveRuntimeName);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.getProActiveRuntime(proActiveRuntimeName);
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public void addAcquaintance(UniqueRuntimeID urid,
        String proActiveRuntimeName) throws ProActiveException {
        if (urid == null) {
            this.addAcquaintance(proActiveRuntimeName);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                part.addAcquaintance(proActiveRuntimeName);
            } else {
                logErrorAndDumpTable(urid);
            }
        }
    }

    public String[] getAcquaintances(UniqueRuntimeID urid)
        throws ProActiveException {
        if (urid == null) {
            return this.getAcquaintances();
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.getAcquaintances();
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public void rmAcquaintance(UniqueRuntimeID urid, String proActiveRuntimeName)
        throws ProActiveException {
        if (urid == null) {
            this.rmAcquaintance(proActiveRuntimeName);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                part.rmAcquaintance(proActiveRuntimeName);
            } else {
                logErrorAndDumpTable(urid);
            }
        }
    }

    public void killRT(UniqueRuntimeID urid, boolean softly)
        throws Exception {
        if (urid == null) {
            this.killRT(softly);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                part.killRT(softly);
            } else {
                logErrorAndDumpTable(urid);
            }
        }
    }

    public String getURL(UniqueRuntimeID urid) {
        if (urid == null) {
            return this.getURL();
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.getURL();
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public ArrayList getActiveObjects(UniqueRuntimeID urid, String nodeName)
        throws ProActiveException {
        if (urid == null) {
            return this.getActiveObjects(nodeName);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.getActiveObjects(nodeName);
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public ArrayList getActiveObjects(UniqueRuntimeID urid, String nodeName,
        String className) throws ProActiveException {
        if (urid == null) {
            return this.getActiveObjects(nodeName, className);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.getActiveObjects(nodeName, className);
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public VirtualNode getVirtualNode(UniqueRuntimeID urid,
        String virtualNodeName) throws ProActiveException {
        if (urid == null) {
            return this.getVirtualNode(virtualNodeName);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.getVirtualNode(virtualNodeName);
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public void registerVirtualNode(UniqueRuntimeID urid,
        String virtualNodeName, boolean replacePreviousBinding)
        throws ProActiveException, AlreadyBoundException {
        if (urid == null) {
            this.registerVirtualNode(virtualNodeName, replacePreviousBinding);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                part.registerVirtualNode(virtualNodeName, replacePreviousBinding);
            } else {
                logErrorAndDumpTable(urid);
            }
        }
    }

    public void unregisterVirtualNode(UniqueRuntimeID urid,
        String virtualNodeName) throws ProActiveException {
        if (urid == null) {
            this.unregisterVirtualNode(virtualNodeName);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                part.unregisterVirtualNode(virtualNodeName);
            } else {
                logErrorAndDumpTable(urid);
            }
        }
    }

    public void unregisterAllVirtualNodes(UniqueRuntimeID urid)
        throws ProActiveException {
        if (urid == null) {
            this.unregisterAllVirtualNodes();
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                part.unregisterAllVirtualNodes();
            } else {
                logErrorAndDumpTable(urid);
            }
        }
    }

    public String getJobID(UniqueRuntimeID urid, String nodeUrl)
        throws ProActiveException {
        if (urid == null) {
            return this.getJobID(nodeUrl);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.getJobID(nodeUrl);
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public UniversalBody receiveBody(UniqueRuntimeID urid, String nodeName,
        Body body) throws ProActiveException {
        if (urid == null) {
            return this.receiveBody(nodeName, body);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.receiveBody(nodeName, body);
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public UniversalBody receiveCheckpoint(UniqueRuntimeID urid,
        String nodeURL, Checkpoint ckpt, int inc) throws ProActiveException {
        if (urid == null) {
            return this.receiveCheckpoint(nodeURL, ckpt, inc);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.receiveCheckpoint(nodeURL, ckpt, inc);
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public ExternalProcess getProcessToDeploy(UniqueRuntimeID urid,
        ProActiveRuntime proActiveRuntimeDist, String creatorID, String vmName,
        String padURL) throws ProActiveException {
        if (urid == null) {
            return this.getProcessToDeploy(proActiveRuntimeDist, creatorID,
                vmName, padURL);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.getProcessToDeploy(proActiveRuntimeDist, creatorID,
                    vmName, padURL);
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public String getVNName(UniqueRuntimeID urid, String Nodename)
        throws ProActiveException {
        if (urid == null) {
            return this.getVNName(Nodename);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.getVNName(Nodename);
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public ArrayList<Entity> getEntities(UniqueRuntimeID urid)
        throws IOException, SecurityNotAvailableException {
        if (urid == null) {
            return this.getEntities();
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.getEntities();
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public SecurityContext getPolicy(UniqueRuntimeID urid, SecurityContext sc)
        throws SecurityNotAvailableException, IOException {
        if (urid == null) {
            return this.getPolicy(sc);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.getPolicy(sc);
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public byte[] getClassDataFromThisRuntime(UniqueRuntimeID urid,
        String className) throws ProActiveException {
        if (urid == null) {
            return this.getClassDataFromThisRuntime(className);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.getClassDataFromThisRuntime(className);
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public byte[] getClassDataFromParentRuntime(UniqueRuntimeID urid,
        String className) throws ProActiveException {
        if (urid == null) {
            return this.getClassDataFromParentRuntime(className);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.getClassDataFromParentRuntime(className);
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public X509Certificate getCertificate(UniqueRuntimeID urid)
        throws SecurityNotAvailableException, IOException {
        if (urid == null) {
            return this.getCertificate();
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.getCertificate();
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public byte[] getCertificateEncoded(UniqueRuntimeID urid)
        throws SecurityNotAvailableException, IOException {
        if (urid == null) {
            return this.getCertificateEncoded();
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.getCertificateEncoded();
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public PublicKey getPublicKey(UniqueRuntimeID urid)
        throws SecurityNotAvailableException, IOException {
        if (urid == null) {
            return this.getPublicKey();
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.getPublicKey();
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public byte[][] publicKeyExchange(UniqueRuntimeID urid, long sessionID,
        byte[] myPublicKey, byte[] myCertificate, byte[] signature)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            KeyExchangeException, IOException {
        if (urid == null) {
            return this.publicKeyExchange(sessionID, myPublicKey,
                myCertificate, signature);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.publicKeyExchange(sessionID, myPublicKey,
                    myCertificate, signature);
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public byte[] randomValue(UniqueRuntimeID urid, long sessionID,
        byte[] clientRandomValue)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        if (urid == null) {
            return this.randomValue(sessionID, clientRandomValue);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.randomValue(sessionID, clientRandomValue);
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public byte[][] secretKeyExchange(UniqueRuntimeID urid, long sessionID,
        byte[] encodedAESKey, byte[] encodedIVParameters,
        byte[] encodedClientMacKey, byte[] encodedLockData,
        byte[] parametersSignature)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        if (urid == null) {
            return this.secretKeyExchange(sessionID, encodedAESKey,
                encodedIVParameters, encodedClientMacKey, encodedLockData,
                parametersSignature);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.secretKeyExchange(sessionID, encodedAESKey,
                    encodedIVParameters, encodedClientMacKey, encodedLockData,
                    parametersSignature);
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public long startNewSession(UniqueRuntimeID urid, Communication policy)
        throws SecurityNotAvailableException, RenegotiateSessionException,
            IOException {
        if (urid == null) {
            return this.startNewSession(policy);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.startNewSession(policy);
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return 0;
    }

    public void terminateSession(UniqueRuntimeID urid, long sessionID)
        throws SecurityNotAvailableException, IOException {
        if (urid == null) {
            this.terminateSession(sessionID);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                part.terminateSession(sessionID);
            } else {
                logErrorAndDumpTable(urid);
            }
        }
    }

    public ProActiveDescriptor getDescriptor(UniqueRuntimeID urid, String url,
        boolean isHierarchicalSearch) throws IOException, ProActiveException {
        if (urid == null) {
            return this.getDescriptor(url, isHierarchicalSearch);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.getDescriptor(url, isHierarchicalSearch);
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public String getJobID(UniqueRuntimeID urid) {
        if (urid == null) {
            return this.getJobID();
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                return part.getJobID();
            } else {
                logErrorAndDumpTable(urid);
            }
        }

        return null;
    }

    public void launchMain(UniqueRuntimeID urid, String className,
        String[] parameters)
        throws ClassNotFoundException, NoSuchMethodException, ProActiveException {
        if (urid == null) {
            this.launchMain(className, parameters);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                part.launchMain(className, parameters);
            } else {
                logErrorAndDumpTable(urid);
            }
        }
    }

    public void newRemote(UniqueRuntimeID urid, String className)
        throws ClassNotFoundException, ProActiveException {
        if (urid == null) {
            this.newRemote(className);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(urid);

            if (part != null) {
                part.newRemote(className);
            } else {
                logErrorAndDumpTable(urid);
            }
        }
    }

    public Object setLocalNodeProperty(UniqueRuntimeID runtimeID,
        String nodeName, String key, String value)
        throws ProActiveException, IOException {
        if (runtimeID == null) {
            return this.setLocalNodeProperty(nodeName, key, value);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(runtimeID);

            if (part != null) {
                return part.setLocalNodeProperty(nodeName, key, value);
            } else {
            }
        }
        return null;
    }

    public void logErrorAndDumpTable(UniqueRuntimeID runtimeID) {
        logger.warn("No runtime associated to this urid :" + runtimeID);
        logger.warn("Registered runtimes are:");
        for (UniqueRuntimeID urid : registeredRuntimes.keySet()) {
            logger.warn("\t" + urid);
        }
        logger.warn("Associated StackTrace");
        for (StackTraceElement se : Thread.currentThread().getStackTrace()) {
            logger.warn(se);
        }
    }

    public String getLocalNodeProperty(UniqueRuntimeID runtimeID,
        String nodeName, String key) throws ProActiveException, IOException {
        if (runtimeID == null) {
            return this.getLocalNodeProperty(nodeName, key);
        } else {
            ProActiveRuntime part = (ProActiveRuntime) registeredRuntimes.get(runtimeID);

            if (part != null) {
                return part.getLocalNodeProperty(nodeName, key);
            } else {
                logErrorAndDumpTable(runtimeID);
            }
        }
        return null;
    }
}
