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
package org.objectweb.proactive.extra.masterslave.core;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extra.masterslave.interfaces.SlaveMemory;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.Slave;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskIntern;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskProvider;


/**
 * The Slaves Active Objects are the workers in the Master/Slave API.<br/>
 * They execute tasks needed by the master
 * @author fviale
 */
public class AOSlave implements InitActive, RunActive, Serializable, Slave,
    SlaveMemory {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.MASTERSLAVE_SLAVES);

    // stub on this active object
    protected Object stubOnThis;
    public static long SLAVE_CYCLE_TIMEMILLIS = 500;

    // Name of the slave
    private String name;

    // The entity which will provide tasks to the slave (i.e. the master)
    private TaskProvider provider;

    // Tells if the slave is terminated
    private boolean terminated;

    // Tells if the slave is currently sleeping (not asking new tasks)
    private boolean isSleeping;

    // The memory of the slave (the slave can keep some data between different tasks executions (connection to a database, file descriptor, etc ...)
    private HashMap<String, Object> memory = new HashMap<String, Object>();

    /**
     * Required for Active Objects
     *
     */
    public AOSlave() {
    }

    /**
     * Creates a slave with the given name
     * @param name
     */
    public AOSlave(String name, TaskProvider provider) {
        this.name = name;
        this.provider = provider;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof AOSlave) {
            return name.equals(((Slave) obj).getName());
        }
        return false;
    }

    /* (non-Javadoc)
         * @see org.objectweb.proactive.extra.masterslave.interfaces.SlaveMemory#erase(java.lang.String)
         */
    public void erase(String name) {
        memory.remove(name);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.Slave#getName()
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return name.hashCode();
    }

    /* (non-Javadoc)
         * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.Slave#heartBeat()
         */
    public void heartBeat() {
        // Do nothing, we simply want the rendez-vous to work
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        stubOnThis = ProActive.getStubOnThis();

        try {
            isSleeping = false;
            terminated = false;
            body.setImmediateService("getName");
            body.setImmediateService("terminate");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
         * @see org.objectweb.proactive.extra.masterslave.interfaces.SlaveMemory#load(java.lang.String)
         */
    public Object load(String name) {
        return memory.get(name);
    }

    /**
     * gets the initial task to solve
     * @return task
     */
    private TaskIntern initialGetTask() {
        if (logger.isDebugEnabled()) {
            logger.debug(name + " asks a new task...");
        }

        // InitialTask
        TaskIntern currentTask = provider.getTask((Slave) stubOnThis, name);
        // we make sure that we have the real task object and not a future)
        currentTask = (TaskIntern) ProActive.getFutureValue(currentTask);
        return currentTask;
    }

    /**
     * Handle a task (run it)
     * @param task task to run
     * @return the same task, but containing the result
     */
    private TaskIntern handleTask(TaskIntern task) {
        Serializable result = null;

        // We run the task and listen to exception thrown by the task itself
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(name + " runs task " + task.getId() + "...");
            }
            result = task.run(this);
        } catch (Exception e) {
            task.setException(e);
        }
        // We store the result inside our internal version of the task
        task.setResult(result);
        return task;
    }

    /* (non-Javadoc)
    * @see org.objectweb.proactive.RunActive#runActivity(org.objectweb.proactive.Body)
    */
    public void runActivity(Body body) {
        Service service = new Service(body);

        while (!terminated) {
            while (!isSleeping) {
                TaskIntern newTask = initialGetTask();
                while (!isSleeping) {
                    // We verify that the task is not a null task (otherwise, we sleep)
                    if (!newTask.isNull()) {
                        newTask = handleTask(newTask);

                        if (logger.isDebugEnabled()) {
                            logger.debug(name + " sends the result of task " +
                                newTask.getId() + " and asks a new task...");
                        }

                        // We send the result back to the master
                        newTask = provider.sendResultAndGetTask(newTask, name);
                        newTask = (TaskIntern) ProActive.getFutureValue(newTask);
                    } else {
                        // if the task is null, we automatically sleep
                        isSleeping = true;
                        if (logger.isDebugEnabled()) {
                            logger.debug(name + " sleeps...");
                        }
                    }
                }
            }

            // The Slave is currently sleeping
            try {
                Thread.sleep(SLAVE_CYCLE_TIMEMILLIS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // We serve any outstanding request
        if (!terminated) {
            service.serveOldest();
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.SlaveMemory#save(java.lang.String, java.io.Serializable)
     */
    public void save(String name, Object data) {
        memory.put(name, data);
    }

    /* (non-Javadoc)
    * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.Slave#terminate(boolean)
    */
    public BooleanWrapper terminate() {
        if (logger.isDebugEnabled()) {
            logger.debug("Terminating " + name + "...");
        }
        this.terminated = true;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(name + " terminated...");
            }
            ProActive.getBodyOnThis().terminate();
        } catch (IOException e) {
        }
        return new BooleanWrapper(true);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.Slave#wakeup()
     */
    public void wakeup() {
        isSleeping = false;
        if (logger.isDebugEnabled()) {
            logger.debug(name + " wakes up...");
        }
    }
}
