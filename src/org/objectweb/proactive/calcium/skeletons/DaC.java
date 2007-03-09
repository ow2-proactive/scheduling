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
package org.objectweb.proactive.calcium.skeletons;

import java.util.Stack;
import java.util.Vector;

import org.objectweb.proactive.calcium.Task;
import org.objectweb.proactive.calcium.exceptions.EnvironmentException;
import org.objectweb.proactive.calcium.muscle.Condition;
import org.objectweb.proactive.calcium.muscle.Conquer;
import org.objectweb.proactive.calcium.muscle.Divide;
import org.objectweb.proactive.calcium.statistics.Timer;


/**
 * This skeleton represents Divide and Conquer parallelism (data parallelism).
 * To function, a Divide, Condition, and Conquer objects must
 * be passed as parameter.
 *
 * If the Condition is met, a Task will be divided using the Divide object.
 * If the Condition is not met, the child skeleton will be executed.
 * If the task has subchilds, then the Conquer object will be used to conquer
 * the child tasks into the parent task.
 *
 * @author The ProActive Team (mleyton)
 *
 * @param <P>
 */
public class DaC<P, R> implements Skeleton<P, R>, Instruction<P, P> {
    Divide<P, P> div;
    Conquer<R, R> conq;
    Condition<P> cond;
    Skeleton<P, R> child;

    /**
     * Creates a Divide and Conquer skeleton structure
     * @param div Divides a task into subtasks
     * @param cond True if divide should be applied to the task. False if it should be solved.
     * @param child The skeleton that should be applied to the subtasks.
     * @param conq Conqueres the computed subtasks into a single task.
     */
    public DaC(Divide<P, P> div, Condition<P> cond, Skeleton<P, R> child,
        Conquer<R, R> conq) {
        this.div = div;
        this.cond = cond;
        this.child = child;
        this.conq = conq;
    }

    public Stack<Instruction> getInstructionStack() {
        Stack<Instruction> v = new Stack<Instruction>();
        v.add(this);

        return v;
    }

    public Task<P> compute(Task<P> t) throws EnvironmentException {
        Timer timer = new Timer();
        boolean evalCondition = cond.evalCondition(t.getObject());
        timer.stop();
        t.getStats().getWorkout().track(cond, timer);

        if (evalCondition) { //Split the task if required
            t.pushInstruction(new ConquerInst<R, R>(conq));
            t.pushInstruction(new DivideSIMD<P, P>(div,
                    this.getInstructionStack()));
            return t;
        } else { //else execute the child skeleton by
                 // appending the child skeleton code to the stack
            Vector<Instruction> currentStack = t.getStack();
            currentStack.addAll(child.getInstructionStack());
            t.setStack(currentStack);
        }
        return t;
    }

    @Override
    public String toString() {
        return "DaC";
    }
}
