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

import java.util.Collection;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.muscle.Divide;
import org.objectweb.proactive.extensions.calcium.stateness.Stateness;
import org.objectweb.proactive.extensions.calcium.statistics.Timer;
import org.objectweb.proactive.extensions.calcium.system.PrefetchFilesMatching;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;
import org.objectweb.proactive.extensions.calcium.task.Task;


/**
 * This class is an instruction that will perform a divition of one task
 * into several sub-tasks. Each sub-tasks will have the same instruction
 * stack, as specified in the constructor of the class.
 *
 * @author The ProActive Team (mleyton)
 *
 * @param <P> The type of the parameter inputed at division.
 * @param <X> The type of the objects resulting from the division.
 */
public class DivideSIMD<P, X> implements Instruction<P, X> {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_STRUCTURE);
    Divide<P, X> div;
    private Stack<Instruction> instruction; //Single instruction stack

    protected DivideSIMD(Divide<P, X> div, Stack<Instruction> instruction) {
        this.div = div;
        this.instruction = instruction;
    }

    public Task<X> compute(SkeletonSystemImpl system, Task<P> parent) throws Exception {
        Timer timer = new Timer();

        Collection<X> childObjects = div.divide(system, parent.getObject());
        timer.stop();

        for (X o : childObjects) {
            Task<X> child = new Task<X>(o);
            child.setStack(instruction); //Each child task executes the same instruction stack
            parent.family.addReadyChild(child); //parent remebers it's children
        }

        parent.getStats().getWorkout().track(div, timer);
        return (Task<X>) parent;
    }

    public boolean isStateFul() {
        return Stateness.isStateFul(div);
    }

    @SuppressWarnings("unchecked")
    public PrefetchFilesMatching getPrefetchFilesAnnotation() {
        Class cls = div.getClass();

        return (PrefetchFilesMatching) cls.getAnnotation(PrefetchFilesMatching.class);
    }
}
