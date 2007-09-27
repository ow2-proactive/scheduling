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
package org.objectweb.proactive.core.component.collectiveitfs;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.node.NodeException;


/**
 * A primitive non-optimized pool for futures handlers. Indeed, in the current implementation,
 * one futures handler is created for each invocation that returns a result...
 *
 * @author Matthieu Morel
 *
 */
public class GatherFuturesHandlerPool {
    static int created = 0;
    static int reused = 0;
    static int passivated = 0;
    static int terminated = 0;
    public ArrayList<GatherFuturesHandler> pool;
    private static GatherFuturesHandlerPool instance = null;

    public static GatherFuturesHandlerPool instance() {
        if (instance == null) {
            instance = new GatherFuturesHandlerPool();
        }
        return instance;
    }

    private GatherFuturesHandlerPool() {
        expirationTime = 10000;
        locked = new Hashtable<GatherFuturesHandler, Long>();
        unlocked = new Hashtable<GatherFuturesHandler, Long>();
    }

    private long expirationTime;
    private Hashtable<GatherFuturesHandler, Long> locked;
    private Hashtable<GatherFuturesHandler, Long> unlocked;

    GatherFuturesHandler create()
        throws ActiveObjectCreationException, NodeException {
        //			System.out.println("CREATED " + ++created );
        return (GatherFuturesHandler) ProActiveObject.newActive(GatherFuturesHandler.class.getName(),
            new Object[] {  });
    }

    boolean validate(GatherFuturesHandler handler) {
        return true;
    }

    void expire(GatherFuturesHandler handler) {
        ProActiveObject.terminateActiveObject(handler, false);
    }

    public synchronized GatherFuturesHandler borrowFuturesHandler()
        throws ActiveObjectCreationException, NodeException {
        long now = System.currentTimeMillis();
        GatherFuturesHandler handler;
        if (unlocked.size() > 0) {
            Enumeration<GatherFuturesHandler> e = unlocked.keys();
            while (e.hasMoreElements()) {
                handler = (GatherFuturesHandler) e.nextElement();
                if ((now - ((Long) unlocked.get(handler)).longValue()) > expirationTime) {
                    // object has expired
                    unlocked.remove(handler);
                    expire(handler);
                    handler = null;
                } else {
                    if (validate(handler)) {
                        unlocked.remove(handler);
                        locked.put(handler, new Long(now));
                        //                        System.out.println("REUSED " + ++reused);
                        return (handler);
                    } else {
                        // object failed validation
                        unlocked.remove(handler);
                        expire(handler);
                        handler = null;
                    }
                }
            }
        }
        // no objects available, create a new one
        handler = create();
        locked.put(handler, new Long(now));
        return (handler);
    }

    public synchronized void returnFuturesHandler(GatherFuturesHandler handler) {
        locked.remove(handler);
        handler.passivate();
        unlocked.put(handler, new Long(System.currentTimeMillis()));
    }
}
