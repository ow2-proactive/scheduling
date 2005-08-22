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
package org.objectweb.proactive.core.body.ft.protocols;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.checkpointing.CheckpointInfo;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.ft.util.location.LocationServer;
import org.objectweb.proactive.core.body.ft.util.recovery.RecoveryProcess;
import org.objectweb.proactive.core.body.ft.util.storage.CheckpointServer;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;


/**
 * Define all hook methods for the management of fault-tolerance.
 * @author cdelbe
 * @since ProActive 2.2
 */
public abstract class FTManager implements java.io.Serializable {

    /** This value is sent by an active object that is not fault tolerant*/
    public static final int NON_FT = -30;

    /** This is the default value in ms of the checkpoint interval time */
    public static final int DEFAULT_TTC_VALUE = 30000;

    /** Value returned by an object if the recieved message is served as an immediate service (@see xxx) */
    public static final int IMMEDIATE_SERVICE = -1;

    /** Value returned by an object if the received message is orphan */
    public static final int ORPHAN_REPLY = -2;

    // true is this is a checkpoint
    private boolean isACheckpoint;

    // body attached to this manager
    public AbstractBody owner;

    // server adresses
    protected CheckpointServer storage;
    protected LocationServer location;
    protected RecoveryProcess recovery;

    // additional codebase for checkpoints
    protected String additionalCodebase;

    // checkpoint interval (ms)
    protected int ttc;

    /**
     * Initialize the FTManager. This method establihes all needed connections with the servers.
     * The owner object is registred in the location server (@see xxx).
     * @param owner The object linked to this FTManager
     * @return still not used
     * @throws ProActiveException A problem occurs during the connection with the servers
     */
    public int init(AbstractBody owner) throws ProActiveException {
        this.owner = owner;
        try {
            String ttcValue = ProActiveConfiguration.getTTCValue();
            if (ttcValue != null) {
                this.ttc = Integer.parseInt(ttcValue) * 1000;
            } else {
                this.ttc = FTManager.DEFAULT_TTC_VALUE;
            }
            String urlGlobal = ProActiveConfiguration.getGlobalFTServer();
            if (urlGlobal != null) {
                this.storage = (CheckpointServer) (Naming.lookup(urlGlobal));
                this.location = (LocationServer) (Naming.lookup(urlGlobal));
                this.recovery = (RecoveryProcess) (Naming.lookup(urlGlobal));
            } else {
                String urlCheckpoint = ProActiveConfiguration.getCheckpointServer();
                String urlRecovery = ProActiveConfiguration.getRecoveryServer();
                String urlLocation = ProActiveConfiguration.getLocationServer();
                if ((urlCheckpoint != null) && (urlRecovery != null) &&
                        (urlLocation != null)) {
                    this.storage = (CheckpointServer) (Naming.lookup(urlCheckpoint));
                    this.location = (LocationServer) (Naming.lookup(urlLocation));
                    this.recovery = (RecoveryProcess) (Naming.lookup(urlRecovery));
                } else {
                    throw new ProActiveException(
                        "Unable to init FTManager : servers are not correctly set");
                }
            }

            // the additional codebase is added to normal codebase 
            // ONLY during serialization for checkpoint !
            this.additionalCodebase = this.storage.getServerCodebase();
        } catch (MalformedURLException e) {
            throw new ProActiveException("Unable to init FTManager : FT is disable.",
                e);
        } catch (RemoteException e) {
            throw new ProActiveException("Unable to init FTManager : FT is disable.",
                e);
        } catch (NotBoundException e) {
            throw new ProActiveException("Unable to init FTManager : FT is disable.",
                e);
        }
        return 0;
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
    public abstract int onSendReplyAfter(Reply reply, int rdvValue,
        UniversalBody destination);

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
     */
    public abstract int onSendRequestAfter(Request request, int rdvValue,
        UniversalBody destination) throws RenegotiateSessionException;

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
     * Fault-tolerant sending: this send notices fault tolerance servers if the destination is
     * unreachable and resent the message until destination is reachable.
     * @param r the request to send
     * @param destination the destination of the request
     * @return the value returned by the sending
     * @throws RenegotiateSessionException
     */
    public abstract int sendRequest(Request r, UniversalBody destination)
        throws RenegotiateSessionException;

    /**
     * Fault-tolerant sending: this send notices fault tolerance servers if the destination is
     * unreachable and resent the message until destination is reachable.
     * @param r the reply to send
     * @param destination the destination of the reply
     * @return the value returned by the sending
     */
    public abstract int sendReply(Reply r, UniversalBody destination);

    /**
     * This method is called when a non fonctionnal fault-tolerance message is received
     * @param fte the received message
     * @return still not used
     */
    public abstract int handleFTMessage(FTMessage fte);
}
