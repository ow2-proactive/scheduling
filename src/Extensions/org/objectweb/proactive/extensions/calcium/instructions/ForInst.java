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
package org.objectweb.proactive.extensions.calcium.instructions;

import java.util.Vector;

import org.objectweb.proactive.extensions.calcium.system.PrefetchFilesMatching;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;
import org.objectweb.proactive.extensions.calcium.task.Task;


public class ForInst<P> implements Instruction<P, P> {
    int times;
    Vector<Instruction> childStack;

    public ForInst(int times, Vector<Instruction> childStack) {
        this.times = times;
        this.childStack = childStack;
    }

    public Task<P> compute(SkeletonSystemImpl system, Task<P> task) throws Exception {
        if (times > 0) {
            //Add the For with one less time to execute
            childStack.add(0, new ForInst<P>(times - 1, childStack));

            Vector<Instruction> taskStack = task.getStack();
            taskStack.addAll(childStack);
            task.setStack(taskStack);
        }
        return task;
    }

    public boolean isStateFul() {
        return false;
    }

    public PrefetchFilesMatching getPrefetchFilesAnnotation() {
        return childStack.get(childStack.size() - 1).getPrefetchFilesAnnotation();
    }
}
