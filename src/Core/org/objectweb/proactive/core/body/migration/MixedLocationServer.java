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
package org.objectweb.proactive.core.body.migration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.ext.locationserver.LocationServer;


public class MixedLocationServer implements org.objectweb.proactive.RunActive, LocationServer {
    static Logger logger = ProActiveLogger.getLogger(Loggers.MIGRATION);

    /**
     * Delay minimum to send the same version to the same caller
     */
    public static final int DELAY_SAME_REPLY = 1000;
    private LocationMap table;
    private Hashtable requestTable;
    private String url;

    public MixedLocationServer() {
    }

    public MixedLocationServer(String url) {
        this.url = normalizeURL(url);
        this.table = new LocationMap();
        this.requestTable = new Hashtable();
    }

    /**
     * Update the location for the mobile object s with id
     */
    public void updateLocation(UniqueID i, UniversalBody s) {
        updateLocation(i, s, LocationMap.CONSTANT_VERSION);
    }

    /**
     * Update the location for the mobile object s with id and indicating the
     * version v
     */
    public void updateLocation(UniqueID i, UniversalBody s, int version) {
        table.updateBody(i, s, version);
    }

    /**
     * Return a reference to the remote body if available. Return null otherwise
     */
    public UniversalBody searchObject(UniqueID id) {
        //System.out.print("Searching for "+ id);
        UniversalBody u = table.getBody(id);
        return u;
    }

    /**
     * First register with the specified url Then wait for request
     *
     * Serve "updateLocation" by priority put a searchObject in queue and wait
     * DELAY_SAME_REPLY if the version of the requested ID has not changed since
     * the last request
     */
    public void runActivity(org.objectweb.proactive.Body body) {
        this.register();
        Service service = new Service(body);

        while (body.isActive()) {
            while (service.hasRequestToServe("updateLocation")) {
                service.serveOldest("updateLocation");
            }
            Request oldest = service.blockingRemoveOldest();

            if (oldest != null) {
                synchronized (this.requestTable) {
                    if (oldest.getMethodName().equals("searchObject")) {
                        // we have to verify if we have received a request of
                        // the
                        // same sender for the same ID
                        UniqueID id = (UniqueID) oldest.getParameter(0);
                        String requestID = oldest.getSender().getID().toString() + id.toString();

                        LocationRequestInfo oldRequest = (LocationRequestInfo) requestTable.get(requestID);

                        if (oldRequest != null) {
                            int oldVersion = oldRequest.getVersion();

                            // an old version exists
                            // if we don't have the same version ==> serve this
                            // request and save it
                            int newVersion = table.getVersion(id);
                            if (oldVersion != newVersion) {
                                service.serve(oldest);
                                requestTable.put(requestID, new LocationRequestInfo(newVersion, System
                                        .currentTimeMillis(), true));
                            } else {
                                // we still have the same version
                                // if the previous call hasn't been served ==>
                                // have a look to it's creation time
                                // else put the request in the table and in the
                                // queue
                                if (oldRequest.hasBeenServed()) {
                                    requestTable.put(requestID, new LocationRequestInfo(newVersion, System
                                            .currentTimeMillis(), false));
                                    try {
                                        body.receiveRequest(oldest);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (RenegotiateSessionException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    // if the delay is gone, serve the request
                                    // else put it in the queue
                                    if ((oldRequest.getCreationTime() + DELAY_SAME_REPLY) < System
                                            .currentTimeMillis()) {
                                        service.serve(oldest);
                                        oldRequest.setServed(true);
                                    } else {
                                        try {
                                            body.receiveRequest(oldest);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } catch (RenegotiateSessionException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        } else {
                            // no old request so serve the request and save it
                            service.serve(oldest);

                            int version = table.getVersion(id);
                            requestTable.put(requestID, new LocationRequestInfo(version, System
                                    .currentTimeMillis(), true));
                        }
                    } else {
                        service.serve(oldest);
                    }
                }
            }
        }
    }

    protected String normalizeURL(String url) {
        String tmp = url;

        try {
            tmp = URIBuilder.checkURI(url).toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return tmp;
    }

    protected void register() {
        try {
            logger.info("Attempt at binding : " + url);
            PAActiveObject.register(PAActiveObject.getStubOnThis(), url);
            logger.info("Location Server bound in registry : " + url);
        } catch (Exception e) {
            logger.fatal("Cannot bind in registry - aborting " + url);
            e.printStackTrace();
            return;
        }
    }

    public static void main(String[] args) {
        ProActiveConfiguration.load();
        String name = PAProperties.PA_LOCATION_SERVER_RMI.getValue();

        Object[] arg = new Object[1];
        arg[0] = name;

        MixedLocationServer server = null;

        try {
            if (args.length == 1) {
                server = (MixedLocationServer) PAActiveObject.newActive(MixedLocationServer.class.getName(),
                        arg, NodeFactory.getNode(args[0]));
            } else {
                server = (MixedLocationServer) PAActiveObject.newActive(MixedLocationServer.class.getName(),
                        arg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //
    protected class LocationMap {
        public static final int CONSTANT_VERSION = 0;
        public static final int NO_VERSION_FOUND = -1;
        public static final int MIGRATING_OUT = 2000;
        private Hashtable idToBodyMap;

        public LocationMap() {
            idToBodyMap = new Hashtable();
        }

        public void updateBody(UniqueID id, UniversalBody body, int version) {
            synchronized (this.idToBodyMap) {
                // remove old reference if exists and if is an older version
                WrappedLocationBody wrappedBody = (WrappedLocationBody) idToBodyMap.get(id);

                if (wrappedBody == null) {
                    // add new reference
                    idToBodyMap.put(id, new WrappedLocationBody(body, version));
                } else if (wrappedBody.getVersion() <= version) {
                    idToBodyMap.remove(id);

                    // add new reference
                    idToBodyMap.put(id, new WrappedLocationBody(body, version));
                }
            }
        }

        public UniversalBody getBody(UniqueID id) {
            Object o = null;
            if (id != null) {
                synchronized (this.idToBodyMap) {
                    o = idToBodyMap.get(id);
                    if (o != null) {

                        /*
                        WrappedLocationBody wrappedBody = (WrappedLocationBody) o;

                        if (wrappedBody.isMigrating()) {
                                try {
                                        wait(MIGRATING_OUT);
                                } catch (InterruptedException e) {
                                        e.printStackTrace();
                                }
                        }
                         */
                        return ((WrappedLocationBody) o).getBody();
                    }
                }
            }

            return (UniversalBody) o;
        }

        public int getVersion(UniqueID id) {
            if (id != null) {
                synchronized (this.idToBodyMap) {
                    Object o = idToBodyMap.get(id);
                    if (o != null) {
                        WrappedLocationBody wrappedBody = (WrappedLocationBody) o;
                        return wrappedBody.getVersion();
                    }
                }
            }
            return NO_VERSION_FOUND;
        }
    }

    protected class WrappedLocationBody {
        private int version;
        private UniversalBody wrappedBody;
        private boolean isMigrating;

        public WrappedLocationBody(UniversalBody body, int version) {
            this.wrappedBody = body;
            this.version = version;
            this.isMigrating = false;
        }

        public WrappedLocationBody(UniversalBody body, int version, boolean isMigrating) {
            this.wrappedBody = body;
            this.version = version;
            this.isMigrating = isMigrating;
        }

        public int getVersion() {
            return this.version;
        }

        public UniversalBody getBody() {
            return this.wrappedBody;
        }

        public boolean isMigrating() {
            return this.isMigrating;
        }
    }

    protected class LocationRequestInfo {
        private int version;
        private long creationTime;
        private boolean served;

        public LocationRequestInfo(int version, long creationTime, boolean served) {
            this.version = version;
            this.creationTime = creationTime;
            this.served = served;
        }

        public boolean hasBeenServed() {
            return this.served;
        }

        public int getVersion() {
            return this.version;
        }

        public long getCreationTime() {
            return this.creationTime;
        }

        public void setServed(boolean served) {
            this.served = served;
        }
    }
}
