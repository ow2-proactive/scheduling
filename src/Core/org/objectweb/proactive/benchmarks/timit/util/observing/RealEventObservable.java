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
package org.objectweb.proactive.benchmarks.timit.util.observing;

import java.util.Vector;


/**
 * This class is the Observable.
 * Part of the specialized Observer/Observable pattern.
 *
 * @author Brian Amedro, Vladimir Bodnartchouk
 *
 */
public class RealEventObservable implements EventObservable {
    private boolean changed = false;
    private Vector<EventObserver> eventDataObservers;

    /** Construct an Observable with zero Observers. */
    public RealEventObservable() {
        this.eventDataObservers = new Vector<EventObserver>();
    }

    /**
     * Adds an observer to the set of observers for this object, provided that
     * it is not the same as some observer already in the set. The order in
     * which notifications will be delivered to multiple observers is not
     * specified. See the class comment.
     *
     * @param o
     *            an observer to be added.
     * @throws NullPointerException
     *             if the parameter o is null.
     */
    public synchronized void addObserver(EventObserver o) {
        if (o == null) {
            throw new NullPointerException();
        }
        if (!this.eventDataObservers.contains(o)) {
            this.eventDataObservers.addElement(o);
        }
    }

    /**
     * Deletes an observer from the set of observers of this object. Passing
     * <CODE>null</CODE> to this method will have no effect.
     *
     * @param o
     *            the observer to be deleted.
     */
    public synchronized void deleteObserver(EventObserver o) {
        this.eventDataObservers.removeElement(o);
    }

    /**
     * If this object has changed, as indicated by the <code>hasChanged</code>
     * method, then notify all of its observers and then call the
     * <code>clearChanged</code> method to indicate that this object has no
     * longer changed.
     * <p>
     * Each observer has its <code>update</code> method called with two
     * arguments: this observable object and <code>null</code>. In other
     * words, this method is equivalent to: <blockquote><tt>
     * notifyObservers(null)</tt></blockquote>
     *
     * @see java.util.Observable#clearChanged()
     * @see java.util.Observable#hasChanged()
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void notifyObservers() {
        this.notifyObservers(null);
    }

    /**
     * If this object has changed, as indicated by the <code>hasChanged</code>
     * method, then notify all of its observers and then call the
     * <code>clearChanged</code> method to indicate that this object has no
     * longer changed.
     * <p>
     * Each observer has its <code>update</code> method called with two
     * arguments: this observable object and the <code>arg</code> argument.
     *
     * @param arg
     *            any object.
     * @see java.util.Observable#clearChanged()
     * @see java.util.Observable#hasChanged()
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void notifyObservers(Object arg) {

        /*
         * a temporary array buffer, used as a snapshot of the state of current
         * Observers.
         */
        Object[] arrLocal;
        this.setChanged();
        synchronized (this) {

            /*
             * We don't want the Observer doing callbacks into arbitrary code
             * while holding its own Monitor. The code where we extract each
             * Observable from the Vector and store the state of the Observer
             * needs synchronization, but notifying observers does not (should
             * not). The worst result of any potential race-condition here is
             * that: 1) a newly-added Observer will miss a notification in
             * progress 2) a recently unregistered Observer will be wrongly
             * notified when it doesn't care
             */
            if (!this.changed) {
                return;
            }
            arrLocal = this.eventDataObservers.toArray();
            this.clearChanged();
        }

        for (int i = arrLocal.length - 1; i >= 0; i--) {
            ((EventObserver) arrLocal[i]).update(this, arg);
        }
    }

    /**
     * Clears the observer list so that this object no longer has any observers.
     */
    public synchronized void deleteObservers() {
        this.eventDataObservers.removeAllElements();
    }

    /**
     * Marks this <tt>Observable</tt> object as having been changed; the
     * <tt>hasChanged</tt> method will now return <tt>true</tt>.
     */
    public synchronized void setChanged() {
        this.changed = true;
    }

    /**
     * Indicates that this object has no longer changed, or that it has already
     * notified all of its observers of its most recent change, so that the
     * <tt>hasChanged</tt> method will now return <tt>false</tt>. This
     * method is called automatically by the <code>notifyObservers</code>
     * methods.
     *
     * @see java.util.Observable#notifyObservers()
     * @see java.util.Observable#notifyObservers(java.lang.Object)
     */
    public synchronized void clearChanged() {
        this.changed = false;
    }

    /**
     * Tests if this object has changed.
     *
     * @return <code>true</code> if and only if the <code>setChanged</code>
     *         method has been called more recently than the
     *         <code>clearChanged</code> method on this object;
     *         <code>false</code> otherwise.
     * @see java.util.Observable#clearChanged()
     * @see java.util.Observable#setChanged()
     */
    public synchronized boolean hasChanged() {
        return this.changed;
    }

    /**
     * Returns the number of observers of this <tt>Observable</tt> object.
     *
     * @return the number of observers of this object.
     */
    public synchronized int countObservers() {
        return this.eventDataObservers.size();
    }

    /**
     * Returns a vector of StatData of the Observers of the current
     * <tt>Observable</tt> object.
     *
     * @return the vector of observed datas.
     */
    public synchronized EventDataBag getEventDataBag(int subjectRank) {
        EventDataBag result = new EventDataBag(subjectRank);
        Vector<EventData> v = new Vector<EventData>();
        EventObserver eventDataObserver = null;
        EventData eventData = null;
        for (int i = 0; i < this.eventDataObservers.size(); i++) {
            eventDataObserver = this.eventDataObservers.get(i);
            if (eventDataObserver == null) {
                throw new NullPointerException();
            } else {
                eventData = eventDataObserver.getEventData();
                if (eventData == null) {
                    throw new NullPointerException();
                } else {
                    v.add(eventData);
                }
            }
        }
        result.setBag(v);
        return result;
    }
}
