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
package org.objectweb.proactive.extensions.calcium;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Example of a manager that uses threads to
 * execute the skeleton program.
 *
 * @author The ProActive Team (mleyton)
 *
 * @param <T>
 */
public class MultiThreadedManager extends ResourceManager {
    ExecutorService threadPool; //Thread pool
    int numThreads; //Maximum number of threads to be used

    public MultiThreadedManager(int numThreads) {
        this.threadPool = Executors.newFixedThreadPool(numThreads);
        this.numThreads = numThreads;
    }

    @Override
    public Skernel boot(Skernel skernel) {
        for (int i = 0; i < numThreads; i++) {
            threadPool.submit(new CallableInterpreter(skernel));
        }
        return skernel;
    }

    @Override
    public void shutdown() {
        threadPool.shutdownNow();
    }

    /**
     * Callable class for invoking the interpret method in a new thread (processor).
     *
     */
    protected class CallableInterpreter extends Interpreter implements Callable<Task<?>> {
        Skernel skernel;

        public CallableInterpreter(Skernel skernel) {
            this.skernel = skernel;
        }

        public Task<?> call() throws Exception {
            Task<?> task = skernel.getReadyTask(DEFAULT_GET_READY_TASK_TIMEOUT);

            while (task != null) {
                task = super.interpret(task);
                skernel.putProcessedTask(task);
                task = skernel.getReadyTask(DEFAULT_GET_READY_TASK_TIMEOUT);
            }
            return task;
        }
    }
}
