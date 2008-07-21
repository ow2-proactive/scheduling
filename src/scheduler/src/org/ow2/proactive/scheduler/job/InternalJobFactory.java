/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.job;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.ProActiveJob;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.ProActiveTask;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.task.JavaExecutableContainer;
import org.ow2.proactive.scheduler.task.NativeExecutableContainer;
import org.ow2.proactive.scheduler.task.internal.InternalJavaTask;
import org.ow2.proactive.scheduler.task.internal.InternalNativeTask;
import org.ow2.proactive.scheduler.task.internal.InternalProActiveTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


/**
 * This is the factory to build Internal job with a job (user).
 * For the moment it performs a simple copy from userJob to InternalJob
 * and recreate the dependences if needed.
 *
 * @author The ProActive Team
 * @since ProActive 3.9
 */
public class InternalJobFactory implements Serializable {

    /**
     * Create a new internal job with the given job (user).
     *
     * @param job the user job that will be used to create the internal job.
     * @return the created internal job.
     * @throws SchedulerException an exception if the factory cannot create the given job.
     */
    public static InternalJob createJob(Job job) throws SchedulerException {
        InternalJob iJob = null;

        switch (job.getType()) {
            case PARAMETER_SWEEPING:
                throw new SchedulerException("The type of the given job is not yet implemented !");
            case PROACTIVE:
                iJob = createJob((ProActiveJob) job);
                break;
            case TASKSFLOW:
                iJob = createJob((TaskFlowJob) job);
                break;
            default:
                throw new SchedulerException("The type of the given job is unknown !");
        }

        try {
            iJob.setName(job.getName());
            iJob.setPriority(job.getPriority());
            iJob.setCancelOnError(job.isCancelOnError());
            iJob.setDescription(job.getDescription());
            iJob.setLogFile(job.getLogFile());
            iJob.setProjectName(job.getProjectName());
            for (Entry<String, String> e : job.getGenericInformations().entrySet()) {
                iJob.addGenericInformation(e.getKey(), e.getValue());
            }
            iJob.setEnv(job.getEnv());
            return iJob;
        } catch (Exception e) {
            throw new SchedulerException("Error while creating the internalJob !", e);
        }
    }

    /**
     * Create an internal ProActive job with the given ProActive job (user)
     *
     * @param job
     *            the user job that will be used to create the internal job.
     * @return the created internal job.
     * @throws SchedulerException
     *             an exception if the factory cannot create the given job.
     */
    private static InternalJob createJob(ProActiveJob userJob) throws SchedulerException {
        InternalProActiveJob job;
        ProActiveTask userTask = userJob.getTask();

        if (userTask == null) {
            throw new SchedulerException("You must specify a ProActive task !");
        }

        if (userTask.getExecutableClassName() != null) {
            job = new InternalProActiveJob(userTask.getNumberOfNodesNeeded(), userTask
                    .getExecutableClassName(), userTask.getArguments());
        } else {
            throw new SchedulerException(
                "You must specify your own executable ProActive task to be launched (in the application task) !");
        }

        InternalProActiveTask iajt = job.getTask();
        userTask.setPreciousResult(true);
        //set common fields
        setCommonProperties(userTask, iajt);

        return job;
    }

    /**
     * Check whether or not every tasks of the given tasks flow can be reached.
     * 
     * @return true if every tasks can be accessed, false if not.
     */
    private static boolean isConsistency(TaskFlowJob userJob) {
        HashSet<Task> tasks = new HashSet<Task>();
        HashSet<Task> reached = new HashSet<Task>();
        for (Task t : userJob.getTasks()) {
            if (t.getDependencesList() == null) {
                reached.add(t);
            } else {
                tasks.add(t);
            }
        }
        boolean change;
        do {
            change = false;
            Iterator<Task> it = tasks.iterator();
            while (it.hasNext()) {
                Task t = it.next();
                if (reached.containsAll(t.getDependencesList())) {
                    it.remove();
                    reached.add(t);
                    change = true;
                }
            }
        } while (change);
        return reached.size() == userJob.getTasks().size();
    }

    /**
     * Create an internalTaskFlow job with the given task flow job (user)
     *
     * @param job the user job that will be used to create the internal job.
     * @return the created internal job.
     * @throws SchedulerException an exception if the factory cannot create the given job. 
     */
    private static InternalJob createJob(TaskFlowJob userJob) throws SchedulerException {
        if (userJob.getTasks().size() == 0) {
            throw new SchedulerException("This job must contains tasks !");
        }

        //check tasks flow
        if (!isConsistency(userJob)) {
            throw new SchedulerException("One or more tasks in this job cannot be reached !");
        }

        InternalJob job = new InternalTaskFlowJob();
        HashMap<Task, InternalTask> tasksList = new HashMap<Task, InternalTask>();
        boolean hasPreciousResult = false;

        for (Task t : userJob.getTasks()) {
            tasksList.put(t, createTask(t));

            if (hasPreciousResult == false) {
                hasPreciousResult = t.isPreciousResult();
            }
        }

        //reinit taskId count
        TaskId.initialize();

        for (Entry<Task, InternalTask> entry : tasksList.entrySet()) {
            if (entry.getKey().getDependencesList() != null) {
                for (Task t : entry.getKey().getDependencesList()) {
                    entry.getValue().addDependence(tasksList.get(t));
                }
            }

            job.addTask(entry.getValue());
        }

        return job;
    }

    private static InternalTask createTask(Task task) throws SchedulerException {
        if (task instanceof NativeTask) {
            return createTask((NativeTask) task);
        } else if (task instanceof JavaTask) {
            return createTask((JavaTask) task);
        } else {
            throw new SchedulerException("The task you intend to add is unknown !");
        }
    }

    /**
     * Create an internal java Task with the given java task (user)
     *
     * @param task the user java task that will be used to create the internal java task.
     * @return the created internal task.
     * @throws SchedulerException an exception if the factory cannot create the given task.
     */
    private static InternalTask createTask(JavaTask task) throws SchedulerException {
        InternalJavaTask javaTask;

        if (task.getExecutableClassName() != null) {
            javaTask = new InternalJavaTask(new JavaExecutableContainer(task.getExecutableClassName(), task
                    .getArguments()));
        } else {
            throw new SchedulerException(
                "You must specify your own executable task to be launched in every task !");
        }

        javaTask.setFork(task.isFork());
        javaTask.setForkEnvironment(task.getForkEnvironment());
        //set common fields
        setCommonProperties(task, javaTask);

        return javaTask;
    }

    /**
     * Create an internal native Task with the given native task (user)
     *
     * @param task the user native task that will be used to create the internal native task.
     * @return the created internal task.
     * @throws SchedulerException an exception if the factory cannot create the given task.
     */
    private static InternalTask createTask(NativeTask task) throws SchedulerException {
        if (((task.getCommandLine() == null) || (task.getCommandLine() == "")) &&
            (task.getGenerationScript() == null)) {
            throw new SchedulerException("The command line is null or empty and not generated !!");
        }
        InternalNativeTask nativeTask = new InternalNativeTask(new NativeExecutableContainer(task
                .getCommandLine(), task.getGenerationScript()));
        setCommonProperties(task, nativeTask);

        return nativeTask;
    }

    /**
     * Set some properties between the user task and internal task.
     *
     * @param task the user task.
     * @param taskToSet the internal task to set.
     */
    private static void setCommonProperties(Task task, InternalTask taskToSet) {
        taskToSet.setDescription(task.getDescription());
        taskToSet.setPreciousResult(task.isPreciousResult());
        taskToSet.setName(task.getName());
        taskToSet.setPostScript(task.getPostScript());
        taskToSet.setPreScript(task.getPreScript());
        taskToSet.setRerunnable(task.getRerunnable());
        taskToSet.setSelectionScript(task.getSelectionScript());
        taskToSet.setResultPreview(task.getResultPreview());
        taskToSet.setWallTime(task.getWallTime());
        taskToSet.setRestartOnError(task.getRestartOnError());
        for (Entry<String, String> e : task.getGenericInformations().entrySet()) {
            taskToSet.addGenericInformation(e.getKey(), e.getValue());
        }
    }
}
