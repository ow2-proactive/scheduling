/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
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

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.task.JavaExecutableInitializer;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * JavaTaskLauncher is the class that will start a Java Process.
 *
 * @author The ProActive Team
 */
public class JavaTaskLauncher extends TaskLauncher {

    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.LAUNCHER);

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
     * @return a task result representing the result of this task execution.
     */
    @Override
    public TaskResult doTask(TaskTerminateNotification core, ExecutableContainer executableContainer,
            TaskResult... results) {
        long duration = -1;
        long sample = 0;
        try {

            //copy datas from OUTPUT or INPUT to local scratch
            copyInputDataToScratch();

            sample = System.currentTimeMillis();
            //launch pre script
            if (pre != null) {
                this.executePreScript(PAActiveObject.getNode());
            }
            duration = System.currentTimeMillis() - sample;

            // create the executable (will set the context class loader to the taskclassserver)
            currentExecutable = executableContainer.getExecutable();

            //init task
            callInternalInit(JavaExecutable.class, JavaExecutableInitializer.class, executableContainer
                    .createExecutableInitializer());

            Throwable exception = null;
            Serializable userResult = null;
            sample = System.currentTimeMillis();
            try {
                //launch task
                userResult = currentExecutable.execute(results);
            } catch (Throwable t) {
                exception = t;
            }
            duration += System.currentTimeMillis() - sample;

            //copy output file
            copyScratchDataToOutput();

            sample = System.currentTimeMillis();
            //launch post script
            if (post != null) {
                this.executePostScript(PAActiveObject.getNode(), exception == null);
            }
            duration += System.currentTimeMillis() - sample;

            //throw exception if needed
            if (exception != null) {
                throw exception;
            }

            //return result
            return new TaskResultImpl(taskId, userResult, this.getLogs(), duration);
        } catch (Throwable ex) {
            logger_dev.info("", ex);
            // exceptions are always handled at scheduler core level
            return new TaskResultImpl(taskId, ex, this.getLogs(), duration);
        } finally {
            terminateDataSpace();
            if (core != null) {
                // This call should be conditioned by the isKilled ... ?
                this.finalizeTask(core);
            } else {
                /* if core == null then don't finalize the task. An example when we don't want to finalize task is when using
                 * forked java task, then only finalizing loggers is enough.
                 */
                this.finalizeLoggers();
            }
        }
    }

}
