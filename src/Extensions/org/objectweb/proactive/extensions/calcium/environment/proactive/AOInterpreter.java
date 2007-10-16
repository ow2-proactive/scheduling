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

import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.environment.FileServerClient;
import org.objectweb.proactive.extensions.calcium.environment.Interpreter;
import org.objectweb.proactive.extensions.calcium.statistics.Timer;
import org.objectweb.proactive.extensions.calcium.task.Task;
import org.objectweb.proactive.extensions.calcium.task.TaskPool;


public class AOInterpreter {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_ENVIRONMENT);
    private AOInterpreter me;
    private TaskPool taskpool;
    private AOInterpreterPool intpool;
    private FileServerClient fserver;
    ExecutorService threadPool;
    BlockingQueue<CallableInterpreter> localIntPool;

    //Semaphore semIn, semOut, semCom;

    /**
     * Empty constructor for ProActive  MOP
     * Do not use directly!!!
     */
    @Deprecated
    public AOInterpreter() {
    }

    public void init(int maxCInterp, AOInterpreter me, TaskPool taskpool,
        FileServerClient fserver, AOInterpreterPool intpool) {
        this.me = me;
        this.taskpool = taskpool;
        this.fserver = fserver;
        this.intpool = intpool;

        this.threadPool = Executors.newFixedThreadPool(3);

        Semaphore semIn = new Semaphore(1, true);
        Semaphore semCom = new Semaphore(1, true);
        Semaphore semOut = new Semaphore(1, true);

        localIntPool = new LinkedBlockingQueue<CallableInterpreter>();

        Timer unusedCPUTimer = new Timer();
        unusedCPUTimer.start();
        for (int i = 0; i < maxCInterp; i++) {
            //intPool.add(new CallableInterpreter(new Interpreter()));
            localIntPool.add(new CallableInterpreter(me, taskpool, intpool,
                    fserver, semIn, semCom, semOut, unusedCPUTimer));
        }

        for (int i = 0; i < (maxCInterp - 1); i++) {
            this.intpool.registerAsAvailable(me);
        }
    }

    public void interpret(Task task) {
        CallableInterpreter cInterp;

        try {
            cInterp = localIntPool.take();
        } catch (InterruptedException e) {
            task.setException(e);
            e.printStackTrace();
            taskpool.putProcessedTask(task); //not really processed but...
            return;
        }

        cInterp.setTask(task);

        threadPool.submit(cInterp);
    }

    public String sayHello() {
        String localhost = "unkown";
        try {
            localhost = InetAddress.getLocalHost().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Hello from " + localhost;
    }

    /**
     * Callable class for invoking the interpret method in a new thread
     * (processor).
     */
    class CallableInterpreter implements Callable<Task> {
        Task task;
        AOInterpreter me;
        Interpreter interpreter;
        Semaphore semIn;
        Semaphore semOut;
        Semaphore semCom;
        FileServerClient fserver;
        TaskPool taskpool;
        AOInterpreterPool intpool;
        Timer unusedCPUTimer;

        public CallableInterpreter(AOInterpreter me, TaskPool taskpool,
            AOInterpreterPool intpool, FileServerClient fserver,
            Semaphore semIn, Semaphore semCom, Semaphore semOut,
            Timer unusedCPUTimer) {
            this.me = me;
            this.interpreter = new Interpreter();
            this.fserver = fserver;

            this.semIn = semIn;
            this.semOut = semOut;
            this.semCom = semCom;

            this.taskpool = taskpool;
            this.intpool = intpool;

            this.unusedCPUTimer = unusedCPUTimer;
        }

        public Task call() throws Exception {
            task = interpreter.interpret(fserver, task, semIn, semCom, semOut,
                    unusedCPUTimer);

            taskpool.putProcessedTask(task);

            intpool.registerAsAvailable(me);

            localIntPool.put(this);

            return task;
        }

        public void setTask(Task task) {
            this.task = task;
        }
    }
}
