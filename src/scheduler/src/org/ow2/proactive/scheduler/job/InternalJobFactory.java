/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.job;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.exception.InternalException;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.FlowChecker;
import org.ow2.proactive.scheduler.common.job.factories.FlowError;
import org.ow2.proactive.scheduler.common.task.CommonAttribute;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.task.ForkedJavaExecutableContainer;
import org.ow2.proactive.scheduler.task.JavaExecutableContainer;
import org.ow2.proactive.scheduler.task.NativeExecutableContainer;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.internal.InternalForkedJavaTask;
import org.ow2.proactive.scheduler.task.internal.InternalJavaTask;
import org.ow2.proactive.scheduler.task.internal.InternalNativeTask;
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
public class InternalJobFactory {

    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.FACTORY);

    /**
     * Create a new internal job with the given job (user).
     *
     * @param job the user job that will be used to create the internal job.
     * @return the created internal job.
     * @throws JobCreationException an exception if the factory cannot create the given job.
     */
    public static InternalJob createJob(Job job, Credentials cred) throws JobCreationException {
        InternalJob iJob = null;

        logger_dev.info("Create job '" + job.getName() + "' - " + job.getClass().getName());

        switch (job.getType()) {
            case PARAMETER_SWEEPING:
                logger_dev.error("The type of the given job is not yet implemented !");
                throw new JobCreationException("The type of the given job is not yet implemented !");
            case TASKSFLOW:
                iJob = createJob((TaskFlowJob) job);
                break;
            default:
                logger_dev.error("The type of the given job is unknown !");
                throw new JobCreationException("The type of the given job is unknown !");
        }

        try {
            //set the job common properties
            iJob.setCredentials(cred);
            setJobCommonProperties(job, iJob);
            return iJob;
        } catch (Exception e) {
            logger_dev.error("", e);
            throw new InternalException("Error while creating the internalJob !", e);
        }
    }

    /**
     * Create an internalTaskFlow job with the given task flow job (user)
     *
     * @param job the user job that will be used to create the internal job.
     * @return the created internal job.
     * @throws JobCreationException an exception if the factory cannot create the given job.
     */
    private static InternalJob createJob(TaskFlowJob userJob) throws JobCreationException {
        if (userJob.getTasks().size() == 0) {
            logger_dev.info("Job '" + userJob.getName() + "' must contain tasks !");
            throw new JobCreationException("This job must contains tasks !");
        }

        int maxTask = PASchedulerProperties.JOB_FACTOR.getValueAsInt();
        if (userJob.getTasks().size() > maxTask) {
            logger_dev.info("Job '" + userJob.getName() + "' cannot contain more than " + maxTask +
                " tasks !");
            throw new JobCreationException("Job cannot contain more than " + maxTask + " tasks !");
        }

        // validate taskflow
        List<FlowChecker.Block> blocks = new ArrayList<FlowChecker.Block>();
        FlowError err = FlowChecker.validate(userJob, blocks);
        if (err != null) {
            String e = "";

            e += "Invalid taskflow: " + err.getMessage() + "; context: " + err.getTask();
            logger_dev.error(e);
            throw new JobCreationException(e, err);
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

        for (Entry<Task, InternalTask> entry : tasksList.entrySet()) {
            if (entry.getKey().getDependencesList() != null) {
                for (Task t : entry.getKey().getDependencesList()) {
                    entry.getValue().addDependence(tasksList.get(t));
                }
            }

            job.addTask(entry.getValue());
        }

        // tag matching blocks in InternalTasks
        for (InternalTask it : tasksList.values()) {
            for (FlowChecker.Block block : blocks) {
                if (it.getName().equals(block.start.element.getName())) {
                    it.setMatchingBlock(block.end.element.getName());
                }
                if (it.getName().equals(block.end.element.getName())) {
                    it.setMatchingBlock(block.start.element.getName());
                }
            }
        }

        // create if/else/join weak dependencies
        for (InternalTask it : tasksList.values()) {

            // it performs an IF action
            if (it.getFlowScript() != null &&
                it.getFlowScript().getActionType().equals(FlowActionType.IF.toString())) {
                String ifBranch = it.getFlowScript().getActionTarget();
                String elseBranch = it.getFlowScript().getActionTargetElse();
                String join = it.getFlowScript().getActionContinuation();
                List<InternalTask> joinedBranches = new ArrayList<InternalTask>();

                // find the ifBranch task
                for (InternalTask it2 : tasksList.values()) {
                    if (it2.getName().equals(ifBranch)) {
                        it2.setIfBranch(it);
                        String match = it2.getMatchingBlock();
                        // find its matching block task
                        if (match == null) {
                            // no match: single task
                            joinedBranches.add(it2);
                        } else {
                            for (InternalTask it3 : tasksList.values()) {
                                if (it3.getName().equals(match)) {
                                    joinedBranches.add(it3);
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }

                // find the elseBranch task
                for (InternalTask it2 : tasksList.values()) {
                    if (it2.getName().equals(elseBranch)) {
                        it2.setIfBranch(it);

                        String match = it2.getMatchingBlock();
                        // find its matching block task
                        if (match == null) {
                            // no match: single task
                            joinedBranches.add(it2);
                        } else {
                            for (InternalTask it3 : tasksList.values()) {
                                if (it3.getName().equals(match)) {
                                    joinedBranches.add(it3);
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }

                // find the joinBranch task
                for (InternalTask it2 : tasksList.values()) {
                    if (it2.getName().equals(join)) {
                        it2.setJoinedBranches(joinedBranches);
                    }
                }
            }
        }
        return job;
    }

    private static InternalTask createTask(Job userJob, Task task) throws JobCreationException {
        //dispatch task creation
        if (task instanceof NativeTask) {
            return createTask(userJob, (NativeTask) task);
        } else if (task instanceof JavaTask) {
            return createTask(userJob, (JavaTask) task);
        } else {
            String msg = "The task you intend to add is unknown ! (type : " + task.getClass().getName() + ")";
            logger_dev.info(msg);
            throw new JobCreationException(msg);
        }
    }

    /**
     * Create an internal java Task with the given java task (user)
     *
     * @param task the user java task that will be used to create the internal java task.
     * @return the created internal task.
     * @throws JobCreationException an exception if the factory cannot create the given task.
     */
    @SuppressWarnings("unchecked")
    private static InternalTask createTask(Job userJob, JavaTask task) throws JobCreationException {
        InternalJavaTask javaTask;

        if (task.getExecutableClassName() != null) {
            // HACK HACK HACK : Get arguments for the task
            Map<String, byte[]> args = null;
            try {
                Field f = JavaTask.class.getDeclaredField(JavaTask.ARGS_FIELD_NAME);
                f.setAccessible(true);
                args = (Map<String, byte[]>) f.get(task);
            } catch (Exception e) {
                // should not happen...
                logger_dev.fatal("Internal error : cannot retreive arguments for task " + task.getName(), e);
                throw new Error("Internal error : implementation must be revised.", e);
            }

            if (task.isFork()) {
                ForkedJavaExecutableContainer fjec = new ForkedJavaExecutableContainer(task
                        .getExecutableClassName(), args);
                fjec.setForkEnvironment(task.getForkEnvironment());
                javaTask = new InternalForkedJavaTask(fjec);
            } else {
                javaTask = new InternalJavaTask(new JavaExecutableContainer(task.getExecutableClassName(),
                    args));
            }
        } else {
            String msg = "You must specify your own executable task class to be launched (in every task) !";
            logger_dev.info(msg);
            throw new JobCreationException(msg);
        }

        //set task common properties
        try {
            setTaskCommonProperties(userJob, task, javaTask);
        } catch (Exception e) {
            throw new JobCreationException(e);
        }
        return javaTask;
    }

    /**
     * Create an internal native Task with the given native task (user)
     *
     * @param task the user native task that will be used to create the internal native task.
     * @return the created internal task.
     * @throws JobCreationException an exception if the factory cannot create the given task.
     */
    private static InternalTask createTask(Job userJob, NativeTask task) throws JobCreationException {
        if (((task.getCommandLine() == null) || (task.getCommandLine().length == 0)) &&
            (task.getGenerationScript() == null)) {
            String msg = "The command line is null or empty and not generated !";
            logger_dev.info(msg);
            throw new JobCreationException(msg);
        }
        InternalNativeTask nativeTask = new InternalNativeTask(new NativeExecutableContainer(task
                .getCommandLine(), task.getGenerationScript(), task.getWorkingDir()));
        //set task common properties
        try {
            setTaskCommonProperties(userJob, task, nativeTask);
        } catch (Exception e) {
            throw new JobCreationException(e);
        }
        return nativeTask;
    }

    /**
     * Set some properties between the user Job and internal Job.
     *
     * @param job the user job.
     * @param jobToSet the internal job to set.
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    private static void setJobCommonProperties(Job job, InternalJob jobToSet)
            throws IllegalArgumentException, IllegalAccessException {
        logger_dev.info("Setting job common properties");

        autoCopyfields(CommonAttribute.class, job, jobToSet);
        autoCopyfields(Job.class, job, jobToSet);
        //special behavior
        jobToSet.setPriority(job.getPriority());
    }

    /**
     * Set some properties between the user task and internal task.
     *
     * @param task the user task.
     * @param taskToSet the internal task to set.
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    private static void setTaskCommonProperties(Job userJob, Task task, InternalTask taskToSet)
            throws IllegalArgumentException, IllegalAccessException {
        logger_dev.debug("Setting task common properties");

        autoCopyfields(CommonAttribute.class, task, taskToSet);
        autoCopyfields(Task.class, task, taskToSet);

        //special behavior
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
    }

    /**
     * Copy fields belonging to 'cFrom' from 'from' to 'to'.
     * Will only iterate on non-private field.
     * Private fields in 'cFrom' won't be set in 'to'.
     *
     * @param <T> check type given as argument is equals or under this type.
     * @param klass the klass in which to find the fields
     * @param from the T object in which to get the value
     * @param to the T object in which to set the value
     */
    private static <T> void autoCopyfields(Class<T> klass, T from, T to) throws IllegalArgumentException,
            IllegalAccessException {
        for (Field f : klass.getDeclaredFields()) {
            if (!Modifier.isPrivate(f.getModifiers()) && !Modifier.isStatic(f.getModifiers())) {
                f.setAccessible(true);
                f.set(to, f.get(from));
            }
        }
    }
}
