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
import java.util.Map.Entry;

import org.objectweb.proactive.extensions.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extensions.scheduler.common.job.Job;
import org.objectweb.proactive.extensions.scheduler.common.job.ProActiveJob;
import org.objectweb.proactive.extensions.scheduler.common.job.TaskFlowJob;
import org.objectweb.proactive.extensions.scheduler.common.task.JavaTask;
import org.objectweb.proactive.extensions.scheduler.common.task.NativeTask;
import org.objectweb.proactive.extensions.scheduler.common.task.ProActiveTask;
import org.objectweb.proactive.extensions.scheduler.common.task.Task;
import org.objectweb.proactive.extensions.scheduler.task.internal.InternalAbstractJavaTask;
import org.objectweb.proactive.extensions.scheduler.task.internal.InternalJavaTask;
import org.objectweb.proactive.extensions.scheduler.task.internal.InternalNativeTask;
import org.objectweb.proactive.extensions.scheduler.task.internal.InternalTask;


/**
 * This is the factory to build Internal job with a job (user).
 * For the moment it performs a simple copy from userJob to InternalJob
 * and recreate the dependences if needed.
 *
 * @author jlscheef - ProActiveTeam
 * @version 3.9, Sept 14, 2007
 * @since ProActive 3.9
 */
public class InternalJobFactory implements Serializable {

    /** Serial Version UID */
    private static final long serialVersionUID = -7017115222916960404L;

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
            throw new SchedulerException(
                "The type of the given job is not yet implemented !");
        case PROACTIVE:
            iJob = createJob((ProActiveJob) job);
            break;
        case TASKSFLOW:
            iJob = createJob((TaskFlowJob) job);
            break;
        default:
            throw new SchedulerException(
                "The type of the given job is unknown !");
        }

        try {
            iJob.setName(job.getName());
            iJob.setPriority(job.getPriority());
            iJob.setCancelOnError(job.isCancelOnError());
            iJob.setDescription(job.getDescription());
            iJob.setLogFile(job.getLogFile());

            return iJob;
        } catch (Exception e) {
            throw new SchedulerException("Error while creating the internalJob !",
                e);
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
    private static InternalJob createJob(ProActiveJob userJob)
        throws SchedulerException {
        InternalProActiveJob job;
        ProActiveTask userTask = userJob.getTask();

        if (userTask == null) {
            throw new SchedulerException("You must specify a ProActive task !");
        }

        if (userTask.getTaskClass() != null) {
            job = new InternalProActiveJob(userTask.getNumberOfNodesNeeded(),
                    userTask.getTaskClass());
        } else if (userTask.getTaskInstance() != null) {
            job = new InternalProActiveJob(userTask.getNumberOfNodesNeeded(),
                    userTask.getTaskInstance());
        } else {
            throw new SchedulerException(
                "You must specify your own executable ProActive task to be launched (in the application task) !");
        }

        InternalAbstractJavaTask iajt = job.getTask();
        userTask.setPreciousResult(true);
        iajt.setArgs(userTask.getArguments());
        setProperties(userTask, iajt);

        return job;
    }

    /**
     * Create an internalTaskFlow job with the given task flow job (user)
     *
     * @param job the user job that will be used to create the internal job.
     * @return the created internal job.
     * @throws SchedulerException an exception if the factory cannot create the given job.
     */
    private static InternalJob createJob(TaskFlowJob userJob)
        throws SchedulerException {
        if (userJob.getTasks().size() == 0) {
            throw new SchedulerException("This job must contains tasks !");
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

        if (!hasPreciousResult) {
            throw new SchedulerException(
                "You must specify at least on precious result in your job !");
        }

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
            throw new SchedulerException(
                "The task you intend to add is unknown !");
        }
    }

    /**
     * Create an internal java Task with the given java task (user)
     *
     * @param task the user java task that will be used to create the internal java task.
     * @return the created internal task.
     * @throws SchedulerException an exception if the factory cannot create the given task.
     */
    private static InternalTask createTask(JavaTask task)
        throws SchedulerException {
        InternalJavaTask javaTask;

        if (task.getTaskClass() != null) {
            javaTask = new InternalJavaTask(task.getTaskClass());
        } else if (task.getTaskInstance() != null) {
            javaTask = new InternalJavaTask(task.getTaskInstance());
        } else {
            throw new SchedulerException(
                "You must specify your own executable task to be launched in every task !");
        }

        javaTask.setArgs(task.getArguments());
        setProperties(task, javaTask);

        return javaTask;
    }

    /**
     * Create an internal native Task with the given native task (user)
     *
     * @param task the user native task that will be used to create the internal native task.
     * @return the created internal task.
     * @throws SchedulerException an exception if the factory cannot create the given task.
     */
    private static InternalTask createTask(NativeTask task)
        throws SchedulerException {
        if (((task.getCommandLine() == null) || (task.getCommandLine() == "")) &&
                (task.getGenerationScript() == null)) {
            throw new SchedulerException(
                "The command line is null or empty and not generated !!");
        }

        InternalNativeTask nativeTask = new InternalNativeTask(task.getCommandLine(),
                task.getGenerationScript());
        setProperties(task, nativeTask);

        return nativeTask;
    }

    /**
     * Set some properties between the user task and internal task.
     *
     * @param task the user task.
     * @param taskToSet the internal task to set.
     */
    private static void setProperties(Task task, InternalTask taskToSet) {
        taskToSet.setDescription(task.getDescription());
        taskToSet.setPreciousResult(task.isPreciousResult());
        taskToSet.setName(task.getName());
        taskToSet.setPostScript(task.getPostScript());
        taskToSet.setPreScript(task.getPreScript());
        taskToSet.setRerunnable(task.getRerunnable());
        //taskToSet.setRunTimeLimit(task.getRunTimeLimit());
        taskToSet.setSelectionScript(task.getSelectionScript());
        taskToSet.setResultPreview(task.getResultPreview());
    }
}
