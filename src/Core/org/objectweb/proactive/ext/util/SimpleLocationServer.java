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
package org.objectweb.proactive.ext.util;

import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.BodyMap;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.ext.locationserver.LocationServer;


/**
 * An implementation of a Location Server
 */
public class SimpleLocationServer implements org.objectweb.proactive.RunActive,
    LocationServer {
    static Logger logger = ProActiveLogger.getLogger(Loggers.MIGRATION);
    private BodyMap table;
    private String url;

    public SimpleLocationServer() {
    }

    public SimpleLocationServer(String url) {
        this.url = normalizeURL(url);
        this.table = new BodyMap();
    }

    /**
     * Update the location for the mobile object s
     * with id
     */
    public void updateLocation(UniqueID i, UniversalBody s) {
        //       System.out.println("Server: updateLocation() " + i + " object = " + s);
        this.table.updateBody(i, s);
    }

    /**
     * Return a reference to the remote body if available.
     * Return null otherwise
     */
    public UniversalBody searchObject(UniqueID id) {
        return this.table.getBody(id);
    }

    /**
     * First register with the specified url
     * Then wait for request
     */
    public void runActivity(org.objectweb.proactive.Body body) {
        this.register();
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
        service.fifoServing();
    }

    protected String normalizeURL(String url) {
        String tmp = url;

        //if it starts with rmi we remove it
        String temp = null;
        try {
            tmp = URIBuilder.checkURI(url).toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return tmp;
    }

    protected void register() {
        try {
            logger.info("Attempt at binding : " + this.url);
            PAActiveObject.register(PAActiveObject.getStubOnThis(), this.url);
            logger.info("Location Server bound in registry : " + this.url);
        } catch (Exception e) {
            logger.fatal("Cannot bind in registry - aborting " + this.url);
            e.printStackTrace();
            return;
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            logger.error(
                "usage: java org.objectweb.proactive.ext.util.SimpleLocationServer <server url> [node]");
            System.exit(-1);
        }
        Object[] arg = new Object[1];
        arg[0] = args[0];
        SimpleLocationServer server = null;
        try {
            if (args.length == 2) {
                server = (SimpleLocationServer) PAActiveObject.newActive("org.objectweb.proactive.ext.util.SimpleLocationServer",
                        arg, NodeFactory.getNode(args[1]));
            } else {
                server = (SimpleLocationServer) PAActiveObject.newActive("org.objectweb.proactive.ext.util.SimpleLocationServer",
                        arg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateLocation(UniqueID i, UniversalBody s, int version) {
        // Commented an obviously broken piece of code
        // this.updateLocation(i, s, 0);
    }
}
