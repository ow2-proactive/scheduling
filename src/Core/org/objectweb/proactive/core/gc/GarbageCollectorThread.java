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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Level;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.util.ProActiveRandom;


/**
 * A single Garbage Collector thread for the whole JVM
 */
public class GarbageCollectorThread implements Runnable {
    private static final Thread singleton = new Thread(new GarbageCollectorThread(), "ProActive GC");

    public void run() {
        try {
            /*
             * Avoid the rush of all threads starting at the same time
             */
            Thread.sleep(ProActiveRandom.nextInt(GarbageCollector.TTB));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (;;) {
            long sleepDuration = GarbageCollector.TTB;
            try {
                Thread.sleep(sleepDuration);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            long start = System.currentTimeMillis();
            Iterator<UniversalBody> bodies = LocalBodyStore.getInstance().getLocalBodies().bodiesIterator();
            Collection<GCSimpleMessage> toSend = new LinkedList<GCSimpleMessage>();
            while (bodies.hasNext()) {
                AbstractBody body = (AbstractBody) bodies.next();
                Collection<GCSimpleMessage> messages = body.getGarbageCollector().iteration();
                if (messages != null) {
                    toSend.addAll(messages);
                }
            }
            Collection<GCSimpleMessage> messages = HalfBodies.getInstance().iteration();
            if (messages != null) {
                toSend.addAll(messages);
            }
            MessageSender.sendMessages(toSend);
            sleepDuration = GarbageCollector.TTB - (System.currentTimeMillis() - start);
            if (sleepDuration <= 0) {
                AsyncLogger.queueLog(Level.WARN, "Broadcasting took longer than TTB (" +
                    GarbageCollector.TTB + "): " + (GarbageCollector.TTB - sleepDuration));
                sleepDuration = 1;
            }
        }
    }

    static void start() {
        singleton.setDaemon(true);
        singleton.start();
    }
}
