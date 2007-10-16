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


/**
 * This interface defines the Observable.
 * Part of the specialized Observer/Observable pattern.
 *
 * @author Brian Amedro, Vladimir Bodnartchouk
 *
 */
public interface EventObservable extends java.io.Serializable {

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
    public void addObserver(EventObserver o);

    /**
     * Deletes an observer from the set of observers of this object. Passing
     * <CODE>null</CODE> to this method will have no effect.
     *
     * @param o
     *            the observer to be deleted.
     */
    public void deleteObserver(EventObserver o);

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
    public void notifyObservers();

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
    public void notifyObservers(Object arg);

    /**
     * Clears the observer list so that this object no longer has any observers.
     */
    public void deleteObservers();

    /**
     * Marks this <tt>Observable</tt> object as having been changed; the
     * <tt>hasChanged</tt> method will now return <tt>true</tt>.
     */
    public void setChanged();

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
    public void clearChanged();

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
    public boolean hasChanged();

    /**
     * Returns the number of observers of this <tt>Observable</tt> object.
     *
     * @return the number of observers of this object.
     */
    public int countObservers();

    /**
     * Returns a vector of StatData of the Observers of the current
     * <tt>Observable</tt> object.
     *
     * @return the vector of observed datas.
     */
    public EventDataBag getEventDataBag(int subjectRank);
}
