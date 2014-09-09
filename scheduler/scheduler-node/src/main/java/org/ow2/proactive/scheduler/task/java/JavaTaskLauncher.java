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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.task.java;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.exception.TaskAbortedException;
import org.ow2.proactive.scheduler.common.exception.WalltimeExceededException;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.task.executable.JavaStandaloneExecutable;
import org.ow2.proactive.scheduler.common.task.executable.internal.ExecutableInitializer;
import org.ow2.proactive.scheduler.common.task.executable.internal.JavaExecutableInitializerImpl;
import org.ow2.proactive.scheduler.common.task.executable.internal.JavaStandaloneExecutableInitializer;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.apache.log4j.Logger;


/**
 * JavaTaskLauncher is the class that will start a Java Process.
 *
 * @author The ProActive Team
 */
@ActiveObject
public class JavaTaskLauncher extends TaskLauncher {

    public static final Logger logger = Logger.getLogger(JavaTaskLauncher.class);

    protected boolean nodeConfigured = false;

    /**
     * ProActive Empty Constructor
     */
    public JavaTaskLauncher() {
    }

    /**
     * Constructor of the java task launcher.
     * CONSTRUCTOR USED BY THE SCHEDULER CORE : do not remove.
     *
     * @param initializer represents the class that contains information to initialize this task launcher.
     */
    public JavaTaskLauncher(TaskLauncherInitializer initializer) {
        super(initializer);
    }

    /**
     * Execute the user task as an active object.
     *
     * @param core The scheduler core to be notify or null if the finalizeTask method is not to be called
     * @param executableContainer contains the task to execute
     * @param results the possible results from parent tasks.(if task flow)
     */
    @Override
    public void doTask(TaskTerminateNotification core, ExecutableContainer executableContainer,
            TaskResult... results) {
        doTaskAndGetResult(core, executableContainer, results);
    }

    public void doTaskAndGetResult(final TaskTerminateNotification terminateNotificationStub,
            ExecutableContainer executableContainer, TaskResult... results) {
        logger.info("Starting Task " + taskId.getReadableName());
        long duration = -1;
        long sample;

        // Executable result (res or ex)
        Throwable exception = null;
        Serializable userResult = null;
        // TaskResult produced by doTask
        TaskResultImpl res;
        try {
            //init dataspace
            executableGuard.initDataSpaces();
            replaceTagsInDataspaces();

            updatePropagatedVariables(results);

            // create the executable (will set the context class loader to the taskclassserver)
            executableGuard.initialize(executableContainer.getExecutable());

            executableGuard.copyInputDataToScratch();

            // set exported vars
            this.setPropagatedProperties(results);

            //start walltime if needed
            if (isWallTime()) {
                scheduleTimer();
            }

            //launch pre script
            if (pre != null) {
                sample = System.nanoTime();
                executableGuard.executePreScript();
                duration += System.nanoTime() - sample;
            }

            //init task
            ExecutableInitializer initializer = createExecutableInitializer(executableContainer);

            setPropagatedVariables((JavaExecutableInitializerImpl) initializer, getPropagatedVariables());
            replaceIterationTags(initializer);

            // if an exception occurs in init method, unwrapp the InvocationTargetException
            // the result of the execution is the user level exception
            try {
                Executable executable = executableContainer.getExecutable();
                if (executable instanceof JavaExecutable) {
                    executableGuard.callInternalInit(JavaExecutable.class,
                            JavaExecutableInitializerImpl.class, initializer);
                } else if (executable instanceof JavaStandaloneExecutable) {
                    executableGuard.callInternalInit(JavaStandaloneExecutable.class,
                            JavaStandaloneExecutableInitializer.class, initializer);
                } else {
                    throw new IllegalArgumentException("Unsupported Java Executable class : " +
                        executable.getClass());
                }

            } catch (InvocationTargetException e) {
                throw e.getCause() != null ? e.getCause() : e;
            }
            sample = System.nanoTime();
            try {
                //launch task
                userResult = executableGuard.execute(results);
                // update propagated variables map after task execution so
                // that any updates that occur during task execution will be
                // visible in post script execution.
                setPropagatedVariables(executableGuard.use().getVariables());
            } catch (Throwable t) {
                exception = t;
            }
            duration += System.nanoTime() - sample;

            //for the next two steps, task could be killed anywhere

            if (post != null) {
                sample = System.nanoTime();
                //launch post script
                executableGuard.executePostScript(exception == null);
                duration += System.nanoTime() - sample;
            }

            executableGuard.copyScratchDataToOutput();
            logger.info("Task " + taskId.getReadableName() + " terminated without error");
        } catch (Throwable ex) {
            logger.debug("Exception occured while running task " + this.taskId.getReadableName() + ": ", ex);
            exception = ex;
            userResult = null;
        } finally {
            if (executableGuard.wasWalltimed()) {
                // killed by a walltime
                res = new TaskResultImpl(taskId, new WalltimeExceededException("Walltime of " + wallTime +
                    " ms reached on task " + taskId.getReadableName()), null, duration / 1000000, null);
            } else if (executableGuard.wasKilled()) {
                // standard kill
                res = new TaskResultImpl(taskId, new TaskAbortedException("Task " + taskId.getReadableName() +
                    " has been killed"), null, duration / 1000000, null);
            } else {
                // set the result
                if (exception != null) {
                    res = new TaskResultImpl(taskId, exception, null, duration / 1000000, null);
                } else {
                    res = new TaskResultImpl(taskId, userResult, null, duration / 1000000, null);
                }
                try {
                    // logs have to be retrieved after flowscript exec if any
                    if (flow != null) {
                        // *WARNING* : flow action is set in res UNLESS an exception is thrown !
                        // see FlowAction.getDefaultAction()
                        executableGuard.executeFlowScript(res);
                    }
                } catch (Throwable e) {
                    // task result is now the exception thrown by flowscript
                    // flowaction is set to default
                    res = new TaskResultImpl(taskId, e, null, duration / 1000000, null);
                    // action is set to default as the script was not evaluated
                    res.setAction(FlowAction.getDefaultAction(this.flow));
                }
                res.setPropagatedProperties(retreivePropagatedProperties());
                attachPropagatedVariables(res);
            }

            // logs are set even if the task is killed
            res.setLogs(this.getLogs());

            finalizeTask(terminateNotificationStub, res);

        }
    }

    protected void setPropagatedVariables(JavaExecutableInitializerImpl init,
            Map<String, Serializable> variables) {
        init.setPropagatedVariables(SerializationUtil.serializeVariableMap(variables));
    }

    /**
     * Replaces iteration and replication index syntactic macros
     * in various string locations across the task descriptor
     *
     * @param init the executable initializer containing the Java arguments
     */
    protected void replaceIterationTags(ExecutableInitializer init) {
        JavaExecutableInitializerImpl jinit = (JavaExecutableInitializerImpl) init;
        try {
            Map<String, Serializable> args = jinit.getArguments(Thread.currentThread()
                    .getContextClassLoader());
            for (Entry<String, Serializable> arg : args.entrySet()) {
                if (arg.getValue() instanceof String) {
                    String str = ((String) arg.getValue()).replace(ITERATION_INDEX_TAG, "" +
                        this.iterationIndex);
                    str = str.replace(REPLICATION_INDEX_TAG, "" + this.replicationIndex);
                    jinit.setArgument(arg.getKey(), str);
                }
            }
        } catch (Throwable e) {
            // a no classDefFoundError can occurs with forked java task and user-type args.
            // see SCHEDULING-1288
        }
    }

}
