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

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


/**
 * This class is a client for a migratable Agent
 */
public class AgentClient {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    public static void main(String[] args) {
        Agent myServer;
        String nodeName;
        String hostName;
        GCMApplication proActiveDescriptor;
        ProActiveConfiguration.load();
        try {
            proActiveDescriptor = PAGCMDeployment.loadApplicationDescriptor(new File(args[0]));
            proActiveDescriptor.startDeployment();

            GCMVirtualNode agent = proActiveDescriptor.getVirtualNode("Agent");
            agent.waitReady();
            List<Node> nodeList = agent.getCurrentNodes();

            // Create an active server within this VM
            myServer = (Agent) org.objectweb.proactive.api.PAActiveObject.newActive(Agent.class.getName(),
                    new Object[] { "local" });
            // Invokes a remote method on this object to get the message
            hostName = myServer.getName();
            nodeName = myServer.getNodeName();
            logger.info("Agent is on: host " + hostName + " Node " + nodeName);

            for (Node node : nodeList) {
                // Prints out the message
                myServer.moveTo(node);
                nodeName = myServer.getNodeName();
                hostName = myServer.getName();
                logger.info("Agent is on: host " + hostName + " Node " + nodeName);
            }
            myServer.endBodyActivity();
        } catch (Exception e) {
            logger.error("Could not reach/create server object");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
