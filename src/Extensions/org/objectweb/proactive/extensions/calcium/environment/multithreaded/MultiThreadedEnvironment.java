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
package org.objectweb.proactive.extensions.calcium.environment.multithreaded;

import java.io.File;

import org.objectweb.proactive.extensions.calcium.environment.EnvironmentFactory;
import org.objectweb.proactive.extensions.calcium.environment.FileServer;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;
import org.objectweb.proactive.extensions.calcium.task.TaskPool;


public class MultiThreadedEnvironment implements EnvironmentFactory {
    TaskDispatcher dispatcher;
    TaskPool taskpool;
    FileServer fserver;
    File outputDir;

    public MultiThreadedEnvironment(int numThreads) {
        this(numThreads, new LocalFileServer(),
            SkeletonSystemImpl.newDirInTmp("calcium-output"));
    }

    public MultiThreadedEnvironment(int numThreads, LocalFileServer fserver,
        File outputDir) {
        this.taskpool = new TaskPool();
        this.fserver = fserver;
        this.dispatcher = new TaskDispatcher(taskpool, fserver, numThreads);
        this.outputDir = outputDir;

        outputDir.mkdirs();
        if (!outputDir.exists()) {
            throw new IllegalArgumentException("Cannot creeat output dir: " +
                outputDir.getPath());
        }
        if (!outputDir.canWrite()) {
            throw new IllegalArgumentException("Cannot write to output dir: " +
                outputDir.getPath());
        }
    }

    public TaskPool getTaskPool() {
        return taskpool;
    }

    public void start() {
        dispatcher.start();
    }

    public void shutdown() {
        dispatcher.shutdown();
    }

    public FileServer getFileServer() {
        return fserver;
    }

    public File getOutPutDir() {
        return this.outputDir;
    }
}
