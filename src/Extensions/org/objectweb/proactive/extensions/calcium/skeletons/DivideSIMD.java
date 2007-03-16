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
package org.objectweb.proactive.extensions.calcium.skeletons;

import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.extensions.calcium.Task;
import org.objectweb.proactive.extensions.calcium.muscle.Divide;
import org.objectweb.proactive.extensions.calcium.statistics.Timer;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


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
public class DivideSIMD<P, X> implements Instruction<P, P> {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_STRUCTURE);
    private Divide<P, X> div;
    private Stack<Instruction> instruction; //Single instruction stack

    protected DivideSIMD(Divide<P, X> div, Stack<Instruction> instruction) {
        this.div = div;
        this.instruction = instruction;
    }

    public Task<P> compute(Task<P> parent) throws Exception {
        Timer timer = new Timer();
        Vector<X> childObjects = div.divide(parent.getObject());
        timer.stop();

        for (int i = 0; i < instruction.size(); i++) {
            Task<X> child = new Task<X>(childObjects.elementAt(i));
            child.setStack(instruction); //Each child task executes the same instruction stack
            parent.addReadyChild(child); //parent remebers it's children
        }

        parent.getStats().getWorkout().track(div, timer);
        return parent;
    }
}
