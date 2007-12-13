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

import java.io.Serializable;
import java.util.Stack;
import java.util.Vector;

import org.objectweb.proactive.extensions.calcium.muscle.Condition;
import org.objectweb.proactive.extensions.calcium.muscle.Conquer;
import org.objectweb.proactive.extensions.calcium.muscle.Divide;
import org.objectweb.proactive.extensions.calcium.stateness.Stateness;
import org.objectweb.proactive.extensions.calcium.statistics.Timer;
import org.objectweb.proactive.extensions.calcium.system.PrefetchFilesMatching;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;
import org.objectweb.proactive.extensions.calcium.task.Task;


public class DaCInst<P extends Serializable, R extends Serializable> implements Instruction<P, P> {
    Divide<P, P> div;
    Conquer<R, R> conq;
    Condition<P> cond;
    Stack<Instruction> childStack;

    public DaCInst(Divide<P, P> div, Conquer<R, R> conq, Condition<P> cond, Stack<Instruction> childStack) {
        super();
        this.div = div;
        this.conq = conq;
        this.cond = cond;
        this.childStack = childStack;
    }

    public Task<P> compute(SkeletonSystemImpl system, Task<P> t) throws Exception {
        Timer timer = new Timer();
        boolean evalCondition = cond.evalCondition(system, t.getObject());
        timer.stop();
        t.getStats().getWorkout().track(cond, timer);

        if (evalCondition) { //Split the task if required
            t.pushInstruction(new ConquerInst<R, R>(conq));
            Stack clone = new Stack();
            clone.push(this);
            t.pushInstruction(new DivideSIMD<P, P>(div, clone));
            return t;
        } else { //else execute the child skeleton by
            // appending the child skeleton code to the stack
            Vector<Instruction> currentStack = t.getStack();
            currentStack.addAll(childStack);
            t.setStack(currentStack);
        }
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
