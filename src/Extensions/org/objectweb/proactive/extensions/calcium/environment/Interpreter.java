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
package org.objectweb.proactive.extensions.calcium.environment;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.statistics.Timer;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;
import org.objectweb.proactive.extensions.calcium.task.Task;
import org.objectweb.proactive.extensions.calcium.task.TaskFiles;


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
    FileServer fserver = null;

    public Interpreter() {
    }

    public Task interpret(FileServer fserver, Task task) {
        Timer timer = new Timer(true);

        this.fserver = fserver;

        try {
            task = interpretLoop(task, new SkeletonSystemImpl());
        } catch (Exception e) {
            task.setException(e);
        }

        //The task is finished
        task.getStats().addComputationTime(timer.getTime());
        timer.stop();

        return task;
    }

    private Task<?> interpretLoop(Task<?> task, SkeletonSystemImpl system)
        throws Exception {
        //Keep track of current stored files
        TaskFiles files = new TaskFiles(task);

        files.stageIn(system.getWorkingSpace());

        //Stop loop if task is finished or has ready children
        while (task.hasInstruction() && !task.family.hasReadyChildTask()) {
            if (logger.isDebugEnabled()) {
                System.out.println(task.stackToString());
            }

            task = task.compute(system);
        } //while

        //Update new/modified/unreferenced files
        files.stageOut(fserver, task);

        //From now on, the parameters inside each task have different spaces
        task.family.splitfReadyTasksSpace();

        //Clean the working space
        system.getWorkingSpace().delete();

        return task;
    }
}
