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
package org.objectweb.proactive.ext.locationserver;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.body.migration.MigrationManagerImpl;
import org.objectweb.proactive.core.body.reply.ReplyReceiver;
import org.objectweb.proactive.core.body.request.RequestReceiver;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class MigrationManagerWithLocationServer extends MigrationManagerImpl {
    static Logger logger = ProActiveLogger.getLogger(Loggers.MIGRATION);
    transient private LocationServer locationServer;
    protected Body myBody;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public MigrationManagerWithLocationServer() {
    }

    public MigrationManagerWithLocationServer(LocationServer locationServer) {
        this.locationServer = locationServer;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * update our location on the Location Server
     */
    public void updateLocation(Body body) {
        if (this.locationServer == null) {
            this.locationServer = LocationServerFactory.getLocationServer();
        }

        //  if (locationServer != null) {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating location with this stub " + body.getRemoteAdapter());
        }

        System.out.println("MigrationManagerWithLocationServer.updateLocation() location server" +
            this.locationServer);

        System.out.println("MigrationManagerWithLocationServer.updateLocation() body.getID()" + body.getID());

        System.out.println("MigrationManagerWithLocationServer.updateLocation() body.getremoteadapter() " +
            body.getRemoteAdapter());

        this.locationServer.updateLocation(body.getID(), body.getRemoteAdapter());
        //   }
    }

    //
    // -- Implements MigrationManager -----------------------------------------------
    //
    @Override
    public UniversalBody migrateTo(Node node, Body body) throws MigrationException {
        this.locationServer = null;
        if (this.myBody == null) {
            this.myBody = body;
        }

        //	 System.out.println("XXXXXXX");
        UniversalBody remoteBody = super.migrateTo(node, body);

        return remoteBody;
    }

    //    public void startingAfterMigration(Body body) {
    //        //we update our location
    //        //   System.out.println("YYYYYYYY");
    //        super.startingAfterMigration(body);
    //        updateLocation(body);
    //    }
    @Override
    public RequestReceiver createRequestReceiver(UniversalBody remoteBody,
            RequestReceiver currentRequestReceiver) {
        return new BouncingRequestReceiver();
    }

    @Override
    public ReplyReceiver createReplyReceiver(UniversalBody remoteBody, ReplyReceiver currentReplyReceiver) {
        return currentReplyReceiver;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.updateLocation(this.myBody);
        //	this.updateLocation();
    }
}
