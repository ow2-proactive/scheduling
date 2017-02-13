/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.resourcemanager.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;


/**
 * Periodically pings connected clients of the resource manager
 */
@ActiveObject
public class ClientPinger implements InitActive, RunActive {

    private static final Logger logger = Logger.getLogger(ClientPinger.class);

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
            List<Client> clients = new LinkedList<>();
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

    /**
     * Method controls the execution of every request.
     * Tries to keep this active object alive in case of any exception.
     */
    public void runActivity(Body body) {
        Service service = new Service(body);
        while (body.isActive()) {
            try {
                Request request = service.blockingRemoveOldest();
                if (request != null) {
                    try {
                        service.serve(request);
                    } catch (Throwable e) {
                        logger.error("Cannot serve request: " + request, e);
                    }
                }
            } catch (InterruptedException e) {
                logger.warn("runActivity interrupted", e);
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
