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
package org.objectweb.proactive.extra.masterslave;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extra.masterslave.core.AOMaster;
import org.objectweb.proactive.extra.masterslave.core.TaskAlreadySubmittedException;
import org.objectweb.proactive.extra.masterslave.core.TaskWrapperImpl;
import org.objectweb.proactive.extra.masterslave.interfaces.Master;
import org.objectweb.proactive.extra.masterslave.interfaces.Task;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskIntern;


/**
 * Entry point of the Master/Slave API.<br/>
 * Here is how the Master/Slave API is used :
 * <ol>
 * <li>Create a ProActiveMaster object through the different constructors</li>
 * <li>Submit tasks through the use of the <b><i>solve</i></b> methods</li>
 * <li>Collect results through the <b><i>wait</i></b> methods</li>
 * </ol>
 * <br/>
 * The <b><i>SlaveMemory</i></b> concept is meant to allow user to store information directly inside the slaves where tasks are executed. <br/>
 * The SlaveMemory has the same structure as a Dictionary with &lt;key, value&gt; pairs where keys are string and values are any Java object. <br/>
 * <br/>
 * A user can specify, when creating the master, the initial memory that every slave will have by providing a Map of &lt;String,Object&gt; pairs to the ProActiveMaster constructors.<br/>
 * <br/>
 * When tasks will later on be executed on the slaves, the tasks will be able to access this memory through the slavememory parameter of the <b><i>run</i></b> method.
 * <br/>
 * The results can be received using two different recpetion order modes: <br/>
 * <ul>
 * <li>In the <b><i>CompletionOrder mode</i></b>, which is the default, results are received in the same order as they are completed by the slaves (i.e. order is unspecified).</li>
 * <li>In the <b><i>SubmissionOrder mode</i></b>, results are received in the same order as they are submitted to the master.</li>
 * </ul>
 * <br/>
 *
 * @see org.objectweb.proactive.extra.masterslave.interfaces.Task
 * @see org.objectweb.proactive.extra.masterslave.interfaces.SlaveMemory
 * @see org.objectweb.proactive.extra.masterslave.interfaces.Master
 *
 *
 * @author fviale
 *
 * @param <T> Task of result R
 * @param <R> Result Object
 */
public class ProActiveMaster<T extends Task<R>, R extends Serializable>
    implements Master<T, R>, Serializable {
    protected AOMaster aomaster = null;

    // IDs management : we keep an internal version of the tasks with an ID internally generated
    protected HashMap<T, TaskIntern> wrappedTasks;
    protected HashMap<TaskIntern, T> wrappedTasksRev;
    protected long taskCounter = 0;

    /**
     * An empty master (you can add resources afterwards)
     */
    public ProActiveMaster() {
        this(new HashMap<String, Object>());
    }

    /**
     * Creates a master with a collection of nodes
     * @param nodes
     */
    public ProActiveMaster(Collection<Node> nodes) {
        this(nodes, new HashMap<String, Object>());
    }

    /**
     * Creates a master with a collection of nodes
     * @param nodes
     */
    public ProActiveMaster(Collection<Node> nodes,
        Map<String, Object> initialMemory) {
        this(initialMemory);
        aomaster.addResources(nodes);
    }

    /**
     * Creates an empty master with an initial slave memory
     * @param initialMemory initial memory that every slaves deployed by the master will have
     */
    public ProActiveMaster(Map<String, Object> initialMemory) {
        wrappedTasks = new HashMap<T, TaskIntern>();
        wrappedTasksRev = new HashMap<TaskIntern, T>();
        try {
            aomaster = (AOMaster) ProActive.newActive(AOMaster.class.getName(),
                    new Object[] { initialMemory });
        } catch (ActiveObjectCreationException e) {
            throw new IllegalArgumentException(e);
        } catch (NodeException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Creates a master with a descriptorURL
     * Every virtual nodes will be activated in the descriptor to collect the resources
     * @param descriptorURL url of the ProActive descriptor
     */
    public ProActiveMaster(URL descriptorURL) {
        this(descriptorURL, new HashMap<String, Object>());
    }

    /**
     * Creates a master with a descriptorURL
     * Every virtual nodes will be activated in the descriptor to collect the resources
     * @param descriptorURL descriptor to use
     * @param virtualNodeName name of the virtual node
     * @param initialMemory initial memory that every slaves deployed by the master will have
     */
    public ProActiveMaster(URL descriptorURL, Map<String, Object> initialMemory) {
        this(initialMemory);
        aomaster.addResources(descriptorURL);
    }

    /**
     * Creates a master with a descriptorURL and the name of a virtual node
     * Only this virtual node will be activated in the descriptor to collect the resources
     * @param descriptorURL url of the ProActive descriptor
     * @param virtualNodeName name of the virtual node to deploy inside the ProActive descriptor
     */
    public ProActiveMaster(URL descriptorURL, String virtualNodeName) {
        this(descriptorURL, virtualNodeName, new HashMap<String, Object>());
    }

    /**
     * Creates a master with a descriptorURL and a virtual node name
     * Only this virtual node will be activated in the descriptor to collect the resources
     * @param descriptorURL descriptor to use
     * @param virtualNodeName name of the virtual node
     * @param initialMemory initial memory that every slaves deployed by the master will have
     */
    public ProActiveMaster(URL descriptorURL, String virtualNodeName,
        Map<String, Object> initialMemory) {
        this(initialMemory);
        aomaster.addResources(descriptorURL, virtualNodeName);
    }

    /**
     * Creates a master with the given virtual node
     * @param virtualNode ProActive virtual node object
     */
    public ProActiveMaster(VirtualNode virtualNode) {
        this(virtualNode, new HashMap<String, Object>());
    }

    /**
     * Creates a master with the given virtual node
     * @param virtualNode ProActive virtual node object
     * @param initialMemory initial memory that every slaves deployed by the master will have
     */
    public ProActiveMaster(VirtualNode virtualNode,
        Map<String, Object> initialMemory) {
        this(initialMemory);
        aomaster.addResources(virtualNode);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#addResources(java.util.Collection)
     */
    public void addResources(Collection<Node> nodes) {
        aomaster.addResources(nodes);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#addResources(java.net.URL)
     */
    public void addResources(URL descriptorURL) {
        aomaster.addResources(descriptorURL);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#addResources(java.net.URL, java.lang.String)
     */
    public void addResources(URL descriptorURL, String virtualNodeName) {
        aomaster.addResources(descriptorURL, virtualNodeName);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#addResources(org.objectweb.proactive.core.descriptor.data.VirtualNode)
     */
    public void addResources(VirtualNode virtualnode) {
        aomaster.addResources(virtualnode);
    }

    public int countAvailableResults() {
        return aomaster.countAvailableResults();
    }

    /**
     * Creates an internal wrapper of the given task
     * This wrapper will identify the task internally via an ID
     * @param task task to be wrapped
     * @return wrapped version
     * @throws IllegalArgumentException if the same task has already been wrapped
     */
    private TaskIntern createWrapping(T task) throws IllegalArgumentException {
        if (wrappedTasks.containsKey(task)) {
            throw new IllegalArgumentException(new TaskAlreadySubmittedException());
        }

        TaskIntern wrapper = new TaskWrapperImpl(taskCounter, task);
        taskCounter = (taskCounter + 1) % (Long.MAX_VALUE - 1);
        wrappedTasks.put(task, wrapper);
        wrappedTasksRev.put(wrapper, task);
        return wrapper;
    }

    /**
     * Creates an internal version of the given collection of tasks
     * This wrapper will identify the task internally via an ID
     * @param tasks collection of tasks to be wrapped
     * @return wrapped version
     * @throws IllegalArgumentException if the same task has already been wrapped
     */
    private List<TaskIntern> createWrappings(List<T> tasks)
        throws IllegalArgumentException {
        List<TaskIntern> wrappings = new ArrayList<TaskIntern>();
        for (T task : tasks) {
            wrappings.add(createWrapping(task));
        }
        return wrappings;
    }

    public boolean isEmpty() {
        return aomaster.isEmpty();
    }

    /**
     * Removes the internal wrapping associated with this task
     * @param task
     * @throws IllegalArgumentException
     */
    private void removeWrapping(T task) throws IllegalArgumentException {
        if (!wrappedTasks.containsKey(task)) {
            throw new IllegalArgumentException("Non existent wrapping.");
        }

        TaskIntern wrapper = wrappedTasks.remove(task);
        wrappedTasksRev.remove(wrapper);
    }

    public void setResultReceptionOrder(
        org.objectweb.proactive.extra.masterslave.interfaces.Master.OrderingMode mode) {
        aomaster.setResultReceptionOrder(mode);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#slavepoolSize()
     */
    public int slavepoolSize() {
        return aomaster.slavepoolSize();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#solveAll(java.util.Collection, boolean)
     */
    public void solve(List<T> tasks) {
        List<TaskIntern> wrappers = createWrappings(tasks);
        aomaster.solve(wrappers);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#terminate(boolean)
     */
    public void terminate(boolean freeResources) {
        // we use here the synchronous version
        aomaster.terminateIntern(freeResources);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#waitAllResults()
     */
    public List<R> waitAllResults() throws TaskException {
        List<TaskIntern> completed = (List<TaskIntern>) ProActive.getFutureValue(aomaster.waitAllResults());
        List<R> results = new ArrayList<R>();
        for (TaskIntern res : completed) {
            if (res.threwException()) {
                throw new TaskException(res.getException());
            }
            Serializable obj = res.getResult();
            if (obj != null) {
                results.add((R) res.getResult());
            } else {
                results.add(null);
            }
            T task = wrappedTasksRev.get(res);
            removeWrapping(task);
        }
        return results;
    }

    public List<R> waitKResults(int k)
        throws IllegalStateException, IllegalArgumentException, TaskException {
        List<TaskIntern> completed = (List<TaskIntern>) ProActive.getFutureValue(aomaster.waitKResults(
                    k));
        List<R> results = new ArrayList<R>();
        for (TaskIntern res : completed) {
            if (res.threwException()) {
                throw new TaskException(res.getException());
            }
            Serializable obj = res.getResult();
            if (obj != null) {
                results.add((R) res.getResult());
            } else {
                results.add(null);
            }
            T task = wrappedTasksRev.get(res);
            removeWrapping(task);
        }
        return results;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#waitOneResult()
     */
    public R waitOneResult() throws TaskException {
        TaskIntern completed = (TaskIntern) ProActive.getFutureValue(aomaster.waitOneResult());
        if (completed.threwException()) {
            throw new TaskException(completed.getException());
        }

        //  we remove the mapping between the task and its wrapper
        T task = wrappedTasksRev.get(completed);
        removeWrapping(task);
        if (completed.threwException()) {
            throw new TaskException(completed.getException());
        }
        Serializable obj = completed.getResult();
        if (obj != null) {
            return (R) completed.getResult();
        }
        return null;
    }
}
