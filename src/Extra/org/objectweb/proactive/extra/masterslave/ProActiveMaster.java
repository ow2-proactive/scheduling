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
import java.util.NoSuchElementException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extra.masterslave.core.AOMaster;
import org.objectweb.proactive.extra.masterslave.core.TaskAlreadySubmittedException;
import org.objectweb.proactive.extra.masterslave.core.TaskException;
import org.objectweb.proactive.extra.masterslave.core.TaskWrapperImpl;
import org.objectweb.proactive.extra.masterslave.interfaces.Master;
import org.objectweb.proactive.extra.masterslave.interfaces.Task;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.ResultIntern;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.TaskIntern;


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
        wrappedTasks = new HashMap<T, TaskIntern>();
        wrappedTasksRev = new HashMap<TaskIntern, T>();
    }

    /**
     * Creates a master with a collection of nodes
     * @param nodes
     */
    public ProActiveMaster(Collection<Node> nodes) {
        this();
        try {
            aomaster = (AOMaster) ProActive.newActive(AOMaster.class.getName(),
                    new Object[] { nodes });
        } catch (ActiveObjectCreationException e) {
            throw new IllegalArgumentException(e);
        } catch (NodeException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Creates a master with a descriptorURL and an array of virtual node names
     * @param descriptorURL
     * @param virtualNodeNames
     */
    public ProActiveMaster(URL descriptorURL, String virtualNodeName) {
        this();
        try {
            aomaster = (AOMaster) ProActive.newActive(AOMaster.class.getName(),
                    new Object[] { descriptorURL, virtualNodeName });
        } catch (ActiveObjectCreationException e) {
            throw new IllegalArgumentException(e);
        } catch (NodeException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Creates a master with the given virtual node
     * @param virtualNode
     */
    public ProActiveMaster(VirtualNode virtualNode) {
        try {
            aomaster = (AOMaster) ProActive.newActive(AOMaster.class.getName(),
                    new Object[] { virtualNode });
        } catch (ActiveObjectCreationException e) {
            throw new IllegalArgumentException(e);
        } catch (NodeException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#addResources(java.util.Collection)
     */
    public void addResources(Collection<Node> nodes) {
        aomaster.addResources(nodes);
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

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#areAllResultsAvailable()
     */
    public boolean areAllResultsAvailable() {
        return aomaster.areAllResultsAvailable();
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
    private Collection<TaskIntern> createWrappings(Collection<T> tasks)
        throws IllegalArgumentException {
        Collection<TaskIntern> wrappings = new ArrayList<TaskIntern>();
        for (T task : tasks) {
            wrappings.add(createWrapping(task));
        }
        return wrappings;
    }

    /**
     * Returns a list of wrappers associated with these tasks
     * @param tasks
     * @return list of internal tasks
     * @throws NoSuchElementException
     */
    private List<TaskIntern> getWrappings(List<T> tasks)
        throws NoSuchElementException {
        List<TaskIntern> answer = new ArrayList<TaskIntern>();
        for (Task task : tasks) {
            if (!wrappedTasks.containsKey(task)) {
                throw new NoSuchElementException();
            }
            answer.add(wrappedTasks.get(task));
        }
        return answer;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#isOneResultAvailable()
     */
    public boolean isOneResultAvailable() {
        return aomaster.isOneResultAvailable();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#isResultAvailable(org.objectweb.proactive.extra.masterslave.interfaces.Task)
     */
    public boolean isResultAvailable(T task) throws IllegalArgumentException {
        if (!wrappedTasks.containsKey(task)) {
            throw new IllegalArgumentException("Non existent wrapping.");
        }
        TaskIntern wrapper = wrappedTasks.get(task);
        return aomaster.isResultAvailable(wrapper);
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

    /**
     * Removes the internal wrapping associated with this collectioon of tasks
     * @param tasks
     * @throws IllegalArgumentException
     */
    private void removeWrappings(Collection<T> tasks)
        throws IllegalArgumentException {
        for (T task : tasks) {
            removeWrapping(task);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#solve(org.objectweb.proactive.extra.masterslave.interfaces.Task)
     */
    public void solve(T task) {
        TaskIntern wrapper = createWrapping(task);
        aomaster.solve(wrapper);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#solveAll(java.util.Collection)
     */
    public void solveAll(Collection<T> tasks) {
        Collection<TaskIntern> wrappers = createWrappings(tasks);
        aomaster.solveAll(wrappers);
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
    public Collection<R> waitAllResults() throws TaskException {
        Collection<ResultIntern> resultsIntern = (Collection<ResultIntern>) ProActive.getFutureValue(aomaster.waitAllResults());
        Collection<R> results = new ArrayList<R>();
        for (ResultIntern res : resultsIntern) {
            results.add((R) res.getResult());
            T task = wrappedTasksRev.get(res.getTask());
            removeWrapping(task);
        }
        return results;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#waitOneResult()
     */
    public R waitOneResult() throws TaskException {
        ResultIntern res = (ResultIntern) ProActive.getFutureValue(aomaster.waitOneResult());
        TaskIntern wrapper = res.getTask();

        //  we remove the mapping between the task and its wrapper
        T task = wrappedTasksRev.get(wrapper);
        removeWrapping(task);
        if (wrapper.threwException()) {
            throw new TaskException(wrapper.getException());
        }
        return (R) res.getResult();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#waitResultOf(org.objectweb.proactive.extra.masterslave.interfaces.Task)
     */
    public R waitResultOf(T task)
        throws IllegalArgumentException, TaskException {
        if (!wrappedTasks.containsKey(task)) {
            throw new NoSuchElementException();
        }
        TaskIntern wrapper = wrappedTasks.get(task);
        ResultIntern res = (ResultIntern) ProActive.getFutureValue(aomaster.waitResultOf(
                    wrapper));
        removeWrapping(task);
        if (wrapper.threwException()) {
            throw new TaskException(wrapper.getException());
        }
        return (R) res.getResult();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#waitResultsOf(java.util.List)
     */
    public List<R> waitResultsOf(List<T> tasks)
        throws IllegalArgumentException, TaskException {
        List<TaskIntern> wrappers = getWrappings(tasks);
        List<ResultIntern> resultsIntern = (List<ResultIntern>) ProActive.getFutureValue(aomaster.waitResultsOf(
                    wrappers));
        List<R> results = new ArrayList<R>();
        for (ResultIntern res : resultsIntern) {
            results.add((R) res.getResult());
        }
        removeWrappings(tasks);
        return results;
    }
}
