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
package org.objectweb.proactive.core.body.ft.util.recovery;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * An object implementing this interface provides recovery methods.
 * This server is an RMI object.
 * @author cdelbe
 * @since ProActive 2.2
 */
public interface RecoveryProcess extends Remote {

    /**
     * Active objects possible states.
     */
    public final static int FAILED = 0;
    public final static int RECOVERING = 1;
    public final static int RUNNING = 2;

    /**
     * Register the calling OA. Each OA is registred on creation by newActive.
     * Default state is RUNNING.
     * @param id the registered body id
     */
    public void register(UniqueID id) throws RemoteException;

    /**
     * Notify the recovery process that the body passed in paramater is suspected to be failed.
     * @param id the id of the suspected OA
     */
    public void failureDetected(UniqueID id) throws RemoteException;

    /**
     * Update the current state of the active object id.
     * Its state can be failed, recovering or running.
     * @param id id of the OA to update
     * @param state state of the active object
     */
    public void updateState(UniqueID id, int state) throws RemoteException;

    /**
     * Broadcast the event fte to every activity registered in the Recovery process
     * @param fte the event to broadcast
     * @throws RemoteException
     */
    public void broadcastFTEvent(FTMessage fte) throws RemoteException;
}
