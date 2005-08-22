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
package org.objectweb.proactive.core.body.ft.util.faultdetection;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.util.location.LocationServer;
import org.objectweb.proactive.core.body.ft.util.recovery.RecoveryProcess;


/**
 * An object implementing this interface provides methods to detect failed active objects
 * by sending heartbeat messages. An internal thread can be started so as to periodically
 * test a set of active objects.
 * This server is an RMI object.
 * @author cdelbe
 * @since ProActive 2.2
 */
public interface FaultDetector extends Remote {

    /**
     * This value is return by an alive and reachable active object that receive a
     * heartbeat message.
     */
    public static int OK = 0;

    /**
     * This value is return by a dead but reachable active object that receive a
     * heartbeat message.
     */
    public static int IS_DEAD = 1;

    /**
     * The fault detector test the reachability of the active object body by sending
     * a heartbeat message to body.
     * @param body the tested active object
     * @return true if body is unreachable, false otherwise
     * @throws RemoteException
     */
    public boolean isUnreachable(UniversalBody body) throws RemoteException;

    /**
     * Start the tread that periodically test the reachability of objects that are registred in
     * the location server ls. If a failure is detected, the recovery process must be noticed.
     * @param ls the location server that localizes objects to test
     * @param rp the recovery process to notice if a failure is detected
     * @throws RemoteException
     */
    public void startFailureDetector(LocationServer ls, RecoveryProcess rp)
        throws RemoteException;

    /**
     * Temporarily suspend the failure detector thread.
     * @throws RemoteException
     */
    public void suspendFailureDetector() throws RemoteException;

    /**
     * Stop the the failure detector thread.
     * @throws RemoteException
     */
    public void stopFailureDetector() throws RemoteException;

    /**
     * Force a failure detection even if the failure detector thread is waiting.
     * @throws RemoteException
     */
    public void forceDetection() throws RemoteException;
}
