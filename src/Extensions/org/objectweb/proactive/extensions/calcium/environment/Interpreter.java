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
package org.objectweb.proactive.extensions.calcium.environment;

import java.io.Serializable;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.statistics.Timer;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;
import org.objectweb.proactive.extensions.calcium.system.files.FileStaging;
import org.objectweb.proactive.extensions.calcium.task.Task;


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

    public Task interpret(FileServerClient fserver, Task task, Semaphore semIn,
        Semaphore semCom, Semaphore semOut, Timer tUnusedCPU) {
        Timer timer = new Timer();

        timer.start();

        try {
            SkeletonSystemImpl system = new SkeletonSystemImpl();

            FileStaging files = stageIn(semIn, task, system, fserver);
            task = theLoop(semCom, task, system, tUnusedCPU);
            task = stageOut(semOut, task, files, system, fserver);
        } catch (Exception e) {
            e.printStackTrace();
            task.setException(e);
        }

        //The task is finished
        task.getStats().addComputationTime(timer.getTime());

        timer.stop();

        return task;
    }

    private FileStaging stageIn(Semaphore semIn, Task<?> task,
        SkeletonSystemImpl system, FileServerClient fserver)
        throws Exception {
        //Keep track of current stored files
        if (semIn != null) {
            semIn.acquire();
        }

        FileStaging tfiles;

        try {
            tfiles = new FileStaging(task, fserver, system.getWorkingSpace());
        } catch (Exception e) {
            throw e;
        } finally {
            if (semIn != null) {
                semIn.release();
            }
        }

        return tfiles;
    }

    private Task<?> theLoop(Semaphore semCom, Task<?> task,
        SkeletonSystemImpl system, Timer timer) throws Exception {
        timer.stop();
        if (semCom != null) {
            semCom.acquire();
        }

        try {
            //Stop loop if task is finished or has ready children 
            while (task.hasInstruction() && !task.family.hasReadyChildTask()) {
                if (logger.isDebugEnabled()) {
                    System.out.println(task.stackToString());
                }

                task = task.compute(system);
            } //while
        } catch (Exception e) {
            throw e;
        } finally {
            task.getStats().addUnusedCPUTime(timer.getTime());
            timer.start();
            if (semCom != null) {
                semCom.release();
            }
        }

        return task;
    }

    private Task<?> stageOut(Semaphore semOut, Task<?> task, FileStaging files,
        SkeletonSystemImpl system, FileServerClient fserver)
        throws Exception {
        if (semOut != null) {
            semOut.acquire();
        }

        try {
            //Update new/modified/unreferenced files
            files.stageOut(fserver, task);

            //From now on, the parameters inside each task have different spaces
            task.family.splitfReadyTasksSpace();

            //Clean the working space
            system.getWorkingSpace().delete();
        } catch (Exception e) {
            throw e;
        } finally {
            if (semOut != null) {
                semOut.release();
            }
        }

        return task;
    }
}
