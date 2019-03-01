/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.descriptor;

import java.util.Vector;

import javax.xml.bind.annotation.XmlTransient;

import org.ow2.proactive.scheduler.common.TaskDescriptor;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


/**
 * This class represents an eligible task for the policy.
 * It is a sort of tag class that will avoid user from giving non-eligible task to the scheduler.
 * In fact policy will handle TaskDescriptor and EligibleTaskDescriptor but
 * will only be allowed to send EligibleTaskDescriptor to the scheduler
 * @see TaskDescriptor
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class EligibleTaskDescriptorImpl implements EligibleTaskDescriptor {

    /** Task Id */
    private TaskId taskId;

    /** Number of nodes needed of the task */
    private int numberOfNodesNeeded;

    /** Internal representation of the task */
    private transient InternalTask internalTask;

    /** Number of parents remaining (initial value must be 0) */
    private int parentsCount = 0;

    /** Number of children remaining (initial value must be 0) */
    private int childrenCount = 0;

    /** Number of attempt to start the task (number of rm.getAtMostNode() called for this task) */
    private int attempt = 0;

    /** list of parent tasks for this task (null if jobType!=TASK_FLOW) */
    @XmlTransient
    private transient Vector<TaskDescriptor> parents;

    /** list of ordered children tasks for this task (null if jobType!=TASK_FLOW) */
    @XmlTransient
    private transient Vector<TaskDescriptor> children;

    /**
     * Get a new eligible task descriptor using a taskDescriptor.
     * Same constructor as TaskDescriptor
     *
     * @param td the taskDescriptor to shrink.
     */
    public EligibleTaskDescriptorImpl(InternalTask td) {
        this.internalTask = td;
        this.taskId = getInternal().getId();
        this.numberOfNodesNeeded = getInternal().getNumberOfNodesNeeded();
    }

    /**
     * To get the children
     *
     * @return the children
     */
    @XmlTransient
    public Vector<TaskDescriptor> getChildren() {
        if (children == null) {
            return new Vector<>();
        }

        return children;
    }

    /**
     * {@inheritDoc}
     */
    public TaskId getTaskId() {
        return taskId;
    }

    /**
     * To get the parents
     *
     * @return the parents
     */
    @XmlTransient
    public Vector<TaskDescriptor> getParents() {
        if (parents == null) {
            return new Vector<>();
        }

        return parents;
    }

    /**
     * {@inheritDoc}
     */
    public InternalTask getInternal() {
        return internalTask;
    }

    /**
     * To get the jobId
     *
     * @return the jobId
     */
    public JobId getJobId() {
        return getTaskId().getJobId();
    }

    /**
     * Get the number of nodes needed for this task (by default: 1).
     *
     * @return the number of Nodes Needed
     */
    public int getNumberOfNodesNeeded() {
        return numberOfNodesNeeded;
    }

    /**
     * Return the number of parents remaining
     *
     * @return the number of parents remaining.
     */
    public int getCount() {
        return parentsCount;
    }

    /**
     * Return the number of children remaining.
     *
     * @return the number of children remaining.
     */
    public int getChildrenCount() {
        return childrenCount;
    }

    /**
     * Set the number of parents remaining.
     *
     * @param count the number of parents remaining.
     */
    public void setCount(int count) {
        this.parentsCount = count;
    }

    /**
     * Set the number of children remaining.
     *
     * @param count the number of children remaining.
     */
    public void setChildrenCount(int count) {
        this.childrenCount = count;
    }

    /**
     * Add an attempt to this task
     */
    public void addAttempt() {
        attempt++;
    }

    /**
     * {@inheritDoc}
     */
    public int getAttempt() {
        return attempt;
    }

    /**
     * Add a parent to the list of parents dependence.
     *
     * @param task the parent task to add.
     */
    public void addParent(TaskDescriptor task) {
        if (parents == null) {
            parents = new Vector<>();
        }

        parents.add(task);
        parentsCount++;
    }

    /**
     * Add a child to the list of children dependence.
     *
     * @param task the child task to add.
     */
    public void addChild(TaskDescriptor task) {
        if (children == null) {
            children = new Vector<>();
        }

        children.add(task);
        childrenCount++;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TaskDescriptor) {
            return ((TaskDescriptor) obj).getTaskId().equals(getTaskId());
        }

        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return getTaskId().hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TaskDescriptor(" + getTaskId() + ")";
    }

    /**
     * Removes all children dependences
     */
    public void clearChildren() {
        if (children == null) {
            children = new Vector<>();
        }

        children.clear();
        childrenCount = 0;
    }

}
