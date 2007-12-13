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
package org.objectweb.proactive.core.group.threadpool;

/** The threads that compose a thread pool. */
public class ThreadInThePool extends Thread {
    protected static int counter;

    protected synchronized static int getNextValue() {
        return counter++;
    }

    /** The threadpool owner of this thread. */
    public ThreadPool myPool;

    /** The constructor associates the thread with a thread pool.
     * @param o the thread pool
     */
    public ThreadInThePool(ThreadPool o) {
        this.myPool = o;
        this.setName("ThreadInThePool " + ThreadInThePool.getNextValue());
    }

    /** Looks for a pending job and executes it. */
    @Override
    public void run() {
        Runnable target = null;
        do {
            target = this.myPool.getJobForThePendingQueue();
            if (target != null) {
                //	System.out.println("ThreadInThePool.run() got tarket, now executing " +target.getClass().getName());
                target.run();
                //     System.out.println("ThreadInThePool.run() execution of targer finished");
                this.myPool.controler.jobFinish();
            }
        } while (target != null);
    }
}
