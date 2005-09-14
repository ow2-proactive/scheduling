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
package org.objectweb.proactive.branchnbound.core.queue;

import java.util.Collection;
import java.util.Vector;

import org.objectweb.proactive.branchnbound.core.Task;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;


public class LargerQueueImpl implements TaskQueue {
    private Vector queue = new Vector();
    private int size = 0;

    public LargerQueueImpl() {
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#addAll(java.util.Collection)
     */
    public void addAll(Collection tasks) {
        if (tasks.size() > 0) {
            this.queue.add(tasks);
            this.size += tasks.size();
            if (logger.isInfoEnabled()) {
                Task t = ((Task) tasks.iterator().next());
                logger.info("Task provider just received and added " +
                    tasks.size() + " of group " + t.getTag());
            }
        }
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#size()
     */
    public IntWrapper size() {
        return new IntWrapper(this.size);
    }

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#hasNext()
     */
    public BooleanWrapper hasNext() {
        return new BooleanWrapper(this.size > 0);
    }

    private int current = 0;

    /**
     * @see org.objectweb.proactive.branchnbound.core.queue.TaskQueue#next()
     */
    public Task next() {
        if (this.size == 0) {
            throw new RuntimeException("No more elements");
        }
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

    public void flushAll() {
        this.queue.removeAllElements();
        this.current = 0;
        this.size = 0;
    }
}
