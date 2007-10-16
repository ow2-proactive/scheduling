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
package org.objectweb.proactive.core.util;


/**
 * <p>
 * A straightford implementation of the threadstore interface.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2002/05
 * @since   ProActive 0.9.2
 */
public class ThreadStoreImpl implements ThreadStore, java.io.Serializable {
    private int counter;
    private boolean defaultOpenState;
    private transient boolean open;

    /**
     * Creates a new ThreadStore that is opened after creation.
     */
    public ThreadStoreImpl() {
        this(true);
    }

    /**
     * Constructor for ThreadStoreImpl.
     * @param isOpened true is the store is opened after creation
     */
    public ThreadStoreImpl(boolean isOpened) {
        defaultOpenState = isOpened;
        open = defaultOpenState;
    }

    /**
     * @see ThreadStore#threadCount()
     */
    public int threadCount() {
        return counter;
    }

    /**
     * @see ThreadStore#enter()
     */
    public synchronized void enter() {
        while (!open) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        counter++;
    }

    /**
     * @see ThreadStore#exit()
     */
    public synchronized void exit() {
        counter--;
        notifyAll();
    }

    /**
     * @see ThreadStore#close()
     */
    public synchronized void close() {
        open = false;
        while ((counter != 0) && !open) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * @see ThreadStore#open()
     */
    public synchronized void open() {
        open = true;
        notifyAll();
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void writeObject(java.io.ObjectOutputStream out)
        throws java.io.IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        // set open to the default value
        open = defaultOpenState;
    }
}
