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
package org.objectweb.proactive.core.event;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * <p>
 * Provides support for adding, removing and notifying <code>ProActiveListener</code>. This class
 * is a generic class that can be derived to get immediate support for sending <code>ProActiveEvent</code>
 * to <code>ProActiveListener</code>.
 * </p><p>
 * A class producing <code>ProActiveEvent</code> and allowing <code>ProActiveListener</code> to register can
 * have two strategies with those listeners in case of migration. If the listeners are local to the JVM,
 * it doesn't make sense to serialize them with the migrating object. If the listener are part of the
 * migrating subsystem, they should be serialized with the subsystem.
 * </p><p>
 * This class supports the two strategies and a boolean passed to the constructor indicates the strategy
 * to apply with the registered listeners in case of migration.
 * </p>
 * </p><p>
 * In case of the listeners are not serialized with this object, meaning that there are not part of the subsystem
 * in which is attached this object, we only keep weak references on them. The reference this event producer is
 * keeping on one given listener won't prevent it to be garbage collected if no other strong reference are kept
 * by another object.
 * </p><p>
 * This class is thread safe.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public abstract class AbstractEventProducer implements java.io.Serializable {
    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.EVENTS);

    /** flag specifying if the list of listeners should be serialized */
    protected boolean shouldSerializeListeners;

    /** the list of listeners. There are serialized or not depending the value
       of the variable shouldSerializeListeners.
       If not serialized we use WeakReference to reference them. */
    protected transient ListenerList eventListeners;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates an <code>AbstractEventProducer</code> that does not serialize its registered listener.
     */
    public AbstractEventProducer() {
        this(false);
    }

    /**
     * Creates an <code>AbstractEventProducer</code> and specifies whether its registered listener
     * should be serialized or not
     * @param shouldSerializeListeners true if the registered listeners should be serialized, false else.
     */
    public AbstractEventProducer(boolean shouldSerializeListeners) {
        this.shouldSerializeListeners = shouldSerializeListeners;
        if (shouldSerializeListeners) {
            eventListeners = new PlainListenerList();
        } else {
            eventListeners = new WeakReferenceListenerList();
        }
    }

    /**
     * Creates an <code>AbstractEventProducer</code> and specifies whether its registered listener
     * should be serialized or not and whether the refence on listener should be weak or not.
     * If tshouldSerializeListeners is true, the secong is ignored, i.e Weak Ref won't be used
     * @param shouldSerializeListeners true if the registered listeners should be serialized, false else.
     */
    public AbstractEventProducer(boolean shouldSerializeListeners, boolean useWeakRef) {
        this.shouldSerializeListeners = shouldSerializeListeners;
        if (!shouldSerializeListeners && useWeakRef) {
            eventListeners = new WeakReferenceListenerList();
        } else {
            eventListeners = new PlainListenerList();
        }
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     * Returns true is this event producer has at least one registered listener
     * @return true is this event producer has at least one registered listener
     */
    protected boolean hasListeners() {
        return !eventListeners.isEmpty();
    }

    /**
     * Adds the given listener
     * @param listener the listener to register
     */
    protected void addListener(ProActiveListener listener) {
        synchronized (eventListeners) {
            if (!eventListeners.contains(listener)) {
                eventListeners.add(listener);
            }
        }
    }

    /**
     * Removes the given listener
     * @param listener the listener to remove
     */
    protected void removeListener(ProActiveListener listener) {
        synchronized (eventListeners) {
            eventListeners.remove(listener);
        }
    }

    /**
     * Notifies all registered listener with the event. This method call
     * <code>notifyOneListener</code> on each registered listener.
     * @param event the event to fire to all listeners.
     */
    protected void notifyAllListeners(ProActiveEvent event) {
        synchronized (eventListeners) {
            java.util.Iterator iterator = eventListeners.iterator();
            while (iterator.hasNext()) {
                notifyOneListener((ProActiveListener) iterator.next(), event);
            }
        }
    }

    /**
     * Notifies one listener with the event.
     * @param listener the listener to notify.
     * @param event the event to fire to the listener.
     */
    protected abstract void notifyOneListener(ProActiveListener listener, ProActiveEvent event);

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //

    /**
     * @serialData Write serializable fields, if any exist.
     */
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.writeBoolean(shouldSerializeListeners);
        if (shouldSerializeListeners) {
            s.writeObject(eventListeners);
        }
    }

    /**
     *
     * @serialData Read serializable fields, if any exist.
     *             Recreate eventListeners.
     */
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        shouldSerializeListeners = s.readBoolean();
        if (shouldSerializeListeners) {
            eventListeners = (ListenerList) s.readObject();
        } else {
            eventListeners = new WeakReferenceListenerList();
        }
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //

    /**
     * <p>
     * A class implementing this interface provide a minimum set of methods
     * to support the addition and removal of listener.
     * </p>
     *
     * @author  ProActive Team
     * @version 1.0,  2001/10/23
     * @since   ProActive 0.9
     *
     */
    protected interface ListenerList {

        /**
         * Returns true if no listener is in the list.
         * @return true if no listener is in the list.
         */
        public boolean isEmpty();

        /**
         * Returns the number of listeners in the list.
         * @return the number of listeners in the list.
         */
        public int size();

        /**
         * Returns true if <code>listener</code> is a listener contained in the list.
         * @return true if <code>listener</code> is a listener contained in the list.
         */
        public boolean contains(ProActiveListener listener);

        /**
         * Adds the given listener
         * @param listener the listener to add in the list
         * @return true if the listener has been added.
         */
        public boolean add(ProActiveListener listener);

        /**
         * Removes the given listener
         * @param listener the listener to remove from the list
         * @return true if the listener has been removed.
         */
        public boolean remove(ProActiveListener listener);

        /**
         * Returns an iterator on the listeners of the list
         * @return an iterator on the listeners of the list
         */
        public java.util.Iterator iterator();
    }

    /**
     * <p>
     * Implements a simple list of listeners backed by an <code>ArrayList</code>.
     * </p>
     *
     * @author  ProActive Team
     * @version 1.0,  2001/10/23
     * @since   ProActive 0.9
     *
     */
    private class PlainListenerList implements java.io.Serializable, ListenerList {
        protected java.util.ArrayList list;

        public PlainListenerList() {
            list = new java.util.ArrayList();
        }

        public boolean isEmpty() {
            return list.isEmpty();
        }

        public int size() {
            return list.size();
        }

        public boolean contains(ProActiveListener listener) {
            return list.contains(listener);
        }

        public boolean add(ProActiveListener listener) {
            return list.add(listener);
        }

        public boolean remove(ProActiveListener listener) {
            return list.remove(listener);
        }

        public java.util.Iterator iterator() {
            return list.iterator();
        }
    }

    /**
     * <p>
     * Implements a list of listeners in which the reference to one listener
     * is a WeakReference allowing that listener to be garbage collected if
     * it is no more used.
     * </p>
     *
     * @author  ProActive Team
     * @version 1.0,  2001/10/23
     * @since   ProActive 0.9
     *
     */
    private class WeakReferenceListenerList extends PlainListenerList {
        public WeakReferenceListenerList() {
        }

        @Override
        public boolean contains(ProActiveListener listener) {
            java.util.Iterator iterator = iterator();
            while (iterator.hasNext()) {
                if (iterator.next() == listener) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean add(ProActiveListener listener) {
            return list.add(new java.lang.ref.WeakReference(listener));
        }

        @Override
        public boolean remove(ProActiveListener listener) {
            java.util.Iterator iterator = iterator();
            while (iterator.hasNext()) {
                if (iterator.next() == listener) {
                    iterator.remove();
                    return true;
                }
            }
            return false;
        }

        @Override
        public java.util.Iterator iterator() {
            return new WeakReferenceIterator(list.iterator());
        }
    }

    /**
     * <p>
     * Implements an <code>Iterator</code> on a list containing
     * <code>WeakReference</code>s and return the referenced object.
     * The iterator automatically removed from the list the objects
     * that have been garbage collected.
     * </p>
     *
     * @author  ProActive Team
     * @version 1.0,  2001/10/23
     * @since   ProActive 0.9
     *
     */
    private class WeakReferenceIterator implements java.util.Iterator {
        private java.util.Iterator iterator;
        private Object nextObject;

        public WeakReferenceIterator(java.util.Iterator iterator) {
            this.iterator = iterator;
            nextObject = getNextObject();
        }

        public boolean hasNext() {
            return nextObject != null;
        }

        public Object next() {
            Object result = nextObject;
            nextObject = getNextObject();
            return result;
        }

        public void remove() {
            iterator.remove();
        }

        private Object getNextObject() {
            while (iterator.hasNext()) {
                java.lang.ref.WeakReference ref = (java.lang.ref.WeakReference) iterator.next();
                Object target = ref.get();
                if (target == null) {
                    // object has been removed
                    //logger.debug("!!!!!!!!!!!! REMOVED A GARBAGED LISTENER");
                    iterator.remove();
                } else {
                    return target;
                }
            }
            return null;
        }
    }

    // end inner class WeakReferenceIterator
}
