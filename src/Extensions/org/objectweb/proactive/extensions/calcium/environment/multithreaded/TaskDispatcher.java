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

import java.io.Serializable;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.environment.FileServerClient;
import org.objectweb.proactive.extensions.calcium.environment.Interpreter;
import org.objectweb.proactive.extensions.calcium.statistics.Timer;
import org.objectweb.proactive.extensions.calcium.task.Task;
import org.objectweb.proactive.extensions.calcium.task.TaskPool;


class TaskDispatcher extends Thread {
    static int DEFAULT_GET_READY_TASK_TIMEOUT = 1000 * 2;
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_ENVIRONMENT);
    boolean shutdown;
    int maxSimulatenusTasks;
    TaskPool taskpool;
    FileServerClient fserver;
    ExecutorService threadPool;
    BlockingQueue<CallableInterpreter> intPool;

    public TaskDispatcher(TaskPool taskpool, FileServerClient fserver,
        int numThreads) {
        this.taskpool = taskpool;
        this.fserver = fserver;
        this.maxSimulatenusTasks = numThreads;
        this.threadPool = Executors.newFixedThreadPool(numThreads);
        this.intPool = new LinkedBlockingQueue<CallableInterpreter>();

        shutdown = true;

        for (int i = 0; i < maxSimulatenusTasks; i++) {
            intPool.add(new CallableInterpreter(new Interpreter()));
        }
    }

    public void run() {
        runSingle();
        //runMutliple();
    }

    public void runSingle() {
        shutdown = false;

        while (!shutdown) {
            Task task = taskpool.getReadyTask(DEFAULT_GET_READY_TASK_TIMEOUT);

            if (task == null) {
                continue;
            }

            //block until there is an available interpreter
            CallableInterpreter interp = null;
            try {
                interp = intPool.take();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            interp.setTask(task);

            if (logger.isDebugEnabled()) {
                logger.debug("Dispatching task=: " + task);
            }
            //intPool.add(interp);
            threadPool.execute(new QueueingFuture(interp));
        }
    }

    public void runMultiple() {
        shutdown = false;

        while (!shutdown) {
            Vector<Task> v = taskpool.getReadyTasks(DEFAULT_GET_READY_TASK_TIMEOUT);

            if (v == null) {
                continue;
            }

            for (Task task : v) {
                //block until there is an available interpreter
                CallableInterpreter interp = null;
                try {
                    interp = intPool.take();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                interp.setTask(task);
                threadPool.execute(new QueueingFuture(interp));
            }
        }
    }

    public void shutdown() {
        shutdown = true;
        threadPool.shutdown();
    }

    /**
     * Callable class for invoking the interpret method in a new thread
     * (processor).
     */
    class CallableInterpreter implements Callable<Task> {
        Task task;
        Interpreter interpreter;

        public CallableInterpreter(Interpreter interpreter) {
            this.interpreter = interpreter;
        }

        public Task call() throws Exception {
            Timer timer = new Timer();
            timer.start();
            task = interpreter.interpret(fserver, task, timer);
            return task;
        }

        public void setTask(Task task) {
            this.task = task;
        }
    }

    class QueueingFuture extends FutureTask<Task> implements Serializable {
        CallableInterpreter callable;

        QueueingFuture(CallableInterpreter c) {
            super(c);
            callable = c;
        }

        QueueingFuture(Runnable runnable, Task result) {
            super(runnable, result);
        }

        protected void done() {
            try {
                Task pTask = get();

                taskpool.putProcessedTask(pTask);
                intPool.put(callable); //recycle CallableObject
            } catch (Exception e) {
                logger.error(
                    "Unable to store processed task back into the taskpool.");
                //TODO put panice exception in taskpool
                e.printStackTrace();
            }
        }
    }
}
