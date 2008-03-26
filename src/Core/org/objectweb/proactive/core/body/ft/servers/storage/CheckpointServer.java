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
package org.objectweb.proactive.core.body.ft.servers.storage;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.body.ft.checkpointing.CheckpointInfo;
import org.objectweb.proactive.core.body.ft.message.HistoryUpdater;
import org.objectweb.proactive.core.body.ft.message.MessageInfo;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;


/**
 * An object implementiong this interface provides services for storing and retreiving
 * checkpoints and checkpointInfos. Its provides also a classserver used during deserialization
 * of checkpoints for a recovery.
 * This server is an RMI object.
 * @author The ProActive Team
 * @since ProActive 2.2
 */
public interface CheckpointServer extends Remote {
    // CHECKPOINTING

    /**
     * Store a checkpoint in the checkpoint server.
     * @param c the checkpoint to stored
     * @param incarnation incarnation number of the caller
     * @return the last global state of the system, i.e. the index of the latest completed
     * image of the system.
     * @throws RemoteException
     */
    public int storeCheckpoint(Checkpoint c, int incarnation) throws RemoteException;

    /**
     * Return a checkpoint of the object identified by id.
     * @param id the owner of the returned checkpoint
     * @param sequenceNumber the index of the requiered checkpoint
     * @return a checkpoint of the object identified by id
     * @throws RemoteException
     */
    public Checkpoint getCheckpoint(UniqueID id, int sequenceNumber) throws RemoteException;

    /**
     * Return the latest checkpoint of the object identified by id
     * @param id the owner of the returned checkpoint
     * @return the latest checkpoint of the object identified by id
     * @throws RemoteException
     */
    public Checkpoint getLastCheckpoint(UniqueID id) throws RemoteException;

    /**
     * Add informations to an already stored checkpoint
     * @param ci informations that have to be added
     * @param id owner of the considered checkpoint
     * @param sequenceNumber index of the considered checkpoint
     * @param incarnation incarnation number of the caller
     * @throws RemoteException
     */
    public void addInfoToCheckpoint(CheckpointInfo ci, UniqueID id, int sequenceNumber, int incarnation)
            throws RemoteException;

    /**
     * Return informations on the given checkpoint
     * @param id owner of the considered checkpoint
     * @param sequenceNumber index of the considered checkpoint
     * @return informations on the given checkpoint
     * @throws RemoteException
     */
    public CheckpointInfo getInfoFromCheckpoint(UniqueID id, int sequenceNumber) throws RemoteException;

    /**
     * Add an history to a checkpoint. Informations about the corresponding checkpoint
     * are stored in the HistoryUpdater object.
     * @param rh the history updater.
     * @throws RemoteException
     */
    public void commitHistory(HistoryUpdater rh) throws RemoteException;

    /**
     * The state of the system must be commited before the sent of the message linked to
     * the messageInfo mi.
     * @param mi the message information linked to the message that is sent to the outside world
     * @throws RemoteException
     */
    public void outputCommit(MessageInfo mi) throws RemoteException;

    /**
     * Return the URL of the classServer linked to this checkpointServer. This classServer is used
     * by recovering active objects
     * @return the URL of the classServer linked to this checkpointServer
     * @throws RemoteException
     */
    public String getServerCodebase() throws RemoteException;

    // MESSAGE LOGGING

    /**
     * The request passed in paramter must be synchronously logged on the server. It must
     * be associated with the ID passed in parameter.
     * @param receiverId the ID associated to the request.
     * @param request the request to log.
     * @throws RemoteException If communication with server fails.
     */
    public void storeRequest(UniqueID receiverId, Request request) throws RemoteException;

    /**
     * The reply passed in paramter must be synchronously logged on the server. It must
     * be associated with the ID passed in parameter.
     * @param receiverID the ID associated to the reply.
     * @param reply the reply to log.
     * @throws RemoteException If communication with server fails.
     */
    public void storeReply(UniqueID receiverID, Reply reply) throws RemoteException;

    /**
     * Reinit the state of the location server.
     */
    public void initialize() throws RemoteException;
}
