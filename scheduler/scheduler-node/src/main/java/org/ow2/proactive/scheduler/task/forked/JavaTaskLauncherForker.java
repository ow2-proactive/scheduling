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
package org.ow2.proactive.scheduler.task.forked;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.core.InputOutputSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceInstanceInfo;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceType;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.exception.ForkedJavaTaskException;
import org.ow2.proactive.scheduler.common.exception.TaskAbortedException;
import org.ow2.proactive.scheduler.common.exception.WalltimeExceededException;
import org.ow2.proactive.scheduler.common.task.SimpleTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.exception.ForkedJVMProcessException;
import org.ow2.proactive.scheduler.exception.IllegalProgressException;
import org.ow2.proactive.scheduler.exception.ProgressPingerException;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.ExecutableContainerInitializer;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.java.JavaTaskLauncher;
import org.ow2.proactive.utils.Formatter;
import org.apache.log4j.Logger;


/**
 * JavaTaskLauncherForker is a task launcher which will create a dedicated JVM for task execution.
 * It creates a JVM, creates a ProActive Node on that JVM, and in the end creates a JavaTaskLauncher active object
 * on that node. This JavaTaskLauncher will be responsible for task execution. 
 * 
 * @author The ProActive Team
 */
@ActiveObject
public class JavaTaskLauncherForker extends JavaTaskLauncher implements ForkerStarterCallback {

    public static final Logger logger = Logger.getLogger(JavaTaskLauncherForker.class);

    private TaskLauncherInitializer initializer;

    /**
     * Create a new instance of JavaTaskLauncherForker.<br/>
     * Used by ProActive active object creation process.
     *
     */
    public JavaTaskLauncherForker() {
    }

    /**
     * Constructor of the forked java task launcher.
     * CONSTRUCTOR USED BY THE SCHEDULER CORE : do not remove.
     *
     * @param initializer represents the class that contains information to initialize this task launcher.
     */
    public JavaTaskLauncherForker(TaskLauncherInitializer initializer) {
        super(initializer);
        this.initializer = initializer;
    }

    /**
     * Method responsible for creating a a dedicated JVM, execution of the task on this JVM and collecting result
     * @see org.ow2.proactive.scheduler.task.java.JavaTaskLauncher#doTask(org.ow2.proactive.scheduler.common.TaskTerminateNotification, org.ow2.proactive.scheduler.task.ExecutableContainer, org.ow2.proactive.scheduler.common.task.TaskResult[])
     */
    @Override
    public void doTask(TaskTerminateNotification taskTerminateNotification,
            ExecutableContainer executableContainer, TaskResult... results) {
        long duration = -1;
        TaskResultImpl taskResult = null;
        try {

            // create the executable (will set the context class loader to the taskclassserver)
            executableGuard.initialize(executableContainer.getExecutable());

            updatePropagatedVariables(results);

            //start walltime if needed
            if (isWallTime()) {
                scheduleTimer();
            }

            //init task
            ForkedJavaExecutableInitializer fjei = (ForkedJavaExecutableInitializer) createExecutableInitializer(executableContainer);
            setPropagatedVariables(fjei, getPropagatedVariables());
            fjei.setJavaTaskLauncherInitializer(initializer);

            decrypter.setCredentials(executableContainer.getCredentials());
            fjei.setDecrypter(decrypter);

            fjei.setTaskId(taskId);

            /*
             * Initialize dataspaces since it can be used in envScript.
             * 
             * Log of the JavaTaskLauncher starts after envScript was executed, so it doesn't
             * contain envScript output, to have complete log file it is specially handled by the
             * JavaTaskLauncherForker.
             */
            executableGuard.initDataSpaces();
            fjei.setDataspaces(SCRATCH, INPUT, OUTPUT, GLOBAL);

            //create initializer
            ExecutableContainerInitializer eci = new ExecutableContainerInitializer();
            eci.setClassServer(((ForkedJavaExecutableContainer) executableContainer).getClassServer());
            fjei.getJavaExecutableContainer().init(eci);
            // if an exception occurs in init method, unwrapp the InvocationTargetException
            // the result of the execution is the user level exception
            try {
                callInternalInit(JavaForkerExecutable.class, ForkedJavaExecutableInitializer.class, fjei);
            } catch (InvocationTargetException e) {
                throw e.getCause() != null ? e.getCause() : e;
            }

            duration = System.nanoTime();
            //launch task : here, result is a taskLauncher if everything terminated without error,
            //result is an integer if forkedJVM has exited abnormally (integer contains the error code)
            Serializable userResult;
            try {
                flushStreams();
                userResult = executableGuard.execute(results);
                // update propagated variables map after task execution, but
                // before post script execution
                setPropagatedVariables(executableGuard.use().getVariables());
            } finally {
                //compute duration in any cases (exception or not)
                duration = System.nanoTime() - duration;
            }

            if (storeLogs) {
                // copy only task output, others files are copied by the forked JavaTaskLauncher

                // logs must be finalized before copying the task output, otherwise truncated files will be sent
                try {
                    this.finalizeLoggers();
                } catch (RuntimeException e) {
                    // exception should not be thrown to the scheduler core
                    // the result has been computed and must be returned !
                    logger.warn("Loggers are not shutdown !", e);
                }
                executableGuard
                        .copyScratchDataToOutput(getTaskLogsSelectors(OutputAccessMode.TransferToOutputSpace));
            }
            executableGuard
                    .copyScratchDataToOutput(getTaskLogsSelectors(OutputAccessMode.TransferToUserSpace));

            if (userResult instanceof TaskResult) {
                // Override the logs since they are stored on forker side
                taskResult = (TaskResultImpl) userResult;
            } else {
                Integer ec = (Integer) userResult;
                if (ec == 0) {
                    taskResult = new TaskResultImpl(taskId, ec, getLogs(), duration / 1000000);
                } else {
                    Throwable t = new ForkedJavaTaskException(
                        "Forked JVM process has been terminated with exit code " + ec, ec);
                    taskResult = new TaskResultImpl(taskId, t, getLogs(), duration / 1000000);
                }
            }
        } catch (Throwable ex) {
            logger.info("", ex);
            if (this.getLogs() == null) {
                taskResult = new TaskResultImpl(taskId, ex, new SimpleTaskLogs("", ex.toString()),
                    duration / 1000000, null);
            } else {
                taskResult = new TaskResultImpl(taskId, ex, this.getLogs(), duration / 1000000, null);
            }
        } finally {
            if (executableGuard.wasWalltimed()) {
                taskResult = new TaskResultImpl(taskId, new WalltimeExceededException("Walltime of " +
                    wallTime + " ms reached on task " + taskId.getReadableName()), null, duration / 1000000,
                    null);
            } else if (executableGuard.wasKilled()) {
                taskResult = new TaskResultImpl(taskId, new TaskAbortedException("Task " +
                    taskId.getReadableName() + " has been killed"), null, duration / 1000000, null);
            }

            taskResult.setLogs(getLogs());
            finalizeTask(taskTerminateNotification, taskResult);
        }
    }

    @Override
    protected void initDataSpaces() {
        super.initDataSpaces();

        try {
            // override scratch space to share it with forked task, make it unique in case of task failure
            String sharedScratchSpaceName = "shared_forker_" + taskId.value() + "_" +
                new UniqueID().getCanonString();
            InputOutputSpaceConfiguration sharedScratchSpaceConfiguration = InputOutputSpaceConfiguration
                    .createConfiguration(SCRATCH.getRealURI(), null, null, sharedScratchSpaceName,
                            SpaceType.OUTPUT);
            namingService.register(new SpaceInstanceInfo(getDataSpacesApplicationId(),
                sharedScratchSpaceConfiguration));

            // use output for RW access between multiple AOs (forked and forker)
            SCRATCH = PADataSpaces.resolveOutput(sharedScratchSpaceName);
        } catch (Throwable t) {
            logger.error("There was a problem while initializing shared scratch space", t);
            this.logDataspacesStatus("There was a problem while initializing shared scratch space",
                    DataspacesStatusLevel.ERROR);
            this.logDataspacesStatus(Formatter.stackTraceToString(t), DataspacesStatusLevel.ERROR);
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.task.TaskLauncher#initEnv()
     */
    @Override
    protected void initEnv() {
        //cancel default behavior defined in taskLauncher
        //it is useless to init environment in forked tasklauncher
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IllegalProgressException if the userExecutable.getProgress() method throws an exception
     * @throws ForkedJVMProcessException if the forked JVM is dead
     */
    @Override
    @ImmediateService
    public int getProgress() throws ProgressPingerException {
        resetLogContextForImmediateService();
        return executableGuard.getProgress();
    }

    /**
     * {@inheritDoc}
     *
     * This method must be immediate service to get the callback of the new starter forked process
     */
    @ImmediateService
    public void callback(Node n) {
        resetLogContextForImmediateService();
        logger.debug("Callback from node " + n.getNodeInformation().getURL());
        ((ForkerStarterCallback) executableGuard.use()).callback(n);
    }

}
