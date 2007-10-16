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
package org.objectweb.proactive.extensions.mixedlocation;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.protocols.FTManager;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.body.request.RequestImpl;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.ext.locationserver.LocationServer;
import org.objectweb.proactive.ext.locationserver.LocationServerFactory;


public class RequestWithMixedLocation extends RequestImpl implements java.io.Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.MIGRATION);
    private static final int MAX_TRIES = 15;
    private static int counter = 0;
    private int tries;
    transient protected LocationServer server;

    public RequestWithMixedLocation(MethodCall methodCall,
        UniversalBody sender, boolean isOneWay, long nextSequenceID,
        LocationServer server) {
        super(methodCall, sender, isOneWay, nextSequenceID);
        if (logger.isDebugEnabled()) {
            logger.debug("RequestWithMixedLocation.RequestWithMixedLocation " +
                ++counter);
        }
        this.server = server;
    }

    @Override
    protected int sendRequest(UniversalBody destinationBody)
        throws java.io.IOException {
        int ftres = FTManager.NON_FT;
        if (logger.isDebugEnabled()) {
            logger.debug("RequestWithMixedLocation: sending to universal " +
                counter);
        }
        try {
            ftres = destinationBody.receiveRequest(this);
        } catch (Exception e) {
            this.backupSolution(destinationBody);
        }
        return ftres;
    }

    /**
     * Implements the backup solution
     */
    protected void backupSolution(UniversalBody destinationBody)
        throws java.io.IOException {
        boolean ok = false;
        tries = 0;
        //get the new location from the server
        UniqueID bodyID = destinationBody.getID();
        while (!ok && (tries < MAX_TRIES)) {
            UniversalBody remoteBody = null;
            UniversalBody mobile = queryServer(bodyID);

            //we want to bypass the stub/proxy
            remoteBody = (UniversalBody) ((FutureProxy) ((StubObject) mobile).getProxy()).getResult();

            try {
                remoteBody.receiveRequest(this);

                //everything went fine, we have to update the current location of the object
                //so that next requests don't go through the server
                if (sender != null) {
                    sender.updateLocation(bodyID, remoteBody);
                } else {
                    LocalBodyStore.getInstance().getLocalBody(getSourceBodyID())
                                  .updateLocation(bodyID, remoteBody);
                }
                ok = true;
            } catch (Exception e) {
                logger.error("FAILED = " + " for method " + methodName);
                tries++;
            }
        }
    }

    protected UniversalBody queryServer(UniqueID bodyID) {
        if (server == null) {
            server = LocationServerFactory.getLocationServer();
        }
        UniversalBody mobile = (UniversalBody) server.searchObject(bodyID);
        logger.debug("backupSolution() server has sent an answer");
        ProFuture.waitFor(mobile);
        return mobile;
    }
}
