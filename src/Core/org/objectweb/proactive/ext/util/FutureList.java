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
package org.objectweb.proactive.ext.util;

/**
 * <p>
 * <code>FutureList</code> is an object used to monitor a subset of all the
 * futures waited by an active object. A user can simply add or remove
 * <code>Future</code> objects from this list and then call methods to test for
 * their availability.
 * </p><p>
 * Future Objects to be watched after are added and removed to this list by the user.
 * This class is not thread safe
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2002/09/25
 * @since   ProActive 0.9
 *
 */
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.PAFuture;


@PublicAPI
public class FutureList {
    private java.util.Vector<Object> futureList;

    public FutureList() {
        futureList = new java.util.Vector<Object>();
    }

    /**
     * Add the future to the futureList
     * This method does not test if the future is already in the list.
     */
    public boolean add(Object o) {
        //	System.out.println("Adding future " + o);
        return this.futureList.add(o);
    }

    /**
     * Remove the object from the FutureList
     * Return true if successfull
     */
    public boolean remove(Object o) {
        //	System.out.println("Trying to remove " + o);
        return this.futureList.remove(o);
    }

    /**
     * Return the number of future in the List
     */
    public int size() {
        return this.futureList.size();
    }

    /**
     * Return the element at the specified position in this List
     */
    public Object get(int index) {
        return this.futureList.elementAt(index);
    }

    /**
     * Return true if all the futures in the current list are awaited
     */
    public boolean allAwaited() {
        boolean value = true;
        for (int i = 0; i < futureList.size(); i++) {
            value = value && PAFuture.isAwaited(futureList.elementAt(i));
        }
        return value;
    }

    /**
     * Return true if none of the futures in the current list are awaited
     */
    public boolean noneAwaited() {
        for (int i = 0; i < futureList.size(); i++) {
            if (PAFuture.isAwaited(futureList.elementAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return the number of currently awaited futures in this list
     */
    public int countAwaited() {
        int count = 0;
        for (int i = 0; i < futureList.size(); i++) {
            if (PAFuture.isAwaited(futureList.elementAt(i))) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns a future available in this list.
     * Returns null if none is available.
     */
    public Object getOne() {
        if (this.countAwaited() == this.size()) {
            //System.out.println("still waiting " + this.countAwaited()+ " futures");
            //futurePool.waitForReply();
            return null;
        } else {
            Object temp;
            for (int i = 0; i < futureList.size(); i++) {
                temp = futureList.elementAt(i);
                if (!PAFuture.isAwaited(temp)) {
                    return temp;
                }
            }
            return null;
        }
    }

    /**
     * Removes and returns a future available this list.
     * Returns null if none is available.
     */
    public Object removeOne() {
        Object tmp;
        tmp = this.getOne();
        if (tmp != null) {
            //	System.out.println("Removing future "  + tmp);
            //System.out.println("Result is " + this.remove(tmp));
            this.remove(tmp);
        }
        return tmp;
    }

    public Object waitAndGetOne() {
        this.waitOne();
        return this.getOne();
    }

    public Object waitAndRemoveOne() {
        this.waitOne();
        return this.removeOne();
    }

    public void waitAll() {
        PAFuture.waitForAll(futureList);
    }

    public void waitOne() {
        PAFuture.waitForAny(futureList);
    }

    public void waitN(int n) {
        java.util.Vector<Object> temp = new java.util.Vector<Object>(futureList);
        for (int i = 0; i < n; i++) {
            int index = PAFuture.waitForAny(temp);
            temp.remove(index);
        }
    }

    public void waitTheNth(int n) {
        PAFuture.waitForTheNth(futureList, n);
    }

    public Object waitAndGetTheNth(int n) {
        PAFuture.waitForTheNth(futureList, n);
        return this.futureList.elementAt(n);
    }
}
