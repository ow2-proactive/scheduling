/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.job;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.job.JobDescriptor;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.common.task.TaskDescriptor;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.task.EligibleTaskDescriptorImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * This class represents a job for the policy.
 * The internal scheduler job is not sent to the policy.
 * Only a restricted number of properties on each jobs is sent to the policy.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class JobDescriptorImpl implements JobDescriptor {
    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.CORE);

    /** Job id */
    private JobId id;

    /** Job priority */
    private JobPriority priority;

    /** Job type */
    private JobType type;

    /** Total number of tasks. */
    private int numberOfTasks;

    /** Project name for this job */
    protected String projectName = "";

    /** Job user informations */
    private Map<String, String> genericInformations;

    /** List that knows which task has children and which have not */
    private Set<TaskId> hasChildren = new HashSet<TaskId>();

    /** Job tasks to be able to be schedule */
    private Map<TaskId, EligibleTaskDescriptor> eligibleTasks = new HashMap<TaskId, EligibleTaskDescriptor>();

    /** Job running tasks */
    private Map<TaskId, TaskDescriptor> runningTasks = new HashMap<TaskId, TaskDescriptor>();

    /** Job paused tasks */
    private Map<TaskId, TaskDescriptor> pausedTasks = new HashMap<TaskId, TaskDescriptor>();

    /**
     * Create a new instance of job descriptor using an internal job.
     * Just make a mapping between some fields of the two type of job in order to
     * give it to the policy.
     * It ensures that the policy won't have bad activities on the real internal job.
     *
     * @param job the internal job to be lighted.
     */
    public JobDescriptorImpl(InternalJob job) {
        logger_dev.debug("job = " + job.getId());
        id = job.getId();
        priority = job.getPriority();
        type = job.getType();
        numberOfTasks = job.getTasks().size();
        genericInformations = job.getGenericInformations();
        projectName = job.getProjectName();

        if (type == JobType.TASKSFLOW) {
            //build dependence tree
            makeTree(job);
        } else {
            //every tasks are eligible
            for (InternalTask td : job.getITasks()) {
                if (td.getStatus() == TaskStatus.SUBMITTED) {
                    eligibleTasks.put(td.getId(), new EligibleTaskDescriptorImpl(td));
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
        logger_dev.debug("job = " + job.getId());
        Map<InternalTask, TaskDescriptor> mem = new HashMap<InternalTask, TaskDescriptor>();

        //create task descriptor list
        for (InternalTask td : job.getITasks()) {
            //if this task is a first task, put it in eligible tasks list
            EligibleTaskDescriptor lt = new EligibleTaskDescriptorImpl(td);

            if (!td.hasDependences()) {
                eligibleTasks.put(td.getId(), lt);
            }

            mem.put(td, lt);
        }

        //now for each taskDescriptor, set the parents and children list
        for (InternalTask td : job.getITasks()) {
            if (td.getDependences() != null) {
                TaskDescriptor taskDescriptor = mem.get(td);

                for (InternalTask depends : td.getIDependences()) {
                    ((EligibleTaskDescriptorImpl) taskDescriptor).addParent(mem.get(depends));
                }

                for (TaskDescriptor lt : taskDescriptor.getParents()) {
                    ((EligibleTaskDescriptorImpl) lt).addChild(taskDescriptor);
                    hasChildren.add(lt.getId());
                }
            }
        }
    }

    /**
     * Return true if the task represented by the given taskId has children, false if not.
     *
     * @param taskId the id representing the real task.
     * @return true if the task represented by the given taskId has children, false if not.
     */
    public boolean hasChildren(TaskId taskId) {
        return hasChildren.contains(taskId);
    }

    /**
     * Delete this task from eligible task view and add it to running view.
     * Visibility is package because user cannot use this method.
     *
     * @param taskId the task that has just been started.
     */
    public void start(TaskId taskId) {
        runningTasks.put(taskId, eligibleTasks.remove(taskId));
    }

    /**
     * Delete this task from running task view and add it to eligible view.
     * Visibility is package because user cannot use this method.
     *
     * @param taskId the task that has just been started.
     */
    public void reStart(TaskId taskId) {
        eligibleTasks.put(taskId, (EligibleTaskDescriptor) runningTasks.remove(taskId));
    }

    /**
     * Update the eligible list of task and dependencies if necessary.
     * This function considered that the taskId is in eligible task list.
     * Visibility is package because user cannot use this method.
     *
     * @param taskId the task to remove from running task.
     */
    public void terminate(TaskId taskId) {
        logger_dev.debug("task = " + taskId);
        if (type == JobType.TASKSFLOW) {
            TaskDescriptor lt = runningTasks.get(taskId);

            for (TaskDescriptor task : lt.getChildren()) {
                ((EligibleTaskDescriptorImpl) task)
                        .setCount(((EligibleTaskDescriptorImpl) task).getCount() - 1);

                if (((EligibleTaskDescriptorImpl) task).getCount() == 0) {
                    eligibleTasks.put(task.getId(), (EligibleTaskDescriptor) task);
                }
            }

            for (TaskDescriptor task : lt.getParents()) {
                ((EligibleTaskDescriptorImpl) task).setChildrenCount(task.getChildrenCount() - 1);
            }
        }

        runningTasks.remove(taskId);
    }

    /**
     * Failed this job descriptor by removing every tasks from eligible and running list.
     * This function considered that the taskIds are in eligible tasks list.
     * Visibility is package because user cannot use this method.
     */
    public void failed() {
        eligibleTasks.clear();
        runningTasks.clear();
    }

    /**
     * Update the list of eligible tasks according to the status of each task.
     * This method is called only if user pause a job.
     *
     * @param taskStatus the taskId with their current status.
     */
    public void update(Map<TaskId, TaskStatus> taskStatus) {
        logger_dev.debug(" ");
        for (Entry<TaskId, TaskStatus> tid : taskStatus.entrySet()) {
            if (tid.getValue() == TaskStatus.PAUSED) {
                TaskDescriptor lt = eligibleTasks.get(tid.getKey());

                if (lt != null) {
                    pausedTasks.put(tid.getKey(), eligibleTasks.remove(tid.getKey()));
                }
            } else if ((tid.getValue() == TaskStatus.PENDING) || (tid.getValue() == TaskStatus.SUBMITTED)) {
                EligibleTaskDescriptor lt = (EligibleTaskDescriptor) pausedTasks.get(tid.getKey());

                if (lt != null) {
                    eligibleTasks.put(tid.getKey(), lt);
                    pausedTasks.remove(tid.getKey());
                }
            }
        }
    }

    /**
     * Get a task descriptor that is in the running task.
     *
     * @param id the id of the task descriptor to retrieve.
     * @return the task descriptor associated to this id, or null if not running.
     */
    public TaskDescriptor GetRunningTaskDescriptor(TaskId id) {
        return runningTasks.get(id);
    }

    /**
     * Set the priority of this job descriptor.
     *
     * @param priority the new priority.
     */
    public void setPriority(JobPriority priority) {
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
     * To get the tasks.
     *
     * @return the tasks.
     */
    public Collection<EligibleTaskDescriptor> getEligibleTasks() {
        return new Vector<EligibleTaskDescriptor>(eligibleTasks.values());
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
     * Returns the number Of Tasks.
     *
     * @return the number Of Tasks.
     */
    public int getNumberOfTasks() {
        return numberOfTasks;
    }

    /**
     * Return the generic informations has a Map.
     *
     * @return the generic informations has a Map.
     */
    public Map<String, String> getGenericInformations() {
        return genericInformations;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * @param job the job to be compared.
     * @return  a negative integer, zero, or a positive integer as this object
     *		is less than, equal to, or greater than the specified object.
     */
    public int compareTo(JobDescriptor job) {
        return job.getPriority().compareTo(priority);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "JobDescriptor(" + getId() + ")";
    }

    /**
     * Returns the projectName.
     * @return the projectName.
     */
    public String getProjectName() {
        return projectName;
    }
}
