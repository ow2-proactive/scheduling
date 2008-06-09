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
package org.objectweb.proactive.core.body.ft.protocols;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.exceptions.BodyTerminatedException;
import org.objectweb.proactive.core.body.ft.checkpointing.CheckpointInfo;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.ft.internalmsg.Heartbeat;
import org.objectweb.proactive.core.body.ft.servers.faultdetection.FaultDetector;
import org.objectweb.proactive.core.body.ft.servers.location.LocationServer;
import org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcess;
import org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer;
import org.objectweb.proactive.core.body.ft.service.FaultToleranceTechnicalService;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.security.exceptions.CommunicationForbiddenException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Define all hook methods for the management of fault-tolerance.
 * @author The ProActive Team
 * @since ProActive 2.2
 */
public abstract class FTManager implements java.io.Serializable {
    //logger
    final protected static Logger logger = ProActiveLogger.getLogger(Loggers.FAULT_TOLERANCE);

    /** This value is sent by an active object that is not fault tolerant*/
    public static final int NON_FT = -30;

    /** This is the default value in ms of the checkpoint interval time */
    public static final int DEFAULT_TTC_VALUE = 30000;

    /** Value returned by an object if the recieved message is served as an immediate service (@see xxx) */
    public static final int IMMEDIATE_SERVICE = -1;

    /** Value returned by an object if the received message is orphan */
    public static final int ORPHAN_REPLY = -2;

    /** Time to wait between a send and a resend in ms*/
    public static final long TIME_TO_RESEND = 3000;

    /** Error message when calling uncallable method on a halfbody */
    public static final String HALF_BODY_EXCEPTION_MESSAGE = "Cannot perform this call on a FTManager of a HalfBody";

    // true is this is a checkpoint
    private boolean isACheckpoint;

    // body attached to this manager
    protected AbstractBody owner;
    protected UniqueID ownerID;

    // server adresses
    protected CheckpointServer storage;
    protected LocationServer location;
    protected RecoveryProcess recovery;

    // additional codebase for checkpoints
    protected String additionalCodebase;

    // checkpoint interval (ms)
    protected int ttc;

    /**
     * Return the selector value for a given protocol.
     * @param protoName the name of the protocol (cic or pml).
     * @return the selector value for a given protocol.
     */
    public static int getProtoSelector(String protoName) {
        if (FTManagerFactory.PROTO_CIC.equals(protoName)) {
            return FTManagerFactory.PROTO_CIC_ID;
        } else if (FTManagerFactory.PROTO_PML.equals(protoName)) {
            return FTManagerFactory.PROTO_PML_ID;
        }
        return 0;
    }

    /**
     * Initialize the FTManager. This method establishes all needed connections with the servers.
     * The owner object is registered in the location server (@see xxx).
     * @param owner The object linked to this FTManager
     * @return still not used
     * @throws ProActiveException A problem occurs during the connection with the servers
     */
    public int init(AbstractBody owner) throws ProActiveException {
        this.owner = owner;
        this.ownerID = owner.getID();
        Node node = NodeFactory.getNode(this.owner.getNodeURL());

        try {
            String ttcValue = node.getProperty(FaultToleranceTechnicalService.TTC);
            if (ttcValue != null) {
                this.ttc = Integer.parseInt(ttcValue) * 1000;
            } else {
                this.ttc = FTManager.DEFAULT_TTC_VALUE;
            }
            String urlGlobal = node.getProperty(FaultToleranceTechnicalService.GLOBAL_SERVER);
            if (urlGlobal != null) {
                this.storage = (CheckpointServer) (Naming.lookup(urlGlobal));
                this.location = (LocationServer) (Naming.lookup(urlGlobal));
                this.recovery = (RecoveryProcess) (Naming.lookup(urlGlobal));
            } else {
                String urlCheckpoint = node.getProperty(FaultToleranceTechnicalService.CKPT_SERVER);
                String urlRecovery = node.getProperty(FaultToleranceTechnicalService.RECOVERY_SERVER);
                String urlLocation = node.getProperty(FaultToleranceTechnicalService.LOCATION_SERVER);
                if ((urlCheckpoint != null) && (urlRecovery != null) && (urlLocation != null)) {
                    this.storage = (CheckpointServer) (Naming.lookup(urlCheckpoint));
                    this.location = (LocationServer) (Naming.lookup(urlLocation));
                    this.recovery = (RecoveryProcess) (Naming.lookup(urlRecovery));
                } else {
                    throw new ProActiveException("Unable to init FTManager : servers are not correctly set");
                }
            }

            // the additional codebase is added to normal codebase
            // ONLY during serialization for checkpoint !
            this.additionalCodebase = this.storage.getServerCodebase();

            // registration in the recovery process and in the localisation server
            try {
                this.recovery.register(ownerID);
                this.location.updateLocation(ownerID, owner.getRemoteAdapter());
            } catch (RemoteException e) {
                logger.error("**ERROR** Unable to register in location server");
                throw new ProActiveException("Unable to register in location server", e);
            }
        } catch (MalformedURLException e) {
            throw new ProActiveException("Unable to init FTManager : FT is disable.", e);
        } catch (RemoteException e) {
            throw new ProActiveException("Unable to init FTManager : FT is disable.", e);
        } catch (NotBoundException e) {
            throw new ProActiveException("Unable to init FTManager : FT is disable.", e);
        }
        return 0;
    }

    /**
     * Unregister this activity from the fault-tolerance mechanism. This method must be called
     * when an active object ends its activity normally.
     */
    public void termination() throws ProActiveException {
        try {
            this.recovery.unregister(this.ownerID);
        } catch (RemoteException e) {
            logger.error("**ERROR** Unable to register in location server");
            throw new ProActiveException("Unable to unregister in location server", e);
        }
    }

    /**
     * Return true if the owner is a checkpoint, i.e. during checkpointing, and on recovery
     * when the owner is deserialized.
     * @return true if the owner is a checkpoint, i.e. during checkpointing, and on recovery
     * when the owner is deserialized, false ohterwise
     */
    public boolean isACheckpoint() {
        return isACheckpoint;
    }

    /**
     * Set the current state of the owner as a checkpoint. Called during checkpoiting.
     * @param tag true during checkpointing, false otherwise
     */
    public void setCheckpointTag(boolean tag) {
        this.isACheckpoint = tag;
    }

    /**
     * Common behavior when a communication with another active object failed.
     * The location server is contacted.
     * @param suspect the uniqueID of the callee
     * @param suspectLocation the supposed location of the callee
     * @param e the exception raised during the communication
     * @return the actual location of the callee
     */
    public UniversalBody communicationFailed(UniqueID suspect, UniversalBody suspectLocation, Exception e) {
        try {
            // send an adapter to suspectLocation: the suspected body could be local
            UniversalBody newLocation = this.location.searchObject(suspect, suspectLocation
                    .getRemoteAdapter(), this.ownerID);

            if (newLocation == null) {
                while (newLocation == null) {
                    try {
                        // suspected is failed or is recovering
                        if (logger.isDebugEnabled()) {
                            logger.debug("[CIC] Waiting for recovery of " + suspect);
                        }
                        Thread.sleep(TIME_TO_RESEND);
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                    newLocation = this.location.searchObject(suspect, suspectLocation.getRemoteAdapter(),
                            this.ownerID);
                }
                return newLocation;
            } else {
                System.out.println("FTManager.communicationFailed() : new location is not null ");

                // newLocation is the new location of suspect
                return newLocation;
            }
        } catch (RemoteException e1) {
            logger.error("**ERROR** Location server unreachable");
            e1.printStackTrace();
            return null;
        }
    }

    /**
     * Fault-tolerant sending: this send notices fault tolerance servers if the destination is
     * unreachable and resent the message until destination is reachable.
     * @param r the reply to send
     * @param destination the destination of the reply
     * @return the value returned by the sending
     */
    public int sendReply(Reply r, UniversalBody destination) {
        try {
            this.onSendReplyBefore(r);
            int res = r.send(destination);
            this.onSendReplyAfter(r, res, destination);
            return res;
        } catch (BodyTerminatedException e) {
            logger.info("[FAULT] " + this.ownerID + " : FAILURE OF " + destination.getID() +
                " SUSPECTED ON REPLY SENDING : " + e.getMessage());
            UniversalBody newDestination = this.communicationFailed(destination.getID(), destination, e);
            return this.sendReply(r, newDestination);
        } catch (IOException e) {
            logger.info("[FAULT] " + this.ownerID + " : FAILURE OF " + destination.getID() +
                " SUSPECTED ON REPLY SENDING : " + e.getMessage());
            UniversalBody newDestination = this.communicationFailed(destination.getID(), destination, e);
            return this.sendReply(r, newDestination);
        }
    }

    /**
     * Fault-tolerant sending: this send notices fault tolerance servers if the destination is
     * unreachable and resent the message until destination is reachable.
     * @param r the request to send
     * @param destination the destination of the request
     * @return the value returned by the sending
     * @throws RenegotiateSessionException
     * @throws CommunicationForbiddenException
     */
    public int sendRequest(Request r, UniversalBody destination) throws RenegotiateSessionException,
            CommunicationForbiddenException {
        try {
            this.onSendRequestBefore(r);
            int res = r.send(destination);
            this.onSendRequestAfter(r, res, destination);
            return res;
        } catch (BodyTerminatedException e) {
            logger.info("[FAULT] " + this.ownerID + " : FAILURE OF " + destination.getID() +
                " SUSPECTED ON REQUEST SENDING : " + e.getMessage());
            UniversalBody newDestination = this.communicationFailed(destination.getID(), destination, e);
            return this.sendRequest(r, newDestination);
        } catch (IOException e) {
            logger.info("[FAULT] " + this.ownerID + " : FAILURE OF " + destination.getID() +
                " SUSPECTED ON REQUEST SENDING : " + e.getMessage());
            UniversalBody newDestination = this.communicationFailed(destination.getID(), destination, e);
            return this.sendRequest(r, newDestination);
        } catch (RenegotiateSessionException e1) {
            throw e1;
        }
    }

    /**
     * Heartbeat message. Send state value to the fault detector.
     * @param fte heartbeat message.
     * @return FaultDetector.OK if active object is alive, FaultDetector.IS_DEAD otherwise.
     */
    public Object handleHBEvent(Heartbeat fte) {
        if (this.owner.isAlive()) {
            return FaultDetector.OK;
        } else {
            return FaultDetector.IS_DEAD;
        }
    }

    //////////////////////
    // ABSTRACT METHODS //
    //////////////////////

    /**
     * This method is called when a reply is received.
     * @param reply the received reply
     */
    public abstract int onReceiveReply(Reply reply);

    /**
     * This method is called when a request is received.
     * @param request the received request
     */
    public abstract int onReceiveRequest(Request request);

    /**
     * This method is called after the future is updated by the reply.
     * @param reply the reply that updates a future
     */
    public abstract int onDeliverReply(Reply reply);

    /**
     * This method is called when a request is stored in the requestqueue
     * @param request the stored request
     */
    public abstract int onDeliverRequest(Request request);

    /**
     * This method is called before the sending of a reply
     * @param reply the reply that will be sent
     */
    public abstract int onSendReplyBefore(Reply reply);

    /**
     * This method is called after the sending of a reply
     * @param reply the sent reply
     * @param rdvValue the value returned by the sending
     * @param destination the destination body of reply
     * @return depends on fault-tolerance protocol
     */
    public abstract int onSendReplyAfter(Reply reply, int rdvValue, UniversalBody destination);

    /**
     * This method is called before the sending of a request
     * @param request the request that will be sent
     * @return depends on fault-tolerance protocol
     */
    public abstract int onSendRequestBefore(Request request);

    /**
     * This method is called after the sending of a request
     * @param request the sent request
     * @param rdvValue the value returned by the sending
     * @param destination the destination body of request
     * @return depends on fault-tolerance protocol
     * @throws RenegotiateSessionException
     * @throws CommunicationForbiddenException
     */
    public abstract int onSendRequestAfter(Request request, int rdvValue, UniversalBody destination)
            throws RenegotiateSessionException, CommunicationForbiddenException;

    /**
     * This method is called before the service of a request
     * @param request the request that is served
     * @return depends on fault-tolerance protocol
     */
    public abstract int onServeRequestBefore(Request request);

    /**
     * This method is called after the service of a request
     * @param request the request that has been served
     * @return depends on fault-tolerance protocol
     */
    public abstract int onServeRequestAfter(Request request);

    /**
     * This method is called before restarting an object which has been recovered
     * from a checkpoint.
     * @param ci infos of the checkpoint used for recovery
     * @param inc incarantion number of this recovery
     * @return depends on fault-tolerance protocol
     */
    public abstract int beforeRestartAfterRecovery(CheckpointInfo ci, int inc);

    /**
     * This method is called when a non fonctionnal fault-tolerance message is received
     * @param fte the received message
     * @return depend on the message meaning
     */
    public abstract Object handleFTMessage(FTMessage fte);

    /**
     * This method is called after a migration to update the object's location
     * @param ownerID the UniversalID of the caller
     * @param remoteBodyAdapter the remotBodyAdapter of the caller
     */
    public void updateLocationAtServer(UniqueID ownerID, UniversalBody remoteBodyAdapter) {
    }
}
