package org.objectweb.proactive.core.gc;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;


/**
 * A single Garbage Collector thread for the whole JVM
 */
public class GarbageCollectorThread implements Runnable {
    private static final Thread singleton = new Thread(new GarbageCollectorThread(),
            "ProActive GC");

    public void run() {
        try {
            /*
             * Avoid the rush of all threads starting at the same time
             */
            Thread.sleep(new Random().nextInt(GarbageCollector.TTB));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (;;) {
            try {
                Thread.sleep(GarbageCollector.TTB);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            Iterator<UniversalBody> bodies = LocalBodyStore.getInstance()
                                                           .getLocalBodies()
                                                           .bodiesIterator();
            Collection<GCSimpleMessage> toSend = new Vector<GCSimpleMessage>();
            while (bodies.hasNext()) {
                AbstractBody body = (AbstractBody) bodies.next();
                Collection<GCSimpleMessage> messages = body.getGarbageCollector()
                                                           .iteration();
                if (messages != null) {
                    toSend.addAll(messages);
                }
            }
            Collection<GCSimpleMessage> messages = HalfBodies.getInstance()
                                                             .iteration();
            if (messages != null) {
                toSend.addAll(messages);
            }
            MessageSender.sendMessages(toSend);
        }
    }

    static void start() {
        singleton.start();
    }
}
