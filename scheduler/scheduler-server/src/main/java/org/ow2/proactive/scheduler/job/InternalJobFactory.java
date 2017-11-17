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
package org.ow2.proactive.scheduler.job;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.exception.InternalException;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.FlowChecker;
import org.ow2.proactive.scheduler.common.job.factories.FlowError;
import org.ow2.proactive.scheduler.common.task.CommonAttribute;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.ScriptTask;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.core.OnErrorPolicyInterpreter;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.internal.InternalForkedScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.task.java.JavaClassScriptEngineFactory;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import com.google.common.base.Joiner;


/**
 * This is the factory to build Internal job with a job (user).
 * For the moment it performs a simple copy from userJob to InternalJob
 * and recreate the dependencies if needed.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class InternalJobFactory {

    private static final Logger logger = Logger.getLogger(InternalJobFactory.class);

    private static final OnErrorPolicyInterpreter onErrorPolicyInterpreter = new OnErrorPolicyInterpreter();

    /**
     * Create a new internal job with the given job (user).
     *
     * @param job the user job that will be used to create the internal job.
     * @return the created internal job.
     * @throws JobCreationException an exception if the factory cannot create the given job.
     */
    public static InternalJob createJob(Job job, Credentials cred) throws JobCreationException {
        InternalJob iJob;

        if (logger.isDebugEnabled()) {
            logger.debug("Create job '" + job.getName() + "' - " + job.getClass().getName());
        }

        switch (job.getType()) {
            case PARAMETER_SWEEPING:
                logger.error("The type of the given job is not yet implemented !");
                throw new JobCreationException("The type of the given job is not yet implemented !");
            case TASKSFLOW:
                iJob = createJob((TaskFlowJob) job);
                break;
            default:
                logger.error("The type of the given job is unknown !");
                throw new JobCreationException("The type of the given job is unknown !");
        }

        try {
            //set the job common properties
            iJob.setCredentials(cred);
            setJobCommonProperties(job, iJob);
            return iJob;
        } catch (Exception e) {
            logger.error("", e);
            throw new InternalException("Error while creating the internalJob !", e);
        }
    }

    /**
     * Create an internalTaskFlow job with the given task flow job (user)
     *
     * @param userJob the user job that will be used to create the internal job.
     * @return the created internal job.
     * @throws JobCreationException an exception if the factory cannot create the given job.
     */
    private static InternalJob createJob(TaskFlowJob userJob) throws JobCreationException {
        if (userJob.getTasks().size() == 0) {
            logger.info("Job '" + userJob.getName() + "' must contain tasks !");
            throw new JobCreationException("This job must contains tasks !");
        }

        // validate taskflow
        List<FlowChecker.Block> blocks = new ArrayList<>();
        FlowError err = FlowChecker.validate(userJob, blocks);
        if (err != null) {
            String e = "";

            e += "Invalid taskflow: " + err.getMessage() + "; context: " + err.getTask();
            logger.error(e);
            throw new JobCreationException(e, err);
        }

        InternalJob job = new InternalTaskFlowJob();
        // keep an initial job content
        job.setTaskFlowJob(userJob);
        Map<Task, InternalTask> tasksList = new LinkedHashMap<>();
        boolean hasPreciousResult = false;

        for (Task t : userJob.getTasks()) {
            tasksList.put(t, createTask(userJob, job, t));

            if (!hasPreciousResult) {
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
            if (it.getFlowScript() != null && it.getFlowScript().getActionType().equals(FlowActionType.IF.toString())) {
                String ifBranch = it.getFlowScript().getActionTarget();
                String elseBranch = it.getFlowScript().getActionTargetElse();
                String join = it.getFlowScript().getActionContinuation();
                List<InternalTask> joinedBranches = new ArrayList<>();

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

    private static InternalTask createTask(Job userJob, InternalJob internalJob, Task task)
            throws JobCreationException {
        // TODO: avoid branching with double dispatch
        if (task instanceof NativeTask) {
            return createTask(userJob, internalJob, (NativeTask) task);
        } else if (task instanceof JavaTask) {
            return createTask(userJob, internalJob, (JavaTask) task);
        } else if (task instanceof ScriptTask) {
            return createTask(userJob, internalJob, (ScriptTask) task);
        }

        String msg = "Unknown task type: " + task.getClass().getName();
        logger.info(msg);
        throw new JobCreationException(msg);
    }

    /**
     * Create an internal java Task with the given java task (user)
     *
     * @param task the user java task that will be used to create the internal java task.
     * @return the created internal task.
     * @throws JobCreationException an exception if the factory cannot create the given task.
     */
    @SuppressWarnings("unchecked")
    private static InternalTask createTask(Job userJob, InternalJob internalJob, JavaTask task)
            throws JobCreationException {
        InternalTask javaTask;

        if (task.getExecutableClassName() != null) {
            HashMap<String, byte[]> args = task.getSerializedArguments();

            try {
                if (isForkingTask()) {
                    javaTask = new InternalForkedScriptTask(new ScriptExecutableContainer(new TaskScript(new SimpleScript(task.getExecutableClassName(),
                                                                                                                          JavaClassScriptEngineFactory.JAVA_CLASS_SCRIPT_ENGINE_NAME,
                                                                                                                          new Serializable[] { args }))),
                                                            internalJob);
                    javaTask.setForkEnvironment(task.getForkEnvironment());
                    configureRunAsMe(task);
                } else {
                    javaTask = new InternalScriptTask(new ScriptExecutableContainer(new TaskScript(new SimpleScript(task.getExecutableClassName(),
                                                                                                                    JavaClassScriptEngineFactory.JAVA_CLASS_SCRIPT_ENGINE_NAME,
                                                                                                                    new Serializable[] { args }))),
                                                      internalJob);
                }
            } catch (InvalidScriptException e) {
                throw new JobCreationException(e);
            }
        } else {
            String msg = "You must specify your own executable task class to be launched (in every task)!";
            logger.info(msg);
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

    private static void configureRunAsMe(Task task) {
        if (isRunAsMeTask()) {
            task.setRunAsMe(true);
        }
    }

    /**
     * Create an internal native Task with the given native task (user)
     *
     * @param task the user native task that will be used to create the internal native task.
     * @return the created internal task.
     * @throws JobCreationException an exception if the factory cannot create the given task.
     */
    private static InternalTask createTask(Job userJob, InternalJob internalJob, NativeTask task)
            throws JobCreationException {
        if (((task.getCommandLine() == null) || (task.getCommandLine().length == 0))) {
            String msg = "The command line is null or empty and not generated !";
            logger.info(msg);
            throw new JobCreationException(msg);
        }

        try {
            String commandAndArguments = "\"" + Joiner.on("\" \"").join(task.getCommandLine()) + "\"";
            InternalTask scriptTask;
            if (isForkingTask()) {
                scriptTask = new InternalForkedScriptTask(new ScriptExecutableContainer(new TaskScript(new SimpleScript(commandAndArguments,
                                                                                                                        "native"))),
                                                          internalJob);
                configureRunAsMe(task);
            } else {
                scriptTask = new InternalScriptTask(new ScriptExecutableContainer(new TaskScript(new SimpleScript(commandAndArguments,
                                                                                                                  "native"))),
                                                    internalJob);
            }
            ForkEnvironment forkEnvironment = new ForkEnvironment();
            scriptTask.setForkEnvironment(forkEnvironment);
            //set task common properties
            setTaskCommonProperties(userJob, task, scriptTask);
            return scriptTask;

        } catch (Exception e) {
            throw new JobCreationException(e);
        }
    }

    private static InternalTask createTask(Job userJob, InternalJob internalJob, ScriptTask task)
            throws JobCreationException {
        InternalTask scriptTask;
        if (isForkingTask()) {
            scriptTask = new InternalForkedScriptTask(new ScriptExecutableContainer(task.getScript()), internalJob);
            configureRunAsMe(task);
        } else {
            scriptTask = new InternalScriptTask(new ScriptExecutableContainer(task.getScript()), internalJob);
        }
        //set task common properties
        try {
            setTaskCommonProperties(userJob, task, scriptTask);
        } catch (Exception e) {
            throw new JobCreationException(e);
        }
        return scriptTask;
    }

    private static boolean isForkingTask() {
        return PASchedulerProperties.TASK_FORK.getValueAsBoolean() || isRunAsMeTask();
    }

    private static boolean isRunAsMeTask() {
        return PASchedulerProperties.TASK_RUNASME.getValueAsBoolean();
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
        autoCopyfields(CommonAttribute.class, job, jobToSet);
        autoCopyfields(Job.class, job, jobToSet);
        jobToSet.setVariables(job.getVariables());
        jobToSet.setGenericInformation(job.getGenericInformation());
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
        autoCopyfields(CommonAttribute.class, task, taskToSet);
        autoCopyfields(Task.class, task, taskToSet);

        //special behavior
        if (onErrorPolicyInterpreter.notSetOrNone(task)) {
            taskToSet.setOnTaskError(userJob.getOnTaskErrorProperty().getValue());
        } else {
            taskToSet.setOnTaskError(task.getOnTaskErrorProperty().getValue());
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
    private static <T> void autoCopyfields(Class<T> klass, T from, T to)
            throws IllegalArgumentException, IllegalAccessException {
        for (Field f : klass.getDeclaredFields()) {
            if (!Modifier.isPrivate(f.getModifiers()) && !Modifier.isStatic(f.getModifiers())) {
                f.setAccessible(true);
                Object newValue = f.get(from);
                if (newValue != null || f.get(to) == null) {
                    f.set(to, newValue);
                }
            }
        }
    }
}
