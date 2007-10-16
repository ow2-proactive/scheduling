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
package org.objectweb.proactive.core.body.ft.servers.location;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;


/**
 * An object implementing this interface provides location services.
 * This server is an RMI object.
 * @author cdelbe
 * @since ProActive 2.2
 */
public interface LocationServer extends Remote {

    /**
     * Return the current location of object id.
     * @param id Unique id of the searched object
     * @param oldLocation last known location of the searched object
     * @return the new location of the searched object
     */
    public UniversalBody searchObject(UniqueID id, UniversalBody oldLocation,
        UniqueID caller) throws RemoteException;

    /**
     * Set the new location of the active object identified by id.
     * Call register in the recovery process.
     * @param id id of the caller
     * @param newLocation new location of the caller. If this location is null,
     * the body id is removed from the location table.
     */
    public void updateLocation(UniqueID id, UniversalBody newLocation)
        throws RemoteException;

    /**
     * Return the list of locations of all registered bodies.
     * @return the list of locations of all registered bodies.
     */
    public List<UniversalBody> getAllLocations() throws RemoteException;

    /**
     * Return the current known location of a registred body.
     * @return the current known location of a registred body.
     */
    public UniversalBody getLocation(UniqueID id) throws RemoteException;

    /**
     * Reinit the state of the location server.
     */
    public void initialize() throws RemoteException;
    ;
}
