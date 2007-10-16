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
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.environment.FileServerClient;
import org.objectweb.proactive.extensions.calcium.task.Task;


public class TaskDispatcher extends Thread {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_ENVIRONMENT);
    private AOInterpreterPool intpool;
    private AOTaskPool taskpool;
    private boolean shutdown;

    public TaskDispatcher(int maxCInterp, AOTaskPool taskpool,
        FileServerClient fserver, AOInterpreterPool intpool, AOInterpreter[] aoi)
        throws NodeException, ActiveObjectCreationException,
            ClassNotFoundException {
        super();
        shutdown = false;

        // Create Active Objects
        this.taskpool = taskpool;
        this.intpool = intpool;

        this.intpool.init(aoi);

        //Instantiate Active Objects
        for (AOInterpreter i : aoi) {
            i.init(maxCInterp, i, taskpool, fserver, intpool);
        }
    }

    public void run() {
        shutdown = false;

        while (!shutdown) {
            Task task = taskpool.getReadyTask(0);
            task = (Task) ProFuture.getFutureValue(task);

            if (task != null) {
                //block until there is an available interpreter
                AOInterpreter interpreter = intpool.getAOInterpreter();
                interpreter.interpret(task); //remote async call
            }
        }
    }

    public void shutdown() {
        shutdown = true;
    }
}
