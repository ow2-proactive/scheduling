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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.masterslave.TaskException;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.MasterIntern;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.ResultIntern;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.Slave;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveDeadListener;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveManager;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveManagerAdmin;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveWatcher;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskIntern;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskProvider;
import org.objectweb.proactive.extra.masterslave.util.HashSetQueue;


/**
 * Main Active Object of the Master/Slave API <br/>
 * Literally : the entity to which an user can submit tasks to be solved
 * @author fviale
 */
public class AOMaster implements Serializable, TaskProvider, InitActive,
    RunActive, MasterIntern, SlaveDeadListener {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.MASTERSLAVE);

    // stub on this active object
    protected Object stubOnThis;

    // global variables
    private boolean terminated; // is the master terminated
    private static enum OrderingMode {UNSPECIFIED, UNORDERED, ORDERED;
    }
    private OrderingMode mode; // current ordering mode
    private long firstId;

    // Slave manager (deploy slaves)
    protected SlaveManager smanager;

    // Pinger (checks that slaves are alive)
    protected SlaveWatcher pinger;

    // Slaves : effective resources
    Slave slaveGroupStub;
    Group slaveGroup;

    // Sleeping slaves (we might want to wake them up)
    Slave sleepingGroupStub;
    Group sleepingGroup;
    HashMap<String, Slave> slavesByName;
    HashMap<Slave, String> slavesByNameRev;
    HashMap<String, TaskIntern> slavesActivity;

    // Task Queues :
    // tasks that wait for an available slave
    protected HashSetQueue<TaskIntern> pendingTasks;

    // tasks that are currently processing
    protected HashSetQueue<TaskIntern> launchedTasks;

    // tasks that are completed
    protected HashSetQueue<TaskIntern> completedTasks; // queue in unordered mode
    protected ArrayList<TaskIntern> completedTasksOrdered; // list in ordered mode 

    public AOMaster() {
        // proactive emty no arg constructor
    }

    /**
     * Creates a master with an existing slave manager
     * @param rmanager
     * @throws IllegalArgumentException
     */
    public AOMaster(SlaveManager rmanager) throws IllegalArgumentException {
        if (rmanager == null) {
            throw new IllegalArgumentException(new NullPointerException());
        }
        this.smanager = rmanager;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#addResources(java.util.Collection)
     */
    public void addResources(Collection nodes) {
        ((SlaveManagerAdmin) smanager).addResources(nodes);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#addResources(java.net.URL, java.lang.String[])
     */
    public void addResources(URL descriptorURL, String virtualNodeName) {
        ((SlaveManagerAdmin) smanager).addResources(descriptorURL,
            virtualNodeName);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#addResources(org.objectweb.proactive.core.descriptor.data.VirtualNode)
     */
    public void addResources(VirtualNode virtualnode) {
        ((SlaveManagerAdmin) smanager).addResources(virtualnode);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#areAllResultsAvailable()
     */
    public boolean areAllResultsAvailable() {
        if (mode == OrderingMode.UNSPECIFIED) {
            throw new IllegalStateException("Master empty.");
        }

        return pendingTasks.isEmpty() && launchedTasks.isEmpty();
    }

    /**
     * Tells if the master has some activity
     * @return master activity
     */
    private boolean emptyPending() {
        return pendingTasks.isEmpty();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskProvider#getTask(java.lang.String)
     */
    public TaskIntern getTask(Slave slave, String slaveName) {
        // if we don't know him, we record the slave in our system
        if (!slavesByName.containsKey(slaveName)) {
            recordSlave(slave, slaveName);
        }

        if (emptyPending()) {
            slavesActivity.put(slaveName, new TaskWrapperImpl());
            sleepingGroup.add(slave);
            // we return the null task, this will cause the slave to sleep for a while
            return new TaskWrapperImpl();
        } else {
            if (sleepingGroup.contains(slave)) {
                sleepingGroup.remove(slave);
            }
            Iterator<TaskIntern> it = pendingTasks.iterator();
            TaskIntern wrapper = it.next();
            // We remove the task from the pending list
            it.remove();
            // We add the task inside the launched list
            launchedTasks.add(wrapper);
            slavesActivity.put(slaveName, wrapper);

            return wrapper;
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        stubOnThis = ProActive.getStubOnThis();
        // General initializations
        terminated = false;
        mode = OrderingMode.UNSPECIFIED;
        firstId = -1;
        // Queues 
        pendingTasks = new HashSetQueue<TaskIntern>();
        launchedTasks = new HashSetQueue<TaskIntern>();
        completedTasks = new HashSetQueue<TaskIntern>();
        completedTasksOrdered = new ArrayList();

        // Slaves
        try {
            slaveGroupStub = (Slave) ProActiveGroup.newGroup(AOSlave.class.getName());
            slaveGroup = ProActiveGroup.getGroup(slaveGroupStub);
            sleepingGroupStub = (Slave) ProActiveGroup.newGroup(AOSlave.class.getName());
            sleepingGroup = ProActiveGroup.getGroup(sleepingGroupStub);
            slavesActivity = new HashMap<String, TaskIntern>();
            slavesByName = new HashMap<String, Slave>();
            slavesByNameRev = new HashMap<Slave, String>();
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            // The resource manager
            smanager = (AOSlaveManager) ProActive.newActive(AOSlaveManager.class.getName(),
                    new Object[] { stubOnThis });

            // The slave pinger
            pinger = (SlaveWatcher) ProActive.newActive(AOPinger.class.getName(),
                    new Object[] { stubOnThis });
        } catch (ActiveObjectCreationException e1) {
            e1.printStackTrace();
        } catch (NodeException e1) {
            e1.printStackTrace();
        }
    }

    public boolean isEmpty() {
        return pendingTasks.isEmpty() && launchedTasks.isEmpty() &&
        completedTasks.isEmpty() && completedTasksOrdered.isEmpty();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveDeadListener#isDead(org.objectweb.proactive.extra.masterslave.interfaces.internal.Slave)
     */
    public void isDead(Slave slave) {
        String slaveName = slavesByNameRev.get(slave);
        if (logger.isDebugEnabled()) {
            logger.debug(slaveName + " reported missing... removing it");
        }
        if (slaveGroup.contains(slave)) {
            slaveGroup.remove(slave);
            if (sleepingGroup.contains(slave)) {
                sleepingGroup.remove(slave);
            }
            slavesByNameRev.remove(slave);
            slavesByName.remove(slaveName);
            TaskIntern wrapper = slavesActivity.get(slaveName);
            if (!wrapper.isNull() && launchedTasks.contains(wrapper)) {
                launchedTasks.remove(wrapper);
                pendingTasks.add(wrapper);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#isOneResultAvailable()
     */
    public boolean isOneResultAvailable() {
        if (mode == OrderingMode.UNORDERED) {
            return !completedTasks.isEmpty();
        } else if (mode == OrderingMode.ORDERED) {
            return (completedTasksOrdered.get(0) != null);
        } else {
            throw new IllegalStateException("Master is empty.");
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveConsumer#receiveSlave(org.objectweb.proactive.extra.masterslave.interfaces.internal.Slave)
     */
    public boolean recordSlave(Slave slave, String slaveName) {
        // We record the slave in our system
        slavesByName.put(slaveName, slave);
        slavesByNameRev.put(slave, slaveName);
        slaveGroup.add(slave);

        // We tell the pinger to watch for this new slave
        pinger.addSlaveToWatch(slave);
        return true;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.RunActive#runActivity(org.objectweb.proactive.Body)
     */
    public void runActivity(Body body) {
        Service service = new Service(body);
        while (!terminated) {
            service.serveAll("getTask");
            service.serveAll("sendResultAndGetTask");
            service.serveAll("isDead");

            service.serveOldest(new MainFilter());
        }
        try {
            body.terminate();
        } catch (IOException e) {
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskProvider#sendResultAndGetTask(org.objectweb.proactive.extra.masterslave.interfaces.Task,java.lang.String)
     */
    public TaskIntern sendResultAndGetTask(TaskIntern task,
        String originatorName) {
        if (launchedTasks.contains(task)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Result of task " + task.getId() + " received.");
            }
            launchedTasks.remove(task);
            if (mode == OrderingMode.UNORDERED) {
                if (!completedTasks.add(task)) {
                    logger.error("Task is already a completed Task");
                }
            } else {
                completedTasksOrdered.set((int) (task.getId() - firstId), task);
            }
        }

        // We assign a new task to the slave
        TaskIntern newTask = getTask(slavesByName.get(originatorName),
                originatorName);
        return newTask;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.MasterIntern#slavepoolSize()
     */
    public int slavepoolSize() {
        return slaveGroup.size();
    }

    /**
     * Adds a task to solve
     * @param task
     * @throws IllegalArgumentException
     */
    private void solve(TaskIntern task) throws IllegalArgumentException {
        //    	 if in ordered mode we prepare the future result slot 
        if (mode == OrderingMode.ORDERED) {
            // we store the first id of tasks to know the beginning of the task set
            if (this.isEmpty()) {
                firstId = task.getId();
            }
            completedTasksOrdered.add(null);
        }
        if (emptyPending()) {
            pendingTasks.add(task);

            if (logger.isDebugEnabled()) {
                logger.debug("Waking up sleeping slaves...");
            }

            // We wake up the sleeping guys
            sleepingGroupStub.wakeup();
        } else {
            pendingTasks.add(task);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#solveAll(java.util.Collection)
     */
    public void solveAll(Collection<TaskIntern> tasks, boolean ordered)
        throws IllegalArgumentException {
        if (ordered && (mode == OrderingMode.UNORDERED)) {
            throw new IllegalArgumentException(
                "Master result Mode was already set to \"Unordered\".");
        } else if (!ordered && (mode == OrderingMode.ORDERED)) {
            throw new IllegalArgumentException(
                "Master result Mode was already set to \"Ordered\".");
        }

        if (mode == OrderingMode.UNSPECIFIED) {
            if (ordered) {
                mode = OrderingMode.ORDERED;
            } else {
                mode = OrderingMode.UNORDERED;
            }
        }

        logger.debug("Adding " + tasks.size() + " tasks...");

        for (TaskIntern task : tasks) {
            solve(task);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#terminate(boolean)
     */
    public void terminate(boolean freeResources) {
        terminateIntern(freeResources);
    }

    /**
     * Synchronous version of terminate
     * @param freeResources
     * @return
     */
    public boolean terminateIntern(boolean freeResources) {
        terminated = true;
        if (logger.isDebugEnabled()) {
            logger.debug("Terminating Master...");
        }

        // We empty every queues
        pendingTasks.clear();
        launchedTasks.clear();
        completedTasks.clear();

        slavesActivity.clear();
        slavesByName.clear();
        slavesByNameRev.clear();

        // We give the slaves back to the resource manager
        List<Slave> slavesToFree = new ArrayList<Slave>();
        while (slaveGroup.size() > 0) {
            Slave slaveToRemove = (Slave) slaveGroup.remove(0);
            pinger.removeSlaveToWatch(slaveToRemove);
            slavesToFree.add(slaveToRemove);
        }
        smanager.freeSlaves(slavesToFree);

        // We terminate the pinger
        ProActive.waitFor(pinger.terminate());
        // We terminate the slave manager
        ProActive.waitFor(((SlaveManagerAdmin) smanager).terminate(
                freeResources));
        if (logger.isDebugEnabled()) {
            logger.debug("Master terminated...");
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#waitAllResults()
     */
    public Collection<ResultIntern> waitAllResults()
        throws IllegalStateException, TaskException {
        List<ResultIntern> results = new ArrayList<ResultIntern>();
        if (mode == OrderingMode.UNORDERED) {
            while (!completedTasks.isEmpty()) {
                results.add(waitOneResult());
            }
        } else {
            while (!completedTasksOrdered.isEmpty()) {
                results.add(waitOneResult());
            }
        }
        return results;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#waitOneResult()
     */
    public ResultIntern waitOneResult()
        throws IllegalStateException, TaskException {
        TaskIntern wrapper = null;
        if (mode == OrderingMode.UNORDERED) {
            wrapper = completedTasks.poll();
        } else {
            wrapper = completedTasksOrdered.get(0);
            completedTasksOrdered.remove(0);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Result of task" + wrapper.getId() +
                " received by the user.");
        }

        // If there is nothing more to serve, we reset the mode
        if (this.isEmpty()) {
            mode = OrderingMode.UNSPECIFIED;
            firstId = -1;
        }

        // We return the result
        return new ResultInternImpl(wrapper.getResult(), wrapper);
    }

    /**
     * @author fviale
     * Internal class for filtering requests in the queue
     */
    private class MainFilter implements RequestFilter {
        public MainFilter() {
        }

        /* (non-Javadoc)
         * @see org.objectweb.proactive.core.body.request.RequestFilter#acceptRequest(org.objectweb.proactive.core.body.request.Request)
         */
        public boolean acceptRequest(Request request) {
            // We filter the requests with the following strategy :
            // If a request asks for the result of a task, we don't serve it until the result is available
            if (request.getMethodName().equals("waitOneResult")) {
                return isOneResultAvailable();
            } else if (request.getMethodName().equals("waitAllResults")) {
                return areAllResultsAvailable();
            } else {
                return true;
            }
        }
    }
}
