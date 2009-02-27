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
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.ProActiveJob;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.ProActiveTask;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.util.BigString;
import org.ow2.proactive.scheduler.task.JavaExecutableContainer;
import org.ow2.proactive.scheduler.task.NativeExecutableContainer;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.internal.InternalJavaTask;
import org.ow2.proactive.scheduler.task.internal.InternalNativeTask;
import org.ow2.proactive.scheduler.task.internal.InternalProActiveTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * This is the factory to build Internal job with a job (user).
 * For the moment it performs a simple copy from userJob to InternalJob
 * and recreate the dependences if needed.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class InternalJobFactory implements Serializable {

    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.FACTORY);

    /**
     * Create a new internal job with the given job (user).
     *
     * @param job the user job that will be used to create the internal job.
     * @return the created internal job.
     * @throws SchedulerException an exception if the factory cannot create the given job.
     */
    public static InternalJob createJob(Job job) throws SchedulerException {
        InternalJob iJob = null;

        logger_dev.info("Create job '" + job.getName() + "' - " + job.getClass().getName());

        switch (job.getType()) {
            case PARAMETER_SWEEPING:
                logger_dev.error("The type of the given job is not yet implemented !");
                throw new SchedulerException("The type of the given job is not yet implemented !");
            case PROACTIVE:
                iJob = createJob((ProActiveJob) job);
                break;
            case TASKSFLOW:
                iJob = createJob((TaskFlowJob) job);
                break;
            default:
                logger_dev.error("The type of the given job is unknown !");
                throw new SchedulerException("The type of the given job is unknown !");
        }

        try {
            //set the job common properties
            setJobCommonProperties(job, iJob);
            return iJob;
        } catch (Exception e) {
            logger_dev.error(e);
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
                    .getExecutableClassName(), toBigStringMap(userTask.getArguments()));
        } else {
            throw new SchedulerException(
                "You must specify your own executable ProActive task to be launched (in the application task) !");
        }

        InternalProActiveTask iajt = job.getTask();
        userTask.setPreciousResult(true);
        //set task common properties
        setTaskCommonProperties(userJob, userTask, iajt);

        return job;
    }

    /**
     * Check whether or not every tasks of the given tasks flow can be reached.
     * 
     * @return true if every tasks can be accessed, false if not.
     */
    private static boolean isConsistency(TaskFlowJob userJob) {
        logger_dev.info("Check if job '" + userJob.getName() +
            "' is consistency : ie Every task can be accessed");
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
            logger_dev.info("Job '" + userJob.getName() + "' must contain tasks !");
            throw new SchedulerException("This job must contains tasks !");
        }

        //check tasks flow
        if (!isConsistency(userJob)) {
            String msg = "One or more tasks in this job cannot be reached !";
            logger_dev.info(msg);
            throw new SchedulerException(msg);
        }

        InternalJob job = new InternalTaskFlowJob();
        Map<Task, InternalTask> tasksList = new HashMap<Task, InternalTask>();
        boolean hasPreciousResult = false;

        for (Task t : userJob.getTasks()) {
            tasksList.put(t, createTask(userJob, t));

            if (hasPreciousResult == false) {
                hasPreciousResult = t.isPreciousResult();
            }
        }

        //reinit taskId count
        TaskIdImpl.initialize();

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

    private static InternalTask createTask(Job userJob, Task task) throws SchedulerException {
        if (task instanceof NativeTask) {
            return createTask(userJob, (NativeTask) task);
        } else if (task instanceof JavaTask) {
            return createTask(userJob, (JavaTask) task);
        } else {
            String msg = "The task you intend to add is unknown ! (type : " + task.getClass().getName() + ")";
            logger_dev.info(msg);
            throw new SchedulerException(msg);
        }
    }

    /**
     * Create an internal java Task with the given java task (user)
     *
     * @param task the user java task that will be used to create the internal java task.
     * @return the created internal task.
     * @throws SchedulerException an exception if the factory cannot create the given task.
     */
    private static InternalTask createTask(Job userJob, JavaTask task) throws SchedulerException {
        InternalJavaTask javaTask;

        if (task.getExecutableClassName() != null) {
            javaTask = new InternalJavaTask(new JavaExecutableContainer(task.getExecutableClassName(),
                toBigStringMap(task.getArguments())));
        } else {
            String msg = "You must specify your own executable task class to be launched (in every task) !";
            logger_dev.info(msg);
            throw new SchedulerException(msg);
        }

        javaTask.setFork(task.isFork());
        javaTask.setForkEnvironment(task.getForkEnvironment());
        //set task common properties
        setTaskCommonProperties(userJob, task, javaTask);
        return javaTask;
    }

    /**
     * Create an internal native Task with the given native task (user)
     *
     * @param task the user native task that will be used to create the internal native task.
     * @return the created internal task.
     * @throws SchedulerException an exception if the factory cannot create the given task.
     */
    private static InternalTask createTask(Job userJob, NativeTask task) throws SchedulerException {
        if (((task.getCommandLine() == null) || (task.getCommandLine().length == 0)) &&
            (task.getGenerationScript() == null)) {
            String msg = "The command line is null or empty and not generated !";
            logger_dev.info(msg);
            throw new SchedulerException(msg);
        }
        InternalNativeTask nativeTask = new InternalNativeTask(new NativeExecutableContainer(task
                .getCommandLine(), task.getGenerationScript()));
        //set task common properties
        setTaskCommonProperties(userJob, task, nativeTask);
        return nativeTask;
    }

    /**
     * Set some properties between the user Job and internal Job.
     *
     * @param job the user job.
     * @param jobToSet the internal job to set.
     */
    private static void setJobCommonProperties(Job job, InternalJob jobToSet) {
        logger_dev.info("Setting job common properties");
        jobToSet.setName(job.getName());
        jobToSet.setPriority(job.getPriority());
        jobToSet.setCancelJobOnError(job.isCancelJobOnError());
        jobToSet.setRestartTaskOnError(job.getRestartTaskOnError());
        jobToSet.setMaxNumberOfExecution(job.getMaxNumberOfExecution());
        jobToSet.setDescription(job.getDescription());
        jobToSet.setLogFile(job.getLogFile());
        jobToSet.setProjectName(job.getProjectName());
        jobToSet.setEnvironment(job.getEnvironment());
        jobToSet.setGenericInformations(job.getGenericInformations());
    }

    /**
     * Set some properties between the user task and internal task.
     *
     * @param task the user task.
     * @param taskToSet the internal task to set.
     */
    private static void setTaskCommonProperties(Job userJob, Task task, InternalTask taskToSet) {
        logger_dev.info("Setting task common properties");
        taskToSet.setDescription(task.getDescription());
        taskToSet.setPreciousResult(task.isPreciousResult());
        taskToSet.setName(task.getName());
        taskToSet.setPreScript(task.getPreScript());
        taskToSet.setPostScript(task.getPostScript());
        taskToSet.setCleaningScript(task.getCleaningScript());
        taskToSet.setSelectionScript(task.getSelectionScript());
        taskToSet.setResultPreview(task.getResultPreview());
        taskToSet.setWallTime(task.getWallTime());
        //Properties with priority between job and tasks
        if (task.getCancelJobOnErrorProperty().isSet()) {
            taskToSet.setCancelJobOnError(task.isCancelJobOnError());
        } else {
            taskToSet.setCancelJobOnError(userJob.isCancelJobOnError());
        }
        if (task.getRestartTaskOnErrorProperty().isSet()) {
            taskToSet.setRestartTaskOnError(task.getRestartTaskOnError());
        } else {
            taskToSet.setRestartTaskOnError(userJob.getRestartTaskOnError());
        }
        if (task.getMaxNumberOfExecutionProperty().isSet()) {
            taskToSet.setMaxNumberOfExecution(task.getMaxNumberOfExecution());
        } else {
            taskToSet.setMaxNumberOfExecution(userJob.getMaxNumberOfExecution());
        }
        //Generic informations
        taskToSet.setGenericInformations(task.getGenericInformations());
    }

    /**
     * Transform a String map into a BigString map to prepare Hibernate storage.
     *
     * @param arguments the String map to transform.
     * @return The same map but with big string type
     */
    private static Map<String, BigString> toBigStringMap(Map<String, String> arguments) {
        if (arguments == null) {
            return null;
        }
        Map<String, BigString> tmp = new HashMap<String, BigString>();
        for (Entry<String, String> e : arguments.entrySet()) {
            tmp.put(e.getKey(), new BigString(e.getValue()));
        }
        return tmp;
    }
}
