/* 
* ################################################################
* 
* ProActive: The Java(TM) library for Parallel, Distributed, 
*            Concurrent computing with Security and Mobility
* 
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
* 
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or any later version.
*  
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
* USA
*  
*  Initial developer(s):               The ProActive Team
*                        http://www.inria.fr/oasis/ProActive/contacts.html
*  Contributor(s): 
* 
* ################################################################
*/ 
package org.objectweb.proactive.core.group;

import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.future.FutureProxy;
import java.util.List;


/**
 * This class is used to monitor futures in a group.
 *
 * @author Laurent Baduel - INRIA
 *
 */
public class FutureListGroup implements java.io.Serializable {

    private java.util.List futureList;
    private FuturePool futurePool;


    public FutureListGroup(List memberListProxy, FuturePool p) {
	this.futureList = memberListProxy;
	this.futurePool = p;
    }




    /**
     * Returns the number of element in the List
     */
    private int size() {
	return futureList.size();
    }

    /**
     * Add the future to the futureList.
     * This method does not test if the future is already in the list.
     */
    public boolean add(Object o) {
	//	System.out.println("Adding future " + o);
	return this.futureList.add(o);
    }

    /**
     * Add the future to the futureList at the specified index.
     * This method does not test if the future is already in the list.
     */
    public void set(int index, Object o) {
	//	System.out.println("Adding future " + o);
	this.futureList.add(index,o);
    }


    /**
     * Remove all objects from the FutureList.
     * Return true if successfull (list changed)
     */
    public boolean removeAll() {
	//	System.out.println("Trying to removeAll");
	return this.futureList.removeAll(this.futureList);
    }





    /**
     * Returns true if all the futures in the current list are awaited.
     */
    public boolean allAwaited() {
	boolean value = true;
	synchronized (futurePool) {
	    for (int i = 0; i < futureList.size(); i++) {
		value = value && FutureProxy.isAwaited(futureList.get(i));
	    }
	}
	return value;
    }


    /**
     * Returns true if none of the futures in the current list are awaited.
     */
    public boolean allArrived() {
	synchronized (futurePool) {
	    for (int i = 0; i < futureList.size(); i++) {
		if (FutureProxy.isAwaited(futureList.get(i))) {
		    return false;
		}
	    }
	}
	return true;
    }


    /**
     * Returns the number of currently awaited futures in this list.
     */
    public int countAwaited() {
	int count = 0;
	synchronized (futurePool) {
	    for (int i = 0; i < futureList.size(); i++) {
		if (FutureProxy.isAwaited(futureList.get(i))) {
		    count++;
		}
	    }
	}
	return count;
    }


    /**
     * Returns a future available in this list.
     * Returns null if none is available.
     */
    public Object getOne() {
	synchronized (futurePool) {
	    if (this.countAwaited() == this.size()) {
		return null;
	    } else {
		for (int i = 0; i < futureList.size(); i++) {
		    if (! FutureProxy.isAwaited(futureList.get(i))) {
			return futureList.get(i);
		    }
		}
		return null;
	    }
	}
    }
    


    /**
     * Returns the N-th future in the list.
     * Returns null if it's not available.
     */
    public Object getTheNth(int n) {
	synchronized (futurePool) {
	    if (! FutureProxy.isAwaited(futureList.get(n)))
		return futureList.get(n);
	    else
		return null;
	}
    }

    /**
     * Wait for one future is arrived and return it.
     */
    public Object waitAndGetOne() {
	synchronized (futurePool) {
	    this.waitOne();
	    return this.getOne();
	}
    }

    /**
     * Wait for all futures.
     */
    public void waitAll() {
	synchronized (futurePool) {
	    while (this.countAwaited() > 0) {
		//	System.out.println("still waiting " + this.countAwaited()+ " futures");
		futurePool.waitForReply();
	    }
	}
    }

    /**
     * Wait one future is arrived.
     */
    public void waitOne() {
	synchronized (futurePool) {
	    //int initialCount = this.countAwaited();
	    while (this.countAwaited() == this.size()) {
		//System.out.println("still waiting " + this.countAwaited()+ " futures");
		futurePool.waitForReply();
	    }
	}
    }

    /**
     * Wait the n-th future in the list is arrived.
     */
    public void waitTheNth(int n) {
	synchronized (futurePool) {
	    //int initialCount = this.countAwaited();
	    while (FutureProxy.isAwaited(futureList.get(n)))
		//System.out.println("still waiting " + this.countAwaited()+ " futures");
		futurePool.waitForReply();
	    }
	}

    /**
     * Wait the n-th future is arrived and return it.
     */
    public Object waitAndGetTheNth(int n) {
	synchronized (futurePool) {
	    this.waitTheNth(n);
	    return this.getTheNth(n);
	}
    }


    /**
     * Wait n futures are arrived.
     */
    public void waitN(int n) {
	synchronized (futurePool) {
	    while ((this.size() - this.countAwaited()) > n)
		futurePool.waitForReply();
	}
    }
}

