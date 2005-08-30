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


public class BasicQueueImpl implements TaskQueue {
    private Vector queue = new Vector();
    private Result bestCurrentResult = null;

    public BasicQueueImpl() {
    }

    /**
     * @see org.objectweb.proactive.p2p.api.core.queue.TaskQueue#addAll(java.util.Collection)
     */
    public void addAll(Collection tasks) {
        queue.addAll(tasks);
        logger.info("Task provider just received and added " + tasks.size() +
            " of group " + ((Task) tasks.iterator().next()).getTag());
    }

    /**
     * @see org.objectweb.proactive.p2p.api.core.queue.TaskQueue#size()
     */
    public IntWrapper size() {
        return new IntWrapper(this.queue.size());
    }

    /**
     * @see org.objectweb.proactive.p2p.api.core.queue.TaskQueue#hasNext()
     */
    public BooleanWrapper hasNext() {
        return new BooleanWrapper(queue.size() > 0);
    }

    /**
     * @see org.objectweb.proactive.p2p.api.core.queue.TaskQueue#next()
     */
    public Task next() {
        return (Task) this.queue.remove(0);
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
