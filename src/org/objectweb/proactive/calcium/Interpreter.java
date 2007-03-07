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
package org.objectweb.proactive.calcium;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.calcium.exceptions.PanicException;
import org.objectweb.proactive.calcium.skeletons.Instruction;
import org.objectweb.proactive.calcium.statistics.Timer;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class corresponds to a skeleton interpreter, which
 * can be seen as a worker of the skeleton framework.
 *
 * The interpreter will loop taking the top skeletal instruction
 * from the task's instruction stack and execute it.
 *
 * When the instruction is executed, the task's stack can be
 * modified. For example the "if" skeleton will choose which
 * branch must be computed and place this branch in the
 * task's stack.
 *
 * The loop will continue to execute until a task is found
 * to have children tasks, or the task has no more instructions.
 * In either case, the task (and it's children) will be returned.
 *
 * @author The ProActive Team (mleyton)
 *
 */
public class Interpreter implements Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_STRUCTURE);

    public Interpreter() {
    }

    public Task<?> interpret(Task<?> task) {
        Timer timer = new Timer(true);
        while (task.hasInstruction()) {
            if (logger.isDebugEnabled()) {
                logger.debug("--Stack Top-- Task=" + task.getId() + " " +
                    task.getObject());
                String tmp = "";
                for (Instruction c : task.getStack())
                    tmp = c + "\n" + tmp;
                tmp = tmp.trim();
                logger.debug(tmp);
            }

            //Take the top instruction
            Instruction<?, ?> inst = task.popInstruction();

            //Compute the instruction
            int oldId = task.getId();

            try {
                task = inst.computeUnknown(task);
            } catch (Exception e) { //If an exception happend, we report the exception in the task
                task.pushInstruction(inst); //we put back the instruction that generated the exception
                task.setException(e); //then we add the exception to the task
                return task;
            }

            if (oldId != task.getId()) {
                String msg = "Panic error, task id changed while interpreting! " +
                    oldId + "->" + task.getId();
                task.pushInstruction(inst);
                task.setException(new PanicException(msg));
                logger.error(msg);
                return task;
            }

            //If child tasks are present, that's all folks (for now)
            if (task.hasReadyChildTask()) {
                timer.stop();
                task.getStats().addComputationTime(timer.getTime());
                return task;
            }
        } //while

        //The task is finished
        timer.stop();
        task.getStats().addComputationTime(timer.getTime());
        return task;
    }
}
