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
package org.objectweb.proactive.extensions.masterworker.core;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.TaskIntern;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.TaskProvider;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.Worker;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * The Workers Active Objects are the workers in the Master/Worker API.<br>
 * They execute tasks needed by the master
 * @author fviale
 */
public class AOWorker implements InitActive, Serializable, Worker, WorkerMemory {

    /**
     *
     */

    /**
     * log4j logger of the worker
     */
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.MASTERWORKER_WORKERS);

    /**
     * stub on this active object
     */
    protected Object stubOnThis;

    /**
     * Name of the worker
     */
    protected String name;

    /**
     * The entity which will provide tasks to the worker (i.e. the master)
     */
    protected TaskProvider<Serializable> provider;

    /**
     * The memory of the worker <br>
     * the worker can keep some data between different tasks executions <br>
     * e.g. connection to a database, file descriptor, etc ...
     */
    protected Map<String, Object> memory;

    /**
     * The current list of tasks to compute
     */
    protected Queue<TaskIntern<Serializable>> pendingTasks;
    protected Queue<Queue<TaskIntern<Serializable>>> pendingTasksFutures;

    /**
     * ProActive no arg contructor
     */
    public AOWorker() {
    }

    /**
     * Creates a worker with the given name
     * @param name name of the worker
     * @param provider the entity which will provide tasks to the worker
     * @param initialMemory initial memory of the worker
     */
    public AOWorker(final String name, final TaskProvider<Serializable> provider,
            final Map<String, Object> initialMemory) {
        this.name = name;
        this.provider = provider;
        this.memory = initialMemory;
        this.pendingTasksFutures = new LinkedList<Queue<TaskIntern<Serializable>>>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof AOWorker) {
            return name.equals(((Worker) obj).getName());
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void erase(final String dataName) {
        memory.remove(dataName);
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public void heartBeat() {
        // Do nothing, we simply want the rendez-vous to work
    }

    /**
     * {@inheritDoc}
     */
    public void initActivity(final Body body) {
        stubOnThis = PAActiveObject.getStubOnThis();
        //PAActiveObject.setImmediateService("getName");
        PAActiveObject.setImmediateService("heartBeat");
        PAActiveObject.setImmediateService("terminate");

        //        RequestQueue rq = ((AbstractBody) body).getRequestQueue();
        //        RequestFactory rf = ProActiveMetaObjectFactory.newInstance().newRequestFactory();
        //        long id = ((BodyImpl) body).getNextSequenceID();
        //        Method meth = null;
        //        try {
        //            meth = this.getClass().getMethod("initialGetTask");
        //        } catch (SecurityException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        } catch (NoSuchMethodException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }
        //        MethodCall call = new MethodCall(meth, null, new Object[0]);
        //        Request req = rf.newRequest(call,body, true,id);
        //        rq.add(req);

        // Initial Task
        ((AOWorker) stubOnThis).initialGetTask();
    }

    /**
     * {@inheritDoc}
     */
    public Object load(final String dataName) {
        return memory.get(dataName);
    }

    /**
     * gets the initial task to solve
     * @return initial task to solve
     */
    @SuppressWarnings("unchecked")
    public void initialGetTask() {
        if (logger.isDebugEnabled()) {
            logger.debug(name + " asks a new task...");
        }
        if (provider == null) {
            System.out.println("Boom P");
        }
        if (name == null)
            System.out.println("Boom N");
        if (stubOnThis == null)
            System.out.println("Boom S");

        // InitialTask
        pendingTasks = (Queue<TaskIntern<Serializable>>) PAFuture.getFutureValue(provider.getTasks(
                (Worker) stubOnThis, name));

        // Schedule
        ((AOWorker) stubOnThis).scheduleTask();
    }

    /**
     * Handle a task (run it)
     * @param task task to run
     * @return the same task, but containing the result
     */
    public void handleTask(final TaskIntern<Serializable> task) {
        Serializable resultObj = null;
        ResultInternImpl result = new ResultInternImpl(task);

        // We run the task and listen to exception thrown by the task itself
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(name + " runs task " + task.getId() + "...");
            }

            resultObj = task.run(this);
        } catch (Exception e) {
            result.setException(e);
        }

        // We store the result inside our internal version of the task
        result.setResult(resultObj);
        if (logger.isDebugEnabled()) {
            logger.debug(name + " sends the result of task " + result.getId() + " and asks a new task...");
        }

        // We send the result back to the master
        Queue<TaskIntern<Serializable>> newTasks;
        if ((pendingTasks.size() == 0) && (pendingTasksFutures.size() == 0)) {
            if (logger.isDebugEnabled()) {
                logger.debug(name + " requests a task flooding...");
            }
            newTasks = provider.sendResultAndGetTasks(result, name, true);
        } else {
            newTasks = provider.sendResultAndGetTasks(result, name, false);
        }
        pendingTasksFutures.offer(newTasks);

        // Schedule
        ((AOWorker) stubOnThis).scheduleTask();
    }

    public void scheduleTask() {
        while ((pendingTasks.size() == 0) && (pendingTasksFutures.size() > 0)) {
            pendingTasks.addAll(pendingTasksFutures.remove());
        }
        if (pendingTasks.size() > 0) {
            TaskIntern<Serializable> newTask = pendingTasks.remove();
            // We handle the current Task
            ((AOWorker) stubOnThis).handleTask(newTask);

        } else {
            // if there is nothing to do we sleep i.e. we do nothing
            if (logger.isDebugEnabled()) {
                logger.debug(name + " sleeps...");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void save(final String dataName, final Object data) {
        memory.put(dataName, data);
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper terminate() {
        if (logger.isDebugEnabled()) {
            logger.debug("Terminating " + name + "...");
        }

        PAActiveObject.terminateActiveObject(true);
        if (logger.isDebugEnabled()) {
            logger.debug(name + " terminated...");
        }

        return new BooleanWrapper(true);
    }

    /**
     * {@inheritDoc}
     */
    public void wakeup() {
        if (logger.isDebugEnabled()) {
            logger.debug(name + " wakes up...");
        }
        // Initial Task
        ((AOWorker) stubOnThis).initialGetTask();
    }
}
