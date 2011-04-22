/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;


/**
 * Periodically pings connected clients of the resource manager
 */
public class ClientPinger implements InitActive {

    private static final Logger logger = ProActiveLogger.getLogger(RMLoggers.PINGER);

    private AtomicBoolean active = new AtomicBoolean(true);
    private RMCore rmcore;

    public ClientPinger() {
    }

    public ClientPinger(RMCore rmcore) {
        this.rmcore = rmcore;
    }

    public void initActivity(Body body) {
        PAActiveObject.setImmediateService("shutdown");
    }

    public void ping() {
        while (active.get()) {
            synchronized (active) {
                try {
                    active.wait(PAResourceManagerProperties.RM_CLIENT_PING_FREQUENCY.getValueAsInt());
                } catch (InterruptedException e) {
                }
            }

            if (!active.get()) {
                return;
            }

            // copy all the clients from core to local list
            // in order not to hold the lock while iterating
            List<Client> clients = new LinkedList<Client>();
            synchronized (RMCore.clients) {
                logger.debug("Number of registered clients " + RMCore.clients.size());
                clients.addAll(RMCore.clients.values());
            }

            int numberOfPingedClients = 0;
            for (Client client : clients) {
                if (!client.isPingable()) {
                    continue;
                }

                numberOfPingedClients++;
                if (client.isAlive()) {
                    logger.debug("Client " + client + " is alive.");
                } else {
                    logger.warn("Client " + client + " is down.");
                    rmcore.disconnect(client.getId());
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Number of pinged clients " + numberOfPingedClients);
            }

        }
    }

    public void shutdown() {
        synchronized (active) {
            active.set(false);
            active.notifyAll();
        }
        PAActiveObject.terminateActiveObject(false);
    }
}
