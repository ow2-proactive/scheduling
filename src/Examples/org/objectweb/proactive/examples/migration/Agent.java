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
package org.objectweb.proactive.examples.migration;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class represents a migratable Agent
 */
public class Agent implements InitActive, RunActive, EndActive, java.io.Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    private String name;
    private String nodename;
    private String hostname;

    public Agent() {
    }

    public Agent(String name) {
        this.name = name;
    }

    public String getName() {
        try {
            //System.out.println("getName called");
            //return the name of the Host
            return URIBuilder.getHostNameorIP(ProActiveInet.getInstance().getInetAddress()).toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
            return "getName failed";
        }
    }

    public String getNodeName() {
        try {
            //System.out.println("getNodeName called");
            //return the name of the Node
            return PAActiveObject.getBodyOnThis().getNodeURL().toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
            return "getNodeName failed";
        }
    }

    public void moveTo(Node node) throws Exception {
        //try {
        logger.info(" I am going to migrate to " + node.getNodeInformation().getURL());
        PAMobileAgent.migrateTo(node);
        // System.out.println("migration done");
        //    } catch (Exception e) {
        //      e.printStackTrace();
        //    }
    }

    public void endBodyActivity() {
        PAActiveObject.terminateActiveObject(true);
    }

    public void initActivity(Body body) {
        logger.info("Initialization of the Activity");
    }

    public void runActivity(Body body) {
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
        while (body.isActive()) {
            // The synchro policy is FIFO
            service.blockingServeOldest();
            // The synchro policy is LIFO uncomment the following lines
            //service.waitForRequest();
            //System.out.println(" I am going to serve " + service.getYoungest().getMethodName());
            //service.serveYoungest();
            //System.out.println("served");
        }
    }

    public void endActivity(Body body) {
        logger.info("End of the activity of this Active Object");
    }
}
