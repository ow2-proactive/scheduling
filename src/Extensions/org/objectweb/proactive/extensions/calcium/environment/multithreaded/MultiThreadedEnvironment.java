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
package org.objectweb.proactive.extensions.calcium.environment.multithreaded;

import org.objectweb.proactive.extensions.calcium.environment.Environment;
import org.objectweb.proactive.extensions.calcium.environment.EnvironmentServices;
import org.objectweb.proactive.extensions.calcium.environment.FileServer;
import org.objectweb.proactive.extensions.calcium.environment.FileServerClient;
import org.objectweb.proactive.extensions.calcium.task.TaskPool;


/**
 * This class provides parallel execution environment for {@link org.objectweb.proactive.extensions.calcium.Calcium Calcium}.
 * The environment is based on threads, which are executed on the local machine.
 *
 * @author The ProActive Team (mleyton)
 */
public class MultiThreadedEnvironment implements EnvironmentServices {
    TaskDispatcher dispatcher;
    TaskPool taskpool;
    FileServerClient fserver;

    public static Environment factory(int maxThreads) {

        return new MultiThreadedEnvironment(maxThreads);
    }

    /**
     * This constructors instantiates a default FileServer based on the temporary directory
     * of the machine.
     * @param numThreads Maximum number of threads to be used.
     */
    MultiThreadedEnvironment(int numThreads) {
        this(numThreads, new FileServer());
    }

    /**
     *
     * @param numThreads Maximum number of threads to be used.
     * @param fserver A previously instantiated FileServer.
     */
    MultiThreadedEnvironment(int numThreads, FileServer fserver) {
        fserver.initFileServer();
        this.taskpool = new TaskPool();
        this.fserver = new FileServerClientImpl(fserver);
        this.dispatcher = new TaskDispatcher(taskpool, this.fserver, numThreads);
    }

    /**
     * @see EnvironmentFactory#getTaskPool();
     */
    public TaskPool getTaskPool() {
        return taskpool;
    }

    /**
     * @see EnvironmentFactory#start();
     */
    public void start() {
        dispatcher.start();
    }

    /**
     * @see EnvironmentFactory#shutdown();
     */
    public void shutdown() {
        dispatcher.shutdown();
        fserver.shutdown();
    }

    /**
     * @see EnvironmentFactory#getFileServer();
     */
    public FileServerClient getFileServer() {
        return fserver;
    }
    
    public String getName() {
        return "Multithreaded Environment";
    }

    public int getVersion() {
        return 1;
    }
}