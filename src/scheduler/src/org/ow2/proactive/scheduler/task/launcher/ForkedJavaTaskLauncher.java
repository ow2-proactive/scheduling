/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.task.launcher;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.task.TaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.ExecutableContainerInitializer;
import org.ow2.proactive.scheduler.task.ForkedJavaExecutable;
import org.ow2.proactive.scheduler.task.ForkedJavaExecutableContainer;
import org.ow2.proactive.scheduler.task.ForkedJavaExecutableInitializer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * ForkedJavaTaskLauncher is a task launcher which will create a dedicated JVM for task execution.
 * It creates a JVM, creates a ProActive Node on that JVM, and in the end creates a JavaTaskLauncher active object
 * on that node. This JavaTaskLauncher will be responsible for task execution. 
 * 
 * @author The ProActive Team
 */
public class ForkedJavaTaskLauncher extends JavaTaskLauncher {

    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.LAUNCHER);

    private TaskLauncherInitializer initializer;
    //next fields are used to activate logs whenever the call to activate logs is done
    private boolean activateLogs = false;
    private AppenderProvider logSink;

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
    public TaskResult doTask(TaskTerminateNotification core, ExecutableContainer executableContainer,
            TaskResult... results) {
        long duration = -1;
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
            //create initializer
            ExecutableContainerInitializer eci = new ExecutableContainerInitializer();
            eci.setClassServer(((ForkedJavaExecutableContainer) executableContainer).getClassServer());
            fjei.getJavaExecutableContainer().init(eci);
            //initialize
            callInternalInit(ForkedJavaExecutable.class, ForkedJavaExecutableInitializer.class, fjei);

            //activate logs if necessary
            if (activateLogs) {
                ((ForkedJavaExecutable) currentExecutable).activateLogs(logSink);
            }

            //schedule timer at any time
            scheduleTimer();

            duration = System.currentTimeMillis();
            //launch task : here, result is obviously a TaskResult
            TaskResult userResult = (TaskResult) currentExecutable.execute(results);
            duration = System.currentTimeMillis() - duration;

            return userResult;
        } catch (Throwable ex) {
            logger_dev.info("", ex);
            return new TaskResultImpl(taskId, ex, this.getLogs(), duration, null);
        } finally {
            cancelTimer();
            finalizeTask(core);
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.task.launcher.TaskLauncher#activateLogs(org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider)
     */
    @Override
    public void activateLogs(AppenderProvider logSink) {
        this.logSink = logSink;
        if (currentExecutable == null) {
            activateLogs = true;
        } else {
            ((ForkedJavaExecutable) currentExecutable).activateLogs(logSink);
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.task.launcher.TaskLauncher#getLogs()
     */
    @Override
    public TaskLogs getLogs() {
        return ((ForkedJavaExecutable) currentExecutable).getLogs();
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
     * @see org.ow2.proactive.scheduler.task.launcher.TaskLauncher#initLoggers()
     */
    @Override
    protected void initLoggers() {
        //cancel default behavior defined in taskLauncher
        //we must NOT initialize loggers in forked tasklauncher
    }

    /**
     * @see org.ow2.proactive.scheduler.task.launcher.TaskLauncher#finalizeLoggers()
     */
    @Override
    protected void finalizeLoggers() {
        //cancel default behavior defined in taskLauncher
        //we must NOT finalize loggers in forked tasklauncher as it was not initialize
    }

    /**
     * @see org.ow2.proactive.scheduler.task.launcher.TaskLauncher#unsetEnv()
     */
    @Override
    protected void unsetEnv() {
        //cancel default behavior defined in taskLauncher
        //it is useless to unset environment in forked tasklauncher
    }

}
