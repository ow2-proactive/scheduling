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
package org.objectweb.proactive.core.body.ft.protocols.pmlrb.servers;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.body.ft.checkpointing.CheckpointInfo;
import org.objectweb.proactive.core.body.ft.exception.NotImplementedException;
import org.objectweb.proactive.core.body.ft.message.HistoryUpdater;
import org.objectweb.proactive.core.body.ft.message.MessageInfo;
import org.objectweb.proactive.core.body.ft.protocols.pmlrb.infos.CheckpointInfoPMLRB;
import org.objectweb.proactive.core.body.ft.servers.FTServer;
import org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServerImpl;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class defines a checkpoint server for PMLRB protcol.
 * @author cdelbe
 * @since 3.0
 */
public class CheckpointServerPMLRB extends CheckpointServerImpl {
    //logger
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.FAULT_TOLERANCE_PML);

    /**
     * Constructor.
     * @param server the corresponding global server.
     */
    public CheckpointServerPMLRB(FTServer server) {
        super(server);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#storeCheckpoint(org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint, int)
     */
    public int storeCheckpoint(Checkpoint c, int incarnation)
        throws RemoteException {
        logger.info("[STORAGE] " + c.getBodyID() + " is checkpointing..." +
            " (used memory = " + this.getUsedMem() + " Kb)");
        UniqueID caller = c.getBodyID();
        List<Checkpoint> already = this.checkpointStorage.get(caller);

        // delete old checkpoint if any
        if (already != null) {
            this.checkpointStorage.remove(caller);
        }

        // store new checkpoint
        // type compatibility : must use List of one element
        List<Checkpoint> vc = new Vector<Checkpoint>(1);
        vc.add(c);
        this.checkpointStorage.put(c.getBodyID(), vc);
        // delete message logs if any
        return 0;
    }

    /**
     * Not implemented for this procotol.
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#getCheckpoint(org.objectweb.proactive.core.UniqueID, int)
     */
    public Checkpoint getCheckpoint(UniqueID id, int sequenceNumber)
        throws RemoteException {
        throw new NotImplementedException();
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#getLastCheckpoint(org.objectweb.proactive.core.UniqueID)
     */
    public Checkpoint getLastCheckpoint(UniqueID id) throws RemoteException {
        List<Checkpoint> lck = this.checkpointStorage.get(id);
        if (lck != null) {
            return lck.get(0);
        }
        return null;
    }

    /**
     * Not implemented for this protocol.
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#addInfoToCheckpoint(org.objectweb.proactive.core.body.ft.checkpointing.CheckpointInfo, org.objectweb.proactive.core.UniqueID, int, int)
     */
    public void addInfoToCheckpoint(CheckpointInfo ci, UniqueID id,
        int sequenceNumber, int incarnation) throws RemoteException {
        throw new NotImplementedException();
    }

    /**
     * Not implemented for this protocol.
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#getInfoFromCheckpoint(org.objectweb.proactive.core.UniqueID, int)
     */
    public CheckpointInfo getInfoFromCheckpoint(UniqueID id, int sequenceNumber)
        throws RemoteException {
        throw new NotImplementedException();
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#storeRequest(org.objectweb.proactive.core.UniqueID, org.objectweb.proactive.core.body.request.Request)
     */
    public void storeRequest(UniqueID receiverId, Request request)
        throws RemoteException {
        List<Checkpoint> lck = this.checkpointStorage.get(receiverId);
        if (lck != null) {
            Checkpoint c = this.checkpointStorage.get(receiverId).get(0);
            if (c != null) {
                CheckpointInfoPMLRB ci = (CheckpointInfoPMLRB) (c.getCheckpointInfo());
                ci.addRequest(request);
            }
        }

        //else {
        //System.out.println(" ****** NOT LOGGED REQUEST ****** ");
        //}
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#storeReply(org.objectweb.proactive.core.UniqueID, org.objectweb.proactive.core.body.reply.Reply)
     */
    public void storeReply(UniqueID receiverID, Reply reply)
        throws RemoteException {
        // checkpoint could not be null : if this oa receive a repy, it thus has already
        // served a request, and has checkpointed before.
        CheckpointInfoPMLRB ci = (CheckpointInfoPMLRB) (this.checkpointStorage.get(receiverID)
                                                                              .get(0)).getCheckpointInfo();
        ci.addReply(reply);
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#outputCommit(org.objectweb.proactive.core.body.ft.message.MessageInfo)
     */
    public void outputCommit(MessageInfo mi) throws RemoteException {
        throw new NotImplementedException("outputCommit(MessageInfo mi)");
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer#commitHistory(org.objectweb.proactive.core.body.ft.message.HistoryUpdater)
     */
    public void commitHistory(HistoryUpdater rh) throws RemoteException {
        // nothing to do
    }
}
