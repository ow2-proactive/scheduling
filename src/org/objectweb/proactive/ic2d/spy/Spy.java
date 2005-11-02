/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
package org.objectweb.proactive.ic2d.spy;

import java.io.IOException;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.migration.Migratable;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;


/**
 * The master Spy class
 */
public class Spy implements org.objectweb.proactive.RunActive,
    ProActiveInternalObject {

    /** Timeout between updates */
    protected long updateFrequence = 3000;

    /** event manager */
    protected transient SpyEventManager spyEventManager;

    /** the listener of our events */
    protected SpyListener spyListener;
    protected boolean isActive = true;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public Spy() {
    }

    public Spy(SpyListener spyListener) {
        this.spyListener = spyListener;
    }

    //
    // -- PUBLIC METHOD -----------------------------------------------
    //
    public long getUpdateFrequence() {
        return updateFrequence;
    }

    public void sendEventsForAllActiveObjects() {
        SpyEvent[] spyEvents = spyEventManager.createSpyEventForExistingBodies(LocalBodyStore.getInstance()
                                                                                             .getCurrentThreadBody());
        notifyListener(spyEvents);
    }

    public void setUpdateFrequence(long updateFrequence) {
        this.updateFrequence = updateFrequence;
    }

    public void migrateTo(UniqueID bodyId, String nodeDestination)
        throws MigrationException {
        Body body = LocalBodyStore.getInstance().getLocalBody(bodyId);
        if (body == null) {
            throw new MigrationException("Cannot find object id=" + bodyId);
        }
        if (!(body instanceof Migratable)) {
            throw new MigrationException("Object cannot Migrate");
        }
        Node node = null;
        try {
            node = org.objectweb.proactive.core.node.NodeFactory.getNode(nodeDestination);
        } catch (org.objectweb.proactive.core.node.NodeException e) {
            throw new MigrationException("Cannot find node " + nodeDestination,
                e);
        }
        ((Migratable) body).migrateTo(node);
    }

    public String getSystemProperty(String key) {
        return System.getProperty(key);
    }

    public void terminate() {
        isActive = false;
    }

    //
    // -- ADD / REMOVE LISTENERS -----------------------------------------------
    //
    public void addMessageEventListener(UniqueID bodyId) {
        Body body = LocalBodyStore.getInstance().getLocalBody(bodyId);
        if (body != null) {
            spyEventManager.addMessageEventListener(body);
        }
    }

    public void removeMessageEventListener(UniqueID bodyId) {
        Body body = LocalBodyStore.getInstance().getLocalBody(bodyId);
        if (body != null) {
            spyEventManager.removeMessageEventListener(body);
        }
    }

    public void runActivity(org.objectweb.proactive.Body body) {
        spyEventManager = new SpyEventManager(body.getID());
        spyEventManager.addBodyEventListener();
        spyEventManager.addFutureEventListener();
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
        while (isActive) {
            long nextUpdate = System.currentTimeMillis() + updateFrequence;

            // Sleep till the next update, or be awaken by  incoming requests!
            service.blockingServeOldest(updateFrequence);
            // Serve any pending request
            while (service.hasRequestToServe()) {
                service.serveOldest();
            }

            // Check if we have been awaken earlier or not
            if (System.currentTimeMillis() >= nextUpdate) {
                SpyEvent[] spyEvents = spyEventManager.collectPendingSpyEvents();
                if (spyEvents.length > 0) {
                    //System.out.println("sending spyEvents n="+spyEvents.length);
                    // we send event if size == 0 just has a check that the listener is still there
                    notifyListener(spyEvents);
                }
            }
        }
        spyEventManager.removeBodyEventListener();
        spyEventManager.removeFutureEventListener();
        try {
            body.terminate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected void notifyListener(SpyEvent[] events) {
        spyListener.observationsPerformed(events);
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void log(String str) {
        //System.out.println(str);
    }
}
