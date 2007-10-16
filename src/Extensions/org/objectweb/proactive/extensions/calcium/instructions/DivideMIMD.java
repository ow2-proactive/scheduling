/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.calcium.instructions;

import java.util.Collection;
import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.exceptions.MuscleException;
import org.objectweb.proactive.extensions.calcium.muscle.Divide;
import org.objectweb.proactive.extensions.calcium.stateness.Stateness;
import org.objectweb.proactive.extensions.calcium.statistics.Timer;
import org.objectweb.proactive.extensions.calcium.system.PrefetchFilesMatching;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;
import org.objectweb.proactive.extensions.calcium.task.Task;


/**
 * This class is an instruction that will perform a divition of one task
 * into several sub-tasks. Each sub-tasks will have a different instruction
 * stack, as specified in the constructor of the class.
 *
 * @author The ProActive Team (mleyton)
 *
 * @param <P> The type of the parameter inputed at division.
 * @param <X> The type of the objects resulting from the division.
 */
public class DivideMIMD<P, X> implements Instruction<P, X> {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_STRUCTURE);
    Divide<P, X> div;
    private Vector<Stack<Instruction>> stages; //multiple instruction stacks

    /**
     * This constructor
     * @param div
     * @param stages
     */
    protected DivideMIMD(Divide<P, X> div, Vector<Stack<Instruction>> stages) {
        this.div = div;
        this.stages = stages;
    }

    public Task<X> compute(SkeletonSystemImpl system, Task<P> parent)
        throws Exception {
        Timer timer = new Timer();
        Collection<X> childObjects = div.divide(system, parent.getObject());
        timer.stop();

        if (childObjects.size() != stages.size()) {
            String msg = "Divided Parameter(" + childObjects.size() +
                ") and number stages(" + stages.size() + ") don't match.";
            logger.error(msg);
            throw new MuscleException(msg);
        }

        int i = 0;
        for (X o : childObjects) {
            Task<X> child = new Task<X>(o);
            child.setStack(stages.elementAt(i++)); //Each child task executes a different sub-skeleton
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
