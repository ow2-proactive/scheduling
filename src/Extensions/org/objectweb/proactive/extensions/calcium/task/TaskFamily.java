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
package org.objectweb.proactive.extensions.calcium.task;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.exceptions.MuscleException;
import org.objectweb.proactive.extensions.calcium.stateness.Stateness;


public class TaskFamily<T> implements Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS);
    Task<T> parent;
    public Vector<Task<T>> childrenReady; //sub tasks ready for execution
    Hashtable<TaskId, Task<T>> childrenWaiting; //sub tasks being awaited (computed)
    public Vector<Task<T>> childrenFinished; //sub tasks completed

    public TaskFamily(Task<T> parent) {
        this.parent = parent;

        childrenReady = new Vector<Task<T>>();
        childrenWaiting = new Hashtable<TaskId, Task<T>>();
        childrenFinished = new Vector<Task<T>>();
    }

    public boolean hasReadyChildTask() {
        return !childrenReady.isEmpty();
    }

    /**
     * Gets a sub task ready for execution. Internally,
     * the subtask will be remembered by putting it in
     * the waite queue.
     * @return A sub task ready for execution.
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public synchronized Task<T> getReadyChild() {
        if (childrenReady.isEmpty()) {
            return null;
        }

        Task<T> task = this.childrenReady.remove(0);
        this.childrenWaiting.put(task.taskId, task);
        return task;
    }

    /**
     * This method is used to eliminate shared references of objects
     * between two task parameters.
     *
     * Each task's parameter is serialized via a deep copy and stored
     * again on each task. As a result if two task's parameters shared
     * an object reference, now each task will have it's own reference on a copy of the object.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void splitfReadyTasksSpace()
        throws IOException, ClassNotFoundException {
        for (Task<T> task : this.childrenReady) {
            task.setObject(Stateness.deepCopy(task.getObject()));
        }
    }

    /**
     * Adds a subtask ready for execution. This subtask
     * will be configured with the parent id (this task's id),
     * and also with a priority higher than it's parent.
     * @param child The sub task.
     */
    @SuppressWarnings("unchecked")
    public synchronized void addReadyChild(Task<?> child) {
        child.priority = parent.priority.getNewChildPriority();
        child.taskId = parent.taskId.getNewChildId();

        this.childrenReady.add((Task<T>) child);
    }

    public synchronized boolean hasFinishedChild() {
        return !this.childrenFinished.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public synchronized boolean setFinishedChild(Task<?> task) {
        if (!task.isFinished()) {
            logger.error("Task id=" + task + " claims to be unfinished.");
            return false;
        }

        if (task.taskId.getParentId().value() != parent.taskId.value()) {
            logger.error("Setting other task's child as my child: child.id=" +
                " task.parent.id=" + task.taskId.getParentId());
            return false; //not my child
        }

        if (!this.childrenWaiting.containsKey(task.taskId)) {
            logger.error("Parent id=" + parent.taskId.value() +
                " not waiting for child: task.id=" + task.taskId.value());
            return false;
        }

        childrenWaiting.remove(task.taskId);

        childrenFinished.add((Task<T>) task);

        parent.stats.addChildStats(task.getStats());

        return true;
    }

    public void setReadyChildParams(T[] param) {
        setParams(this.childrenReady, param);
    }

    public void setFinishedChildParams(T[] param) {
        setParams(this.childrenFinished, param);
    }

    public T[] getFinishedChildParams() {
        return getParams(childrenFinished);
    }

    public T[] getReadyChildParams() {
        return getParams(childrenReady);
    }

    static private <Y> void setParams(Vector<Task<Y>> queue, Y[] param) {
        if (param.length != queue.size()) {
            String msg = "Number of parameters (" + param.length +
                ") does not match number of subtasks (" + queue.size() + ")";
            logger.error(msg);
            throw new MuscleException(msg);
        }

        Collections.sort(queue);

        for (int i = 0; i < param.length; i++) {
            queue.get(i).setObject(param[i]);
        }
    }

    @SuppressWarnings("unchecked")
    static private <T> T[] getParams(Vector<Task<T>> queue) {
        if (queue.size() <= 0) {
            String msg = "Need at least one child to get parameters!";
            logger.error(msg);
            throw new MuscleException(msg);
        }

        Collections.sort(queue);

        Vector<T> childResults = new Vector<T>();
        for (Task<T> t : queue) {
            childResults.add(t.getObject());
        }

        T[] res = (T[]) Array.newInstance(childResults.get(0).getClass(),
                childResults.size());
        return childResults.toArray(res);
    }
}
