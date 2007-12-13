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
package org.objectweb.proactive.extensions.calcium.environment.proactive;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.environment.Interpreter;
import org.objectweb.proactive.extensions.calcium.statistics.Timer;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;
import org.objectweb.proactive.extensions.calcium.system.files.FileStaging;
import org.objectweb.proactive.extensions.calcium.task.Task;
import org.objectweb.proactive.extensions.calcium.task.TaskPool;


public class AOStageCompute {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_ENVIRONMENT);
    private TaskPool taskpool;
    private AOStageOut stageOut;
    private Timer unusedCPUTimer;
    private Interpreter interpreter;

    /**
     * Empty constructor for ProActive  MOP
     * Do not use directly!!!
     */
    @Deprecated
    public AOStageCompute() {
    }

    /**
     * @param taskpool
     * @param stageOut
     * @param fserver
     */
    public AOStageCompute(TaskPool taskpool, AOStageOut stageOut) {
        super();
        this.taskpool = taskpool;
        this.stageOut = stageOut;

        unusedCPUTimer = new Timer();
        unusedCPUTimer.start();

        interpreter = new Interpreter();
    }

    public void computeTheLoop(InterStageParam param) {
        Task<?> task = param.task;
        SkeletonSystemImpl system = param.system;
        FileStaging fstaging = param.fstaging;

        try {
            task = interpreter.theLoop(task, system, unusedCPUTimer);
            stageOut.stageOut(new InterStageParam(task, fstaging, system));
        } catch (Exception e) {
            task.setException(e);
            taskpool.putProcessedTask(task);
        }
    }
}
