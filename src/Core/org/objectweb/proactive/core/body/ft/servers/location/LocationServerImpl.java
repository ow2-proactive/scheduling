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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.servers.FTServer;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * An implementation of the LocationServer
 * @author cdelbe
 */
public class LocationServerImpl implements LocationServer {
    //logger
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.FAULT_TOLERANCE);

    // global server
    private FTServer server;

    // locations table : id <-> adapter
    private Hashtable<UniqueID, UniversalBody> locations;

    public LocationServerImpl(FTServer server) {
        this.server = server;
        this.locations = new Hashtable<UniqueID, UniversalBody>();
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.location.LocationServer#searchObject(org.objectweb.proactive.core.UniqueID, org.objectweb.proactive.core.body.UniversalBody, org.objectweb.proactive.core.UniqueID)
     */
    public UniversalBody searchObject(UniqueID id, UniversalBody oldLocation,
        UniqueID caller) throws RemoteException {
        synchronized (this.locations) {
            UniversalBody currentLocation = this.locations.get(id);
            if (currentLocation == null) {
                logger.error("[LOCATION] **ERROR** " + id +
                    " is not registered !");
                //throw new RuntimeException("TEST");
                return null;
            } else if (currentLocation.equals(oldLocation)) {
                System.out.println(
                    "LocationServerImpl.searchObject() : SEARCHING FOR " + id);
                this.server.forceDetection();
                return null;
            } else {
                // send the new location of id
                logger.debug("[LOCATION] Return the new location of " + id);
                return currentLocation;
            }
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.location.LocationServer#updateLocation(org.objectweb.proactive.core.UniqueID, org.objectweb.proactive.core.body.UniversalBody)
     */
    public void updateLocation(UniqueID id, UniversalBody newLocation)
        throws RemoteException {
        synchronized (this.locations) {
            UniversalBody currentLocation = (this.locations.get(id));
            if (newLocation == null) {
                // the body id is no more localized. Remove it from the location table
                logger.info("[LOCATION] " + id +
                    " is removed from the location table");
                this.locations.remove(id);
                return;
            } else if (currentLocation == null) {
                // register the location
                this.locations.put(id, newLocation);
            } else {
                if (currentLocation.equals(newLocation)) {
                    logger.info("[LOCATION] location of " + id +
                        " is already " + newLocation.getNodeURL());
                } else {
                    logger.info("[LOCATION] " + id +
                        " is updating its location : " +
                        newLocation.getNodeURL());
                    this.locations.put(id, newLocation);
                }
            }
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.location.LocationServer#getAllLocations()
     */
    public List<UniversalBody> getAllLocations() throws RemoteException {
        synchronized (this.locations) {
            return new ArrayList<UniversalBody>(locations.values());
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.location.LocationServer#getLocation(org.objectweb.proactive.core.UniqueID)
     */
    public UniversalBody getLocation(UniqueID id) throws RemoteException {
        synchronized (this.locations) {
            return (this.locations.get(id));
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.servers.location.LocationServer#initialize()
     */
    public void initialize() throws RemoteException {
        this.locations = new Hashtable<UniqueID, UniversalBody>();
    }
}
