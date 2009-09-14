/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.nodesource.utils;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.exceptions.BodyTerminatedException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.ow2.proactive.authentication.RestrictedService;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 * Class which implements the Pinger thread.
 * <BR>This class communicate with its NodeSource upper class by the NodeSource AO stub,
 * not directly, in order to avoid concurrent access.
 * This object ask periodically list of nodes managed by its NodeSource object,
 * verify if nodes are still alive, and warn the {@link NodeSource} object if a node is down by calling
 *  {@link NodeSource#detectedPingedDownNode(String)} method.
 * @see org.ow2.proactive.resourcemanager.nodesource.deprecated.NodeSource
 *
 */
public class Pinger extends RestrictedService implements InitActive {

    /** stub of the NodeSource Active Object*/
    private NodeSource nodeSource;
    private int pingFrequency = PAResourceManagerProperties.RM_NODE_SOURCE_PING_FREQUENCY.getValueAsInt();

    /** state of the thread, true Pinger "ping", false
     * pinger is stopped */
    private boolean active;

    private Thread pingerThread;

    public Pinger() {
    }

    /**
     * Pinger constructor.
     * Launch the nodes monitoring thread
     * @param source stub of the NodeSource Active Object
     */
    public Pinger(NodeSource source) {
        nodeSource = source;
        registerTrustedService(nodeSource);
        this.active = true;
    }

    public void initActivity(Body body) {
        PAActiveObject.setImmediateService("setPingFrequency");
        PAActiveObject.setImmediateService("shutdown");
        pingerThread = Thread.currentThread();
    }

    /**
     * shutdown the Pinger thread .
     */
    public void shutdown() {
        active = false;
        pingerThread.interrupt();
    }

    /**
     * Gives the state the Thread's state.
     * @return boolean indicating thread's state :
     * true, Pinger continues Pinging or
     * false Pinger thread stops.
     */
    public synchronized boolean isActive() {
        return this.active;
    }

    /**
     * Activity thread of the Pinger.
     * <BR>Each ping frequency time
     * the Pinger get the NodeList of the NodeSource,
     * and verify if nodes are always reachable
     * if one of them is unreachable, the node will be said "down",
     * and must be removed from the NodeSource.
     * {@link NodeSource#detectedPingedDownNode(String)} is called when a down node is detected.
     */
    public void ping() {
        while (this.isActive()) {
            try {
                try {
                    Thread.sleep(pingFrequency);
                } catch (InterruptedException ex) {
                    break;
                }
                if (!this.isActive()) {
                    break;
                }
                for (Node node : nodeSource.getAliveNodes()) {
                    // check active between each ping
                    if (!this.isActive()) {
                        break;
                    }
                    String nodeURL = node.getNodeInformation().getURL();
                    try {
                        node.getNumberOfActiveObjects();
                    } catch (Exception e) {
                        this.nodeSource.detectedPingedDownNode(nodeURL);
                    }
                }
            } catch (BodyTerminatedException e) {
                // node source is terminated
                // terminate...
                break;
            }
        }

        nodeSource.finishNodeSourceShutdown();
        PAActiveObject.terminateActiveObject(true);
    }

    /**
     * @see org.ow2.proactive.authentication.Loggable#getLogger()
     */
    public Logger getLogger() {
        return ProActiveLogger.getLogger(RMLoggers.CONNECTION);
    }

    /**
     * Gets a ping frequency
     * @return a ping frequency
     */
    public IntWrapper getPingFrequency() {
        return new IntWrapper(pingFrequency);
    }

    /**
     * Sets a ping frequency
     * @param a new frequency
     */
    public synchronized void setPingFrequency(int frequency) {
        pingFrequency = frequency;
    }
}
