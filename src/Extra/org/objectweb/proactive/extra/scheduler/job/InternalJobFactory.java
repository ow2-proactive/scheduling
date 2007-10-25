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
import java.util.HashMap;
import java.util.Map.Entry;

import org.objectweb.proactive.extra.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extra.scheduler.common.job.ApplicationJob;
import org.objectweb.proactive.extra.scheduler.common.job.Job;
import org.objectweb.proactive.extra.scheduler.common.job.TaskFlowJob;
import org.objectweb.proactive.extra.scheduler.common.task.ApplicationTask;
import org.objectweb.proactive.extra.scheduler.common.task.JavaTask;
import org.objectweb.proactive.extra.scheduler.common.task.NativeTask;
import org.objectweb.proactive.extra.scheduler.common.task.Task;
import org.objectweb.proactive.extra.scheduler.task.internal.InternalAbstractJavaTask;
import org.objectweb.proactive.extra.scheduler.task.internal.InternalJavaTask;
import org.objectweb.proactive.extra.scheduler.task.internal.InternalNativeTask;
import org.objectweb.proactive.extra.scheduler.task.internal.InternalTask;


/**
 * This is the factory to build Internal job with a job (user).
 * For the moment it performs a simple copy from userJob to InternalJob
 * and recreate the dependences if needed.
 *
 * @author ProActive Team
 * @version 1.0, Sept 14, 2007
 * @since ProActive 3.2
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
        switch (job.getType()) {
        case PARAMETER_SWEEPING:
            throw new SchedulerException(
                "The type of the given job is not yet implemented !");
        case APPLI:
            return createJob((ApplicationJob) job);
        case TASKSFLOW:
            return createJob((TaskFlowJob) job);
        }
        throw new SchedulerException("The type of the given job is unknown !");
    }

    /**
     * Create an internalApplication job with the given Application job (user)
     *
     * @param job the user job that will be used to create the internal job.
     * @return the created internal job.
     * @throws SchedulerException an exception if the factory cannot create the given job.
     */
    private static InternalJob createJob(ApplicationJob userJob)
        throws SchedulerException {
        InternalApplicationJob job;
        ApplicationTask userTask = userJob.getTask();
        if (userTask == null) {
            throw new SchedulerException(
                "You must specify an application task !");
        }

        // TODO cdelbe,jlscheef,jfradj : extremely UGLY "design" pattern...
        if (userTask.getTaskClass() != null) {
            job = new InternalApplicationJob(userJob.getName(),
                    userJob.getPriority(), userJob.getRuntimeLimit(),
                    userJob.isCancelOnError(), userJob.getDescription(),
                    userTask.getNumberOfNodesNeeded(), userTask.getTaskClass());
            job.setLogFile(userJob.getLogFile());
        } else if (userTask.getTaskInstance() != null) {
            job = new InternalApplicationJob(userJob.getName(),
                    userJob.getPriority(), userJob.getRuntimeLimit(),
                    userJob.isCancelOnError(), userJob.getDescription(),
                    userTask.getNumberOfNodesNeeded(),
                    userTask.getTaskInstance());
            job.setLogFile(userJob.getLogFile());
        } else {
            throw new SchedulerException(
                "You must specify your own executable application task to be launched (in the application task) !");
        }
        InternalAbstractJavaTask iajt = job.getTask();
        userTask.setFinalTask(true);
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

        // TODO cdelbe,jlscheef,jfradj : extremely UGLY "design" pattern...
        InternalJob job = new InternalTaskFlowJob(userJob.getName(),
                userJob.getPriority(), userJob.getRuntimeLimit(),
                userJob.isCancelOnError(), userJob.getDescription());
        job.setLogFile(userJob.getLogFile());
        HashMap<Task, InternalTask> tasksList = new HashMap<Task, InternalTask>();
        boolean hasFinalTask = false;
        for (Task t : userJob.getTasks()) {
            tasksList.put(t, createTask(t));
            if (hasFinalTask == false) {
                hasFinalTask = t.isFinalTask();
            }
        }
        if (!hasFinalTask) {
            throw new SchedulerException(
                "You must specify at least one final task in your job !");
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
        //TODO jlscheef : change this instance of by better solutions, no time left for the moment
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
        if ((task.getCommandLine() == null) || (task.getCommandLine() == "")) {
            throw new SchedulerException("The command line is null or empty !!");
        }
        InternalNativeTask nativeTask = new InternalNativeTask(task.getCommandLine());
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
        taskToSet.setFinalTask(task.isFinalTask());
        taskToSet.setName(task.getName());
        taskToSet.setPostTask(task.getPostTask());
        taskToSet.setPreTask(task.getPreTask());
        taskToSet.setRerunnable(task.getRerunnable());
        taskToSet.setRunTimeLimit(task.getRunTimeLimit());
        taskToSet.setVerifyingScript(task.getVerifyingScript());
        taskToSet.setResultDescriptor(task.getResultDescriptor());
    }
}
