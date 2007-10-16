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

import java.util.Stack;
import java.util.Vector;

import org.objectweb.proactive.extensions.calcium.muscle.Condition;
import org.objectweb.proactive.extensions.calcium.stateness.Stateness;
import org.objectweb.proactive.extensions.calcium.statistics.Timer;
import org.objectweb.proactive.extensions.calcium.system.PrefetchFilesMatching;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;
import org.objectweb.proactive.extensions.calcium.task.Task;


public class IfInst<P> implements Instruction<P, P> {
    Condition<P> cond;
    Stack<Instruction> trueChildStack;
    Stack<Instruction> falseChildStack;

    public IfInst(Condition<P> cond, Stack<Instruction> trueChildStack,
        Stack<Instruction> falseChildStack) {
        this.cond = cond;
        this.trueChildStack = trueChildStack;
        this.falseChildStack = falseChildStack;
    }

    public Task<P> compute(SkeletonSystemImpl system, Task<P> t)
        throws Exception {
        Stack<Instruction> childStack;
        Timer timer = new Timer();
        boolean evalCondition = cond.evalCondition(system, t.getObject());
        timer.stop();

        if (evalCondition) {
            childStack = trueChildStack;
        } else {
            childStack = falseChildStack;
        }

        Vector<Instruction> taskStack = t.getStack();
        taskStack.addAll(childStack);
        t.setStack(taskStack);
        t.getStats().getWorkout().track(cond, timer);

        return t;
    }

    public boolean isStateFul() {
        return Stateness.isStateFul(cond);
    }

    @SuppressWarnings("unchecked")
    public PrefetchFilesMatching getPrefetchFilesAnnotation() {
        Class cls = cond.getClass();

        return (PrefetchFilesMatching) cls.getAnnotation(PrefetchFilesMatching.class);
    }
}
