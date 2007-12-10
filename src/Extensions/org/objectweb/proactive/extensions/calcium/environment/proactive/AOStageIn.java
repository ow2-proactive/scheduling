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

import org.objectweb.proactive.extensions.calcium.environment.Interpreter;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;
import org.objectweb.proactive.extensions.calcium.system.files.FileStaging;
import org.objectweb.proactive.extensions.calcium.task.Task;


public class AOStageIn {
    FileServerClientImpl fserver;
    AOStageCompute stageCompute;
    AOTaskPool taskpool;
    Interpreter interpreter;

    /**
     * Empty constructor for ProActive  MOP
     * Do not use directly!!!
     */
    @Deprecated
    public AOStageIn() {
    }

    /**
     * @param fserver
     * @param next
     * @param taskpool
     */
    public AOStageIn(AOTaskPool taskpool, FileServerClientImpl fserver,
        AOStageCompute stageCompute) {
        super();
        this.fserver = fserver;
        this.stageCompute = stageCompute;
        this.taskpool = taskpool;

        interpreter = new Interpreter();
    }

    public void stageIn(Task task) {
        //task = (Task) PAFuture.getFutureValue(task);
        try {
            SkeletonSystemImpl system = new SkeletonSystemImpl();
            FileStaging files = interpreter.stageIn(task, system, fserver);
            stageCompute.computeTheLoop(new InterStageParam(task, files, system));

            //TODO put my self in the AOI pool
        } catch (Exception e) {
            task.setException(e);
            taskpool.putProcessedTask(task);
        }
    }
}
