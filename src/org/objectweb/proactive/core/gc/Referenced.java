package org.objectweb.proactive.core.gc;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.apache.log4j.Level;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;


public class Referenced implements Comparable {

    /**
     * The body we use to communicate with the proxy
     */
    private final UniversalBody body;

    /**
     * The tag to keep track of all the proxy instances targetting this AO
     */
    private WeakReference<GCTag> weakTag;

    /**
     * The last GC response we got, can be null for example if we are
     * currently sending a GC message
     */
    private GCSimpleResponse lastResponse;

    /**
     * Detect missed deadlines
     */
    private long lastResponseTimestamp;

    /**
     * The thread doing the communication
     */
    private Thread commThread;

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
            if ((this.commThread != null) && this.commThread.isAlive()) {
                this.gc.log(Level.WARN,
                    "Sending thread for " + this + " still running");
                return;
            }
            this.lastResponse = null;
            this.commThread = new Thread() {
                        public void run() {
                            Referenced.this.doSendTheMessage(msg);
                        }
                    };
            this.commThread.start();
        }
        try {
            /*
             * Alleviate some of the load
             */
            this.commThread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
            this.gc.log(Level.WARN,
                "Delay " + delay + " too long talking to " + this);
        }
    }

    GCSimpleResponse getLastResponse() {
        return this.lastResponse;
    }

    public String toString() {
        return this.body.getID().shortString();
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
        return (this.lastResponse != null) &&
        this.lastResponse.isTerminationResponse();
    }

    public int compareTo(Object o) {
        return this.body.getID().compareTo(((Referenced) o).body.getID());
    }

    UniqueID getBodyID() {
        return this.body.getID();
    }
}
