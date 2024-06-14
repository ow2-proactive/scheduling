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
package org.ow2.proactive.scheduler.common.job;

import java.util.*;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.common.util.LogFormatter;

import com.google.common.collect.Sets;


/**
 * Use this class to create your job if you want to define a task flow job.<br>
 * A task flow job or data flow job, is a job that can contain
 * one or more task(s) with the dependencies you want.<br>
 * To make this type of job, just use the default no arg constructor,
 * and set the properties you want to set.<br>
 * Then add tasks with the given method {@link #addTask(Task)} in order to fill the job with your own tasks.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public class TaskFlowJob extends Job {

    /** Task count for unset task name */
    private int taskCountForUnSetTaskName = 1;

    /** List of task for the task flow job */
    private Map<String, Task> tasks = new LinkedHashMap<String, Task>();

    /** ProActive Empty Constructor */
    public TaskFlowJob() {
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.Job#getType()
     */
    @Override
    public JobType getType() {
        return JobType.TASKSFLOW;
    }

    /**
     * Add a task to this task flow job.<br>
     * The task name must not be null as it is not by default.<br>
     * The task name must also be different for each task as it is used to identify each task result.<br>
     * <br>
     * If not set, the task name will be a generated one : 'task_X' (where X is the Xth added task number)
     *
     * @param task the task to add.
     * @throws UserException if a problem occurred while the task is being added.
     */
    public void addTask(Task task) throws UserException {
        if (task.getName() == null) {
            throw new UserException("The name of the task must not be null !");
        }
        if (task.getName().equals(SchedulerConstants.TASK_DEFAULT_NAME)) {
            task.setName(SchedulerConstants.TASK_NAME_IFNOTSET + taskCountForUnSetTaskName);
            taskCountForUnSetTaskName++;
        }
        if (tasks.containsKey(task.getName())) {
            throw new UserException("The name of the task is already used : " + task.getName());
        }
        tasks.put(task.getName(), task);
    }

    /**
     * Add a list of tasks to this task flow job.
     * The task names must not be null as it is not by default.<br>
     * The task names must also be different for each task as it is used to identify each task result.<br>
     * <br>
     * If not set, the task names will be generated : 'task_X' (where X is the Xth added task number)
     *
     * @param tasks the list of tasks to add.
     * @throws UserException if a problem occurred while the task is being added.
     */
    public void addTasks(List<Task> tasks) throws UserException {
        for (Task task : tasks) {
            addTask(task);
        }
    }

    /**
     * To get the list of tasks.
     *
     * @return the list of tasks.
     */
    public ArrayList<Task> getTasks() {
        return new ArrayList<Task>(tasks.values());
    }

    /**
     * Find terminal tasks for this workflow
     *
     * Terminal tasks are tasks without children and not source of a If or Else branch
     * @return a set of terminal tasks
     */
    public Set<Task> findTerminalTasks() {

        Set<Task> ifParents = new HashSet<>();
        Set<String> ifChildrenTaskNames = new HashSet<>();
        Set<String> elseChildrenTaskNames = new HashSet<>();

        Set<Task> terminalTasks = new HashSet<>();

        // First iteration, add all tasks as terminal and store IF flow information
        for (Task task : tasks.values()) {
            FlowScript flowScript = task.getFlowScript();
            if (flowScript != null && flowScript.getActionType().equals(FlowActionType.IF.toString())) {
                ifParents.add(task);
                if (flowScript.getActionTarget() != null) {
                    ifChildrenTaskNames.add(flowScript.getActionTarget());
                }
                if (flowScript.getActionTargetElse() != null) {
                    elseChildrenTaskNames.add(flowScript.getActionTargetElse());
                }
            }
            terminalTasks.add(task);
        }

        for (Task task : tasks.values()) {
            // for each task, if it has dependencies, dependencies cannot be terminal
            if (task.getDependencesList() != null) {
                terminalTasks.removeAll(task.getDependencesList());
            }
            // If the task is a root if, it cannot be terminal
            if (ifParents.contains(task)) {
                terminalTasks.remove(task);
            }
            // if the task is a target of a if branch, all the branch cannot be terminal
            if (ifChildrenTaskNames.contains(task.getName())) {
                terminalTasks.removeAll(findSubTree(task));
            }
            // if the task is a target of a else branch, all the branch cannot be terminal
            if (elseChildrenTaskNames.contains(task.getName())) {
                terminalTasks.removeAll(findSubTree(task));
            }

        }
        // all remaining tasks are terminal
        return terminalTasks;
    }

    /**
     * Find root tasks for this workflow
     *
     * Root tasks are tasks without parent and not targets of a If, Else or Continuation branch
     * @return a set of terminal tasks
     */
    public Set<Task> findRootTasks() {

        Set<Task> rootTasks = new LinkedHashSet<>(tasks.values());

        for (Task task : tasks.values()) {

            FlowScript flowScript = task.getFlowScript();
            if (flowScript != null && flowScript.getActionType().equals(FlowActionType.IF.toString())) {
                if (flowScript.getActionTarget() != null) {
                    // remove from root all tasks that are target of this task's IF, ELSE or CONTINUATION branches
                    rootTasks.removeIf(t -> t.getName().equals(flowScript.getActionTarget()) ||
                                            t.getName().equals(flowScript.getActionTargetElse()) ||
                                            t.getName().equals(flowScript.getActionContinuation()));
                }
            }
            if (task.getDependencesList() != null && !task.getDependencesList().isEmpty()) {
                // remove from root the task if it has dependencies
                rootTasks.remove(task);
            }
        }
        return rootTasks;
    }

    /**
     * Find all tasks reachable by the given task
     */
    private Set<Task> findSubTree(Task parentTask) {
        Set<Task> childrenSet = new HashSet<>();
        addTaskToChildrenSet(childrenSet, parentTask);
        int lastChildrenSetSize;
        do {
            lastChildrenSetSize = childrenSet.size();
            for (Task task : tasks.values()) {
                if (task.getDependencesList() != null) {
                    Set<Task> dependencySet = new HashSet<>(task.getDependencesList());
                    if (Sets.intersection(dependencySet, childrenSet).size() > 0) {
                        addTaskToChildrenSet(childrenSet, task);
                    }
                }
            }
        } while (childrenSet.size() > lastChildrenSetSize);
        return childrenSet;
    }

    private void addTaskToChildrenSet(Set<Task> childrenSet, Task task) {
        childrenSet.add(task);
        if (task.getFlowScript() != null && task.getFlowScript().getActionType().equals(FlowActionType.IF.toString())) {
            childrenSet.add(getTask(task.getFlowScript().getActionTarget()));
            childrenSet.add(getTask(task.getFlowScript().getActionTargetElse()));
            childrenSet.add(getTask(task.getFlowScript().getActionContinuation()));
        }
    }

    /**
     * Get the task corresponding to the given name.
     *
     * @param name the name of the task to look for. 
     * @return the task corresponding to the given name.
     */
    public Task getTask(String name) {
        return tasks.get(name);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.job.Job#getId()
     */
    @Override
    public JobId getId() {
        // Not yet assigned
        return null;
    }

    @Override
    public String display() {
        String nl = System.lineSeparator();
        return super.display() + nl + LogFormatter.addIndent(displayAllTasks());
    }

    private String displayAllTasks() {
        String nl = System.lineSeparator();
        StringBuilder answer = new StringBuilder("Tasks = {");
        answer.append(nl);
        for (String tid : tasks.keySet()) {
            answer.append(LogFormatter.addIndent(tasks.get(tid).display())).append(nl).append(nl);
        }
        answer.append("}");
        return answer.toString();
    }
}
