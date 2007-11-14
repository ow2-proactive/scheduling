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
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Level;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.HalfBody;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;


/**
 * We do not create a separate {@link GarbageCollector} instance for each
 * Half Body, they are all managed by this singleton
 */
public class HalfBodies extends GarbageCollector {

    /**
     * We always send this dummy activity as we don't take part in the
     * Cyclic DGC, just the Acyclic DGC.
     */
    private final Activity dummyActivity = new Activity(new UniqueID(), 1);

    /**
     * Keep track of who knows what
     */
    private final WeakHashMap<HalfBody, ConcurrentLinkedQueue<Referenced>> references =
        new WeakHashMap<HalfBody, ConcurrentLinkedQueue<Referenced>>();

    /**
     * The singleton
     */
    private static final HalfBodies singleton = new HalfBodies();

    /**
     * Build the singleton with a dummy half body
     * @throws ActiveObjectCreationException
     */
    private HalfBodies() {
        super(makeMyHalfBody());
    }

    /*
     * Avoid creating a useless HalfBody when the DGC is disabled
     */
    private static HalfBody makeMyHalfBody() {
        HalfBody hb = null;

        if (GarbageCollector.dgcIsEnabled()) {
            hb = HalfBody.getHalfBody(LocalBodyStore.getInstance()
                                                    .getHalfBodyMetaObjectFactory());
        }

        return hb;
    }

    /**
     * Give the singleton to actual half bodies
     */
    public static HalfBodies getInstance() {
        return singleton;
    }

    /**
     * Get the set of all known referenced proxys, with no doubles
     */
    private Set<Referenced> getReferenced() {
        Set<Referenced> refs = new TreeSet<Referenced>();
        synchronized (this.references) {
            for (ConcurrentLinkedQueue<Referenced> c : this.references.values()) {
                for (Referenced r : c) {
                    if (r.hasTerminated() || !r.isReferenced()) {
                        c.remove(r);
                    } else {
                        refs.add(r);
                    }
                }
            }
        }
        return refs;
    }

    /**
     * Called by the broadcasting thread to ping all known referenced
     */
    @Override
    protected synchronized Collection<GCSimpleMessage> iteration() {
        if (this.isFinished()) {
            return null;
        }
        Collection<Referenced> refs = this.getReferenced();
        this.log(Level.DEBUG, "Sending GC Message to: " + refs);
        Vector<GCSimpleMessage> messages = new Vector<GCSimpleMessage>(refs.size());
        for (Referenced r : refs) {
            messages.add(new GCSimpleMessage(r, this.body.getID(), false,
                    this.dummyActivity));
        }

        return messages;
    }

    /**
     * A half body is here to keep its referenced busy
     */
    @Override
    protected boolean isBusy() {
        return !this.references.isEmpty();
    }

    /**
     * Find the actual half body, and attach the referenced
     */
    @Override
    public synchronized void addProxy(AbstractBody body,
        UniversalBodyProxy proxy) {
        if (this.isFinished()) {
            return;
        }
        if (proxy.getBody() == null) {
            return;
        }
        ConcurrentLinkedQueue<Referenced> refs = this.references.get(body);
        if (refs == null) {
            refs = new ConcurrentLinkedQueue<Referenced>();
            this.references.put((HalfBody) body, refs);
        }

        Referenced ref = new Referenced(proxy, this);
        if (!refs.contains(ref)) {
            refs.add(ref);
        }
        this.log(Level.DEBUG,
            "New referenced: " + proxy.getBodyID().shortString());
    }

    /**
     * Your standard Java code holds references to AOs, this method should
     * be called to make the half bodies appear as idle and let the referenced
     * AO be garbage collected. Otherwise one can wait for the local GC to
     * collect these unused references.
     */
    public static void end() {
        singleton.finish();
    }

    private synchronized void finish() {
        this.setFinishedState(FinishedState.ACYCLIC);
        this.references.clear();
    }
}
