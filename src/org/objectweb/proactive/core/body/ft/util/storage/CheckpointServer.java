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
package org.objectweb.proactive.core.body.ft.util.storage;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.body.ft.checkpointing.CheckpointInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * An object implementiong this interface provides services for storing and retreiving
 * checkpoints and checkpointInfos. Its provides also a classserver used during deserialization
 * of checkpoints for a recovery.
 * This server is an RMI object.
 * @author cdelbe
 * @since ProActive 2.2
 */
public interface CheckpointServer extends Remote {

    /**
     * Store a checkpoint in the checkpoint server.
     * @param c the checkpoint to stored
     * @return the last global state of the system, i.e. the index of the latest completed
     * image of the system.
     * @throws RemoteException
     */
    public int storeCheckpoint(Checkpoint c) throws RemoteException;

    /**
     * Return a checkpoint of the object identified by id.
     * @param id the owner of the returned checkpoint
     * @param sequenceNumber the index of the requiered checkpoint
     * @return a checkpoint of the object identified by id
     * @throws RemoteException
     */
    public Checkpoint getCheckpoint(UniqueID id, int sequenceNumber)
        throws RemoteException;

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
     * @throws RemoteException
     */
    public void addInfoToCheckpoint(CheckpointInfo ci, UniqueID id,
        int sequenceNumber) throws RemoteException;

    /**
     * Return informations on the given checkpoint
     * @param id owner of the considered checkpoint
     * @param sequenceNumber index of the considered checkpoint
     * @return informations on the given checkpoint
     * @throws RemoteException
     */
    public CheckpointInfo getInfoFromCheckpoint(UniqueID id, int sequenceNumber)
        throws RemoteException;

    /**
     * Return the URL of the classServer linked to this checkpointServer. This classServer is used
     * by recovering active objects
     * @return the URL of the classServer linked to this checkpointServer
     * @throws RemoteException
     */
    public String getServerCodebase() throws RemoteException;
}
