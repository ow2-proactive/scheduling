/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.p2p.core.service;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.p2p.core.service.KnownTable.KnownTableElement;

import java.io.Serializable;

/**
 * @author Alexandre di Costanzo
 *  
 */
public class Updater implements RunActive, Serializable {

    protected static Logger logger = Logger.getLogger(Updater.class.getName());

    private KnownTable knownTable = null;

    private P2PServiceImpl service = null;

    public Updater() {
        // empty constructor
    }

    public Updater(KnownTable table, P2PServiceImpl service) {
        this.knownTable = table;
        this.service = service;
    }

    /**
     * @see org.objectweb.proactive.RunActive#runActivity(org.objectweb.proactive.Body)
     */
    public void runActivity(Body body) {
        while (true) {
            Object[] table = this.knownTable.toArray();
            for (int i = 0; i < table.length; i++) {
                KnownTableElement element = (KnownTableElement) table[i];

                // Host responds ??
                if (!ping(element.getP2PService())) {
                    this.service
                            .unregisterP2PService((String) element.getKey());
                    continue;
                }

                long currentTime = System.currentTimeMillis();
                if ((currentTime + P2PService.TTU) >= element.getLastUpdate()) {
                    // Register
                    element.getP2PService().registerP2PService(
                            this.service.getServiceName(), this.service,
                            this.service.getLoad(), false);
                } else {
                    // Unregister
                    this.service.unregisterP2PService(element.getP2PService());
                }
            }

            // How many peers ?
            if (this.knownTable.size() < P2PService.FRIENDS) {
                try {
                    ProActiveRuntime[] newFriends = this.service
                            .getProActiveJVMs(P2PService.FRIENDS);
                    for (int i = 0; i < newFriends.length; i++) {
                        String url = newFriends[i].getURL();
                        P2PService remote = P2PServiceImpl
                                .getRemoteP2PService(url.replaceFirst(
                                        "PA_JVM.*", ""));
                        if (!remote.equals(this.service)) {
                            this.knownTable.put(url,
                                    knownTable.new KnownTableElement(url,
                                            remote, remote.getLoad(), System
                                                    .currentTimeMillis()));
                        }
                    }
                } catch (NodeException e) {
                    logger.warn("Could't found the remote node", e);
                } catch (ActiveObjectCreationException e) {
                    logger.warn("Could't found the remote active object", e);
                } catch (ProActiveException e) {
                    logger.warn("Could't connect to the remote service", e);
                }
            }

            try {
                // waiting
                Thread.sleep(P2PService.TTU);
            } catch (InterruptedException e) {
                logger.error("Can't wait TTU.", e);
            }
        }
    }

    /**
     * @param knownTable
     * @return true if the knownTable responds.
     */
    private static boolean ping(P2PService service) {
        try {
            P2PServiceImpl.getRemoteP2PService(service.getURL());
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}