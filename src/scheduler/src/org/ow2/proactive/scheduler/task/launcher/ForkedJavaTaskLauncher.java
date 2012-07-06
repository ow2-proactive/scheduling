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
package org.ow2.proactive.scheduler.task.launcher;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.exception.ForkedJavaTaskException;
import org.ow2.proactive.scheduler.common.task.SimpleTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.exception.ForkedJVMProcessException;
import org.ow2.proactive.scheduler.exception.IllegalProgressException;
import org.ow2.proactive.scheduler.exception.ProgressPingerException;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.ExecutableContainerInitializer;
import org.ow2.proactive.scheduler.task.ForkedJavaExecutable;
import org.ow2.proactive.scheduler.task.ForkedJavaExecutableContainer;
import org.ow2.proactive.scheduler.task.ForkedJavaExecutableInitializer;
import org.ow2.proactive.scheduler.task.ForkerStarterCallback;
import org.ow2.proactive.scheduler.task.TaskResultImpl;


/**
 * ForkedJavaTaskLauncher is a task launcher which will create a dedicated JVM for task execution.
 * It creates a JVM, creates a ProActive Node on that JVM, and in the end creates a JavaTaskLauncher active object
 * on that node. This JavaTaskLauncher will be responsible for task execution. 
 * 
 * @author The ProActive Team
 */
@ActiveObject
public class ForkedJavaTaskLauncher extends JavaTaskLauncher implements ForkerStarterCallback {

    public static final Logger logger_dev = ProActiveLogger.getLogger(ForkedJavaTaskLauncher.class);

    private TaskLauncherInitializer initializer;

    /**
     * Create a new instance of ForkedJavaTaskLauncher.<br/>
     * Used by ProActive active object creation process.
     *
     */
    public ForkedJavaTaskLauncher() {
    }

    /**
     * Constructor of the forked java task launcher.
     * CONSTRUCTOR USED BY THE SCHEDULER CORE : do not remove.
     *
     * @param initializer represents the class that contains information to initialize this task launcher.
     */
    public ForkedJavaTaskLauncher(TaskLauncherInitializer initializer) {
        super(initializer);
        this.initializer = initializer;
    }

    /**
     * Method responsible for creating a a dedicated JVM, execution of the task on this JVM and collecting result
     * @see org.ow2.proactive.scheduler.task.launcher.JavaTaskLauncher#doTask(org.ow2.proactive.scheduler.common.TaskTerminateNotification, org.ow2.proactive.scheduler.task.ExecutableContainer, org.ow2.proactive.scheduler.common.task.TaskResult[])
     */
    @Override
    public void doTask(TaskTerminateNotification core, ExecutableContainer executableContainer,
            TaskResult... results) {
        long duration = -1;
        TaskResultImpl taskResult = null;
        try {

            // create the executable (will set the context class loader to the taskclassserver)
            currentExecutable = executableContainer.getExecutable();

            //init task
            ForkedJavaExecutableInitializer fjei = (ForkedJavaExecutableInitializer) executableContainer
                    .createExecutableInitializer();
            replaceIterationTags(fjei);
            fjei.setJavaTaskLauncherInitializer(initializer);
            //decrypt credentials if needed
            if (executableContainer.isRunAsUser()) {
                decrypter.setCredentials(executableContainer.getCredentials());
                fjei.setDecrypter(decrypter);
            }

            /*
             * Initialize dataspaces since it can be used in envScript.
             * 
             * Log of the JavaTaskLauncher starts after envScript was executed, so it doesn't
             * contain envScript output, to have complete log file it is specially handled by the
             * ForkedJavaTaskLauncher.
             */
            this.initDataSpaces();
            fjei.setDataspaces(SCRATCH, INPUT, OUTPUT, GLOBAL);

            //create initializer
            ExecutableContainerInitializer eci = new ExecutableContainerInitializer();
            eci.setClassServer(((ForkedJavaExecutableContainer) executableContainer).getClassServer());
            fjei.getJavaExecutableContainer().init(eci);
            // if an exception occurs in init method, unwrapp the InvocationTargetException
            // the result of the execution is the user level exception
            try {
                callInternalInit(ForkedJavaExecutable.class, ForkedJavaExecutableInitializer.class, fjei);
            } catch (InvocationTargetException e) {
                throw e.getCause() != null ? e.getCause() : e;
            }

            //schedule timer at any time
            scheduleTimer();

            duration = System.nanoTime();
            //launch task : here, result is a taskLauncher if everything terminated without error,
            //result is an integer if forkedJVM has exited abnormally (integer contains the error code)
            Serializable userResult;
            try {
                userResult = currentExecutable.execute(results);
            } catch (Throwable t) {
                throw t;
            } finally {
                //compute duration in any cases (exception or not)
                duration = System.nanoTime() - duration;
            }

            if (storeLogs) {
                // copy only task output, others files are copied by the forked JavaTaskLauncher
                copyScratchDataToOutput(getTaskOutputSelectors());
            }

            if (userResult instanceof TaskResult) {
                taskResult = (TaskResultImpl) PAFuture.getFutureValue(userResult);
                // Override the logs since they are stored on forker side
                taskResult.setLogs(getLogs());
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
            logger_dev.info("", ex);
            if (this.getLogs() == null) {
                taskResult = new TaskResultImpl(taskId, ex, new SimpleTaskLogs("", ex.toString()),
                    duration / 1000000, null);
            } else {
                taskResult = new TaskResultImpl(taskId, ex, this.getLogs(), duration / 1000000, null);
            }
        } finally {
            // finalize doTask
            terminateDataSpace();
            cancelTimer();
            finalizeTask(core, taskResult);
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.task.launcher.TaskLauncher#initEnv()
     */
    @Override
    protected void initEnv() {
        //cancel default behavior defined in taskLauncher
        //it is useless to init environment in forked tasklauncher
    }

    /**
     * @see org.ow2.proactive.scheduler.task.launcher.TaskLauncher#unsetEnv()
     */
    @Override
    protected void unsetEnv() {
        //cancel default behavior defined in taskLauncher
        //it is useless to unset environment in forked tasklauncher
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
        if (this.currentExecutable == null) {
            //not yet started
            return 0;
        } else {
            try {
                int progress = currentExecutable.getProgress();//(1)
                if (progress < 0) {
                    logger_dev.warn("Returned progress (" + progress + ") is negative, return 0 instead.");
                    return 0;
                } else if (progress > 100) {
                    logger_dev.warn("Returned progress (" + progress +
                        ") is greater than 100, return 100 instead.");
                    return 100;
                } else {
                    return progress;
                }
            } catch (IllegalProgressException ipe) {
                //can be thrown by (1) if userExecutable.getProgress() method threw an exception
                //exception comes from javaTaskLauncher.getProgress()
                throw ipe;
            } catch (Throwable t) {
                //communication error with the forked JVM (probably dead VM)
                throw new ForkedJVMProcessException("Forked JVM seems to be dead", t);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * This method must be immediate service to get the callback of the new starter forked process
     */
    @ImmediateService
    public void callback(Node n) {
        ((ForkerStarterCallback) currentExecutable).callback(n);
    }

}
