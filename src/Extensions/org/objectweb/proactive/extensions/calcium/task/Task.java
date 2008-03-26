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

import java.io.Serializable;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.exceptions.TaskException;
import org.objectweb.proactive.extensions.calcium.instructions.Instruction;
import org.objectweb.proactive.extensions.calcium.statistics.StatsImpl;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;


/**
 * This class is the main wrapper class for the objects passed to the task pool.
 * Users should never know that this class exists.
 *
 * Among others, this class: provides a wrapper for the user
 * parameters, holds the instruction stack for this task, handles the creation
 * and conquer of child tasks (subtasks).
 *
 * @author The ProActive Team
 *
 */
public class Task<T> implements Serializable, Comparable<Task> {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS);

    //Rebirth preserved parameters
    private T param;
    public TaskId taskId;
    public TaskId parentId;
    TaskPriority priority;
    private boolean isTainted;
    public StatsImpl stats;

    //The program stack. Higher indexed elements are served first (LIFO).
    private Stack<Instruction> stack;

    //Not preserved in rebirth
    public TaskFamily<T> family;
    private Exception exception;

    /**
     * ProActive's empty constructor. Don't use!!
     */
    @Deprecated
    public Task() {
    }

    private Task(T object, TaskId taskId, TaskPriority priority) {
        this.param = object;
        this.taskId = taskId;
        this.priority = priority;

        this.family = new TaskFamily<T>(this);

        stats = new StatsImpl();
        stack = new Stack<Instruction>();

        exception = null;
    }

    public Task(T object) {
        this(object, new TaskId(), new TaskPriority());
    }

    /**
     * Makes a new task that represents a rebirth of the current one.
     * All parameters like: id, priority, parentId, computationTime, and current
     * instruction stack are preserved.
     * The child (subtasks) references are not preserved,
     * and the contained object is the one passed as parameter.
     * @param object The new object to be hold in this task.
     * @return A new birth of the current task containting object
     */
    public <R> Task<R> reBirth(R object) {
        Task<R> newMe = new Task<R>(object, taskId, priority);
        newMe.setStack(this.stack);

        newMe.isTainted = this.isTainted;

        newMe.stats = this.stats;

        return newMe;
    }

    public int compareTo(Task task) {
        int comp;

        //priority tasks go first
        comp = task.priority.priority - this.priority.priority;
        if (comp != 0) {
            return comp;
        }

        //if priority is tied then consider fifo order of root task
        comp = taskId.getFamilyId().value() - this.taskId.getFamilyId().value();
        if (comp != 0) {
            return comp;
        }

        //if two tasks belong to the same family then consider family hierarchy
        comp = task.priority.intraFamilyPri - this.priority.intraFamilyPri;
        if (comp != 0) {
            return comp;
        }

        int taskParentValue = (task.taskId.getParentId() != null) ? task.taskId.getParentId().value() : (-1);
        int thisParentValue = (this.taskId.getParentId() != null) ? this.taskId.getParentId().value() : (-1);
        return taskParentValue - thisParentValue;
    }

    @Override
    public int hashCode() {
        return taskId.value();
    }

    @Override
    public boolean equals(Object o) {

        /*
            if(!(o instanceof Task)){
                    return false;
            }
         */
        Task other = (Task) o;
        return this.taskId.equals(other.taskId);
    }

    /**
     * Gives a not so shallow copy of the stack. Modifications to the return value
     * stack will not be reflected on the Task's stack. But, modifications on the
     * stack objects will be reflected.
     * @return
     */
    public Vector<Instruction> getStack() {
        return getVector(stack);
    }

    /**
     * Sets a not so shallow reference to the parameter stack.
     * Further modifications on the parameter will not modify the internal stack.
     * But, modifications on the stack values will be modified.
     * @param v
     */
    public void setStack(Vector<Instruction> v) {
        setVector(stack, v);
    }

    private <E> Vector<E> getVector(Vector<E> vector) {
        Iterator<E> it = vector.iterator();

        Vector<E> v = new Vector<E>();
        while (it.hasNext()) {
            v.add(it.next());
        }
        return v;
    }

    private <E> void setVector(Vector<E> oldVector, Vector<E> newVector) {
        oldVector.clear();

        Iterator<E> it = newVector.iterator();
        while (it.hasNext()) {
            oldVector.add(it.next());
        }
    }

    public boolean hasInstruction() {
        return !stack.isEmpty();
    }

    public void pushInstruction(Instruction inst) {
        stack.add(inst);
    }

    @SuppressWarnings("unchecked")
    public Task compute(SkeletonSystemImpl system) throws Exception {
        Instruction inst = stack.pop();

        int oldId = taskId.value();
        Task task = inst.compute(system, this);

        if (oldId != task.taskId.value()) {
            String msg = "Task Error, task id changed while interpreting! " + inst + " (" + oldId + "->" +
                task.taskId + ")";
            logger.error(msg);
            throw new TaskException(msg);
        }

        return task;
    }

    public T getObject() {
        return param;
    }

    public void setObject(T object) {
        this.param = object;
    }

    public synchronized boolean isReady() {
        return family.childrenReady.isEmpty() && family.childrenWaiting.isEmpty();
    }

    public synchronized boolean isFinished() {
        return isReady() && !hasInstruction();
    }

    public boolean isRootTask() {
        return this.taskId.isRootTaskId();
    }

    @Override
    public String toString() {
        return taskId.toString();
    }

    /**
     * @return Returns the exception.
     */
    public Exception getException() {
        return exception;
    }

    /**
     * @param exception The exception to set.
     */
    public void setException(Exception exception) {
        exception.printStackTrace();
        this.exception = exception;
    }

    public boolean hasException() {
        return this.exception != null;
    }

    /**
     * @return true if this task is tainted
     */
    public boolean isTainted() {
        return isTainted;
    }

    /**
     * @param isTainted true sets this task to tainted
     */
    public void setTainted(boolean isTainted) {
        this.isTainted = isTainted;
    }

    public StatsImpl getStats() {
        return stats;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public String stackToString() {
        String res = "--Stack Top-- (Size=" + stack.size() + ") Task=" + taskId + " " +
            param.getClass().getSimpleName() + "@" + System.identityHashCode(param) + "\n";

        for (int i = stack.size() - 1; i >= 0; i--) {
            res = res + "   " + stack.get(i).getClass().getSimpleName() + "\n";
        }
        res.trim();

        return res + "--Stack Bottom --";
    }

    /*
    @SuppressWarnings("unchecked")
        public void setWSpace(WSpaceImpl wspace) throws IllegalArgumentException, IllegalAccessException, IOException{

            Map hash = Stateness.getAllFieldObjects(param, ProxyFile.class);
            Collection<ProxyFile> list = hash.values();
            for(ProxyFile file:list){
                    file.setWSpace(wspace.getWSpaceDir());
                    file.saveRemoteFileInWSpace();
            }
    }
     */
}
