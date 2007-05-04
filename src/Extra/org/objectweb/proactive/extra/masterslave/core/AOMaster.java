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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

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
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.masterslave.TaskException;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.MasterIntern;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.ResultIntern;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.Slave;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveConsumer;
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
public class AOMaster implements Serializable, TaskProvider, SlaveConsumer,
    InitActive, RunActive, MasterIntern, SlaveDeadListener {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.MASTERSLAVE);

    // stub on this active object
    protected Object stubOnThis;
    private boolean terminated;

    // Slave manager (deploy slaves)
    protected SlaveManager smanager;

    // Pinger (checks that slaves are alive)
    protected SlaveWatcher pinger;

    // Slaves : effective resources
    protected int resourceReserved;
    Slave slaveGroupStub;
    Group slaveGroup;
    HashMap<String, Slave> slavesByName;
    HashMap<Slave, String> slavesByNameRev;
    HashMap<String, TaskIntern> slavesActivity;

    // Task Queues :
    // tasks that wait for an available slave
    protected HashSetQueue<TaskIntern> pendingTasks;

    // tasks that are currently processing
    protected HashSetQueue<TaskIntern> launchedTasks;

    // tasks that are completed
    protected HashSetQueue<TaskIntern> completedTasks;

    public AOMaster() {
    }

    /**
     * Creates a master with a collection of nodes
     * @param nodes
     * @throws IllegalArgumentException
     */
    public AOMaster(Node[] nodes) throws IllegalArgumentException {
        try {
            smanager = (AOSlaveManager) ProActive.newActive(AOSlaveManager.class.getName(),
                    new Object[] {  });
            List<Node> nodeList = Arrays.asList(nodes);

            ((SlaveManagerAdmin) smanager).addResources(nodeList);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
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

    /**
     * Creates a master with a descriptor and some virtual node names
     * @param descriptorURL
     * @param virtualNodeNames
     * @throws IllegalArgumentException
     */
    public AOMaster(URL descriptorURL, String virtualNodeName)
        throws IllegalArgumentException {
        try {
            smanager = (AOSlaveManager) ProActive.newActive(AOSlaveManager.class.getName(),
                    new Object[] {  });
            ((SlaveManagerAdmin) smanager).addResources(descriptorURL,
                virtualNodeName);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a master with a virtual node
     * @param virtualnode
     * @throws IllegalArgumentException
     */
    public AOMaster(VirtualNode virtualnode) throws IllegalArgumentException {
        try {
            smanager = (AOSlaveManager) ProActive.newActive(AOSlaveManager.class.getName(),
                    new Object[] {  });

            ((SlaveManagerAdmin) smanager).addResources(virtualnode);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
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
        return pendingTasks.isEmpty() && launchedTasks.isEmpty();
    }

    /**
     * are results of this list of tasks available
     * @param tasks list of tasks
     * @return true if all the results are available
     * @throws NoSuchElementException
     */
    private boolean areResultsAvailable(List<TaskIntern> tasks)
        throws NoSuchElementException {
        return completedTasks.containsAll(tasks);
    }

    /**
     * Tells if the master has some activity
     * @return master activity
     */
    private boolean emptyPending() {
        return pendingTasks.isEmpty();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskProvider#getTask(org.objectweb.proactive.extra.masterslave.interfaces.internal.Slave)
     */
    public TaskIntern getTask(String originatorName) {
        if (emptyPending()) {
            // we return the null task, this will cause the slave to sleep for a while
            return new TaskWrapperImpl();
        } else {
            Iterator<TaskIntern> it = pendingTasks.iterator();
            TaskIntern wrapper = it.next();
            // We remove the task from the pending list
            it.remove();
            // We add the task inside the launched list
            launchedTasks.add(wrapper);
            slavesActivity.put(originatorName, wrapper);

            return wrapper;
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        stubOnThis = ProActive.getStubOnThis();
        // General initializations
        resourceReserved = 0;
        terminated = false;
        // Queues 
        pendingTasks = new HashSetQueue<TaskIntern>();
        launchedTasks = new HashSetQueue<TaskIntern>();
        completedTasks = new HashSetQueue<TaskIntern>();
        ;

        // Slaves
        try {
            slaveGroupStub = (Slave) ProActiveGroup.newGroup(AOSlave.class.getName());
            slaveGroup = ProActiveGroup.getGroup(slaveGroupStub);
            slavesActivity = new HashMap<String, TaskIntern>();
            slavesByName = new HashMap<String, Slave>();
            slavesByNameRev = new HashMap<Slave, String>();
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // The slave pinger
        try {
            pinger = (SlaveWatcher) ProActive.newActive(AOPinger.class.getName(),
                    new Object[] { stubOnThis });
        } catch (ActiveObjectCreationException e1) {
            e1.printStackTrace();
        } catch (NodeException e1) {
            e1.printStackTrace();
        }

        // Our resource utilisation strategy is currently brute-force : we reserve as many ressources as available
        // TODO make a better Resource usage policy
        smanager.reserveAllSlaves((SlaveConsumer) stubOnThis);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveDeadListener#isDead(org.objectweb.proactive.extra.masterslave.interfaces.internal.Slave)
     */
    public void isDead(Slave slave) {
        String slaveName = slavesByNameRev.get(slave);
        logger.debug(slaveName + " reported missing... removing it");
        if (slaveGroup.contains(slave)) {
            slaveGroup.remove(slave);
            slavesByNameRev.remove(slave);
            slavesByName.remove(slaveName);
            TaskIntern wrapper = slavesActivity.get(slaveName);
            if (launchedTasks.contains(wrapper)) {
                launchedTasks.remove(wrapper);
                pendingTasks.add(wrapper);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#isOneResultAvailable()
     */
    public boolean isOneResultAvailable() {
        return !completedTasks.isEmpty();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#isResultAvailable()
     */
    public boolean isResultAvailable(TaskIntern task)
        throws IllegalArgumentException {
        return completedTasks.contains(task);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveConsumer#receiveSlave(org.objectweb.proactive.extra.masterslave.interfaces.internal.Slave)
     */
    public void receiveSlave(Slave slave) {
        // We record the slave in our system
        String slaveName = slave.getName();
        slavesByName.put(slaveName, slave);
        slavesByNameRev.put(slave, slaveName);
        slaveGroup.add(slave);
        slavesActivity.put(slaveName, new TaskWrapperImpl());
        // We connect the slave to us
        slave.setProvider((TaskProvider) stubOnThis);

        // We tell the pinger to watch for this new slave
        pinger.addSlaveToWatch(slave);

        // if there is some activity, we wake up the slave
        if (!emptyPending()) {
            slave.wakeup();
        }
        // One of the previously reserved resource is now available
        resourceReserved--;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.RunActive#runActivity(org.objectweb.proactive.Body)
     */
    public void runActivity(Body body) {
        Service service = new Service(body);
        while (!terminated) {
            service.serveAll("sendResult");
            service.serveAll("receiveSlave");
            service.serveAll(new MainFilter());
        }
        try {
            body.terminate();
        } catch (IOException e) {
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskProvider#sendResult(org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskIntern, org.objectweb.proactive.extra.masterslave.interfaces.internal.Slave)
     */
    public void sendResult(TaskIntern task, String originatorName) {
        if (launchedTasks.contains(task)) {
            logger.debug("Result of task " + task.getId() + " received.");
            launchedTasks.remove(task);
            if (!completedTasks.add(task)) {
                logger.error("Task is already a completed Task");
            }
            // This slave is now idle
            slavesActivity.put(originatorName, new TaskWrapperImpl());
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.MasterIntern#slavepoolSize()
     */
    public int slavepoolSize() {
        return slaveGroup.size();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#solve(org.objectweb.proactive.extra.masterslave.interfaces.Task)
     */
    public void solve(TaskIntern task) throws IllegalArgumentException {
        if (emptyPending()) {
            pendingTasks.add(task);
            slaveGroupStub.wakeup();
        } else {
            pendingTasks.add(task);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#solveAll(java.util.Collection)
     */
    public void solveAll(Collection<TaskIntern> tasks)
        throws IllegalArgumentException {
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
        logger.debug("Terminating Master...");

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

        logger.debug("Master terminated...");
        return true;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#waitAllResults()
     */
    public Collection<ResultIntern> waitAllResults()
        throws IllegalStateException, TaskException {
        logger.debug("All Results received by the user...");
        List<ResultIntern> results = new ArrayList<ResultIntern>();
        while (!completedTasks.isEmpty()) {
            results.add(waitOneResult());
        }
        return results;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#waitOneResult()
     */
    public ResultIntern waitOneResult()
        throws IllegalStateException, TaskException {
        TaskIntern wrapper = completedTasks.poll();

        // We return the result
        return new ResultInternImpl(wrapper.getResult(), wrapper);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#waitResultOf(org.objectweb.proactive.extra.masterslave.interfaces.Task)
     */
    public ResultIntern waitResultOf(TaskIntern task)
        throws IllegalArgumentException, TaskException {
        logger.debug("Result of task " + task.getId() +
            " retrieved by the user");
        Iterator<TaskIntern> it = completedTasks.iterator();
        while (it.hasNext()) {
            TaskIntern candidate = it.next();
            if (candidate.equals(task)) {
                it.remove();
                return new ResultInternImpl(candidate.getResult(), candidate);
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#waitResultsOf(java.util.List)
     */
    public List<ResultIntern> waitResultsOf(List<TaskIntern> tasks)
        throws IllegalArgumentException, TaskException {
        ArrayList<ResultIntern> results = new ArrayList<ResultIntern>();
        for (TaskIntern task : tasks) {
            ResultIntern res = waitResultOf(task);
            results.add(res);
        }
        return results;
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
            } else if (request.getMethodName().equals("waitResultOf")) {
                try {
                    return isResultAvailable((TaskIntern) request.getParameter(
                            0));
                } catch (NoSuchElementException e) {
                    return true;
                }
            } else if (request.getMethodName().equals("waitResultsOf")) {
                // TODO Might be unscalable if the number of tasks gets too high
                try {
                    return areResultsAvailable((List<TaskIntern>) request.getParameter(
                            0));
                } catch (NoSuchElementException e) {
                    return true;
                }
            } else {
                return true;
            }
        }
    }
}
