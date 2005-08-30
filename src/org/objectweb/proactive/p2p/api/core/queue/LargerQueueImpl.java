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
package org.objectweb.proactive.p2p.api.core.queue;

import java.util.Collection;
import java.util.Vector;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.p2p.api.core.Result;
import org.objectweb.proactive.p2p.api.core.Task;


public class LargerQueueImpl implements TaskQueue {
    private Vector queue = new Vector();
    private int size = 0;
    private Result bestCurrentResult = null;

    public LargerQueueImpl() {
    }

    /**
     * @see org.objectweb.proactive.p2p.api.core.queue.TaskQueue#addAll(java.util.Collection)
     */
    public void addAll(Collection tasks) {
        this.queue.add(tasks);
        this.size += tasks.size();
        logger.info("Task provider just received and added " + tasks.size() +
            " of group " + ((Task) tasks.iterator().next()).getTag());
    }

    /**
     * @see org.objectweb.proactive.p2p.api.core.queue.TaskQueue#size()
     */
    public IntWrapper size() {
        return new IntWrapper(this.size);
    }

    /**
     * @see org.objectweb.proactive.p2p.api.core.queue.TaskQueue#hasNext()
     */
    public BooleanWrapper hasNext() {
        return new BooleanWrapper(this.size > 0);
    }

    private int current = 0;

    /**
     * @see org.objectweb.proactive.p2p.api.core.queue.TaskQueue#next()
     */
    public Task next() {
        if (current >= this.queue.size()) {
            current = 0;
        }
        Vector subTasks = (Vector) this.queue.get(current);
        if (subTasks.size() == 0) {
            this.queue.remove(current);
            current++;
            return this.next();
        } else {
            this.size--;
            return (Task) subTasks.remove(0);
        }
    }

    /**
     * @see org.objectweb.proactive.p2p.api.core.queue.TaskQueue#informNewBestResult(org.objectweb.proactive.p2p.api.core.Result, java.lang.String)
     */
    public void informNewBestResult(Result newBest, String taskTag) {
        if (this.bestCurrentResult == null) {
            this.bestCurrentResult = newBest;
        } else if (newBest.isBetterThan(this.bestCurrentResult)) {
            this.bestCurrentResult = newBest;
        } else {
            // not a new best result
            logger.debug("The task provider just had inform of a new result," +
                " but it is not better than the precedent");
            return;
        }

        logger.info("The task provider was inform of a new best result from " +
            taskTag);
    }
}
