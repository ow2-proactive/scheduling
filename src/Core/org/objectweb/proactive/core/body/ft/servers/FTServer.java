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
package org.objectweb.proactive.core.body.ft.servers;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.body.ft.checkpointing.CheckpointInfo;
import org.objectweb.proactive.core.body.ft.message.HistoryUpdater;
import org.objectweb.proactive.core.body.ft.message.MessageInfo;
import org.objectweb.proactive.core.body.ft.servers.faultdetection.FaultDetector;
import org.objectweb.proactive.core.body.ft.servers.location.LocationServer;
import org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcess;
import org.objectweb.proactive.core.body.ft.servers.resource.ResourceServer;
import org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer;
import org.objectweb.proactive.core.body.ft.servers.util.ActiveQueueJob;
import org.objectweb.proactive.core.body.ft.servers.util.JobBarrier;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This server contains one instance of each needed servers for fault tolerance, and
 * delegates each method call to the concerned subserver.
 * @author The ProActive Team
 * @since 3.0
 */
public class FTServer extends UnicastRemoteObject implements FaultDetector, LocationServer, RecoveryProcess,
        ResourceServer, CheckpointServer {
    //logger
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.FAULT_TOLERANCE);

    /** Default server port */
    public static final int DEFAULT_PORT = 1100;

    /** Period of the failure detector sanning (ms) */
    public static final int DEFAULT_FDETECT_SCAN_PERIOD = 10000;

    /** Default name of this server */
    public static final String DEFAULT_SERVER_NAME = "FTServer";

    // internal servers
    private FaultDetector faultDetector;
    private LocationServer locationServer;
    private RecoveryProcess recoveryProcess;
    private ResourceServer resourceServer;
    private CheckpointServer checkpointServer;

    /**
     * @throws RemoteException
     */
    public FTServer() throws RemoteException {
        super();
        // this.killingQueue.start();
    }

    /**
     * Initialized server with each needed subservers
     *
     */
    public void init(FaultDetector fd, LocationServer ls, RecoveryProcess rp, ResourceServer rs,
            CheckpointServer cs) {
        this.faultDetector = fd;
        this.locationServer = ls;
        this.recoveryProcess = rp;
        this.resourceServer = rs;
        this.checkpointServer = cs;
    }

    //////////////////////////
    ///  Delegated methods  //
    //////////////////////////

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.faultdetection.FaultDetector#isUnreachable(org.objectweb.proactive.core.body.UniversalBody)
     */
    public boolean isUnreachable(UniversalBody body) throws RemoteException {
        return this.faultDetector.isUnreachable(body);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.faultdetection.FaultDetector#startFailureDetector()
     */
    public void startFailureDetector() throws RemoteException {
        this.faultDetector.startFailureDetector();
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.faultdetection.FaultDetector#suspendFailureDetector()
     */
    public void suspendFailureDetector() throws RemoteException {
        this.faultDetector.suspendFailureDetector();
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.faultdetection.FaultDetector#stopFailureDetector()
     */
    public void stopFailureDetector() throws RemoteException {
        this.faultDetector.stopFailureDetector();
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.faultdetection.FaultDetector#forceDetection()
     */
    public void forceDetection() throws RemoteException {
        this.faultDetector.forceDetection();
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.location.LocationServer#searchObject(org.objectweb.proactive.core.UniqueID, org.objectweb.proactive.core.body.UniversalBody, org.objectweb.proactive.core.UniqueID)
     */
    public UniversalBody searchObject(UniqueID id, UniversalBody oldLocation, UniqueID caller)
            throws RemoteException {
        return this.locationServer.searchObject(id, oldLocation, caller);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.location.LocationServer#updateLocation(org.objectweb.proactive.core.UniqueID, org.objectweb.proactive.core.body.UniversalBody)
     */
    public void updateLocation(UniqueID id, UniversalBody newLocation) throws RemoteException {
        this.locationServer.updateLocation(id, newLocation);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.location.LocationServer#getAllLocations()
     */
    public List<UniversalBody> getAllLocations() throws RemoteException {
        return this.locationServer.getAllLocations();
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.location.LocationServer#getLocation(org.objectweb.proactive.core.UniqueID)
     */
    public UniversalBody getLocation(UniqueID id) throws RemoteException {
        return this.locationServer.getLocation(id);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcess#register(org.objectweb.proactive.core.UniqueID)
     */
    public void register(UniqueID id) throws RemoteException {
        this.recoveryProcess.register(id);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcess#unregister(org.objectweb.proactive.core.UniqueID)
     */
    public void unregister(UniqueID id) throws RemoteException {
        this.recoveryProcess.unregister(id);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcess#failureDetected(org.objectweb.proactive.core.UniqueID)
     */
    public void failureDetected(UniqueID id) throws RemoteException {
        this.recoveryProcess.failureDetected(id);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcess#updateState(org.objectweb.proactive.core.UniqueID, int)
     */
    public void updateState(UniqueID id, int state) throws RemoteException {
        this.recoveryProcess.updateState(id, state);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcess#getSystemSize()
     */
    public int getSystemSize() throws RemoteException {
        return this.recoveryProcess.getSystemSize();
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcess#submitJob(org.objectweb.proactive.core.body.ft.servers.util.ActiveQueueJob)
     */
    public void submitJob(ActiveQueueJob job) throws RemoteException {
        this.recoveryProcess.submitJob(job);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcess#submitJobWithBarrier(org.objectweb.proactive.core.body.ft.servers.util.ActiveQueueJob)
     */
    public JobBarrier submitJobWithBarrier(ActiveQueueJob job) throws RemoteException {
        return this.recoveryProcess.submitJobWithBarrier(job);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.resource.ResourceServer#addFreeNode(org.objectweb.proactive.core.node.Node)
     */
    public void addFreeNode(Node n) throws RemoteException {
        this.resourceServer.addFreeNode(n);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.resource.ResourceServer#getFreeNode()
     */
    public Node getFreeNode() throws RemoteException {
        return this.resourceServer.getFreeNode();
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#storeCheckpoint(org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint, int)
     */
    public int storeCheckpoint(Checkpoint c, int incarnation) throws RemoteException {
        return this.checkpointServer.storeCheckpoint(c, incarnation);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#getCheckpoint(org.objectweb.proactive.core.UniqueID, int)
     */
    public Checkpoint getCheckpoint(UniqueID id, int sequenceNumber) throws RemoteException {
        return this.checkpointServer.getCheckpoint(id, sequenceNumber);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#getLastCheckpoint(org.objectweb.proactive.core.UniqueID)
     */
    public Checkpoint getLastCheckpoint(UniqueID id) throws RemoteException {
        return this.checkpointServer.getLastCheckpoint(id);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#addInfoToCheckpoint(org.objectweb.proactive.core.body.ft.checkpointing.CheckpointInfo, org.objectweb.proactive.core.UniqueID, int, int)
     */
    public void addInfoToCheckpoint(CheckpointInfo ci, UniqueID id, int sequenceNumber, int incarnation)
            throws RemoteException {
        this.checkpointServer.addInfoToCheckpoint(ci, id, sequenceNumber, incarnation);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#getInfoFromCheckpoint(org.objectweb.proactive.core.UniqueID, int)
     */
    public CheckpointInfo getInfoFromCheckpoint(UniqueID id, int sequenceNumber) throws RemoteException {
        return this.checkpointServer.getInfoFromCheckpoint(id, sequenceNumber);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#commitHistory(org.objectweb.proactive.core.body.ft.message.HistoryUpdater)
     */
    public void commitHistory(HistoryUpdater rh) throws RemoteException {
        this.checkpointServer.commitHistory(rh);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#outputCommit(org.objectweb.proactive.core.body.ft.message.MessageInfo)
     */
    public void outputCommit(MessageInfo mi) throws RemoteException {
        this.checkpointServer.outputCommit(mi);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#getServerCodebase()
     */
    public String getServerCodebase() throws RemoteException {
        return this.checkpointServer.getServerCodebase();
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#storeRequest(org.objectweb.proactive.core.UniqueID, org.objectweb.proactive.core.body.request.Request)
     */
    public void storeRequest(UniqueID receiverId, Request request) throws RemoteException {
        this.checkpointServer.storeRequest(receiverId, request);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#storeReply(org.objectweb.proactive.core.UniqueID, org.objectweb.proactive.core.body.reply.Reply)
     */
    public void storeReply(UniqueID receiverID, Reply reply) throws RemoteException {
        this.checkpointServer.storeReply(receiverID, reply);
    }

    //////////////////////////
    /// Getters and setters //
    //////////////////////////

    /**
     * @return Returns the checkpointServer.
     */
    public CheckpointServer getCheckpointServer() {
        return checkpointServer;
    }

    /**
     * @param checkpointServer The checkpointServer to set.
     */
    public void setCheckpointServer(CheckpointServer checkpointServer) {
        this.checkpointServer = checkpointServer;
    }

    /**
     * @return Returns the faultDetector.
     */
    public FaultDetector getFaultDetector() {
        return faultDetector;
    }

    /**
     * @param faultDetector The faultDetector to set.
     */
    public void setFaultDetector(FaultDetector faultDetector) {
        this.faultDetector = faultDetector;
    }

    /**
     * @return Returns the locationServer.
     */
    public LocationServer getLocationServer() {
        return locationServer;
    }

    /**
     * @param locationServer The locationServer to set.
     */
    public void setLocationServer(LocationServer locationServer) {
        this.locationServer = locationServer;
    }

    /**
     * @return Returns the recoveryProcess.
     */
    public RecoveryProcess getRecoveryProcess() {
        return recoveryProcess;
    }

    /**
     * @param recoveryProcess The recoveryProcess to set.
     */
    public void setRecoveryProcess(RecoveryProcess recoveryProcess) {
        this.recoveryProcess = recoveryProcess;
    }

    /**
     * @return Returns the resourceServer.
     */
    public ResourceServer getResourceServer() {
        return resourceServer;
    }

    /**
     * @param resourceServer The resourceServer to set.
     */
    public void setResourceServer(ResourceServer resourceServer) {
        this.resourceServer = resourceServer;
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.location.LocationServer#initialize()
     */
    public void initialize() throws RemoteException {
        logger.info("[GLOBAL] Reinitializing server ...");
        this.checkpointServer.initialize();
        this.locationServer.initialize();
        this.recoveryProcess.initialize();
        this.resourceServer.initialize();
        logger.info("[GLOBAL] Server reinitialized");
    }
}
