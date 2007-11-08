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
package org.objectweb.proactive.extensions.calcium.environment.proactive;

import org.objectweb.proactive.extensions.calcium.environment.Interpreter;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;
import org.objectweb.proactive.extensions.calcium.system.files.FileStaging;
import org.objectweb.proactive.extensions.calcium.task.Task;


public class AOStageOut {
    AOTaskPool taskpool;
    AOInterpreterPool interpool;
    FileServerClientImpl fserver;
    AOStageIn stageIn;
    Interpreter interpreter;

    /**
     * Empty constructor for ProActive  MOP
     * Do not use directly!!!
     */
    @Deprecated
    public AOStageOut() {
    }

    /**
     * @param taskpool
     * @param fserver
     */
    public AOStageOut(AOTaskPool taskpool, FileServerClientImpl fserver) {
        super();
        this.taskpool = taskpool;
        this.fserver = fserver;
        this.stageIn = null;
        this.interpool = null;

        interpreter = new Interpreter();
    }

    public void setStageInAndInterPool(AOStageIn stageIn,
        AOInterpreterPool interpool) {
        this.stageIn = stageIn;
        this.interpool = interpool;
    }

    public void stageOut(InterStageParam param) {
        Task<?> task = param.task;
        SkeletonSystemImpl system = param.system;
        FileStaging fstaging = param.fstaging;

        try {
            task = interpreter.stageOut(task, fstaging, system, fserver);
        } catch (Exception e) {
            task.setException(e);
        }

        taskpool.putProcessedTask(task);
        interpool.put(stageIn);
    }
}
