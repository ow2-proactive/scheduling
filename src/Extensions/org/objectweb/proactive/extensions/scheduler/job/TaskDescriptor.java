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
package org.objectweb.proactive.extensions.scheduler.job;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

import org.objectweb.proactive.extensions.scheduler.common.job.JobId;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskId;
import org.objectweb.proactive.extensions.scheduler.task.internal.InternalTask;


/**
 * This class represents a task for the policy.
 * The internal scheduler tasks are not sent to the policy.
 * Only a restricted number of properties on each tasks is sent to the policy.
 *
 * @author jlscheef - ProActiveTeam
 * @version 3.9, Jul 9, 2007
 * @since ProActive 3.9
 */
public class TaskDescriptor implements Serializable {

    /** Task id */
    private TaskId id;

    /** Number of parents remaining (initial value must be 0) */
    private int parentsCount = 0;

    /** Number of children remaining (initial value must be 0) */
    private int childrenCount = 0;

    /** Number of nodes that are used by this task */
    private int numberOfUsedNodes;

    /** Task user informations */
    private HashMap<String, Object> genericInformations;

    /** list of parent tasks for this task (null if jobType!=TASK_FLOW) */
    private Vector<TaskDescriptor> parents;

    /** list of ordered children tasks for this task (null if jobType!=TASK_FLOW) */
    private Vector<TaskDescriptor> children;

    /**
     * Get a new task descriptor using a taskDescriptor.
     *
     * @param td the taskDescriptor to shrink.
     */
    public TaskDescriptor(InternalTask td) {
        this.id = td.getId();
        this.numberOfUsedNodes = td.getNumberOfNodesNeeded();
        this.genericInformations = td.getGenericInformations();
    }

    /**
     * To get the children
     *
     * @return the children
     */
    public Vector<TaskDescriptor> getChildren() {
        if (children == null) {
            return new Vector<TaskDescriptor>();
        }

        return children;
    }

    /**
     * To get the id
     *
     * @return the id
     */
    public TaskId getId() {
        return id;
    }

    /**
     * To get the parents
     *
     * @return the parents
     */
    public Vector<TaskDescriptor> getParents() {
        if (parents == null) {
            return new Vector<TaskDescriptor>();
        }

        return parents;
    }

    /**
     * To get the jobId
     *
     * @return the jobId
     */
    public JobId getJobId() {
        return this.id.getJobId();
    }

    /**
     * Return the number of parents remaining
     *
     * @return the number of parents remaining.
     */
    int getCount() {
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
     * Returns the number Of nodes used by this task.
     *
     * @return the number Of nodes used by this task.
     */
    public int getNumberOfUsedNodes() {
        return numberOfUsedNodes;
    }

    /**
     * Set the number of parents remaining.
     *
     * @param count the number of parents remaining.
     */
    void setCount(int count) {
        this.parentsCount = count;
    }

    /**
     * Set the number of children remaining.
     *
     * @param count the number of children remaining.
     */
    void setChildrenCount(int count) {
        this.childrenCount = count;
    }

    /**
     * Add a parent to the list of parents dependence.
     *
     * @param task the parent task to add.
     */
    void addParent(TaskDescriptor task) {
        if (parents == null) {
            parents = new Vector<TaskDescriptor>();
        }

        parents.add(task);
        parentsCount++;
    }

    /**
     * Add a child to the list of children dependence.
     *
     * @param task the child task to add.
     */
    void addChild(TaskDescriptor task) {
        if (children == null) {
            children = new Vector<TaskDescriptor>();
        }

        children.add(task);
        childrenCount++;
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.common.job.GenericInformationsProvider#getGenericInformations()
     */
    public HashMap<String, Object> getGenericInformations() {
        return genericInformations;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TaskDescriptor) {
            return ((TaskDescriptor) obj).id.equals(id);
        }

        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TaskDescriptor(" + getId() + ")";
    }
}
