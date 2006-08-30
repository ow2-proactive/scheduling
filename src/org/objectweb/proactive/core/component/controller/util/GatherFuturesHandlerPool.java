package org.objectweb.proactive.core.component.controller.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.NodeException;

/**
 * A primitive non-optimized pool
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
        return (GatherFuturesHandler) ProActive.newActive(GatherFuturesHandler.class.getName(),
            new Object[] {  });
    }

    boolean validate(GatherFuturesHandler handler) {
        return true;
    }

    void expire(GatherFuturesHandler handler) {
        ProActive.terminateActiveObject(handler, false);
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
