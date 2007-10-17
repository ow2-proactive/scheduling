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
package org.objectweb.proactive.extra.scheduler.job;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.job.JobPriority;
import org.objectweb.proactive.extra.scheduler.common.job.JobType;
import org.objectweb.proactive.extra.scheduler.common.task.Status;
import org.objectweb.proactive.extra.scheduler.common.task.TaskId;
import org.objectweb.proactive.extra.scheduler.task.EligibleTaskDescriptor;
import org.objectweb.proactive.extra.scheduler.task.internal.InternalTask;


/**
 * This class represents a job for the policy.
 * The internal scheduler job is not sent to the policy.
 * Only a restricted number of properties on each jobs is sent to the policy.
 *
 * @author ProActive Team
 * @version 1.0, Jul 6, 2007
 * @since ProActive 3.2
 */
public class JobDescriptor implements Serializable, Comparable<JobDescriptor> {

    /** Serial version UID */
    private static final long serialVersionUID = -2183608268194326422L;

    /** Job id */
    private JobId id;

    /** Job priority */
    private JobPriority priority;

    /** Job type */
    private JobType type;

    /** Job tasks to be able to be schedule */
    private HashMap<TaskId, EligibleTaskDescriptor> eligibleTasks = new HashMap<TaskId, EligibleTaskDescriptor>();

    /** Job running tasks */
    private HashMap<TaskId, TaskDescriptor> runningTasks = new HashMap<TaskId, TaskDescriptor>();

    /** Job paused tasks */
    private HashMap<TaskId, TaskDescriptor> pausedTasks = new HashMap<TaskId, TaskDescriptor>();

    //TODO penser à mettre ici un champ pour connaitre le nb de tache total du job.

    /**
     * Constructor of light job.
     * Just make a mapping between some fields of the two type of job.
     *
     * @param job the entire job to be lighted.
     */
    public JobDescriptor(InternalJob job) {
        id = job.getId();
        priority = job.getPriority();
        type = job.getType();
        if (type == JobType.TASKSFLOW) {
            //build dependence tree
            makeTree(job);
        } else {
            //every tasks are eligible
            for (InternalTask td : job.getTasks()) {
                if (td.getStatus() == Status.SUBMITTED) {
                    eligibleTasks.put(td.getId(), new EligibleTaskDescriptor(td));
                }
            }
        }
    }

    /**
     * Make a dependences tree of the job's tasks according to the dependence list
     * stored in taskDescriptor.
     * This list represents the ordered TaskDescriptor list of its parent tasks.
     */
    private void makeTree(InternalJob job) {
        HashMap<InternalTask, TaskDescriptor> mem = new HashMap<InternalTask, TaskDescriptor>();

        //create lightTask list
        for (InternalTask td : job.getTasks()) {
            //if this task is a first task, put it in eligible tasks list
            EligibleTaskDescriptor lt = new EligibleTaskDescriptor(td);
            if (!td.hasDependences()) {
                eligibleTasks.put(td.getId(), lt);
            }
            mem.put(td, lt);
        }

        //now for each taskDescriptor, set the parents and children list
        for (InternalTask td : job.getTasks()) {
            if (td.getDependences() != null) {
                TaskDescriptor taskDescriptor = mem.get(td);
                for (InternalTask depends : td.getDependences()) {
                    taskDescriptor.addParent(mem.get(depends));
                }
                taskDescriptor.setCount(td.getDependences().size());
                for (TaskDescriptor lt : taskDescriptor.getParents()) {
                    lt.addChild(taskDescriptor);
                }
            }
        }
    }

    /**
     * Delete this task from eligible task view and add it to running view.
     * Visibility is package because user cannot use this method.
     *
     * @param taskId the task that has just been started.
     */
    void start(TaskId taskId) {
        runningTasks.put(taskId, eligibleTasks.get(taskId));
        eligibleTasks.remove(taskId);
    }

    /**
     * Delete this task from running task view and add it to eligible view.
     * Visibility is package because user cannot use this method.
     *
     * @param taskId the task that has just been started.
     */
    void reStart(TaskId taskId) {
        eligibleTasks.put(taskId,
            (EligibleTaskDescriptor) runningTasks.remove(taskId));
    }

    /**
     * Update the eligible list of task and dependencies if necessary.
     * This function considered that the taskId is in eligible task list.
     * Visibility is package because user cannot use this method.
     *
     * @param taskId the task to remove from running task.
     */
    void terminate(TaskId taskId) {
        if (type == JobType.TASKSFLOW) {
            TaskDescriptor lt = runningTasks.get(taskId);
            for (TaskDescriptor task : lt.getChildren()) {
                task.setCount(task.getCount() - 1);
                if (task.getCount() == 0) {
                    eligibleTasks.put(task.getId(),
                        (EligibleTaskDescriptor) task);
                }
            }
        }
        runningTasks.remove(taskId);
    }

    /**
     * Failed this light job by removing every tasks from eligible and running list.
     * This function considered that the taskIds are in eligible tasks list.
     * Visibility is package because user cannot use this method.
     */
    void failed() {
        eligibleTasks.clear();
        runningTasks.clear();
    }

    /**
     * Update the list of eligible tasks according to the status of each task.
     * This method is called only if user pause a job.
     *
     * @param status the taskId with their current status.
     */
    void update(HashMap<TaskId, Status> status) {
        for (Entry<TaskId, Status> tid : status.entrySet()) {
            if (tid.getValue() == Status.PAUSED) {
                TaskDescriptor lt = eligibleTasks.get(tid.getKey());
                if (lt != null) {
                    pausedTasks.put(tid.getKey(),
                        eligibleTasks.get(tid.getKey()));
                    eligibleTasks.remove(tid.getKey());
                }
            } else if ((tid.getValue() == Status.PENDING) ||
                    (tid.getValue() == Status.SUBMITTED)) {
                EligibleTaskDescriptor lt = (EligibleTaskDescriptor) pausedTasks.get(tid.getKey());
                if (lt != null) {
                    eligibleTasks.put(tid.getKey(), lt);
                    pausedTasks.remove(tid.getKey());
                }
            }
        }
    }

    /**
     * Set the priority of this job descriptor.
     *
     * @param priority the new priority.
     */
    void setPriority(JobPriority priority) {
        this.priority = priority;
    }

    /**
     * To get the id
     *
     * @return the id
     */
    public JobId getId() {
        return id;
    }

    /**
     * To get the priority
     *
     * @return the priority
     */
    public JobPriority getPriority() {
        return priority;
    }

    /**
     * To get the tasks
     *
     * @return the tasks
     */
    public Collection<EligibleTaskDescriptor> getEligibleTasks() {
        return eligibleTasks.values();
    }

    /**
     * To get the type
     *
     * @return the type
     */
    public JobType getType() {
        return type;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(JobDescriptor o) {
        return o.priority.compareTo(priority);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "LightJob(" + getId() + ")";
    }
}
