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
package org.objectweb.proactive.core.gc;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;


/**
 * Just to give a name to our threads
 */
class GCThreadPool implements ThreadFactory {
    static int id;

    public Thread newThread(Runnable r) {
        return new Thread(r, "ProActive GC Broadcasting Thread " + (id++));
    }
}

public class Referenced implements Comparable<Referenced> {

    /**
     * The threaded broadcaster
     */
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(2, Integer.MAX_VALUE,
        GarbageCollector.TTA * 2, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
        new GCThreadPool());

    /**
     * The body we use to communicate with the proxy
     */
    private final UniversalBody body;

    /**
     * The tag to keep track of all the proxy instances targetting this AO
     */
    private WeakReference<GCTag> weakTag;

    /**
     * The last GC response we got, can be null if not applicable
     */
    private GCSimpleResponse lastResponse;

    /**
     * Is there currently a thread sending a message to this referencer?
     */
    private boolean isSendingMessage;

    /**
     * Detect missed deadlines
     */
    private long lastResponseTimestamp;

    /**
     * Can be useful to know who we belong to
     */
    private final GarbageCollector gc;

    Referenced(UniversalBodyProxy proxy, GarbageCollector gc) {
        this.body = proxy.getBody();
        GCTag tag = new GCTag();
        proxy.setGCTag(tag);
        this.weakTag = new WeakReference<GCTag>(tag);
        this.gc = gc;
        this.isSendingMessage = false;
        this.lastResponse = null;
        this.lastResponseTimestamp = System.currentTimeMillis();
    }

    private void setDead() {
        this.weakTag.clear();
    }

    /**
     * Called by the thread
     */
    private void doSendTheMessage(GCMessage msg) {
        GCResponse resp = null;
        try {
            resp = this.body.receiveGCMessage(msg);
        } catch (IOException e) {

            /* Ignore this proxy */
        } catch (Throwable e) {
            e.printStackTrace();
        }

        for (int i = 0; i < msg.size(); i++) {
            GCSimpleMessage m = msg.get(i);
            Referenced ref = m.getReferenced();
            synchronized (ref.gc) {
                if (resp == null) {
                    ref.setDead();
                } else {
                    ref.setLastResponse(resp.get(i));
                }
            }
        }
    }

    void sendMessage(final GCMessage msg) {
        synchronized (this.gc) {
            if (this.isSendingMessage) {
                this.gc.log(Level.WARN, "Sending thread for " + this + " still running");
                return;
            }
            this.isSendingMessage = true;
            executor.execute(new Runnable() {
                public void run() {
                    Referenced.this.doSendTheMessage(msg);
                    synchronized (Referenced.this.gc) {
                        Referenced.this.isSendingMessage = false;
                    }
                }
            });
        }
    }

    void setLastResponse(GCSimpleResponse response) {
        long now = System.currentTimeMillis();
        long acceptableDelay = (GarbageCollector.TTB + GarbageCollector.TTA) / 2;
        long delay;

        synchronized (this.gc) {
            delay = now - this.lastResponseTimestamp;
            this.lastResponse = response;
            this.lastResponseTimestamp = now;
            this.gc.newResponse(this);
        }

        if (delay > acceptableDelay) {
            this.gc.log(Level.WARN, "Delay " + delay + " too long talking to " + this);
        }
    }

    GCSimpleResponse getLastResponse() {
        return this.lastResponse;
    }

    @Override
    public String toString() {
        return this.getBodyID().shortString();
    }

    boolean isReferenced() {
        return this.weakTag.get() != null;
    }

    void add(UniversalBodyProxy ubp) {
        GCTag tag = this.weakTag.get();
        if (tag == null) {
            tag = new GCTag();
            this.weakTag = new WeakReference<GCTag>(tag);
        }
        ubp.setGCTag(tag);
    }

    boolean hasTerminated() {
        return (this.lastResponse != null) && this.lastResponse.isTerminationResponse();
    }

    public int compareTo(Referenced o) {
        return this.getBodyID().compareTo(o.getBodyID());
    }

    UniqueID getBodyID() {
        return this.body.getID();
    }
}
