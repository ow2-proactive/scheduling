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
package org.objectweb.proactive.core.body.ft.servers.util;

import java.util.Hashtable;


/**
 * This class is a generic job queue.
 * @author cdelbe
 * @since 2.2
 */
public class ActiveQueue extends Thread {
    private java.util.ArrayList queue;
    private int counter;
    private boolean kill;
    private Hashtable barriers;

    /**
     *
     */
    public ActiveQueue(String name) {
        queue = new java.util.ArrayList();
        counter = 0;
        kill = false;
        barriers = new Hashtable();
        this.setName(name);
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * return the current queue of jobs to perform
     */
    public java.util.ArrayList getQueue() {
        return queue;
    }

    /**
     * Add a ACservice in the active queue.
     * @return the sequence number of the job
     */
    public synchronized void addJob(ActiveQueueJob j) {
        queue.add(j);
        counter++;
        notifyAll();
    }

    public synchronized JobBarrier addJobWithBarrier(ActiveQueueJob j) {
        JobBarrier b = new JobBarrier(j);
        this.barriers.put(j, b); // hash method of job !!!
        queue.add(j);
        counter++;
        notifyAll();
        return b;
    }

    /**
     * Return the oldest job in queue and remove it from the queue
     */
    public synchronized ActiveQueueJob removeJob() {
        counter--;
        return (ActiveQueueJob) (queue.remove(0));
    }

    /**
     * To stop the thread.
     */
    public synchronized void killMe() {
        kill = true;
        notifyAll();
    }

    public void run() {
        while (true) {
            // if there is no job to do, wait...
            waitForJob();
            // if someone want to kill me, break the loop
            if (kill) {
                break;
            }

            // there are jobs to do !           
            ActiveQueueJob toDo = this.removeJob();
            if (toDo != null) {
                toDo.doTheJob();
                // unlock barrier if any
                JobBarrier b = (JobBarrier) (this.barriers.get(toDo));
                if (b != null) {
                    // this job is barriered
                    b.signalJobCompletion();
                    this.barriers.remove(toDo);
                }
            }
        }
    }

    // synchronized wait on job queue
    private synchronized void waitForJob() {
        try {
            while ((counter == 0) && !kill) {
                wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
