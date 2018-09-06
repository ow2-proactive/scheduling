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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.util.LogFormatter;


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
